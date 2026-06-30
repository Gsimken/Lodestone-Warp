package dev.simke.lodestoneteleport;

import net.minecraft.server.level.ServerPlayer;

public final class LodestoneUi {
	private LodestoneUi() {
	}

	public static void showDestinations(ServerPlayer player, LodestoneLocation location) {
		if (LodestoneNetworking.canUseClientScreen(player)) {
			LodestoneNetworking.openClientScreen(player, location);
			return;
		}
		LodestoneDialogs.showDestinations(player, location);
	}

	public static void showRename(ServerPlayer player, LodestoneLocation location) {
		if (LodestoneNetworking.canUseClientScreen(player)) {
			LodestoneNetworking.openClientRenameScreen(player, location);
			return;
		}
		LodestoneDialogs.showRename(player, location);
	}
}
