# Configuración

**Última actualización:** 2026-07-01

La configuración se genera en el primer inicio:

```text
config/lodestone_teleport.json
```

Cuando se carga una configuración existente, Lodestone Warps rellena las opciones faltantes con los defaults actuales y vuelve a escribir el archivo normalizado.

Si Mod Menu está instalado en el cliente, Lodestone Warps también muestra una pantalla opcional de configuración dentro del juego. Esta pantalla edita solo la config local; los servidores remotos siguen usando su propia configuración server-side.

Los dueños de servidor también pueden usar la UI vanilla de config:

```mcfunction
/warp config
```

## Ejemplo

```json
{
  "costItem": "minecraft:diamond",
  "costType": "xp_levels",
  "baseCost": 1,
  "blocksPerExtraCost": 1000,
  "crossDimensionMultiplier": 2.0,
  "maxCost": 64,
  "allowCrossDimension": true,
  "allowPersonalLodestones": true,
  "defaultLodestoneVisibility": "discoverable",
  "maxLodestonesGlobal": 0,
  "maxLodestonesPerPlayer": 0,
  "registerPlacedLodestonesOnlyWhenSneaking": true,
  "autoRegisterUntrackedLodestones": false,
  "maxDialogDestinations": 24,
  "teleportSourceRange": 8,
  "teleportCastSeconds": 2,
  "teleportCastMoveTolerance": 0.2,
  "teleportCooldownSeconds": 3,
  "teleportEffects": true,
  "vanillaTeleportEffect": "end",
  "modTeleportEffect": "lodestone",
  "networkMode": "discover",
  "playerPermissions": [
    "lodestone_teleport.use",
    "lodestone_teleport.create",
    "lodestone_teleport.create.private",
    "lodestone_teleport.create.discoverable",
    "lodestone_teleport.own.rename",
    "lodestone_teleport.own.remove",
    "lodestone_teleport.own.destroy",
    "lodestone_teleport.own.visibility.private",
    "lodestone_teleport.own.visibility.discoverable",
    "lodestone_teleport.mode.discover"
  ],
  "adminPermissions": [
    "lodestone_teleport.admin",
    "lodestone_teleport.config",
    "lodestone_teleport.global",
    "lodestone_teleport.rename",
    "lodestone_teleport.remove",
    "lodestone_teleport.mode.all",
    "lodestone_teleport.create.global",
    "lodestone_teleport.own.visibility.global",
    "lodestone_teleport.bypass_cost",
    "lodestone_teleport.bypass_cast",
    "lodestone_teleport.bypass_cooldown",
    "lodestone_teleport.bypass_max_warps"
  ],
  "commandName": "warp",
  "fallbackCommandName": "lodestone_warp",
  "serverLanguage": "en_us"
}
```

## Costos

`costType`

Controla cómo se paga el teleport.

- `xp_levels`: paga niveles de experiencia.
- `item`: paga el item configurado en `costItem`.

`costItem`

Item usado como pago cuando `costType` es `item`.

`baseCost`

Costo mínimo. Por defecto es `1`.

`blocksPerExtraCost`

Agrega costo extra según distancia en la misma dimensión. Con el default `1000`, el costo aumenta en 1 cada 1000 bloques.

`crossDimensionMultiplier`

Multiplicador para teleports entre dimensiones.

`maxCost`

Costo máximo final. Usa `0` para no limitarlo.

## Registro y Visibilidad

`allowPersonalLodestones`

Permite que los jugadores creen y mantengan Lodestones privadas.

`defaultLodestoneVisibility`

Visibilidad asignada a las Lodestones nuevas cuando el jugador tiene permiso para ese tipo.

Valores soportados:

- `private`
- `discoverable`
- `global`

`maxLodestonesGlobal`

Cantidad máxima de Lodestones registradas en el servidor.

- `0`: ilimitado.
- Valores positivos limitan toda la red.

`maxLodestonesPerPlayer`

Cantidad máxima de Lodestones registradas por cada jugador.

- `0`: ilimitado.
- Jugadores con `lodestone_teleport.bypass_max_warps` ignoran este límite y el límite global.

`registerPlacedLodestonesOnlyWhenSneaking`

Controla el registro automático al colocar una Lodestone.

- `true`: solo colocar agachado registra la Lodestone como warp.
- `false`: toda Lodestone colocada se registra.

`autoRegisterUntrackedLodestones`

Controla qué pasa cuando un jugador hace click derecho con la mano vacía sobre una Lodestone no registrada.

- `false`: el bloque queda vanilla y no se registra con click normal.
- `true`: el bloque se registra al usarlo si el jugador puede crear Lodestones y los límites lo permiten.

Incluso cuando esta opción es `false`, un jugador puede hacer click derecho agachado con la mano vacía sobre una Lodestone no registrada para registrarla de forma intencional, siempre que tenga permiso de crear y los límites lo permitan.

## Discovery

`networkMode`

Controla qué Lodestones pueden ver y usar los jugadores.

- `all`: los jugadores ven todas las Lodestones registradas.
- `discover`: los jugadores ven Lodestones globales, propias y descubiertas.

Default:

```json
"networkMode": "discover"
```

Las Lodestones privadas no pueden ser descubiertas ni registradas por otra persona. Si alguien lo intenta, recibe un mensaje específico indicando que ya está registrada y es privada.

## Reglas de Teleport

`allowCrossDimension`

Permite o bloquea teleports entre dimensiones.

`teleportSourceRange`

El jugador debe estar cerca de una Lodestone registrada para teletransportarse.

- `8`: radio por defecto en bloques.
- `0`: desactiva esta restricción.

`teleportCastSeconds`

Tiempo de casteo quieto antes de teletransportarse.

- `2`: duración por defecto en segundos.
- `0`: desactiva el casteo y teletransporta inmediatamente.

Los jugadores con `lodestone_teleport.bypass_cast` saltan el casteo.

`teleportCastMoveTolerance`

Distancia que el jugador puede moverse durante el casteo antes de cancelarlo.

`teleportCooldownSeconds`

Cooldown server-side después de un teleport exitoso.

Los jugadores con `lodestone_teleport.bypass_cooldown` saltan el cooldown.

## Efectos de Teleport

`teleportEffects`

Activa o desactiva partículas y sonidos de teleport.

`vanillaTeleportEffect`

Preset de efecto usado para clientes vanilla. Default: `end`.

`modTeleportEffect`

Preset de efecto usado cuando el jugador tiene Lodestone Warps instalado en el cliente. Default: `lodestone`.

Presets soportados:

- `end`: partículas estilo End/portal y sonido de teleport de Enderman.
- `lodestone`: partículas más brillantes estilo Lodestone usando portal, end rod y enchant.
- `none` u `off`: desactiva ese preset.

## UI

`maxDialogDestinations`

Cantidad máxima de destinos mostrados en la UI vanilla.

La UI de mod tiene paginación.

## Permisos

`playerPermissions`

Permisos default entregados a todos los jugadores cuando ningún administrador de permisos responde.

`adminPermissions`

Permisos default entregados a admins OP/gamemaster cuando ningún administrador de permisos responde.

LuckPerms u otro administrador compatible con Fabric Permissions es lo recomendado para grupos, jugadores específicos e herencia. Si un administrador de permisos responde, esa respuesta gana sobre estos defaults.

## Overrides de Permisos para Desarrollo

Para debug local usa system properties de JVM:

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.rename=false
```

Ejemplo con permisos granulares:

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.create=true -Dlodestone_teleport.create.private=true -Dlodestone_teleport.own.rename=true -Dlodestone_teleport.own.destroy=false
```

Estos overrides son solo para desarrollo y tienen prioridad sobre los defaults de config.

## Comandos

`commandName`

Comando principal. Default:

```json
"commandName": "warp"
```

`fallbackCommandName`

Comando seguro de fallback. Default:

```json
"fallbackCommandName": "lodestone_warp"
```

## Idioma del Servidor

`serverLanguage` controla los textos fallback que envía el servidor, principalmente para la UI vanilla.

Valores soportados:

- `en_us`
- `es_es`
