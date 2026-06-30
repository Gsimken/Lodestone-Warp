package dev.simke.lodestoneteleport;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record LodestoneLocation(
	String id,
	String name,
	ResourceKey<Level> dimension,
	BlockPos pos,
	UUID ownerUuid,
	String ownerName,
	long createdAt,
	LodestoneVisibility visibility
) {
	private static final String GLOBAL_PREFIX = "\u25ce ";

	public String positionKey() {
		return positionKey(dimension, pos);
	}

	public String displayName() {
		return name == null || name.isBlank() ? autoName(dimension, pos) : name;
	}

	public String displayNameWithGlobalPrefix() {
		return global() ? GLOBAL_PREFIX + displayName() : displayName();
	}

	public boolean global() {
		return visibility == LodestoneVisibility.GLOBAL;
	}

	public boolean discoverable() {
		return visibility == LodestoneVisibility.DISCOVERABLE;
	}

	public boolean privateWarp() {
		return visibility == LodestoneVisibility.PRIVATE;
	}

	public boolean ownedBy(UUID uuid) {
		return ownerUuid != null && ownerUuid.equals(uuid);
	}

	public static String positionKey(ResourceKey<Level> dimension, BlockPos pos) {
		return dimension.identifier() + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}

	public static String autoName(ResourceKey<Level> dimension, BlockPos pos) {
		Identifier id = dimension.identifier();
		String dimensionName = id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
		return prettyDimension(dimensionName) + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
	}

	private static String prettyDimension(String name) {
		String[] parts = name.replace('_', ' ').split(" ");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.isEmpty() ? name : builder.toString();
	}
}
