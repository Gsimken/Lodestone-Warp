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

List registered Lodestones:

```mcfunction
/warp list
```

## Teleport by Name

`tp` accepts either a Lodestone id or its display name.

Examples:

```mcfunction
/warp tp lodestone_3
/warp tp Casa
/warp tp Main Base
```

If multiple Lodestones share the same name, the mod shows clickable options so the player can choose the exact destination.
