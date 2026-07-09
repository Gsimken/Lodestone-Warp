package dev.simke.lodestoneteleport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LodestoneConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = LodestoneTeleportMod.MOD_ID + ".json";
	private static LodestoneConfig INSTANCE = defaults();

	public String costItem = "minecraft:diamond";
	public String costType = "xp_levels";
	public int baseCost = 1;
	public int blocksPerExtraCost = 1000;
	public double crossDimensionMultiplier = 2.0D;
	public int maxCost = 64;
	public boolean allowCrossDimension = true;
	public boolean allowPersonalLodestones = true;
	public String defaultLodestoneVisibility = "discoverable";
	public int maxLodestonesGlobal = 0;
	public boolean registerPlacedLodestonesOnlyWhenSneaking = true;
	public boolean autoRegisterUntrackedLodestones = false;
	public int maxDialogDestinations = 24;
	public int vanillaDialogDestinationColumnWidth = 245;
	public int vanillaDialogCostColumnWidth = 70;
	public int vanillaDialogEditColumnWidth = 70;
	public String vanillaDialogColumnOrder = "c,d,e";
	public boolean showVanillaDialogHeaderNavigation = true;
	public boolean showVanillaDialogButtonNavigation = true;
	public boolean showVanillaDialogDestinationSuffix = false;
	public String vanillaDialogDestinationSuffix = "[{x}, {y}, {z}, {dimension}]";
	public int teleportSourceRange = 8;
	public int teleportCastSeconds = 2;
	public double teleportCastMoveTolerance = 0.2D;
	public int teleportCooldownSeconds = 3;
	public boolean teleportEffects = true;
	public String vanillaTeleportEffect = "end";
	public String modTeleportEffect = "lodestone";
	public String networkMode = "discover";
	public boolean resolveOwnerNames = true;
	public List<String> playerPermissions = List.of(
		"lodestone_teleport.use",
		"lodestone_teleport.create",
		"lodestone_teleport.create.private",
		"lodestone_teleport.create.discoverable",
		"lodestone_teleport.own.rename",
		"lodestone_teleport.own.remove",
		"lodestone_teleport.own.destroy",
		"lodestone_teleport.own.visibility.private",
		"lodestone_teleport.own.visibility.discoverable"
	);
	public List<String> adminPermissions = List.of(
		"lodestone_teleport.admin",
		"lodestone_teleport.config",
		"lodestone_teleport.global",
		"lodestone_teleport.rename",
		"lodestone_teleport.remove",
		"lodestone_teleport.mode.all",
		"lodestone_teleport.create.global",
		"lodestone_teleport.own.visibility.global",
		"lodestone_teleport.bypass_cost",
		"lodestone_teleport.bypass_cast",
		"lodestone_teleport.bypass_cooldown",
		"lodestone_teleport.bypass_max_warps"
	);
	public String commandName = "warp";
	public String fallbackCommandName = "lodestone_warp";
	public String serverLanguage = "en_us";

	private LodestoneConfig() {
	}

	public static LodestoneConfig get() {
		return INSTANCE;
	}

	public static void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		INSTANCE = sanitize(INSTANCE);
		save(path, INSTANCE);
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		if (Files.notExists(path)) {
			INSTANCE = defaults();
			save(path, INSTANCE);
			return;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			JsonObject merged = mergeDefaults(JsonParser.parseReader(reader));
			LodestoneConfig loaded = GSON.fromJson(merged, LodestoneConfig.class);
			INSTANCE = sanitize(loaded == null ? defaults() : loaded);
			save(path, INSTANCE);
		} catch (IOException | JsonSyntaxException exception) {
			LodestoneTeleportMod.LOGGER.warn("Failed to load {}, using defaults.", FILE_NAME, exception);
			INSTANCE = defaults();
		}
	}

	public Item costItem() {
		Identifier id = Identifier.tryParse(costItem);
		if (id == null) {
			return Items.DIAMOND;
		}
		return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.DIAMOND);
	}

	private static LodestoneConfig defaults() {
		return new LodestoneConfig();
	}

	private static JsonObject mergeDefaults(JsonElement loaded) {
		JsonObject merged = GSON.toJsonTree(defaults()).getAsJsonObject();
		if (loaded == null || !loaded.isJsonObject()) {
			return merged;
		}
		JsonObject loadedObject = loaded.getAsJsonObject();
		boolean legacyCostConfig = isLegacyDefaultCostConfig(loadedObject);
		for (var entry : loadedObject.entrySet()) {
			merged.add(entry.getKey(), entry.getValue());
		}
		if (legacyCostConfig) {
			merged.addProperty("blocksPerExtraCost", 1000);
		}
		return merged;
	}

	public LodestoneVisibility defaultVisibility() {
		LodestoneVisibility visibility = LodestoneVisibility.from(defaultLodestoneVisibility, LodestoneVisibility.DISCOVERABLE);
		return !allowPersonalLodestones && visibility == LodestoneVisibility.PRIVATE ? LodestoneVisibility.DISCOVERABLE : visibility;
	}

	private static boolean isLegacyDefaultCostConfig(JsonObject loadedObject) {
		if (loadedObject.has("costType") || !loadedObject.has("blocksPerExtraCost")) {
			return false;
		}
		try {
			return loadedObject.get("blocksPerExtraCost").getAsInt() == 500;
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static LodestoneConfig sanitize(LodestoneConfig config) {
		if (config.costItem == null || Identifier.tryParse(config.costItem) == null) {
			config.costItem = "minecraft:diamond";
		}
		config.costType = cleanCostType(config.costType);
		config.baseCost = Math.max(0, config.baseCost);
		config.blocksPerExtraCost = Math.max(0, config.blocksPerExtraCost);
		config.crossDimensionMultiplier = Math.max(0.0D, config.crossDimensionMultiplier);
		config.maxCost = Math.max(0, config.maxCost);
		config.defaultLodestoneVisibility = cleanVisibility(config.defaultLodestoneVisibility);
		config.maxLodestonesGlobal = Math.max(0, config.maxLodestonesGlobal);
		config.maxDialogDestinations = Math.max(1, config.maxDialogDestinations);
		config.vanillaDialogDestinationColumnWidth = clamp(config.vanillaDialogDestinationColumnWidth, 80, 500);
		config.vanillaDialogCostColumnWidth = clamp(config.vanillaDialogCostColumnWidth, 30, 180);
		config.vanillaDialogEditColumnWidth = clamp(config.vanillaDialogEditColumnWidth, 20, 120);
		config.vanillaDialogColumnOrder = cleanVanillaDialogColumnOrder(config.vanillaDialogColumnOrder);
		if (config.vanillaDialogDestinationSuffix == null || config.vanillaDialogDestinationSuffix.length() > 96) {
			config.vanillaDialogDestinationSuffix = "[{x}, {y}, {z}, {dimension}]";
		}
		config.teleportSourceRange = Math.max(0, config.teleportSourceRange);
		config.teleportCastSeconds = Math.max(0, config.teleportCastSeconds);
		config.teleportCastMoveTolerance = Math.max(0.0D, config.teleportCastMoveTolerance);
		config.teleportCooldownSeconds = Math.max(0, config.teleportCooldownSeconds);
		config.vanillaTeleportEffect = cleanEffect(config.vanillaTeleportEffect, "end");
		config.modTeleportEffect = cleanEffect(config.modTeleportEffect, "lodestone");
		config.networkMode = cleanNetworkMode(config.networkMode);
		config.playerPermissions = cleanPermissionList(config.playerPermissions, defaults().playerPermissions);
		config.adminPermissions = cleanPermissionList(config.adminPermissions, defaults().adminPermissions);
		addPermissionMigrationDefaults(config);
		config.commandName = cleanCommandName(config.commandName, "warp");
		config.fallbackCommandName = cleanCommandName(config.fallbackCommandName, "lodestone_warp");
		config.serverLanguage = cleanLanguage(config.serverLanguage);
		return config;
	}

	public static List<String> parsePermissionList(String value, List<String> fallback) {
		if (value == null) {
			return new ArrayList<>(fallback);
		}
		return cleanPermissionList(List.of(value.split(",")), fallback);
	}

	public static String permissionListToString(List<String> permissions) {
		return String.join(", ", cleanPermissionList(permissions, List.of()));
	}

	private static List<String> cleanPermissionList(List<String> permissions, List<String> fallback) {
		if (permissions == null) {
			return new ArrayList<>(fallback);
		}
		List<String> cleanPermissions = new ArrayList<>();
		for (String permission : permissions) {
			String clean = cleanPermission(permission);
			if (!clean.isBlank() && !cleanPermissions.contains(clean)) {
				cleanPermissions.add(clean);
			}
		}
		return cleanPermissions;
	}

	private static String cleanPermission(String permission) {
		if (permission == null) {
			return "";
		}
		String clean = permission.trim().toLowerCase(Locale.ROOT);
		if (clean.isBlank()) {
			return "";
		}
		if ("*".equals(clean) || clean.endsWith(".*")) {
			return clean;
		}
		if (clean.startsWith("limit.") || clean.startsWith("mode.") || clean.startsWith("own.") || clean.startsWith("create.") || clean.startsWith("bypass_")) {
			return LodestoneTeleportMod.MOD_ID + "." + clean;
		}
		if (!clean.contains(".")) {
			return LodestoneTeleportMod.MOD_ID + "." + clean;
		}
		return clean;
	}

	private static void addPermissionMigrationDefaults(LodestoneConfig config) {
		if (config.playerPermissions.contains(LodestoneTeleportMod.MOD_ID + ".create")
			&& config.playerPermissions.stream().noneMatch(permission -> permission.startsWith(LodestoneTeleportMod.MOD_ID + ".create."))) {
			addPermission(config.playerPermissions, "create.private");
			addPermission(config.playerPermissions, "create.discoverable");
			addPermission(config.playerPermissions, "own.visibility.private");
			addPermission(config.playerPermissions, "own.visibility.discoverable");
		}
		if (config.playerPermissions.contains(LodestoneTeleportMod.MOD_ID + ".rename")) {
			addPermission(config.playerPermissions, "own.rename");
		}
		if (config.playerPermissions.contains(LodestoneTeleportMod.MOD_ID + ".remove")) {
			addPermission(config.playerPermissions, "own.remove");
			addPermission(config.playerPermissions, "own.destroy");
		}
		if (config.adminPermissions.contains(LodestoneTeleportMod.MOD_ID + ".global")) {
			addPermission(config.adminPermissions, "create.global");
			addPermission(config.adminPermissions, "own.visibility.global");
		}
	}

	private static void addPermission(List<String> permissions, String node) {
		String fullNode = LodestoneTeleportMod.MOD_ID + "." + node;
		if (!permissions.contains(fullNode)) {
			permissions.add(fullNode);
		}
	}

	private static String cleanCommandName(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
		if (!clean.matches("[a-z0-9_\\-.]+")) {
			return fallback;
		}
		return clean;
	}

	private static String cleanLanguage(String value) {
		if (value == null) {
			return "en_us";
		}
		String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
		return switch (clean) {
			case "es", "es_es", "spanish" -> "es_es";
			default -> "en_us";
		};
	}

	private static String cleanEffect(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
		return switch (clean) {
			case "none", "off", "end", "lodestone" -> clean;
			default -> fallback;
		};
	}

	private static String cleanCostType(String value) {
		if (value == null) {
			return "xp_levels";
		}
		String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
		return switch (clean) {
			case "xp", "level", "levels", "xp_level", "xp_levels" -> "xp_levels";
			case "item", "items" -> "item";
			default -> "xp_levels";
		};
	}

	private static String cleanNetworkMode(String value) {
		if (value == null) {
			return "discover";
		}
		String clean = value.trim().toLowerCase(java.util.Locale.ROOT);
		return switch (clean) {
			case "all", "discover" -> clean;
			default -> "all";
		};
	}

	private static String cleanVisibility(String value) {
		return LodestoneVisibility.from(value, LodestoneVisibility.DISCOVERABLE).id();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static String cleanVanillaDialogColumnOrder(String value) {
		if (value == null) {
			return "c,d,e";
		}
		String clean = value.trim().toLowerCase(Locale.ROOT).replace(" ", "");
		String[] parts = clean.split(",");
		if (parts.length != 3) {
			return "c,d,e";
		}
		boolean hasCost = false;
		boolean hasDestination = false;
		boolean hasEdit = false;
		for (String part : parts) {
			switch (part) {
				case "c" -> hasCost = !hasCost;
				case "d" -> hasDestination = !hasDestination;
				case "e" -> hasEdit = !hasEdit;
				default -> {
					return "c,d,e";
				}
			}
		}
		return hasCost && hasDestination && hasEdit ? String.join(",", parts) : "c,d,e";
	}

	private static void save(Path path, LodestoneConfig config) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			LodestoneTeleportMod.LOGGER.warn("Failed to write default {}.", FILE_NAME, exception);
		}
	}
}
