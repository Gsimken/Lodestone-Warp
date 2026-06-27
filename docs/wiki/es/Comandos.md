# Comandos

**Ultima actualizacion:** 2026-06-27

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

Abrir edicion:

```mcfunction
/warp edit <id>
```

Listar Lodestones registradas:

```mcfunction
/warp list
```

## Teleport por Nombre

`tp` acepta el id o el nombre visible de la Lodestone.

Ejemplos:

```mcfunction
/warp tp lodestone_3
/warp tp Casa
/warp tp Base Principal
```

Si hay varias Lodestones con el mismo nombre, el mod muestra opciones clickeables para elegir el destino exacto.
