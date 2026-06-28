package dev.simke.lodestoneteleport.client;

import dev.simke.lodestoneteleport.LodestoneConfig;
import dev.simke.lodestoneteleport.LodestoneText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class LodestoneConfigScreen extends Screen {
	private static final int MARGIN = 28;
	private static final int FIELD_WIDTH = 260;
	private static final int ROW_HEIGHT = 24;

	private final Screen parent;
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
					visibleField.toggle();
					widget.setMessage(visibleField.booleanTitle());
				}).bounds(fieldX, y, right - fieldX, 18).build();
				addRenderableWidget(button);
				this.fields.add(field);
			} else {
				EditBox box = new EditBox(this.font, fieldX, y, right - fieldX, 18, field.label());
				box.setMaxLength(96);
				box.setValue(field.get());
				addRenderableWidget(box);
				this.fields.add(field.withBox(box));
			}
			y += ROW_HEIGHT;
		}

		int actionsY = this.height - 30;
		addRenderableWidget(Button.builder(LodestoneText.text("config.button.save", "Save"), button -> save()).bounds(left, actionsY, 130, 20).build());
		addRenderableWidget(Button.builder(LodestoneText.text("config.button.reload", "Reload"), button -> {
			LodestoneConfig.load();
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

		int y = top + 108;
		for (ConfigField field : this.fields) {
			graphics.text(this.font, field.label(), left, y + 4, 0xFF8DEEFF);
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
		for (ConfigField field : this.fields) {
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
		this.status = LodestoneText.text("config.status.saved", "Saved config.");
		rebuildWidgets();
	}

	private List<ConfigField> filteredFields() {
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
					text("config.field.cost_item", "Cost item", () -> LodestoneConfig.get().costItem, (config, value) -> config.costItem = value),
					integer("config.field.base_cost", "Base cost", () -> LodestoneConfig.get().baseCost, (config, value) -> config.baseCost = value),
					integer("config.field.blocks_per_extra_cost", "Blocks per extra cost", () -> LodestoneConfig.get().blocksPerExtraCost, (config, value) -> config.blocksPerExtraCost = value),
					decimal("config.field.cross_dimension_multiplier", "Cross-dimension multiplier", () -> LodestoneConfig.get().crossDimensionMultiplier, (config, value) -> config.crossDimensionMultiplier = value),
					integer("config.field.max_cost", "Max cost", () -> LodestoneConfig.get().maxCost, (config, value) -> config.maxCost = value)
				);
			}
		},
		REGISTRATION("config.page.registration", "Registration") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					bool("config.field.allow_cross_dimension", "Allow cross-dimension", () -> LodestoneConfig.get().allowCrossDimension, (config, value) -> config.allowCrossDimension = value),
					integer("config.field.max_lodestones_global", "Max Lodestones global", () -> LodestoneConfig.get().maxLodestonesGlobal, (config, value) -> config.maxLodestonesGlobal = value),
					integer("config.field.max_lodestones_per_player", "Max Lodestones per player", () -> LodestoneConfig.get().maxLodestonesPerPlayer, (config, value) -> config.maxLodestonesPerPlayer = value),
					bool("config.field.sneak_place_only", "Sneak-place only", () -> LodestoneConfig.get().registerPlacedLodestonesOnlyWhenSneaking, (config, value) -> config.registerPlacedLodestonesOnlyWhenSneaking = value),
					bool("config.field.auto_register_untracked", "Auto-register untracked", () -> LodestoneConfig.get().autoRegisterUntrackedLodestones, (config, value) -> config.autoRegisterUntrackedLodestones = value)
				);
			}
		},
		TELEPORT("config.page.teleport", "Teleport") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					integer("config.field.teleport_source_range", "Source range", () -> LodestoneConfig.get().teleportSourceRange, (config, value) -> config.teleportSourceRange = value),
					integer("config.field.teleport_cast_seconds", "Cast seconds", () -> LodestoneConfig.get().teleportCastSeconds, (config, value) -> config.teleportCastSeconds = value),
					decimal("config.field.teleport_cast_move_tolerance", "Cast move tolerance", () -> LodestoneConfig.get().teleportCastMoveTolerance, (config, value) -> config.teleportCastMoveTolerance = value),
					integer("config.field.teleport_cooldown_seconds", "Cooldown seconds", () -> LodestoneConfig.get().teleportCooldownSeconds, (config, value) -> config.teleportCooldownSeconds = value),
					integer("config.field.max_dialog_destinations", "Vanilla dialog destinations", () -> LodestoneConfig.get().maxDialogDestinations, (config, value) -> config.maxDialogDestinations = value)
				);
			}
		},
		ADVANCED("config.page.advanced", "Advanced") {
			@Override
			List<ConfigField> fields() {
				return List.of(
					bool("config.field.teleport_effects", "Teleport effects", () -> LodestoneConfig.get().teleportEffects, (config, value) -> config.teleportEffects = value),
					text("config.field.vanilla_teleport_effect", "Vanilla effect", () -> LodestoneConfig.get().vanillaTeleportEffect, (config, value) -> config.vanillaTeleportEffect = value),
					text("config.field.mod_teleport_effect", "Mod effect", () -> LodestoneConfig.get().modTeleportEffect, (config, value) -> config.modTeleportEffect = value),
					bool("config.field.require_permissions", "Require permissions", () -> LodestoneConfig.get().requirePermissions, (config, value) -> config.requirePermissions = value),
					text("config.field.command_name", "Command name", () -> LodestoneConfig.get().commandName, (config, value) -> config.commandName = value),
					text("config.field.fallback_command_name", "Fallback command", () -> LodestoneConfig.get().fallbackCommandName, (config, value) -> config.fallbackCommandName = value),
					text("config.field.server_language", "Server language", () -> LodestoneConfig.get().serverLanguage, (config, value) -> config.serverLanguage = value)
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
		private final Component label;
		private String value;
		private final String searchText;
		private final BiConsumer<LodestoneConfig, String> setter;
		private final EditBox box;
		private final boolean booleanField;

		private ConfigField(Component label, String value, String searchText, BiConsumer<LodestoneConfig, String> setter, EditBox box, boolean booleanField) {
			this.label = label;
			this.value = value;
			this.searchText = searchText;
			this.setter = setter;
			this.box = box;
			this.booleanField = booleanField;
		}

		static ConfigField with(String key, String fallback, String value, BiConsumer<LodestoneConfig, String> setter) {
			return new ConfigField(LodestoneText.text(key, fallback), value, (key + " " + fallback).toLowerCase(java.util.Locale.ROOT), setter, null, false);
		}

		static ConfigField bool(String key, String fallback, String value, BiConsumer<LodestoneConfig, String> setter) {
			return new ConfigField(LodestoneText.text(key, fallback), value, (key + " " + fallback).toLowerCase(java.util.Locale.ROOT), setter, null, true);
		}

		ConfigField withBox(EditBox box) {
			return new ConfigField(this.label, this.value, this.searchText, this.setter, box, this.booleanField);
		}

		String get() {
			return this.value;
		}

		Component label() {
			return this.label;
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

		void toggle() {
			this.value = String.valueOf(!Boolean.parseBoolean(this.value));
			this.setter.accept(LodestoneConfig.get(), this.value);
		}

		void apply(LodestoneConfig config) {
			this.setter.accept(config, this.booleanField ? this.value : this.box.getValue());
		}
	}

	private static ConfigField text(String key, String fallback, Supplier<String> getter, BiConsumer<LodestoneConfig, String> setter) {
		return ConfigField.with(key, fallback, getter.get(), (config, value) -> setter.accept(config, value.trim()));
	}

	private static ConfigField integer(String key, String fallback, Supplier<Integer> getter, BiConsumer<LodestoneConfig, Integer> setter) {
		return ConfigField.with(key, fallback, String.valueOf(getter.get()), (config, value) -> setter.accept(config, Integer.parseInt(value.trim())));
	}

	private static ConfigField decimal(String key, String fallback, Supplier<Double> getter, BiConsumer<LodestoneConfig, Double> setter) {
		return ConfigField.with(key, fallback, String.valueOf(getter.get()), (config, value) -> setter.accept(config, Double.parseDouble(value.trim())));
	}

	private static ConfigField bool(String key, String fallback, Supplier<Boolean> getter, BiConsumer<LodestoneConfig, Boolean> setter) {
		return ConfigField.bool(key, fallback, String.valueOf(getter.get()), (config, value) -> {
			String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
			if (!clean.equals("true") && !clean.equals("false")) {
				throw new IllegalArgumentException("Boolean expected");
			}
			setter.accept(config, Boolean.parseBoolean(clean));
		});
	}
}
