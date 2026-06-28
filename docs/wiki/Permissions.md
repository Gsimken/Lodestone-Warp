# Permissions

**Last updated:** 2026-06-27

Lodestone Warps uses Fabric Permissions API and is compatible with LuckPerms.

## Permission Nodes

```text
lodestone_teleport.use
lodestone_teleport.rename
lodestone_teleport.create
lodestone_teleport.remove
lodestone_teleport.admin
lodestone_teleport.bypass_cost
lodestone_teleport.bypass_cooldown
lodestone_teleport.bypass_max_warps
lodestone_teleport.mode.all
lodestone_teleport.mode.discover
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

## Create Permission

`lodestone_teleport.create`

Allows:

- registering Lodestones when placed
- auto-registering old or untracked Lodestones when interacted with, if enabled in config

## Remove Permission

`lodestone_teleport.remove`

Allows:

- breaking registered Lodestones and removing them from the warp network
- unlinking registered Lodestone warps with `/warp remove <id>` or `/warp unlink <id>`

## Admin Permission

`lodestone_teleport.admin`

Allows:

- using diagnostic/admin commands such as `/warp list`

## Bypass Permissions

`lodestone_teleport.bypass_cost`

Allows teleporting without paying the configured cost.

`lodestone_teleport.bypass_cooldown`

Allows teleporting without waiting for teleport cooldown.

`lodestone_teleport.bypass_max_warps`

Allows registering Lodestones even when `maxLodestonesGlobal` or `maxLodestonesPerPlayer` has been reached.

## Mode Permissions

`lodestone_teleport.mode.all`

Reserved for the future `all` network visibility mode.

`lodestone_teleport.mode.discover`

Reserved for the future `discover` network visibility mode.

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

Allow admins to bypass cost and cooldown:

```mcfunction
/lp group admin permission set lodestone_teleport.bypass_cost true
/lp group admin permission set lodestone_teleport.bypass_cooldown true
```

## Disable Permission Checks

For open or small servers:

```json
"requirePermissions": false
```

When disabled, everyone can use, rename, create, and remove Lodestones.

Bypass and admin permissions are not granted to normal players by disabling permission checks. Operators still receive operator-level fallback access.
