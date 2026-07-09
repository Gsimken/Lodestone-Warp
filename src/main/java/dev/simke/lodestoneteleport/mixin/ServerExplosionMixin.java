package dev.simke.lodestoneteleport.mixin;

import dev.simke.lodestoneteleport.LodestoneSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@ModifyVariable(method = "interactWithBlocks", at = @At("HEAD"), argsOnly = true)
	private List<BlockPos> lodestone_teleport$protectRegisteredLodestones(List<BlockPos> positions) {
		LodestoneSavedData data = LodestoneSavedData.from(this.level);
		List<BlockPos> filtered = new ArrayList<>(positions.size());
		for (BlockPos pos : positions) {
			if (data.at(this.level.dimension(), pos).isEmpty()) {
				filtered.add(pos);
			}
		}
		return filtered;
	}
}
