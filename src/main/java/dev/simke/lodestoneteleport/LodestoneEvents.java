package dev.simke.lodestoneteleport;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class LodestoneEvents {
	private static final List<PendingPlacement> PENDING_PLACEMENTS = new ArrayList<>();
	private static final List<PendingRemoval> PENDING_REMOVALS = new ArrayList<>();

	private LodestoneEvents() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register(LodestoneEvents::onUseBlock);
		PlayerBlockBreakEvents.BEFORE.register(LodestoneEvents::beforeBlockBreak);
		PlayerBlockBreakEvents.AFTER.register(LodestoneEvents::afterBlockBreak);
		ServerTickEvents.END_SERVER_TICK.register(LodestoneEvents::onEndServerTick);
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		BlockState clickedState = level.getBlockState(hit.getBlockPos());
		if (hand == InteractionHand.MAIN_HAND && clickedState.is(Blocks.LODESTONE) && player.getItemInHand(hand).isEmpty()) {
			if (!LodestonePermissions.canUse(serverPlayer)) {
				serverPlayer.sendSystemMessage(LodestoneText.text("error.no_permission.use", "You do not have permission to use lodestones."));
				return InteractionResult.SUCCESS_SERVER;
			}
			LodestoneSavedData data = LodestoneSavedData.from(level);
			Optional<LodestoneLocation> existing = data.at(level.dimension(), hit.getBlockPos());
			if (existing.isEmpty()) {
				if (!LodestoneConfig.get().autoRegisterUntrackedLodestones && !player.isShiftKeyDown()) {
					serverPlayer.sendSystemMessage(LodestoneText.text("error.lodestone_not_registered", "This lodestone is not registered."));
					return InteractionResult.SUCCESS_SERVER;
				}
				if (!canRegister(serverPlayer, data)) {
					return InteractionResult.SUCCESS_SERVER;
				}
			}
			LodestoneLocation location = existing.orElseGet(() -> data.register(level.dimension(), hit.getBlockPos(), player.getUUID(), player.getName().getString()));
			LodestoneUi.showDestinations(serverPlayer, location);
			return InteractionResult.SUCCESS_SERVER;
		}

		if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).is(Items.LODESTONE)) {
			if (LodestoneConfig.get().registerPlacedLodestonesOnlyWhenSneaking && !player.isShiftKeyDown()) {
				return InteractionResult.PASS;
			}
			BlockPos clicked = hit.getBlockPos().immutable();
			BlockPos adjacent = clicked.relative(hit.getDirection()).immutable();
			PENDING_PLACEMENTS.add(new PendingPlacement(serverPlayer.getUUID(), clicked, adjacent, player.isShiftKeyDown()));
		}
		return InteractionResult.PASS;
	}

	private static boolean beforeBlockBreak(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
		if (level.isClientSide() || !state.is(Blocks.LODESTONE) || !(player instanceof ServerPlayer serverPlayer)) {
			return true;
		}
		if (LodestoneSavedData.from(level).at(level.dimension(), pos).isEmpty()) {
			return true;
		}
		if (LodestonePermissions.canRemove(serverPlayer)) {
			PENDING_REMOVALS.add(new PendingRemoval(level.dimension(), pos.immutable()));
			return true;
		}
		serverPlayer.sendSystemMessage(LodestoneText.text("error.no_permission.remove", "You do not have permission to remove registered lodestones."));
		return false;
	}

	private static void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
		if (!level.isClientSide() && state.is(Blocks.LODESTONE)) {
			LodestoneSavedData.from(level).remove(level.dimension(), pos);
		}
	}

	private static void onEndServerTick(MinecraftServer server) {
		LodestoneTeleportCasts.tick(server);
		processPendingRemovals(server);

		Iterator<PendingPlacement> iterator = PENDING_PLACEMENTS.iterator();
		while (iterator.hasNext()) {
			PendingPlacement pending = iterator.next();
			iterator.remove();

			ServerPlayer player = server.getPlayerList().getPlayer(pending.playerUuid());
			if (player == null) {
				continue;
			}
			ServerLevel level = (ServerLevel) player.level();
			BlockPos placed = findPlacedLodestone(level, pending.clicked(), pending.adjacent());
			if (placed == null) {
				continue;
			}
			LodestoneSavedData data = LodestoneSavedData.from(level);
			if (!canRegister(player, data)) {
				continue;
			}

			LodestoneLocation location = data.register(level.dimension(), placed, player.getUUID(), player.getName().getString());
			player.sendSystemMessage(LodestoneText.text("registered", "Registered lodestone: %s", location.displayName()));
			if (pending.rename() && LodestonePermissions.canRename(player)) {
				LodestoneUi.showRename(player, location);
			} else if (pending.rename()) {
				player.sendSystemMessage(LodestoneText.text("error.no_permission.rename", "You do not have permission to rename lodestones."));
			}
		}
	}

	private static BlockPos findPlacedLodestone(ServerLevel level, BlockPos clicked, BlockPos adjacent) {
		if (level.getBlockState(adjacent).is(Blocks.LODESTONE)) {
			return adjacent;
		}
		if (level.getBlockState(clicked).is(Blocks.LODESTONE)) {
			return clicked;
		}
		return null;
	}

	private static boolean canRegister(ServerPlayer player, LodestoneSavedData data) {
		if (!LodestonePermissions.canCreate(player)) {
			player.sendSystemMessage(LodestoneText.text("error.no_permission.create", "You do not have permission to register lodestones."));
			return false;
		}
		if (LodestonePermissions.canBypassMaxWarps(player)) {
			return true;
		}

		LodestoneConfig config = LodestoneConfig.get();
		if (config.maxLodestonesGlobal > 0 && data.all().size() >= config.maxLodestonesGlobal) {
			player.sendSystemMessage(LodestoneText.text("error.max_lodestones_global", "The server has reached the maximum number of registered lodestones."));
			return false;
		}
		if (config.maxLodestonesPerPlayer > 0) {
			long owned = data.all().stream()
				.filter(location -> location.ownerUuid().equals(player.getUUID()))
				.count();
			if (owned >= config.maxLodestonesPerPlayer) {
				player.sendSystemMessage(LodestoneText.text("error.max_lodestones_player", "You have reached your maximum number of registered lodestones."));
				return false;
			}
		}
		return true;
	}

	private static void processPendingRemovals(MinecraftServer server) {
		Iterator<PendingRemoval> iterator = PENDING_REMOVALS.iterator();
		while (iterator.hasNext()) {
			PendingRemoval pending = iterator.next();
			iterator.remove();

			ServerLevel level = server.getLevel(pending.dimension());
			if (level == null) {
				continue;
			}
			if (!level.getBlockState(pending.pos()).is(Blocks.LODESTONE)) {
				LodestoneSavedData.from(level).remove(pending.dimension(), pending.pos());
			}
		}
	}

	private record PendingPlacement(UUID playerUuid, BlockPos clicked, BlockPos adjacent, boolean rename) {
	}

	private record PendingRemoval(net.minecraft.resources.ResourceKey<Level> dimension, BlockPos pos) {
	}
}
