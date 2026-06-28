# Permisos

**Última actualización:** 2026-06-27

Lodestone Warps usa Fabric Permissions API y es compatible con LuckPerms.

## Nodos

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

## Crear

`lodestone_teleport.create`

Permite:

- registrar Lodestones al colocarlas
- auto-registrar Lodestones antiguas o no registradas al interactuar con ellas

## Remover

`lodestone_teleport.remove`

Permite:

- romper Lodestones registradas y removerlas de la red de warps

## Admin

`lodestone_teleport.admin`

Permite:

- usar comandos de diagnóstico/admin como `/warp list`

## Bypass

`lodestone_teleport.bypass_cost`

Permite teletransportarse sin pagar el costo configurado.

`lodestone_teleport.bypass_cooldown`

Permite teletransportarse sin esperar el cooldown.

`lodestone_teleport.bypass_max_warps`

Reservado para el futuro límite máximo de warps.

## Modos

`lodestone_teleport.mode.all`

Reservado para el futuro modo de visibilidad `all`.

`lodestone_teleport.mode.discover`

Reservado para el futuro modo de visibilidad `discover`.

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

Permitir que admins ignoren costo y cooldown:

```mcfunction
/lp group admin permission set lodestone_teleport.bypass_cost true
/lp group admin permission set lodestone_teleport.bypass_cooldown true
```

## Desactivar Permisos

Para servidores pequeños o abiertos:

```json
"requirePermissions": false
```

Cuando está desactivado, todos pueden usar, renombrar, crear y remover Lodestones.

Los permisos de bypass y admin no se entregan a jugadores normales al desactivar permisos. Los operadores siguen teniendo acceso fallback de operador.
