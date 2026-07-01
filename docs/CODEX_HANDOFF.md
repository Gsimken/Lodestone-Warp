# Codex Handoff - Lodestone Warps

**Updated:** 2026-07-01

Use this as context when continuing work in a new Codex session.

## Repository

- Repo path: `C:\Users\simke\Documents\Github\Waystone Teleport`
- GitHub: `https://github.com/Gsimken/Lodestone-Warp`
- Current working branch: `codex/discovery-mode-0.5.0`
- Mod name: `Lodestone Warps`
- Mod id/package theme: `lodestone_teleport`
- Minecraft target: `26.2`
- Fabric project, Java 25+

## Current Product Shape

Lodestone Warps turns vanilla Lodestones into a server-side warp network.

- Vanilla clients use Minecraft Dialog UI.
- Clients with the mod installed use the custom mod UI.
- The server validates teleports, costs, cooldowns, cast time, source range, permissions, discovery, and visibility.
- Main command defaults to `/warp`.
- Fallback command defaults to `/lodestone_warp`.

## Core Gameplay

1. Player sneak-places a Lodestone to register it.
2. Registered Lodestones have ownership and visibility.
3. Right-click with empty hand opens UI.
4. Player chooses a destination.
5. Server validates source Lodestone proximity, destination visibility, cost, cast, cooldown, and dimensions.
6. Player teleports if everything passes.

## Visibility and Discovery

Lodestone visibility modes:

- `private`: owner-only unless player has all/admin access.
- `discoverable`: other players can discover it by interacting with it.
- `global`: visible to everyone, useful for lobbies/hubs.

Config:

- `networkMode: "discover"` is the intended default.
- `networkMode: "all"` shows all registered Lodestones.
- Players with `lodestone_teleport.mode.all` bypass discovery visibility.
- Players with `lodestone_teleport.mode.discover` follow discovery visibility.

Private Lodestones cannot be discovered or registered by other players. If another player tries, they get:

```text
This lodestone is already registered and private; it cannot be registered by another player.
```

Discovery commands:

```mcfunction
/warp discover grant <player> <id|all>
/warp discover revoke <player> <id|all>
/warp discover list <player>
/warp discover who <id>
```

## Permissions

The mod uses Fabric Permissions API and is LuckPerms-compatible.

If a permission manager answers, that answer wins. If not, the mod falls back to:

- `playerPermissions`
- `adminPermissions`

Important design decision:

- `lodestone_teleport.rename` and `lodestone_teleport.remove` are broad staff permissions.
- Normal players should use `own.*` permissions.
- Players should not get `lodestone_teleport.remove` by default because it allows breaking/unlinking Lodestones owned by others.

Recommended player permissions:

```text
lodestone_teleport.use
lodestone_teleport.create
lodestone_teleport.create.private
lodestone_teleport.create.discoverable
lodestone_teleport.own.rename
lodestone_teleport.own.remove
lodestone_teleport.own.destroy
lodestone_teleport.own.visibility.private
lodestone_teleport.own.visibility.discoverable
lodestone_teleport.mode.discover
```

Recommended admin permissions:

```text
lodestone_teleport.admin
lodestone_teleport.config
lodestone_teleport.global
lodestone_teleport.rename
lodestone_teleport.remove
lodestone_teleport.mode.all
lodestone_teleport.create.global
lodestone_teleport.own.visibility.global
lodestone_teleport.bypass_cost
lodestone_teleport.bypass_cast
lodestone_teleport.bypass_cooldown
lodestone_teleport.bypass_max_warps
```

Debug permissions for local testing should use JVM system properties:

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.create=true -Dlodestone_teleport.create.private=true -Dlodestone_teleport.own.rename=true -Dlodestone_teleport.own.destroy=false
```

## Config

Current important defaults:

```json
{
  "costType": "xp_levels",
  "baseCost": 1,
  "blocksPerExtraCost": 1000,
  "crossDimensionMultiplier": 2.0,
  "maxCost": 64,
  "allowPersonalLodestones": true,
  "defaultLodestoneVisibility": "discoverable",
  "networkMode": "discover",
  "teleportSourceRange": 8,
  "teleportCastSeconds": 2,
  "teleportCastMoveTolerance": 0.2,
  "teleportCooldownSeconds": 3,
  "vanillaTeleportEffect": "end",
  "modTeleportEffect": "lodestone",
  "commandName": "warp",
  "fallbackCommandName": "lodestone_warp",
  "serverLanguage": "en_us"
}
```

Server owner config commands:

```mcfunction
/warp reload
/warp config
/warp config list
/warp config get <key>
/warp config set <key> <value>
```

Changing command names requires restart.

## UI State

Terminology:

- "UI vanilla" means Minecraft Dialog UI for clients without the mod.
- "UI de mod" or "mod UI" means the custom client screen.

Mod UI features:

- Search.
- Pagination.
- Table columns: name, coords, dimension, cost.
- Cost icons for XP/item.
- Visibility icons:
  - `🌐` global
  - `🔒` private
- Header shows the current Lodestone visibility icon.

Mod edit screen shape:

```text
[textbox]
[Mode][Remove]

[Save][Cancel]
```

- `Mode` cycles locally through allowed visibility modes.
- `Save` sends name + visibility together using `save_edit`.
- `Cancel` closes without saving local changes.
- `Remove` unlinks immediately.
- Server revalidates permissions before applying changes.

## Recent Work Completed

- Added ownership-aware visibility model: private/discoverable/global.
- Added discovery storage and grant/revoke/list/who commands.
- Added admin all-mode and player discover-mode behavior.
- Added global Lodestones for shared hubs/lobbies.
- Added mod UI visibility icons.
- Added current Lodestone visibility icon in mod and vanilla UI headers.
- Added staged edit save flow for mod UI.
- Added private-registration error when another player tries to discover/register a private Lodestone.
- Fixed default permission model so players do not get global rename/remove by default.
- Updated wiki docs in English and Spanish.

## Verification

Last verified command used:

```powershell
.\gradlew.bat build
```

Expected result: build succeeds.

## Suggested Next Steps

- Test with two different player identities:
  - Player A creates private Lodestone.
  - Player B cannot discover/register/break it.
  - Player B can discover discoverable Lodestones.
  - Admin with `mode.all` can see all.
- Test LuckPerms real permission behavior against config fallback behavior.
- Test vanilla UI and mod UI side by side.
- Consider adding a small automated/manual release checklist for 0.5.x or 1.0.
- Review README/Modrinth description after final feature freeze.
