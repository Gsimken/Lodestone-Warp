package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

public final class LodestoneRenameScreen extends Screen {
	private static final int PANEL_WIDTH = 320;

	private final Screen parent;
	private final String id;
	private final String currentName;
	private final String returnId;
	private EditBox nameBox;

	public LodestoneRenameScreen(Screen parent, String id, String currentName, String returnId) {
		super(LodestoneText.text("rename.title", "Name lodestone"));
		this.parent = parent;
		this.id = id;
		this.currentName = currentName;
		this.returnId = returnId;
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = Math.max(40, (this.height - 120) / 2);

		this.nameBox = new EditBox(this.font, left, top + 36, PANEL_WIDTH, 20, LodestoneText.text("rename.title", "Name lodestone"));
		this.nameBox.setMaxLength(48);
		this.nameBox.setValue(this.currentName);
		addRenderableWidget(this.nameBox);
		setInitialFocus(this.nameBox);

		addRenderableWidget(Button.builder(LodestoneText.text("button.rename", "Rename %s", ""), button -> {
			LodestoneWarpScreen.sendAction("rename", this.id, this.nameBox.getValue(), this.returnId);
			this.minecraft.setScreenAndShow(null);
		}).bounds(left, top + 66, 155, 20).build());

		addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("gui.cancel"), button -> {
			this.minecraft.setScreenAndShow(this.parent);
		}).bounds(left + 165, top + 66, 155, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = Math.max(40, (this.height - 120) / 2);
		graphics.fill(left - 10, top - 18, left + PANEL_WIDTH + 10, top + 100, 0xCC101010);
		graphics.outline(left - 10, top - 18, PANEL_WIDTH + 20, 118, 0xFF595959);
		graphics.centeredText(this.font, this.title, this.width / 2, top - 8, 0xFFFFFFFF);
		graphics.text(this.font, LodestoneText.text("rename.body", "Choose a name for this lodestone."), left, top + 14, 0xFFA8A8A8);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}
}
