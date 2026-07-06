# Troubleshooting

[English](Troubleshooting.md) | [Español](es/Solucion-de-problemas.md)

**Last updated:** 2026-07-01

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

- `lodestone_teleport.use`
- whether the player is near a registered Lodestone
- whether the destination still exists physically
- whether the destination is visible under `networkMode`
- whether the player has discovered the destination in discovery mode

Useful permission:

```text
lodestone_teleport.use
```

## Players Cannot See a Destination

Check `networkMode`.

If it is:

```json
"networkMode": "discover"
```

the player can see:

- global Lodestones
- Lodestones they own
- Lodestones they have discovered
- all Lodestones only if they have `lodestone_teleport.mode.all`

Use:

```mcfunction
/warp discover list <player>
/warp discover who <id>
```

## A Private Lodestone Cannot Be Discovered

That is intended.

Private Lodestones are for the owner. Another player trying to discover/register it should receive a message saying the Lodestone is already registered and private.

To make it discoverable, the owner or an admin must change visibility:

```mcfunction
/warp visibility <id> discoverable
```

## Players Can Break Other Players' Lodestones

Check whether they have:

```text
lodestone_teleport.remove
```

That is a broad staff permission and allows removing or breaking any registered Lodestone.

Normal players should usually have:

```text
lodestone_teleport.own.destroy
```

which only applies to Lodestones they own.

## Players Cannot Rename Their Own Lodestones

Check:

```text
lodestone_teleport.own.rename
```

For staff-wide rename access, use:

```text
lodestone_teleport.rename
```

## Teleport Cost Looks Wrong

Check:

- `costType`
- `baseCost`
- `blocksPerExtraCost`
- `crossDimensionMultiplier`
- `maxCost`
- whether the player has `lodestone_teleport.bypass_cost`

Default XP behavior is:

- `baseCost: 1`
- `blocksPerExtraCost: 1000`
- cross-dimension multiplier `2.0`

## Teleport Cast Cancels

The player moved too far during the cast.

Check:

```json
"teleportCastSeconds": 2,
"teleportCastMoveTolerance": 0.2
```

Players with this permission skip cast:

```text
lodestone_teleport.bypass_cast
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

Changing command names requires a server restart.
