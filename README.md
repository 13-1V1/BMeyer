# Brennan Meyer — Portfolio

A personal portfolio site for coursework, shop work, and self-built apps across
software, electronics, engineering, welding, manufacturing, and the creative arts.

Live site: https://13-1v1.github.io/BMeyer/

The repo also holds two native projects that are **not** part of the published
site: `android-app-manager/` (BEZ App Manager) and `game-asset-gen/`. GitHub
Pages copies an allowlist (`*.html`, root media, `README.md`, `assets/`,
`demos/`, `vibe/`) and leaves every other top-level tree in the repo only.
See `.github/workflows/static.yml`.

## Site

Plain HTML, CSS, and vanilla JavaScript — no build step. Dark theme. Page
changes from the home cards use a small zoom effect, with a normal link
navigation (including open-in-new-tab and keyboard) and a
`prefers-reduced-motion` fallback.

- `index.html` — home. Featured work (BEZ App Manager, BlueCAD & G-Code, welding, truss bridge) comes before the areas-of-study grid. Music is below that.
- Area rooms: `computer-science`, `electronics-robotics`, `engineering`, `welding`, `manufacturing`, `network`, `cybersecurity`, `automotive`, `creative-electives`
- `downloads.html` — **Projects** (nav label and page title). Featured app is BEZ App Manager. Résumé PDF is at `#resume`.
- `fun.html` — two harmless joke programs. Not in the nav, and not on the Projects page.
- `transcripts.html`, `about.html`, `contact.html`
- `assets/` — `css/site.css`, `js/site.js`, and `images/` (photos, icons, certificates)
- `demos/` — playable class programs (word games) and machinist calculators
- `vibe/` — in-browser rebuilds of desktop apps, plus hosted BlueCAD

## Updating release labels

Search `downloads.html` for `VERSION LABELS`.

- **BEZ App Manager** — one visible label (`v1.3.3` today) plus the matching tag and APK filename in the download URL: `app-manager-v1.3.3.apk` from release `v1.3.3`. The home card links to `#bez-app-manager` and does not copy the number. Current source version is `versionName` in `android-app-manager/app/build.gradle.kts`; the site should match the GitHub Release you can actually download.
- **Desktop apps and the résumé PDF** — GitHub release `v1.0`. URLs look like `https://github.com/13-1V1/BMeyer/releases/download/v1.0/<filename>`. The résumé file is `Brennan-Meyer-Resume.pdf` (same link as About).

There is no official transcript PDF in the repo or in that release. The transcripts page is the record.
