# User Interfaces

**Last updated:** 2026-06-27

## UI Vanilla

The UI vanilla is the Minecraft Dialog UI sent by the server.

It is used when the player does not have Lodestone Warps installed on the client.

Features:

- destination buttons
- search
- rename flow
- server-side validation
- no client mod required

Because vanilla clients do not have the mod language files, server-side fallback text is controlled by:

```json
"serverLanguage": "en_us"
```

## UI de Mod

The UI de mod is the custom client screen shown when the player has Lodestone Warps installed.

Features:

- table-style destination list
- search
- pagination
- cost item icons
- destination edit buttons
- instant refresh after rename

The server automatically detects whether the client supports the custom UI.

Players without the mod still use UI vanilla.
