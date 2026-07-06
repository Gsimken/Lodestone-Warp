package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class LodestoneWarpSettingsScreen extends Screen {
	private static final int PANEL_WIDTH = 460;
	private static final int ROW_HEIGHT = 24;

	private final Screen parent;
	private final Runnable onDone;
	private final List<String> columns;

	public LodestoneWarpSettingsScreen(Screen parent, Runnable onDone) {
		super(LodestoneText.text("client.settings.title", "Warp UI settings"));
		this.parent = parent;
		this.onDone = onDone;
		this.columns = new ArrayList<>(LodestoneClientPreferences.get().columns());
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int y = top() + 56;
		for (String column : rows()) {
			boolean enabled = this.columns.contains(column);
			addRenderableWidget(Button.builder(columnLabel(column, enabled), button -> {
				toggleColumn(column);
				rebuildWidgets();
			}).bounds(left, y, 190, 20).build());

			Button leftButton = Button.builder(Component.literal("<"), button -> {
				moveColumn(column, -1);
				rebuildWidgets();
			}).bounds(left + 205, y, 42, 20).build();
			leftButton.active = enabled && this.columns.indexOf(column) > 0;
			addRenderableWidget(leftButton);

			Button rightButton = Button.builder(Component.literal(">"), button -> {
				moveColumn(column, 1);
				rebuildWidgets();
			}).bounds(left + 252, y, 42, 20).build();
			rightButton.active = enabled && this.columns.indexOf(column) >= 0 && this.columns.indexOf(column) < this.columns.size() - 1;
			addRenderableWidget(rightButton);

			addRenderableWidget(Button.builder(LodestoneText.text("client.settings.add_last", "Add last"), button -> {
				addLast(column);
				rebuildWidgets();
			}).bounds(left + 310, y, 120, 20).build()).active = !enabled;
			y += ROW_HEIGHT;
		}

		int actionsY = this.height - 36;
		addRenderableWidget(Button.builder(LodestoneText.text("client.settings.reset", "Reset"), button -> {
			this.columns.clear();
			this.columns.addAll(List.of("favorite", "name", "coords", "dimension", "cost"));
			rebuildWidgets();
		}).bounds(left, actionsY, 120, 20).build());
		addRenderableWidget(Button.builder(LodestoneText.text("button.save", "Save"), button -> saveAndClose()).bounds(left + 130, actionsY, 150, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.minecraft.setScreenAndShow(this.parent)).bounds(left + 290, actionsY, 140, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = top();
		graphics.fill(left - 12, top - 18, left + PANEL_WIDTH + 12, this.height - 10, 0xDD101010);
		graphics.outline(left - 12, top - 18, PANEL_WIDTH + 24, this.height - top + 8, 0xFF595959);
		graphics.centeredText(this.font, this.title, this.width / 2, top - 8, 0xFFFFFFFF);
		graphics.text(this.font, LodestoneText.text("client.settings.body", "Choose visible columns and move them left or right."), left, top + 18, 0xFFA8A8A8);
		graphics.text(this.font, LodestoneText.text("client.settings.order", "Current order: %s", String.join(", ", this.columns)), left, top + 34, 0xFFFFD37A);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

	private void toggleColumn(String column) {
		if (this.columns.contains(column)) {
			if (this.columns.size() > 1) {
				this.columns.remove(column);
			}
			return;
		}
		this.columns.add(column);
	}

	private void addLast(String column) {
		if (!this.columns.contains(column)) {
			this.columns.add(column);
		}
	}

	private void moveColumn(String column, int direction) {
		int index = this.columns.indexOf(column);
		int next = index + direction;
		if (index < 0 || next < 0 || next >= this.columns.size()) {
			return;
		}
		this.columns.remove(index);
		this.columns.add(next, column);
	}

	private void saveAndClose() {
		LodestoneClientPreferences.get().setColumns(this.columns);
		this.onDone.run();
		this.minecraft.setScreenAndShow(this.parent);
	}

	private List<String> rows() {
		List<String> rows = new ArrayList<>(this.columns);
		for (String column : LodestoneClientPreferences.allColumns()) {
			if (!rows.contains(column)) {
				rows.add(column);
			}
		}
		return rows;
	}

	private Component columnLabel(String column, boolean enabled) {
		Component label = LodestoneText.text("client.column." + column, column);
		MutableComponent marker = enabled ? Component.literal("[x] ") : Component.literal("[ ] ");
		return marker.append(label).withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.GRAY);
	}

	private int top() {
		return Math.max(18, (this.height - 260) / 2);
	}
}
