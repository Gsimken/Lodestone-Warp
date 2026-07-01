# Commands

**Last updated:** 2026-07-01

## Default Command

```mcfunction
/warp
```

## Fallback Command

```mcfunction
/lodestone_warp
```

The fallback command exists to reduce conflicts with other warp mods. If another mod already owns `/warp`, use `/lodestone_warp`.

## Subcommands

Teleport:

```mcfunction
/warp tp <id or name>
```

Rename:

```mcfunction
/warp rename <id> <name>
```

Open edit flow:

```mcfunction
/warp edit <id>
```

Unlink a Lodestone warp from the network:

```mcfunction
/warp remove <id>
/warp unlink <id>
```

The physical Lodestone block is left in the world. By default, it will not re-register on a normal click; sneak-right-click it with an empty hand to register it again intentionally.

Change visibility:

```mcfunction
/warp visibility <id> <private|discoverable|global>
```

Legacy/global helper:

```mcfunction
/warp global <id> <true|false>
```

List registered Lodestones:

```mcfunction
/warp list
```

`/warp list` shows compact clickable actions after each entry:

- `[TP]`: teleport to the Lodestone.
- `[✎]`: open the edit flow.
- `[X]`: unlink the warp entry while leaving the physical Lodestone block in place.

## Discovery Commands

Grant discovery:

```mcfunction
/warp discover grant <player> <id|all>
```

Revoke discovery:

```mcfunction
/warp discover revoke <player> <id|all>
```

List what a player has discovered:

```mcfunction
/warp discover list <player>
```

List who has discovered a Lodestone:

```mcfunction
/warp discover who <id>
```

## Config Commands

Reload config:

```mcfunction
/warp reload
```

Open vanilla Dialog config UI:

```mcfunction
/warp config
```

List config keys:

```mcfunction
/warp config list
```

Read or write a config value:

```mcfunction
/warp config get <key>
/warp config set <key> <value>
```

Changing `commandName` or `fallbackCommandName` requires a server restart because commands are registered during startup.

## Teleport by Name

`tp` accepts either a Lodestone id or its display name.

Examples:

```mcfunction
/warp tp lodestone_3
/warp tp Casa
/warp tp Main Base
```

If multiple Lodestones share the same name, the mod shows clickable options so the player can choose the exact destination.
