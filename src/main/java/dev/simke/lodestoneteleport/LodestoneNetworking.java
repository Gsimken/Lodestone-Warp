package dev.simke.lodestoneteleport;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.simke.lodestoneteleport.network.LodestoneActionPayload;
import dev.simke.lodestoneteleport.network.LodestoneOpenScreenPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

public final class LodestoneNetworking {
	private LodestoneNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(LodestoneOpenScreenPayload.TYPE, LodestoneOpenScreenPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(LodestoneActionPayload.TYPE, LodestoneActionPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(LodestoneActionPayload.TYPE, LodestoneNetworking::handleAction);
	}

	public static boolean canUseClientScreen(ServerPlayer player) {
		return ServerPlayNetworking.canSend(player, LodestoneOpenScreenPayload.TYPE);
	}

	public static void openClientScreen(ServerPlayer player, LodestoneLocation current) {
		ServerPlayNetworking.send(player, new LodestoneOpenScreenPayload(screenData(player, current)));
	}

	public static void openClientRenameScreen(ServerPlayer player, LodestoneLocation location) {
		ServerPlayNetworking.send(player, new LodestoneOpenScreenPayload(editData(player, location, location.id())));
	}

	private static CompoundTag screenData(ServerPlayer player, LodestoneLocation current) {
		CompoundTag root = new CompoundTag();
		root.putString("currentId", current.id());
		root.putString("currentName", current.displayName());
		root.putString("currentVisibility", current.visibility().id());
		String currentOwnerName = ownerName(current);
		root.putString("currentOwner", currentOwnerName);
		root.putString("currentOwnerName", currentOwnerName);
		root.putString("currentOwnerUuid", ownerUuid(current));
		root.putString("currentDimension", LodestoneText.dimension(current.dimension()).getString());
		root.putInt("currentX", current.pos().getX());
		root.putInt("currentY", current.pos().getY());
		root.putInt("currentZ", current.pos().getZ());
		root.putBoolean("canRename", LodestonePermissions.canRename(player));
		root.putBoolean("canEditCurrent", canEdit(player, current));
		root.putBoolean("viewingAll", LodestoneDiscovery.canSeeAll(player));
		long cooldownSeconds = LodestonePermissions.canBypassCooldown(player) ? 0L : LodestoneTeleportCooldowns.remainingSeconds(player);
		root.putInt("cooldownSeconds", (int) Math.min(Integer.MAX_VALUE, cooldownSeconds));

		ListTag destinations = new ListTag();
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		for (LodestoneLocation destination : data.all()) {
			if (destination.id().equals(current.id())) {
				continue;
			}
			if (!LodestoneDiscovery.canSee(player, data, destination)) {
				continue;
			}
			LodestoneTeleportCost cost = LodestoneTeleportCost.between(player, destination);
			CompoundTag item = new CompoundTag();
			String destinationOwnerName = ownerName(destination);
			item.putString("id", destination.id());
			item.putString("name", destination.displayName());
			item.putString("owner", destinationOwnerName);
			item.putString("ownerName", destinationOwnerName);
			item.putString("ownerUuid", ownerUuid(destination));
			item.putBoolean("global", destination.global());
			item.putString("visibility", destination.visibility().id());
			item.putBoolean("canEdit", canEdit(player, destination));
			item.putString("dimension", LodestoneText.dimension(destination.dimension()).getString());
			item.putInt("x", destination.pos().getX());
			item.putInt("y", destination.pos().getY());
			item.putInt("z", destination.pos().getZ());
			item.putString("cost", LodestoneText.cost(cost).getString());
			item.putString("costType", cost.type());
			item.putString("costItem", cost.itemId());
			item.putInt("costAmount", cost.amount());
			LodestoneTeleportAvailability availability = LodestoneTeleportAvailability.check(player, data, destination, cost);
			item.putBoolean("canTeleport", availability.canTeleport());
			item.putString("disabledReason", availability.reason());
			item.putInt("cooldownSeconds", availability.canTeleport() ? 0 : (int) Math.min(Integer.MAX_VALUE, cooldownSeconds));
			destinations.add(item);
		}
		root.put("destinations", destinations);
		return root;
	}

	public static CompoundTag editData(ServerPlayer player, LodestoneLocation location, String returnId) {
		CompoundTag data = new CompoundTag();
		data.putString("screen", "edit");
		data.putString("id", location.id());
		data.putString("name", location.displayName());
		data.putString("visibility", location.visibility().id());
		data.putString("returnId", returnId == null || returnId.isBlank() ? location.id() : returnId);
		data.putBoolean("canRename", LodestonePermissions.canRename(player, location));
		data.putBoolean("canRemove", LodestonePermissions.canRemove(player, location));
		data.putBoolean("canPrivate", LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.PRIVATE));
		data.putBoolean("canDiscoverable", LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.DISCOVERABLE));
		data.putBoolean("canGlobal", LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.GLOBAL));
		return data;
	}

	private static boolean canEdit(ServerPlayer player, LodestoneLocation location) {
		return LodestonePermissions.canRename(player, location)
			|| LodestonePermissions.canRemove(player, location)
			|| LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.PRIVATE)
			|| LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.DISCOVERABLE)
			|| LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.GLOBAL);
	}

	private static String ownerName(LodestoneLocation location) {
		if (!LodestoneConfig.get().resolveOwnerNames) {
			return "unknown";
		}
		return location.ownerName() == null || location.ownerName().isBlank() ? "unknown" : location.ownerName();
	}

	private static String ownerUuid(LodestoneLocation location) {
		return LodestoneConfig.get().resolveOwnerNames ? location.ownerUuid().toString() : "";
	}

	private static void handleAction(LodestoneActionPayload payload, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			CompoundTag data = payload.data();
			String action = data.getStringOr("action", "");
			String id = data.getStringOr("id", "");
			try {
				switch (action) {
					case "tp" -> LodestoneCommands.teleport(context.player().createCommandSourceStack(), id);
					case "rename" -> {
						LodestoneCommands.rename(context.player().createCommandSourceStack(), id, data.getStringOr("name", ""));
						refreshAfterRename(context.player(), data.getStringOr("returnId", ""));
					}
					case "save_edit" -> {
						if (LodestoneCommands.saveEdit(context.player(), id, data.getStringOr("name", ""), data.getStringOr("visibility", ""))) {
							refreshAfterRename(context.player(), data.getStringOr("returnId", ""));
						}
					}
					case "edit" -> LodestoneSavedData.from(context.player().level()).get(id)
						.ifPresentOrElse(
							location -> ServerPlayNetworking.send(context.player(), new LodestoneOpenScreenPayload(editData(context.player(), location, data.getStringOr("returnId", "")))),
							() -> context.player().sendSystemMessage(LodestoneText.text("error.lodestone_not_found", "I could not find that lodestone."))
						);
					case "visibility" -> {
						LodestoneCommands.setVisibility(context.player().createCommandSourceStack(), id, data.getStringOr("visibility", ""));
						refreshAfterRename(context.player(), data.getStringOr("returnId", ""));
					}
					case "remove" -> {
						LodestoneCommands.remove(context.player().createCommandSourceStack(), id);
						refreshAfterRename(context.player(), data.getStringOr("returnId", ""));
					}
					default -> context.player().sendSystemMessage(LodestoneText.text("error.action_failed", "Could not run the lodestone action."));
				}
			} catch (CommandSyntaxException exception) {
				context.player().sendSystemMessage(LodestoneText.text("error.action_failed", "Could not run the lodestone action."));
			}
		});
	}

	private static void refreshAfterRename(ServerPlayer player, String returnId) {
		if (returnId.isBlank() || !canUseClientScreen(player)) {
			return;
		}
		LodestoneSavedData.from(player.level()).get(returnId).ifPresent(location -> openClientScreen(player, location));
	}
}
