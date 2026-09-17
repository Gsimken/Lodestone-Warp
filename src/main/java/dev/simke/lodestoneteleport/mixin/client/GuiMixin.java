package dev.simke.lodestoneteleport.mixin.client;

import dev.simke.lodestoneteleport.client.LodestoneConfigScreen;
import dev.simke.lodestoneteleport.client.LodestoneRenameScreen;
import dev.simke.lodestoneteleport.client.LodestoneWarpScreen;
import dev.simke.lodestoneteleport.client.LodestoneWarpSettingsScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	private Screen screen;

	@Redirect(
		method = "extractRenderState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
		)
	)
	private void lodestoneTeleport$extractScreenAboveDebug(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, DeltaTracker deltaTracker, boolean renderHud, boolean renderScreens) {
		if (isLodestoneScreen(screen)) {
			((Gui) (Object) this).hud.extractDebugOverlay(graphics);
		}
		screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
	}

	@Redirect(
		method = "extractRenderState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Hud;extractDebugOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"
		)
	)
	private void lodestoneTeleport$skipLateDebugForLodestoneScreen(Hud hud, GuiGraphicsExtractor graphics) {
		if (!isLodestoneScreen(this.screen)) {
			hud.extractDebugOverlay(graphics);
		}
	}

	private static boolean isLodestoneScreen(Screen screen) {
		return screen instanceof LodestoneWarpScreen
			|| screen instanceof LodestoneWarpSettingsScreen
			|| screen instanceof LodestoneRenameScreen
			|| screen instanceof LodestoneConfigScreen;
	}
}
