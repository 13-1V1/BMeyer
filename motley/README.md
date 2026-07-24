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
  - `Status` / `StatusRules` / `Abilities` — Burn/Poison/Chill/Paralyze: damage-over-time, speed
    drops, and skipped turns. Wild types take extra DoT, so status hard-counters them (D4).
  - `Leveling` / `Growth` / `CreatureProgress` — infinite but **asymptotic** leveling, growth
    stages, and Bloom (prestige rebirth). Raw level never becomes the whole game.
  - `BattleReward` / `Training` / `Catalyst` — the three XP paths (battle, idle-or-puzzle
    training, catalysts) that feed leveling.
  - `Player` / `OwnedCreature` — the player's collection: essence inventory + a roster of owned
    creatures, each with a permanent serial, nickname, and recipe (the "signal" identity layer).
  - `Session` — runs one encounter of the real game loop (field a team → fight → award XP).
  - `SaveCodec` — tiny dependency-free save/load (round-trips the whole `Player`).
  - `StarterEssences` — the authored starter catalog (game content/data).

The full game loop — **collect** essences → **grow** a creature (`CreatureFactory`) → **train**
(`Training`) → **fight** (`Session`/`BattleResolver`) → **level up** (`Leveling`) → **save**
(`SaveCodec`) → repeat — is all here, all pure Kotlin, all tested. `Demo` runs a headless
end-to-end playthrough (`./gradlew :engine:run`).

## Build & test

```bash
./gradlew :engine:test     # run the unit tests
./gradlew :engine:run      # print the console demo (grows a few creatures)
```

CI runs the tests on every PR touching `motley/**` (`.github/workflows/motley-ci.yml`).
