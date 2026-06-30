package dev.simke.lodestoneteleport;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class LodestoneSavedData extends SavedData {
	public static final SavedDataType<LodestoneSavedData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(LodestoneTeleportMod.MOD_ID, "lodestones"),
		LodestoneSavedData::new,
		CompoundTag.CODEC.xmap(LodestoneSavedData::fromTag, LodestoneSavedData::toTag),
		DataFixTypes.LEVEL
	);

	private final Map<String, LodestoneLocation> byId = new LinkedHashMap<>();
	private final Map<String, String> idByPosition = new LinkedHashMap<>();
	private final Map<UUID, Set<String>> discoveredByPlayer = new LinkedHashMap<>();
	private long nextId = 1L;

	public static LodestoneSavedData from(Level level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public Optional<LodestoneLocation> get(String id) {
		return Optional.ofNullable(byId.get(id));
	}

	public Optional<LodestoneLocation> at(ResourceKey<Level> dimension, BlockPos pos) {
		String id = idByPosition.get(LodestoneLocation.positionKey(dimension, pos));
		return id == null ? Optional.empty() : get(id);
	}

	public Collection<LodestoneLocation> all() {
		ArrayList<LodestoneLocation> locations = new ArrayList<>(byId.values());
		locations.sort(Comparator.comparing(LodestoneLocation::displayName, String.CASE_INSENSITIVE_ORDER));
		return locations;
	}

	public LodestoneLocation register(ResourceKey<Level> dimension, BlockPos pos, UUID ownerUuid, String ownerName) {
		return register(dimension, pos, ownerUuid, ownerName, LodestoneConfig.get().defaultVisibility());
	}

	public LodestoneLocation register(ResourceKey<Level> dimension, BlockPos pos, UUID ownerUuid, String ownerName, LodestoneVisibility visibility) {
		return at(dimension, pos).orElseGet(() -> {
			String id = "lodestone_" + nextId++;
			LodestoneLocation location = new LodestoneLocation(
				id,
				LodestoneLocation.autoName(dimension, pos),
				dimension,
				pos.immutable(),
				ownerUuid,
				ownerName,
				System.currentTimeMillis(),
				visibility
			);
			put(location);
			return location;
		});
	}

	public boolean rename(String id, String name) {
		LodestoneLocation current = byId.get(id);
		if (current == null) {
			return false;
		}
		String cleanName = cleanName(name);
		LodestoneLocation renamed = new LodestoneLocation(
			current.id(),
			cleanName.isBlank() ? current.displayName() : cleanName,
			current.dimension(),
			current.pos(),
			current.ownerUuid(),
			current.ownerName(),
			current.createdAt(),
			current.visibility()
		);
		put(renamed);
		return true;
	}

	public boolean setGlobal(String id, boolean global) {
		return setVisibility(id, global ? LodestoneVisibility.GLOBAL : LodestoneVisibility.DISCOVERABLE);
	}

	public boolean setVisibility(String id, LodestoneVisibility visibility) {
		LodestoneLocation current = byId.get(id);
		if (current == null || current.visibility() == visibility) {
			return false;
		}
		LodestoneLocation updated = new LodestoneLocation(
			current.id(),
			current.name(),
			current.dimension(),
			current.pos(),
			current.ownerUuid(),
			current.ownerName(),
			current.createdAt(),
			visibility
		);
		put(updated);
		return true;
	}

	public boolean isDiscovered(UUID playerUuid, String id) {
		return discoveredByPlayer.getOrDefault(playerUuid, Set.of()).contains(id);
	}

	public boolean discover(UUID playerUuid, String id) {
		if (!byId.containsKey(id)) {
			return false;
		}
		boolean added = discoveredByPlayer.computeIfAbsent(playerUuid, ignored -> new HashSet<>()).add(id);
		if (added) {
			setDirty();
		}
		return added;
	}

	public int discoverAll(UUID playerUuid) {
		Set<String> discovered = discoveredByPlayer.computeIfAbsent(playerUuid, ignored -> new HashSet<>());
		int before = discovered.size();
		discovered.addAll(byId.keySet());
		int added = discovered.size() - before;
		if (added > 0) {
			setDirty();
		}
		return added;
	}

	public boolean revokeDiscovery(UUID playerUuid, String id) {
		Set<String> discovered = discoveredByPlayer.get(playerUuid);
		if (discovered == null) {
			return false;
		}
		boolean removed = discovered.remove(id);
		if (discovered.isEmpty()) {
			discoveredByPlayer.remove(playerUuid);
		}
		if (removed) {
			setDirty();
		}
		return removed;
	}

	public int revokeAllDiscoveries(UUID playerUuid) {
		Set<String> removed = discoveredByPlayer.remove(playerUuid);
		if (removed == null || removed.isEmpty()) {
			return 0;
		}
		setDirty();
		return removed.size();
	}

	public Set<String> discoveredIds(UUID playerUuid) {
		return Set.copyOf(discoveredByPlayer.getOrDefault(playerUuid, Set.of()));
	}

	public Set<UUID> discoverers(String id) {
		Set<UUID> players = new HashSet<>();
		for (Map.Entry<UUID, Set<String>> entry : discoveredByPlayer.entrySet()) {
			if (entry.getValue().contains(id)) {
				players.add(entry.getKey());
			}
		}
		return Set.copyOf(players);
	}

	public boolean remove(ResourceKey<Level> dimension, BlockPos pos) {
		String positionKey = LodestoneLocation.positionKey(dimension, pos);
		String id = idByPosition.remove(positionKey);
		if (id == null) {
			return false;
		}
		byId.remove(id);
		for (Set<String> discovered : discoveredByPlayer.values()) {
			discovered.remove(id);
		}
		setDirty();
		return true;
	}

	private void put(LodestoneLocation location) {
		byId.put(location.id(), location);
		idByPosition.put(location.positionKey(), location.id());
		setDirty();
	}

	private CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putLong("next_id", nextId);
		ListTag list = new ListTag();
		for (LodestoneLocation location : byId.values()) {
			CompoundTag entry = new CompoundTag();
			entry.putString("id", location.id());
			entry.putString("name", location.displayName());
			entry.putString("dimension", location.dimension().identifier().toString());
			entry.putInt("x", location.pos().getX());
			entry.putInt("y", location.pos().getY());
			entry.putInt("z", location.pos().getZ());
			entry.putString("owner_uuid", location.ownerUuid().toString());
			entry.putString("owner_name", location.ownerName());
			entry.putLong("created_at", location.createdAt());
			entry.putBoolean("global", location.global());
			entry.putString("visibility", location.visibility().id());
			list.add(entry);
		}
		tag.put("lodestones", list);
		ListTag discoveries = new ListTag();
		for (Map.Entry<UUID, Set<String>> playerEntry : discoveredByPlayer.entrySet()) {
			CompoundTag entry = new CompoundTag();
			entry.putString("player_uuid", playerEntry.getKey().toString());
			ListTag ids = new ListTag();
			for (String id : playerEntry.getValue()) {
				CompoundTag idTag = new CompoundTag();
				idTag.putString("id", id);
				ids.add(idTag);
			}
			entry.put("ids", ids);
			discoveries.add(entry);
		}
		tag.put("discoveries", discoveries);
		return tag;
	}

	private static LodestoneSavedData fromTag(CompoundTag tag) {
		LodestoneSavedData data = new LodestoneSavedData();
		data.nextId = Math.max(1L, tag.getLongOr("next_id", 1L));
		for (CompoundTag entry : tag.getListOrEmpty("lodestones").compoundStream().toList()) {
			Identifier dimensionId = Identifier.tryParse(entry.getStringOr("dimension", "minecraft:overworld"));
			if (dimensionId == null) {
				continue;
			}
			ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
			UUID ownerUuid = parseUuid(entry.getStringOr("owner_uuid", "00000000-0000-0000-0000-000000000000"));
			LodestoneVisibility visibility = entry.contains("visibility")
				? LodestoneVisibility.from(entry.getStringOr("visibility", "discoverable"), LodestoneVisibility.DISCOVERABLE)
				: (entry.getBooleanOr("global", false) ? LodestoneVisibility.GLOBAL : LodestoneVisibility.DISCOVERABLE);
			LodestoneLocation location = new LodestoneLocation(
				entry.getStringOr("id", ""),
				entry.getStringOr("name", ""),
				dimension,
				new BlockPos(entry.getIntOr("x", 0), entry.getIntOr("y", 0), entry.getIntOr("z", 0)),
				ownerUuid,
				entry.getStringOr("owner_name", "unknown"),
				entry.getLongOr("created_at", 0L),
				visibility
			);
			if (!location.id().isBlank()) {
				data.byId.put(location.id(), location);
				data.idByPosition.put(location.positionKey(), location.id());
			}
		}
		for (CompoundTag entry : tag.getListOrEmpty("discoveries").compoundStream().toList()) {
			UUID playerUuid = parseUuid(entry.getStringOr("player_uuid", "00000000-0000-0000-0000-000000000000"));
			Set<String> ids = new HashSet<>();
			for (CompoundTag idTag : entry.getListOrEmpty("ids").compoundStream().toList()) {
				String id = idTag.getStringOr("id", "");
				if (!id.isBlank()) {
					ids.add(id);
				}
			}
			if (!ids.isEmpty()) {
				data.discoveredByPlayer.put(playerUuid, ids);
			}
		}
		return data;
	}

	private static UUID parseUuid(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException exception) {
			return new UUID(0L, 0L);
		}
	}

	private static String cleanName(String value) {
		if (value == null) {
			return "";
		}
		String clean = value.trim().replaceAll("\\s+", " ");
		return clean.substring(0, Math.min(48, clean.length()));
	}
}
