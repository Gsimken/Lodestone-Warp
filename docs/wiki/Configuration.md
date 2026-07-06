# Configuration

[English](Configuration.md) | [Español](es/Configuracion.md)

**Last updated:** 2026-07-01

The config is generated on first run:

```text
config/lodestone_teleport.json
```

When an existing config is loaded, Lodestone Warps fills missing options with current defaults and writes the normalized file back to disk.

If Mod Menu is installed on the client, Lodestone Warps also exposes an optional in-game config screen. This edits the local config file only; remote servers keep using their own server-side config.

Server owners can also use the vanilla Dialog config UI:

```mcfunction
/warp config
```

## Example

```json
{
  "costItem": "minecraft:diamond",
  "costType": "xp_levels",
  "baseCost": 1,
  "blocksPerExtraCost": 1000,
  "crossDimensionMultiplier": 2.0,
  "maxCost": 64,
  "allowCrossDimension": true,
  "allowPersonalLodestones": true,
  "defaultLodestoneVisibility": "discoverable",
  "maxLodestonesGlobal": 0,
  "maxLodestonesPerPlayer": 0,
  "registerPlacedLodestonesOnlyWhenSneaking": true,
  "autoRegisterUntrackedLodestones": false,
  "maxDialogDestinations": 24,
  "vanillaDialogDestinationColumnWidth": 245,
  "vanillaDialogCostColumnWidth": 70,
  "vanillaDialogEditColumnWidth": 70,
  "vanillaDialogColumnOrder": "c,d,e",
  "showVanillaDialogHeaderNavigation": true,
  "showVanillaDialogButtonNavigation": true,
  "showVanillaDialogDestinationSuffix": false,
  "vanillaDialogDestinationSuffix": "[{x}, {y}, {z}, {dimension}]",
  "teleportSourceRange": 8,
  "teleportCastSeconds": 2,
  "teleportCastMoveTolerance": 0.2,
  "teleportCooldownSeconds": 3,
  "teleportEffects": true,
  "vanillaTeleportEffect": "end",
  "modTeleportEffect": "lodestone",
  "networkMode": "discover",
  "playerPermissions": [
    "lodestone_teleport.use",
    "lodestone_teleport.create",
    "lodestone_teleport.create.private",
    "lodestone_teleport.create.discoverable",
    "lodestone_teleport.own.rename",
    "lodestone_teleport.own.remove",
    "lodestone_teleport.own.destroy",
    "lodestone_teleport.own.visibility.private",
    "lodestone_teleport.own.visibility.discoverable"
  ],
  "adminPermissions": [
    "lodestone_teleport.admin",
    "lodestone_teleport.config",
    "lodestone_teleport.global",
    "lodestone_teleport.rename",
    "lodestone_teleport.remove",
    "lodestone_teleport.mode.all",
    "lodestone_teleport.create.global",
    "lodestone_teleport.own.visibility.global",
    "lodestone_teleport.bypass_cost",
    "lodestone_teleport.bypass_cast",
    "lodestone_teleport.bypass_cooldown",
    "lodestone_teleport.bypass_max_warps"
  ],
  "commandName": "warp",
  "fallbackCommandName": "lodestone_warp",
  "serverLanguage": "en_us"
}
```

## Cost

`costType`

Controls how teleport cost is paid.

- `xp_levels`: pay experience levels.
- `item`: pay the configured `costItem`.

`costItem`

The item used as teleport payment when `costType` is `item`.

Example:

```json
"costItem": "minecraft:diamond"
```

`baseCost`

Minimum teleport cost.

Default:

```json
"baseCost": 1
```

`blocksPerExtraCost`

Adds extra cost based on same-dimension distance. With the default `1000`, the cost increases by 1 per 1000 blocks.

`crossDimensionMultiplier`

Multiplier used for cross-dimensional teleports.

`maxCost`

Maximum final cost. Use `0` for no cap.

## Lodestone Registration and Visibility

`allowPersonalLodestones`

Allows players to create and keep private Lodestones.

`defaultLodestoneVisibility`

Visibility assigned to newly registered Lodestones when the player has permission for that type.

Supported values:

- `private`
- `discoverable`
- `global`

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

- `false`: the block stays vanilla and is not registered on normal use.
- `true`: the block is registered on use if the player can create Lodestones and limits allow it.

Even when this option is `false`, a player can sneak-right-click an unregistered Lodestone with an empty hand to register it intentionally, as long as they have create permission and limits allow it.

## Discovery

`networkMode`

Controls which Lodestones players can see and teleport to.

- `all`: players can see every registered Lodestone.
- `discover`: players can see global Lodestones, Lodestones they own, and Lodestones they have discovered.

Mode permissions can override this default per player:

- `lodestone_teleport.mode.discover` forces discovery rules even if `networkMode` is `all`.
- `lodestone_teleport.mode.all` bypasses discovery even if `networkMode` is `discover`.

Default:

```json
"networkMode": "discover"
```

Private Lodestones cannot be discovered or registered by another player. If another player tries, they receive a dedicated private-registration error.

## Teleport Rules

`allowCrossDimension`

Allows or blocks teleports between dimensions.

`teleportSourceRange`

Players must be near a registered Lodestone before teleporting.

- `8`: default radius in blocks.
- `0`: disables this requirement.

`teleportCastSeconds`

Stand-still cast time before teleporting.

- `2`: default cast duration in seconds.
- `0`: disables the cast and teleports immediately.

Players with `lodestone_teleport.bypass_cast` skip the cast.

`teleportCastMoveTolerance`

How far the player can move during the cast before it is cancelled.

- `0.2`: default tolerance in blocks.
- `0`: requires the player to stay exactly still.

`teleportCooldownSeconds`

Server-side cooldown after a successful teleport.

- `3`: default cooldown in seconds.
- `0`: disables cooldown.
- Players with `lodestone_teleport.bypass_cooldown` skip cooldown.

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

`vanillaDialogDestinationColumnWidth`

Width of the destination column in the vanilla Dialog UI. Default: `245`.

`vanillaDialogCostColumnWidth`

Width of the cost column in the vanilla Dialog UI. Default: `70`.

`vanillaDialogEditColumnWidth`

Width of the edit/spacer column in the vanilla Dialog UI. Default: `70`.

`vanillaDialogColumnOrder`

Column order for the vanilla Dialog destination grid. Default: `c,d,e`.

- `c`: cost column
- `d`: destination column
- `e`: edit column

Examples: `c,d,e`, `d,c,e`, `d,e,c`.

`showVanillaDialogHeaderNavigation`

Shows clickable page arrows around the page label in the vanilla Dialog body. Default: `true`.

`showVanillaDialogButtonNavigation`

Shows page arrow buttons beside Search and Edit this warp in the vanilla Dialog grid. Default: `true`.

`showVanillaDialogDestinationSuffix`

Shows extra configured information after destination names in the vanilla Dialog UI. Default: `false`.

`vanillaDialogDestinationSuffix`

Suffix pattern appended to destination names when enabled.

Examples:

```json
"vanillaDialogDestinationSuffix": "[{x}, {y}, {z}, {dimension}]"
```

```json
"vanillaDialogDestinationSuffix": "({x}, {z}, {owner})"
```

Supported placeholders:

- `{x}`
- `{y}`
- `{z}`
- `{dimension}`
- `{owner}`

## Permissions

`playerPermissions`

Default permissions granted to every player when no permission manager answers a permission request.

`adminPermissions`

Default permissions granted to OP/gamemaster-level admins when no permission manager answers a permission request.

LuckPerms or another Fabric Permissions-compatible manager is recommended for real group/player management. If a permission manager answers, its answer wins over these config defaults.

## Development Permission Overrides

For local debugging, use JVM system properties. Put the `-D...` flags before `--args` if you use extra run arguments.

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.rename=false
```

Example with granular permissions:

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.create=true -Dlodestone_teleport.create.private=true -Dlodestone_teleport.own.rename=true -Dlodestone_teleport.own.destroy=false
```

These overrides are intended for development only and take priority over config defaults.

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
