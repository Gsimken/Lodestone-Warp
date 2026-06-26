package dev.simke.lodestoneteleport;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class LodestoneCommands {
	private LodestoneCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(root("lodestone_warps"));
			dispatcher.register(root("lodestone_teleport"));
			dispatcher.register(root("waystone_teleport"));
		});
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root(String command) {
		return Commands.literal(command)
				.then(Commands.literal("tp")
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> teleport(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("edit")
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> edit(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("rename")
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("name", MessageArgument.message())
							.executes(context -> rename(context.getSource(), StringArgumentType.getString(context, "id"), MessageArgument.getMessage(context, "name").getString())))))
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource())));
	}

	static int teleport(CommandSourceStack source, String id) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		Optional<LodestoneLocation> maybeLocation = data.get(id);
		if (maybeLocation.isEmpty()) {
			source.sendFailure(LodestoneText.text("error.missing_destination", "Ese destino ya no existe."));
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

	static int rename(CommandSourceStack source, String id, String name) {
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
