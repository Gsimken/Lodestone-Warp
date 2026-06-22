package dev.simke.lodestoneteleport;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public record LodestoneTeleportCost(Item item, String itemId, int amount, double distance, boolean crossDimension) {
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

		return new LodestoneTeleportCost(config.costItem(), config.costItem, Math.max(0, amount), distance, crossDimension);
	}

	public String label() {
		if (amount <= 0) {
			return "gratis";
		}
		return symbol() + " " + amount;
	}

	public String fullLabel() {
		if (amount <= 0) {
			return "gratis";
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
