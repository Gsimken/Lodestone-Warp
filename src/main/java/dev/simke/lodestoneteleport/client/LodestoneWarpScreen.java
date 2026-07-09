package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import dev.simke.lodestoneteleport.LodestoneTeleportMod;
import dev.simke.lodestoneteleport.LodestoneConfig;
import dev.simke.lodestoneteleport.network.LodestoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LodestoneWarpScreen extends Screen {
	private static final int DEFAULT_PANEL_WIDTH = 620;
	private static final int DEFAULT_PANEL_HEIGHT = 420;
	private static final int MIN_PANEL_WIDTH = 430;
	private static final int MIN_PANEL_HEIGHT = 300;
	private static final int HANDLE_SIZE = 14;
	private static final int ROW_HEIGHT = 24;
	private static final int GAP = 5;
	private static final int VISIBILITY_ICON_WIDTH = 14;
	private static final int EDIT_WIDTH = 47;
	private static final int FAVORITE_WIDTH = 24;
	private static final int TOP_BUTTON_SIZE = 24;
	private static final int TOP_BUTTON_GAP = 6;
	private static final int TOP_BUTTON_RIGHT_PADDING = 12;
	private static final Identifier EXPERIENCE_ORB_SPRITE = Identifier.fromNamespaceAndPath(LodestoneTeleportMod.MOD_ID, "experience_orb");

	private final String currentId;
	private final String currentName;
	private final String currentVisibility;
	private final String currentSubtitle;
	private final String currentOwner;
	private final boolean canRename;
	private final boolean canEditCurrent;
	private final boolean viewingAll;
	private final List<Destination> destinations;
	private final long openedAtMillis;
	private final int initialCooldownSeconds;
	private final List<Button> destinationButtons = new ArrayList<>();
	private final List<VisibleRow> visibleRows = new ArrayList<>();
	private EditBox searchBox;
	private String query = "";
	private int page = 0;
	private boolean draggingPanel;
	private boolean resizingPanel;
	private double dragOffsetX;
	private double dragOffsetY;
	private int tableScrollX;
	private boolean pageHasEditButtons;
	private long lastCooldownSecond = -1L;

	public LodestoneWarpScreen(CompoundTag data) {
		super(LodestoneText.title());
		this.currentId = data.getStringOr("currentId", "");
		this.currentName = data.getStringOr("currentName", "");
		this.currentVisibility = data.getStringOr("currentVisibility", "discoverable");
		this.currentSubtitle = formatPosition(data, "current");
		this.currentOwner = firstString(data, "currentOwnerName", "currentOwner");
		this.canRename = data.getBooleanOr("canRename", false);
		this.canEditCurrent = data.getBooleanOr("canEditCurrent", this.canRename);
		this.viewingAll = data.getBooleanOr("viewingAll", false);
		this.initialCooldownSeconds = data.getIntOr("cooldownSeconds", 0);
		this.openedAtMillis = System.currentTimeMillis();
		this.destinations = readDestinations(data.getListOrEmpty("destinations"));
	}

	@Override
	public boolean isPauseScreen() {
		return LodestoneConfig.get().pauseGameInSingleplayerUi;
	}

	@Override
	protected void init() {
		clampPanelToScreen();
		int left = panelLeft();
		int top = panelTop();
		int panelWidth = panelWidth();

		int closeX = left + panelWidth - TOP_BUTTON_RIGHT_PADDING - TOP_BUTTON_SIZE;
		int maximizeX = closeX - TOP_BUTTON_GAP - TOP_BUTTON_SIZE;
		int centerX = maximizeX - TOP_BUTTON_GAP - TOP_BUTTON_SIZE;
		int settingsX = centerX - TOP_BUTTON_GAP - TOP_BUTTON_SIZE;
		Button settingsButton = Button.builder(Component.literal("\u2699"), button -> {
			this.minecraft.setScreenAndShow(new LodestoneWarpSettingsScreen(this, this::refreshDestinations));
		}).bounds(settingsX, top + 8, TOP_BUTTON_SIZE, 20).build();
		settingsButton.setTooltip(Tooltip.create(LodestoneText.text("client.tooltip.columns", "Choose visible columns and their order.")));
		addRenderableWidget(settingsButton);

		Button centerButton = Button.builder(Component.literal("\u25ce"), button -> {
			centerPanel();
			rebuildWidgets();
		}).bounds(centerX, top + 8, TOP_BUTTON_SIZE, 20).build();
		centerButton.setTooltip(Tooltip.create(LodestoneText.text("client.tooltip.center", "Center this window on screen.")));
		addRenderableWidget(centerButton);

		Button maximizeButton = Button.builder(Component.literal("\u2610"), button -> {
			maximizePanel();
			rebuildWidgets();
		}).bounds(maximizeX, top + 8, TOP_BUTTON_SIZE, 20).build();
		maximizeButton.setTooltip(Tooltip.create(LodestoneText.text("client.tooltip.maximize", "Maximize this window.")));
		addRenderableWidget(maximizeButton);

		Button closeButton = Button.builder(Component.literal("X"), button -> {
			this.minecraft.setScreenAndShow(null);
		}).bounds(closeX, top + 8, TOP_BUTTON_SIZE, 20).build();
		closeButton.setTooltip(Tooltip.create(LodestoneText.text("client.tooltip.close", "Close this window.")));
		addRenderableWidget(closeButton);

		this.searchBox = new EditBox(this.font, left + 18, top + 92, panelWidth - 36, 20, LodestoneText.text("input.search", "Search"));
		this.searchBox.setHint(LodestoneText.text("input.search", "Search"));
		this.searchBox.setMaxLength(64);
		this.searchBox.setValue(this.query);
		this.searchBox.setResponder(value -> {
			this.query = value;
			this.page = 0;
			refreshDestinations();
		});
		addRenderableWidget(this.searchBox);

		if (this.canEditCurrent) {
			Button editCurrent = Button.builder(LodestoneText.text("button.rename_current", "Edit this warp"), button -> {
				sendAction("edit", this.currentId, "");
			}).bounds(left + 18, panelBottom() - 32, panelWidth - 36, 20).build();
			editCurrent.setTooltip(Tooltip.create(LodestoneText.text("client.tooltip.edit_current", "Edit this lodestone name, visibility, or registration.")));
			addRenderableWidget(editCurrent);
		}

		refreshDestinations();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.initialCooldownSeconds <= 0) {
			return;
		}
		long remaining = remainingCooldownSeconds();
		if (remaining != this.lastCooldownSecond) {
			this.lastCooldownSecond = remaining;
			refreshDestinations();
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = panelLeft();
		int top = panelTop();
		int panelWidth = panelWidth();
		int panelHeight = panelHeight();
		graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xCC101010);
		graphics.outline(left, top, panelWidth, panelHeight, 0xFF595959);
		graphics.centeredText(this.font, this.title, left + panelWidth / 2, top + 12, 0xFFFFFFFF);
		graphics.text(this.font, LodestoneText.text("client.current_name", "Warp name: %s", currentNameWithIcon()), left + 18, top + 46, 0xFFFFD37A);
		graphics.text(this.font, LodestoneText.text("client.current_coords", "Coords Warp: %s", this.currentSubtitle), left + 18, top + 63, 0xFFA8A8A8);
		graphics.text(this.font, LodestoneText.text("client.current_owner", "Owner: %s", this.currentOwner), left + 18, top + 80, 0xFFA8A8A8);
		if (this.viewingAll) {
			graphics.text(this.font, LodestoneText.text("client.viewing_all", "Admin view: showing all lodestones"), left + panelWidth - 226, top + 80, 0xFFFF5555);
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		this.tableScrollX = clamp(this.tableScrollX, 0, tableMaxScroll());
		drawFixedTableHeader(graphics, top + 128);
		graphics.enableScissor(dataLeft(), top + 122, dataRight(), panelBottom() - 72);
		drawScrollableTableHeader(graphics, top + 128);
		drawScrollableRows(graphics);
		graphics.disableScissor();
		drawTableScrollBar(graphics);
		graphics.text(this.font, "\u21f2", left + panelWidth - HANDLE_SIZE + 2, top + panelHeight - HANDLE_SIZE + 1, 0xFF8DEEFF);
		if (inDragArea(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(this.font, LodestoneText.text("client.tooltip.move", "Drag here to move this window."), mouseX, mouseY);
		} else if (inResizeHandle(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(this.font, LodestoneText.text("client.tooltip.resize", "Drag this corner to resize."), mouseX, mouseY);
		}
	}

	private void refreshDestinations() {
		for (Button button : this.destinationButtons) {
			removeWidget(button);
		}
		this.destinationButtons.clear();
		this.visibleRows.clear();

		int left = tableLeft();
		int y = panelTop() + 143;
		int panelWidth = contentWidth();
		int bottom = panelBottom() - 70;
		String needle = this.query.toLowerCase(Locale.ROOT).trim();
		List<Destination> filtered = filteredDestinations(needle);
		sortDestinations(filtered);
		int rowsPerPage = Math.max(1, (bottom - y + GAP) / (ROW_HEIGHT + GAP));
		int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) rowsPerPage));
		this.page = Math.min(this.page, totalPages - 1);
		int start = this.page * rowsPerPage;
		int end = Math.min(filtered.size(), start + rowsPerPage);
		int shown = 0;
		this.pageHasEditButtons = false;
		for (int index = start; index < end; index++) {
			if (filtered.get(index).canEdit()) {
				this.pageHasEditButtons = true;
				break;
			}
		}

		for (int index = start; index < end; index++) {
			Destination destination = filtered.get(index);
			boolean showFavorite = showColumn("favorite");
			int rowLeft = dataLeft();
			int teleportWidth = dataRenderWidth();
			if (showFavorite) {
				Button favorite = Button.builder(favoriteLabel(destination), button -> {
					LodestoneClientPreferences.toggleFavorite(destination.id());
					refreshDestinations();
				}).bounds(left, y, FAVORITE_WIDTH, ROW_HEIGHT).build();
				favorite.setTooltip(Tooltip.create(LodestoneClientPreferences.get().favorite(destination.id())
					? LodestoneText.text("client.tooltip.unfavorite", "Remove from favorites.")
					: LodestoneText.text("client.tooltip.favorite", "Add to favorites.")));
				this.destinationButtons.add(favorite);
				addRenderableWidget(favorite);
			}
			Button teleport = Button.builder(Component.empty(), button -> {
				sendAction("tp", destination.id(), "");
				this.minecraft.setScreenAndShow(null);
			})
				.bounds(rowLeft, y, teleportWidth, ROW_HEIGHT)
				.build();
			boolean canTeleportNow = canTeleportNow(destination);
			teleport.active = canTeleportNow;
			teleport.setTooltip(Tooltip.create(canTeleportNow
				? LodestoneText.text("client.tooltip.teleport", "Teleport to %s.", destination.name())
				: LodestoneText.text("client.tooltip.teleport_disabled", "Unavailable: %s", disabledReason(destination))));
			this.destinationButtons.add(teleport);
			this.visibleRows.add(new VisibleRow(destination, rowLeft, teleportWidth, y));
			addRenderableWidget(teleport);
			if (destination.canEdit()) {
				Button edit = Button.builder(Component.literal("\u270e"), button -> {
					sendAction("edit", destination.id(), "", this.currentId);
				}).bounds(rowLeft + teleportWidth + GAP, y, EDIT_WIDTH, ROW_HEIGHT).build();
				edit.setTooltip(Tooltip.create(LodestoneText.text("client.tooltip.edit_destination", "Edit %s.", destination.name())));
				this.destinationButtons.add(edit);
				addRenderableWidget(edit);
			}
			y += ROW_HEIGHT + GAP;
			shown++;
		}

		if (shown == 0) {
			Button empty = Button.builder(LodestoneText.text("menu.empty", "No other destinations."), button -> {
			}).bounds(left, y, panelWidth, ROW_HEIGHT).build();
			empty.active = false;
			this.destinationButtons.add(empty);
			addRenderableWidget(empty);
		}
		addPaginationButtons(left, totalPages);
	}

	private List<Destination> filteredDestinations(String needle) {
		if (needle.isEmpty()) {
			return this.destinations;
		}
		List<Destination> filtered = new ArrayList<>();
		for (Destination destination : this.destinations) {
			if (destination.searchText().contains(needle)) {
				filtered.add(destination);
			}
		}
		return filtered;
	}

	private void sortDestinations(List<Destination> destinations) {
		destinations.sort((left, right) -> {
			boolean leftFavorite = LodestoneClientPreferences.get().favorite(left.id());
			boolean rightFavorite = LodestoneClientPreferences.get().favorite(right.id());
			if (LodestoneClientPreferences.get().sortFavoritesFirst && leftFavorite != rightFavorite) {
				return leftFavorite ? -1 : 1;
			}
			boolean leftAvailable = canTeleportNow(left);
			boolean rightAvailable = canTeleportNow(right);
			if (leftAvailable != rightAvailable) {
				return leftAvailable ? -1 : 1;
			}
			int costCompare = Integer.compare(left.costAmount(), right.costAmount());
			if (costCompare != 0) {
				return costCompare;
			}
			return left.name().compareToIgnoreCase(right.name());
		});
	}

	private void addPaginationButtons(int left, int totalPages) {
		int panelWidth = contentWidth();
		int y = panelBottom() - 60;
		Button previous = Button.builder(LodestoneText.text("client.page.previous", "Previous"), button -> {
			this.page = Math.max(0, this.page - 1);
			refreshDestinations();
		}).bounds(left, y, 100, 20).build();
		previous.active = this.page > 0;

		Button label = Button.builder(LodestoneText.text("client.page", "Page %s / %s", this.page + 1, totalPages), button -> {
		}).bounds(left + 110, y, panelWidth - 220, 20).build();
		label.active = false;

		Button next = Button.builder(LodestoneText.text("client.page.next", "Next"), button -> {
			this.page = Math.min(totalPages - 1, this.page + 1);
			refreshDestinations();
		}).bounds(left + panelWidth - 100, y, 100, 20).build();
		next.active = this.page < totalPages - 1;

		this.destinationButtons.add(previous);
		this.destinationButtons.add(label);
		this.destinationButtons.add(next);
		addRenderableWidget(previous);
		addRenderableWidget(label);
		addRenderableWidget(next);
	}

	private void drawFixedTableHeader(GuiGraphicsExtractor graphics, int y) {
		if (showColumn("favorite")) {
			graphics.text(this.font, LodestoneText.text("client.column.favorite", "Fav"), tableLeft() + 9, y, 0xFFFFD37A);
		}
		if (this.pageHasEditButtons) {
			graphics.centeredText(this.font, Component.literal("\u270e"), dataLeft() + dataRenderWidth() + GAP + EDIT_WIDTH / 2, y, 0xFFFFD37A);
		}
	}

	private void drawScrollableTableHeader(GuiGraphicsExtractor graphics, int y) {
		List<String> columns = visibleTextColumns();
		for (ColumnLayout column : layoutColumns(dataLeft(), dataViewportWidth(), columns)) {
			if (!columnFullyVisible(column)) {
				continue;
			}
			graphics.text(this.font, columnTitle(column.key()), column.textX(), y, 0xFF8DEEFF);
		}
	}

	private void drawScrollableRows(GuiGraphicsExtractor graphics) {
		for (VisibleRow row : this.visibleRows) {
			Destination destination = row.destination();
			int textY = row.y() + 8;
			for (ColumnLayout column : layoutColumns(row.x(), row.width(), visibleTextColumns())) {
				drawColumn(graphics, destination, column, textY);
			}
		}
	}

	private void drawColumn(GuiGraphicsExtractor graphics, Destination destination, ColumnLayout column, int textY) {
		if (!columnFullyVisible(column)) {
			return;
		}
		if ("cost".equals(column.key())) {
			drawCost(graphics, destination, column.textX(), textY - 4);
			return;
		}
		int textX = column.textX();
		String value = switch (column.key()) {
			case "coords" -> destination.coords();
			case "dimension" -> destination.dimension();
			case "owner" -> destination.owner();
			case "visibility" -> destination.visibility();
			default -> destination.name();
		};
		int color = canTeleportNow(destination) ? columnColor(column.key()) : 0xFF8A8A8A;
		if ("name".equals(column.key())) {
			if (destination.global()) {
				graphics.text(this.font, "\ud83c\udf10", textX, textY, 0xFF55FF55);
				textX += VISIBILITY_ICON_WIDTH;
			} else if (destination.privateWarp()) {
				graphics.text(this.font, "\ud83d\udd12", textX, textY, 0xFFFFD37A);
				textX += VISIBILITY_ICON_WIDTH;
			}
		}
		graphics.text(this.font, truncate(value, column.textWidth() - (textX - column.textX()) - 2), textX, textY, color);
	}

	private void drawCost(GuiGraphicsExtractor graphics, Destination destination, int x, int y) {
		if (destination.costAmount() <= 0) {
			graphics.text(this.font, destination.cost(), x, y + 4, canTeleportNow(destination) ? 0xFF8CFF8C : 0xFF8A8A8A);
			return;
		}
		if (destination.usesXpLevels()) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_ORB_SPRITE, x, y, 16, 16);
		} else {
			graphics.item(destination.costStack(), x, y);
		}
		graphics.text(this.font, String.valueOf(destination.costAmount()), x + 19, y + 5, canTeleportNow(destination) ? 0xFFFFFFFF : 0xFF8A8A8A);
	}

	private Component currentNameWithIcon() {
		return Component.empty().append(visibilityIcon(this.currentVisibility)).append(Component.literal(this.currentName));
	}

	private static Component visibilityIcon(String visibility) {
		if ("global".equals(visibility)) {
			return Component.literal("\ud83c\udf10 ").withStyle(ChatFormatting.GREEN);
		}
		if ("private".equals(visibility)) {
			return Component.literal("\ud83d\udd12 ").withStyle(ChatFormatting.GOLD);
		}
		return Component.empty();
	}

	private String truncate(String value, int width) {
		if (this.font.width(value) <= width) {
			return value;
		}
		return this.font.plainSubstrByWidth(value, Math.max(1, width - this.font.width("..."))) + "...";
	}

	private Component favoriteLabel(Destination destination) {
		return Component.literal(LodestoneClientPreferences.get().favorite(destination.id()) ? "\u2605" : "\u2606")
			.withStyle(LodestoneClientPreferences.get().favorite(destination.id()) ? ChatFormatting.GOLD : ChatFormatting.GRAY);
	}

	private boolean canTeleportNow(Destination destination) {
		return destination.canTeleport() || (destination.cooldownSeconds() > 0 && remainingCooldownSeconds() <= 0);
	}

	private String disabledReason(Destination destination) {
		long remaining = remainingCooldownSeconds();
		if (destination.cooldownSeconds() > 0 && remaining > 0) {
			return LodestoneText.text("error.cooldown", "You must wait %s seconds before teleporting again.", remaining).getString();
		}
		return destination.disabledReason();
	}

	private long remainingCooldownSeconds() {
		if (this.initialCooldownSeconds <= 0) {
			return 0L;
		}
		long elapsed = Math.max(0L, System.currentTimeMillis() - this.openedAtMillis);
		long remainingMillis = this.initialCooldownSeconds * 1000L - elapsed;
		return remainingMillis <= 0L ? 0L : (long) Math.ceil(remainingMillis / 1000.0D);
	}

	private boolean showColumn(String key) {
		return LodestoneClientPreferences.get().columns().contains(key);
	}

	private List<String> visibleTextColumns() {
		return LodestoneClientPreferences.get().columns().stream()
			.filter(column -> !"favorite".equals(column))
			.toList();
	}

	private List<ColumnLayout> layoutColumns(int left, int width, List<String> columns) {
		List<ColumnLayout> layouts = new ArrayList<>();
		if (columns.isEmpty()) {
			return layouts;
		}
		int gap = columnGap(columns, width);
		int available = Math.max(30, width - 16);
		int preferredTotal = 0;
		for (String column : columns) {
			preferredTotal += preferredWidth(column);
		}
		int totalWidth = preferredTotal + (columns.size() - 1) * gap;
		boolean overflow = totalWidth > available;
		int x = left + 10 - (overflow ? this.tableScrollX : 0);
		for (int index = 0; index < columns.size(); index++) {
			String key = columns.get(index);
			int columnWidth = preferredWidth(key);
			layouts.add(new ColumnLayout(key, x, columnWidth));
			x += columnWidth + gap;
		}
		return layouts;
	}

	private int columnGap(List<String> columns, int width) {
		if (columns.size() <= 1) {
			return 10;
		}
		int available = Math.max(30, width - 16);
		int preferredTotal = 0;
		for (String column : columns) {
			preferredTotal += preferredWidth(column);
		}
		if (preferredTotal >= available) {
			return 10;
		}
		return clamp((available - preferredTotal) / (columns.size() - 1), 10, 80);
	}

	private int preferredWidth(String key) {
		return switch (key) {
			case "name" -> 145;
			case "coords" -> 86;
			case "dimension" -> 96;
			case "owner" -> 96;
			case "visibility" -> 92;
			case "cost" -> 62;
			default -> 70;
		};
	}

	private int minWidth(String key) {
		return switch (key) {
			case "cost" -> 52;
			case "coords" -> 74;
			case "name", "dimension", "owner", "visibility" -> 70;
			default -> 44;
		};
	}

	private Component columnTitle(String key) {
		return LodestoneText.text("client.column." + key, key);
	}

	private int columnColor(String key) {
		return "name".equals(key) ? 0xFFFFFFFF : 0xFFD6D6D6;
	}

	private boolean columnFullyVisible(ColumnLayout column) {
		return column.textX() >= dataLeft() + 4 && column.textX() + column.textWidth() <= dataLeft() + dataRenderWidth() - 4;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && inResizeHandle(event.x(), event.y())) {
			this.resizingPanel = true;
			return true;
		}
		if (event.button() == 0 && inDragArea(event.x(), event.y())) {
			this.draggingPanel = true;
			this.dragOffsetX = event.x() - panelLeft();
			this.dragOffsetY = event.y() - panelTop();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int maxScroll = tableMaxScroll();
		if (maxScroll > 0 && inTableArea(mouseX, mouseY)) {
			double amount = horizontalAmount != 0.0D ? horizontalAmount : verticalAmount;
			this.tableScrollX = clamp(this.tableScrollX - (int) Math.signum(amount) * 24, 0, maxScroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (event.button() == 0 && this.resizingPanel) {
			LodestoneClientPreferences preferences = LodestoneClientPreferences.get();
			preferences.modUiPanelWidth = clamp((int) event.x() - panelLeft(), MIN_PANEL_WIDTH, this.width - panelLeft() - 8);
			preferences.modUiPanelHeight = clamp((int) event.y() - panelTop(), MIN_PANEL_HEIGHT, this.height - panelTop() - 8);
			rebuildWidgets();
			return true;
		}
		if (event.button() == 0 && this.draggingPanel) {
			LodestoneClientPreferences preferences = LodestoneClientPreferences.get();
			preferences.modUiPanelX = clamp((int) (event.x() - this.dragOffsetX), 8, this.width - panelWidth() - 8);
			preferences.modUiPanelY = clamp((int) (event.y() - this.dragOffsetY), 8, this.height - panelHeight() - 8);
			rebuildWidgets();
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0 && (this.draggingPanel || this.resizingPanel)) {
			this.draggingPanel = false;
			this.resizingPanel = false;
			LodestoneClientPreferences.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	private int panelLeft() {
		int configured = LodestoneClientPreferences.get().modUiPanelX;
		return configured < 0 ? (this.width - panelWidth()) / 2 : configured;
	}

	private int panelTop() {
		int configured = LodestoneClientPreferences.get().modUiPanelY;
		return configured < 0 ? Math.max(8, (this.height - panelHeight()) / 2) : configured;
	}

	private int panelWidth() {
		return clamp(LodestoneClientPreferences.get().modUiPanelWidth <= 0 ? DEFAULT_PANEL_WIDTH : LodestoneClientPreferences.get().modUiPanelWidth, MIN_PANEL_WIDTH, Math.max(MIN_PANEL_WIDTH, this.width - 16));
	}

	private int panelHeight() {
		return clamp(LodestoneClientPreferences.get().modUiPanelHeight <= 0 ? DEFAULT_PANEL_HEIGHT : LodestoneClientPreferences.get().modUiPanelHeight, MIN_PANEL_HEIGHT, Math.max(MIN_PANEL_HEIGHT, this.height - 16));
	}

	private int panelBottom() {
		return panelTop() + panelHeight();
	}

	private int contentWidth() {
		return panelWidth() - 36;
	}

	private void centerPanel() {
		LodestoneClientPreferences preferences = LodestoneClientPreferences.get();
		preferences.modUiPanelX = (this.width - panelWidth()) / 2;
		preferences.modUiPanelY = (this.height - panelHeight()) / 2;
		LodestoneClientPreferences.save();
	}

	private void maximizePanel() {
		LodestoneClientPreferences preferences = LodestoneClientPreferences.get();
		preferences.modUiPanelWidth = Math.max(MIN_PANEL_WIDTH, this.width - 16);
		preferences.modUiPanelHeight = Math.max(MIN_PANEL_HEIGHT, this.height - 16);
		preferences.modUiPanelX = 8;
		preferences.modUiPanelY = 8;
		this.tableScrollX = clamp(this.tableScrollX, 0, tableMaxScroll());
		LodestoneClientPreferences.save();
	}

	private void clampPanelToScreen() {
		LodestoneClientPreferences preferences = LodestoneClientPreferences.get();
		preferences.modUiPanelWidth = panelWidth();
		preferences.modUiPanelHeight = panelHeight();
		if (preferences.modUiPanelX >= 0) {
			preferences.modUiPanelX = clamp(preferences.modUiPanelX, 8, this.width - preferences.modUiPanelWidth - 8);
		}
		if (preferences.modUiPanelY >= 0) {
			preferences.modUiPanelY = clamp(preferences.modUiPanelY, 8, this.height - preferences.modUiPanelHeight - 8);
		}
	}

	@Override
	public void resize(int width, int height) {
		fitPanelToScreen(width, height);
		super.resize(width, height);
	}

	private void fitPanelToScreen(int screenWidth, int screenHeight) {
		LodestoneClientPreferences preferences = LodestoneClientPreferences.get();
		int maxWidth = Math.max(MIN_PANEL_WIDTH, screenWidth - 16);
		int maxHeight = Math.max(MIN_PANEL_HEIGHT, screenHeight - 16);
		preferences.modUiPanelWidth = clamp(Math.max(preferences.modUiPanelWidth, Math.min(DEFAULT_PANEL_WIDTH, maxWidth)), MIN_PANEL_WIDTH, maxWidth);
		preferences.modUiPanelHeight = clamp(Math.max(preferences.modUiPanelHeight, Math.min(DEFAULT_PANEL_HEIGHT, maxHeight)), MIN_PANEL_HEIGHT, maxHeight);
		preferences.modUiPanelX = Math.max(8, (screenWidth - preferences.modUiPanelWidth) / 2);
		preferences.modUiPanelY = Math.max(8, (screenHeight - preferences.modUiPanelHeight) / 2);
		this.tableScrollX = clamp(this.tableScrollX, 0, tableMaxScroll());
		LodestoneClientPreferences.save();
	}

	private boolean inDragArea(double mouseX, double mouseY) {
		int reservedButtons = TOP_BUTTON_RIGHT_PADDING + TOP_BUTTON_SIZE * 4 + TOP_BUTTON_GAP * 3 + 8;
		return mouseX >= panelLeft() && mouseX <= panelLeft() + panelWidth() - reservedButtons && mouseY >= panelTop() && mouseY <= panelTop() + 34;
	}

	private boolean inResizeHandle(double mouseX, double mouseY) {
		return mouseX >= panelLeft() + panelWidth() - HANDLE_SIZE && mouseX <= panelLeft() + panelWidth()
			&& mouseY >= panelTop() + panelHeight() - HANDLE_SIZE && mouseY <= panelTop() + panelHeight();
	}

	private boolean inTableArea(double mouseX, double mouseY) {
		return mouseX >= tableLeft() && mouseX <= tableRight() && mouseY >= panelTop() + 122 && mouseY <= panelBottom() - 72;
	}

	private int tableLeft() {
		return panelLeft() + 18;
	}

	private int tableRight() {
		return panelLeft() + panelWidth() - 18;
	}

	private int dataLeft() {
		return tableLeft() + (showColumn("favorite") ? FAVORITE_WIDTH + GAP : 0);
	}

	private int dataRight() {
		return dataLeft() + dataViewportWidth();
	}

	private int dataRenderWidth() {
		return dataViewportWidth();
	}

	private int dataViewportWidth() {
		return Math.max(40, contentWidth() - (showColumn("favorite") ? FAVORITE_WIDTH + GAP : 0) - editReserveWidth());
	}

	private int editReserveWidth() {
		return this.pageHasEditButtons ? EDIT_WIDTH + GAP : 0;
	}

	private int dataTotalWidth() {
		List<String> columns = visibleTextColumns();
		if (columns.isEmpty()) {
			return 0;
		}
		int total = 16;
		for (String column : columns) {
			total += preferredWidth(column);
		}
		total += Math.max(0, columns.size() - 1) * columnGap(columns, dataViewportWidth());
		return total;
	}

	private int tableMaxScroll() {
		return Math.max(0, dataTotalWidth() - dataViewportWidth());
	}

	private void drawTableScrollBar(GuiGraphicsExtractor graphics) {
		int maxScroll = tableMaxScroll();
		if (maxScroll <= 0) {
			return;
		}
		int x = dataLeft();
		int y = panelBottom() - 86;
		int width = dataViewportWidth();
		int thumbWidth = Math.max(28, width * width / Math.max(width, dataTotalWidth()));
		int thumbX = x + (width - thumbWidth) * this.tableScrollX / maxScroll;
		graphics.fill(x, y, x + width, y + 3, 0x66000000);
		graphics.fill(thumbX, y, thumbX + thumbWidth, y + 3, 0xFF8DEEFF);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	static void sendAction(String action, String id, String name) {
		sendAction(action, id, name, "");
	}

	static void sendAction(String action, String id, String name, String returnId) {
		CompoundTag data = new CompoundTag();
		data.putString("action", action);
		data.putString("id", id);
		if ("visibility".equals(action)) {
			data.putString("visibility", name);
		} else if (!name.isBlank()) {
			data.putString("name", name);
		}
		if (!returnId.isBlank()) {
			data.putString("returnId", returnId);
		}
		ClientPlayNetworking.send(new LodestoneActionPayload(data));
	}

	static void sendEditSave(String id, String name, String visibility, String returnId) {
		CompoundTag data = new CompoundTag();
		data.putString("action", "save_edit");
		data.putString("id", id);
		data.putString("name", name);
		data.putString("visibility", visibility);
		if (!returnId.isBlank()) {
			data.putString("returnId", returnId);
		}
		ClientPlayNetworking.send(new LodestoneActionPayload(data));
	}

	private static List<Destination> readDestinations(ListTag tags) {
		List<Destination> destinations = new ArrayList<>();
		for (int index = 0; index < tags.size(); index++) {
			CompoundTag tag = tags.getCompoundOrEmpty(index);
			destinations.add(new Destination(
				tag.getStringOr("id", ""),
				tag.getStringOr("name", ""),
				tag.getBooleanOr("global", false),
				tag.getStringOr("visibility", tag.getBooleanOr("global", false) ? "global" : "discoverable"),
				tag.getBooleanOr("canEdit", false),
				firstString(tag, "ownerName", "owner"),
				tag.getStringOr("dimension", ""),
				tag.getIntOr("x", 0),
				tag.getIntOr("y", 0),
				tag.getIntOr("z", 0),
				tag.getStringOr("cost", ""),
				tag.getStringOr("costType", "item"),
				tag.getStringOr("costItem", "minecraft:diamond"),
				tag.getIntOr("costAmount", 0),
				tag.getBooleanOr("canTeleport", true),
				tag.getStringOr("disabledReason", ""),
				tag.getIntOr("cooldownSeconds", 0)
			));
		}
		return destinations;
	}

	private static String formatPosition(CompoundTag tag, String prefix) {
		return tag.getIntOr(prefix + "X", 0) + " " + tag.getIntOr(prefix + "Y", 0) + " " + tag.getIntOr(prefix + "Z", 0) + " (" + tag.getStringOr(prefix + "Dimension", "") + ")";
	}

	private static String firstString(CompoundTag tag, String... keys) {
		for (String key : keys) {
			String value = tag.getStringOr(key, "");
			if (!value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
				return value;
			}
		}
		return "unknown";
	}

	private record Destination(String id, String name, boolean global, String visibility, boolean canEdit, String owner, String dimension, int x, int y, int z, String cost, String costType, String costItem, int costAmount, boolean canTeleport, String disabledReason, int cooldownSeconds) {
		String coords() {
			return x + " " + y + " " + z;
		}

		String searchText() {
			return (name + " " + owner + " " + visibility + " " + dimension + " " + x + " " + y + " " + z).toLowerCase(Locale.ROOT);
		}

		boolean privateWarp() {
			return "private".equals(visibility);
		}

		boolean hasVisibilityIcon() {
			return global || privateWarp();
		}

		ItemStack costStack() {
			Item item = BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(costItem)).orElse(Items.DIAMOND);
			return new ItemStack(item);
		}

		boolean usesXpLevels() {
			return "xp_levels".equals(costType);
		}
	}

	private record VisibleRow(Destination destination, int x, int width, int y) {
	}

	private record ColumnLayout(String key, int x, int width) {
		int textX() {
			return x + 2;
		}

		int textWidth() {
			return Math.max(1, width - 4);
		}
	}
}
