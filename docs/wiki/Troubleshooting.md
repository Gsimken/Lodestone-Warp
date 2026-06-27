# Troubleshooting

**Last updated:** 2026-06-27

## Vanilla UI Shows English or Spanish Unexpectedly

Vanilla clients do not have the mod language files, so they use server-side fallback text.

Set:

```json
"serverLanguage": "en_us"
```

or:

```json
"serverLanguage": "es_es"
```

## Players Cannot Use Warps

Check:

- `requirePermissions`
- LuckPerms permissions
- whether the player is near a registered Lodestone
- whether the Lodestone still exists physically

Useful permission:

```text
lodestone_teleport.use
```

## Players Cannot Rename Lodestones

Check:

```text
lodestone_teleport.rename
```

Or disable permission checks:

```json
"requirePermissions": false
```

## Command Conflict With Another Warp Mod

The default command is:

```mcfunction
/warp
```

The safe fallback command is:

```mcfunction
/lodestone_warp
```

You can change both in config:

```json
"commandName": "warp",
"fallbackCommandName": "lodestone_warp"
```
