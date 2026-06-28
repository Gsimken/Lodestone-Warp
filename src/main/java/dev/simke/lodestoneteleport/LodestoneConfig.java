package dev.simke.lodestoneteleport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public final class LodestoneConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = LodestoneTeleportMod.MOD_ID + ".json";
	private static LodestoneConfig INSTANCE = defaults();

	public String costItem = "minecraft:diamond";
	public int baseCost = 1;
	public int blocksPerExtraCost = 500;
	public double crossDimensionMultiplier = 2.0D;
	public int maxCost = 64;
	public boolean allowCrossDimension = true;
	public int maxDialogDestinations = 24;
	public int teleportSourceRange = 8;
	public int teleportCastSeconds = 2;
	public double teleportCastMoveTolerance = 0.2D;
	public int teleportCooldownSeconds = 3;
	public boolean teleportEffects = true;
	public String vanillaTeleportEffect = "end";
	public String modTeleportEffect = "lodestone";
	public boolean requirePermissions = false;
	public String commandName = "warp";
	public String fallbackCommandName = "lodestone_warp";
	public String serverLanguage = "en_us";

	private LodestoneConfig() {
	}

	public static LodestoneConfig get() {
		return INSTANCE;
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		if (Files.notExists(path)) {
			INSTANCE = defaults();
			save(path, INSTANCE);
			return;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			LodestoneConfig loaded = GSON.fromJson(reader, LodestoneConfig.class);
			INSTANCE = sanitize(loaded == null ? defaults() : loaded);
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

	private static LodestoneConfig sanitize(LodestoneConfig config) {
		if (config.costItem == null || Identifier.tryParse(config.costItem) == null) {
			config.costItem = "minecraft:diamond";
		}
		config.baseCost = Math.max(0, config.baseCost);
		config.blocksPerExtraCost = Math.max(0, config.blocksPerExtraCost);
		config.crossDimensionMultiplier = Math.max(0.0D, config.crossDimensionMultiplier);
		config.maxCost = Math.max(0, config.maxCost);
		config.maxDialogDestinations = Math.max(1, config.maxDialogDestinations);
		config.teleportSourceRange = Math.max(0, config.teleportSourceRange);
		config.teleportCastSeconds = Math.max(0, config.teleportCastSeconds);
		config.teleportCastMoveTolerance = Math.max(0.0D, config.teleportCastMoveTolerance);
		config.teleportCooldownSeconds = Math.max(0, config.teleportCooldownSeconds);
		config.vanillaTeleportEffect = cleanEffect(config.vanillaTeleportEffect, "end");
		config.modTeleportEffect = cleanEffect(config.modTeleportEffect, "lodestone");
		config.commandName = cleanCommandName(config.commandName, "warp");
		config.fallbackCommandName = cleanCommandName(config.fallbackCommandName, "lodestone_warp");
		config.serverLanguage = cleanLanguage(config.serverLanguage);
		return config;
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
