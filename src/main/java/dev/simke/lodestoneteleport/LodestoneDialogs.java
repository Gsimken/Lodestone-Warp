package dev.simke.lodestoneteleport;

import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LodestoneDialogs {
	private static final int INPUT_WIDTH = 300;
	private static final int DESTINATION_BUTTON_WIDTH = 250;
	private static final int EDIT_BUTTON_WIDTH = 40;
	private static final int CONFIG_BUTTON_WIDTH = 340;
	private static final int GRID_COLUMNS = 2;
	private static final int DESTINATION_LABEL_WIDTH = 25;

	private LodestoneDialogs() {
	}

	public static void showDestinations(ServerPlayer player, LodestoneLocation current) {
		showDestinations(player, current, "");
	}

	public static void showDestinations(ServerPlayer player, LodestoneLocation current, String query) {
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		List<ActionButton> buttons = new ArrayList<>();
		int limit = LodestoneConfig.get().maxDialogDestinations;
		String cleanQuery = query == null ? "" : query.trim();
		boolean canRename = LodestonePermissions.canRename(player);
		int columns = canRename ? GRID_COLUMNS : 1;
		int destinationCount = 0;

		buttons.add(searchButton(current.id()));
		if (canRename) {
			buttons.add(spacerButton());
		}

		for (LodestoneLocation destination : data.all()) {
			if (destination.id().equals(current.id())) {
				continue;
			}
			if (!LodestoneDiscovery.canSee(player, data, destination)) {
				continue;
			}
			if (!matches(destination, cleanQuery)) {
				continue;
			}
			if (destinationCount >= limit) {
				break;
			}
			LodestoneTeleportCost cost = LodestoneTeleportCost.between(player, destination);
			buttons.add(customButton(destinationLabel(destination, cost), Optional.of(LodestoneText.cost(cost)), "tp", destination.id()));
			if (canRename) {
				buttons.add(editButton(destination));
			}
			destinationCount++;
		}
		if (canRename) {
			buttons.add(customButton(LodestoneText.text("button.rename_current", "Rename this warp").withStyle(ChatFormatting.GOLD), "edit", current.id()));
			buttons.add(spacerButton());
		}

		CommonDialogData common = new CommonDialogData(
			LodestoneText.title(),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(bodyText(current, cleanQuery, destinationCount == 0), INPUT_WIDTH)),
			List.of(new Input("query", new TextInput(INPUT_WIDTH, LodestoneText.text("input.search", "Search"), true, cleanQuery, 48, Optional.empty())))
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), columns));
	}

	public static void showRename(ServerPlayer player, LodestoneLocation location) {
		CommonDialogData common = new CommonDialogData(
			LodestoneText.text("rename.title", "Name lodestone"),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(LodestoneText.text("rename.body", "Choose a name for this lodestone."), INPUT_WIDTH)),
			List.of(new Input("name", new TextInput(INPUT_WIDTH, LodestoneText.text("input.name", "Name"), true, location.displayName(), 48, Optional.empty())))
		);

		ActionButton confirm = new ActionButton(
			new CommonButtonData(LodestoneText.text("button.save", "Save"), INPUT_WIDTH),
			Optional.of(renameAction(location.id()))
		);
		send(player, new NoticeDialog(common, confirm));
	}

	public static void showConfig(ServerPlayer player, String category, String query) {
		String cleanCategory = LodestoneConfigOptions.cleanCategory(category);
		String cleanQuery = query == null ? "" : query.trim();
		List<ActionButton> buttons = new ArrayList<>();

		buttons.add(configSearchButton(cleanCategory));
		buttons.add(configActionButton(LodestoneText.text("config.server.button.reload", "Reload from disk").withStyle(ChatFormatting.GOLD), Optional.empty(), CONFIG_BUTTON_WIDTH, "config_reload", cleanCategory, "", cleanQuery));
		buttons.add(configCategoryButton(LodestoneConfigOptions.ALL, "config.page.all", "All", cleanCategory, cleanQuery));
		buttons.add(configCategoryButton(LodestoneConfigOptions.COST, "config.page.cost", "Cost", cleanCategory, cleanQuery));
		buttons.add(configCategoryButton(LodestoneConfigOptions.REGISTRATION, "config.page.registration", "Registration", cleanCategory, cleanQuery));
		buttons.add(configCategoryButton(LodestoneConfigOptions.TELEPORT, "config.page.teleport", "Teleport", cleanCategory, cleanQuery));
		buttons.add(configCategoryButton(LodestoneConfigOptions.ADVANCED, "config.page.advanced", "Advanced", cleanCategory, cleanQuery));

		for (LodestoneConfigOptions.Option option : LodestoneConfigOptions.filtered(cleanCategory, cleanQuery)) {
			if (option.type() == LodestoneConfigOptions.Type.BOOLEAN) {
				buttons.add(configActionButton(configOptionLabel(option), Optional.of(configOptionTooltip(option)), CONFIG_BUTTON_WIDTH, "config_toggle", cleanCategory, option.id(), cleanQuery));
				continue;
			}
			buttons.add(configActionButton(configOptionLabel(option).copy().append(Component.literal(" \u270e").withStyle(ChatFormatting.GOLD)), Optional.of(configOptionTooltip(option)), CONFIG_BUTTON_WIDTH, "config_edit", cleanCategory, option.id(), cleanQuery));
		}

		CommonDialogData common = new CommonDialogData(
			LodestoneText.text("config.server.title", "Server Config"),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(configBody(cleanCategory, cleanQuery), INPUT_WIDTH)),
			List.of(new Input("query", new TextInput(INPUT_WIDTH, LodestoneText.text("input.search", "Search"), true, cleanQuery, 48, Optional.empty())))
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), 1));
	}

	public static void showConfigEdit(ServerPlayer player, LodestoneConfigOptions.Option option, String category, String query) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "config_save");
		payload.putString("key", option.id());
		payload.putString("category", LodestoneConfigOptions.cleanCategory(category));
		payload.putString("query", query == null ? "" : query);

		CommonDialogData common = new CommonDialogData(
			LodestoneText.text("config.server.edit_title", "Edit %s", option.labelFallback()),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(configOptionTooltip(option), INPUT_WIDTH)),
			List.of(new Input("value", new TextInput(INPUT_WIDTH, LodestoneText.text("config.server.input.value", "Value"), true, option.currentValue(), 96, Optional.empty())))
		);
		ActionButton confirm = new ActionButton(
			new CommonButtonData(LodestoneText.text("button.save", "Save"), INPUT_WIDTH),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
		send(player, new NoticeDialog(common, confirm));
	}

	public static void showNotice(ServerPlayer player, String title, String message) {
		showNotice(player, title, message, NoticeDialog.DEFAULT_ACTION);
	}

	private static void showNotice(ServerPlayer player, String title, String message, ActionButton action) {
		CommonDialogData common = new CommonDialogData(
			Component.literal(title),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.<DialogBody>of(new PlainMessage(Component.literal(message), INPUT_WIDTH)),
			List.of()
		);
		send(player, new NoticeDialog(common, action));
	}

	private static ActionButton customButton(String label, String action, String id) {
		return customButton(Component.literal(label), Optional.empty(), action, id);
	}

	private static ActionButton customButton(Component label, String action, String id) {
		return customButton(label, Optional.empty(), action, id);
	}

	private static ActionButton customButton(Component label, Optional<Component> tooltip, String action, String id) {
		return customButton(label, tooltip, DESTINATION_BUTTON_WIDTH, action, id);
	}

	private static ActionButton customButton(Component label, Optional<Component> tooltip, int width, String action, String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", action);
		payload.putString("id", id);
		return new ActionButton(
			new CommonButtonData(label, tooltip, width),
			Optional.of(new StaticAction(new ClickEvent.Custom(LodestoneCustomActions.ACTION_ID, Optional.of(payload))))
		);
	}

	private static ActionButton editButton(LodestoneLocation location) {
		return editButton(location, LodestoneText.text("button.rename", "Rename %s", location.displayName()));
	}

	private static ActionButton editButton(LodestoneLocation location, Component tooltip) {
		return customButton(Component.literal("\u270e").withStyle(ChatFormatting.GOLD), Optional.of(tooltip), EDIT_BUTTON_WIDTH, "edit", location.id());
	}

	private static ActionButton spacerButton() {
		return new ActionButton(
			new CommonButtonData(Component.empty(), EDIT_BUTTON_WIDTH),
			Optional.empty()
		);
	}

	private static ActionButton searchButton(String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "search");
		payload.putString("id", id);
		return new ActionButton(
			new CommonButtonData(LodestoneText.text("button.search", "Search location").withStyle(ChatFormatting.AQUA), DESTINATION_BUTTON_WIDTH),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static ActionButton configSearchButton(String category) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "config_open");
		payload.putString("category", category);
		return new ActionButton(
			new CommonButtonData(LodestoneText.text("button.search", "Search location").withStyle(ChatFormatting.AQUA), CONFIG_BUTTON_WIDTH),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static ActionButton configCategoryButton(String category, String key, String fallback, String currentCategory, String query) {
		Component label = LodestoneText.text(key, fallback).withStyle(category.equals(currentCategory) ? ChatFormatting.YELLOW : ChatFormatting.WHITE);
		return configActionButton(label, Optional.empty(), CONFIG_BUTTON_WIDTH, "config_open", category, "", query);
	}

	private static ActionButton configActionButton(Component label, Optional<Component> tooltip, int width, String action, String category, String key, String query) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", action);
		payload.putString("category", category);
		payload.putString("key", key);
		payload.putString("query", query);
		return new ActionButton(
			new CommonButtonData(label, tooltip, width),
			Optional.of(new StaticAction(new ClickEvent.Custom(LodestoneCustomActions.ACTION_ID, Optional.of(payload))))
		);
	}

	private static Component configBody(String category, String query) {
		Component categoryLabel = switch (category) {
			case LodestoneConfigOptions.COST -> LodestoneText.text("config.page.cost", "Cost");
			case LodestoneConfigOptions.REGISTRATION -> LodestoneText.text("config.page.registration", "Registration");
			case LodestoneConfigOptions.TELEPORT -> LodestoneText.text("config.page.teleport", "Teleport");
			case LodestoneConfigOptions.ADVANCED -> LodestoneText.text("config.page.advanced", "Advanced");
			default -> LodestoneText.text("config.page.all", "All");
		};
		if (query == null || query.isBlank()) {
			return LodestoneText.text("config.server.body", "Category: %s", categoryLabel);
		}
		return LodestoneText.text("config.server.body.search", "Category: %s\nSearch: %s", categoryLabel, query);
	}

	private static Component configOptionLabel(LodestoneConfigOptions.Option option) {
		ChatFormatting valueColor = option.isDefault() ? ChatFormatting.WHITE : ChatFormatting.YELLOW;
		Component state = option.type() == LodestoneConfigOptions.Type.BOOLEAN
			? LodestoneText.text(Boolean.parseBoolean(option.currentValue()) ? "config.switch.on" : "config.switch.off", Boolean.parseBoolean(option.currentValue()) ? "ON" : "OFF")
			: Component.literal(option.currentValue());
		return LodestoneText.text(option.labelKey(), option.labelFallback()).withStyle(ChatFormatting.AQUA)
			.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
			.append(state.copy().withStyle(valueColor));
	}

	private static Component configOptionTooltip(LodestoneConfigOptions.Option option) {
		return Component.literal(option.description())
			.append(Component.literal("\n"))
			.append(Component.literal(option.acceptedValues()).withStyle(ChatFormatting.GRAY))
			.append(Component.literal("\n"))
			.append(LodestoneText.text("config.default", "Default: %s", option.defaultValue()).withStyle(option.isDefault() ? ChatFormatting.DARK_GRAY : ChatFormatting.YELLOW))
			.append(Component.literal("\n"))
			.append(LodestoneText.text("config.current", "Current: %s", option.currentValue()).withStyle(option.isDefault() ? ChatFormatting.GRAY : ChatFormatting.YELLOW));
	}

	private static Action renameAction(String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "rename");
		payload.putString("id", id);
		return new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload));
	}

	private static boolean matches(LodestoneLocation location, String query) {
		if (query.isBlank()) {
			return true;
		}
		String needle = query.toLowerCase();
		return location.displayName().toLowerCase().contains(needle)
			|| location.id().toLowerCase().contains(needle)
			|| location.dimension().identifier().toString().toLowerCase().contains(needle);
	}

	private static Component bodyText(LodestoneLocation current, String query, boolean noResults) {
		if (noResults && !query.isBlank()) {
			return LodestoneText.text("menu.body.no_results", "From %s\nNo results for: %s", current.displayName(), query);
		}
		return LodestoneText.text("menu.body", "From %s", current.displayName());
	}

	private static Component destinationLabel(LodestoneLocation destination, LodestoneTeleportCost cost) {
		String name = truncate(destination.displayName(), DESTINATION_LABEL_WIDTH - 8);
		String costLabel = cost.label();
		int prefixLength = destination.global() ? 2 : 0;
		int padding = Math.max(2, DESTINATION_LABEL_WIDTH - prefixLength - name.length() - costLabel.length());
		Component label = Component.empty();
		if (destination.global()) {
			label = label.copy().append(Component.literal("\ud83c\udf10 ").withStyle(ChatFormatting.GREEN));
		}
		return label.copy()
			.append(Component.literal(name))
			.append(Component.literal(" ".repeat(padding) + costLabel));
	}

	private static String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, Math.max(0, maxLength - 3)) + "...";
	}

	private static void send(ServerPlayer player, Dialog dialog) {
		player.connection.send(new ClientboundShowDialogPacket(Holder.direct(dialog)));
	}
}
