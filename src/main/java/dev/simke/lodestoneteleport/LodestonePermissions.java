package dev.simke.lodestoneteleport;

import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.List;

public final class LodestonePermissions {
	public static final PermissionNode<Boolean> USE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "use");
	public static final PermissionNode<Boolean> RENAME = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "rename");
	public static final PermissionNode<Boolean> CREATE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "create");
	public static final PermissionNode<Boolean> REMOVE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "remove");
	public static final PermissionNode<Boolean> ADMIN = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "admin");
	public static final PermissionNode<Boolean> BYPASS_COST = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "bypass_cost");
	public static final PermissionNode<Boolean> BYPASS_CAST = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "bypass_cast");
	public static final PermissionNode<Boolean> BYPASS_COOLDOWN = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "bypass_cooldown");
	public static final PermissionNode<Boolean> BYPASS_MAX_WARPS = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "bypass_max_warps");
	public static final PermissionNode<Boolean> MODE_ALL = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "mode.all");
	public static final PermissionNode<Boolean> MODE_DISCOVER = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "mode.discover");
	public static final PermissionNode<Boolean> CONFIG = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "config");
	public static final PermissionNode<Boolean> GLOBAL = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "global");

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

	public static boolean canCreate(CommandSourceStack source) {
		return has(source, CREATE);
	}

	public static boolean canCreate(ServerPlayer player) {
		return canCreate(player.createCommandSourceStack());
	}

	public static boolean canRemove(CommandSourceStack source) {
		return has(source, REMOVE);
	}

	public static boolean canRemove(ServerPlayer player) {
		return canRemove(player.createCommandSourceStack());
	}

	public static boolean canAdmin(CommandSourceStack source) {
		return has(source, ADMIN);
	}

	public static boolean canBypassCost(CommandSourceStack source) {
		return has(source, BYPASS_COST);
	}

	public static boolean canBypassCost(ServerPlayer player) {
		return canBypassCost(player.createCommandSourceStack());
	}

	public static boolean canBypassCast(CommandSourceStack source) {
		return has(source, BYPASS_CAST);
	}

	public static boolean canBypassCast(ServerPlayer player) {
		return canBypassCast(player.createCommandSourceStack());
	}

	public static boolean canBypassCooldown(CommandSourceStack source) {
		return has(source, BYPASS_COOLDOWN);
	}

	public static boolean canBypassCooldown(ServerPlayer player) {
		return canBypassCooldown(player.createCommandSourceStack());
	}

	public static boolean canBypassMaxWarps(CommandSourceStack source) {
		return has(source, BYPASS_MAX_WARPS);
	}

	public static boolean canBypassMaxWarps(ServerPlayer player) {
		return canBypassMaxWarps(player.createCommandSourceStack());
	}

	public static boolean canUseAllMode(CommandSourceStack source) {
		return has(source, MODE_ALL);
	}

	public static boolean canUseDiscoverMode(CommandSourceStack source) {
		return has(source, MODE_DISCOVER);
	}

	public static boolean canConfig(CommandSourceStack source) {
		return has(source, CONFIG);
	}

	public static boolean canConfig(ServerPlayer player) {
		return canConfig(player.createCommandSourceStack());
	}

	public static boolean canSetGlobal(CommandSourceStack source) {
		return has(source, GLOBAL);
	}

	private static boolean has(CommandSourceStack source, PermissionNode<Boolean> permission) {
		Boolean debugOverride = debugOverride(permission);
		if (debugOverride != null) {
			return debugOverride;
		}
		return PermissionPredicates.require(permission, hasConfiguredPermission(source, permission)).test(source);
	}

	private static boolean hasConfiguredPermission(CommandSourceStack source, PermissionNode<Boolean> permission) {
		String node = LodestoneTeleportMod.MOD_ID + "." + permissionName(permission);
		LodestoneConfig config = LodestoneConfig.get();
		if (containsPermission(config.playerPermissions, node)) {
			return true;
		}
		if (!source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			return false;
		}
		return containsPermission(config.adminPermissions, node);
	}

	private static boolean containsPermission(List<String> permissions, String node) {
		if (permissions == null) {
			return false;
		}
		String namespaceWildcard = LodestoneTeleportMod.MOD_ID + ".*";
		for (String permission : permissions) {
			if (permission.equals("*") || permission.equals(namespaceWildcard) || permission.equals(node)) {
				return true;
			}
		}
		return false;
	}

	private static Boolean debugOverride(PermissionNode<Boolean> permission) {
		return readBoolean(LodestoneTeleportMod.MOD_ID + "." + permissionName(permission));
	}

	private static String permissionName(PermissionNode<Boolean> permission) {
		if (permission == USE) return "use";
		if (permission == RENAME) return "rename";
		if (permission == CREATE) return "create";
		if (permission == REMOVE) return "remove";
		if (permission == ADMIN) return "admin";
		if (permission == BYPASS_COST) return "bypass_cost";
		if (permission == BYPASS_CAST) return "bypass_cast";
		if (permission == BYPASS_COOLDOWN) return "bypass_cooldown";
		if (permission == BYPASS_MAX_WARPS) return "bypass_max_warps";
		if (permission == MODE_ALL) return "mode.all";
		if (permission == MODE_DISCOVER) return "mode.discover";
		if (permission == CONFIG) return "config";
		if (permission == GLOBAL) return "global";
		return "";
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
