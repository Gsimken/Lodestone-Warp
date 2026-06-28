# Configuration

**Last updated:** 2026-06-27

The config is generated on first run:

```text
config/lodestone_teleport.json
```

When an existing config is loaded, Lodestone Warps fills missing options with current defaults and writes the normalized file back to disk.

## Options

```json
{
  "costItem": "minecraft:diamond",
  "baseCost": 1,
  "blocksPerExtraCost": 500,
  "crossDimensionMultiplier": 2.0,
  "maxCost": 64,
  "allowCrossDimension": true,
  "maxLodestonesGlobal": 0,
  "maxLodestonesPerPlayer": 0,
  "registerPlacedLodestonesOnlyWhenSneaking": true,
  "autoRegisterUntrackedLodestones": false,
  "maxDialogDestinations": 24,
  "teleportSourceRange": 8,
  "teleportCastSeconds": 2,
  "teleportCastMoveTolerance": 0.2,
  "teleportCooldownSeconds": 3,
  "teleportEffects": true,
  "vanillaTeleportEffect": "end",
  "modTeleportEffect": "lodestone",
  "requirePermissions": false,
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

## Lodestone Registration

`maxLodestonesGlobal`

Maximum registered Lodestones on the server.

- `0`: unlimited.
- Positive values cap the whole server network.

`maxLodestonesPerPlayer`

Maximum registered Lodestones owned by each player.

- `0`: unlimited.
- Players with `lodestone_teleport.bypass_max_warps` ignore this and the global cap.

`registerPlacedLodestonesOnlyWhenSneaking`

Controls automatic registration when placing a Lodestone.

- `true`: only sneak-place registers the Lodestone as a warp.
- `false`: every placed Lodestone is registered.

`autoRegisterUntrackedLodestones`

Controls what happens when a player right-clicks an unregistered Lodestone with an empty hand.

- `false`: the block stays vanilla and is not registered.
- `true`: the block is registered on use if the player can create Lodestones and limits allow it.

## Teleport Rules

`allowCrossDimension`

Allows or blocks teleports between dimensions.

`teleportSourceRange`

Players must be near a registered Lodestone before teleporting.

- `8`: default radius in blocks.
- `0`: disables this requirement.

`teleportCooldownSeconds`

Server-side cooldown after a successful teleport.

- `3`: default cooldown in seconds.
- `0`: disables cooldown.
- Any positive value is measured in seconds.

`teleportCastSeconds`

Stand-still cast time before teleporting.

- `2`: default cast duration in seconds.
- `0`: disables the cast and teleports immediately.

`teleportCastMoveTolerance`

How far the player can move during the cast before it is cancelled.

- `0.2`: default tolerance in blocks.
- `0`: requires the player to stay exactly still.

## Teleport Effects

`teleportEffects`

Enables or disables teleport particles and sounds.

`vanillaTeleportEffect`

Effect preset used for vanilla clients.

Default:

```json
"vanillaTeleportEffect": "end"
```

`modTeleportEffect`

Effect preset used when the player has Lodestone Warps installed on the client.

Default:

```json
"modTeleportEffect": "lodestone"
```

Supported presets:

- `end`: End/portal-style particles and Enderman teleport sound.
- `lodestone`: brighter Lodestone-style particles using portal, end rod, and enchant effects.
- `none` or `off`: disables that preset.

## UI

`maxDialogDestinations`

Maximum destinations shown in the vanilla Dialog UI.

The custom mod UI has pagination.

## Permissions

`requirePermissions`

- `true`: use LuckPerms/Fabric Permissions API.
- `false`: everyone can use and rename Lodestones.

## Development Permission Overrides

For local debugging, `runClient` and `runServer` can simulate permission results without LuckPerms:

```powershell
.\gradlew.bat runClient -Plodestone_teleport.use=true -Plodestone_teleport.rename=false
```

Example with granular permissions:

```powershell
.\gradlew.bat runClient -Plodestone_teleport.use=true -Plodestone_teleport.create=false -Plodestone_teleport.bypass_cost=true
```

The same flags can also be passed as JVM system properties:

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.rename=true
```

These overrides are intended for development only and take priority over `requirePermissions`.

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
