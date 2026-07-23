# Sprite Forge — on-device AI game asset generator

A native Android app (Kotlin + Jetpack Compose) that turns a text prompt into
game-ready 2D assets — sprites, item icons, tiles, and backgrounds — generated
**on-device** so it works fully offline once set up.

This is a standalone Gradle project, unrelated to the sibling `android-app-manager/`.
It intentionally mirrors that project's proven toolchain (AGP 8.7, Kotlin 2.0,
Compose BOM 2024.09, `compileSdk` 35 / `minSdk` 26) so CI is identical.

## Features (v0.1)

- **Text → image** with a prompt box and one-tap generation.
- **Style presets** — Pixel Art, Low Poly, Hand-Drawn, Painterly, Flat Icon —
  each shaping the prompt and diffusion step count.
- **Asset types** — Character, Item/Icon, Tile/Texture, Background — which set
  the canvas size and framing, and decide whether transparency/tiling applies.
- **Transparent background** — an edge-seeded flood-fill cut-out for sprites and
  icons (pure, unit-tested pixel math).
- **Session gallery** and **Save PNG** to `Pictures/SpriteForge` (keeps alpha).

## How generation works — two backends

The app talks to a single `AssetGenerator` interface and auto-selects the best
backend available at generate time:

1. **On-device Stable Diffusion 1.5** via MediaPipe's Image Generator
   (`MediaPipeAssetGenerator`). Runs offline. Requires a converted SD 1.5 model
   directory, which is **far too large to ship in an APK**, so you import a model
   bundle (`.zip`) once via **Import model** in-app; it's extracted to app storage
   (`ModelManager`) and reused from then on.
2. **Procedural preview** (`StubAssetGenerator`) — a dependency-free, deterministic
   placeholder painter used whenever no model is installed. It makes the whole
   pipeline (generate → cut-out → gallery → export) usable immediately and keeps
   CI green with no device or model.

> Getting a MediaPipe-compatible SD 1.5 model is a one-time conversion step
> documented by Google AI Edge. Until you import one, the app runs on the preview
> engine.

## Architecture

Single-activity, one-directional state flow — same shape as the app-manager:

`MainActivity` → `ui/AssetGenScreen.kt` ← `ui/AssetGenViewModel.kt` (`UiState`
is the single source of truth) ← `gen/` backends ← `data/` pure logic.

The **filter/prompt/pixel logic lives in `data/` as pure Kotlin** (no Android
framework calls) so it runs under JVM unit tests with no device:

- `PromptComposer` — composes positive/negative prompts from preset + type.
- `BackgroundRemover` — ARGB flood-fill cut-out (operates on `IntArray`).
- `SpriteSheetPacker` — grid layout math for packing frames.
- `AssetNaming` — filesystem-safe, deterministic file names.

`gen/BitmapTransforms.kt` is the thin Android bridge that feeds real `Bitmap`s
through those pure algorithms.

## Build / test

Run from `game-asset-gen/`:

```bash
./gradlew testDebugUnitTest   # pure-logic JVM tests (fast, no device)
./gradlew assembleDebug       # build the debug APK
```

As with the app-manager, local builds are usually blocked (no Android SDK /
`dl.google.com` in the sandbox). **CI is the build environment** — `game-asset-ci.yml`
runs `testDebugUnitTest` then `assembleDebug` on any PR touching `game-asset-gen/**`.
