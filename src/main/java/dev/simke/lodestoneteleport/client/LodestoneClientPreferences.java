package dev.simke.lodestoneteleport.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.simke.lodestoneteleport.LodestoneConfig;
import dev.simke.lodestoneteleport.LodestoneTeleportMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LodestoneClientPreferences {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = LodestoneTeleportMod.MOD_ID + "_client.json";
	private static final List<String> DEFAULT_COLUMNS = List.of("favorite", "name", "coords", "dimension", "cost");
	private static final List<String> ALL_COLUMNS = List.of("favorite", "name", "coords", "dimension", "owner", "visibility", "cost");
	private static LodestoneClientPreferences INSTANCE = defaults();

	public List<String> modUiColumns = new ArrayList<>(DEFAULT_COLUMNS);
	public List<String> favoriteLodestones = new ArrayList<>();
	public boolean sortFavoritesFirst = true;
	public int modUiPanelX = -1;
	public int modUiPanelY = -1;
	public int modUiPanelWidth = 620;
	public int modUiPanelHeight = 420;

	private LodestoneClientPreferences() {
	}

	public static LodestoneClientPreferences get() {
		return INSTANCE;
	}

	public static List<String> allColumns() {
		return ALL_COLUMNS;
	}

	public static void load() {
		Path path = path();
		Path legacyPath = legacyPath();
		if (Files.notExists(path) && Files.exists(legacyPath)) {
			loadFrom(path, legacyPath);
			return;
		}
		if (Files.notExists(path)) {
			INSTANCE = defaults();
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonObject merged = mergeDefaults(JsonParser.parseReader(reader));
			LodestoneClientPreferences loaded = GSON.fromJson(merged, LodestoneClientPreferences.class);
			INSTANCE = sanitize(loaded == null ? defaults() : loaded);
			save();
		} catch (IOException | JsonSyntaxException exception) {
			LodestoneTeleportMod.LOGGER.warn("Failed to load {}, using defaults.", FILE_NAME, exception);
			INSTANCE = defaults();
		}
	}

	private static void loadFrom(Path targetPath, Path sourcePath) {
		try (Reader reader = Files.newBufferedReader(sourcePath)) {
			JsonObject merged = mergeDefaults(JsonParser.parseReader(reader));
			LodestoneClientPreferences loaded = GSON.fromJson(merged, LodestoneClientPreferences.class);
			INSTANCE = sanitize(loaded == null ? defaults() : loaded);
			save(targetPath);
			LodestoneTeleportMod.LOGGER.info("Migrated Lodestone Warps client preferences from {} to {}.", sourcePath, targetPath);
		} catch (IOException | JsonSyntaxException exception) {
			LodestoneTeleportMod.LOGGER.warn("Failed to migrate {}, using defaults.", sourcePath, exception);
			INSTANCE = defaults();
			save(targetPath);
		}
	}

	public static void save() {
		INSTANCE = sanitize(INSTANCE);
		save(path());
	}

	private static void save(Path path) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(INSTANCE, writer);
			}
		} catch (IOException exception) {
			LodestoneTeleportMod.LOGGER.warn("Failed to save {}.", FILE_NAME, exception);
		}
	}

	public static void toggleFavorite(String id) {
		if (INSTANCE.favoriteLodestones.contains(id)) {
			INSTANCE.favoriteLodestones.remove(id);
		} else {
			INSTANCE.favoriteLodestones.add(id);
		}
		save();
	}

	public boolean favorite(String id) {
		return this.favoriteLodestones.contains(id);
	}

	public List<String> columns() {
		return this.modUiColumns;
	}

	public void setColumns(List<String> columns) {
		this.modUiColumns = new ArrayList<>(columns);
		save();
	}

	public void setPanelBounds(int x, int y, int width, int height) {
		this.modUiPanelX = x;
		this.modUiPanelY = y;
		this.modUiPanelWidth = width;
		this.modUiPanelHeight = height;
		save();
	}

	private static LodestoneClientPreferences defaults() {
		return new LodestoneClientPreferences();
	}

	private static JsonObject mergeDefaults(JsonElement loaded) {
		JsonObject merged = GSON.toJsonTree(defaults()).getAsJsonObject();
		if (loaded != null && loaded.isJsonObject()) {
			for (var entry : loaded.getAsJsonObject().entrySet()) {
				merged.add(entry.getKey(), entry.getValue());
			}
		}
		return merged;
	}

	private static LodestoneClientPreferences sanitize(LodestoneClientPreferences preferences) {
		preferences.modUiColumns = cleanColumns(preferences.modUiColumns);
		preferences.favoriteLodestones = cleanStrings(preferences.favoriteLodestones);
		preferences.modUiPanelWidth = Math.max(430, preferences.modUiPanelWidth);
		preferences.modUiPanelHeight = Math.max(300, preferences.modUiPanelHeight);
		return preferences;
	}

	private static List<String> cleanColumns(List<String> columns) {
		Set<String> clean = new LinkedHashSet<>();
		if (columns != null) {
			for (String column : columns) {
				if (ALL_COLUMNS.contains(column)) {
					clean.add(column);
				}
			}
		}
		if (clean.isEmpty()) {
			clean.addAll(DEFAULT_COLUMNS);
		}
		List<String> ordered = new ArrayList<>(clean);
		if (ordered.remove("favorite")) {
			ordered.add(0, "favorite");
		}
		return ordered;
	}

	private static List<String> cleanStrings(List<String> values) {
		List<String> clean = new ArrayList<>();
		if (values == null) {
			return clean;
		}
		for (String value : values) {
			if (value != null && !value.isBlank() && !clean.contains(value)) {
				clean.add(value);
			}
		}
		return clean;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(LodestoneConfig.CONFIG_DIR_NAME).resolve(FILE_NAME);
	}

	private static Path legacyPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}
}
