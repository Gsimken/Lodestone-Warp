# Lodestone Warps

**Last updated:** 2026-06-29

**Lodestone Warps** turns vanilla Lodestones into a server-side warp network for Minecraft **26.2**.

Players can place Lodestones to register warp points, then use those Lodestones to travel through a shared network. Vanilla clients use Minecraft's built-in Dialog UI, while players who install the mod on their client get an enhanced custom interface with search, pagination, item-cost icons, destination editing, and cleaner table-style information.

The mod is designed for servers that want immersive teleportation without requiring every player to install a client mod.

## Wiki

The most up-to-date documentation is kept in the wiki:

- [English Wiki](https://github.com/Gsimken/Lodestone-Warp/wiki)
- [Wiki en Español](https://github.com/Gsimken/Lodestone-Warp/wiki/Inicio)

## Features

- Server-side Lodestone warp network.
- Vanilla client support through Minecraft Dialogs.
- Optional enhanced UI for players with the mod installed client-side.
- Optional Mod Menu config screen when installed on the client.
- Sneak-place Lodestones to register them as warps by default.
- Optional auto-registration for old or untracked Lodestones when interacted with.
- Configurable global and per-player Lodestone registration limits.
- Removes registered Lodestones when broken.
- Searchable destination list.
- Pagination in the custom mod UI.
- Rename Lodestones from the UI.
- Teleport cost with configurable XP-level or item payment and dynamic distance scaling.
- Stand-still teleport cast before travel.
- Server-side teleport cooldown.
- Configurable teleport particles and sounds.
- Separate vanilla and mod-client teleport effect presets.
- Item icons for cost display in the custom mod UI.
- Cross-dimensional teleport support.
- Configurable requirement to stand near a registered Lodestone before teleporting.
- Configurable command name, defaulting to `/warp`.
- Safe fallback command, defaulting to `/lodestone_warp`.
- Server-side config commands and a vanilla Dialog quick config UI for server owners.
- Configurable network mode: show all Lodestones or only discovered Lodestones.
- Per-player Lodestone discovery storage.
- Admin-managed global Lodestones for lobbies and shared hubs.
- LuckPerms-compatible permissions through Fabric Permissions API.
- Optional config to disable permission checks for open servers.
- Server-side language fallback for vanilla UI text.

## Requirements

- Minecraft **26.2**
- Java **25+**
- Fabric Loader **0.19.3+**
- Fabric API
- Optional: LuckPerms for permission management
- Optional client-side: Mod Menu for an in-game local config editor

## Client Support

Lodestone Warps supports two setups:

- **Server only:** vanilla clients can use the mod through Minecraft's vanilla Dialog UI.
- **Server + client:** players with the mod installed get the enhanced custom UI.

Players without the client mod are still supported.

## Basic Usage

1. Sneak-place a Lodestone to register it as a warp.
2. Right-click a registered Lodestone with an empty hand.
3. Choose a destination from the UI.
4. Pay the configured cost.
5. Teleport to the selected Lodestone.

Sneak while placing a Lodestone to open the rename flow.

## Commands

Default command: `/warp`

Fallback command: `/lodestone_warp`

Available subcommands:

- `/warp tp <id or name>`
- `/warp rename <id> <name>`
- `/warp edit <id>`
- `/warp remove <id>`
- `/warp unlink <id>`
- `/warp list`
- `/warp global <id> <true|false>`
- `/warp discover grant <player> <id>`
- `/warp discover revoke <player> <id>`
- `/warp discover list <player>`
- `/warp reload`
- `/warp config`
- `/warp config list`
- `/warp config get <key>`
- `/warp config set <key> <value>`

If another mod already uses `/warp`, Lodestone Warps keeps `/lodestone_warp` available as a safer fallback.

## Permissions

Lodestone Warps supports LuckPerms-compatible permissions through Fabric Permissions API. It also has a simple config fallback with two lists:

- `playerPermissions`: permissions granted to every player when `requirePermissions` is enabled.
- `adminPermissions`: permissions granted to OP/gamemaster-level admins when `requirePermissions` is enabled.

Use LuckPerms or another permission manager for per-player, group, inheritance, or temporary permissions.

Permission nodes:

- `lodestone_teleport.use`
- `lodestone_teleport.rename`
- `lodestone_teleport.create`
- `lodestone_teleport.remove`
- `lodestone_teleport.admin`
- `lodestone_teleport.bypass_cost`
- `lodestone_teleport.bypass_cooldown`
- `lodestone_teleport.bypass_max_warps`
- `lodestone_teleport.config`
- `lodestone_teleport.global`
- `lodestone_teleport.mode.all`
- `lodestone_teleport.mode.discover`

If permissions are enabled, players need `lodestone_teleport.use` to use warps, `lodestone_teleport.rename` to rename Lodestones, `lodestone_teleport.create` to register Lodestones, and `lodestone_teleport.remove` to unlink or remove registered Lodestones.

Server owners need `lodestone_teleport.config` or OP-level access to use `/warp reload`, `/warp config`, and server config editing actions.

Admins need `lodestone_teleport.global` or OP-level access to mark Lodestones as global in discovery mode.

Permission checks can be disabled in the server config with `requirePermissions: false`.

The config permission lists accept full nodes such as `lodestone_teleport.use`, bare names such as `use`, and wildcards such as `lodestone_teleport.*` or `*`.

Permission compatibility warnings:

- `lodestone_teleport.mode.all` + `lodestone_teleport.mode.discover` in `playerPermissions`: `mode.all` bypasses discovery for all players.
- `lodestone_teleport.mode.all` + `lodestone_teleport.mode.discover` in `adminPermissions`: admins bypass discovery while also being marked as discovery users.
- `networkMode: discover` + `lodestone_teleport.mode.all` in `playerPermissions`: discovery mode is effectively disabled for players.
- `networkMode: discover` + `lodestone_teleport.mode.discover` without `lodestone_teleport.use` in `playerPermissions`: players can be in discovery mode but cannot use Lodestones.

## Configuration

The config is generated on first run at:

`config/lodestone_teleport.json`

Full config reference:

[Configuration Wiki](https://github.com/Gsimken/Lodestone-Warp/wiki/Configuration)

## Current Status

Current release line: **0.5.x**

This is still an early mod, but the core gameplay loop is playable:

- register Lodestones
- open UI
- search destinations
- teleport with cost
- use discovery mode
- rename destinations
- use permissions
- support vanilla and modded clients

Feedback from real server usage is welcome.
