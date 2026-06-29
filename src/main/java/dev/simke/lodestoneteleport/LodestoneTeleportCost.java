package dev.simke.lodestoneteleport;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record LodestoneTeleportCost(String type, Item item, String itemId, int amount, double distance, boolean crossDimension) {
	public static LodestoneTeleportCost between(ServerPlayer player, LodestoneLocation destination) {
		LodestoneConfig config = LodestoneConfig.get();
		boolean crossDimension = !player.level().dimension().equals(destination.dimension());
		double distance = crossDimension ? 0.0D : Math.sqrt(player.blockPosition().distSqr(destination.pos()));
		int amount = config.baseCost;

		if (!crossDimension && config.blocksPerExtraCost > 0) {
			amount += (int) Math.floor(distance / config.blocksPerExtraCost);
		}
		if (crossDimension) {
			amount = (int) Math.ceil(amount * config.crossDimensionMultiplier);
		}
		if (config.maxCost > 0) {
			amount = Math.min(amount, config.maxCost);
		}

		if ("item".equals(config.costType)) {
			return new LodestoneTeleportCost("item", config.costItem(), config.costItem, Math.max(0, amount), distance, crossDimension);
		}
		return new LodestoneTeleportCost("xp_levels", Items.EXPERIENCE_BOTTLE, "minecraft:experience_bottle", Math.max(0, amount), distance, crossDimension);
	}

	public boolean usesXpLevels() {
		return "xp_levels".equals(type);
	}

	public String label() {
		if (amount <= 0) {
			return "free";
		}
		if (usesXpLevels()) {
			return "XP " + amount;
		}
		return symbol() + " " + amount;
	}

	public String fullLabel() {
		if (amount <= 0) {
			return "free";
		}
		if (usesXpLevels()) {
			return amount + " levels";
		}
		return amount + "x " + displayItemId();
	}

	private String symbol() {
		String id = displayItemId();
		return switch (id) {
			case "diamond" -> "\u25c6";
			case "emerald" -> "\u25c7";
			case "experience_bottle" -> "\u2726";
			default -> id;
		};
	}

	private String displayItemId() {
		return itemId.startsWith("minecraft:") ? itemId.substring("minecraft:".length()) : itemId;
	}
}
