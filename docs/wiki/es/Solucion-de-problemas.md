# Solución de Problemas

[English](../Troubleshooting.md) | [Español](Solucion-de-problemas.md)

**Última actualización:** 2026-07-01

## La UI Vanilla Sale en Inglés o Español

Los clientes vanilla no tienen los archivos de idioma del mod, así que usan los textos fallback del servidor.

Configura:

```json
"serverLanguage": "en_us"
```

o:

```json
"serverLanguage": "es_es"
```

## Los Jugadores No Pueden Usar Warps

Revisa:

- `lodestone_teleport.use`
- si el jugador está cerca de una Lodestone registrada
- si el destino existe físicamente
- si el destino es visible bajo `networkMode`
- si el jugador descubrió el destino en modo discovery

Permiso útil:

```text
lodestone_teleport.use
```

## Un Jugador No Ve un Destino

Revisa `networkMode`.

Si está en:

```json
"networkMode": "discover"
```

el jugador puede ver:

- Lodestones globales
- Lodestones propias
- Lodestones descubiertas
- todas las Lodestones solo si tiene `lodestone_teleport.mode.all`

Usa:

```mcfunction
/warp discover list <jugador>
/warp discover who <id>
```

## Una Lodestone Privada No Se Puede Descubrir

Es intencional.

Las Lodestones privadas son para su dueño. Otro jugador que intente descubrirla o registrarla debería recibir un mensaje indicando que la Lodestone ya está registrada y es privada.

Para hacerla descubrible, el dueño o un admin debe cambiar visibilidad:

```mcfunction
/warp visibility <id> discoverable
```

## Los Jugadores Pueden Romper Lodestones Ajenas

Revisa si tienen:

```text
lodestone_teleport.remove
```

Ese es un permiso amplio de staff y permite remover o romper cualquier Lodestone registrada.

Jugadores normales deberían tener:

```text
lodestone_teleport.own.destroy
```

que solo aplica a Lodestones propias.

## Los Jugadores No Pueden Renombrar sus Lodestones

Revisa:

```text
lodestone_teleport.own.rename
```

Para acceso global de staff:

```text
lodestone_teleport.rename
```

## El Costo de Teleport Se Ve Mal

Revisa:

- `costType`
- `baseCost`
- `blocksPerExtraCost`
- `crossDimensionMultiplier`
- `maxCost`
- si el jugador tiene `lodestone_teleport.bypass_cost`

El default de XP es:

- `baseCost: 1`
- `blocksPerExtraCost: 1000`
- multiplicador interdimensional `2.0`

## El Casteo de Teleport Se Cancela

El jugador se movió demasiado durante el casteo.

Revisa:

```json
"teleportCastSeconds": 2,
"teleportCastMoveTolerance": 0.2
```

Los jugadores con este permiso saltan el casteo:

```text
lodestone_teleport.bypass_cast
```

## Conflicto con Otro Mod de Warps

Comando default:

```mcfunction
/warp
```

Comando fallback:

```mcfunction
/lodestone_warp
```

Puedes cambiar ambos:

```json
"commandName": "warp",
"fallbackCommandName": "lodestone_warp"
```

Cambiar nombres de comando requiere reiniciar el servidor.
