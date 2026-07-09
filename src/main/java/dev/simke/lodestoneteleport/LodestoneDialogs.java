package dev.simke.lodestoneteleport;

import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import java.util.Locale;
import java.util.Optional;

public final class LodestoneDialogs {
	private static final int INPUT_WIDTH = 300;
	private static final int DEFAULT_DESTINATION_BUTTON_WIDTH = 245;
	private static final int DEFAULT_COST_BUTTON_WIDTH = 70;
	private static final int DEFAULT_EDIT_BUTTON_WIDTH = 70;
	private static final int FULL_ROW_BUTTON_WIDTH = 340;
	private static final int CONFIG_BUTTON_WIDTH = 340;
	private static final int GRID_COLUMNS = 3;
	private static final String WIKI_URL = "https://github.com/Gsimken/Lodestone-Warp/wiki";

	private LodestoneDialogs() {
	}

	public static void showDestinations(ServerPlayer player, LodestoneLocation current) {
		showDestinations(player, current, "");
	}

	public static void showDestinations(ServerPlayer player, LodestoneLocation current, String query) {
		showDestinations(player, current, query, 0);
	}

	public static void showDestinations(ServerPlayer player, LodestoneLocation current, String query, int page) {
		LodestoneSavedData data = LodestoneSavedData.from(player.level());
		List<ActionButton> buttons = new ArrayList<>();
		int limit = LodestoneConfig.get().maxDialogDestinations;
		String cleanQuery = query == null ? "" : query.trim();
		boolean canEditCurrent = canEdit(player, current);
		int columns = GRID_COLUMNS;
		List<LodestoneLocation> destinations = new ArrayList<>();

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
			destinations.add(destination);
		}
		int totalPages = Math.max(1, (int) Math.ceil(destinations.size() / (double) limit));
		int currentPage = Math.max(0, Math.min(page, totalPages - 1));
		int start = currentPage * limit;
		int end = Math.min(destinations.size(), start + limit);

		addOrderedRow(
			buttons,
			navButton(current.id(), cleanQuery, currentPage - 1, "\u00ab", currentPage > 0, costButtonWidth()),
			searchButton(current.id()),
			navButton(current.id(), cleanQuery, currentPage + 1, "\u00bb", currentPage < totalPages - 1, editButtonWidth())
		);

		for (int index = start; index < end; index++) {
			LodestoneLocation destination = destinations.get(index);
			LodestoneTeleportCost cost = LodestoneTeleportCost.between(player, destination);
			LodestoneTeleportAvailability availability = LodestoneTeleportAvailability.check(player, data, destination, cost);
			ActionButton edit = canEdit(player, destination) ? editButton(destination) : spacerButton(editButtonWidth());
			addOrderedRow(
				buttons,
				costButton(cost, availability),
				destinationButton(destination, availability),
				edit
			);
		}
		if (canEditCurrent) {
			addOrderedRow(
				buttons,
				navButton(current.id(), cleanQuery, currentPage - 1, "\u00ab", currentPage > 0, costButtonWidth()),
				customButton(LodestoneText.serverText("button.rename_current", "Edit this warp").withStyle(ChatFormatting.GOLD), "edit", current.id()),
				navButton(current.id(), cleanQuery, currentPage + 1, "\u00bb", currentPage < totalPages - 1, editButtonWidth())
			);
		}

		CommonDialogData common = new CommonDialogData(
			LodestoneText.serverTitle(),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(bodyText(player, current, cleanQuery, destinations.isEmpty(), currentPage, totalPages), INPUT_WIDTH)),
			List.of(new Input("query", new TextInput(INPUT_WIDTH, LodestoneText.serverText("input.search", "Search"), true, cleanQuery, 48, Optional.empty())))
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), columns));
	}

	public static void showRename(ServerPlayer player, LodestoneLocation location) {
		showEdit(player, location);
	}

	public static void showEdit(ServerPlayer player, LodestoneLocation location) {
		showEdit(player, location, location.displayName(), location.visibility());
	}

	public static void showEdit(ServerPlayer player, LodestoneLocation location, String pendingName, LodestoneVisibility pendingVisibility) {
		List<ActionButton> buttons = new ArrayList<>();
		if (LodestonePermissions.canRename(player, location) || pendingVisibility != location.visibility()) {
			buttons.add(new ActionButton(
				new CommonButtonData(LodestoneText.serverText("button.save", "Save"), INPUT_WIDTH),
				Optional.of(saveEditAction(location.id(), pendingVisibility))
			));
		}
		List<LodestoneVisibility> allowedVisibilities = allowedVisibilities(player, location);
		if (allowedVisibilities.size() > 1) {
			buttons.add(modeButton(location.id(), pendingVisibility, nextVisibility(allowedVisibilities, pendingVisibility)));
		}
		if (LodestonePermissions.canRemove(player, location)) {
			buttons.add(customButton(LodestoneText.serverText("button.remove_current", "Unlink lodestone").withStyle(ChatFormatting.RED), "remove", location.id()));
		}
		if (buttons.isEmpty()) {
			buttons.add(new ActionButton(new CommonButtonData(Component.translatable("gui.done"), INPUT_WIDTH), Optional.empty()));
		}

		CommonDialogData common = new CommonDialogData(
			LodestoneText.serverText("edit.title", "Edit lodestone"),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(editBody(player, location, pendingVisibility), INPUT_WIDTH)),
			List.of(new Input("name", new TextInput(INPUT_WIDTH, LodestoneText.serverText("input.name", "Name"), true, pendingName, 48, Optional.empty())))
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), 1));
	}

	public static void showConfig(ServerPlayer player, String category, String query) {
		String cleanCategory = LodestoneConfigOptions.ALL;
		String cleanQuery = query == null ? "" : query.trim();
		List<ActionButton> buttons = new ArrayList<>();

		buttons.add(configSearchButton(cleanCategory));
		buttons.add(configActionButton(LodestoneText.serverText("config.server.button.reload", "Reload from disk").withStyle(ChatFormatting.GOLD), Optional.empty(), CONFIG_BUTTON_WIDTH, "config_reload", cleanCategory, "", cleanQuery));

		for (LodestoneConfigOptions.Option option : LodestoneConfigOptions.filtered(cleanCategory, cleanQuery)) {
			if (isPermissionOption(option.id())) {
				buttons.add(configActionButton(configOptionLabel(option).copy().append(Component.literal(" \u2699").withStyle(ChatFormatting.GOLD)), Optional.of(configOptionTooltip(option)), CONFIG_BUTTON_WIDTH, "config_permissions", cleanCategory, option.id(), cleanQuery));
				continue;
			}
			if (option.type() == LodestoneConfigOptions.Type.BOOLEAN) {
				buttons.add(configActionButton(configOptionLabel(option), Optional.of(configOptionTooltip(option)), CONFIG_BUTTON_WIDTH, "config_toggle", cleanCategory, option.id(), cleanQuery));
				continue;
			}
			buttons.add(configActionButton(configOptionLabel(option).copy().append(Component.literal(" \u270e").withStyle(ChatFormatting.GOLD)), Optional.of(configOptionTooltip(option)), CONFIG_BUTTON_WIDTH, "config_edit", cleanCategory, option.id(), cleanQuery));
		}

		CommonDialogData common = new CommonDialogData(
			LodestoneText.serverText("config.server.title", "Server Config"),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(configBody(cleanCategory, cleanQuery), INPUT_WIDTH)),
			List.of(new Input("query", new TextInput(INPUT_WIDTH, LodestoneText.serverText("input.search", "Search"), true, cleanQuery, 48, Optional.empty())))
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), 1));
	}

	public static void showConfigPermissions(ServerPlayer player, String key, String query) {
		String cleanKey = isPermissionOption(key) ? key : "player_permissions";
		String cleanQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		List<ActionButton> buttons = new ArrayList<>();

		buttons.add(permissionActionButton(LodestoneText.serverText("button.search", "Search location").withStyle(ChatFormatting.AQUA), "permission_search", cleanKey, "", cleanQuery));
		buttons.add(permissionActionButton(LodestoneText.serverText("config.permission.add", "Add permission").withStyle(ChatFormatting.GOLD), "permission_add", cleanKey, "", cleanQuery));
		buttons.add(configActionButton(Component.translatable("gui.back"), Optional.empty(), CONFIG_BUTTON_WIDTH, "config_open", LodestoneConfigOptions.ALL, "", ""));

		for (var entry : LodestoneConfig.permissionMap(cleanKey).entrySet()) {
			String permission = entry.getKey();
			if (!cleanQuery.isBlank() && !permission.contains(cleanQuery)) {
				continue;
			}
			boolean enabled = Boolean.TRUE.equals(entry.getValue());
			Component toggle = LodestoneText.serverText(enabled ? "config.switch.on" : "config.switch.off", enabled ? "ON" : "OFF")
				.withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
			buttons.add(permissionActionButton(toggle, "permission_toggle", cleanKey, permission, cleanQuery, 64));
			buttons.add(new ActionButton(new CommonButtonData(Component.literal(permission).withStyle(enabled ? ChatFormatting.WHITE : ChatFormatting.GRAY), CONFIG_BUTTON_WIDTH - 120), Optional.empty()));
			buttons.add(permissionActionButton(LodestoneText.serverText("config.permission.remove", "Remove").withStyle(ChatFormatting.RED), "permission_remove", cleanKey, permission, cleanQuery, 80));
		}

		CommonDialogData common = new CommonDialogData(
			LodestoneText.serverText("config.permission.title", "Permissions: %s", cleanKey),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(permissionBody(cleanKey, cleanQuery), INPUT_WIDTH)),
			List.of(
				new Input("query", new TextInput(INPUT_WIDTH, LodestoneText.serverText("input.search", "Search"), true, cleanQuery, 64, Optional.empty())),
				new Input("permission", new TextInput(INPUT_WIDTH, LodestoneText.serverText("config.permission.input", "Permission"), true, "", 128, Optional.empty()))
			)
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), 3));
	}

	public static void showConfigEdit(ServerPlayer player, LodestoneConfigOptions.Option option, String category, String query) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "config_save");
		payload.putString("key", option.id());
		payload.putString("category", LodestoneConfigOptions.cleanCategory(category));
		payload.putString("query", query == null ? "" : query);

		CommonDialogData common = new CommonDialogData(
			LodestoneText.serverText("config.server.edit_title", "Edit %s", option.labelFallback()),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(configOptionTooltip(option), INPUT_WIDTH)),
			List.of(new Input("value", new TextInput(INPUT_WIDTH, LodestoneText.serverText("config.server.input.value", "Value"), true, option.currentValue(), 512, Optional.empty())))
		);
		ActionButton confirm = new ActionButton(
			new CommonButtonData(LodestoneText.serverText("button.save", "Save"), INPUT_WIDTH),
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
		return customButton(label, tooltip, destinationButtonWidth(), action, id);
	}

	private static ActionButton destinationButton(LodestoneLocation destination, LodestoneTeleportAvailability availability) {
		if (availability.canTeleport()) {
			return customButton(destinationLabel(destination), Optional.of(destinationTooltip(destination)), "tp", destination.id());
		}
		Component label = destinationLabel(destination)
			.copy()
			.append(Component.literal(" (" + availability.reason() + ")"))
			.withStyle(ChatFormatting.GRAY);
		return new ActionButton(
			new CommonButtonData(label, Optional.of(destinationTooltip(destination).copy().append(Component.literal("\n" + availability.reason()).withStyle(ChatFormatting.RED))), destinationButtonWidth()),
			Optional.empty()
		);
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

	private static ActionButton customButton(Component label, String action, String id, String visibility) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", action);
		payload.putString("id", id);
		payload.putString("visibility", visibility);
		return new ActionButton(
			new CommonButtonData(label, destinationButtonWidth()),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static ActionButton editButton(LodestoneLocation location) {
		return editButton(location, LodestoneText.serverText("button.rename", "Rename %s", location.displayName()));
	}

	private static ActionButton editButton(LodestoneLocation location, Component tooltip) {
		return customButton(Component.literal("\u270e").withStyle(ChatFormatting.GOLD), Optional.of(tooltip), editButtonWidth(), "edit", location.id());
	}

	private static ActionButton spacerButton() {
		return spacerButton(editButtonWidth());
	}

	private static ActionButton spacerButton(int width) {
		return new ActionButton(
			new CommonButtonData(Component.empty(), width),
			Optional.empty()
		);
	}

	private static ActionButton searchButton(String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "search");
		payload.putString("id", id);
		return new ActionButton(
			new CommonButtonData(LodestoneText.serverText("button.search", "Search location").withStyle(ChatFormatting.AQUA), destinationButtonWidth()),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static ActionButton costButton(LodestoneTeleportCost cost) {
		return costButton(cost, LodestoneTeleportAvailability.enabled());
	}

	private static ActionButton costButton(LodestoneTeleportCost cost, LodestoneTeleportAvailability availability) {
		return new ActionButton(
			new CommonButtonData(Component.literal(cost.label()).withStyle(availability.canTeleport() ? ChatFormatting.WHITE : ChatFormatting.GRAY), Optional.of(LodestoneText.serverCost(cost)), costButtonWidth()),
			Optional.empty()
		);
	}

	private static ActionButton pageButton(String id, String query, int page, String label, boolean active, int width) {
		Component component = Component.literal(label).withStyle(active ? ChatFormatting.AQUA : ChatFormatting.GRAY);
		if (!active) {
			return new ActionButton(new CommonButtonData(component, width), Optional.empty());
		}
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "page");
		payload.putString("id", id);
		payload.putString("query", query);
		payload.putInt("page", Math.max(0, page));
		return new ActionButton(
			new CommonButtonData(component, width),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static ActionButton navButton(String id, String query, int page, String label, boolean active, int width) {
		if (!LodestoneConfig.get().showVanillaDialogButtonNavigation) {
			return spacerButton(width);
		}
		return pageButton(id, query, page, label, active, width);
	}

	private static void addOrderedRow(List<ActionButton> buttons, ActionButton cost, ActionButton destination, ActionButton edit) {
		for (String token : LodestoneConfig.get().vanillaDialogColumnOrder.split(",")) {
			switch (token) {
				case "c" -> buttons.add(cost);
				case "d" -> buttons.add(destination);
				case "e" -> buttons.add(edit);
				default -> {
					buttons.add(cost);
					buttons.add(destination);
					buttons.add(edit);
					return;
				}
			}
		}
	}

	private static Component pageLink(String id, String query, int page, String label, boolean active) {
		Component component = Component.literal(label).withStyle(active ? ChatFormatting.AQUA : ChatFormatting.GRAY);
		if (!active) {
			return component;
		}
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "page");
		payload.putString("id", id);
		payload.putString("query", query);
		payload.putInt("page", Math.max(0, page));
		return component.copy().withStyle(style -> style
			.withUnderlined(true)
			.withClickEvent(new ClickEvent.Custom(LodestoneCustomActions.ACTION_ID, Optional.of(payload))));
	}

	private static ActionButton configSearchButton(String category) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "config_open");
		payload.putString("category", LodestoneConfigOptions.ALL);
		return new ActionButton(
			new CommonButtonData(LodestoneText.serverText("button.search", "Search location").withStyle(ChatFormatting.AQUA), CONFIG_BUTTON_WIDTH),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
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

	private static ActionButton permissionActionButton(Component label, String action, String key, String permission, String query) {
		return permissionActionButton(label, action, key, permission, query, CONFIG_BUTTON_WIDTH);
	}

	private static ActionButton permissionActionButton(Component label, String action, String key, String permission, String query, int width) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", action);
		payload.putString("key", key);
		payload.putString("permission", permission);
		payload.putString("query", query);
		return new ActionButton(
			new CommonButtonData(label, width),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static boolean isPermissionOption(String key) {
		return "player_permissions".equals(key) || "admin_permissions".equals(key);
	}

	private static Component configBody(String category, String query) {
		if (query == null || query.isBlank()) {
			return LodestoneText.serverText("config.server.body", "Search server config.\nWiki: %s", wikiUrl());
		}
		return LodestoneText.serverText("config.server.body.search", "Search server config.\nWiki: %s\nSearch: %s", wikiUrl(), query);
	}

	private static Component permissionBody(String key, String query) {
		Component title = LodestoneText.serverText("config.permission.body", "Toggle, add, or remove fallback permissions.");
		if (query == null || query.isBlank()) {
			return title;
		}
		return title.copy()
			.append(Component.literal("\n"))
			.append(LodestoneText.serverText("config.permission.search", "Search: %s", query));
	}

	private static Component configOptionLabel(LodestoneConfigOptions.Option option) {
		ChatFormatting valueColor = option.isDefault() ? ChatFormatting.WHITE : ChatFormatting.YELLOW;
		Component state = option.type() == LodestoneConfigOptions.Type.BOOLEAN
			? LodestoneText.serverText(Boolean.parseBoolean(option.currentValue()) ? "config.switch.on" : "config.switch.off", Boolean.parseBoolean(option.currentValue()) ? "ON" : "OFF")
			: Component.literal(truncate(option.currentValue(), 42));
		return LodestoneText.serverText(option.labelKey(), option.labelFallback()).withStyle(ChatFormatting.AQUA)
			.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
			.append(state.copy().withStyle(valueColor));
	}

	private static Component configOptionTooltip(LodestoneConfigOptions.Option option) {
		String acceptedValues = option.id().equals("player_permissions") || option.id().equals("admin_permissions")
			? String.format(Locale.ROOT, LodestoneText.serverPattern("config.permissions_wiki", "More info in the permissions wiki: %s"), permissionsWikiUrl())
			: LodestoneText.serverText("config.accepted_values", "Accepted: %s", LodestoneText.serverConfigAcceptedValues(option.id(), option.acceptedValues())).getString();
		return Component.literal(LodestoneText.serverConfigDescription(option.id(), option.description()))
			.append(Component.literal("\n"))
			.append(Component.literal(acceptedValues).withStyle(ChatFormatting.GRAY))
			.append(Component.literal("\n"))
			.append(LodestoneText.serverText("config.default", "Default: %s", option.defaultValue()).withStyle(option.isDefault() ? ChatFormatting.DARK_GRAY : ChatFormatting.YELLOW))
			.append(Component.literal("\n"))
			.append(LodestoneText.serverText("config.current", "Current: %s", truncate(option.currentValue(), 64)).withStyle(option.isDefault() ? ChatFormatting.GRAY : ChatFormatting.YELLOW));
	}

	private static Action renameAction(String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "rename");
		payload.putString("id", id);
		return new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload));
	}

	private static Action saveEditAction(String id, LodestoneVisibility visibility) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "save_edit");
		payload.putString("id", id);
		payload.putString("visibility", visibility.id());
		return new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload));
	}

	private static ActionButton modeButton(String id, LodestoneVisibility currentVisibility, LodestoneVisibility nextVisibility) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "edit_mode");
		payload.putString("id", id);
		payload.putString("visibility", nextVisibility.id());
		return new ActionButton(
			new CommonButtonData(LodestoneText.serverText("button.mode", "Mode: %s", visibilityValue(currentVisibility)).withStyle(visibilityColor(currentVisibility)), destinationButtonWidth()),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
	}

	private static List<LodestoneVisibility> allowedVisibilities(ServerPlayer player, LodestoneLocation location) {
		List<LodestoneVisibility> values = new ArrayList<>();
		values.add(location.visibility());
		for (LodestoneVisibility visibility : LodestoneVisibility.values()) {
			if (visibility != location.visibility() && LodestonePermissions.canSetVisibility(player, location, visibility)) {
				values.add(visibility);
			}
		}
		return values;
	}

	private static LodestoneVisibility nextVisibility(List<LodestoneVisibility> visibilities, LodestoneVisibility current) {
		int index = visibilities.indexOf(current);
		if (index < 0) {
			return visibilities.getFirst();
		}
		return visibilities.get((index + 1) % visibilities.size());
	}

	private static Component editBody(ServerPlayer player, LodestoneLocation location, LodestoneVisibility visibility) {
		MutableComponent body = LodestoneText.serverText("edit.body", "Change this lodestone name, visibility, or registration.")
			.copy()
			.append(Component.literal("\n"))
			.append(LodestoneText.serverText("visibility.current", "Visibility: %s", visibilityValue(visibility)).withStyle(visibilityColor(visibility)));
		if (!LodestonePermissions.canRename(player, location)) {
			body.append(Component.literal("\n"))
				.append(LodestoneText.serverText("error.no_permission.rename_specific", "You do not have permission to edit this lodestone name.").withStyle(ChatFormatting.RED));
		}
		return body;
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

	private static Component bodyText(ServerPlayer player, LodestoneLocation current, String query, boolean noResults, int page, int totalPages) {
		Component body;
		if (noResults && !query.isBlank()) {
			body = formatWithLeadingComponent(
				LodestoneText.serverPattern("menu.body.no_results", "From %s\nNo results for: %s"),
				displayNameWithVisibilityIcon(current),
				query
			);
		} else {
			body = formatWithLeadingComponent(
				LodestoneText.serverPattern("menu.body", "From %s"),
				displayNameWithVisibilityIcon(current)
			);
		}
		body = body.copy()
			.append(Component.literal("\n"))
			.append(LodestoneText.serverText("menu.coords", "Coords: %s", currentPosition(current)).withStyle(ChatFormatting.GRAY))
			.append(Component.literal("\n"))
			.append(LodestoneText.serverText("menu.owner", "Owner: %s", ownerName(current)).withStyle(ChatFormatting.GRAY));
		if (totalPages > 1) {
			body = body.copy().append(Component.literal("\n"));
			if (LodestoneConfig.get().showVanillaDialogHeaderNavigation) {
				body = body.copy()
					.append(pageLink(current.id(), query, page - 1, "\u00ab", page > 0))
					.append(Component.literal("  "));
			}
			body = body.copy().append(LodestoneText.serverText("client.page", "Page %s / %s", page + 1, totalPages).withStyle(ChatFormatting.GOLD));
			if (LodestoneConfig.get().showVanillaDialogHeaderNavigation) {
				body = body.copy()
					.append(Component.literal("  "))
					.append(pageLink(current.id(), query, page + 1, "\u00bb", page < totalPages - 1));
			}
		}
		if (LodestoneDiscovery.canSeeAll(player)) {
			return body.copy()
				.append(Component.literal("\n"))
				.append(LodestoneText.serverText("menu.viewing_all", "Admin view: showing all lodestones").withStyle(ChatFormatting.RED));
		}
		return body;
	}

	private static Component formatWithLeadingComponent(String pattern, Component first, Object... remainingArgs) {
		int placeholder = pattern.indexOf("%s");
		if (placeholder < 0) {
			return Component.literal(pattern);
		}
		Component result = Component.literal(pattern.substring(0, placeholder))
			.append(first);
		String suffix = pattern.substring(placeholder + 2);
		if (remainingArgs.length == 0) {
			return result.copy().append(Component.literal(suffix));
		}
		try {
			return result.copy().append(Component.literal(String.format(Locale.ROOT, suffix, remainingArgs)));
		} catch (IllegalArgumentException exception) {
			return result.copy().append(Component.literal(suffix));
		}
	}

	private static String wikiUrl() {
		return "es_es".equals(LodestoneConfig.get().serverLanguage) ? WIKI_URL + "/es/Inicio" : WIKI_URL;
	}

	private static String permissionsWikiUrl() {
		return "es_es".equals(LodestoneConfig.get().serverLanguage) ? WIKI_URL + "/es/Permisos" : WIKI_URL + "/Permissions";
	}

	private static Component visibilityLabel(LodestoneVisibility visibility) {
		return LodestoneText.serverText("visibility." + visibility.id(), visibility.id()).withStyle(switch (visibility) {
			case PRIVATE -> ChatFormatting.GRAY;
			case DISCOVERABLE -> ChatFormatting.AQUA;
			case GLOBAL -> ChatFormatting.GREEN;
		});
	}

	private static Component visibilityValue(LodestoneVisibility visibility) {
		return LodestoneText.serverText("visibility.value." + visibility.id(), visibility.id());
	}

	private static ChatFormatting visibilityColor(LodestoneVisibility visibility) {
		return switch (visibility) {
			case PRIVATE -> ChatFormatting.GOLD;
			case DISCOVERABLE -> ChatFormatting.AQUA;
			case GLOBAL -> ChatFormatting.GREEN;
		};
	}

	private static boolean canEdit(ServerPlayer player, LodestoneLocation location) {
		return LodestonePermissions.canRename(player, location)
			|| LodestonePermissions.canRemove(player, location)
			|| LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.PRIVATE)
			|| LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.DISCOVERABLE)
			|| LodestonePermissions.canSetVisibility(player, location, LodestoneVisibility.GLOBAL);
	}

	private static Component destinationLabel(LodestoneLocation destination) {
		String name = destination.displayName() + destinationSuffix(destination);
		int prefixLength = destination.global() || destination.privateWarp() ? 2 : 0;
		return Component.empty()
			.append(visibilityIcon(destination))
			.append(Component.literal(truncate(name, destinationLabelWidth() - prefixLength)));
	}

	private static Component destinationTooltip(LodestoneLocation destination) {
		return Component.literal(destination.displayName())
			.append(Component.literal("\n"))
			.append(currentPosition(destination).copy().withStyle(ChatFormatting.GRAY));
	}

	private static Component displayNameWithVisibilityIcon(LodestoneLocation location) {
		return Component.empty()
			.append(visibilityIcon(location))
			.append(Component.literal(location.displayName()));
	}

	private static Component currentPosition(LodestoneLocation location) {
		return Component.literal(location.pos().getX() + ", " + location.pos().getY() + ", " + location.pos().getZ() + ", ")
			.append(LodestoneText.serverDimension(location.dimension()));
	}

	private static String ownerName(LodestoneLocation location) {
		if (!LodestoneConfig.get().resolveOwnerNames) {
			return "unknown";
		}
		return location.ownerName() == null || location.ownerName().isBlank() ? "unknown" : location.ownerName();
	}

	private static String destinationSuffix(LodestoneLocation location) {
		LodestoneConfig config = LodestoneConfig.get();
		if (!config.showVanillaDialogDestinationSuffix || config.vanillaDialogDestinationSuffix.isBlank()) {
			return "";
		}
		String suffix = config.vanillaDialogDestinationSuffix
			.replace("{x}", String.valueOf(location.pos().getX()))
			.replace("{y}", String.valueOf(location.pos().getY()))
			.replace("{z}", String.valueOf(location.pos().getZ()))
			.replace("{dimension}", LodestoneText.serverDimension(location.dimension()).getString())
			.replace("{owner}", ownerName(location));
		return suffix.isBlank() ? "" : " " + suffix;
	}

	private static Component visibilityIcon(LodestoneLocation location) {
		if (location.global()) {
			return Component.literal("\ud83c\udf10 ").withStyle(ChatFormatting.GREEN);
		}
		if (location.privateWarp()) {
			return Component.literal("\ud83d\udd12 ").withStyle(ChatFormatting.GOLD);
		}
		return Component.empty();
	}

	private static String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, Math.max(0, maxLength - 3)) + "...";
	}

	private static int destinationButtonWidth() {
		return Math.max(80, LodestoneConfig.get().vanillaDialogDestinationColumnWidth);
	}

	private static int costButtonWidth() {
		return Math.max(30, LodestoneConfig.get().vanillaDialogCostColumnWidth);
	}

	private static int editButtonWidth() {
		return Math.max(20, LodestoneConfig.get().vanillaDialogEditColumnWidth);
	}

	private static int destinationLabelWidth() {
		return Math.max(12, (destinationButtonWidth() - 18) / 6);
	}

	private static void send(ServerPlayer player, Dialog dialog) {
		player.connection.send(new ClientboundShowDialogPacket(Holder.direct(dialog)));
	}
}
