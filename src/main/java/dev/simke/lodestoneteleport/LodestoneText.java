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
		return Component.translatableWithFallback("text.lodestone_teleport." + key, fallbackFor(key, fallback), args);
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
			return text("cost.free", "free");
		}
		if (cost.usesXpLevels()) {
			return text("cost.xp_levels", "%s levels", cost.amount());
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

	private static String fallbackFor(String key, String fallback) {
		if ("es_es".equals(LodestoneConfig.get().serverLanguage)) {
			return fallback;
		}
		return switch (key) {
			case "menu.body" -> "From %s";
			case "menu.body.no_results" -> "From %s\nNo results for: %s";
			case "input.search" -> "Search";
			case "input.name" -> "Name";
			case "button.search" -> "Search location";
			case "button.rename" -> "Rename %s";
			case "button.rename_short" -> "[\u270e]";
			case "button.rename_current" -> "Rename this warp";
			case "button.remove" -> "[X]";
			case "button.save" -> "Save";
			case "button.teleport" -> "[TP]";
			case "rename.title" -> "Name lodestone";
			case "rename.body" -> "Choose a name for this lodestone.";
			case "cost.free" -> "free";
			case "cost.xp_levels" -> "%s levels";
			case "cost.item" -> "%sx %s";
			case "dimension.overworld" -> "overworld";
			case "dimension.the_nether" -> "nether";
			case "dimension.the_end" -> "the end";
			case "arrived" -> "You arrived at \"%s\" (%s, %s, %s, %s).";
			case "renamed" -> "Lodestone renamed to %s.";
			case "removed" -> "Unlinked lodestone warp: %s.";
			case "registered" -> "Lodestone registered: %s";
			case "discovered" -> "Discovered lodestone: %s";
			case "global.badge" -> "[Global]";
			case "global.enabled" -> "Lodestone marked global: %s";
			case "global.disabled" -> "Lodestone is no longer global: %s";
			case "discover.granted" -> "Granted %s discovery of %s.";
			case "discover.revoked" -> "Revoked %s discovery of %s.";
			case "discover.list_header" -> "%s has discovered:";
			case "discover.list_empty" -> "No discovered lodestones.";
			case "list.empty" -> "No lodestones registered.";
			case "list.header" -> "Registered lodestones:";
			case "list.entry" -> "- %s: %s (%s)";
			case "error.missing_destination" -> "That destination no longer exists.";
			case "error.duplicate_destination_name" -> "More than one lodestone is named \"%s\". Choose one:";
			case "error.dimension_unloaded" -> "The destination dimension is not loaded.";
			case "error.cross_dimension_disabled" -> "Cross-dimension teleport is disabled.";
			case "error.destination_removed" -> "The destination no longer has a lodestone and was removed.";
			case "error.lodestone_not_registered" -> "This lodestone is not registered.";
			case "error.not_discovered" -> "You have not discovered that lodestone.";
			case "error.max_lodestones_global" -> "The server has reached the maximum number of registered lodestones.";
			case "error.max_lodestones_player" -> "You have reached your maximum number of registered lodestones.";
			case "error.need_cost" -> "You need %s.";
			case "error.need_near_lodestone" -> "You must be near a registered lodestone to teleport.";
			case "error.cooldown" -> "You must wait %s seconds before teleporting again.";
			case "teleport.cast_start" -> "Casting teleport... stand still for %s seconds.";
			case "teleport.cast_cancelled" -> "Teleport cancelled: you moved.";
			case "teleport.cast_already" -> "You are already casting a teleport.";
			case "error.no_permission.use" -> "You do not have permission to use lodestones.";
			case "error.no_permission.rename" -> "You do not have permission to rename lodestones.";
			case "error.no_permission.create" -> "You do not have permission to register lodestones.";
			case "error.no_permission.remove" -> "You do not have permission to remove registered lodestones.";
			case "error.no_permission.global" -> "You do not have permission to manage global lodestones.";
			case "error.lodestone_not_found" -> "I could not find that lodestone.";
			case "error.invalid_action" -> "Invalid lodestone action.";
			case "error.action_failed" -> "Could not run the lodestone action.";
			default -> fallback;
		};
	}
}
