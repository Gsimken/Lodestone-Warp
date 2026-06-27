package dev.simke.lodestoneteleport;

import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

public final class LodestonePermissions {
	public static final PermissionNode<Boolean> USE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "use");
	public static final PermissionNode<Boolean> RENAME = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "rename");
	private static final String DEBUG_USE = LodestoneTeleportMod.MOD_ID + ".use";
	private static final String DEBUG_RENAME = LodestoneTeleportMod.MOD_ID + ".rename";

	private LodestonePermissions() {
	}

	public static boolean canUse(CommandSourceStack source) {
		return has(source, USE);
	}

	public static boolean canUse(ServerPlayer player) {
		return canUse(player.createCommandSourceStack());
	}

	public static boolean canRename(CommandSourceStack source) {
		return has(source, RENAME);
	}

	public static boolean canRename(ServerPlayer player) {
		return canRename(player.createCommandSourceStack());
	}

	private static boolean has(CommandSourceStack source, PermissionNode<Boolean> permission) {
		Boolean debugOverride = debugOverride(permission);
		if (debugOverride != null) {
			return debugOverride;
		}
		if (!LodestoneConfig.get().requirePermissions) {
			return true;
		}
		return PermissionPredicates.require(permission, PermissionLevel.GAMEMASTERS).test(source);
	}

	private static Boolean debugOverride(PermissionNode<Boolean> permission) {
		if (permission == USE) {
			return readBoolean(DEBUG_USE);
		}
		if (permission == RENAME) {
			return readBoolean(DEBUG_RENAME);
		}
		return null;
	}

	private static Boolean readBoolean(String property) {
		String value = System.getProperty(property);
		if (value == null || value.isBlank()) {
			value = System.getenv(property.toUpperCase(java.util.Locale.ROOT).replace('.', '_'));
		}
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
			case "true", "1", "yes", "on" -> true;
			case "false", "0", "no", "off" -> false;
			default -> null;
		};
	}
}
