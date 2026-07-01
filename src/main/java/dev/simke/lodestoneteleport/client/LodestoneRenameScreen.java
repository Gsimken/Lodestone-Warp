package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class LodestoneRenameScreen extends Screen {
	private static final int PANEL_WIDTH = 360;
	private static final int PANEL_HEIGHT = 178;

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
	private final List<String> allowedVisibilities = new ArrayList<>();
	private EditBox nameBox;
	private Button modeButton;
	private String pendingVisibility;

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
		this.pendingVisibility = visibility;
		this.allowedVisibilities.add(visibility);
		addVisibilityOption("private", canPrivate);
		addVisibilityOption("discoverable", canDiscoverable);
		addVisibilityOption("global", canGlobal);
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = top();

		this.nameBox = new EditBox(this.font, left, top + 58, PANEL_WIDTH, 20, LodestoneText.text("input.name", "Name"));
		this.nameBox.setMaxLength(48);
		this.nameBox.setValue(this.currentName);
		this.nameBox.setEditable(this.canRename);
		addRenderableWidget(this.nameBox);
		if (this.canRename) {
			setInitialFocus(this.nameBox);
		}

		int y = top + 86;
		int halfWidth = (PANEL_WIDTH - 8) / 2;
		this.modeButton = Button.builder(modeButtonText(), button -> cycleVisibility())
			.bounds(left, y, halfWidth, 20)
			.build();
		this.modeButton.active = this.allowedVisibilities.size() > 1;
		addRenderableWidget(this.modeButton);

		if (this.canRemove) {
			addRenderableWidget(Button.builder(LodestoneText.text("button.remove_current", "Remove lodestone").withStyle(ChatFormatting.RED), button -> {
				LodestoneWarpScreen.sendAction("remove", this.id, "", this.returnId);
				this.minecraft.setScreenAndShow(null);
			}).bounds(left + halfWidth + 8, y, halfWidth, 20).build());
		}

		y += 28;
		addRenderableWidget(Button.builder(LodestoneText.text("button.save", "Save").withStyle(ChatFormatting.GREEN), button -> {
			LodestoneWarpScreen.sendEditSave(this.id, this.nameBox.getValue(), this.pendingVisibility, this.returnId);
			this.minecraft.setScreenAndShow(null);
		}).bounds(left, y, halfWidth, 20).build());

		addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("gui.cancel"), button -> {
			this.minecraft.setScreenAndShow(this.parent);
		}).bounds(left + halfWidth + 8, y, halfWidth, 20).build());
	}

	private void addVisibilityOption(String visibility, boolean allowed) {
		if (allowed && !this.allowedVisibilities.contains(visibility)) {
			this.allowedVisibilities.add(visibility);
		}
	}

	private void cycleVisibility() {
		int index = this.allowedVisibilities.indexOf(this.pendingVisibility);
		this.pendingVisibility = this.allowedVisibilities.get((index + 1) % this.allowedVisibilities.size());
		this.modeButton.setMessage(modeButtonText());
	}

	private Component modeButtonText() {
		return LodestoneText.text("button.mode", "Mode: %s", visibilityName(this.pendingVisibility)).withStyle(visibilityColor(this.pendingVisibility));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = top();
		graphics.fill(left - 10, top - 18, left + PANEL_WIDTH + 10, top + PANEL_HEIGHT, 0xCC101010);
		graphics.outline(left - 10, top - 18, PANEL_WIDTH + 20, PANEL_HEIGHT + 18, 0xFF595959);
		graphics.centeredText(this.font, this.title, this.width / 2, top - 8, 0xFFFFFFFF);
		graphics.text(this.font, LodestoneText.text("edit.body", "Change this lodestone name, visibility, or registration."), left, top + 14, 0xFFA8A8A8);
		graphics.text(this.font, LodestoneText.text("visibility.current", "Visibility: %s", visibilityName(this.pendingVisibility)), left, top + 30, 0xFFFFD37A);
		graphics.text(this.font, LodestoneText.text("input.name", "Name"), left, top + 47, 0xFF8DEEFF);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private Component visibilityName(String value) {
		return LodestoneText.text("visibility.value." + value, value);
	}

	private ChatFormatting visibilityColor(String value) {
		return switch (value) {
			case "private" -> ChatFormatting.GOLD;
			case "global" -> ChatFormatting.GREEN;
			default -> ChatFormatting.AQUA;
		};
	}

	private int top() {
		return Math.max(28, (this.height - PANEL_HEIGHT) / 2);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}
}
