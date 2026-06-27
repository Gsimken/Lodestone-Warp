# Configuración

**Última actualización:** 2026-06-27

La configuración se genera en el primer inicio:

```text
config/lodestone_teleport.json
```

## Ejemplo

```json
{
  "costItem": "minecraft:diamond",
  "baseCost": 1,
  "blocksPerExtraCost": 500,
  "crossDimensionMultiplier": 2.0,
  "maxCost": 64,
  "allowCrossDimension": true,
  "maxDialogDestinations": 24,
  "teleportSourceRange": 8,
  "requirePermissions": true,
  "commandName": "warp",
  "fallbackCommandName": "lodestone_warp",
  "serverLanguage": "en_us"
}
```

## Idioma del Servidor

`serverLanguage` controla los textos fallback que envía el servidor, principalmente para la UI vanilla.

Clientes vanilla no tienen los archivos de idioma del mod, por eso usan estos fallbacks.

Valores soportados:

```json
"serverLanguage": "en_us"
```

```json
"serverLanguage": "es_es"
```

Si quieres que la UI vanilla se vea en español, usa:

```json
"serverLanguage": "es_es"
```

Si quieres que se vea en inglés, deja el default:

```json
"serverLanguage": "en_us"
```

## Costos

`costItem`

Item usado como pago del teleport.

`baseCost`

Costo mínimo.

`blocksPerExtraCost`

Agrega costo extra según distancia en la misma dimensión.

`crossDimensionMultiplier`

Multiplicador para teleports entre dimensiones.

`maxCost`

Costo máximo final. Usa `0` para no limitarlo.

## Reglas de Teleport

`allowCrossDimension`

Permite o bloquea teleports entre dimensiones.

`teleportSourceRange`

El jugador debe estar cerca de una Lodestone registrada para teletransportarse.

- `8`: radio por defecto en bloques.
- `0`: desactiva esta restricción.

## Permisos

`requirePermissions`

- `true`: usa LuckPerms/Fabric Permissions API.
- `false`: todos pueden usar y renombrar Lodestones.

## Comandos

`commandName`

Comando principal. Default:

```json
"commandName": "warp"
```

`fallbackCommandName`

Comando seguro de fallback. Default:

```json
"fallbackCommandName": "lodestone_warp"
```
