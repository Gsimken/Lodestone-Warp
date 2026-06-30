package dev.simke.lodestoneteleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class LodestoneCommands {
	private LodestoneCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerConfiguredCommand(dispatcher);
		});
	}

	private static void registerConfiguredCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		LodestoneConfig config = LodestoneConfig.get();
		String commandName = config.commandName;
		String fallback = config.fallbackCommandName;

		if (isAvailable(dispatcher, fallback)) {
			dispatcher.register(root(fallback));
			LodestoneTeleportMod.LOGGER.info("Registered Lodestone Warps fallback command /{}.", fallback);
		} else {
			LodestoneTeleportMod.LOGGER.warn("Could not register Lodestone Warps fallback command /{} because it is already in use.", fallback);
		}

		if (commandName.equals(fallback)) {
			return;
		}
		if (isAvailable(dispatcher, commandName)) {
			dispatcher.register(root(commandName));
			LodestoneTeleportMod.LOGGER.info("Registered Lodestone Warps command /{}.", commandName);
			return;
		}
		LodestoneTeleportMod.LOGGER.warn("Command /{} was already registered; use /{} instead.", commandName, fallback);
	}

	private static boolean isAvailable(CommandDispatcher<CommandSourceStack> dispatcher, String commandName) {
		return dispatcher.getRoot().getChild(commandName) == null;
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root(String command) {
		return Commands.literal(command)
				.then(Commands.literal("tp")
					.requires(LodestonePermissions::canUse)
					.then(Commands.argument("destination", StringArgumentType.greedyString())
						.executes(context -> teleport(context.getSource(), StringArgumentType.getString(context, "destination")))))
				.then(Commands.literal("edit")
					.requires(LodestonePermissions::canRename)
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> edit(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("rename")
					.requires(LodestonePermissions::canRename)
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("name", MessageArgument.message())
							.executes(context -> rename(context.getSource(), StringArgumentType.getString(context, "id"), MessageArgument.getMessage(context, "name").getString())))))
				.then(Commands.literal("remove")
					.requires(LodestonePermissions::canRemove)
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("unlink")
					.requires(LodestonePermissions::canRemove)
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("global")
					.requires(LodestonePermissions::canSetGlobal)
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(LodestoneCommands::suggestLodestoneIds)
						.then(Commands.argument("enabled", BoolArgumentType.bool())
							.executes(context -> setGlobal(context.getSource(), StringArgumentType.getString(context, "id"), BoolArgumentType.getBool(context, "enabled"))))))
				.then(Commands.literal("discover")
					.requires(LodestonePermissions::canAdmin)
					.then(Commands.literal("grant")
						.then(Commands.argument("player", EntityArgument.player())
							.then(Commands.argument("id", StringArgumentType.word())
								.suggests(LodestoneCommands::suggestLodestoneIds)
								.executes(context -> grantDiscovery(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "id"))))))
					.then(Commands.literal("revoke")
						.then(Commands.argument("player", EntityArgument.player())
							.then(Commands.argument("id", StringArgumentType.word())
								.suggests(LodestoneCommands::suggestLodestoneIds)
								.executes(context -> revokeDiscovery(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "id"))))))
					.then(Commands.literal("list")
						.then(Commands.argument("player", EntityArgument.player())
							.executes(context -> listDiscoveries(context.getSource(), EntityArgument.getPlayer(context, "player"))))))
				.then(Commands.literal("list")
					.requires(LodestonePermissions::canAdmin)
					.executes(context -> list(context.getSource())))
				.then(Commands.literal("reload")
					.requires(LodestonePermissions::canConfig)
					.executes(context -> reloadConfig(context.getSource())))
				.then(Commands.literal("config")
					.requires(LodestonePermissions::canConfig)
					.executes(context -> openConfig(context.getSource()))
					.then(Commands.literal("list")
						.executes(context -> listConfig(context.getSource())))
					.then(Commands.literal("get")
						.then(Commands.argument("key", StringArgumentType.word())
							.suggests(LodestoneCommands::suggestConfigKeys)
							.executes(context -> getConfig(context.getSource(), StringArgumentType.getString(context, "key")))))
					.then(Commands.literal("set")
						.then(Commands.argument("key", StringArgumentType.word())
							.suggests(LodestoneCommands::suggestConfigKeys)
							.then(Commands.argument("value", StringArgumentType.greedyString())
								.executes(context -> setConfig(context.getSource(), StringArgumentType.getString(context, "key"), StringArgumentType.getString(context, "value")))))));
	}

	static int teleport(CommandSourceStack source, String destination) throws CommandSyntaxException {
		return teleport(source, destination, false);
	}

	static void completeCast(ServerPlayer player, String destination) {
		try {
			teleport(player.createCommandSourceStack(), destination, true);
		} catch (CommandSyntaxException exception) {
			player.sendSystemMessage(LodestoneText.text("error.action_failed", "Could not run the lodestone action."));
		}
	}

	private static int teleport(CommandSourceStack source, String destination, boolean fromCast) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (!LodestonePermissions.canUse(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.use", "You do not have permission to use lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		Optional<LodestoneLocation> maybeLocation = resolveTeleportDestination(source, player, data, destination);
		if (maybeLocation.isEmpty()) {
			return 0;
		}

		LodestoneLocation location = maybeLocation.get();
		ServerLevel destinationLevel = source.getServer().getLevel(location.dimension());
		if (destinationLevel == null) {
			source.sendFailure(LodestoneText.text("error.dimension_unloaded", "The destination dimension is not loaded."));
			return 0;
		}
		if (!LodestoneConfig.get().allowCrossDimension && !player.level().dimension().equals(location.dimension())) {
			source.sendFailure(LodestoneText.text("error.cross_dimension_disabled", "Cross-dimension teleport is disabled."));
			return 0;
		}
		if (!destinationLevel.getBlockState(location.pos()).is(Blocks.LODESTONE)) {
			data.remove(location.dimension(), location.pos());
			source.sendFailure(LodestoneText.text("error.destination_removed", "The destination no longer has a lodestone and was removed."));
			return 0;
		}
		if (!isNearRegisteredLodestone(player, data)) {
			source.sendFailure(LodestoneText.text("error.need_near_lodestone", "You must be near a registered lodestone to teleport."));
			return 0;
		}
		long cooldown = LodestonePermissions.canBypassCooldown(source) ? 0L : LodestoneTeleportCooldowns.remainingSeconds(player);
		if (cooldown > 0L) {
			source.sendFailure(LodestoneText.text("error.cooldown", "You must wait %s seconds before teleporting again.", cooldown));
			return 0;
		}

		boolean bypassCost = LodestonePermissions.canBypassCost(source);
		LodestoneTeleportCost cost = LodestoneTeleportCost.between(player, location);
		if (!hasCost(player, cost)) {
			source.sendFailure(LodestoneText.text("error.need_cost", "You need %s.", LodestoneText.cost(cost)));
			return 0;
		}

		if (!fromCast && LodestoneConfig.get().teleportCastSeconds > 0) {
			if (LodestoneTeleportCasts.isCasting(player)) {
				source.sendFailure(LodestoneText.text("teleport.cast_already", "You are already casting a teleport."));
				return 0;
			}
			LodestoneTeleportCasts.start(player, location.id());
			return 1;
		}

		LodestoneTeleportEffects.before(player);
		ServerPlayer teleportedPlayer = teleportPlayer(player, destinationLevel, location);
		if (teleportedPlayer == null) {
			source.sendFailure(LodestoneText.text("error.action_failed", "Could not run the lodestone action."));
			return 0;
		}

		if (!bypassCost) {
			consumeCost(teleportedPlayer, cost);
		}
		if (!LodestonePermissions.canBypassCooldown(teleportedPlayer)) {
			LodestoneTeleportCooldowns.mark(teleportedPlayer);
		}
		LodestoneTeleportEffects.after(teleportedPlayer);
		teleportedPlayer.sendSystemMessage(LodestoneText.text(
			"arrived",
			"You arrived at \"%s\" (%s, %s, %s, %s).",
			location.displayName(),
			location.pos().getX(),
			location.pos().getY(),
			location.pos().getZ(),
			LodestoneText.dimension(location.dimension())
		));
		return 1;
	}

	private static Optional<LodestoneLocation> resolveTeleportDestination(CommandSourceStack source, ServerPlayer player, LodestoneSavedData data, String destination) {
		String clean = destination.trim();
		Optional<LodestoneLocation> byId = data.get(clean);
		if (byId.isPresent()) {
			if (!LodestoneDiscovery.canSee(player, data, byId.get())) {
				source.sendFailure(LodestoneText.text("error.not_discovered", "You have not discovered that lodestone."));
				return Optional.empty();
			}
			return byId;
		}

		List<LodestoneLocation> matches = new ArrayList<>();
		for (LodestoneLocation location : data.all()) {
			if (!LodestoneDiscovery.canSee(player, data, location)) {
				continue;
			}
			if (location.displayName().equalsIgnoreCase(clean)) {
				matches.add(location);
			}
		}
		if (matches.size() == 1) {
			return Optional.of(matches.getFirst());
		}
		if (matches.size() > 1) {
			sendDuplicateDestinationMessage(source, clean, matches);
			return Optional.empty();
		}
		source.sendFailure(LodestoneText.text("error.missing_destination", "That destination no longer exists."));
		return Optional.empty();
	}

	private static void sendDuplicateDestinationMessage(CommandSourceStack source, String name, List<LodestoneLocation> matches) {
		source.sendFailure(LodestoneText.text("error.duplicate_destination_name", "More than one lodestone is named \"%s\". Choose one:", name));
		for (LodestoneLocation location : matches) {
			CompoundTag payload = new CompoundTag();
			payload.putString("action", "tp");
			payload.putString("id", location.id());
			MutableComponent entry = Component.literal("- " + location.displayName() + " ")
				.withStyle(ChatFormatting.YELLOW)
				.append(Component.literal("(" + location.pos().getX() + ", " + location.pos().getY() + ", " + location.pos().getZ() + ", ")
					.withStyle(ChatFormatting.GRAY))
				.append(LodestoneText.dimension(location.dimension()).copy().withStyle(ChatFormatting.GRAY))
				.append(Component.literal(") ")
					.withStyle(ChatFormatting.GRAY))
				.append(LodestoneText.text("button.teleport", "[TP]").withStyle(style -> style
					.withColor(ChatFormatting.AQUA)
					.withUnderlined(true)
					.withClickEvent(new ClickEvent.Custom(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))));
			source.sendSystemMessage(entry);
		}
	}

	static int rename(CommandSourceStack source, String id, String name) {
		if (!LodestonePermissions.canRename(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.rename", "You do not have permission to rename lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		if (!data.rename(id, name)) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "Could not find that lodestone."));
			return 0;
		}
		String cleanName = data.get(id).map(LodestoneLocation::displayName).orElse(name.trim());
		source.sendSuccess(() -> LodestoneText.text("renamed", "Lodestone renamed to %s.", cleanName), false);
		return 1;
	}

	static int edit(CommandSourceStack source, String id) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (!LodestonePermissions.canRename(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.rename", "You do not have permission to rename lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		Optional<LodestoneLocation> location = data.get(id);
		if (location.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "Could not find that lodestone."));
			return 0;
		}
		LodestoneDialogs.showRename(player, location.get());
		return 1;
	}

	static int remove(CommandSourceStack source, String id) {
		if (!LodestonePermissions.canRemove(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.remove", "You do not have permission to remove registered lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		Optional<LodestoneLocation> location = data.get(id);
		if (location.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "Could not find that lodestone."));
			return 0;
		}
		data.remove(location.get().dimension(), location.get().pos());
		source.sendSuccess(() -> LodestoneText.text("removed", "Unlinked lodestone warp: %s.", location.get().displayName()), false);
		return 1;
	}

	static int setGlobal(CommandSourceStack source, String id, boolean global) {
		if (!LodestonePermissions.canSetGlobal(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.global", "You do not have permission to manage global lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		Optional<LodestoneLocation> location = data.get(id);
		if (location.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "I could not find that lodestone."));
			return 0;
		}
		data.setGlobal(id, global);
		source.sendSuccess(() -> LodestoneText.text(global ? "global.enabled" : "global.disabled", global ? "Lodestone marked global: %s" : "Lodestone is no longer global: %s", location.get().displayName()), true);
		return 1;
	}

	static int grantDiscovery(CommandSourceStack source, ServerPlayer target, String id) {
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		Optional<LodestoneLocation> location = data.get(id);
		if (location.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "I could not find that lodestone."));
			return 0;
		}
		data.discover(target.getUUID(), id);
		source.sendSuccess(() -> LodestoneText.text("discover.granted", "Granted %s discovery of %s.", target.getName().getString(), location.get().displayName()), true);
		return 1;
	}

	static int revokeDiscovery(CommandSourceStack source, ServerPlayer target, String id) {
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		Optional<LodestoneLocation> location = data.get(id);
		if (location.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "I could not find that lodestone."));
			return 0;
		}
		data.revokeDiscovery(target.getUUID(), id);
		source.sendSuccess(() -> LodestoneText.text("discover.revoked", "Revoked %s discovery of %s.", target.getName().getString(), location.get().displayName()), true);
		return 1;
	}

	static int listDiscoveries(CommandSourceStack source, ServerPlayer target) {
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		source.sendSystemMessage(LodestoneText.text("discover.list_header", "%s has discovered:", target.getName().getString()));
		int count = 0;
		for (String id : data.discoveredIds(target.getUUID())) {
			Optional<LodestoneLocation> location = data.get(id);
			if (location.isEmpty()) {
				continue;
			}
			source.sendSystemMessage(LodestoneText.text("list.entry", "- %s: %s (%s)", location.get().id(), location.get().displayName(), LodestoneText.dimension(location.get().dimension())));
			count++;
		}
		if (count == 0) {
			source.sendSystemMessage(LodestoneText.text("discover.list_empty", "No discovered lodestones."));
		}
		return Math.max(1, count);
	}

	static int reloadConfig(CommandSourceStack source) {
		if (!LodestonePermissions.canConfig(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.config", "You do not have permission to configure Lodestone Warps."));
			return 0;
		}
		LodestoneConfig.load();
		source.sendSuccess(() -> LodestoneText.text("config.server.reloaded", "Lodestone Warps config reloaded."), true);
		return 1;
	}

	static int openConfig(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (!LodestonePermissions.canConfig(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.config", "You do not have permission to configure Lodestone Warps."));
			return 0;
		}
		LodestoneDialogs.showConfig(player, LodestoneConfigOptions.ALL, "");
		return 1;
	}

	static int listConfig(CommandSourceStack source) {
		if (!LodestonePermissions.canConfig(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.config", "You do not have permission to configure Lodestone Warps."));
			return 0;
		}
		source.sendSystemMessage(LodestoneText.text("config.server.list_header", "Lodestone Warps config keys:"));
		for (LodestoneConfigOptions.Option option : LodestoneConfigOptions.all()) {
			source.sendSystemMessage(configEntry(option));
		}
		return LodestoneConfigOptions.all().size();
	}

	static int getConfig(CommandSourceStack source, String key) {
		if (!LodestonePermissions.canConfig(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.config", "You do not have permission to configure Lodestone Warps."));
			return 0;
		}
		Optional<LodestoneConfigOptions.Option> option = LodestoneConfigOptions.get(key);
		if (option.isEmpty()) {
			source.sendFailure(LodestoneText.text("config.server.unknown_key", "Unknown config key: %s", key));
			return 0;
		}
		source.sendSystemMessage(configEntry(option.get()));
		return 1;
	}

	static int setConfig(CommandSourceStack source, String key, String value) {
		if (!LodestonePermissions.canConfig(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.config", "You do not have permission to configure Lodestone Warps."));
			return 0;
		}
		Optional<LodestoneConfigOptions.Option> option = LodestoneConfigOptions.get(key);
		if (option.isEmpty()) {
			source.sendFailure(LodestoneText.text("config.server.unknown_key", "Unknown config key: %s", key));
			return 0;
		}
		try {
			option.get().apply(value);
			LodestoneConfig.save();
		} catch (IllegalArgumentException exception) {
			source.sendFailure(LodestoneText.text("config.server.invalid_value", "Invalid value for %s. Accepted: %s", option.get().id(), option.get().acceptedValues()));
			return 0;
		}
		source.sendSuccess(() -> LodestoneText.text("config.server.changed", "Set %s to %s.", option.get().id(), option.get().currentValue()), true);
		if (option.get().id().equals("command_name") || option.get().id().equals("fallback_command_name")) {
			source.sendSystemMessage(LodestoneText.text("config.server.restart_required", "Restart the server for command name changes to take effect."));
		}
		return 1;
	}

	private static Component configEntry(LodestoneConfigOptions.Option option) {
		String command = "/" + LodestoneConfig.get().commandName + " config set " + option.id() + " " + option.currentValue();
		MutableComponent entry = Component.literal("- " + option.id() + " = ")
			.withStyle(ChatFormatting.GRAY)
			.append(Component.literal(option.currentValue()).withStyle(option.isDefault() ? ChatFormatting.WHITE : ChatFormatting.YELLOW))
			.append(Component.literal(" "))
			.append(LodestoneText.text("config.default", "Default: %s", option.defaultValue()).withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal(" "))
			.append(LodestoneText.text("config.server.set_suggest", "[Set]").withStyle(style -> style
				.withColor(ChatFormatting.AQUA)
				.withUnderlined(true)
				.withClickEvent(new ClickEvent.SuggestCommand(command))));
		return entry;
	}

	private static CompletableFuture<Suggestions> suggestConfigKeys(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
		for (LodestoneConfigOptions.Option option : LodestoneConfigOptions.all()) {
			if (option.id().startsWith(remaining)) {
				builder.suggest(option.id());
			}
		}
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestLodestoneIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
		for (LodestoneLocation location : LodestoneSavedData.from(context.getSource().getLevel()).all()) {
			if (location.id().startsWith(remaining)) {
				builder.suggest(location.id());
			}
		}
		return builder.buildFuture();
	}

	private static int list(CommandSourceStack source) {
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		if (data.all().isEmpty()) {
			source.sendSystemMessage(LodestoneText.text("list.empty", "No registered lodestones."));
			return 1;
		}
		source.sendSystemMessage(LodestoneText.text("list.header", "Registered lodestones:"));
		for (LodestoneLocation location : data.all()) {
			source.sendSystemMessage(listEntry(location));
		}
		return data.all().size();
	}

	private static Component listEntry(LodestoneLocation location) {
		return LodestoneText.text("list.entry", "- %s: %s (%s)", location.id(), location.displayNameWithGlobalPrefix(), LodestoneText.dimension(location.dimension()))
			.copy()
			.append(Component.literal(" "))
			.append(actionButton("button.teleport", "[TP]", ChatFormatting.AQUA, "tp", location.id()))
			.append(Component.literal(" "))
			.append(actionButton("button.rename_short", "[\u270e]", ChatFormatting.GOLD, "edit", location.id()))
			.append(Component.literal(" "))
			.append(actionButton("button.remove", "[X]", ChatFormatting.RED, "remove", location.id()));
	}

	private static Component actionButton(String textKey, String fallback, ChatFormatting color, String action, String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", action);
		payload.putString("id", id);
		return LodestoneText.text(textKey, fallback).withStyle(style -> style
			.withColor(color)
			.withUnderlined(true)
			.withClickEvent(new ClickEvent.Custom(LodestoneCustomActions.ACTION_ID, Optional.of(payload))));
	}

	private static boolean isNearRegisteredLodestone(ServerPlayer player, LodestoneSavedData data) {
		int range = LodestoneConfig.get().teleportSourceRange;
		if (range <= 0) {
			return true;
		}
		double maxDistance = range * range;
		ServerLevel level = (ServerLevel) player.level();
		for (LodestoneLocation location : data.all()) {
			if (!location.dimension().equals(level.dimension())) {
				continue;
			}
			if (player.blockPosition().distSqr(location.pos()) > maxDistance) {
				continue;
			}
			if (level.getBlockState(location.pos()).is(Blocks.LODESTONE)) {
				return true;
			}
		}
		return false;
	}

	private static ServerPlayer teleportPlayer(ServerPlayer player, ServerLevel destinationLevel, LodestoneLocation location) {
		Vec3 target = new Vec3(
			location.pos().getX() + 0.5D,
			location.pos().getY() + 1.0D,
			location.pos().getZ() + 0.5D
		);
		TeleportTransition transition = new TeleportTransition(
			destinationLevel,
			target,
			Vec3.ZERO,
			player.getYRot(),
			player.getXRot(),
			TeleportTransition.PLACE_PORTAL_TICKET
		);
		return player.teleport(transition);
	}

	private static boolean hasCost(ServerPlayer player, LodestoneTeleportCost cost) {
		if (cost.amount() <= 0 || player.isCreative() || LodestonePermissions.canBypassCost(player)) {
			return true;
		}
		if (cost.usesXpLevels()) {
			return player.experienceLevel >= cost.amount();
		}
		Item costItem = cost.item();
		Inventory inventory = player.getInventory();
		int remaining = cost.amount();

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && stack.getItem() == costItem) {
				remaining -= stack.getCount();
				if (remaining <= 0) {
					break;
				}
			}
		}
		if (remaining > 0) {
			return false;
		}
		return true;
	}

	private static void consumeCost(ServerPlayer player, LodestoneTeleportCost cost) {
		if (cost.amount() <= 0 || player.isCreative()) {
			return;
		}
		if (cost.usesXpLevels()) {
			player.giveExperienceLevels(-cost.amount());
			return;
		}
		Inventory inventory = player.getInventory();
		int remaining = cost.amount();
		for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && stack.getItem() == cost.item()) {
				int removed = Math.min(remaining, stack.getCount());
				stack.shrink(removed);
				remaining -= removed;
			}
		}
	}
}
