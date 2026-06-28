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
import java.util.UUID;

public final class LodestoneEvents {
	private static final List<PendingPlacement> PENDING_PLACEMENTS = new ArrayList<>();

	private LodestoneEvents() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register(LodestoneEvents::onUseBlock);
		PlayerBlockBreakEvents.AFTER.register(LodestoneEvents::afterBlockBreak);
		ServerTickEvents.END_SERVER_TICK.register(LodestoneEvents::onEndServerTick);
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		BlockState clickedState = level.getBlockState(hit.getBlockPos());
		if (clickedState.is(Blocks.LODESTONE) && player.getItemInHand(hand).isEmpty()) {
			if (!LodestonePermissions.canUse(serverPlayer)) {
				serverPlayer.sendSystemMessage(LodestoneText.text("error.no_permission.use", "You do not have permission to use lodestones."));
				return InteractionResult.SUCCESS_SERVER;
			}
			LodestoneSavedData data = LodestoneSavedData.from(level);
			LodestoneLocation location = data.at(level.dimension(), hit.getBlockPos())
				.orElseGet(() -> data.register(level.dimension(), hit.getBlockPos(), player.getUUID(), player.getName().getString()));
			LodestoneUi.showDestinations(serverPlayer, location);
			return InteractionResult.SUCCESS_SERVER;
		}

		if (hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).is(Items.LODESTONE)) {
			BlockPos clicked = hit.getBlockPos().immutable();
			BlockPos adjacent = clicked.relative(hit.getDirection()).immutable();
			PENDING_PLACEMENTS.add(new PendingPlacement(serverPlayer.getUUID(), clicked, adjacent, player.isShiftKeyDown()));
		}
		return InteractionResult.PASS;
	}

	private static void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
		if (!level.isClientSide() && state.is(Blocks.LODESTONE)) {
			LodestoneSavedData.from(level).remove(level.dimension(), pos);
		}
	}

	private static void onEndServerTick(MinecraftServer server) {
		LodestoneTeleportCasts.tick(server);

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

			LodestoneLocation location = LodestoneSavedData.from(level).register(level.dimension(), placed, player.getUUID(), player.getName().getString());
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

	private record PendingPlacement(UUID playerUuid, BlockPos clicked, BlockPos adjacent, boolean rename) {
	}
}
