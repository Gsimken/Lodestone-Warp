package dev.simke.lodestoneteleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource())));
	}

	static int teleport(CommandSourceStack source, String destination) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (!LodestonePermissions.canUse(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.use", "No tienes permiso para usar lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		Optional<LodestoneLocation> maybeLocation = resolveTeleportDestination(source, data, destination);
		if (maybeLocation.isEmpty()) {
			return 0;
		}

		LodestoneLocation location = maybeLocation.get();
		ServerLevel destinationLevel = source.getServer().getLevel(location.dimension());
		if (destinationLevel == null) {
			source.sendFailure(LodestoneText.text("error.dimension_unloaded", "La dimension del destino no esta cargada."));
			return 0;
		}
		if (!LodestoneConfig.get().allowCrossDimension && !player.level().dimension().equals(location.dimension())) {
			source.sendFailure(LodestoneText.text("error.cross_dimension_disabled", "El teleport entre dimensiones esta desactivado."));
			return 0;
		}
		if (!destinationLevel.getBlockState(location.pos()).is(Blocks.LODESTONE)) {
			data.remove(location.dimension(), location.pos());
			source.sendFailure(LodestoneText.text("error.destination_removed", "El destino ya no tiene una lodestone y fue eliminado."));
			return 0;
		}
		if (!isNearRegisteredLodestone(player, data)) {
			source.sendFailure(LodestoneText.text("error.need_near_lodestone", "Debes estar cerca de una lodestone registrada para teletransportarte."));
			return 0;
		}

		LodestoneTeleportCost cost = LodestoneTeleportCost.between(player, location);
		if (!hasCost(player, cost)) {
			source.sendFailure(LodestoneText.text("error.need_cost", "Necesitas %s.", LodestoneText.cost(cost)));
			return 0;
		}

		ServerPlayer teleportedPlayer = teleportPlayer(player, destinationLevel, location);
		if (teleportedPlayer == null) {
			source.sendFailure(LodestoneText.text("error.action_failed", "No se pudo ejecutar la accion de lodestone."));
			return 0;
		}

		consumeCost(teleportedPlayer, cost);
		teleportedPlayer.sendSystemMessage(LodestoneText.text(
			"arrived",
			"Has llegado a \"%s\" (%s, %s, %s, %s).",
			location.displayName(),
			location.pos().getX(),
			location.pos().getY(),
			location.pos().getZ(),
			LodestoneText.dimension(location.dimension())
		));
		return 1;
	}

	private static Optional<LodestoneLocation> resolveTeleportDestination(CommandSourceStack source, LodestoneSavedData data, String destination) {
		String clean = destination.trim();
		Optional<LodestoneLocation> byId = data.get(clean);
		if (byId.isPresent()) {
			return byId;
		}

		List<LodestoneLocation> matches = new ArrayList<>();
		for (LodestoneLocation location : data.all()) {
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
		source.sendFailure(LodestoneText.text("error.missing_destination", "Ese destino ya no existe."));
		return Optional.empty();
	}

	private static void sendDuplicateDestinationMessage(CommandSourceStack source, String name, List<LodestoneLocation> matches) {
		source.sendFailure(LodestoneText.text("error.duplicate_destination_name", "Hay mas de una lodestone llamada \"%s\". Elige una:", name));
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
			source.sendFailure(LodestoneText.text("error.no_permission.rename", "No tienes permiso para renombrar lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		if (!data.rename(id, name)) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "No encontre esa lodestone."));
			return 0;
		}
		String cleanName = data.get(id).map(LodestoneLocation::displayName).orElse(name.trim());
		source.sendSuccess(() -> LodestoneText.text("renamed", "Lodestone renombrada a %s.", cleanName), false);
		return 1;
	}

	static int edit(CommandSourceStack source, String id) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		if (!LodestonePermissions.canRename(source)) {
			source.sendFailure(LodestoneText.text("error.no_permission.rename", "No tienes permiso para renombrar lodestones."));
			return 0;
		}
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		Optional<LodestoneLocation> location = data.get(id);
		if (location.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.lodestone_not_found", "No encontre esa lodestone."));
			return 0;
		}
		LodestoneDialogs.showRename(player, location.get());
		return 1;
	}

	private static int list(CommandSourceStack source) {
		LodestoneSavedData data = LodestoneSavedData.from(source.getLevel());
		if (data.all().isEmpty()) {
			source.sendSystemMessage(LodestoneText.text("list.empty", "No hay lodestones registradas."));
			return 1;
		}
		source.sendSystemMessage(LodestoneText.text("list.header", "Lodestones registradas:"));
		for (LodestoneLocation location : data.all()) {
			source.sendSystemMessage(LodestoneText.text("list.entry", "- %s: %s (%s)", location.id(), location.displayName(), LodestoneText.dimension(location.dimension())));
		}
		return data.all().size();
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
		if (cost.amount() <= 0 || player.isCreative()) {
			return true;
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
