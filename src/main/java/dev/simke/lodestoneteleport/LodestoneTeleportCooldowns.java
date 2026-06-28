package dev.simke.lodestoneteleport;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LodestoneTeleportCooldowns {
	private static final Map<UUID, Long> LAST_TELEPORTS = new HashMap<>();

	private LodestoneTeleportCooldowns() {
	}

	public static long remainingSeconds(ServerPlayer player) {
		int cooldown = LodestoneConfig.get().teleportCooldownSeconds;
		if (cooldown <= 0) {
			return 0L;
		}
		long now = System.currentTimeMillis();
		long last = LAST_TELEPORTS.getOrDefault(player.getUUID(), 0L);
		long elapsed = Math.max(0L, now - last);
		long remainingMillis = cooldown * 1000L - elapsed;
		if (remainingMillis <= 0L) {
			return 0L;
		}
		return (long) Math.ceil(remainingMillis / 1000.0D);
	}

	public static void mark(ServerPlayer player) {
		if (LodestoneConfig.get().teleportCooldownSeconds <= 0) {
			return;
		}
		LAST_TELEPORTS.put(player.getUUID(), System.currentTimeMillis());
	}
}
