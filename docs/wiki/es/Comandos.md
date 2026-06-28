# Comandos

**Última actualización:** 2026-06-27

## Comando Principal

```mcfunction
/warp
```

## Comando Fallback

```mcfunction
/lodestone_warp
```

El fallback ayuda a evitar conflictos con otros mods de warps.

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

Remover una entrada antigua:

```mcfunction
/warp remove <id>
```

Listar Lodestones registradas:

```mcfunction
/warp list
```

`/warp list` muestra acciones clicables compactas después de cada entrada:

- `[TP]`: teletransportarse a la Lodestone.
- `[✎]`: abrir el flujo de renombrado.
- `[X]`: remover la entrada solo si el bloque físico de Lodestone ya no existe.

## Teleport por Nombre

`tp` acepta el id o el nombre visible de la Lodestone.

Ejemplos:

```mcfunction
/warp tp lodestone_3
/warp tp Casa
/warp tp Base Principal
```

Si hay varias Lodestones con el mismo nombre, el mod muestra opciones clicables para elegir el destino exacto.
