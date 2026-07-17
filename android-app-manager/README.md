# BEZ App Manager

An Android utility for cleaning house: browse every installed app, **sort and
filter by size, last-used time, total usage time, and install date**, then
**bulk-select and uninstall** the ones you don't need.

Built with **Kotlin + Jetpack Compose** (Material 3).

## Features

- **Full inventory** of installed apps (user apps by default; toggle to include
  system apps).
- **Sort** by:
  - Least / most recently used
  - Least / most usage time (total foreground time)
  - Largest / smallest size (app + data + cache)
  - Name, newest install, oldest install
- **Quick-filter chips** for one-tap "clean house" views:
  - Never used · Unused 30d+ · Unused 90d+ · ≥ 500 MB
  - (usage/size chips are disabled until Usage access is granted)
- **Category filter** — narrow to Games, Audio, Video, Photos, Social, News,
  Maps, Productivity, or Uncategorized (from each app's declared category).
- **Advanced compound search** — combine multiple criteria (all AND-matched) to
  refine precisely, e.g. *used within the last year AND for more than 5 minutes*:
  - Last used — within / older than / never, with a day threshold
  - Installed — within / older than, with a day threshold
  - Total usage time (minutes) — min/max
  - Times opened (last 30 days) — min/max
  - Storage size (MB) — min/max
- **Richer per-app info** — each row shows size, last used, total usage time,
  open count, and last-updated date.
- **Search** by app name or package name.
- **Reclaimable-space dashboard** — a header card showing total apps, combined
  app storage, and how many apps are unused (90d+) with an estimate of how much
  space removing them would free, plus a one-tap **Review** to jump to that list.
- **View totals** — the controls row shows the app count and combined size of
  whatever is currently filtered in.
- **Bulk selection** with an always-visible **Select all / Unselect all** toggle
  and a running count.
- **Reclaimable size** — a bottom bar totals how much storage your current
  selection would free.
- **Bulk uninstall**, three backends (best available is auto-selected):
  - *Hands-free (Accessibility)* — enable a one-time auto-confirm service and the
    app taps every system confirmation for you: tap Uninstall once, walk away.
  - *Silent (Shizuku)* — when [Shizuku](https://shizuku.rikka.app/) is running and
    permission-granted, removes the whole selection with no dialogs at all.
  - *Manual* — the always-works fallback: a queue firing one system confirmation
    per app.
- **Per-app "App info"** shortcut (ℹ️ on each row) that opens the system settings
  page so you can force-stop, clear cache, or manage permissions.
- **Remembers your preferences** — sort order, active filter, and the
  system-apps toggle persist across launches.

## How bulk uninstall works

Android does **not** let an ordinary app silently remove other apps — every
uninstall must be confirmed. The app offers three ways to handle that, and picks
the best one available when you tap Uninstall:

1. **Hands-free (Accessibility auto-confirm).** Enable BEZ App Manager's
   accessibility service once (Settings → Accessibility). During a bulk uninstall
   *you started*, the app watches for the system confirmation dialog and taps it
   for you — so you tap **Uninstall** once and the batch clears itself. The
   service only acts while a batch you started is running, and never touches
   anything else. Keep the screen on and don't interact while it runs.
2. **Silent (Shizuku).** If [Shizuku](https://shizuku.rikka.app/) is running and
   permission-granted, the batch runs with no dialogs at all.
3. **Manual.** With neither enabled, the app fires the system prompt for each app
   back-to-back and you confirm each — works on any phone, no setup.

## Silent uninstall with Shizuku (optional)

[Shizuku](https://shizuku.rikka.app/) lets apps use ADB-level privileges without
root. When it's installed and started (via wireless debugging or a PC once per
boot), App Manager can uninstall your whole selection silently:

1. Install the Shizuku app and start it (its own instructions cover this).
2. In App Manager, select apps and tap **Uninstall** → **Enable silent mode**
   the first time, and approve Shizuku's permission prompt.
3. The selection is removed in one go via a privileged user-service that runs
   `pm uninstall` — a progress dialog shows N/total.

If Shizuku isn't running or permission isn't granted, the button simply performs
the standard per-app confirmation queue instead. No functionality is lost.

## Install on your phone (prebuilt APK)

Every version tag publishes an installable APK to **GitHub Releases**
(built by `.github/workflows/android-release.yml`). On your phone, open the
latest release, download `app-manager-<version>.apk`, and tap it to install
(allow "Install unknown apps" for your browser if prompted). The APK is
debug-signed for easy sideloading.

## Permissions

| Permission | Why |
|---|---|
| `PACKAGE_USAGE_STATS` (Usage access) | last-used time, total usage time, and per-app storage size. This is a special access the user grants in **Settings → Usage access** — the app shows a banner and a shortcut button. Without it the app still lists apps, but size/usage columns show `—`. |
| `QUERY_ALL_PACKAGES` | enumerate every installed package on Android 11+. |
| `REQUEST_DELETE_PACKAGES` | launch the per-app uninstall confirmation. |
| Shizuku permission (runtime) | optional; only requested when you opt into silent uninstall. |

## Build & run

Requires the Android SDK (via Android Studio or command-line tools).

```bash
# From this directory:
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # build + install on a connected device/emulator
```

Or just open the `android-app-manager/` folder in **Android Studio** and press Run.

- Compile SDK: 35 · Min SDK: 26 (Android 8.0) · Target SDK: 35
- The Gradle wrapper is committed, so no separate Gradle install is needed.

## Project layout

```
app/src/main/java/com/bmeyer/appmanager/
├── MainActivity.kt              # Compose entry point + usage-access launcher
├── data/
│   ├── AppInfo.kt               # per-app model (size, last used, usage, install, category)
│   ├── AppRepository.kt         # PackageManager + UsageStatsManager + StorageStatsManager
│   ├── SortOption.kt            # sort dimensions
│   ├── QuickFilter.kt           # "clean house" filter presets + predicates
│   ├── AppCategory.kt           # content-category filter (Games, Audio, …)
│   ├── Prefs.kt                 # persists sort / filter / system-apps toggle
│   └── UsageAccess.kt           # checks the Usage-access grant
├── shizuku/
│   ├── IUserService.aidl        # privileged user-service interface
│   ├── UserService.kt           # runs `pm uninstall` in the Shizuku process
│   └── ShizukuManager.kt        # availability, permission, silent batch uninstall
└── ui/
    ├── AppListViewModel.kt      # UI state: load, filter, sort, selection, dashboard stats
    ├── AppManagerScreen.kt      # the whole screen + dashboard + uninstall backends
    ├── Format.kt                # byte / duration / relative-time formatting
    └── Theme.kt                 # Material 3 theme (dynamic color on Android 12+)
```

## Notes / ideas for later

- Cache icons/sizes so large device inventories load faster.
- Per-category storage breakdown in the dashboard.
- Batch "clear cache" (needs the same privileged backend).
