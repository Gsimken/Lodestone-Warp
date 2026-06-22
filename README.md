# Lodestone Warps

Fabric server-side mod for Minecraft 26.2.

Version 0 implements a first playable lodestone-based warp network:

- Minecraft 26.2
- Java 25
- Fabric Loader 0.19.3
- Fabric API 0.152.2+26.2
- Mojang/unobfuscated names, matching Fabric guidance for 26.1+

Gameplay loop:

1. Register a lodestone when a player places it.
2. Remove it when the block is broken.
3. Open a vanilla dialog when a player right-clicks a registered lodestone with an empty hand.
4. Consume the configured item cost.
5. Warp to the selected registered lodestone.

Sneak while placing a lodestone to open a vanilla rename dialog. Existing lodestones are registered automatically the first time they are used with an empty hand.

Server config is generated at `config/lodestone_teleport.json` on first run.

Teleport cost is dynamic by default:

- `baseCost`: minimum item cost.
- `blocksPerExtraCost`: adds 1 cost per this many same-dimension blocks.
- `crossDimensionMultiplier`: multiplier for cross-dimension teleports.
- `maxCost`: caps the final cost when greater than 0.

V2 note: keep the vanilla dialog flow for clients without the mod, and consider adding a custom client UI for players who do install Lodestone Warps client-side.
