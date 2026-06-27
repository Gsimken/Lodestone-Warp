package dev.simke.lodestoneteleport;

import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

public final class LodestonePermissions {
	public static final PermissionNode<Boolean> USE = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "use");
	public static final PermissionNode<Boolean> RENAME = PermissionNode.of(LodestoneTeleportMod.MOD_ID, "rename");

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
		if (!LodestoneConfig.get().requirePermissions) {
			return true;
		}
		return PermissionPredicates.require(permission, PermissionLevel.GAMEMASTERS).test(source);
	}
}
