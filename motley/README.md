# 🌱 Motley — engine

**Motley** is a creature-collector where you **combine collectible essences** (concept-words
like `Mad`, `Robo`, `Electric`) to **grow** one-of-a-kind creatures, then battle them 3v3.
Design docs live in [`../docs/`](../docs) (`motley-gdd.md`, `motley-decisions.md`).

This project holds the **game engine** — the pure, platform-agnostic game logic. It has **no
Android dependencies**, so the whole thing runs and unit-tests on the plain JVM (the design's
"balance lives in testable pure Kotlin" principle). An Android/Compose app will later depend on
this module.

## Modules

- **`:engine`** — the essence combination engine and battle system (`com.motley.engine`):
  - `CreatureFactory` — grows a `Creature` from essences under a **fixed stat budget** (combine
    more essences → *wilder, not stronger*).
  - `TypeChart` — the `EMBER > THORN > TIDE` triangle + neutral `WILD` hidden types.
  - `Momentum` — the tempo mechanic (exploiting a weakness earns extra actions).
  - `Synergy` / `SynergyEffect` — named combos certain essence pairings unlock, with real battle
    effects (e.g. Skyborne acts first regardless of Speed; Overload builds Momentum faster).
  - `Opponents` — generates fair enemy teams (same catalog, same rules, no stat cheating) to battle.
  - `BattleResolver` / `Damage` — 3v3 turn-based battles with a bench, type-scaled damage, and
    Momentum bursts. RNG is injectable, so a fixed seed makes a battle fully reproducible.
  - `Leveling` / `Growth` / `CreatureProgress` — infinite but **asymptotic** leveling, growth
    stages, and Bloom (prestige rebirth). Raw level never becomes the whole game.
  - `BattleReward` / `Training` / `Catalyst` — the three XP paths (battle, idle-or-puzzle
    training, catalysts) that feed leveling.
  - `StarterEssences` — the authored starter catalog (game content/data).

The full core loop — **grow** (`CreatureFactory`) → **fight** (`BattleResolver`) → **invest &
grow stronger** (`Leveling`/`Training`) — is all here, all pure, all tested.

## Build & test

```bash
./gradlew :engine:test     # run the unit tests
./gradlew :engine:run      # print the console demo (grows a few creatures)
```

CI runs the tests on every PR touching `motley/**` (`.github/workflows/motley-ci.yml`).
