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
		if (LodestonePermissions.canUseAllMode(player.createCommandSourceStack())) {
			return true;
		}
		if (!isDiscoverMode() && !LodestonePermissions.canUseDiscoverMode(player.createCommandSourceStack())) {
			return true;
		}
		return data.isDiscovered(player.getUUID(), location.id());
	}

	public static boolean canSeeAll(ServerPlayer player) {
		return LodestonePermissions.canUseAllMode(player.createCommandSourceStack());
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
