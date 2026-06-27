# Configuracion

**Ultima actualizacion:** 2026-06-27

La configuracion se genera en el primer inicio:

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

`serverLanguage` controla los textos fallback que envia el servidor, principalmente para la UI vanilla.

Clientes vanilla no tienen los archivos de idioma del mod, por eso usan estos fallbacks.

Valores soportados:

```json
"serverLanguage": "en_us"
```

```json
"serverLanguage": "es_es"
```

Si quieres que la UI vanilla se vea en espanol, usa:

```json
"serverLanguage": "es_es"
```

Si quieres que se vea en ingles, deja el default:

```json
"serverLanguage": "en_us"
```

## Costos

`costItem`

Item usado como pago del teleport.

`baseCost`

Costo minimo.

`blocksPerExtraCost`

Agrega costo extra segun distancia en la misma dimension.

`crossDimensionMultiplier`

Multiplicador para teleports entre dimensiones.

`maxCost`

Costo maximo final. Usa `0` para no limitarlo.

## Reglas de Teleport

`allowCrossDimension`

Permite o bloquea teleports entre dimensiones.

`teleportSourceRange`

El jugador debe estar cerca de una Lodestone registrada para teletransportarse.

- `8`: radio por defecto en bloques.
- `0`: desactiva esta restriccion.

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
