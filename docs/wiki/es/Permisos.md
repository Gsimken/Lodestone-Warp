# Permisos

[English](../Permissions.md) | [Español](Permisos.md)

**Última actualización:** 2026-07-08

Lodestone Warps usa Fabric Permissions API y es compatible con LuckPerms.

Si un administrador de permisos responde una consulta, esa respuesta gana. Si ningún administrador responde, Lodestone Warps usa:

- `playerPermissions`: permisos default para todos los jugadores.
- `adminPermissions`: permisos default para admins OP/gamemaster.

## Nodos

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
lodestone_teleport.limit.<numero>
```

## Defaults Recomendados

Jugadores normales:

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

Admins:

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

`lodestone_teleport.rename` y `lodestone_teleport.remove` son permisos amplios de staff. Pueden afectar cualquier Lodestone registrada. Para jugadores normales usa permisos `own.*`.

## Uso

`lodestone_teleport.use`

Permite:

- abrir la UI
- teletransportarse desde UI vanilla
- teletransportarse desde UI de mod
- usar `/warp tp <id o nombre>`

## Crear

`lodestone_teleport.create`

Permite registrar Lodestones, pero el jugador también necesita un permiso de visibilidad compatible:

- `lodestone_teleport.create.private`
- `lodestone_teleport.create.discoverable`
- `lodestone_teleport.create.global`

`create.global` normalmente debería ser solo para admins.

## Editar y Remover

`lodestone_teleport.rename`

Permiso amplio de staff. Permite renombrar cualquier Lodestone registrada.

`lodestone_teleport.remove`

Permiso amplio de staff. Permite desvincular o romper cualquier Lodestone registrada.

`lodestone_teleport.own.rename`

Permite renombrar Lodestones propias.

`lodestone_teleport.own.remove`

Permite desvincular Lodestones propias.

`lodestone_teleport.own.destroy`

Permite romper Lodestones registradas propias.

## Visibilidad

`lodestone_teleport.own.visibility.private`

Permite marcar Lodestones propias como privadas.

`lodestone_teleport.own.visibility.discoverable`

Permite marcar Lodestones propias como descubribles.

`lodestone_teleport.own.visibility.global`

Permite marcar Lodestones propias como globales. Normalmente debería ser admin-only.

`lodestone_teleport.global`

Permiso amplio de staff para administrar Lodestones globales.

## Modos

`lodestone_teleport.mode.discover`

Fuerza reglas de discovery para ese jugador. El jugador ve Lodestones globales, propias y descubiertas. Puede forzar discovery incluso cuando `networkMode` es `all`.

`lodestone_teleport.mode.all`

Ignora discovery y permite ver todas las Lodestones registradas. Es útil para admins.

Si ambos están configurados, `mode.all` gana en la práctica.

## Admin y Config

`lodestone_teleport.admin`

Permite comandos de diagnóstico/admin como `/warp list`.

`lodestone_teleport.config`

Permite comandos de config y la UI vanilla de config:

```mcfunction
/warp config
/warp reload
```

## Bypass

`lodestone_teleport.bypass_cost`

Permite teletransportarse sin pagar costo.

`lodestone_teleport.bypass_cast`

Permite teletransportarse sin casteo quieto.

`lodestone_teleport.bypass_cooldown`

Permite teletransportarse sin cooldown.

`lodestone_teleport.bypass_max_warps`

Permite registrar Lodestones aunque se alcance `maxLodestonesGlobal` o el límite por permiso del jugador.

## Permisos de Límite

`lodestone_teleport.limit.<numero>`

Define la cantidad máxima de Lodestones que un jugador puede tener. Se usa el número más alto que coincida.

Ejemplos:

- `lodestone_teleport.limit.3`
- `lodestone_teleport.limit.10`
- `lodestone.limit.25`

Usa `lodestone_teleport.bypass_max_warps` para staff o grupos que deban ignorar este límite.

## Permisos Fallback de Config

Las listas de config aceptan:

- nodos completos, como `lodestone_teleport.use`
- nodos cortos, como `use`
- wildcards, como `lodestone_teleport.*`, `lodestone.*` o `*`

Si usas LuckPerms, deja `playerPermissions` y `adminPermissions` mínimos o vacíos para que LuckPerms sea la fuente principal de permisos.

## Ejemplos con LuckPerms

Setup recomendado inicial:

1. Detén el servidor.
2. Abre `config/lodestone_warp_and_tp/lodestone_teleport.json`.
3. Elimina o vacía `playerPermissions` y `adminPermissions` si quieres que LuckPerms controle todos los permisos.
4. Inicia el servidor.
5. Ejecuta `/lp editor`.
6. Agrega los permisos de jugador al grupo `default`.
7. Crea un grupo `admin` y agrega los permisos de admin.
8. Guarda y aplica los cambios del editor de LuckPerms.

Permitir gameplay básico con discovery:

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

Permitir administración global:

```mcfunction
/lp group admin permission set lodestone_teleport.admin true
/lp group admin permission set lodestone_teleport.config true
/lp group admin permission set lodestone_teleport.global true
/lp group admin permission set lodestone_teleport.rename true
/lp group admin permission set lodestone_teleport.remove true
/lp group admin permission set lodestone_teleport.mode.all true
```

## Advertencias de Compatibilidad

Evita estas combinaciones en jugadores normales salvo que sea intencional:

- `lodestone_teleport.mode.all` con `networkMode: discover`: los jugadores ven todas las Lodestones.
- `lodestone_teleport.rename`: pueden renombrar Lodestones ajenas.
- `lodestone_teleport.remove`: pueden desvincular o romper Lodestones ajenas.
- `lodestone_teleport.create.global`: pueden crear Lodestones globales.
