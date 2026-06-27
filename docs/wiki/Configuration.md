# Configuration

**Last updated:** 2026-06-27

The config is generated on first run:

```text
config/lodestone_teleport.json
```

## Options

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

## Cost

`costItem`

The item used as teleport payment.

Example:

```json
"costItem": "minecraft:diamond"
```

`baseCost`

Minimum teleport cost.

`blocksPerExtraCost`

Adds extra cost based on same-dimension distance.

`crossDimensionMultiplier`

Multiplier used for cross-dimensional teleports.

`maxCost`

Maximum final cost. Use `0` for no cap.

## Teleport Rules

`allowCrossDimension`

Allows or blocks teleports between dimensions.

`teleportSourceRange`

Players must be near a registered Lodestone before teleporting.

- `8`: default radius in blocks.
- `0`: disables this requirement.

## UI

`maxDialogDestinations`

Maximum destinations shown in the vanilla Dialog UI.

The custom mod UI has pagination.

## Permissions

`requirePermissions`

- `true`: use LuckPerms/Fabric Permissions API.
- `false`: everyone can use and rename Lodestones.

## Commands

`commandName`

Primary command. Default:

```json
"commandName": "warp"
```

`fallbackCommandName`

Safe fallback command. Default:

```json
"fallbackCommandName": "lodestone_warp"
```

Lodestone Warps always tries to register the fallback command, then tries the primary command if it is available.

## Language

`serverLanguage`

Controls server-side fallback text, mostly for vanilla clients that do not have the mod language files.

Supported values:

- `en_us`
- `es_es`

Default:

```json
"serverLanguage": "en_us"
```
