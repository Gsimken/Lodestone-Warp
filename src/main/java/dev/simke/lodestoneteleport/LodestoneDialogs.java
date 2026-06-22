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
	private static final int DESTINATION_BUTTON_WIDTH = 300;
	private static final int GRID_COLUMNS = 1;
	private static final int DESTINATION_LABEL_WIDTH = 30;

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

		buttons.add(searchButton(current.id()));

		for (LodestoneLocation destination : data.all()) {
			if (destination.id().equals(current.id())) {
				continue;
			}
			if (!matches(destination, cleanQuery)) {
				continue;
			}
			if (buttons.size() > limit) {
				break;
			}
			LodestoneTeleportCost cost = LodestoneTeleportCost.between(player, destination);
			buttons.add(customButton(Component.literal(destinationLabel(destination, cost)), Optional.of(LodestoneText.cost(cost)), "tp", destination.id()));
		}
		buttons.add(customButton(LodestoneText.text("button.rename_current", "Renombrar esta lodestone").withStyle(ChatFormatting.GOLD), "edit", current.id()));

		CommonDialogData common = new CommonDialogData(
			LodestoneText.title(),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(bodyText(current, cleanQuery, buttons.size() <= 2), 300)),
			List.of(new Input("query", new TextInput(300, LodestoneText.text("input.search", "Buscar"), true, cleanQuery, 48, Optional.empty())))
		);
		send(player, new MultiActionDialog(common, buttons, Optional.empty(), GRID_COLUMNS));
	}

	public static void showRename(ServerPlayer player, LodestoneLocation location) {
		CommonDialogData common = new CommonDialogData(
			LodestoneText.text("rename.title", "Nombrar lodestone"),
			Optional.empty(),
			true,
			false,
			DialogAction.CLOSE,
			List.of(new PlainMessage(LodestoneText.text("rename.body", "Elige un nombre para esta lodestone."), 300)),
			List.of(new Input("name", new TextInput(300, LodestoneText.text("input.name", "Nombre"), true, location.displayName(), 48, Optional.empty())))
		);

		ActionButton confirm = new ActionButton(
			new CommonButtonData(LodestoneText.text("button.save", "Guardar"), DESTINATION_BUTTON_WIDTH),
			Optional.of(renameAction(location.id()))
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
			List.<DialogBody>of(new PlainMessage(Component.literal(message), 300)),
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
		CompoundTag payload = new CompoundTag();
		payload.putString("action", action);
		payload.putString("id", id);
		return new ActionButton(
			new CommonButtonData(label, tooltip, DESTINATION_BUTTON_WIDTH),
			Optional.of(new StaticAction(new ClickEvent.Custom(LodestoneCustomActions.ACTION_ID, Optional.of(payload))))
		);
	}

	private static ActionButton searchButton(String id) {
		CompoundTag payload = new CompoundTag();
		payload.putString("action", "search");
		payload.putString("id", id);
		return new ActionButton(
			new CommonButtonData(LodestoneText.text("button.search", "Buscar ubicacion").withStyle(ChatFormatting.AQUA), DESTINATION_BUTTON_WIDTH),
			Optional.of(new CustomAll(LodestoneCustomActions.ACTION_ID, Optional.of(payload)))
		);
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
			return LodestoneText.text("menu.body.no_results", "Desde %s\nSin resultados para: %s", current.displayName(), query);
		}
		return LodestoneText.text("menu.body", "Desde %s", current.displayName());
	}

	private static String destinationLabel(LodestoneLocation destination, LodestoneTeleportCost cost) {
		String name = truncate(destination.displayName(), DESTINATION_LABEL_WIDTH - 6);
		String costLabel = cost.label();
		int padding = Math.max(2, DESTINATION_LABEL_WIDTH - name.length() - costLabel.length());
		return name + " ".repeat(padding) + costLabel;
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
