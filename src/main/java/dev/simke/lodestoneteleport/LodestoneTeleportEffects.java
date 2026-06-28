package dev.simke.lodestoneteleport;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class LodestoneTeleportEffects {
	private LodestoneTeleportEffects() {
	}

	public static void before(ServerPlayer player) {
		play(player, (ServerLevel) player.level(), player.position().add(0.0D, 0.6D, 0.0D), true);
	}

	public static void after(ServerPlayer player) {
		play(player, (ServerLevel) player.level(), player.position().add(0.0D, 0.6D, 0.0D), false);
	}

	public static void cast(ServerPlayer player) {
		if (!LodestoneConfig.get().teleportEffects) {
			return;
		}
		ServerLevel level = (ServerLevel) player.level();
		Vec3 pos = player.position().add(0.0D, 0.15D, 0.0D);
		level.sendParticles(ParticleTypes.ENCHANT, pos.x(), pos.y() + 0.35D, pos.z(), 12, 0.35D, 0.25D, 0.35D, 0.25D);
		level.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y() + 0.55D, pos.z(), 8, 0.25D, 0.45D, 0.25D, 0.08D);
		level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.PLAYERS, 0.18F, 1.6F);
	}

	public static void cancel(ServerPlayer player) {
		if (!LodestoneConfig.get().teleportEffects) {
			return;
		}
		ServerLevel level = (ServerLevel) player.level();
		Vec3 pos = player.position().add(0.0D, 0.45D, 0.0D);
		level.sendParticles(ParticleTypes.SMOKE, pos.x(), pos.y(), pos.z(), 18, 0.35D, 0.35D, 0.35D, 0.04D);
		level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.45F, 1.2F);
	}

	private static void play(ServerPlayer player, ServerLevel level, Vec3 pos, boolean departure) {
		LodestoneConfig config = LodestoneConfig.get();
		if (!config.teleportEffects) {
			return;
		}
		String effect = LodestoneNetworking.canUseClientScreen(player) ? config.modTeleportEffect : config.vanillaTeleportEffect;
		if (effect.equals("none") || effect.equals("off")) {
			return;
		}
		if (effect.equals("lodestone")) {
			lodestone(level, pos, departure);
			return;
		}
		end(level, pos, departure);
	}

	private static void end(ServerLevel level, Vec3 pos, boolean departure) {
		level.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), departure ? 56 : 72, 0.45D, 0.75D, 0.45D, departure ? 0.55D : 0.25D);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x(), pos.y(), pos.z(), departure ? 18 : 24, 0.30D, 0.45D, 0.30D, 0.04D);
		level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, departure ? 0.85F : 1.1F);
	}

	private static void lodestone(ServerLevel level, Vec3 pos, boolean departure) {
		level.sendParticles(ParticleTypes.PORTAL, pos.x(), pos.y(), pos.z(), departure ? 36 : 48, 0.36D, 0.65D, 0.36D, 0.18D);
		level.sendParticles(ParticleTypes.END_ROD, pos.x(), pos.y() + 0.15D, pos.z(), departure ? 18 : 26, 0.28D, 0.55D, 0.28D, 0.035D);
		level.sendParticles(ParticleTypes.ENCHANT, pos.x(), pos.y() + 0.25D, pos.z(), departure ? 28 : 36, 0.55D, 0.35D, 0.55D, 0.45D);
		level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.65F, departure ? 0.9F : 1.25F);
		level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.45F, departure ? 1.25F : 1.55F);
	}
}
