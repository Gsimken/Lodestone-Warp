package dev.simke.lodestoneteleport;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LodestoneConfigWarnings {
	private LodestoneConfigWarnings() {
	}

	public static List<Component> current() {
		return warnings(LodestoneConfig.get());
	}

	public static List<Component> warnings(LodestoneConfig config) {
		List<Component> warnings = new ArrayList<>();

		if (has(config.playerPermissions, "mode.all") && has(config.playerPermissions, "mode.discover")) {
			warnings.add(warning("config.warning.permissions.player_mode_conflict", "Both lodestone_teleport.mode.all and lodestone_teleport.mode.discover are configured for players. mode.all bypasses discovery and can affect the gameplay experience."));
		}
		if (has(config.adminPermissions, "mode.all") && has(config.adminPermissions, "mode.discover")) {
			warnings.add(warning("config.warning.permissions.admin_mode_conflict", "Both lodestone_teleport.mode.all and lodestone_teleport.mode.discover are configured for admins. mode.all bypasses discovery and can affect the gameplay experience."));
		}
		if ("discover".equals(config.networkMode) && has(config.playerPermissions, "mode.all")) {
			warnings.add(warning("config.warning.permissions.discover_bypassed", "Discovery mode is enabled, but players have lodestone_teleport.mode.all. Players will see every lodestone instead of only discovered/global ones."));
		}
		if ("discover".equals(config.networkMode) && has(config.playerPermissions, "mode.discover") && !has(config.playerPermissions, "use")) {
			warnings.add(warning("config.warning.permissions.discover_without_use", "Players have lodestone_teleport.mode.discover without lodestone_teleport.use. They can be in discovery mode but cannot use lodestones."));
		}
		if ("all".equals(config.networkMode) && has(config.playerPermissions, "mode.discover")) {
			warnings.add(warning("config.warning.permissions.all_with_player_discover", "Network mode is all, but players have lodestone_teleport.mode.discover. That permission forces discovery rules for players."));
		}
		if ("all".equals(config.networkMode) && has(config.adminPermissions, "mode.discover")) {
			warnings.add(warning("config.warning.permissions.all_with_admin_discover", "Network mode is all, but admins have lodestone_teleport.mode.discover. That permission forces discovery rules unless they also have lodestone_teleport.mode.all."));
		}

		return warnings;
	}

	public static void sendTo(CommandSourceStack source) {
		for (Component warning : current()) {
			source.sendSystemMessage(warning);
		}
	}

	public static void logCurrent() {
		for (Component warning : current()) {
			LodestoneTeleportMod.LOGGER.warn(warning.getString());
		}
	}

	private static Component warning(String key, String fallback) {
		return LodestoneText.text(key, fallback).withStyle(ChatFormatting.RED);
	}

	private static boolean has(Map<String, Boolean> permissions, String node) {
		if (permissions == null) {
			return false;
		}
		String fullNode = LodestoneTeleportMod.MOD_ID + "." + node;
		String namespaceWildcard = LodestoneTeleportMod.MOD_ID + ".*";
		return permissions.entrySet().stream().anyMatch(entry -> Boolean.TRUE.equals(entry.getValue())
			&& (entry.getKey().equals("*") || entry.getKey().equals(namespaceWildcard) || entry.getKey().equals(fullNode) || entry.getKey().equals(node)));
	}
}
