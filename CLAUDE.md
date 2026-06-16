# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Fungus is a **client-side** Fabric mod for **Minecraft 26.1.2** (Java 25) — a Hypixel SkyBlock QoL toolkit
of toggleable modules (viewmodel tweaks, potion-icon hider, scoreboard hider, block highlight) with a custom
in-game GUI. There is no server/common logic of substance; `environment` is `client`.

## Build & run

```sh
./gradlew build       # → build/libs/fungus-viewmodel-<version>.jar  (drop into mods/)
./gradlew runClient   # launch Minecraft with the mod for manual testing
```

On Windows PowerShell use `.\gradlew.bat build`. The build targets Java 25 (`options.release = 25`); a JDK 25
is required.

**MC 26.1.2 is unobfuscated** — it ships with Mojang's official names, so there are **no Yarn mappings**:
`gradle.properties` has no `yarn_mappings` and `build.gradle` has no `mappings` dependency. Every `net.minecraft.*`
/ `com.mojang.*` reference uses the official name (`Minecraft`, `GuiGraphicsExtractor`, `PoseStack`,
`ItemInHandRenderer`, `Component`, `RenderType`, …). To confirm an official class/method/field name or a mixin
`@At` target, inspect the deobfuscated jar with `javap` (loom caches it under
`~/.gradle/caches/fabric-loom/minecraftMaven/.../minecraft-merged-deobf-26.1.2.jar`) — don't guess.

**There is no automated test suite** — no `src/test`, no test framework. Verification is manual: `runClient`,
join a world, exercise the module, and check `config/fungus-viewmodel.json`. Don't invent test commands.

## Architecture

### Two entry points (Fabric convention)
- `Fungus` (`ModInitializer`, `main`) — a near-empty stub that only logs. Don't put logic here.
- `FungusClient` (`ClientModInitializer`, `client`) — the real entry point: loads config, registers the
  block-highlight world renderer, registers the `/fungus`, `/viewmodel`, `/blockhighlight` commands.

### The central pattern: module layer ⟂ feature-state layer, bridged by public static fields
This is the design that ties the whole codebase together and is not obvious from any single file:

- **`module/`** — the GUI-facing abstraction. `Module` (interface: `id`, `displayName`, `isEnabled/setEnabled`,
  `hasSettings`, `createSettingsScreen`) and `Modules` (a static registry of singleton instances). The GUI
  iterates `Modules.all()`; it never touches feature internals directly.
- **`feature/`** — plain classes (`Viewmodel`, `BlockHighlight`) that are **never instantiated**. They hold the
  module's live runtime state as **`public static` mutable fields** plus a `resetDefaults()`.
- **Mixins and renderers read those static fields directly** (e.g. `Viewmodel.isActive()`,
  `BlockHighlight.fillColor`). Mixins live in their own package and can't reach module singletons, so static
  fields are the deliberate bridge between "what the user toggled" and "the bytecode injected into Minecraft."

**Two state-storage styles coexist** — know which you're touching:
- Modules with settings (`ViewmodelModule`, `BlockHighlightModule`) keep their state in the matching
  `feature/` class.
- Simple hiders (`PotionIconHiderModule`, `ScoreboardHiderModule`) have **no feature class**; their single
  `enabled` flag is a `public static` field on the module class itself, read straight from the mixin.

### Config: one flat DTO mirroring all live state
`ViewmodelConfig` is a single GSON POJO holding flat fields for **every module's state** (the `viewmodel` name
is historical — see Gotchas). `fromLive()` copies the static fields into it; `applyToLive()` copies back.
`ConfigManager.load()` runs at client init (→ `applyToLive`); `save()` serializes on a daemon thread.
**Saves are triggered from `Screen.removed()`** — i.e. closing any settings/main screen persists. There is no
other save path, so a new stateful screen must override `removed()` to call `ConfigManager.save()`.
File: `<config>/fungus-viewmodel.json`.

### GUI: hand-drawn screens + live-binding widgets
MC 26.1.2 replaced immediate-mode `DrawContext` with the retained-mode **`GuiGraphicsExtractor`** ("extract
render state") model. Screens extend vanilla `Screen` and paint themselves by overriding
**`extractRenderState(GuiGraphicsExtractor, …)`** (not `render`), calling `ctx.fill()` rectangles and `ctx.text()`
with the shared `Theme` palette (dark, cyan accent). Custom flat widgets in `gui/widgets/` (`FlatToggle`,
`FlatSlider`, `FlatButton`, `FlatColorRow`, `ModuleRowWidget`) extend `AbstractWidget` / `AbstractSliderButton`
and override **`extractWidgetRenderState(GuiGraphicsExtractor, …)`**; mouse handlers take `MouseButtonEvent`.
Widgets bind to state via **lambdas over the static fields** (`() -> Viewmodel.enabled`, `v -> Viewmodel.enabled
= v`; `DoubleSupplier`/`DoubleConsumer` for sliders), so edits mutate live state immediately — the in-world
preview updates as you drag. After `resetDefaults()`, screens call `rebuild()` (`clearWidgets()` + `init()`) to
resync widget positions/values. All screens use `isPauseScreen() == false` so the world keeps rendering behind
the menu.

### Commands defer screen opening by one tick
A command executes on the wrong thread to call `setScreen` directly, so `FungusClient` stashes a
`Function<Screen,Screen>` factory in an `AtomicReference` and applies it on the next `END_CLIENT_TICK`. Any new
command that opens a GUI must follow this same deferral.

### Mixins & rendering
- Registered in `src/main/resources/fungus.mixins.json` (all under `client`); a new mixin must be added there
  or it won't load. Compatibility level `JAVA_25`. **MixinExtras** is used (`@WrapOperation`).
- Mixin targets use official class names: `HeldItemRendererMixin` → `ItemInHandRenderer`, `InGameHudMixin` →
  `Gui` (the HUD hiders cancel `extractEffects` / `extractScoreboardSidebar`), `EffectsInInventoryMixin` →
  `EffectsInInventory` (inventory-screen potion icons). There is no longer a `CameraAccessor` — the renderer
  reads the camera via the public `gameRenderer.getMainCamera().position()`.
- `LivingEntityMixin` reimplements the entire swing-timer (custom swing speed / no-haste) using `@Unique`
  fields rather than tweaking vanilla values (targets `getCurrentSwingDuration` / `swing` / `updateSwingTime` /
  `getAttackAnim`).
- `BlockHighlightRenderer` registers a `LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN` callback and uses a custom
  `RenderPipeline` + `RenderType` (built through the `RenderTypeInvoker` `@Invoker`, since `RenderType.create`
  is package-private) for the translucent fill. Colors are packed ARGB ints unpacked to float RGBA. The outline
  is drawn as thin quads through that same layer (the vanilla LINES type needs a per-vertex line-width element
  the custom `VertexConsumer` can't supply).
- `LevelRendererMixin` cancels vanilla's `renderBlockOutline` whenever the Block Highlight module is active (and
  a fill/outline sub-toggle is on) so the mod's highlight does not z-fight with the vanilla wireframe.

### `sample/` is reference code, not part of the build
The repo-root `sample/` directory holds adapted source from other mods (`nofrills.*`, credited NoFrills/CHUD)
that the viewmodel transforms were derived from. It sits **outside `src/`, so it is never compiled**. Treat it
as read-only reference; don't wire it into the build or edit it expecting effects.

## Adding a module (the wiring checklist)
1. Add static state: either a new `feature/` class (if it has settings) or a `public static boolean enabled`
   on the module class (for a simple toggle).
2. Read that state from a mixin (HUD/render hook) or renderer. Register any new mixin in `fungus.mixins.json`.
3. Implement `Module`; register the singleton in `Modules`.
4. Add fields to `ViewmodelConfig` and copy them in both `fromLive()` and `applyToLive()` (easy to forget one
   half — state silently won't persist).
5. If it has a settings screen, build it from the existing `Theme` + `gui/widgets/` and override `removed()` to
   `ConfigManager.save()`. Add display/label strings to `assets/fungus/lang/en_us.json`.

## Gotchas
- **The `viewmodel` name is legacy and now spans the whole mod**: `archives_base_name`, `rootProject.name`, the
  jar, the config file (`fungus-viewmodel.json`), and the `ViewmodelConfig` class all say "viewmodel" even
  though they cover all four modules. The mod id and Java package are `fungus`. Renaming the artifact or config
  path would orphan existing user configs and change release tags — avoid unless intended.
- **CI auto-releases from `main`**: `.github/workflows/build.yml` (JDK 25) builds on every push/PR to `main`, and
  on push to `main` it creates a GitHub release tagged `v<mod_version>+build.<run_number>` with a git-log
  changelog. Bumping `mod_version` in `gradle.properties` is what cuts a new versioned release — but note that
  **every** push to `main` (docs included) triggers a build+release.
