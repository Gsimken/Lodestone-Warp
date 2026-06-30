package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public final class LodestoneRenameScreen extends Screen {
	private static final int PANEL_WIDTH = 320;

	private final Screen parent;
	private final String id;
	private final String currentName;
	private final String visibility;
	private final String returnId;
	private final boolean canRename;
	private final boolean canRemove;
	private final boolean canPrivate;
	private final boolean canDiscoverable;
	private final boolean canGlobal;
	private EditBox nameBox;

	public LodestoneRenameScreen(Screen parent, String id, String currentName, String returnId) {
		this(parent, id, currentName, "discoverable", returnId, true, false, false, false, false);
	}

	public LodestoneRenameScreen(Screen parent, CompoundTag data) {
		this(
			parent,
			data.getStringOr("id", ""),
			data.getStringOr("name", ""),
			data.getStringOr("visibility", "discoverable"),
			data.getStringOr("returnId", ""),
			data.getBooleanOr("canRename", false),
			data.getBooleanOr("canRemove", false),
			data.getBooleanOr("canPrivate", false),
			data.getBooleanOr("canDiscoverable", false),
			data.getBooleanOr("canGlobal", false)
		);
	}

	private LodestoneRenameScreen(Screen parent, String id, String currentName, String visibility, String returnId, boolean canRename, boolean canRemove, boolean canPrivate, boolean canDiscoverable, boolean canGlobal) {
		super(LodestoneText.text("edit.title", "Edit lodestone"));
		this.parent = parent;
		this.id = id;
		this.currentName = currentName;
		this.visibility = visibility;
		this.returnId = returnId;
		this.canRename = canRename;
		this.canRemove = canRemove;
		this.canPrivate = canPrivate;
		this.canDiscoverable = canDiscoverable;
		this.canGlobal = canGlobal;
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = Math.max(36, (this.height - 190) / 2);

		this.nameBox = new EditBox(this.font, left, top + 42, PANEL_WIDTH, 20, LodestoneText.text("rename.title", "Name lodestone"));
		this.nameBox.setMaxLength(48);
		this.nameBox.setValue(this.currentName);
		addRenderableWidget(this.nameBox);
		setInitialFocus(this.nameBox);

		int y = top + 72;
		if (this.canRename) {
			addRenderableWidget(Button.builder(LodestoneText.text("button.save", "Save"), button -> {
				LodestoneWarpScreen.sendAction("rename", this.id, this.nameBox.getValue(), this.returnId);
				this.minecraft.setScreenAndShow(null);
			}).bounds(left, y, PANEL_WIDTH, 20).build());
			y += 24;
		}
		y = visibilityButton(left, y, "private", this.canPrivate);
		y = visibilityButton(left, y, "discoverable", this.canDiscoverable);
		y = visibilityButton(left, y, "global", this.canGlobal);
		if (this.canRemove) {
			addRenderableWidget(Button.builder(LodestoneText.text("button.remove", "[X]"), button -> {
				LodestoneWarpScreen.sendAction("remove", this.id, "", this.returnId);
				this.minecraft.setScreenAndShow(null);
			}).bounds(left, y, PANEL_WIDTH, 20).build());
			y += 24;
		}

		addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("gui.cancel"), button -> {
			this.minecraft.setScreenAndShow(this.parent);
		}).bounds(left, y, PANEL_WIDTH, 20).build());
	}

	private int visibilityButton(int left, int y, String target, boolean allowed) {
		if (!allowed || target.equals(this.visibility)) {
			return y;
		}
		addRenderableWidget(Button.builder(LodestoneText.text("visibility." + target, target), button -> {
			LodestoneWarpScreen.sendAction("visibility", this.id, target, this.returnId);
			this.minecraft.setScreenAndShow(null);
		}).bounds(left, y, PANEL_WIDTH, 20).build());
		return y + 24;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = Math.max(36, (this.height - 190) / 2);
		graphics.fill(left - 10, top - 18, left + PANEL_WIDTH + 10, top + 190, 0xCC101010);
		graphics.outline(left - 10, top - 18, PANEL_WIDTH + 20, 208, 0xFF595959);
		graphics.centeredText(this.font, this.title, this.width / 2, top - 8, 0xFFFFFFFF);
		graphics.text(this.font, LodestoneText.text("edit.body", "Change this lodestone name, visibility, or registration."), left, top + 14, 0xFFA8A8A8);
		graphics.text(this.font, LodestoneText.text("visibility.current", "Visibility: %s", this.visibility), left, top + 28, 0xFFFFD37A);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}
}
