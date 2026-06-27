# Permissions

**Last updated:** 2026-06-27

Lodestone Warps uses Fabric Permissions API and is compatible with LuckPerms.

## Permission Nodes

```text
lodestone_teleport.use
lodestone_teleport.rename
```

## Use Permission

`lodestone_teleport.use`

Allows:

- opening the Lodestone UI
- teleporting through UI vanilla
- teleporting through UI de mod
- using `/warp tp <id or name>`

## Rename Permission

`lodestone_teleport.rename`

Allows:

- renaming Lodestones from UI vanilla
- renaming Lodestones from UI de mod
- using `/warp rename <id> <name>`
- using `/warp edit <id>`

## LuckPerms Examples

Give everyone warp usage:

```mcfunction
/lp group default permission set lodestone_teleport.use true
```

Give admins rename access:

```mcfunction
/lp group admin permission set lodestone_teleport.rename true
```

Give one player both permissions:

```mcfunction
/lp user PlayerName permission set lodestone_teleport.use true
/lp user PlayerName permission set lodestone_teleport.rename true
```

## Disable Permission Checks

For open or small servers:

```json
"requirePermissions": false
```

When disabled, everyone can use and rename Lodestones.
