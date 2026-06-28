package dev.simke.lodestoneteleport;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class LodestoneTeleportCasts {
	private static final Map<UUID, PendingCast> CASTS = new HashMap<>();

	private LodestoneTeleportCasts() {
	}

	public static boolean isCasting(ServerPlayer player) {
		return CASTS.containsKey(player.getUUID());
	}

	public static void start(ServerPlayer player, String destination) {
		int ticks = LodestoneConfig.get().teleportCastSeconds * 20;
		if (ticks <= 0) {
			LodestoneCommands.completeCast(player, destination);
			return;
		}
		CASTS.put(player.getUUID(), new PendingCast(destination, player.level().dimension(), player.position(), ticks, ticks));
		player.sendSystemMessage(LodestoneText.text("teleport.cast_start", "Casting teleport... stand still for %s seconds.", LodestoneConfig.get().teleportCastSeconds));
		LodestoneTeleportEffects.cast(player);
	}

	public static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, PendingCast>> iterator = CASTS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, PendingCast> entry = iterator.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			if (player == null || player.isRemoved() || player.isDeadOrDying()) {
				iterator.remove();
				continue;
			}

			PendingCast cast = entry.getValue();
			if (moved(player, cast)) {
				iterator.remove();
				player.sendSystemMessage(LodestoneText.text("teleport.cast_cancelled", "Teleport cancelled: you moved."));
				LodestoneTeleportEffects.cancel(player);
				continue;
			}

			int remaining = cast.remainingTicks() - 1;
			if (remaining % 10 == 0) {
				LodestoneTeleportEffects.cast(player);
			}
			if (remaining > 0) {
				entry.setValue(cast.withRemainingTicks(remaining));
				continue;
			}

			iterator.remove();
			LodestoneCommands.completeCast(player, cast.destination());
		}
	}

	private static boolean moved(ServerPlayer player, PendingCast cast) {
		if (!player.level().dimension().equals(cast.dimension())) {
			return true;
		}
		double tolerance = LodestoneConfig.get().teleportCastMoveTolerance;
		return player.position().distanceToSqr(cast.startPosition()) > tolerance * tolerance;
	}

	private record PendingCast(String destination, ResourceKey<Level> dimension, Vec3 startPosition, int totalTicks, int remainingTicks) {
		PendingCast withRemainingTicks(int remainingTicks) {
			return new PendingCast(destination, dimension, startPosition, totalTicks, remainingTicks);
		}
	}
}
