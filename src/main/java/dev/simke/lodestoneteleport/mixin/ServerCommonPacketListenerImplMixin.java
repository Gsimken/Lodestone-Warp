package dev.simke.lodestoneteleport.mixin;

import dev.simke.lodestoneteleport.LodestoneCustomActions;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	@Inject(method = "handleCustomClickAction", at = @At("HEAD"), cancellable = true)
	private void lodestoneTeleport$handleCustomClickAction(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
		if ((Object) this instanceof ServerGamePacketListenerImpl listener && LodestoneCustomActions.handle(listener.player, packet)) {
			ci.cancel();
		}
	}
}
