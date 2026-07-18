# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository shape

This repo holds two unrelated projects:

1. **A static portfolio website** — the root `*.html` files, `styles.css`, `assets/`,
   images, and `BMeyer-site/`. Deployed to GitHub Pages by `.github/workflows/static.yml`
   on every push to `main` (it uploads the whole repo root). No build step.
2. **`android-app-manager/`** — "BEZ App Manager", a native Android app (Kotlin +
   Jetpack Compose). This is where nearly all active development happens; the rest of
   this document is about it.

## Build / test (android-app-manager)

Run everything from `android-app-manager/` (the Gradle wrapper is committed):

```bash
./gradlew testDebugUnitTest      # JVM unit tests (fast, no device)
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # build + install on a connected device/emulator
./gradlew testDebugUnitTest --tests "com.bmeyer.appmanager.data.AdvancedFilterTest"   # one test class
```

**You usually cannot build here.** The dev sandbox blocks `dl.google.com` (the Android
SDK + all AndroidX/Compose/AGP artifacts) at the egress proxy, and has no `/dev/kvm`
(so no emulator). Treat **GitHub CI as the build/test environment**: `android-ci.yml`
runs `testDebugUnitTest` then `assembleDebug` on any PR touching `android-app-manager/**`.
Iterate by pushing and reading CI, not by building locally.

- `compileSdk`/`targetSdk` = 35, `minSdk` = 26. Kotlin 2.0, AGP 8.7, Compose BOM 2024.09.

## Architecture (android-app-manager)

Single-activity Compose app, one-directional state flow:

`MainActivity` → `ui/AppManagerScreen.kt` (the entire UI) ← `ui/AppListViewModel.kt`
← `data/AppRepository.kt`.

- **`UiState` (in AppListViewModel.kt) is the single source of truth.** The ViewModel
  exposes one `StateFlow<UiState>`. `UiState.visibleApps` is a **lazily-computed derived
  property** that applies, in order: search text → `QuickFilter` chip → `AppCategory` →
  `AdvancedFilter` (AND-combined bundle) → `SortOption`. Dashboard totals
  (`unusedCount`, `totalBytes`, etc.) are also lazy `UiState` properties. To change how
  filtering/sorting/stats work, edit `UiState` and the `data/` enums — not the UI.
- **The filter/sort/format logic is deliberately pure Kotlin** so it's unit-testable on
  the JVM with no device or Robolectric. `AppInfo` and the filter enums only touch
  Android `ApplicationInfo.CATEGORY_*` *constants* (which inline at compile time), never
  framework method calls. Keep it that way: tests live in `app/src/test/` and run under
  `testDebugUnitTest`. Don't introduce Android runtime calls into tested code paths.
- **`data/AppRepository.loadApps()`** does all the heavy enrichment off the main thread:
  `PackageManager` inventory + `UsageStatsManager` (last used / foreground time /
  open-count via `queryEvents`) + `StorageStatsManager` (size, split into app/data/cache
  on `AppInfo`). All of this requires the **"Usage access" special permission**
  (`PACKAGE_USAGE_STATS`); without it size/usage fields are `-1`/`0` and the UI degrades
  gracefully (see `data/UsageAccess.kt`).
- **View state is persisted in `data/Prefs.kt`** (SharedPreferences): sort, quick filter,
  category, the system-apps toggle, and the whole `AdvancedFilter` bundle survive
  restarts. `AdvancedFilter` serializes via its own `encode`/`decode` (round-trip
  unit-tested). When you add a new persisted filter dimension, wire it through `Prefs`
  *and* the ViewModel's initial `UiState` + the relevant setter, or it won't stick.

### Bulk uninstall — three backends, auto-selected

Android forbids a normal app from silently uninstalling others. `AppManagerScreen`'s
confirm dialog picks the best available backend at Uninstall time:

1. **Shizuku silent** (`shizuku/`) — `ShizukuManager` binds a `UserService` (AIDL in
   `app/src/main/aidl/`) that runs `pm uninstall`. Only when Shizuku is running + granted.
2. **Accessibility auto-confirm** (`uninstall/UninstallAutoConfirmService.kt`) — auto-taps
   the system uninstall dialog. It is gated by a static `AtomicBoolean active` that
   `AppManagerScreen` sets true only for the duration of a user-started batch, so the
   service never acts on its own.
3. **Manual queue** — fires `ACTION_UNINSTALL_PACKAGE` per package, driven by a
   `LaunchedEffect(queue, queueIndex)` loop that advances on each activity result.

## Releasing (android-app-manager)

`android-release.yml` builds a **debug-signed** APK and publishes it as a GitHub Release
asset (installable by sideloading). Triggered by a `v*` tag **or** `workflow_dispatch`
with a `version` input.

**Pushing tags is blocked in this environment**, so cut a release by dispatching the
workflow (`run_workflow`, ref = the branch or `main`, `version: vX.Y`) — the release
action creates the tag itself. Before releasing, bump both `versionCode` and
`versionName` in `app/build.gradle.kts`.

## Gotchas that have actually bitten

- **NewApi crashes aren't caught by CI.** `minSdk` is 26 but `compileSdk` is 35, so calls
  to APIs newer than 26 compile fine and then crash at runtime on old devices — and CI
  runs `assembleDebug`/`testDebugUnitTest` but **not `lint`**, so nothing flags it. A real
  launch crash on Android 9 came from `AppOpsManager.unsafeCheckOpNoThrow` (API 29+).
  Guard any API-gated call with `Build.VERSION.SDK_INT` (see `UsageAccess.isGranted`).
- The accessibility auto-tap matches the confirm button by `android:id/button1` and a few
  label strings (`uninstall/UninstallAutoConfirmService.kt`); OEM dialogs with different
  wording may need that list extended.
- **The launcher icon is a plain square bitmap, not an adaptive icon.** It lives at
  `res/mipmap-*/ic_launcher.png` (+ `ic_launcher_round.png`) across the five densities;
  the `mipmap-anydpi-v26` adaptive XML and its vector drawables were deliberately removed
  because the art is a full-bleed badge with its own border/rounded corners (Android's
  adaptive mask would crop it). To swap the icon, regenerate those five PNGs (48/72/96/
  144/192 px) from the source art — `pip install Pillow` works in the sandbox — rather
  than adding an adaptive icon back.
