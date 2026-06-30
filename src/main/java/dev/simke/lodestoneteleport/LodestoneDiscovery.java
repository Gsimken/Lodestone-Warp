package dev.simke.lodestoneteleport;

import net.minecraft.server.level.ServerPlayer;

public final class LodestoneDiscovery {
	private LodestoneDiscovery() {
	}

	public static boolean isDiscoverMode() {
		return "discover".equals(LodestoneConfig.get().networkMode);
	}

	public static boolean canSee(ServerPlayer player, LodestoneSavedData data, LodestoneLocation location) {
		if (!LodestonePermissions.canUse(player)) {
			return false;
		}
		if (!isDiscoverMode()) {
			return true;
		}
		if (LodestonePermissions.canUseAllMode(player.createCommandSourceStack())) {
			return true;
		}
		if (!LodestonePermissions.canUseDiscoverMode(player.createCommandSourceStack())) {
			return false;
		}
		return location.global() || data.isDiscovered(player.getUUID(), location.id());
	}

	public static boolean discover(ServerPlayer player, LodestoneSavedData data, LodestoneLocation location) {
		if (!LodestonePermissions.canUse(player)) {
			return false;
		}
		return data.discover(player.getUUID(), location.id());
	}
}
