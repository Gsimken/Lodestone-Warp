# Solución de Problemas

**Última actualización:** 2026-06-27

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

- `requirePermissions`
- permisos de LuckPerms
- si el jugador está cerca de una Lodestone registrada
- si la Lodestone existe físicamente

Permiso:

```text
lodestone_teleport.use
```

## Los Jugadores No Pueden Renombrar

Revisa:

```text
lodestone_teleport.rename
```

O desactiva permisos:

```json
"requirePermissions": false
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
