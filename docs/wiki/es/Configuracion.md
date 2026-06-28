# Configuración

**Última actualización:** 2026-06-27

La configuración se genera en el primer inicio:

```text
config/lodestone_teleport.json
```

Cuando se carga una configuración existente, Lodestone Warps rellena las opciones faltantes con los defaults actuales y vuelve a escribir el archivo normalizado.

Si Mod Menu está instalado en el cliente, Lodestone Warps también muestra una pantalla opcional de configuración dentro del juego. Esta pantalla edita solo la config local; los servidores remotos siguen usando su propia configuración server-side.

## Ejemplo

```json
{
  "costItem": "minecraft:diamond",
  "baseCost": 1,
  "blocksPerExtraCost": 500,
  "crossDimensionMultiplier": 2.0,
  "maxCost": 64,
  "allowCrossDimension": true,
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
  "requirePermissions": false,
  "commandName": "warp",
  "fallbackCommandName": "lodestone_warp",
  "serverLanguage": "en_us"
}
```

## Idioma del Servidor

`serverLanguage` controla los textos fallback que envía el servidor, principalmente para la UI vanilla.

Clientes vanilla no tienen los archivos de idioma del mod, por eso usan estos fallbacks.

Valores soportados:

```json
"serverLanguage": "en_us"
```

```json
"serverLanguage": "es_es"
```

Si quieres que la UI vanilla se vea en español, usa:

```json
"serverLanguage": "es_es"
```

Si quieres que se vea en inglés, deja el default:

```json
"serverLanguage": "en_us"
```

## Costos

`costItem`

Item usado como pago del teleport.

`baseCost`

Costo mínimo.

`blocksPerExtraCost`

Agrega costo extra según distancia en la misma dimensión.

`crossDimensionMultiplier`

Multiplicador para teleports entre dimensiones.

`maxCost`

Costo máximo final. Usa `0` para no limitarlo.

## Registro de Lodestones

`maxLodestonesGlobal`

Cantidad maxima de Lodestones registradas en el servidor.

- `0`: ilimitado.
- Valores positivos limitan toda la red global.

`maxLodestonesPerPlayer`

Cantidad maxima de Lodestones registradas por cada jugador.

- `0`: ilimitado.
- Jugadores con `lodestone_teleport.bypass_max_warps` ignoran este limite y el limite global.

`registerPlacedLodestonesOnlyWhenSneaking`

Controla el registro automatico al colocar una Lodestone.

- `true`: solo colocar agachado registra la Lodestone como warp.
- `false`: toda Lodestone colocada se registra.

`autoRegisterUntrackedLodestones`

Controla que pasa cuando un jugador hace click derecho con la mano vacia sobre una Lodestone no registrada.

- `false`: el bloque queda vanilla y no se registra.
- `true`: el bloque se registra al usarlo si el jugador puede crear Lodestones y los limites lo permiten.

Incluso cuando esta opcion es `false`, un jugador puede hacer click derecho agachado con la mano vacia sobre una Lodestone no registrada para registrarla de forma intencional, siempre que tenga permiso de crear y los limites lo permitan.

## Reglas de Teleport

`allowCrossDimension`

Permite o bloquea teleports entre dimensiones.

`teleportSourceRange`

El jugador debe estar cerca de una Lodestone registrada para teletransportarse.

- `8`: radio por defecto en bloques.
- `0`: desactiva esta restricción.

`teleportCooldownSeconds`

Cooldown server-side después de un teleport exitoso.

- `3`: cooldown por defecto en segundos.
- `0`: desactiva el cooldown.
- Cualquier valor positivo se mide en segundos.

`teleportCastSeconds`

Tiempo de canalización quieto antes de teletransportarse.

- `2`: duración por defecto en segundos.
- `0`: desactiva la canalización y teletransporta inmediatamente.

`teleportCastMoveTolerance`

Distancia que el jugador puede moverse durante la canalización antes de cancelarla.

- `0.2`: tolerancia por defecto en bloques.
- `0`: exige que el jugador esté completamente quieto.

## Efectos de Teleport

`teleportEffects`

Activa o desactiva partículas y sonidos de teleport.

`vanillaTeleportEffect`

Preset de efecto usado para clientes vanilla.

Default:

```json
"vanillaTeleportEffect": "end"
```

`modTeleportEffect`

Preset de efecto usado cuando el jugador tiene Lodestone Warps instalado en el cliente.

Default:

```json
"modTeleportEffect": "lodestone"
```

Presets soportados:

- `end`: partículas estilo End/portal y sonido de teleport de Enderman.
- `lodestone`: partículas más brillantes estilo Lodestone usando portal, end rod y enchant.
- `none` u `off`: desactiva ese preset.

## Permisos

`requirePermissions`

- `true`: usa LuckPerms/Fabric Permissions API.
- `false`: todos pueden usar y renombrar Lodestones.

## Overrides de Permisos para Desarrollo

Para debug local, `runClient` y `runServer` pueden simular permisos sin LuckPerms:

```powershell
.\gradlew.bat runClient -Plodestone_teleport.use=true -Plodestone_teleport.rename=false
```

Ejemplo con permisos granulares:

```powershell
.\gradlew.bat runClient -Plodestone_teleport.use=true -Plodestone_teleport.create=false -Plodestone_teleport.bypass_cost=true
```

También se pueden pasar como system properties de JVM:

```powershell
.\gradlew.bat runClient -Dlodestone_teleport.use=true -Dlodestone_teleport.rename=true
```

Estos overrides son solo para desarrollo y tienen prioridad sobre `requirePermissions`.

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
