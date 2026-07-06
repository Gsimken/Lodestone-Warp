# Comandos

[English](../Commands.md) | [Español](Comandos.md)

**Última actualización:** 2026-07-01

## Comando Principal

```mcfunction
/warp
```

## Comando Fallback

```mcfunction
/lodestone_warp
```

El fallback ayuda a evitar conflictos con otros mods de warps. Si otro mod ya usa `/warp`, usa `/lodestone_warp`.

## Subcomandos

Teleport:

```mcfunction
/warp tp <id o nombre>
```

Renombrar:

```mcfunction
/warp rename <id> <nombre>
```

Abrir edición:

```mcfunction
/warp edit <id>
```

Desvincular un warp de Lodestone de la red:

```mcfunction
/warp remove <id>
/warp unlink <id>
```

El bloque físico de Lodestone queda en el mundo. Por defecto, no se volverá a registrar con click normal; usa click derecho agachado con la mano vacía para registrarlo otra vez de forma intencional.

Cambiar visibilidad:

```mcfunction
/warp visibility <id> <private|discoverable|global>
```

Helper global:

```mcfunction
/warp global <id> <true|false>
```

Listar Lodestones registradas:

```mcfunction
/warp list
```

`/warp list` muestra acciones clicables compactas después de cada entrada:

- `[TP]`: teletransportarse a la Lodestone.
- `[✎]`: abrir el flujo de edición.
- `[X]`: desvincular el warp y dejar el bloque físico de Lodestone en su lugar.

## Comandos de Discovery

Dar discovery:

```mcfunction
/warp discover grant <jugador> <id|all>
```

Cuando usas `all`, las Lodestones privadas quedan excluidas por defecto. Para incluir Lodestones privadas intencionalmente, usa:

```mcfunction
/warp discover grant <jugador> all add_private=true
```

Quitar discovery:

```mcfunction
/warp discover revoke <jugador> <id|all>
```

Ver qué descubrió un jugador:

```mcfunction
/warp discover list <jugador>
```

Ver quién descubrió una Lodestone:

```mcfunction
/warp discover who <id>
```

## Comandos de Config

Recargar config:

```mcfunction
/warp reload
```

Abrir UI vanilla de config:

```mcfunction
/warp config
```

Listar claves de config:

```mcfunction
/warp config list
```

Leer o cambiar un valor:

```mcfunction
/warp config get <key>
/warp config set <key> <value>
```

Cambiar `commandName` o `fallbackCommandName` requiere reiniciar el servidor porque los comandos se registran durante el inicio.

## Teleport por Nombre

`tp` acepta el id o el nombre visible de la Lodestone.

Ejemplos:

```mcfunction
/warp tp lodestone_3
/warp tp Casa
/warp tp Base Principal
```

Si hay varias Lodestones con el mismo nombre, el mod muestra opciones clicables para elegir el destino exacto.
