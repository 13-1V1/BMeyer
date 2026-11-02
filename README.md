# Brennan Meyer — Portfolio

A personal portfolio site for coursework, shop work, and self-built apps across
software, electronics, engineering, welding, manufacturing, and the creative arts.

Live site: https://13-1v1.github.io/BMeyer/

The repo also holds two native projects that are **not** part of the published
site: `android-app-manager/` (BEZ App Manager) and `game-asset-gen/`. GitHub
Pages copies an allowlist (`*.html`, root media, `README.md`, `assets/`,
`work/`) and leaves every other top-level tree in the repo only.
See `.github/workflows/static.yml`.

## Site

Plain HTML, CSS, and vanilla JavaScript — no build step. Dark theme. Page
changes from the home cards use a small zoom effect, with a normal link
navigation (including open-in-new-tab and keyboard) and a
`prefers-reduced-motion` fallback.

- `index.html` — home. Short intro and portrait, then areas of study, a single link out to BEZ Works for software, and music.
- `work/` — case studies (`welding`, `bridge`). Welding is linked from the welding room and bridge from the engineering room. Software lives on BEZ Works; this site hosts no programs or tools of its own.
- Area rooms: `computer-science`, `electronics-robotics`, `engineering`, `welding`, `manufacturing`, `network` (network & cybersecurity), `automotive`, `creative-electives`
- `downloads.html` — redirect to BEZ Works (https://13-1v1.github.io/BEZ-Works/), kept so old links still work. Every **Projects** link on the site goes to BEZ Works.
- `music.html` — albums, music video, and channel link
- `transcripts.html`, `about.html`, `contact.html`, `404.html`
- `assets/` — `css/site.css`, `js/site.js`, and `images/` (photos, icons, certificates)

## Downloads

Software downloads and version labels live on BEZ Works, not here. The résumé PDF
and older binaries are attached to GitHub release `v1.0`, with URLs like
`https://github.com/13-1V1/BMeyer/releases/download/v1.0/<filename>`. The
résumé file is `Brennan-Meyer-Resume.pdf`, linked from About.

There is no official transcript PDF in the repo or in that release. The transcripts page is the record.
