# 🌱 Motley — engine

**Motley** is a creature-collector where you **combine collectible essences** (concept-words
like `Mad`, `Robo`, `Electric`) to **grow** one-of-a-kind creatures, then battle them 3v3.
Design docs live in [`../docs/`](../docs) (`motley-gdd.md`, `motley-decisions.md`).

This project holds the **game engine** — the pure, platform-agnostic game logic. It has **no
Android dependencies**, so the whole thing runs and unit-tests on the plain JVM (the design's
"balance lives in testable pure Kotlin" principle). An Android/Compose app will later depend on
this module.

## Modules

- **`:engine`** — the essence combination engine and battle math (`com.motley.engine`):
  - `CreatureFactory` — grows a `Creature` from essences under a **fixed stat budget** (combine
    more essences → *wilder, not stronger*).
  - `TypeChart` — the `EMBER > THORN > TIDE` triangle + neutral `WILD` hidden types.
  - `Momentum` — the tempo mechanic (exploiting a weakness earns extra actions).
  - `Synergy` — named combos certain essence pairings unlock.
  - `StarterEssences` — the authored starter catalog (game content/data).

## Build & test

```bash
./gradlew :engine:test     # run the unit tests
./gradlew :engine:run      # print the console demo (grows a few creatures)
```

CI runs the tests on every PR touching `motley/**` (`.github/workflows/motley-ci.yml`).
