package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.network.LodestoneOpenScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class LodestoneTeleportClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		LodestoneClientPreferences.load();
		ClientPlayNetworking.registerGlobalReceiver(LodestoneOpenScreenPayload.TYPE, (payload, context) -> {
			Minecraft client = context.client();
			client.execute(() -> {
				if ("edit".equals(payload.data().getStringOr("screen", ""))) {
					client.setScreenAndShow(new LodestoneRenameScreen(null, payload.data()));
					return;
				}
				client.setScreenAndShow(new LodestoneWarpScreen(payload.data()));
			});
		});
	}
}
