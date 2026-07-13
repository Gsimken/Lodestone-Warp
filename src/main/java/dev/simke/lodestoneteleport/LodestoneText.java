package dev.simke.lodestoneteleport;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Locale;

public final class LodestoneText {
	private LodestoneText() {
	}

	public static MutableComponent title() {
		return text("title", "Lodestone Warps");
	}

	public static MutableComponent text(String key, String fallback, Object... args) {
		return Component.translatableWithFallback("text.lodestone_teleport." + key, fallbackFor(key, fallback), args);
	}

	public static MutableComponent serverTitle() {
		return serverText("title", "Lodestone Warps");
	}

	public static MutableComponent serverText(String key, String fallback, Object... args) {
		return Component.literal(format(fallbackFor(key, fallback), args));
	}

	public static String serverPattern(String key, String fallback) {
		return fallbackFor(key, fallback);
	}

	public static String serverConfigDescription(String id, String fallback) {
		if (!"es_es".equals(LodestoneConfig.get().serverLanguage)) {
			return fallback;
		}
		return switch (id) {
			case "cost_type" -> "Tipo de costo cobrado por cada teleport.";
			case "cost_item" -> "Id del item cobrado por cada teleport cuando el costo usa items.";
			case "base_cost" -> "Costo mínimo para un teleport.";
			case "blocks_per_extra_cost" -> "Agrega un nivel de costo cada cierta cantidad de bloques en la misma dimensión.";
			case "cross_dimension_multiplier" -> "Multiplica el costo calculado al teletransportarse entre dimensiones.";
			case "max_cost" -> "Limita el costo final del teleport.";
			case "allow_cross_dimension" -> "Permite teleports entre Overworld, Nether, End y otras dimensiones.";
			case "allow_personal_lodestones" -> "Permite lodestones personales privadas del jugador que las coloca.";
			case "default_lodestone_visibility" -> "Visibilidad asignada a las lodestones nuevas cuando el jugador puede crear ese tipo.";
			case "max_lodestones_global" -> "Máximo de lodestones registradas para todo el servidor.";
			case "max_lodestones_per_player" -> "Máximo de lodestones registradas por cada jugador.";
			case "sneak_place_only" -> "Solo registra lodestones nuevas cuando el jugador las coloca agachado.";
			case "auto_register_untracked" -> "Registra lodestones antiguas o desvinculadas al hacer click derecho normal.";
			case "teleport_source_range" -> "Distancia horizontal maxima alrededor de una lodestone registrada.";
			case "teleport_source_y_range" -> "Distancia vertical maxima alrededor de una lodestone registrada.";
			case "teleport_cast_seconds" -> "Segundos que el jugador debe quedarse quieto antes del teleport.";
			case "teleport_cast_move_tolerance" -> "Movimiento máximo permitido durante la canalización del teleport.";
			case "teleport_cooldown_seconds" -> "Cooldown después de un teleport exitoso.";
			case "max_dialog_destinations" -> "Máximo de destinos mostrados en la UI vanilla Dialog.";
			case "vanilla_dialog_destination_column_width" -> "Ancho de la columna de destino en la UI vanilla Dialog.";
			case "vanilla_dialog_cost_column_width" -> "Ancho de la columna de costo en la UI vanilla Dialog.";
			case "vanilla_dialog_edit_column_width" -> "Ancho de la columna editar/espacio en la UI vanilla Dialog.";
			case "vanilla_dialog_column_order" -> "Orden de columnas en la grilla de destinos de la UI vanilla Dialog.";
			case "show_vanilla_dialog_header_navigation" -> "Muestra flechas de página clickeables junto al texto de página en la UI vanilla.";
			case "show_vanilla_dialog_button_navigation" -> "Muestra botones de flecha de página junto a Buscar y Editar este warp en la grilla vanilla.";
			case "show_vanilla_dialog_destination_suffix" -> "Muestra información extra configurada después del nombre del destino en la UI vanilla.";
			case "vanilla_dialog_destination_suffix" -> "Patrón de sufijo añadido a los nombres de destino cuando está activado.";
			case "teleport_effects" -> "Activa sonidos y partículas alrededor de las acciones de teleport.";
			case "vanilla_teleport_effect" -> "Preset de efecto usado para jugadores sin el mod de cliente.";
			case "mod_teleport_effect" -> "Preset de efecto usado para jugadores con el mod de cliente instalado.";
			case "network_mode" -> "Controla qué lodestones pueden ver y usar los jugadores.";
			case "resolve_owner_names" -> "Envía nombres de dueños guardados a las interfaces de Lodestone Warps.";
			case "player_permissions" -> "Permisos default usados para todos los jugadores cuando ningún administrador de permisos responde.";
			case "admin_permissions" -> "Permisos default usados para admins OP/gamemaster cuando ningún administrador de permisos responde.";
			case "command_name" -> "Comando principal registrado por Lodestone Warps. Requiere reiniciar el servidor.";
			case "fallback_command_name" -> "Comando fallback disponible cuando el comando principal entra en conflicto. Requiere reiniciar.";
			case "server_language" -> "Idioma fallback para textos generados por servidor y visibles para clientes vanilla.";
			case "pause_game_in_singleplayer_ui" -> "Pausa mundos singleplayer mientras la UI de teleport de cliente est\u00e1 abierta. Servidores dedicados ignoran esta opci\u00f3n.";
			default -> fallback;
		};
	}

	public static String serverConfigAcceptedValues(String id, String fallback) {
		if (id.equals("player_permissions") || id.equals("admin_permissions")) {
			return serverPattern("config.permissions_wiki", "See the permissions wiki: %s");
		}
		if (!"es_es".equals(LodestoneConfig.get().serverLanguage)) {
			return fallback;
		}
		return switch (id) {
			case "cost_type" -> "xp_levels o item.";
			case "cost_item" -> "Identificador de item, por ejemplo minecraft:diamond.";
			case "base_cost", "teleport_source_range", "teleport_source_y_range", "teleport_cast_seconds", "teleport_cooldown_seconds" -> "Número entero, 0 o mayor.";
			case "blocks_per_extra_cost" -> "Número entero, 0 desactiva el escalado por distancia.";
			case "cross_dimension_multiplier", "teleport_cast_move_tolerance" -> "Número decimal, 0 o mayor.";
			case "max_cost", "max_lodestones_global", "max_lodestones_per_player" -> "Número entero, 0 significa sin límite.";
			case "allow_cross_dimension", "allow_personal_lodestones", "sneak_place_only", "auto_register_untracked", "show_vanilla_dialog_destination_suffix", "teleport_effects", "resolve_owner_names" -> "true o false. false muestra dueños como unknown.";
			case "default_lodestone_visibility" -> "private, discoverable o global.";
			case "max_dialog_destinations" -> "Número entero, 1 o mayor.";
			case "vanilla_dialog_destination_column_width" -> "Número entero, 80 a 500.";
			case "vanilla_dialog_cost_column_width" -> "Número entero, 30 a 180.";
			case "vanilla_dialog_edit_column_width" -> "Número entero, 20 a 120.";
			case "vanilla_dialog_column_order" -> "Separado por comas: c,d,e. c=costo, d=destino, e=editar.";
			case "show_vanilla_dialog_header_navigation", "show_vanilla_dialog_button_navigation" -> "true o false.";
			case "vanilla_dialog_destination_suffix" -> "Placeholders: {x}, {y}, {z}, {dimension}, {owner}.";
			case "vanilla_teleport_effect", "mod_teleport_effect" -> "none, off, end o lodestone.";
			case "network_mode" -> "all o discover.";
			case "command_name", "fallback_command_name" -> "Letras, números, guion bajo, guion o punto. Requiere reiniciar.";
			case "server_language" -> "en_us o es_es.";
			case "pause_game_in_singleplayer_ui" -> "true o false.";
			default -> fallback;
		};
	}

	public static Component dimension(ResourceKey<Level> dimension) {
		Identifier id = dimension.identifier();
		if (id.getNamespace().equals("minecraft")) {
			return text("dimension." + id.getPath(), fallbackDimension(id.getPath()));
		}
		return Component.literal(id.getNamespace() + ":" + id.getPath());
	}

	public static Component serverDimension(ResourceKey<Level> dimension) {
		Identifier id = dimension.identifier();
		if (id.getNamespace().equals("minecraft")) {
			return serverText("dimension." + id.getPath(), fallbackDimension(id.getPath()));
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

	public static Component serverCost(LodestoneTeleportCost cost) {
		if (cost.amount() <= 0) {
			return serverText("cost.free", "free");
		}
		if (cost.usesXpLevels()) {
			return serverText("cost.xp_levels", "%s levels", cost.amount());
		}
		return serverText("cost.item", "%sx %s", cost.amount(), item(cost));
	}

	private static String format(String pattern, Object... args) {
		Object[] plainArgs = new Object[args.length];
		for (int index = 0; index < args.length; index++) {
			plainArgs[index] = args[index] instanceof Component component ? component.getString() : args[index];
		}
		try {
			return String.format(Locale.ROOT, pattern, plainArgs);
		} catch (IllegalArgumentException exception) {
			return pattern;
		}
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
			return spanishFallbackFor(key, fallback);
		}
		return englishFallbackFor(key, fallback);
	}

	private static String englishFallbackFor(String key, String fallback) {
		return switch (key) {
			case "menu.body" -> "From %s";
			case "menu.body.no_results" -> "From %s\nNo results for: %s";
			case "menu.coords" -> "Coords: %s";
			case "menu.owner" -> "Owner: %s";
			case "menu.viewing_all" -> "Admin view: showing all lodestones";
			case "input.search" -> "Search";
			case "input.name" -> "Name";
			case "button.search" -> "Search location";
			case "button.rename" -> "Rename %s";
			case "button.rename_short" -> "[\u270e]";
			case "button.rename_current" -> "Edit this warp";
			case "button.remove" -> "[X]";
			case "button.remove_current" -> "Unlink lodestone";
			case "button.save" -> "Save";
			case "button.mode" -> "Mode: %s";
			case "button.teleport" -> "[TP]";
			case "edit.title" -> "Edit lodestone";
			case "edit.body" -> "Change this lodestone name, visibility, or registration.";
			case "edit.saved" -> "Lodestone changes saved.";
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
			case "visibility.private" -> "Make private";
			case "visibility.discoverable" -> "Make discoverable";
			case "visibility.global" -> "Make global";
			case "visibility.value.private" -> "private";
			case "visibility.value.discoverable" -> "discoverable";
			case "visibility.value.global" -> "global";
			case "visibility.current" -> "Visibility: %s";
			case "visibility.changed" -> "Lodestone visibility changed to %s.";
			case "discover.granted" -> "Granted %s discovery of %s.";
			case "discover.revoked" -> "Revoked %s discovery of %s.";
			case "discover.granted_all" -> "Granted %s discovery of all discoverable and global lodestones (%s new).";
			case "discover.granted_all_with_private" -> "Granted %s discovery of all lodestones, including private ones (%s new).";
			case "discover.revoked_all" -> "Revoked %s discovery of all lodestones (%s removed).";
			case "discover.list_header" -> "%s has discovered:";
			case "discover.list_empty" -> "No discovered lodestones.";
			case "discover.who_header" -> "%s has been discovered by:";
			case "discover.who_empty" -> "No players have discovered this lodestone.";
			case "list.empty" -> "No lodestones registered.";
			case "list.header" -> "Registered lodestones:";
			case "list.entry" -> "- %s: %s (%s)";
			case "error.missing_destination" -> "That destination no longer exists.";
			case "error.duplicate_destination_name" -> "More than one lodestone is named \"%s\". Choose one:";
			case "error.dimension_unloaded" -> "The destination dimension is not loaded.";
			case "error.cross_dimension_disabled" -> "Cross-dimension teleport is disabled.";
			case "error.destination_removed" -> "The destination no longer has a lodestone and was removed.";
			case "error.lodestone_not_registered" -> "This lodestone is not registered.";
			case "error.private_registered" -> "This lodestone is already registered and private; it cannot be registered by another player.";
			case "error.not_discovered" -> "You have not discovered that lodestone.";
			case "error.max_lodestones_global" -> "The server has reached the maximum number of registered lodestones.";
			case "error.max_lodestones_player" -> "You have reached your maximum number of registered lodestones.";
			case "error.need_cost" -> "You need %s.";
			case "error.need_near_lodestone" -> "You must be near a registered lodestone to teleport.";
			case "error.cooldown" -> "You must wait %s seconds before teleporting again.";
			case "error.cooldown_reopen" -> "You must wait %s seconds before teleporting again. Reopen this UI after the cooldown.";
			case "teleport.cast_start" -> "Casting teleport... stand still for %s seconds.";
			case "teleport.cast_cancelled" -> "Teleport cancelled: you moved.";
			case "teleport.cast_already" -> "You are already casting a teleport.";
			case "error.no_permission.use" -> "You do not have permission to use lodestones.";
			case "error.no_permission.rename" -> "You do not have permission to rename lodestones.";
			case "error.no_permission.rename_specific" -> "You do not have permission to edit this lodestone name.";
			case "error.no_permission.create" -> "You do not have permission to register lodestones.";
			case "error.no_permission.remove" -> "You do not have permission to remove registered lodestones.";
			case "error.no_permission.global" -> "You do not have permission to manage global lodestones.";
			case "error.no_permission.visibility" -> "You do not have permission to change that lodestone visibility.";
			case "error.lodestone_not_found" -> "I could not find that lodestone.";
			case "error.invalid_visibility" -> "Invalid visibility. Use private, discoverable, or global.";
			case "error.invalid_action" -> "Invalid lodestone action.";
			case "error.action_failed" -> "Could not run the lodestone action.";
			case "config.warning.permissions.all_with_player_discover" -> "Network mode is all, but players have lodestone_teleport.mode.discover. That permission forces discovery rules for players.";
			case "config.warning.permissions.all_with_admin_discover" -> "Network mode is all, but admins have lodestone_teleport.mode.discover. That permission forces discovery rules unless they also have lodestone_teleport.mode.all.";
			case "config.server.body" -> "Search server config.\nWiki: %s";
			case "config.server.body.search" -> "Search server config.\nWiki: %s\nSearch: %s";
			case "config.permission.title" -> "Permissions: %s";
			case "config.permission.body" -> "Toggle, add, or remove fallback permissions.";
			case "config.permission.search" -> "Search: %s";
			case "config.permission.add" -> "Add permission";
			case "config.permission.input" -> "Permission";
			case "config.permission.remove" -> "Remove";
			case "config.accepted_values" -> "Accepted: %s";
			case "config.permissions_wiki" -> "More info in the permissions wiki: %s";
			case "config.button.default" -> "D";
			default -> fallback;
		};
	}

	private static String spanishFallbackFor(String key, String fallback) {
		return switch (key) {
			case "menu.body" -> "Desde %s";
			case "menu.body.no_results" -> "Desde %s\nSin resultados para: %s";
			case "menu.coords" -> "Coords: %s";
			case "menu.owner" -> "Due\u00f1o: %s";
			case "menu.viewing_all" -> "Vista admin: mostrando todas las lodestones";
			case "input.search" -> "Buscar";
			case "input.name" -> "Nombre";
			case "button.search" -> "Buscar ubicación";
			case "button.rename" -> "Renombrar %s";
			case "button.rename_short" -> "[\u270e]";
			case "button.rename_current" -> "Editar este warp";
			case "button.remove" -> "[X]";
			case "button.remove_current" -> "Desvincular lodestone";
			case "button.save" -> "Guardar";
			case "button.mode" -> "Modo: %s";
			case "button.teleport" -> "[TP]";
			case "edit.title" -> "Editar lodestone";
			case "edit.body" -> "Cambia el nombre, visibilidad o registro de esta lodestone.";
			case "edit.saved" -> "Cambios de lodestone guardados.";
			case "rename.title" -> "Nombrar lodestone";
			case "rename.body" -> "Elige un nombre para esta lodestone.";
			case "cost.free" -> "gratis";
			case "cost.xp_levels" -> "%s niveles";
			case "cost.item" -> "%sx %s";
			case "dimension.overworld" -> "overworld";
			case "dimension.the_nether" -> "nether";
			case "dimension.the_end" -> "end";
			case "arrived" -> "Has llegado a \"%s\" (%s, %s, %s, %s).";
			case "renamed" -> "Lodestone renombrada a %s.";
			case "removed" -> "Warp de lodestone desvinculado: %s.";
			case "registered" -> "Lodestone registrada: %s";
			case "discovered" -> "Lodestone descubierta: %s";
			case "global.badge" -> "[Global]";
			case "global.enabled" -> "Lodestone marcada como global: %s";
			case "global.disabled" -> "Lodestone ya no es global: %s";
			case "visibility.private" -> "Hacer privada";
			case "visibility.discoverable" -> "Hacer descubrible";
			case "visibility.global" -> "Hacer global";
			case "visibility.value.private" -> "privada";
			case "visibility.value.discoverable" -> "descubrible";
			case "visibility.value.global" -> "global";
			case "visibility.current" -> "Visibilidad: %s";
			case "visibility.changed" -> "Visibilidad de la lodestone cambiada a %s.";
			case "discover.granted" -> "%s ahora descubrió %s.";
			case "discover.revoked" -> "%s ya no tiene descubierto %s.";
			case "discover.granted_all" -> "%s ahora descubrió todas las lodestones descubribles y globales (%s nuevas).";
			case "discover.granted_all_with_private" -> "%s ahora descubrió todas las lodestones, incluyendo privadas (%s nuevas).";
			case "discover.revoked_all" -> "%s ya no tiene ninguna lodestone descubierta (%s removidas).";
			case "discover.list_header" -> "%s ha descubierto:";
			case "discover.list_empty" -> "No hay lodestones descubiertas.";
			case "discover.who_header" -> "%s fue descubierta por:";
			case "discover.who_empty" -> "Ningún jugador ha descubierto esta lodestone.";
			case "list.empty" -> "No hay lodestones registradas.";
			case "list.header" -> "Lodestones registradas:";
			case "list.entry" -> "- %s: %s (%s)";
			case "error.missing_destination" -> "Ese destino ya no existe.";
			case "error.duplicate_destination_name" -> "Hay más de una lodestone llamada \"%s\". Elige una:";
			case "error.dimension_unloaded" -> "La dimensión del destino no está cargada.";
			case "error.cross_dimension_disabled" -> "El teleport entre dimensiones está desactivado.";
			case "error.destination_removed" -> "El destino ya no tiene una lodestone y fue eliminado.";
			case "error.lodestone_not_registered" -> "Esta lodestone no está registrada.";
			case "error.private_registered" -> "Esta lodestone ya está registrada y es privada; no puede ser registrada por otra persona.";
			case "error.not_discovered" -> "No has descubierto esa lodestone.";
			case "error.max_lodestones_global" -> "El servidor alcanzó el máximo de lodestones registradas.";
			case "error.max_lodestones_player" -> "Alcanzaste tu máximo de lodestones registradas.";
			case "error.need_cost" -> "Necesitas %s.";
			case "error.need_near_lodestone" -> "Debes estar cerca de una lodestone registrada para teletransportarte.";
			case "error.cooldown" -> "Debes esperar %s segundos antes de teletransportarte otra vez.";
			case "error.cooldown_reopen" -> "Debes esperar %s segundos antes de teletransportarte otra vez. Vuelve a abrir esta UI cuando termine el cooldown.";
			case "teleport.cast_start" -> "Canalizando teleport... quédate quieto por %s segundos.";
			case "teleport.cast_cancelled" -> "Teleport cancelado: te moviste.";
			case "teleport.cast_already" -> "Ya estás canalizando un teleport.";
			case "error.no_permission.use" -> "No tienes permiso para usar lodestones.";
			case "error.no_permission.rename" -> "No tienes permiso para renombrar lodestones.";
			case "error.no_permission.rename_specific" -> "No tienes permiso para editar el nombre de esta lodestone.";
			case "error.no_permission.create" -> "No tienes permiso para registrar lodestones.";
			case "error.no_permission.remove" -> "No tienes permiso para remover lodestones registradas.";
			case "error.no_permission.config" -> "No tienes permiso para configurar Lodestone Warps.";
			case "error.no_permission.global" -> "No tienes permiso para administrar lodestones globales.";
			case "error.no_permission.visibility" -> "No tienes permiso para cambiar la visibilidad de esa lodestone.";
			case "error.lodestone_not_found" -> "No encontré esa lodestone.";
			case "error.invalid_visibility" -> "Visibilidad inválida. Usa private, discoverable o global.";
			case "error.invalid_action" -> "Acción de lodestone inválida.";
			case "error.action_failed" -> "No se pudo ejecutar la acción de lodestone.";
			case "config.warning.permissions.all_with_player_discover" -> "El network mode es all, pero los jugadores tienen lodestone_teleport.mode.discover. Ese permiso fuerza reglas de discovery para jugadores.";
			case "config.warning.permissions.all_with_admin_discover" -> "El network mode es all, pero los admins tienen lodestone_teleport.mode.discover. Ese permiso fuerza reglas de discovery salvo que también tengan lodestone_teleport.mode.all.";
			case "client.page" -> "Página %s / %s";
			case "client.page.previous" -> "Anterior";
			case "client.page.next" -> "Siguiente";
			case "config.page.all" -> "Todo";
			case "config.page.cost" -> "Costo";
			case "config.page.registration" -> "Registro";
			case "config.page.teleport" -> "Teleport";
			case "config.page.advanced" -> "Avanzado";
			case "config.field.cost_type" -> "Tipo de costo";
			case "config.field.cost_item" -> "Item de costo";
			case "config.field.base_cost" -> "Costo base";
			case "config.field.blocks_per_extra_cost" -> "Bloques por costo extra";
			case "config.field.cross_dimension_multiplier" -> "Multiplicador entre dimensiones";
			case "config.field.max_cost" -> "Costo máximo";
			case "config.field.allow_cross_dimension" -> "Permitir entre dimensiones";
			case "config.field.allow_personal_lodestones" -> "Permitir lodestones personales";
			case "config.field.default_lodestone_visibility" -> "Visibilidad default de lodestone";
			case "config.field.max_lodestones_global" -> "Máximo global de lodestones";
			case "config.field.max_lodestones_per_player" -> "Máximo de lodestones por jugador";
			case "config.field.sneak_place_only" -> "Solo colocar agachado";
			case "config.field.auto_register_untracked" -> "Auto-registrar no registradas";
			case "config.field.teleport_source_range" -> "Rango de origen";
			case "config.field.teleport_source_y_range" -> "Rango Y de origen";
			case "config.field.teleport_cast_seconds" -> "Segundos de canalización";
			case "config.field.teleport_cast_move_tolerance" -> "Tolerancia de movimiento";
			case "config.field.teleport_cooldown_seconds" -> "Segundos de cooldown";
			case "config.field.max_dialog_destinations" -> "Destinos en UI vanilla";
			case "config.field.vanilla_dialog_destination_column_width" -> "Ancho destino UI vanilla";
			case "config.field.vanilla_dialog_cost_column_width" -> "Ancho costo UI vanilla";
			case "config.field.vanilla_dialog_edit_column_width" -> "Ancho editar UI vanilla";
			case "config.field.vanilla_dialog_column_order" -> "Orden columnas UI vanilla";
			case "config.field.show_vanilla_dialog_header_navigation" -> "Flechas superiores UI vanilla";
			case "config.field.show_vanilla_dialog_button_navigation" -> "Botones de flecha UI vanilla";
			case "config.field.show_vanilla_dialog_destination_suffix" -> "Mostrar sufijo destino UI vanilla";
			case "config.field.vanilla_dialog_destination_suffix" -> "Sufijo destino UI vanilla";
			case "config.field.teleport_effects" -> "Efectos de teleport";
			case "config.field.vanilla_teleport_effect" -> "Efecto vanilla";
			case "config.field.mod_teleport_effect" -> "Efecto con mod";
			case "config.field.network_mode" -> "Modo de red";
			case "config.field.resolve_owner_names" -> "Resolver nombres de dueños";
			case "config.field.player_permissions" -> "Permisos de jugadores";
			case "config.field.admin_permissions" -> "Permisos de admins";
			case "config.field.command_name" -> "Nombre del comando";
			case "config.field.fallback_command_name" -> "Comando fallback";
			case "config.field.server_language" -> "Idioma del servidor";
			case "config.field.pause_game_in_singleplayer_ui" -> "Pausar juego en UI singleplayer";
			case "config.server.title" -> "Config del servidor";
			case "config.server.body" -> "Busca en la configuración del servidor.\nWiki: %s";
			case "config.server.body.search" -> "Busca en la configuración del servidor.\nWiki: %s\nBúsqueda: %s";
			case "config.permission.title" -> "Permisos: %s";
			case "config.permission.body" -> "Activa, desactiva, agrega o elimina permisos fallback.";
			case "config.permission.search" -> "Búsqueda: %s";
			case "config.permission.add" -> "Agregar permiso";
			case "config.permission.input" -> "Permiso";
			case "config.permission.remove" -> "Eliminar";
			case "config.server.button.reload" -> "Recargar desde disco";
			case "config.server.edit_title" -> "Editar %s";
			case "config.server.input.value" -> "Valor";
			case "config.default" -> "Default: %s";
			case "config.current" -> "Actual: %s";
			case "config.accepted_values" -> "Acepta: %s";
			case "config.permissions_wiki" -> "Más info en la wiki de permisos: %s";
			case "config.button.default" -> "D";
			case "config.switch.on" -> "ON";
			case "config.switch.off" -> "OFF";
			default -> fallback;
		};
	}
}
