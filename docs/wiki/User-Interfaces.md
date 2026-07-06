# User Interfaces

[English](User-Interfaces.md) | [Español](es/Interfaces.md)

**Last updated:** 2026-07-01

Lodestone Warps has two user interfaces. The server chooses automatically depending on whether the player has the client mod installed.

## Vanilla UI

The vanilla UI is Minecraft's Dialog UI sent by the server.

It is used when the player does not have Lodestone Warps installed on the client.

Features:

- destination buttons
- search input
- edit/rename flow
- visibility actions when permitted
- server config Dialog UI for admins
- server-side validation
- no client mod required

Vanilla UI buttons can show colored text and simple symbols, but they cannot render full custom layouts or item icons like the mod UI.

Because vanilla clients do not have the mod language files, server-side fallback text is controlled by:

```json
"serverLanguage": "en_us"
```

## Mod UI

The mod UI is the custom client screen shown when the player has Lodestone Warps installed.

Features:

- table-style destination list
- search
- pagination
- XP/item cost icon display
- destination edit buttons
- visibility icons:
  - 🌐 global
  - 🔒 private
- current Lodestone visibility icon in the header
- instant refresh after save/remove actions

## Mod Edit Screen

The mod edit screen is staged:

```text
[textbox]
[Mode][Remove]

[Save][Cancel]
```

- `Mode` cycles locally through the visibility modes the player is allowed to use.
- `Save` applies name and visibility together.
- `Cancel` closes without saving local changes.
- `Remove` unlinks the Lodestone immediately when permitted.

The server validates permissions again before applying any change.

## Mod Menu Config Screen

When Mod Menu is installed, Lodestone Warps exposes a client-side config editor.

This screen edits the local config file. Remote servers still use their own server-side config.

## Notes

Players without the client mod are still fully supported through vanilla UI.
