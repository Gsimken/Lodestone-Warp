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
		if (location.ownedBy(player.getUUID())) {
			return true;
		}
		if (canSeeAll(player)) {
			return true;
		}
		if (location.privateWarp()) {
			return false;
		}
		if (location.global()) {
			return true;
		}
		if (!isDiscoverMode()) {
			return true;
		}
		if (!LodestonePermissions.canUseDiscoverMode(player.createCommandSourceStack())) {
			return false;
		}
		return data.isDiscovered(player.getUUID(), location.id());
	}

	public static boolean canSeeAll(ServerPlayer player) {
		return isDiscoverMode() && LodestonePermissions.canUseAllMode(player.createCommandSourceStack());
	}

	public static boolean discover(ServerPlayer player, LodestoneSavedData data, LodestoneLocation location) {
		if (!LodestonePermissions.canUse(player)) {
			return false;
		}
		if (location.privateWarp() && !location.ownedBy(player.getUUID())) {
			return false;
		}
		return data.discover(player.getUUID(), location.id());
	}
}
