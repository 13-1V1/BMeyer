# App Manager

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
- **Search** by app name or package name.
- **View totals** — the header shows the app count and combined size of whatever
  is currently filtered in.
- **Bulk selection** with a select-all-shown action and a running count.
- **Reclaimable size** — a bottom bar totals how much storage your current
  selection would free.
- **Bulk uninstall** via a sequential queue — one system confirmation per app.
  Uninstalled apps drop out of the list immediately.
- **Per-app "App info"** shortcut (ℹ️ on each row) that opens the system settings
  page so you can force-stop, clear cache, or manage permissions.
- **Remembers your preferences** — sort order, active filter, and the
  system-apps toggle persist across launches.

## How bulk uninstall works (and why it's a queue)

Android does **not** let an ordinary Play-Store-installable app silently remove
other apps — every uninstall must be confirmed by the user. So "bulk" here means
you do the *selection and filtering* in bulk, then the app fires the system
uninstall prompt for each selected app back-to-back. You tap **OK** (or cancel)
on each one; the app tracks the results and refreshes the list.

Truly silent one-tap bulk removal would require Shizuku (ADB privileges), root,
or a Device Owner (MDM) setup. This app deliberately stays on the standard,
no-setup path so it runs on any phone.

## Permissions

| Permission | Why |
|---|---|
| `PACKAGE_USAGE_STATS` (Usage access) | last-used time, total usage time, and per-app storage size. This is a special access the user grants in **Settings → Usage access** — the app shows a banner and a shortcut button. Without it the app still lists apps, but size/usage columns show `—`. |
| `QUERY_ALL_PACKAGES` | enumerate every installed package on Android 11+. |
| `REQUEST_DELETE_PACKAGES` | launch the per-app uninstall confirmation. |

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
│   ├── AppInfo.kt               # per-app model (size, last used, usage, install date)
│   ├── AppRepository.kt         # PackageManager + UsageStatsManager + StorageStatsManager
│   ├── SortOption.kt            # sort dimensions
│   ├── QuickFilter.kt           # "clean house" filter presets + predicates
│   ├── Prefs.kt                 # persists sort / filter / system-apps toggle
│   └── UsageAccess.kt           # checks the Usage-access grant
└── ui/
    ├── AppListViewModel.kt      # UI state: load, filter, sort, selection
    ├── AppManagerScreen.kt      # the whole screen + uninstall queue
    ├── Format.kt                # byte / duration / relative-time formatting
    └── Theme.kt                 # Material 3 theme (dynamic color on Android 12+)
```

## Notes / ideas for later

- Add a "total reclaimable size" readout for the current selection.
- Cache icons/sizes so large device inventories load faster.
- Optional Shizuku backend for genuinely silent bulk uninstall.
