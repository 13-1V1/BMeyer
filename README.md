# Brennan Meyer — Portfolio

A personal portfolio site for coursework, shop work, and self-built apps across
software, electronics, engineering, welding, manufacturing, and the creative arts.

Live site: https://13-1v1.github.io/BMeyer/

The repo also holds two native projects that are **not** part of the published
site: `android-app-manager/` (BEZ App Manager) and `game-asset-gen/`. GitHub
Pages copies an allowlist (`*.html`, root media, `README.md`, `assets/`,
`demos/`, `vibe/`, `work/`) and leaves every other top-level tree in the repo only.
See `.github/workflows/static.yml`.

## Site

Plain HTML, CSS, and vanilla JavaScript — no build step. Dark theme. Page
changes from the home cards use a small zoom effect, with a normal link
navigation (including open-in-new-tab and keyboard) and a
`prefers-reduced-motion` fallback.

- `index.html` — home. Plain pitch and a full portrait, then G-Code Trainer 0.1.16 as the lead. BlueCAD, TileSmith, and welding sit under that. Areas of study and music are below. BEZ App Manager stays on Projects.
- `work/` — case studies (`gcode`, `bluecad`, `welding`, `bridge`). G-Code, BlueCAD, and welding are linked from home. The bridge case study stays, linked from the engineering room and from Projects.
- Area rooms: `computer-science`, `electronics-robotics`, `engineering`, `welding`, `manufacturing`, `network`, `cybersecurity`, `automotive`, `creative-electives`
- `downloads.html` — **Projects**. Order is G-Code Trainer 0.1.16, then BlueCAD and TileSmith, then welding and the truss bridge, then tools and apps (LearnLinux+ leads that tier; BEZ App Manager is in it), then a link to joke programs, then the résumé at `#resume`.
- `fun.html` — two harmless joke programs. Not in the nav. Projects links here last, under “Just for fun.”
- `transcripts.html`, `about.html`, `contact.html`
- `assets/` — `css/site.css`, `js/site.js`, and `images/` (photos, icons, certificates)
- `demos/` — playable class programs (word games) and machinist calculators
- `vibe/` — in-browser rebuilds of desktop apps, plus hosted BlueCAD

## Updating release labels

Search `downloads.html` for `VERSION LABELS`.

- **G-Code Trainer** — visible label `v0.1.16` on the homepage, Projects (`#gcode`), and `work/gcode.html`. Installer: `G-Code.Trainer.Setup.0.1.16.exe` from release `gcode-trainer-v0.1.16`. Do not link the older `v1.0` file `G-Code-Trainer-Setup.exe`.
- **TileSmith** — visible label `v0.2` on Projects (`#tilesmith`) and the homepage card. Portable zip: `TileSmith-Desktop-portable.zip` from release `tilesmith-v0.2`. Unzip and keep the folder together. Unsigned Electron build, no installer. Open `TileSmith.exe`.
- **BEZ App Manager** — one visible label (`v1.3.3` today) plus the matching tag and APK filename in the download URL: `app-manager-v1.3.3.apk` from release `v1.3.3`. It appears only on the Projects page (`#bez-app-manager`), not on the homepage. Current source version is `versionName` in `android-app-manager/app/build.gradle.kts`; the site should match the GitHub Release you can actually download.
- **Other desktop apps and the résumé PDF** — GitHub release `v1.0`. URLs look like `https://github.com/13-1V1/BMeyer/releases/download/v1.0/<filename>`. The résumé file is `Brennan-Meyer-Resume.pdf` (same link as About). BlueCAD repeats `BlueCAD-Setup-1.0.0.exe` on `work/bluecad.html`.

There is no official transcript PDF in the repo or in that release. The transcripts page is the record.
