# Permissions

[English](Permissions.md) | [Español](es/Permisos.md)

**Last updated:** 2026-07-13

Lodestone Warps uses Fabric Permissions API and is compatible with LuckPerms.

If LuckPerms is installed, Lodestone Warps treats LuckPerms as the source of truth and does not grant positive fallback permissions. Without LuckPerms, Lodestone Warps falls back to:

- `playerPermissions`: fallback permission map for every player.
- `adminPermissions`: extra fallback permission map for OP/gamemaster-level admins.

OP/gamemaster players keep `playerPermissions` and additionally receive enabled `adminPermissions`; admin permissions do not subtract player permissions.

If LuckPerms is installed but a group/player has no Lodestone Warps permissions, that group/player should be treated as having no Lodestone Warps access until permissions are granted in LuckPerms. The fallback config is intended for servers without a permission manager.

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
lodestone_teleport.limit.<number>
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
lodestone_teleport.limit.10
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

Allows registering Lodestones even when `maxLodestonesGlobal` or the player's permission limit has been reached.

## Limit Permissions

`lodestone_teleport.limit.<number>`

Sets the maximum number of Lodestones a player can own. The highest matching limit wins.

Examples:

- `lodestone_teleport.limit.3`
- `lodestone_teleport.limit.10`
- `lodestone.limit.25`

Use `lodestone_teleport.bypass_max_warps` for staff or groups that should ignore this limit.

Limit values are dynamic. You can use any positive number, such as `lodestone_teleport.limit.1`, `lodestone_teleport.limit.3`, or `lodestone_teleport.limit.50`.

## Config Fallback Permissions

The config maps use boolean values:

```json
"lodestone_teleport.use": true,
"lodestone_teleport.rename": false
```

This lets you disable permissions without deleting them. Known missing permissions can be written back as `false`, which makes auditing easier. Dynamic limit nodes such as `lodestone_teleport.limit.1` are not auto-added; add the exact limit keys you want to use.

The config maps accept:

- full nodes, such as `lodestone_teleport.use`
- short nodes, such as `use`
- wildcards, such as `lodestone_teleport.*`, `lodestone.*`, or `*`

If you use LuckPerms, Lodestone Warps ignores positive fallback grants and lets LuckPerms decide. You can still keep limit keys such as `"lodestone_teleport.limit.1": false` in the config as audit/candidate entries.

The alias prefix `lodestone.*` is supported for convenience, but the canonical permission prefix is `lodestone_teleport.*`.

## LuckPerms Examples

Recommended first setup:

1. Stop the server.
2. Open `config/lodestone_warp_and_tp/lodestone_teleport.json`.
3. Set fallback permission values to `false`, or empty `playerPermissions` and `adminPermissions`, if you want LuckPerms to control all permissions.
4. Start the server.
5. Run `/lp editor`.
6. Add the player permissions below to the `default` group.
7. Create an `admin` group and add the admin permissions below.
8. Save and apply the LuckPerms editor changes.

Give everyone basic discovery gameplay:

```mcfunction
/lp group default permission set lodestone_teleport.use true
/lp group default permission set lodestone_teleport.create true
/lp group default permission set lodestone_teleport.create.private true
/lp group default permission set lodestone_teleport.create.discoverable true
/lp group default permission set lodestone_teleport.own.rename true
/lp group default permission set lodestone_teleport.own.destroy true
/lp group default permission set lodestone_teleport.mode.discover true
/lp group default permission set lodestone_teleport.limit.10 true
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
