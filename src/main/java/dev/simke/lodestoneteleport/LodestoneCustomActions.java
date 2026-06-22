package dev.simke.lodestoneteleport;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class LodestoneCustomActions {
	public static final Identifier ACTION_ID = Identifier.fromNamespaceAndPath(LodestoneTeleportMod.MOD_ID, "dialog");

	private LodestoneCustomActions() {
	}

	public static boolean handle(ServerPlayer player, ServerboundCustomClickActionPacket packet) {
		if (!ACTION_ID.equals(packet.id()) || packet.payload().isEmpty()) {
			return false;
		}
		if (!(packet.payload().get() instanceof CompoundTag payload)) {
			return true;
		}

		String action = payload.getStringOr("action", "");
		String id = payload.getStringOr("id", "");
		if (id.isBlank()) {
			player.sendSystemMessage(LodestoneText.text("error.invalid_action", "Accion de lodestone invalida."));
			return true;
		}

		try {
			return switch (action) {
				case "tp" -> {
					LodestoneCommands.teleport(player.createCommandSourceStack(), id);
					yield true;
				}
				case "edit" -> {
					LodestoneCommands.edit(player.createCommandSourceStack(), id);
					yield true;
				}
				case "search" -> {
					LodestoneSavedData data = LodestoneSavedData.from(player.level());
					data.get(id).ifPresent(location -> LodestoneDialogs.showDestinations(player, location, readField(payload, "query")));
					yield true;
				}
				case "rename" -> {
					LodestoneCommands.rename(player.createCommandSourceStack(), id, readField(payload, "name"));
					yield true;
				}
				default -> false;
			};
		} catch (CommandSyntaxException exception) {
			player.sendSystemMessage(LodestoneText.text("error.action_failed", "No se pudo ejecutar la accion de lodestone."));
			return true;
		}
	}

	private static String readField(CompoundTag payload, String key) {
		Tag tag = payload.get(key);
		if (tag != null) {
			return tag.asString().orElse("");
		}
		return "";
	}
}
