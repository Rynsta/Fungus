```
fungus.client                                         
─────────────────────────────────────────────────────────────
skyblock qol mod  ·  fabric  ·  minecraft 1.21.11
```

## modules

- **viewmodel** — offset, scale, rotation, swing speed, no-haste, no-equip animation, no-bow-swing
- **potion icon hider** — hides effect icons in the HUD and the inventory side panel
- **scoreboard hider** — hides the vanilla / Hypixel sidebar scoreboard
- **block highlight** - highlights the colour you are currently looking at, with colour options

## commands

| command       | opens                           |
| ------------- | ------------------------------- |
| `/fungus`     | module list                     |
| `/viewmodel`  | viewmodel settings (shortcut)   |

`ESC` to close. State is persisted to `config/fungus-viewmodel.json`.

## build

```sh
./gradlew build
```

Output → `build/libs/fungus-viewmodel-<version>.jar`. Drop into `mods/`.

## requires

Minecraft **1.21.11**  ·  Fabric Loader **0.16+**  ·  Fabric API
