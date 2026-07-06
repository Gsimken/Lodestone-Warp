# Permissions

[English](Permissions.md) | [Español](es/Permisos.md)

**Last updated:** 2026-07-01

Lodestone Warps uses Fabric Permissions API and is compatible with LuckPerms.

If a permission manager answers a permission request, that answer wins. If no permission manager answers, Lodestone Warps falls back to:

- `playerPermissions`: default permissions for every player.
- `adminPermissions`: default permissions for OP/gamemaster-level admins.

## Permission Nodes

```text
lodestone_teleport.use
lodestone_teleport.rename
lodestone_teleport.create
lodestone_teleport.create.private
lodestone_teleport.create.discoverable
lodestone_teleport.create.global
lodestone_teleport.remove
lodestone_teleport.own.rename
lodestone_teleport.own.remove
lodestone_teleport.own.destroy
lodestone_teleport.own.visibility.private
lodestone_teleport.own.visibility.discoverable
lodestone_teleport.own.visibility.global
lodestone_teleport.admin
lodestone_teleport.config
lodestone_teleport.global
lodestone_teleport.bypass_cost
lodestone_teleport.bypass_cast
lodestone_teleport.bypass_cooldown
lodestone_teleport.bypass_max_warps
lodestone_teleport.mode.all
lodestone_teleport.mode.discover
```

## Recommended Defaults

Players should usually receive:

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

Admins should usually receive:

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

`lodestone_teleport.rename` and `lodestone_teleport.remove` are broad staff permissions. They can affect any registered Lodestone. Use `own.*` permissions for normal players.

## Use Permission

`lodestone_teleport.use`

Allows:

- opening the Lodestone UI
- teleporting through vanilla UI
- teleporting through mod UI
- using `/warp tp <id or name>`

## Create Permissions

`lodestone_teleport.create`

Allows registering Lodestones, but the player also needs a matching visibility permission.

Visibility-specific create permissions:

- `lodestone_teleport.create.private`
- `lodestone_teleport.create.discoverable`
- `lodestone_teleport.create.global`

`create.global` should usually be admin-only.

## Edit and Remove Permissions

`lodestone_teleport.rename`

Broad staff permission. Allows renaming any registered Lodestone.

`lodestone_teleport.remove`

Broad staff permission. Allows unlinking or breaking any registered Lodestone.

`lodestone_teleport.own.rename`

Allows a player to rename Lodestones they own.

`lodestone_teleport.own.remove`

Allows a player to unlink Lodestones they own.

`lodestone_teleport.own.destroy`

Allows a player to break registered Lodestones they own.

## Visibility Permissions

`lodestone_teleport.own.visibility.private`

Allows a player to make their own Lodestones private.

`lodestone_teleport.own.visibility.discoverable`

Allows a player to make their own Lodestones discoverable.

`lodestone_teleport.own.visibility.global`

Allows a player to make their own Lodestones global. This should usually be admin-only.

`lodestone_teleport.global`

Broad staff permission for global Lodestone management.

## Mode Permissions

`lodestone_teleport.mode.discover`

Forces discovery rules for that player. Players see global Lodestones, owned Lodestones, and Lodestones they have discovered. This can force discovery even when `networkMode` is `all`.

`lodestone_teleport.mode.all`

Bypasses discovery visibility and lets the player see all registered Lodestones. This is useful for admins.

If both are granted, `mode.all` effectively wins.

## Admin and Config

`lodestone_teleport.admin`

Allows diagnostic/admin commands such as `/warp list`.

`lodestone_teleport.config`

Allows server config commands and the vanilla Dialog config UI:

```mcfunction
/warp config
/warp reload
```

## Bypass Permissions

`lodestone_teleport.bypass_cost`

Allows teleporting without paying the configured cost.

`lodestone_teleport.bypass_cast`

Allows teleporting without the stand-still cast time.

`lodestone_teleport.bypass_cooldown`

Allows teleporting without waiting for teleport cooldown.

`lodestone_teleport.bypass_max_warps`

Allows registering Lodestones even when `maxLodestonesGlobal` or `maxLodestonesPerPlayer` has been reached.

## LuckPerms Examples

Give everyone basic discovery gameplay:

```mcfunction
/lp group default permission set lodestone_teleport.use true
/lp group default permission set lodestone_teleport.create true
/lp group default permission set lodestone_teleport.create.private true
/lp group default permission set lodestone_teleport.create.discoverable true
/lp group default permission set lodestone_teleport.own.rename true
/lp group default permission set lodestone_teleport.own.destroy true
/lp group default permission set lodestone_teleport.mode.discover true
```

Give admins global management:

```mcfunction
/lp group admin permission set lodestone_teleport.admin true
/lp group admin permission set lodestone_teleport.config true
/lp group admin permission set lodestone_teleport.global true
/lp group admin permission set lodestone_teleport.rename true
/lp group admin permission set lodestone_teleport.remove true
/lp group admin permission set lodestone_teleport.mode.all true
```

Allow admins to bypass teleport rules:

```mcfunction
/lp group admin permission set lodestone_teleport.bypass_cost true
/lp group admin permission set lodestone_teleport.bypass_cast true
/lp group admin permission set lodestone_teleport.bypass_cooldown true
```

## Compatibility Warnings

Avoid granting these combinations to normal players unless it is intentional:

- `lodestone_teleport.mode.all` with `networkMode: discover`: players see all Lodestones, bypassing discovery.
- `lodestone_teleport.rename`: players can rename Lodestones they do not own.
- `lodestone_teleport.remove`: players can unlink or break Lodestones they do not own.
- `lodestone_teleport.create.global`: players can create global Lodestones.
