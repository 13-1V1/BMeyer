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

- `index.html` — home. A full first screen (pitch and primary CTA), then a showcase of four case studies: BlueCAD, G-Code Trainer, welding / NC3, and the truss bridge. Areas of study and music sit below that. BEZ App Manager is not on the home page.
- `work/` — case studies (`bluecad`, `gcode`, `welding`, `bridge`). Linked from the home showcase and from Projects.
- Area rooms: `computer-science`, `electronics-robotics`, `engineering`, `welding`, `manufacturing`, `network`, `cybersecurity`, `automotive`, `creative-electives`
- `downloads.html` — **Projects**. Order is showcase case studies, then tools and apps (LearnLinux+ leads; BEZ App Manager is in this tier), then a link to joke programs, then the résumé at `#resume`.
- `fun.html` — two harmless joke programs. Not in the nav. Projects links here last, under “Just for fun.”
- `transcripts.html`, `about.html`, `contact.html`
- `assets/` — `css/site.css`, `js/site.js`, and `images/` (photos, icons, certificates)
- `demos/` — playable class programs (word games) and machinist calculators
- `vibe/` — in-browser rebuilds of desktop apps, plus hosted BlueCAD

## Updating release labels

Search `downloads.html` for `VERSION LABELS`.

- **BEZ App Manager** — one visible label (`v1.3.3` today) plus the matching tag and APK filename in the download URL: `app-manager-v1.3.3.apk` from release `v1.3.3`. It appears only on the Projects page (`#bez-app-manager`), not on the homepage. Current source version is `versionName` in `android-app-manager/app/build.gradle.kts`; the site should match the GitHub Release you can actually download.
- **Desktop apps and the résumé PDF** — GitHub release `v1.0`. URLs look like `https://github.com/13-1V1/BMeyer/releases/download/v1.0/<filename>`. The résumé file is `Brennan-Meyer-Resume.pdf` (same link as About). BlueCAD and G-Code Trainer repeat those installer URLs on `work/bluecad.html` and `work/gcode.html`; the comment in `downloads.html` lists both spots.

There is no official transcript PDF in the repo or in that release. The transcripts page is the record.
