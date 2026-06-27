# Interfaces

**Última actualización:** 2026-06-27

## UI Vanilla

La UI vanilla es la UI de Dialogs de Minecraft enviada por el servidor.

Se usa cuando el jugador no tiene Lodestone Warps instalado en el cliente.

Incluye:

- botones de destinos
- búsqueda
- renombrado
- validación server-side
- no requiere mod en cliente

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
- íconos del costo
- botones de edición
- actualización instantánea después de renombrar

El servidor detecta automáticamente si el cliente soporta la UI de mod.

Los jugadores sin el mod siguen usando la UI vanilla.
