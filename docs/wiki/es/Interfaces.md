# Interfaces

**Última actualización:** 2026-07-01

Lodestone Warps tiene dos interfaces. El servidor elige automáticamente según si el jugador tiene instalado el mod en el cliente.

## UI Vanilla

La UI vanilla es la UI de Dialogs de Minecraft enviada por el servidor.

Se usa cuando el jugador no tiene Lodestone Warps instalado en el cliente.

Incluye:

- botones de destinos
- input de búsqueda
- flujo de edición/renombrado
- acciones de visibilidad cuando el jugador tiene permisos
- UI vanilla de config para admins
- validación server-side
- no requiere mod en cliente

La UI vanilla puede mostrar texto con color y símbolos simples, pero no puede renderizar layouts custom completos ni iconos de items como la UI de mod.

Como los clientes vanilla no tienen los archivos de idioma del mod, los textos dependen de:

```json
"serverLanguage": "en_us"
```

Para español:

```json
"serverLanguage": "es_es"
```

## UI de Mod

La UI de mod es la pantalla custom que aparece cuando el jugador tiene Lodestone Warps instalado.

Incluye:

- tabla de destinos
- búsqueda
- paginación
- iconos de costo por XP/item
- botones de edición
- iconos de visibilidad:
  - 🌐 global
  - 🔒 privada
- icono de visibilidad en el nombre de la Lodestone actual
- actualización instantánea después de guardar/remover

## Pantalla de Edición del Mod

La pantalla de edición del mod guarda cambios en staged:

```text
[textbox]
[Mode][Remove]

[Save][Cancel]
```

- `Mode` cicla localmente entre los modos de visibilidad permitidos para el jugador.
- `Save` aplica nombre y visibilidad juntos.
- `Cancel` cierra sin guardar cambios locales.
- `Remove` desvincula la Lodestone inmediatamente cuando está permitido.

El servidor vuelve a validar permisos antes de aplicar cualquier cambio.

## Pantalla de Config con Mod Menu

Cuando Mod Menu está instalado, Lodestone Warps expone un editor de config client-side.

Esta pantalla edita el archivo local. Los servidores remotos siguen usando su propia config server-side.

## Notas

Los jugadores sin el mod siguen soportados mediante la UI vanilla.
