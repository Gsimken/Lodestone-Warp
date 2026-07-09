package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneConfig;
import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public final class LodestoneWarpSettingsScreen extends Screen {
	private static final int PANEL_WIDTH = 460;
	private static final int ROW_HEIGHT = 24;
	private static final int TOGGLE_WIDTH = 170;
	private static final int DRAG_HANDLE_WIDTH = 24;
	private static final int MOVE_BUTTON_WIDTH = 28;

	private final Screen parent;
	private final Runnable onDone;
	private final List<String> columns;
	private String draggingColumn;

	public LodestoneWarpSettingsScreen(Screen parent, Runnable onDone) {
		super(LodestoneText.text("client.settings.title", "Warp UI settings"));
		this.parent = parent;
		this.onDone = onDone;
		this.columns = new ArrayList<>(LodestoneClientPreferences.get().columns());
	}

	@Override
	public boolean isPauseScreen() {
		return LodestoneConfig.get().pauseGameInSingleplayerUi;
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int y = top() + 56;
		for (String column : rows()) {
			boolean enabled = this.columns.contains(column);
			boolean movable = canMoveColumn(column);
			addRenderableWidget(Button.builder(columnLabel(column, enabled), button -> {
				toggleColumn(column);
				rebuildWidgets();
			}).bounds(left, y, TOGGLE_WIDTH, 20).build());

			Button dragHandle = Button.builder(Component.literal("\u2630"), button -> {
			}).bounds(left + TOGGLE_WIDTH + 5, y, DRAG_HANDLE_WIDTH, 20).build();
			dragHandle.active = enabled && movable;
			addRenderableWidget(dragHandle);

			Button upButton = Button.builder(Component.literal("\u25b2"), button -> {
				moveColumn(column, -1);
				rebuildWidgets();
			}).bounds(left + 205, y, MOVE_BUTTON_WIDTH, 20).build();
			upButton.active = enabled && movable && this.columns.indexOf(column) > firstMovableIndex();
			addRenderableWidget(upButton);

			Button downButton = Button.builder(Component.literal("\u25bc"), button -> {
				moveColumn(column, 1);
				rebuildWidgets();
			}).bounds(left + 238, y, MOVE_BUTTON_WIDTH, 20).build();
			downButton.active = enabled && movable && this.columns.indexOf(column) >= 0 && this.columns.indexOf(column) < this.columns.size() - 1;
			addRenderableWidget(downButton);

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
		graphics.text(this.font, LodestoneText.text("client.settings.body", "Toggle columns, then drag the handle to reorder enabled ones."), left, top + 18, 0xFFA8A8A8);
		graphics.text(this.font, LodestoneText.text("client.settings.order", "Current order: %s", String.join(", ", this.columns)), left, top + 34, 0xFFFFD37A);
		String hovered = rowAt(mouseY);
		if (hovered != null && this.columns.contains(hovered)) {
			int rowY = rowY(hovered);
			int color = hovered.equals(this.draggingColumn) ? 0xFFFFD37A : 0x668DEEFF;
			graphics.outline(left - 2, rowY - 2, TOGGLE_WIDTH + DRAG_HANDLE_WIDTH + 9, 24, color);
		}
		if (this.draggingColumn != null) {
			Component label = LodestoneText.text("client.settings.dragging", "Dragging: %s", columnName(this.draggingColumn));
			graphics.fill(left, mouseY - 12, left + TOGGLE_WIDTH + DRAG_HANDLE_WIDTH + 5, mouseY + 12, 0xCC30260A);
			graphics.outline(left, mouseY - 12, TOGGLE_WIDTH + DRAG_HANDLE_WIDTH + 5, 24, 0xFFFFD37A);
			graphics.text(this.font, label, left + 8, mouseY - 4, 0xFFFFD37A);
			String target = rowAt(mouseY);
			if (target != null && this.columns.contains(target) && !target.equals(this.draggingColumn)) {
				int targetY = rowY(target);
				graphics.fill(left - 4, targetY - 4, left + TOGGLE_WIDTH + DRAG_HANDLE_WIDTH + 9, targetY - 2, 0xFFFFD37A);
			}
			graphics.setTooltipForNextFrame(this.font, LodestoneText.text("client.tooltip.drag_column", "Drop on another enabled column to reorder."), mouseX, mouseY);
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			String column = rowAt(event.y());
			if (column != null && this.columns.contains(column) && canMoveColumn(column) && event.x() >= dragHandleLeft() && event.x() <= dragHandleLeft() + DRAG_HANDLE_WIDTH) {
				this.draggingColumn = column;
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0 && this.draggingColumn != null) {
			String target = rowAt(event.y());
			if (target != null && this.columns.contains(target) && canMoveColumn(target) && !target.equals(this.draggingColumn)) {
				moveColumnTo(this.draggingColumn, target);
				this.draggingColumn = null;
				rebuildWidgets();
				return true;
			}
			this.draggingColumn = null;
			return true;
		}
		return super.mouseReleased(event);
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
		if (!canMoveColumn(column)) {
			return;
		}
		int index = this.columns.indexOf(column);
		int next = index + direction;
		if (index < 0 || next < firstMovableIndex() || next >= this.columns.size()) {
			return;
		}
		this.columns.remove(index);
		this.columns.add(next, column);
	}

	private void moveColumnTo(String column, String target) {
		if (!canMoveColumn(column) || !canMoveColumn(target)) {
			return;
		}
		int from = this.columns.indexOf(column);
		int to = this.columns.indexOf(target);
		if (from < 0 || to < 0 || from == to) {
			return;
		}
		this.columns.remove(from);
		if (from < to) {
			to--;
		}
		this.columns.add(to, column);
	}

	private boolean canMoveColumn(String column) {
		return this.columns.contains(column) && !"favorite".equals(column);
	}

	private int firstMovableIndex() {
		int favorite = this.columns.indexOf("favorite");
		return favorite == 0 ? 1 : 0;
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
		Component label = columnName(column);
		MutableComponent marker = enabled ? Component.literal("[x] ") : Component.literal("[ ] ");
		return marker.append(label).withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.GRAY);
	}

	private Component columnName(String column) {
		return LodestoneText.text("client.column." + column, column);
	}

	private String rowAt(double mouseY) {
		int index = (int) ((mouseY - (top() + 56)) / ROW_HEIGHT);
		List<String> rows = rows();
		if (index < 0 || index >= rows.size()) {
			return null;
		}
		return rows.get(index);
	}

	private int rowY(String column) {
		int index = rows().indexOf(column);
		return top() + 56 + index * ROW_HEIGHT;
	}

	private int left() {
		return (this.width - PANEL_WIDTH) / 2;
	}

	private int dragHandleLeft() {
		return left() + TOGGLE_WIDTH + 5;
	}

	private int top() {
		return Math.max(18, (this.height - 260) / 2);
	}
}
