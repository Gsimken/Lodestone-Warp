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
		if (requiresConfigPermission(action) && !LodestonePermissions.canConfig(player)) {
			player.createCommandSourceStack().sendFailure(LodestoneText.text("error.no_permission.config", "You do not have permission to configure Lodestone Warps."));
			return true;
		}
		if (id.isBlank() && requiresLodestoneId(action)) {
			player.sendSystemMessage(LodestoneText.text("error.invalid_action", "Invalid lodestone action."));
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
				case "page" -> {
					LodestoneSavedData data = LodestoneSavedData.from(player.level());
					data.get(id).ifPresent(location -> LodestoneDialogs.showDestinations(player, location, readField(payload, "query"), payload.getIntOr("page", 0)));
					yield true;
				}
				case "rename" -> {
					LodestoneCommands.rename(player.createCommandSourceStack(), id, readField(payload, "name"));
					yield true;
				}
				case "save_edit" -> {
					LodestoneCommands.saveEdit(player, id, readField(payload, "name"), payload.getStringOr("visibility", ""));
					yield true;
				}
				case "edit_mode" -> {
					LodestoneSavedData data = LodestoneSavedData.from(player.level());
					LodestoneVisibility visibility = LodestoneVisibility.from(payload.getStringOr("visibility", ""), null);
					if (visibility == null) {
						player.createCommandSourceStack().sendFailure(LodestoneText.text("error.invalid_visibility", "Invalid visibility. Use private, discoverable, or global."));
						yield true;
					}
					data.get(id).ifPresentOrElse(
						location -> LodestoneDialogs.showEdit(player, location, readField(payload, "name"), visibility),
						() -> player.sendSystemMessage(LodestoneText.text("error.lodestone_not_found", "I could not find that lodestone."))
					);
					yield true;
				}
				case "remove" -> {
					LodestoneCommands.remove(player.createCommandSourceStack(), id);
					yield true;
				}
				case "visibility" -> {
					LodestoneCommands.setVisibility(player.createCommandSourceStack(), id, payload.getStringOr("visibility", ""));
					yield true;
				}
				case "config_open" -> {
					LodestoneDialogs.showConfig(player, payload.getStringOr("category", LodestoneConfigOptions.ALL), readField(payload, "query"));
					yield true;
				}
				case "config_reload" -> {
					LodestoneCommands.reloadConfig(player.createCommandSourceStack());
					LodestoneDialogs.showConfig(player, payload.getStringOr("category", LodestoneConfigOptions.ALL), readField(payload, "query"));
					yield true;
				}
				case "config_toggle" -> {
					String key = payload.getStringOr("key", "");
					LodestoneConfigOptions.get(key).ifPresent(option -> LodestoneCommands.setConfig(player.createCommandSourceStack(), key, String.valueOf(!Boolean.parseBoolean(option.currentValue()))));
					LodestoneDialogs.showConfig(player, payload.getStringOr("category", LodestoneConfigOptions.ALL), readField(payload, "query"));
					yield true;
				}
				case "config_edit" -> {
					String key = payload.getStringOr("key", "");
					LodestoneConfigOptions.get(key).ifPresent(option -> LodestoneDialogs.showConfigEdit(player, option, payload.getStringOr("category", LodestoneConfigOptions.ALL), readField(payload, "query")));
					yield true;
				}
				case "config_permissions" -> {
					LodestoneDialogs.showConfigPermissions(player, payload.getStringOr("key", "player_permissions"), readField(payload, "query"));
					yield true;
				}
				case "config_save" -> {
					LodestoneCommands.setConfig(player.createCommandSourceStack(), payload.getStringOr("key", ""), readField(payload, "value"));
					LodestoneDialogs.showConfig(player, payload.getStringOr("category", LodestoneConfigOptions.ALL), payload.getStringOr("query", ""));
					yield true;
				}
				case "permission_search" -> {
					LodestoneDialogs.showConfigPermissions(player, payload.getStringOr("key", "player_permissions"), readField(payload, "query"));
					yield true;
				}
				case "permission_add" -> {
					LodestoneConfig.putPermission(payload.getStringOr("key", "player_permissions"), readField(payload, "permission"), false);
					LodestoneConfig.save();
					LodestoneDialogs.showConfigPermissions(player, payload.getStringOr("key", "player_permissions"), readField(payload, "query"));
					yield true;
				}
				case "permission_toggle" -> {
					LodestoneConfig.togglePermission(payload.getStringOr("key", "player_permissions"), payload.getStringOr("permission", ""));
					LodestoneConfig.save();
					LodestoneDialogs.showConfigPermissions(player, payload.getStringOr("key", "player_permissions"), readField(payload, "query"));
					yield true;
				}
				case "permission_remove" -> {
					LodestoneConfig.removePermission(payload.getStringOr("key", "player_permissions"), payload.getStringOr("permission", ""));
					LodestoneConfig.save();
					LodestoneDialogs.showConfigPermissions(player, payload.getStringOr("key", "player_permissions"), readField(payload, "query"));
					yield true;
				}
				default -> false;
			};
		} catch (CommandSyntaxException exception) {
			player.sendSystemMessage(LodestoneText.text("error.action_failed", "Could not run the lodestone action."));
			return true;
		}
	}

	private static boolean requiresLodestoneId(String action) {
		return switch (action) {
			case "config_open", "config_reload", "config_toggle", "config_edit", "config_save", "config_permissions", "permission_search", "permission_add", "permission_toggle", "permission_remove" -> false;
			default -> true;
		};
	}

	private static boolean requiresConfigPermission(String action) {
		return action.startsWith("config_") || action.startsWith("permission_");
	}

	private static String readField(CompoundTag payload, String key) {
		Tag tag = payload.get(key);
		if (tag != null) {
			return tag.asString().orElse("");
		}
		return "";
	}
}
