# Configuración

[English](../Configuration.md) | [Español](Configuracion.md)

**Última actualización:** 2026-07-13

La configuración se genera en el primer inicio:

```text
config/lodestone_warp_and_tp/lodestone_teleport.json
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
  "registerPlacedLodestonesOnlyWhenSneaking": true,
  "autoRegisterUntrackedLodestones": false,
  "maxDialogDestinations": 10,
  "vanillaDialogDestinationColumnWidth": 245,
  "vanillaDialogCostColumnWidth": 70,
  "vanillaDialogEditColumnWidth": 70,
  "vanillaDialogColumnOrder": "c,d,e",
  "showVanillaDialogHeaderNavigation": true,
  "showVanillaDialogButtonNavigation": true,
  "showVanillaDialogDestinationSuffix": false,
  "vanillaDialogDestinationSuffix": "[{x}, {y}, {z}, {dimension}]",
  "teleportSourceRange": 8,
  "teleportSourceYRange": 3,
  "teleportCastSeconds": 2,
  "teleportCastMoveTolerance": 0.2,
  "teleportCooldownSeconds": 3,
  "teleportEffects": true,
  "vanillaTeleportEffect": "end",
  "modTeleportEffect": "lodestone",
  "networkMode": "discover",
  "playerPermissions": {
    "lodestone_teleport.use": true,
    "lodestone_teleport.rename": false,
    "lodestone_teleport.create": true,
    "lodestone_teleport.create.private": true,
    "lodestone_teleport.create.discoverable": true,
    "lodestone_teleport.create.global": false,
    "lodestone_teleport.own.rename": true,
    "lodestone_teleport.own.remove": true,
    "lodestone_teleport.own.destroy": true,
    "lodestone_teleport.own.visibility.private": true,
    "lodestone_teleport.own.visibility.discoverable": true,
    "lodestone_teleport.own.visibility.global": false,
    "lodestone_teleport.mode.all": false,
    "lodestone_teleport.mode.discover": false
  },
  "adminPermissions": {
    "lodestone_teleport.admin": true,
    "lodestone_teleport.config": true,
    "lodestone_teleport.global": true,
    "lodestone_teleport.rename": true,
    "lodestone_teleport.remove": true,
    "lodestone_teleport.mode.all": true,
    "lodestone_teleport.create.global": true,
    "lodestone_teleport.own.visibility.global": true,
    "lodestone_teleport.bypass_cost": true,
    "lodestone_teleport.bypass_cast": true,
    "lodestone_teleport.bypass_cooldown": true,
    "lodestone_teleport.bypass_max_warps": true
  },
  "commandName": "warp",
  "fallbackCommandName": "lodestone_warp",
  "serverLanguage": "en_us",
  "pauseGameInSingleplayerUi": true
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

Los límites de Lodestones por jugador ahora se controlan por permisos. Usa permisos como `lodestone_teleport.limit.5` o `lodestone.limit.10`; se aplica el número más alto encontrado. Jugadores con `lodestone_teleport.bypass_max_warps` ignoran el límite por jugador y el límite global.

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

Los permisos de modo pueden sobrescribir este default por jugador:

- `lodestone_teleport.mode.discover` fuerza reglas de discovery incluso si `networkMode` es `all`.
- `lodestone_teleport.mode.all` ignora discovery incluso si `networkMode` es `discover`.

Default:

```json
"networkMode": "discover"
```

Las Lodestones privadas no pueden ser descubiertas ni registradas por otra persona. Si alguien lo intenta, recibe un mensaje específico indicando que ya está registrada y es privada.

## Reglas de Teleport

`allowCrossDimension`

Permite o bloquea teleports entre dimensiones.

`teleportSourceRange`

Rango horizontal X/Z alrededor de una Lodestone registrada para teletransportarse.

- `8`: radio por defecto en bloques.
- `0`: desactiva la validación horizontal.

`teleportSourceYRange`

Rango vertical Y alrededor de una Lodestone registrada para teletransportarse.

- `3`: rango vertical por defecto en bloques.
- `0`: desactiva la validación vertical.

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

Default: `10`.

La UI de mod tiene paginación.

`vanillaDialogDestinationColumnWidth`

Ancho de la columna de destino en la UI vanilla. Default: `245`.

`vanillaDialogCostColumnWidth`

Ancho de la columna de costo en la UI vanilla. Default: `70`.

`vanillaDialogEditColumnWidth`

Ancho de la columna de editar/espaciador en la UI vanilla. Default: `70`.

`vanillaDialogColumnOrder`

Orden de columnas para la grilla de destinos de la UI vanilla. Default: `c,d,e`.

- `c`: columna de costo
- `d`: columna de destino
- `e`: columna de editar

Ejemplos: `c,d,e`, `d,c,e`, `d,e,c`.

`showVanillaDialogHeaderNavigation`

Muestra flechas de página clickeables junto al texto de página en el cuerpo del Dialog vanilla. Default: `true`.

`showVanillaDialogButtonNavigation`

Muestra botones de flecha junto a Buscar y Editar este warp en la grilla vanilla. Default: `true`.

`showVanillaDialogDestinationSuffix`

Muestra información extra configurable después del nombre del destino en la UI vanilla. Default: `false`.

`vanillaDialogDestinationSuffix`

Patrón de sufijo que se agrega al nombre del destino cuando está activado.

Ejemplos:

```json
"vanillaDialogDestinationSuffix": "[{x}, {y}, {z}, {dimension}]"
```

```json
"vanillaDialogDestinationSuffix": "({x}, {z}, {owner})"
```

Placeholders soportados:

- `{x}`
- `{y}`
- `{z}`
- `{dimension}`
- `{owner}`

## Permisos

`playerPermissions`

Permisos default entregados a todos los jugadores cuando ningún administrador de permisos responde.

`adminPermissions`

Permisos default extra entregados a admins OP/gamemaster cuando ningún administrador de permisos responde. Son aditivos: los OP/gamemaster mantienen `playerPermissions` y además reciben los `adminPermissions` activos.

Ambas configs de permisos son mapas:

```json
"lodestone_teleport.use": true,
"lodestone_teleport.rename": false
```

Esto permite desactivar un permiso sin eliminarlo. Cuando Lodestone Warps agregue permisos conocidos en el futuro, las claves faltantes pueden escribirse como `false` para mantener el archivo auditable.

LuckPerms u otro administrador compatible con Fabric Permissions es lo recomendado para grupos, jugadores específicos e herencia. Si LuckPerms está instalado, Lodestone Warps trata LuckPerms como fuente de verdad y no concede permisos positivos desde estos mapas fallback.

`pauseGameInSingleplayerUi`

Opción de cliente usada solo cuando la UI de mod se abre en singleplayer. Los servidores dedicados ignoran este valor.

- `true`: el mundo singleplayer se pausa mientras la UI de cliente está abierta.
- `false`: el mundo singleplayer sigue avanzando mientras la UI de cliente está abierta.

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
