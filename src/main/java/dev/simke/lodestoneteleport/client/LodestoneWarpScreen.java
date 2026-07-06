package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import dev.simke.lodestoneteleport.LodestoneTeleportMod;
import dev.simke.lodestoneteleport.network.LodestoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
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
	private static final int PANEL_WIDTH = 430;
	private static final int ROW_HEIGHT = 24;
	private static final int GAP = 5;
	private static final int VISIBILITY_ICON_WIDTH = 14;
	private static final int EDIT_WIDTH = 47;
	private static final int FAVORITE_WIDTH = 24;
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
	private final List<Button> destinationButtons = new ArrayList<>();
	private final List<VisibleRow> visibleRows = new ArrayList<>();
	private EditBox searchBox;
	private String query = "";
	private int page = 0;

	public LodestoneWarpScreen(CompoundTag data) {
		super(LodestoneText.title());
		this.currentId = data.getStringOr("currentId", "");
		this.currentName = data.getStringOr("currentName", "");
		this.currentVisibility = data.getStringOr("currentVisibility", "discoverable");
		this.currentSubtitle = formatPosition(data, "current");
		this.currentOwner = data.getStringOr("currentOwner", "unknown");
		this.canRename = data.getBooleanOr("canRename", false);
		this.canEditCurrent = data.getBooleanOr("canEditCurrent", this.canRename);
		this.viewingAll = data.getBooleanOr("viewingAll", false);
		this.destinations = readDestinations(data.getListOrEmpty("destinations"));
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = top();

		addRenderableWidget(Button.builder(Component.literal("\u2699"), button -> {
			this.minecraft.setScreenAndShow(new LodestoneWarpSettingsScreen(this, this::refreshDestinations));
		}).bounds(left + PANEL_WIDTH - 24, top - 14, 24, 20).build());

		this.searchBox = new EditBox(this.font, left, top + 75, PANEL_WIDTH, 20, LodestoneText.text("input.search", "Search"));
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
			addRenderableWidget(Button.builder(LodestoneText.text("button.rename_current", "Edit this warp"), button -> {
				sendAction("edit", this.currentId, "");
			}).bounds(left, this.height - 38, PANEL_WIDTH, 20).build());
		}

		refreshDestinations();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = top();
		graphics.fill(left - 10, top - 18, left + PANEL_WIDTH + 10, this.height - 12, 0xCC101010);
		graphics.outline(left - 10, top - 18, PANEL_WIDTH + 20, this.height - top + 6, 0xFF595959);
		graphics.centeredText(this.font, this.title, this.width / 2, top - 9, 0xFFFFFFFF);
		graphics.text(this.font, LodestoneText.text("client.current_name", "Warp name: %s", currentNameWithIcon()), left, top + 14, 0xFFFFD37A);
		graphics.text(this.font, LodestoneText.text("client.current_coords", "Coords Warp: %s", this.currentSubtitle), left, top + 31, 0xFFA8A8A8);
		graphics.text(this.font, LodestoneText.text("client.current_owner", "Owner: %s", this.currentOwner), left, top + 48, 0xFFA8A8A8);
		if (this.viewingAll) {
			graphics.text(this.font, LodestoneText.text("client.viewing_all", "Admin view: showing all lodestones"), left + PANEL_WIDTH - 208, top + 48, 0xFFFF5555);
		}
		drawTableHeader(graphics, left, top + 100);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		drawRows(graphics, left);
	}

	private void refreshDestinations() {
		for (Button button : this.destinationButtons) {
			removeWidget(button);
		}
		this.destinationButtons.clear();
		this.visibleRows.clear();

		int left = (this.width - PANEL_WIDTH) / 2;
		int y = top() + 115;
		int bottom = this.height - 76;
		String needle = this.query.toLowerCase(Locale.ROOT).trim();
		List<Destination> filtered = filteredDestinations(needle);
		sortDestinations(filtered);
		int rowsPerPage = Math.max(1, (bottom - y + GAP) / (ROW_HEIGHT + GAP));
		int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) rowsPerPage));
		this.page = Math.min(this.page, totalPages - 1);
		int start = this.page * rowsPerPage;
		int end = Math.min(filtered.size(), start + rowsPerPage);
		int shown = 0;

		for (int index = start; index < end; index++) {
			Destination destination = filtered.get(index);
			boolean showFavorite = showColumn("favorite");
			int rowLeft = showFavorite ? left + FAVORITE_WIDTH + GAP : left;
			int editSpace = destination.canEdit() ? EDIT_WIDTH + GAP : 0;
			int teleportWidth = PANEL_WIDTH - (showFavorite ? FAVORITE_WIDTH + GAP : 0) - editSpace;
			if (showFavorite) {
				Button favorite = Button.builder(favoriteLabel(destination), button -> {
					LodestoneClientPreferences.toggleFavorite(destination.id());
					refreshDestinations();
				}).bounds(left, y, FAVORITE_WIDTH, ROW_HEIGHT).build();
				this.destinationButtons.add(favorite);
				addRenderableWidget(favorite);
			}
			Button teleport = Button.builder(Component.empty(), button -> {
				sendAction("tp", destination.id(), "");
				this.minecraft.setScreenAndShow(null);
			})
				.bounds(rowLeft, y, teleportWidth, ROW_HEIGHT)
				.build();
			this.destinationButtons.add(teleport);
			this.visibleRows.add(new VisibleRow(destination, rowLeft, teleportWidth, y));
			addRenderableWidget(teleport);
			if (destination.canEdit()) {
				Button edit = Button.builder(Component.literal("\u270e"), button -> {
					sendAction("edit", destination.id(), "", this.currentId);
				}).bounds(left + PANEL_WIDTH - EDIT_WIDTH, y, EDIT_WIDTH, ROW_HEIGHT).build();
				this.destinationButtons.add(edit);
				addRenderableWidget(edit);
			}
			y += ROW_HEIGHT + GAP;
			shown++;
		}

		if (shown == 0) {
			Button empty = Button.builder(LodestoneText.text("menu.empty", "No other destinations."), button -> {
			}).bounds(left, y, PANEL_WIDTH, ROW_HEIGHT).build();
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
		if (!LodestoneClientPreferences.get().sortFavoritesFirst) {
			return;
		}
		destinations.sort((left, right) -> {
			boolean leftFavorite = LodestoneClientPreferences.get().favorite(left.id());
			boolean rightFavorite = LodestoneClientPreferences.get().favorite(right.id());
			if (leftFavorite == rightFavorite) {
				return left.name().compareToIgnoreCase(right.name());
			}
			return leftFavorite ? -1 : 1;
		});
	}

	private void addPaginationButtons(int left, int totalPages) {
		int y = this.height - 66;
		Button previous = Button.builder(LodestoneText.text("client.page.previous", "Previous"), button -> {
			this.page = Math.max(0, this.page - 1);
			refreshDestinations();
		}).bounds(left, y, 100, 20).build();
		previous.active = this.page > 0;

		Button label = Button.builder(LodestoneText.text("client.page", "Page %s / %s", this.page + 1, totalPages), button -> {
		}).bounds(left + 110, y, PANEL_WIDTH - 220, 20).build();
		label.active = false;

		Button next = Button.builder(LodestoneText.text("client.page.next", "Next"), button -> {
			this.page = Math.min(totalPages - 1, this.page + 1);
			refreshDestinations();
		}).bounds(left + PANEL_WIDTH - 100, y, 100, 20).build();
		next.active = this.page < totalPages - 1;

		this.destinationButtons.add(previous);
		this.destinationButtons.add(label);
		this.destinationButtons.add(next);
		addRenderableWidget(previous);
		addRenderableWidget(label);
		addRenderableWidget(next);
	}

	private void drawTableHeader(GuiGraphicsExtractor graphics, int left, int y) {
		List<String> columns = visibleTextColumns();
		if (showColumn("favorite")) {
			graphics.text(this.font, LodestoneText.text("client.column.favorite", "Fav"), left + 4, y, 0xFFFFD37A);
			left += FAVORITE_WIDTH + GAP;
		}
		int width = PANEL_WIDTH - (showColumn("favorite") ? FAVORITE_WIDTH + GAP : 0) - EDIT_WIDTH - GAP;
		for (ColumnLayout column : layoutColumns(left, width, columns)) {
			graphics.text(this.font, columnTitle(column.key()), column.x(), y, 0xFF8DEEFF);
		}
	}

	private void drawRows(GuiGraphicsExtractor graphics, int left) {
		for (VisibleRow row : this.visibleRows) {
			Destination destination = row.destination();
			int textY = row.y() + 8;
			for (ColumnLayout column : layoutColumns(row.x(), row.width(), visibleTextColumns())) {
				drawColumn(graphics, destination, column, textY);
			}
		}
	}

	private void drawColumn(GuiGraphicsExtractor graphics, Destination destination, ColumnLayout column, int textY) {
		if ("cost".equals(column.key())) {
			drawCost(graphics, destination, column.x(), textY - 4);
			return;
		}
		int textX = column.x();
		String value = switch (column.key()) {
			case "coords" -> destination.coords();
			case "dimension" -> destination.dimension();
			case "owner" -> destination.owner();
			case "visibility" -> destination.visibility();
			default -> destination.name();
		};
		if ("name".equals(column.key())) {
			if (destination.global()) {
				graphics.text(this.font, "\ud83c\udf10", textX, textY, 0xFF55FF55);
				textX += VISIBILITY_ICON_WIDTH;
			} else if (destination.privateWarp()) {
				graphics.text(this.font, "\ud83d\udd12", textX, textY, 0xFFFFD37A);
				textX += VISIBILITY_ICON_WIDTH;
			}
		}
		graphics.text(this.font, truncate(value, column.width() - (textX - column.x()) - 2), textX, textY, columnColor(column.key()));
	}

	private void drawCost(GuiGraphicsExtractor graphics, Destination destination, int x, int y) {
		if (destination.costAmount() <= 0) {
			graphics.text(this.font, destination.cost(), x, y + 4, 0xFF8CFF8C);
			return;
		}
		if (destination.usesXpLevels()) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_ORB_SPRITE, x, y, 16, 16);
		} else {
			graphics.item(destination.costStack(), x, y);
		}
		graphics.text(this.font, String.valueOf(destination.costAmount()), x + 19, y + 5, 0xFFFFFFFF);
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
		int remainingWidth = width;
		int x = left + 8;
		int gap = 8;
		for (int index = 0; index < columns.size(); index++) {
			String key = columns.get(index);
			int columnWidth;
			if (index == columns.size() - 1) {
				columnWidth = Math.max(30, remainingWidth - 8);
			} else {
				columnWidth = Math.max(30, Math.min(preferredWidth(key), remainingWidth / (columns.size() - index)));
			}
			layouts.add(new ColumnLayout(key, x, columnWidth));
			x += columnWidth + gap;
			remainingWidth -= columnWidth + gap;
		}
		return layouts;
	}

	private int preferredWidth(String key) {
		return switch (key) {
			case "name" -> 145;
			case "coords" -> 86;
			case "dimension" -> 86;
			case "owner" -> 92;
			case "visibility" -> 78;
			case "cost" -> 54;
			default -> 70;
		};
	}

	private Component columnTitle(String key) {
		return LodestoneText.text("client.column." + key, key);
	}

	private int columnColor(String key) {
		return "name".equals(key) ? 0xFFFFFFFF : 0xFFD6D6D6;
	}

	private int top() {
		return Math.max(8, (this.height - 230) / 2 - 55);
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
				tag.getStringOr("owner", "unknown"),
				tag.getStringOr("dimension", ""),
				tag.getIntOr("x", 0),
				tag.getIntOr("y", 0),
				tag.getIntOr("z", 0),
				tag.getStringOr("cost", ""),
				tag.getStringOr("costType", "item"),
				tag.getStringOr("costItem", "minecraft:diamond"),
				tag.getIntOr("costAmount", 0)
			));
		}
		return destinations;
	}

	private static String formatPosition(CompoundTag tag, String prefix) {
		return tag.getIntOr(prefix + "X", 0) + " " + tag.getIntOr(prefix + "Y", 0) + " " + tag.getIntOr(prefix + "Z", 0) + " (" + tag.getStringOr(prefix + "Dimension", "") + ")";
	}

	private record Destination(String id, String name, boolean global, String visibility, boolean canEdit, String owner, String dimension, int x, int y, int z, String cost, String costType, String costItem, int costAmount) {
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
	}
}
