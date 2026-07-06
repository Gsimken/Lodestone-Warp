package dev.simke.lodestoneteleport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

public record LodestoneTeleportAvailability(boolean canTeleport, String reason) {
	public static LodestoneTeleportAvailability check(ServerPlayer player, LodestoneSavedData data, LodestoneLocation destination, LodestoneTeleportCost cost) {
		ServerLevel destinationLevel = player.level().getServer().getLevel(destination.dimension());
		if (destinationLevel == null) {
			return disabled(LodestoneText.text("error.dimension_unloaded", "The destination dimension is not loaded.").getString());
		}
		if (!LodestoneConfig.get().allowCrossDimension && !player.level().dimension().equals(destination.dimension())) {
			return disabled(LodestoneText.text("error.cross_dimension_disabled", "Cross-dimension teleport is disabled.").getString());
		}
		if (!destinationLevel.getBlockState(destination.pos()).is(Blocks.LODESTONE)) {
			return disabled(LodestoneText.text("error.destination_removed", "The destination no longer has a lodestone and was removed.").getString());
		}
		if (!LodestoneCommands.isNearRegisteredLodestone(player, data)) {
			return disabled(LodestoneText.text("error.need_near_lodestone", "You must be near a registered lodestone to teleport.").getString());
		}
		long cooldown = LodestonePermissions.canBypassCooldown(player) ? 0L : LodestoneTeleportCooldowns.remainingSeconds(player);
		if (cooldown > 0L) {
			return disabled(LodestoneText.text("error.cooldown", "You must wait %s seconds before teleporting again.", cooldown).getString());
		}
		if (!LodestoneCommands.hasCost(player, cost)) {
			return disabled(LodestoneText.text("error.need_cost", "You need %s.", LodestoneText.cost(cost)).getString());
		}
		return enabled();
	}

	public static LodestoneTeleportAvailability enabled() {
		return new LodestoneTeleportAvailability(true, "");
	}

	public static LodestoneTeleportAvailability disabled(String reason) {
		return new LodestoneTeleportAvailability(false, reason);
	}
}
