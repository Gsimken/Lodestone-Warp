package dev.simke.lodestoneteleport;

import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class LodestonePermissions {
	private static final String LEGACY_PERMISSION_PREFIX = LodestoneTeleportMod.MOD_ID + ".";
	private static final String SHORT_PERMISSION_PREFIX = "lodestone.";
	private static final int MAX_SCANNED_LIMIT_PERMISSION = 1000;

	public static final PermissionNode<Boolean> USE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "use");
	public static final PermissionNode<Boolean> RENAME = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "rename");
	public static final PermissionNode<Boolean> CREATE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "create");
	public static final PermissionNode<Boolean> CREATE_PRIVATE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "create.private");
	public static final PermissionNode<Boolean> CREATE_DISCOVERABLE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "create.discoverable");
	public static final PermissionNode<Boolean> CREATE_GLOBAL = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "create.global");
	public static final PermissionNode<Boolean> REMOVE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "remove");
	public static final PermissionNode<Boolean> OWN_RENAME = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "own.rename");
	public static final PermissionNode<Boolean> OWN_REMOVE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "own.remove");
	public static final PermissionNode<Boolean> OWN_DESTROY = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "own.destroy");
	public static final PermissionNode<Boolean> OWN_VISIBILITY_PRIVATE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "own.visibility.private");
	public static final PermissionNode<Boolean> OWN_VISIBILITY_DISCOVERABLE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "own.visibility.discoverable");
	public static final PermissionNode<Boolean> OWN_VISIBILITY_GLOBAL = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "own.visibility.global");
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

	public static boolean canCreateVisibility(ServerPlayer player, LodestoneVisibility visibility) {
		CommandSourceStack source = player.createCommandSourceStack();
		if (!canCreate(source)) {
			return false;
		}
		if (visibility == LodestoneVisibility.PRIVATE && !LodestoneConfig.get().allowPersonalLodestones) {
			return false;
		}
		return switch (visibility) {
			case PRIVATE -> has(source, CREATE_PRIVATE);
			case DISCOVERABLE -> has(source, CREATE_DISCOVERABLE);
			case GLOBAL -> has(source, CREATE_GLOBAL) || canSetGlobal(source);
		};
	}

	public static boolean canRemove(CommandSourceStack source) {
		return has(source, REMOVE);
	}

	public static boolean canRemove(ServerPlayer player) {
		return canRemove(player.createCommandSourceStack());
	}

	public static boolean canRename(ServerPlayer player, LodestoneLocation location) {
		return canRename(player) || (location.ownedBy(player.getUUID()) && has(player.createCommandSourceStack(), OWN_RENAME));
	}

	public static boolean canRemove(ServerPlayer player, LodestoneLocation location) {
		return canRemove(player) || (location.ownedBy(player.getUUID()) && has(player.createCommandSourceStack(), OWN_REMOVE));
	}

	public static boolean canDestroy(ServerPlayer player, LodestoneLocation location) {
		return canRemove(player) || (location.ownedBy(player.getUUID()) && has(player.createCommandSourceStack(), OWN_DESTROY));
	}

	public static boolean canSetVisibility(ServerPlayer player, LodestoneLocation location, LodestoneVisibility visibility) {
		CommandSourceStack source = player.createCommandSourceStack();
		if (visibility == LodestoneVisibility.GLOBAL && canSetGlobal(source)) {
			return true;
		}
		if (!location.ownedBy(player.getUUID())) {
			return false;
		}
		if (visibility == LodestoneVisibility.PRIVATE && !LodestoneConfig.get().allowPersonalLodestones) {
			return false;
		}
		return switch (visibility) {
			case PRIVATE -> has(source, OWN_VISIBILITY_PRIVATE);
			case DISCOVERABLE -> has(source, OWN_VISIBILITY_DISCOVERABLE);
			case GLOBAL -> has(source, OWN_VISIBILITY_GLOBAL);
		};
	}

	public static boolean canEdit(ServerPlayer player, LodestoneLocation location) {
		return canRename(player, location)
			|| canRemove(player, location)
			|| canSetVisibility(player, location, LodestoneVisibility.PRIVATE)
			|| canSetVisibility(player, location, LodestoneVisibility.DISCOVERABLE)
			|| canSetVisibility(player, location, LodestoneVisibility.GLOBAL);
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

	public static OptionalInt ownedLodestoneLimit(ServerPlayer player) {
		if (canBypassMaxWarps(player)) {
			return OptionalInt.empty();
		}
		CommandSourceStack source = player.createCommandSourceStack();
		int configured = highestConfiguredLimit(source);
		int debug = highestDebugLimit();
		int external = highestExternalLimit(source);
		int limit = Math.max(configured, Math.max(debug, external));
		return limit >= 0 ? OptionalInt.of(limit) : OptionalInt.empty();
	}

	private static boolean has(CommandSourceStack source, PermissionNode<Boolean> permission) {
		Boolean debugOverride = debugOverride(permission);
		if (debugOverride != null) {
			return debugOverride;
		}
		if (debugWildcardOverride() || hasConfiguredWildcard(source) || hasExternalWildcard(source)) {
			return true;
		}
		return PermissionPredicates.require(permission, hasConfiguredPermission(source, permission)).test(source);
	}

	private static boolean hasConfiguredPermission(CommandSourceStack source, PermissionNode<Boolean> permission) {
		String node = LEGACY_PERMISSION_PREFIX + permissionName(permission);
		String shortNode = SHORT_PERMISSION_PREFIX + permissionName(permission);
		LodestoneConfig config = LodestoneConfig.get();
		if (containsPermission(config.playerPermissions, node) || containsPermission(config.playerPermissions, shortNode)) {
			return true;
		}
		if (!source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			return false;
		}
		return containsPermission(config.adminPermissions, node) || containsPermission(config.adminPermissions, shortNode);
	}

	private static boolean containsPermission(Map<String, Boolean> permissions, String node) {
		if (permissions == null) {
			return false;
		}
		String namespaceWildcard = LEGACY_PERMISSION_PREFIX + "*";
		String shortWildcard = SHORT_PERMISSION_PREFIX + "*";
		for (var entry : permissions.entrySet()) {
			String permission = entry.getKey();
			if (Boolean.TRUE.equals(entry.getValue()) && (permission.equals("*") || permission.equals(namespaceWildcard) || permission.equals(shortWildcard) || permission.equals(node))) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasConfiguredWildcard(CommandSourceStack source) {
		LodestoneConfig config = LodestoneConfig.get();
		if (containsPermission(config.playerPermissions, LEGACY_PERMISSION_PREFIX + "*") || containsPermission(config.playerPermissions, SHORT_PERMISSION_PREFIX + "*")) {
			return true;
		}
		return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
			&& (containsPermission(config.adminPermissions, LEGACY_PERMISSION_PREFIX + "*") || containsPermission(config.adminPermissions, SHORT_PERMISSION_PREFIX + "*"));
	}

	private static boolean hasExternalWildcard(CommandSourceStack source) {
		return checkExternal(source, LodestoneTeleportMod.MOD_ID + ":*")
			|| checkExternal(source, "lodestone:*");
	}

	private static int highestConfiguredLimit(CommandSourceStack source) {
		LodestoneConfig config = LodestoneConfig.get();
		int limit = highestLimit(config.playerPermissions);
		if (source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			limit = Math.max(limit, highestLimit(config.adminPermissions));
		}
		return limit;
	}

	private static int highestLimit(Map<String, Boolean> permissions) {
		int limit = -1;
		if (permissions == null) {
			return limit;
		}
		for (var entry : permissions.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				limit = Math.max(limit, parseLimitPermission(entry.getKey()));
			}
		}
		return limit;
	}

	private static int highestDebugLimit() {
		int limit = -1;
		for (String property : System.getProperties().stringPropertyNames()) {
			if (readBoolean(property) == Boolean.TRUE) {
				limit = Math.max(limit, parseLimitPermission(property));
			}
		}
		for (var entry : System.getenv().entrySet()) {
			String key = entry.getKey().toLowerCase(java.util.Locale.ROOT).replace('_', '.');
			String value = entry.getValue();
			if (List.of("true", "1", "yes", "on").contains(value.trim().toLowerCase(java.util.Locale.ROOT))) {
				limit = Math.max(limit, parseLimitPermission(key));
			}
		}
		return limit;
	}

	private static int highestExternalLimit(CommandSourceStack source) {
		int limit = -1;
		for (int candidate = 0; candidate <= MAX_SCANNED_LIMIT_PERMISSION; candidate++) {
			if (externalLimit(source, LodestoneTeleportMod.MOD_ID, candidate) || externalLimit(source, "lodestone", candidate)) {
				limit = candidate;
			}
		}
		return limit;
	}

	private static boolean externalLimit(CommandSourceStack source, String namespace, int limit) {
		return checkExternal(source, namespace + ":limit." + limit);
	}

	private static boolean checkExternal(CommandSourceStack source, String permission) {
		Identifier id = Identifier.tryParse(permission);
		return id != null && PermissionPredicates.require(id, false).test(source);
	}

	private static int parseLimitPermission(String permission) {
		if (permission == null) {
			return -1;
		}
		String clean = permission.trim().toLowerCase(java.util.Locale.ROOT);
		for (String prefix : List.of(LEGACY_PERMISSION_PREFIX + "limit.", SHORT_PERMISSION_PREFIX + "limit.")) {
			if (!clean.startsWith(prefix)) {
				continue;
			}
			try {
				return Math.max(0, Integer.parseInt(clean.substring(prefix.length())));
			} catch (NumberFormatException exception) {
				return -1;
			}
		}
		return -1;
	}

	private static Boolean debugOverride(PermissionNode<Boolean> permission) {
		return readBoolean(LodestoneTeleportMod.MOD_ID + "." + permissionName(permission));
	}

	private static boolean debugWildcardOverride() {
		return readBoolean(LEGACY_PERMISSION_PREFIX + "*") == Boolean.TRUE || readBoolean(SHORT_PERMISSION_PREFIX + "*") == Boolean.TRUE;
	}

	private static String permissionName(PermissionNode<Boolean> permission) {
		if (permission == USE) return "use";
		if (permission == RENAME) return "rename";
		if (permission == CREATE) return "create";
		if (permission == CREATE_PRIVATE) return "create.private";
		if (permission == CREATE_DISCOVERABLE) return "create.discoverable";
		if (permission == CREATE_GLOBAL) return "create.global";
		if (permission == REMOVE) return "remove";
		if (permission == OWN_RENAME) return "own.rename";
		if (permission == OWN_REMOVE) return "own.remove";
		if (permission == OWN_DESTROY) return "own.destroy";
		if (permission == OWN_VISIBILITY_PRIVATE) return "own.visibility.private";
		if (permission == OWN_VISIBILITY_DISCOVERABLE) return "own.visibility.discoverable";
		if (permission == OWN_VISIBILITY_GLOBAL) return "own.visibility.global";
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
