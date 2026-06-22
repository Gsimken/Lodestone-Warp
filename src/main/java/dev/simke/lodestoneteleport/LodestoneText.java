package dev.simke.lodestoneteleport;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LodestoneText {
	private LodestoneText() {
	}

	public static MutableComponent title() {
		return text("title", "Lodestone Warps");
	}

	public static MutableComponent text(String key, String fallback, Object... args) {
		return Component.translatableWithFallback("text.lodestone_teleport." + key, fallback, args);
	}

	public static Component dimension(ResourceKey<Level> dimension) {
		Identifier id = dimension.identifier();
		if (id.getNamespace().equals("minecraft")) {
			return text("dimension." + id.getPath(), fallbackDimension(id.getPath()));
		}
		return Component.literal(id.getNamespace() + ":" + id.getPath());
	}

	public static String dimensionPlain(ResourceKey<Level> dimension) {
		Identifier id = dimension.identifier();
		return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
	}

	public static Component item(LodestoneTeleportCost cost) {
		return new ItemStack(cost.item()).getItemName();
	}

	public static Component cost(LodestoneTeleportCost cost) {
		if (cost.amount() <= 0) {
			return text("cost.free", "gratis");
		}
		return text("cost.item", "%sx %s", cost.amount(), item(cost));
	}

	private static String fallbackDimension(String path) {
		return switch (path) {
			case "overworld" -> "overworld";
			case "the_nether" -> "nether";
			case "the_end" -> "end";
			default -> path;
		};
	}
}
