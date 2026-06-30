package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneConfig;
import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class LodestoneConfigScreen extends Screen {
	private static final int MARGIN = 28;
	private static final int FIELD_WIDTH = 260;
	private static final int ROW_HEIGHT = 24;

	private final Screen parent;
	private final Map<String, String> drafts = new HashMap<>();
	private final List<ConfigField> fields = new ArrayList<>();
	private Section section = Section.ALL;
	private String query = "";
	private int scrollOffset = 0;
	private Component status = Component.empty();
	private EditBox searchBox;

	public LodestoneConfigScreen(Screen parent) {
		super(LodestoneText.text("config.title", "Lodestone Warps Config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.fields.clear();
		List<ConfigField> filtered = filteredFields();
		int visibleRows = visibleRows();
		this.scrollOffset = Math.clamp(this.scrollOffset, 0, Math.max(0, filtered.size() - visibleRows));
		int left = MARGIN;
		int right = this.width - MARGIN;
		int contentWidth = right - left;
		int top = 28;

		int buttonWidth = Math.max(84, (contentWidth - 20) / Section.values().length);
		int x = left;
		for (Section sectionOption : Section.values()) {
			addRenderableWidget(Button.builder(sectionOption.title(), button -> {
				this.section = sectionOption;
				this.scrollOffset = 0;
				rebuildWidgets();
			}).bounds(x, top + 42, buttonWidth, 20).build());
			x += buttonWidth + 5;
		}

		this.searchBox = new EditBox(this.font, left, top + 72, contentWidth, 20, LodestoneText.text("input.search", "Search"));
		this.searchBox.setHint(LodestoneText.text("input.search", "Search"));
		this.searchBox.setMaxLength(64);
		this.searchBox.setValue(this.query);
		this.searchBox.setResponder(value -> {
			this.query = value;
			this.scrollOffset = 0;
			rebuildWidgets();
		});
		addRenderableWidget(this.searchBox);

		int y = top + 104;
		int fieldX = Math.max(left + 220, right - FIELD_WIDTH);
		for (ConfigField field : filtered.stream().skip(this.scrollOffset).limit(visibleRows).toList()) {
			if (field.booleanField()) {
				ConfigField visibleField = field;
				Button button = Button.builder(field.booleanTitle(), widget -> {
					visibleField.toggle(this.drafts);
					widget.setMessage(visibleField.booleanTitle());
				}).bounds(fieldX, y + 4, right - fieldX, 18).build();
				addRenderableWidget(button);
				this.fields.add(field);
			} else {
				EditBox box = new EditBox(this.font, fieldX, y + 4, right - fieldX, 18, field.label());
				box.setMaxLength(96);
				box.setValue(field.get());
				ConfigField visibleField = field.withBox(box);
				box.setResponder(value -> {
					this.drafts.put(field.key(), value);
					visibleField.setValue(value);
				});
				addRenderableWidget(box);
				this.fields.add(visibleField);
			}
			y += ROW_HEIGHT;
		}

		int actionsY = this.height - 30;
		addRenderableWidget(Button.builder(LodestoneText.text("config.button.save", "Save"), button -> save()).bounds(left, actionsY, 130, 20).build());
		addRenderableWidget(Button.builder(LodestoneText.text("config.button.reload", "Reload"), button -> {
			LodestoneConfig.load();
			this.drafts.clear();
			this.status = LodestoneText.text("config.status.reloaded", "Reloaded config from disk.");
			rebuildWidgets();
		}).bounds(left + 140, actionsY, 130, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.minecraft.setScreenAndShow(this.parent)).bounds(right - 130, actionsY, 130, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		this.extractTransparentBackground(graphics);
		int left = MARGIN;
		int right = this.width - MARGIN;
		int top = 28;
		graphics.fill(0, 0, this.width, this.height, 0xCC060606);
		graphics.fill(left - 12, top - 18, right + 12, this.height - 6, 0xDD101010);
		graphics.outline(left - 12, top - 18, right - left + 24, this.height - top + 12, 0xFF595959);
		graphics.centeredText(this.font, this.title, this.width / 2, top - 8, 0xFFFFFFFF);
		graphics.centeredText(this.font, LodestoneText.text("config.notice", "Client-side editor for the local config file. Remote servers use their own config."), this.width / 2, top + 12, 0xFFA8A8A8);

		int y = top + 105;
		for (ConfigField field : this.fields) {
			graphics.text(this.font, field.displayLabel(), left, y + 4, field.labelColor());
			if (mouseX >= left && mouseX <= right && mouseY >= y && mouseY < y + ROW_HEIGHT) {
				graphics.setComponentTooltipForNextFrame(this.font, field.tooltip(), mouseX, mouseY);
			}
			y += ROW_HEIGHT;
		}
		int totalRows = filteredFields().size();
		if (totalRows > visibleRows()) {
			graphics.text(this.font, LodestoneText.text("config.scroll", "%s-%s / %s", this.scrollOffset + 1, Math.min(totalRows, this.scrollOffset + visibleRows()), totalRows), right - 80, top + 94, 0xFFA8A8A8);
		}
		if (!this.status.getString().isBlank()) {
			graphics.centeredText(this.font, this.status, this.width / 2, this.height - 48, 0xFFFFD37A);
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int maxScroll = Math.max(0, filteredFields().size() - visibleRows());
		if (maxScroll <= 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		int next = Math.clamp(this.scrollOffset - (int) Math.signum(verticalAmount), 0, maxScroll);
		if (next == this.scrollOffset) {
			return true;
		}
		this.scrollOffset = next;
		rebuildWidgets();
		return true;
	}

	private void save() {
		LodestoneConfig config = LodestoneConfig.get();
		List<Component> errors = new ArrayList<>();
		ACTIVE_SCREEN = this;
		for (ConfigField field : Section.ALL.fields()) {
			try {
				field.apply(config);
			} catch (IllegalArgumentException exception) {
				errors.add(field.label());
			}
		}
		if (!errors.isEmpty()) {
			this.status = LodestoneText.text("config.status.invalid", "Some values are invalid.");
			return;
		}
		LodestoneConfig.save();
		this.drafts.clear();
		this.status = LodestoneText.text("config.status.saved", "Saved config.");
		rebuildWidgets();
	}

	private List<ConfigField> filteredFields() {
		ACTIVE_SCREEN = this;
		String cleanQuery = this.query.trim().toLowerCase(java.util.Locale.ROOT);
		return this.section.fields().stream()
			.filter(field -> cleanQuery.isBlank() || field.searchText().contains(cleanQuery))
			.toList();
	}

	private int visibleRows() {
		return Math.max(1, (this.height - 164) / ROW_HEIGHT);
	}

	private enum Section {
		ALL("config.page.all", "All") {
			@Override
			List<ConfigField> fields() {
				List<ConfigField> fields = new ArrayList<>();
				for (Section section : Section.values()) {
					if (section != ALL) {
						fields.addAll(section.fields());
					}
				}
				return fields;
			}
		},
		COST("config.page.cost", "Cost") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					text("cost_type", "config.field.cost_type", "Cost type", "xp_levels", "Type of cost charged for each teleport.", "xp_levels or item.", () -> LodestoneConfig.get().costType, (config, value) -> config.costType = value),
					text("cost_item", "config.field.cost_item", "Cost item", "minecraft:diamond", "Item id charged for each teleport.", "Item identifier, for example minecraft:diamond.", () -> LodestoneConfig.get().costItem, (config, value) -> config.costItem = value),
					integer("base_cost", "config.field.base_cost", "Base cost", "1", "Minimum item cost for a teleport.", "Whole number, 0 or higher.", () -> LodestoneConfig.get().baseCost, (config, value) -> config.baseCost = value),
					integer("blocks_per_extra_cost", "config.field.blocks_per_extra_cost", "Blocks per extra cost", "1000", "Adds one cost level every configured blocks in the same dimension.", "Whole number, 0 disables distance scaling.", () -> LodestoneConfig.get().blocksPerExtraCost, (config, value) -> config.blocksPerExtraCost = value),
					decimal("cross_dimension_multiplier", "config.field.cross_dimension_multiplier", "Cross-dimension multiplier", "2.0", "Multiplies the calculated cost when teleporting between dimensions.", "Decimal number, 0 or higher.", () -> LodestoneConfig.get().crossDimensionMultiplier, (config, value) -> config.crossDimensionMultiplier = value),
					integer("max_cost", "config.field.max_cost", "Max cost", "64", "Caps the final teleport cost.", "Whole number, 0 means no cap.", () -> LodestoneConfig.get().maxCost, (config, value) -> config.maxCost = value)
				);
			}
		},
		REGISTRATION("config.page.registration", "Registration") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					bool("allow_cross_dimension", "config.field.allow_cross_dimension", "Allow cross-dimension", "true", "Allows teleports between Overworld, Nether, End, and other dimensions.", "true or false.", () -> LodestoneConfig.get().allowCrossDimension, (config, value) -> config.allowCrossDimension = value),
					bool("allow_personal_lodestones", "config.field.allow_personal_lodestones", "Allow personal Lodestones", "true", "Allows private personal Lodestones owned by the placing player.", "true or false.", () -> LodestoneConfig.get().allowPersonalLodestones, (config, value) -> config.allowPersonalLodestones = value),
					text("default_lodestone_visibility", "config.field.default_lodestone_visibility", "Default Lodestone visibility", "discoverable", "Visibility assigned to newly registered Lodestones when the player can create that type.", "private, discoverable, or global.", () -> LodestoneConfig.get().defaultLodestoneVisibility, (config, value) -> config.defaultLodestoneVisibility = value),
					integer("max_lodestones_global", "config.field.max_lodestones_global", "Max Lodestones global", "0", "Maximum registered Lodestones for the whole server.", "Whole number, 0 means unlimited.", () -> LodestoneConfig.get().maxLodestonesGlobal, (config, value) -> config.maxLodestonesGlobal = value),
					integer("max_lodestones_per_player", "config.field.max_lodestones_per_player", "Max Lodestones per player", "0", "Maximum registered Lodestones owned by each player.", "Whole number, 0 means unlimited.", () -> LodestoneConfig.get().maxLodestonesPerPlayer, (config, value) -> config.maxLodestonesPerPlayer = value),
					bool("sneak_place_only", "config.field.sneak_place_only", "Sneak-place only", "true", "Only registers newly placed Lodestones when the player is sneaking.", "true or false.", () -> LodestoneConfig.get().registerPlacedLodestonesOnlyWhenSneaking, (config, value) -> config.registerPlacedLodestonesOnlyWhenSneaking = value),
					bool("auto_register_untracked", "config.field.auto_register_untracked", "Auto-register untracked", "false", "Registers old or unlinked Lodestones on normal right-click.", "true or false. Sneak-right-click can still register intentionally.", () -> LodestoneConfig.get().autoRegisterUntrackedLodestones, (config, value) -> config.autoRegisterUntrackedLodestones = value)
				);
			}
		},
		TELEPORT("config.page.teleport", "Teleport") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					integer("teleport_source_range", "config.field.teleport_source_range", "Source range", "8", "Player must stand near a registered Lodestone to teleport.", "Whole number, 0 disables the range check.", () -> LodestoneConfig.get().teleportSourceRange, (config, value) -> config.teleportSourceRange = value),
					integer("teleport_cast_seconds", "config.field.teleport_cast_seconds", "Cast seconds", "2", "Seconds the player must stand still before teleporting.", "Whole number, 0 disables casting.", () -> LodestoneConfig.get().teleportCastSeconds, (config, value) -> config.teleportCastSeconds = value),
					decimal("teleport_cast_move_tolerance", "config.field.teleport_cast_move_tolerance", "Cast move tolerance", "0.2", "Maximum movement allowed during the teleport cast.", "Decimal number, 0 or higher.", () -> LodestoneConfig.get().teleportCastMoveTolerance, (config, value) -> config.teleportCastMoveTolerance = value),
					integer("teleport_cooldown_seconds", "config.field.teleport_cooldown_seconds", "Cooldown seconds", "3", "Cooldown after a successful teleport.", "Whole number, 0 disables cooldown.", () -> LodestoneConfig.get().teleportCooldownSeconds, (config, value) -> config.teleportCooldownSeconds = value),
					integer("max_dialog_destinations", "config.field.max_dialog_destinations", "Vanilla dialog destinations", "24", "Maximum destination buttons shown in the vanilla Dialog UI.", "Whole number, 1 or higher.", () -> LodestoneConfig.get().maxDialogDestinations, (config, value) -> config.maxDialogDestinations = value)
				);
			}
		},
		ADVANCED("config.page.advanced", "Advanced") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					bool("teleport_effects", "config.field.teleport_effects", "Teleport effects", "true", "Enables sounds and particles around teleport actions.", "true or false.", () -> LodestoneConfig.get().teleportEffects, (config, value) -> config.teleportEffects = value),
					text("vanilla_teleport_effect", "config.field.vanilla_teleport_effect", "Vanilla effect", "end", "Effect preset used for players without the client mod.", "none, off, end, or lodestone.", () -> LodestoneConfig.get().vanillaTeleportEffect, (config, value) -> config.vanillaTeleportEffect = value),
					text("mod_teleport_effect", "config.field.mod_teleport_effect", "Mod effect", "lodestone", "Effect preset used for players with the client mod installed.", "none, off, end, or lodestone.", () -> LodestoneConfig.get().modTeleportEffect, (config, value) -> config.modTeleportEffect = value),
					text("network_mode", "config.field.network_mode", "Network mode", "discover", "Controls which Lodestones players can see and teleport to.", "all or discover.", () -> LodestoneConfig.get().networkMode, (config, value) -> config.networkMode = value),
					text("player_permissions", "config.field.player_permissions", "Player permissions", "lodestone_teleport.use, lodestone_teleport.create, lodestone_teleport.rename, lodestone_teleport.remove, lodestone_teleport.mode.discover", "Default permissions used for every player when no permission manager answers.", "Comma-separated permission nodes. Bare names like use are accepted. Supports * and lodestone_teleport.*.", () -> LodestoneConfig.permissionListToString(LodestoneConfig.get().playerPermissions), (config, value) -> config.playerPermissions = LodestoneConfig.parsePermissionList(value, List.of())),
					text("admin_permissions", "config.field.admin_permissions", "Admin permissions", "lodestone_teleport.admin, lodestone_teleport.config, lodestone_teleport.global, lodestone_teleport.mode.all, lodestone_teleport.bypass_cost, lodestone_teleport.bypass_cast, lodestone_teleport.bypass_cooldown, lodestone_teleport.bypass_max_warps", "Default permissions used for gamemaster-level admins when no permission manager answers.", "Comma-separated permission nodes. Bare names like config are accepted. Supports * and lodestone_teleport.*.", () -> LodestoneConfig.permissionListToString(LodestoneConfig.get().adminPermissions), (config, value) -> config.adminPermissions = LodestoneConfig.parsePermissionList(value, List.of())),
					text("command_name", "config.field.command_name", "Command name", "warp", "Primary command registered by Lodestone Warps.", "Letters, numbers, underscore, dash, or dot.", () -> LodestoneConfig.get().commandName, (config, value) -> config.commandName = value),
					text("fallback_command_name", "config.field.fallback_command_name", "Fallback command", "lodestone_warp", "Fallback command kept available when the primary command conflicts.", "Letters, numbers, underscore, dash, or dot.", () -> LodestoneConfig.get().fallbackCommandName, (config, value) -> config.fallbackCommandName = value),
					text("server_language", "config.field.server_language", "Server language", "en_us", "Fallback language for server-generated text shown to vanilla clients.", "en_us or es_es.", () -> LodestoneConfig.get().serverLanguage, (config, value) -> config.serverLanguage = value)
				);
			}
		};

		private final String key;
		private final String fallback;

		Section(String key, String fallback) {
			this.key = key;
			this.fallback = fallback;
		}

		Component title() {
			return LodestoneText.text(this.key, this.fallback);
		}

		abstract List<ConfigField> fields();
	}

	private static final class ConfigField {
		private final String id;
		private final Component label;
		private String value;
		private final String savedValue;
		private final String defaultValue;
		private final Component description;
		private final Component acceptedValues;
		private final String searchText;
		private final BiConsumer<LodestoneConfig, String> setter;
		private final EditBox box;
		private final boolean booleanField;

		private ConfigField(String id, Component label, String value, String savedValue, String defaultValue, Component description, Component acceptedValues, String searchText, BiConsumer<LodestoneConfig, String> setter, EditBox box, boolean booleanField) {
			this.id = id;
			this.label = label;
			this.value = value;
			this.savedValue = savedValue;
			this.defaultValue = defaultValue;
			this.description = description;
			this.acceptedValues = acceptedValues;
			this.searchText = searchText;
			this.setter = setter;
			this.box = box;
			this.booleanField = booleanField;
		}

		static ConfigField with(String id, String key, String fallback, String value, String defaultValue, String description, String acceptedValues, BiConsumer<LodestoneConfig, String> setter) {
			return new ConfigField(
				id,
				LodestoneText.text(key, fallback),
				LodestoneConfigScreen.currentValue(id, value),
				value,
				defaultValue,
				Component.literal(description),
				Component.literal(acceptedValues),
				(id + " " + key + " " + fallback + " " + description + " " + acceptedValues).toLowerCase(java.util.Locale.ROOT),
				setter,
				null,
				false
			);
		}

		static ConfigField bool(String id, String key, String fallback, String value, String defaultValue, String description, String acceptedValues, BiConsumer<LodestoneConfig, String> setter) {
			return new ConfigField(
				id,
				LodestoneText.text(key, fallback),
				LodestoneConfigScreen.currentValue(id, value),
				value,
				defaultValue,
				Component.literal(description),
				Component.literal(acceptedValues),
				(id + " " + key + " " + fallback + " " + description + " " + acceptedValues).toLowerCase(java.util.Locale.ROOT),
				setter,
				null,
				true
			);
		}

		ConfigField withBox(EditBox box) {
			return new ConfigField(this.id, this.label, box.getValue(), this.savedValue, this.defaultValue, this.description, this.acceptedValues, this.searchText, this.setter, box, this.booleanField);
		}

		String get() {
			return this.value;
		}

		String key() {
			return this.id;
		}

		void setValue(String value) {
			this.value = value;
		}

		Component label() {
			return this.label;
		}

		MutableComponent displayLabel() {
			if (!dirty()) {
				return this.label.copy();
			}
			return Component.literal("* ")
				.append(this.label)
				.withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC);
		}

		List<Component> tooltip() {
			return List.of(
				this.label.copy().withStyle(ChatFormatting.AQUA),
				this.description,
				this.acceptedValues.copy().withStyle(ChatFormatting.GRAY),
				LodestoneText.text("config.default", "Default: %s", this.defaultValue).withStyle(this.nonDefault() ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY),
				LodestoneText.text("config.current", "Current: %s", this.value).withStyle(this.nonDefault() ? ChatFormatting.YELLOW : ChatFormatting.GRAY)
			);
		}

		int labelColor() {
			if (dirty() || nonDefault()) {
				return 0xFFFFD37A;
			}
			return 0xFF8DEEFF;
		}

		String searchText() {
			return this.searchText;
		}

		boolean booleanField() {
			return this.booleanField;
		}

		Component booleanTitle() {
			return LodestoneText.text(Boolean.parseBoolean(this.value) ? "config.switch.on" : "config.switch.off", Boolean.parseBoolean(this.value) ? "ON" : "OFF");
		}

		void toggle(Map<String, String> drafts) {
			this.value = String.valueOf(!Boolean.parseBoolean(this.value));
			drafts.put(this.id, this.value);
		}

		void apply(LodestoneConfig config) {
			this.setter.accept(config, this.value);
		}

		boolean dirty() {
			return !this.value.equals(this.savedValue);
		}

		boolean nonDefault() {
			return !this.value.equals(this.defaultValue);
		}
	}

	private static String currentValue(String id, String fallback) {
		return ACTIVE_SCREEN == null ? fallback : ACTIVE_SCREEN.drafts.getOrDefault(id, fallback);
	}

	private static String savedValue(ConfigField field) {
		return field.box == null ? field.get() : field.box.getValue();
	}

	private static LodestoneConfigScreen ACTIVE_SCREEN;

	private static ConfigField text(String id, String key, String fallback, String defaultValue, String description, String acceptedValues, Supplier<String> getter, BiConsumer<LodestoneConfig, String> setter) {
		return ConfigField.with(id, key, fallback, getter.get(), defaultValue, description, acceptedValues, (config, value) -> setter.accept(config, value.trim()));
	}

	private static ConfigField integer(String id, String key, String fallback, String defaultValue, String description, String acceptedValues, Supplier<Integer> getter, BiConsumer<LodestoneConfig, Integer> setter) {
		return ConfigField.with(id, key, fallback, String.valueOf(getter.get()), defaultValue, description, acceptedValues, (config, value) -> setter.accept(config, Integer.parseInt(value.trim())));
	}

	private static ConfigField decimal(String id, String key, String fallback, String defaultValue, String description, String acceptedValues, Supplier<Double> getter, BiConsumer<LodestoneConfig, Double> setter) {
		return ConfigField.with(id, key, fallback, String.valueOf(getter.get()), defaultValue, description, acceptedValues, (config, value) -> setter.accept(config, Double.parseDouble(value.trim())));
	}

	private static ConfigField bool(String id, String key, String fallback, String defaultValue, String description, String acceptedValues, Supplier<Boolean> getter, BiConsumer<LodestoneConfig, Boolean> setter) {
		return ConfigField.bool(id, key, fallback, String.valueOf(getter.get()), defaultValue, description, acceptedValues, (config, value) -> {
			String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
			if (!clean.equals("true") && !clean.equals("false")) {
				throw new IllegalArgumentException("Boolean expected");
			}
			setter.accept(config, Boolean.parseBoolean(clean));
		});
	}
}
