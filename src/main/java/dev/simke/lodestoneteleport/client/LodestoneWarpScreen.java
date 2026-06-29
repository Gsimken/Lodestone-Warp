package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneText;
import dev.simke.lodestoneteleport.LodestoneTeleportMod;
import dev.simke.lodestoneteleport.network.LodestoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
	private static final int NAME_X = 8;
	private static final int COORDS_X = 160;
	private static final int DIMENSION_X = 260;
	private static final int COST_X = 350;
	private static final Identifier EXPERIENCE_ORB_TEXTURE = Identifier.fromNamespaceAndPath(LodestoneTeleportMod.MOD_ID, "textures/gui/experience_orb.png");

	private final String currentId;
	private final String currentName;
	private final String currentSubtitle;
	private final boolean canRename;
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
		this.currentSubtitle = formatPosition(data, "current");
		this.canRename = data.getBooleanOr("canRename", false);
		this.destinations = readDestinations(data.getListOrEmpty("destinations"));
	}

	@Override
	protected void init() {
		int left = (this.width - PANEL_WIDTH) / 2;
		int top = top();

		this.searchBox = new EditBox(this.font, left, top + 58, PANEL_WIDTH, 20, LodestoneText.text("input.search", "Search"));
		this.searchBox.setHint(LodestoneText.text("input.search", "Search"));
		this.searchBox.setMaxLength(64);
		this.searchBox.setValue(this.query);
		this.searchBox.setResponder(value -> {
			this.query = value;
			this.page = 0;
			refreshDestinations();
		});
		addRenderableWidget(this.searchBox);

		if (this.canRename) {
			addRenderableWidget(Button.builder(LodestoneText.text("button.rename_current", "Rename this warp"), button -> {
				this.minecraft.setScreenAndShow(new LodestoneRenameScreen(this, this.currentId, this.currentName, this.currentId));
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
		graphics.text(this.font, LodestoneText.text("client.current_name", "Warp name: %s", this.currentName), left, top + 14, 0xFFFFD37A);
		graphics.text(this.font, LodestoneText.text("client.current_coords", "Coords Warp: %s", this.currentSubtitle), left, top + 31, 0xFFA8A8A8);
		drawTableHeader(graphics, left, top + 83);
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
		int y = top() + 98;
		int bottom = this.height - 76;
		String needle = this.query.toLowerCase(Locale.ROOT).trim();
		List<Destination> filtered = filteredDestinations(needle);
		int rowsPerPage = Math.max(1, (bottom - y + GAP) / (ROW_HEIGHT + GAP));
		int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) rowsPerPage));
		this.page = Math.min(this.page, totalPages - 1);
		int start = this.page * rowsPerPage;
		int end = Math.min(filtered.size(), start + rowsPerPage);
		int shown = 0;

		for (int index = start; index < end; index++) {
			Destination destination = filtered.get(index);
			int teleportWidth = this.canRename ? PANEL_WIDTH - 52 : PANEL_WIDTH;
			Button teleport = Button.builder(Component.empty(), button -> {
				sendAction("tp", destination.id(), "");
				this.minecraft.setScreenAndShow(null);
			})
				.bounds(left, y, teleportWidth, ROW_HEIGHT)
				.build();
			this.destinationButtons.add(teleport);
			this.visibleRows.add(new VisibleRow(destination, y));
			addRenderableWidget(teleport);
			if (this.canRename) {
				Button edit = Button.builder(Component.literal("\u270e"), button -> {
					this.minecraft.setScreenAndShow(new LodestoneRenameScreen(this, destination.id(), destination.name(), this.currentId));
				}).bounds(left + PANEL_WIDTH - 47, y, 47, ROW_HEIGHT).build();
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
		graphics.text(this.font, LodestoneText.text("client.column.name", "Name"), left + NAME_X, y, 0xFF8DEEFF);
		graphics.text(this.font, LodestoneText.text("client.column.coords", "Coords"), left + COORDS_X, y, 0xFF8DEEFF);
		graphics.text(this.font, LodestoneText.text("client.column.dimension", "Dimension"), left + DIMENSION_X, y, 0xFF8DEEFF);
		graphics.text(this.font, LodestoneText.text("client.column.cost", "Cost"), left + COST_X, y, 0xFF8DEEFF);
	}

	private void drawRows(GuiGraphicsExtractor graphics, int left) {
		for (VisibleRow row : this.visibleRows) {
			Destination destination = row.destination();
			int textY = row.y() + 8;
			graphics.text(this.font, truncate(destination.name(), 145), left + NAME_X, textY, 0xFFFFFFFF);
			graphics.text(this.font, destination.coords(), left + COORDS_X, textY, 0xFFD6D6D6);
			graphics.text(this.font, truncate(destination.dimension(), 78), left + DIMENSION_X, textY, 0xFFD6D6D6);
			drawCost(graphics, destination, left + COST_X, row.y() + 4);
		}
	}

	private void drawCost(GuiGraphicsExtractor graphics, Destination destination, int x, int y) {
		if (destination.costAmount() <= 0) {
			graphics.text(this.font, destination.cost(), x, y + 4, 0xFF8CFF8C);
			return;
		}
		if (destination.usesXpLevels()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, EXPERIENCE_ORB_TEXTURE, x, y, 0.0F, 0.0F, 16, 16, 150, 150);
		} else {
			graphics.item(destination.costStack(), x, y);
		}
		graphics.text(this.font, String.valueOf(destination.costAmount()), x + 19, y + 5, 0xFFFFFFFF);
	}

	private String truncate(String value, int width) {
		if (this.font.width(value) <= width) {
			return value;
		}
		return this.font.plainSubstrByWidth(value, Math.max(1, width - this.font.width("..."))) + "...";
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
		if (!name.isBlank()) {
			data.putString("name", name);
		}
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

	private record Destination(String id, String name, String dimension, int x, int y, int z, String cost, String costType, String costItem, int costAmount) {
		String coords() {
			return x + " " + y + " " + z;
		}

		String searchText() {
			return (name + " " + dimension + " " + x + " " + y + " " + z).toLowerCase(Locale.ROOT);
		}

		ItemStack costStack() {
			Item item = BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(costItem)).orElse(Items.DIAMOND);
			return new ItemStack(item);
		}

		boolean usesXpLevels() {
			return "xp_levels".equals(costType);
		}
	}

	private record VisibleRow(Destination destination, int y) {
	}
}
