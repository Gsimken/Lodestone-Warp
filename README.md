# Lodestone Warps

**Last updated:** 2026-06-27

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
- Automatic Lodestone registration when placed.
- Auto-registration for old or untracked Lodestones when interacted with.
- Removes registered Lodestones when broken.
- Searchable destination list.
- Pagination in the custom mod UI.
- Rename Lodestones from the UI.
- Teleport cost with configurable item and dynamic distance scaling.
- Stand-still teleport cast before travel.
- Server-side teleport cooldown.
- Configurable teleport particles and sounds.
- Separate vanilla and mod-client teleport effect presets.
- Item icons for cost display in the custom mod UI.
- Cross-dimensional teleport support.
- Configurable requirement to stand near a registered Lodestone before teleporting.
- Configurable command name, defaulting to `/warp`.
- Safe fallback command, defaulting to `/lodestone_warp`.
- LuckPerms-compatible permissions through Fabric Permissions API.
- Optional config to disable permission checks for open servers.
- Server-side language fallback for vanilla UI text.

## Requirements

- Minecraft **26.2**
- Java **25+**
- Fabric Loader **0.19.3+**
- Fabric API
- Optional: LuckPerms for permission management

## Client Support

Lodestone Warps supports two setups:

- **Server only:** vanilla clients can use the mod through Minecraft's vanilla Dialog UI.
- **Server + client:** players with the mod installed get the enhanced custom UI.

Players without the client mod are still supported.

## Basic Usage

1. Place a Lodestone to register it as a warp.
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
- `/warp list`

If another mod already uses `/warp`, Lodestone Warps keeps `/lodestone_warp` available as a safer fallback.

## Permissions

Lodestone Warps supports LuckPerms-compatible permissions through Fabric Permissions API.

Permission nodes:

- `lodestone_teleport.use`
- `lodestone_teleport.rename`

If permissions are enabled, players need `lodestone_teleport.use` to use warps and `lodestone_teleport.rename` to rename Lodestones.

Permission checks can be disabled in the server config.

## Configuration

The config is generated on first run at:

`config/lodestone_teleport.json`

Full config reference:

[Configuration Wiki](https://github.com/Gsimken/Lodestone-Warp/wiki/Configuration)

## Current Status

Current release line: **0.2.x**

This is still an early mod, but the core gameplay loop is playable:

- register Lodestones
- open UI
- search destinations
- teleport with cost
- rename destinations
- use permissions
- support vanilla and modded clients

Feedback from real server usage is welcome.
