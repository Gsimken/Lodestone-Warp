# Permisos

**Ultima actualizacion:** 2026-06-27

Lodestone Warps usa Fabric Permissions API y es compatible con LuckPerms.

## Nodos

```text
lodestone_teleport.use
lodestone_teleport.rename
```

## Uso

`lodestone_teleport.use`

Permite:

- abrir la UI
- teletransportarse desde UI vanilla
- teletransportarse desde UI de mod
- usar `/warp tp <id o nombre>`

## Renombrar

`lodestone_teleport.rename`

Permite:

- renombrar desde UI vanilla
- renombrar desde UI de mod
- usar `/warp rename <id> <nombre>`
- usar `/warp edit <id>`

## Ejemplos con LuckPerms

Permitir que todos usen warps:

```mcfunction
/lp group default permission set lodestone_teleport.use true
```

Permitir que admins renombren:

```mcfunction
/lp group admin permission set lodestone_teleport.rename true
```

Permitir ambos permisos a un jugador:

```mcfunction
/lp user PlayerName permission set lodestone_teleport.use true
/lp user PlayerName permission set lodestone_teleport.rename true
```

## Desactivar Permisos

Para servidores pequenos o abiertos:

```json
"requirePermissions": false
```

Cuando esta desactivado, todos pueden usar y renombrar Lodestones.
