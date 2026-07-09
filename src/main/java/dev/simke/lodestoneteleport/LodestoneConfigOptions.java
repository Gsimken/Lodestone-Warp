package dev.simke.lodestoneteleport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class LodestoneConfigOptions {
	public enum Type {
		BOOLEAN,
		INTEGER,
		DECIMAL,
		TEXT
	}

	public static final String ALL = "all";
	public static final String COST = "cost";
	public static final String REGISTRATION = "registration";
	public static final String TELEPORT = "teleport";
	public static final String ADVANCED = "advanced";

	private static final List<Option> OPTIONS = List.of(
		text("cost_type", COST, "config.field.cost_type", "Cost type", "xp_levels", "Type of cost charged for each teleport.", "xp_levels or item.", config -> config.costType, (config, value) -> config.costType = value),
		text("cost_item", COST, "config.field.cost_item", "Cost item", "minecraft:diamond", "Item id charged for each teleport.", "Item identifier, for example minecraft:diamond.", config -> config.costItem, (config, value) -> config.costItem = value),
		integer("base_cost", COST, "config.field.base_cost", "Base cost", "1", "Minimum item cost for a teleport.", "Whole number, 0 or higher.", config -> config.baseCost, (config, value) -> config.baseCost = value),
		integer("blocks_per_extra_cost", COST, "config.field.blocks_per_extra_cost", "Blocks per extra cost", "1000", "Adds one cost level every configured blocks in the same dimension.", "Whole number, 0 disables distance scaling.", config -> config.blocksPerExtraCost, (config, value) -> config.blocksPerExtraCost = value),
		decimal("cross_dimension_multiplier", COST, "config.field.cross_dimension_multiplier", "Cross-dimension multiplier", "2.0", "Multiplies the calculated cost when teleporting between dimensions.", "Decimal number, 0 or higher.", config -> config.crossDimensionMultiplier, (config, value) -> config.crossDimensionMultiplier = value),
		integer("max_cost", COST, "config.field.max_cost", "Max cost", "64", "Caps the final teleport cost.", "Whole number, 0 means no cap.", config -> config.maxCost, (config, value) -> config.maxCost = value),

		bool("allow_cross_dimension", REGISTRATION, "config.field.allow_cross_dimension", "Allow cross-dimension", "true", "Allows teleports between Overworld, Nether, End, and other dimensions.", "true or false.", config -> config.allowCrossDimension, (config, value) -> config.allowCrossDimension = value),
		bool("allow_personal_lodestones", REGISTRATION, "config.field.allow_personal_lodestones", "Allow personal Lodestones", "true", "Allows private personal Lodestones owned by the placing player.", "true or false.", config -> config.allowPersonalLodestones, (config, value) -> config.allowPersonalLodestones = value),
		text("default_lodestone_visibility", REGISTRATION, "config.field.default_lodestone_visibility", "Default Lodestone visibility", "discoverable", "Visibility assigned to newly registered Lodestones when the player can create that type.", "private, discoverable, or global.", config -> config.defaultLodestoneVisibility, (config, value) -> config.defaultLodestoneVisibility = value),
		integer("max_lodestones_global", REGISTRATION, "config.field.max_lodestones_global", "Max Lodestones global", "0", "Maximum registered Lodestones for the whole server.", "Whole number, 0 means unlimited.", config -> config.maxLodestonesGlobal, (config, value) -> config.maxLodestonesGlobal = value),
		bool("sneak_place_only", REGISTRATION, "config.field.sneak_place_only", "Sneak-place only", "true", "Only registers newly placed Lodestones when the player is sneaking.", "true or false.", config -> config.registerPlacedLodestonesOnlyWhenSneaking, (config, value) -> config.registerPlacedLodestonesOnlyWhenSneaking = value),
		bool("auto_register_untracked", REGISTRATION, "config.field.auto_register_untracked", "Auto-register untracked", "false", "Registers old or unlinked Lodestones on normal right-click.", "true or false. Sneak-right-click can still register intentionally.", config -> config.autoRegisterUntrackedLodestones, (config, value) -> config.autoRegisterUntrackedLodestones = value),

		integer("teleport_source_range", TELEPORT, "config.field.teleport_source_range", "Source range", "8", "Player must stand near a registered Lodestone to teleport.", "Whole number, 0 disables the range check.", config -> config.teleportSourceRange, (config, value) -> config.teleportSourceRange = value),
		integer("teleport_cast_seconds", TELEPORT, "config.field.teleport_cast_seconds", "Cast seconds", "2", "Seconds the player must stand still before teleporting.", "Whole number, 0 disables casting.", config -> config.teleportCastSeconds, (config, value) -> config.teleportCastSeconds = value),
		decimal("teleport_cast_move_tolerance", TELEPORT, "config.field.teleport_cast_move_tolerance", "Cast move tolerance", "0.2", "Maximum movement allowed during the teleport cast.", "Decimal number, 0 or higher.", config -> config.teleportCastMoveTolerance, (config, value) -> config.teleportCastMoveTolerance = value),
		integer("teleport_cooldown_seconds", TELEPORT, "config.field.teleport_cooldown_seconds", "Cooldown seconds", "3", "Cooldown after a successful teleport.", "Whole number, 0 disables cooldown.", config -> config.teleportCooldownSeconds, (config, value) -> config.teleportCooldownSeconds = value),
		integer("max_dialog_destinations", TELEPORT, "config.field.max_dialog_destinations", "Vanilla dialog destinations", "24", "Maximum destination buttons shown in the vanilla Dialog UI.", "Whole number, 1 or higher.", config -> config.maxDialogDestinations, (config, value) -> config.maxDialogDestinations = value),
		integer("vanilla_dialog_destination_column_width", TELEPORT, "config.field.vanilla_dialog_destination_column_width", "Vanilla destination column width", "245", "Width of the destination column in the vanilla Dialog UI.", "Whole number, 80 to 500.", config -> config.vanillaDialogDestinationColumnWidth, (config, value) -> config.vanillaDialogDestinationColumnWidth = value),
		integer("vanilla_dialog_cost_column_width", TELEPORT, "config.field.vanilla_dialog_cost_column_width", "Vanilla cost column width", "70", "Width of the cost column in the vanilla Dialog UI.", "Whole number, 30 to 180.", config -> config.vanillaDialogCostColumnWidth, (config, value) -> config.vanillaDialogCostColumnWidth = value),
		integer("vanilla_dialog_edit_column_width", TELEPORT, "config.field.vanilla_dialog_edit_column_width", "Vanilla edit column width", "70", "Width of the edit/spacer column in the vanilla Dialog UI.", "Whole number, 20 to 120.", config -> config.vanillaDialogEditColumnWidth, (config, value) -> config.vanillaDialogEditColumnWidth = value),
		text("vanilla_dialog_column_order", TELEPORT, "config.field.vanilla_dialog_column_order", "Vanilla column order", "c,d,e", "Column order for the vanilla Dialog destination grid.", "Comma-separated c,d,e. c=cost, d=destination, e=edit.", config -> config.vanillaDialogColumnOrder, (config, value) -> config.vanillaDialogColumnOrder = value),
		bool("show_vanilla_dialog_header_navigation", TELEPORT, "config.field.show_vanilla_dialog_header_navigation", "Show vanilla header navigation", "true", "Shows clickable page arrows around the page label in the vanilla Dialog body.", "true or false.", config -> config.showVanillaDialogHeaderNavigation, (config, value) -> config.showVanillaDialogHeaderNavigation = value),
		bool("show_vanilla_dialog_button_navigation", TELEPORT, "config.field.show_vanilla_dialog_button_navigation", "Show vanilla button navigation", "true", "Shows page arrow buttons beside Search and Edit this warp in the vanilla Dialog grid.", "true or false.", config -> config.showVanillaDialogButtonNavigation, (config, value) -> config.showVanillaDialogButtonNavigation = value),
		bool("show_vanilla_dialog_destination_suffix", TELEPORT, "config.field.show_vanilla_dialog_destination_suffix", "Show vanilla destination suffix", "false", "Shows extra configured information after destination names in the vanilla Dialog UI.", "true or false.", config -> config.showVanillaDialogDestinationSuffix, (config, value) -> config.showVanillaDialogDestinationSuffix = value),
		text("vanilla_dialog_destination_suffix", TELEPORT, "config.field.vanilla_dialog_destination_suffix", "Vanilla destination suffix", "[{x}, {y}, {z}, {dimension}]", "Suffix pattern appended to destination names when enabled.", "Placeholders: {x}, {y}, {z}, {dimension}, {owner}.", config -> config.vanillaDialogDestinationSuffix, (config, value) -> config.vanillaDialogDestinationSuffix = value),

		bool("teleport_effects", ADVANCED, "config.field.teleport_effects", "Teleport effects", "true", "Enables sounds and particles around teleport actions.", "true or false.", config -> config.teleportEffects, (config, value) -> config.teleportEffects = value),
		text("vanilla_teleport_effect", ADVANCED, "config.field.vanilla_teleport_effect", "Vanilla effect", "end", "Effect preset used for players without the client mod.", "none, off, end, or lodestone.", config -> config.vanillaTeleportEffect, (config, value) -> config.vanillaTeleportEffect = value),
		text("mod_teleport_effect", ADVANCED, "config.field.mod_teleport_effect", "Mod effect", "lodestone", "Effect preset used for players with the client mod installed.", "none, off, end, or lodestone.", config -> config.modTeleportEffect, (config, value) -> config.modTeleportEffect = value),
		text("network_mode", ADVANCED, "config.field.network_mode", "Network mode", "discover", "Controls which Lodestones players can see and teleport to.", "all or discover.", config -> config.networkMode, (config, value) -> config.networkMode = value),
		bool("resolve_owner_names", ADVANCED, "config.field.resolve_owner_names", "Resolve owner names", "true", "Sends stored owner names to Lodestone Warps UIs.", "true or false. false hides owners as unknown.", config -> config.resolveOwnerNames, (config, value) -> config.resolveOwnerNames = value),
		text("player_permissions", ADVANCED, "config.field.player_permissions", "Player permissions", "lodestone_teleport.use, lodestone_teleport.create, lodestone_teleport.create.private, lodestone_teleport.create.discoverable, lodestone_teleport.own.rename, lodestone_teleport.own.remove, lodestone_teleport.own.destroy", "Default permissions used for every player when no permission manager answers.", "Comma-separated permission nodes. Bare names like use are accepted. Supports *, lodestone_teleport.*, lodestone.*, and lodestone_teleport.limit.10.", config -> LodestoneConfig.permissionListToString(config.playerPermissions), (config, value) -> config.playerPermissions = LodestoneConfig.parsePermissionList(value, List.of())),
		text("admin_permissions", ADVANCED, "config.field.admin_permissions", "Admin permissions", "lodestone_teleport.admin, lodestone_teleport.config, lodestone_teleport.global, lodestone_teleport.mode.all, lodestone_teleport.bypass_cost, lodestone_teleport.bypass_cast, lodestone_teleport.bypass_cooldown, lodestone_teleport.bypass_max_warps", "Default permissions used for gamemaster-level admins when no permission manager answers.", "Comma-separated permission nodes. Bare names like config are accepted. Supports *, lodestone_teleport.*, lodestone.*, and lodestone_teleport.limit.10.", config -> LodestoneConfig.permissionListToString(config.adminPermissions), (config, value) -> config.adminPermissions = LodestoneConfig.parsePermissionList(value, List.of())),
		text("command_name", ADVANCED, "config.field.command_name", "Command name", "warp", "Primary command registered by Lodestone Warps. Changes require a server restart.", "Letters, numbers, underscore, dash, or dot. Restart required.", config -> config.commandName, (config, value) -> config.commandName = value),
		text("fallback_command_name", ADVANCED, "config.field.fallback_command_name", "Fallback command", "lodestone_warp", "Fallback command kept available when the primary command conflicts. Changes require a server restart.", "Letters, numbers, underscore, dash, or dot. Restart required.", config -> config.fallbackCommandName, (config, value) -> config.fallbackCommandName = value),
		text("server_language", ADVANCED, "config.field.server_language", "Server language", "en_us", "Fallback language for server-generated text shown to vanilla clients.", "en_us or es_es.", config -> config.serverLanguage, (config, value) -> config.serverLanguage = value)
	);

	private LodestoneConfigOptions() {
	}

	public static List<Option> all() {
		return OPTIONS;
	}

	public static Optional<Option> get(String id) {
		String clean = cleanId(id);
		return OPTIONS.stream().filter(option -> option.id().equals(clean)).findFirst();
	}

	public static List<Option> filtered(String category, String query) {
		String cleanCategory = cleanCategory(category);
		String cleanQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		List<Option> result = new ArrayList<>();
		for (Option option : OPTIONS) {
			if (!ALL.equals(cleanCategory) && !option.category().equals(cleanCategory)) {
				continue;
			}
			if (!cleanQuery.isBlank() && !option.matches(cleanQuery)) {
				continue;
			}
			result.add(option);
		}
		return result;
	}

	public static String cleanCategory(String category) {
		if (category == null) {
			return ALL;
		}
		return switch (category.trim().toLowerCase(Locale.ROOT)) {
			case COST -> COST;
			case REGISTRATION -> REGISTRATION;
			case TELEPORT -> TELEPORT;
			case ADVANCED -> ADVANCED;
			default -> ALL;
		};
	}

	public static String cleanId(String id) {
		return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
	}

	private static Option text(String id, String category, String key, String fallback, String defaultValue, String description, String acceptedValues, Function<LodestoneConfig, String> getter, BiConsumer<LodestoneConfig, String> setter) {
		return new Option(id, category, Type.TEXT, key, fallback, defaultValue, description, acceptedValues, config -> getter.apply(config), (config, value) -> setter.accept(config, value.trim()));
	}

	private static Option integer(String id, String category, String key, String fallback, String defaultValue, String description, String acceptedValues, Function<LodestoneConfig, Integer> getter, BiConsumer<LodestoneConfig, Integer> setter) {
		return new Option(id, category, Type.INTEGER, key, fallback, defaultValue, description, acceptedValues, config -> String.valueOf(getter.apply(config)), (config, value) -> setter.accept(config, Integer.parseInt(value.trim())));
	}

	private static Option decimal(String id, String category, String key, String fallback, String defaultValue, String description, String acceptedValues, Function<LodestoneConfig, Double> getter, BiConsumer<LodestoneConfig, Double> setter) {
		return new Option(id, category, Type.DECIMAL, key, fallback, defaultValue, description, acceptedValues, config -> String.valueOf(getter.apply(config)), (config, value) -> setter.accept(config, Double.parseDouble(value.trim())));
	}

	private static Option bool(String id, String category, String key, String fallback, String defaultValue, String description, String acceptedValues, Function<LodestoneConfig, Boolean> getter, BiConsumer<LodestoneConfig, Boolean> setter) {
		return new Option(id, category, Type.BOOLEAN, key, fallback, defaultValue, description, acceptedValues, config -> String.valueOf(getter.apply(config)), (config, value) -> {
			String clean = value.trim().toLowerCase(Locale.ROOT);
			if (!List.of("true", "false", "yes", "no", "on", "off", "1", "0").contains(clean)) {
				throw new IllegalArgumentException("Expected true or false.");
			}
			setter.accept(config, switch (clean) {
				case "true", "yes", "on", "1" -> true;
				default -> false;
			});
		});
	}

	public record Option(
		String id,
		String category,
		Type type,
		String labelKey,
		String labelFallback,
		String defaultValue,
		String description,
		String acceptedValues,
		Function<LodestoneConfig, String> getter,
		BiConsumer<LodestoneConfig, String> setter
	) {
		public String currentValue() {
			return getter.apply(LodestoneConfig.get());
		}

		public boolean isDefault() {
			return defaultValue.equals(currentValue());
		}

		public void apply(String value) {
			setter.accept(LodestoneConfig.get(), value);
		}

		public boolean matches(String query) {
			return id.contains(query)
				|| labelFallback.toLowerCase(Locale.ROOT).contains(query)
				|| description.toLowerCase(Locale.ROOT).contains(query)
				|| currentValue().toLowerCase(Locale.ROOT).contains(query);
		}
	}
}
