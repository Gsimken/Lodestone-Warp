# Commands

**Last updated:** 2026-06-27

## Default Command

```mcfunction
/warp
```

## Fallback Command

```mcfunction
/lodestone_warp
```

The fallback command exists to reduce conflicts with other warp mods.

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
```

Alias:

```mcfunction
/warp unlink <id>
```

The physical Lodestone block is left in the world. By default, it will not re-register on a normal click; sneak-right-click it with an empty hand to register it again intentionally.

List registered Lodestones:

```mcfunction
/warp list
```

`/warp list` shows compact clickable actions after each entry:

- `[TP]`: teleport to the Lodestone.
- `[✎]`: open the rename flow.
- `[X]`: unlink the warp entry while leaving the physical Lodestone block in place.

## Teleport by Name

`tp` accepts either a Lodestone id or its display name.

Examples:

```mcfunction
/warp tp lodestone_3
/warp tp Casa
/warp tp Main Base
```

If multiple Lodestones share the same name, the mod shows clickable options so the player can choose the exact destination.
