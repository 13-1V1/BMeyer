# 🌱 SEEDLINGS — Game Design Doc (v0.1)

*A creature-collector where you **plant, grow, and battle** monsters no one has ever seen.*

> Status: brainstorm / early design. Working title. Nothing here is locked except the pillars.

---

## 1. Pitch

Pokémon, but the creatures are **grown, not caught** — and every one is unique, its art
and soul authored on the fly. You're a **Cultivator**: you gather essences from the world,
sow them into a seed, raise the creature that sprouts, and battle to prove your garden is
stronger than theirs.

The hook is **authorship**. In Pokémon you love your Charizard because you *earned* it.
Here you love your creature because *you made it exist* — nobody else in the world has it.

## 2. Design pillars

1. **You steer, the world surprises.** The player controls the *themes* (which essences,
   how it's raised); the game controls the *execution* (what actually grows). The gap
   between "what I planted" and "what I got" is the game.
2. **Authorship through investment.** Attachment comes from effort — sowing the seed *and*
   raising it — not from a lucky pull.
3. **AI is diegetic.** Creatures are grown from living essence, so of course each is
   unique and strange. The fiction *explains* the procedural weirdness instead of
   apologizing for it.
4. **Discovery never runs out.** Creatures are infinite; the collectible is the finite set
   of **essences**, and the hidden types keep mystery alive deep into the game.

## 3. Core loop

```
Explore → Gather essences → Sow a seed → Raise it (battle / train / catalysts)
   → Watch it grow through stages → Battle with your team → earn rarer essences → sow bolder seeds
```

## 4. Creation: the seed ritual

1. **Sow.** Combine 2–3 **essences** (`Ember`, `Tide`, `Thorn`, `Ancient`, `Storm`,
   `Bone`, …) into a seed. This is the player's steering — you pick the themes, not the
   outcome.
2. **Grow.** The seed matures through **investment**, not passive time (see §5).
3. **Reveal.** At each growth milestone the art re-composites with more detail — a real
   reveal moment each stage, ending in a unique AI-authored **art + name + flavor** that is
   yours alone.

## 5. Growth system

Growth is driven by **what the player does**, so raising a creature *is* authorship.

**Stages:** `Seed → Sprout → Juvenile → Mature → Awakened (rare)`
At each stage the composited art gains detail (sprout = cute blob; Mature = the striking
creature). You literally watch it grow.

**Three growth inputs → three playstyles:**

| Input | Feel | Risk | Effect |
|---|---|---|---|
| **Battling** | "raise it in the field" | risky — a sprout can lose | organic growth, biases toward aggressive Mature forms |
| **Training** | "the gym" — safe, player-chosen mode | none | slower, biases toward tanky/utility forms |
| **Powerups / catalysts** | consumables | varies | accelerate *or bend* growth; the scarcity + surprise lever |

**Care shapes the outcome.** Same seed, different upbringing → different Mature form
(Tyrogue → Hitmon-style branching). Grow mostly by battle → lean/aggressive; mostly by
training → tanky; feed a rare catalyst at Juvenile → branch into a strange **Awakened**
variant. The upbringing is part of the design, not just the seed.

**Training — two modes, player's choice:**

- **Idle timer.** Set a creature to train and walk away; it accrues growth over real time
  (offline-friendly, respects the player's time, the "check back later" hook). Capped per
  session so it supplements battle rather than replacing it.
- **Puzzle.** An active minigame — solve to earn a burst of growth. Rewards engagement for
  players who want to lean in, and gives a skill outlet on days they don't feel like
  battling.

Same reward currency (growth), two paces. Idle for the busy, puzzle for the engaged.

## 6. Stats without AI balance chaos

**The essences carry the numbers; the AI only carries the soul.** Each essence
deterministically contributes to type, stat curve, and a signature move:

- `Ember` → +ATK, **Ember** type, a burn move
- `Ancient` → +HP, defensive trait
- `Tide` → sets a base stat shape / evasion
- (etc.)

So the **build is fully player-authored and always balanced**, while the **appearance is a
genuine surprise**. The magic sentence: *"It plays exactly how I designed it, but it looks
like nothing I expected."* Balance stays under the designer's control — never the model's.

## 7. Battle

Turn-based tactics — Pokémon's DNA, kept **small and tight**:

- **3v3**, no items to start, clean type chart.
- HP / Speed / Attack / Defense; a handful of moves per creature.
- Depth comes from **team composition across your one-of-a-kind roster**, not a huge move
  database.

### Type chart — the triangle + hidden types

**The visible game is a clean 3-type triangle** players learn instantly:

```
Ember → Thorn → Tide → Ember     (each beats one, loses to one)
```

**Hidden types** (`Wild` / `Voidgrown`) sit *outside* the triangle:

- **A family, not a single type.** Grown only from rare, uncategorized essence. Each
  discovered variant has its **own** abilities, quirks, and edge-case strengths — so
  finding one is never "I already know what this does." Discovery stays alive all game.
- **Neutral to the triangle.** No advantage *or* disadvantage vs. Ember/Tide/Thorn — the
  natural order doesn't apply to them. They win or lose on raw stats and abilities, not
  matchup.
- **Glass cannon.** Generally **high attack, low defense**: hits like a truck, folds under
  pressure. Fielding one is a *decision* (it can carry a fight or evaporate turn one), not
  a free upgrade.
- **Rarity ≈ 5% (1 in 20).** Sowing a seed has roughly a 1-in-20 chance to turn up a Wild
  variant instead of a normal creature (tunable, and nudgeable by rare hidden essences).
  Common enough that a dedicated player *will* find one; rare enough that each is an event.
- **Discovery is the reward.** You don't know they exist until you stumble into one; you
  learn each variant's tricks by experimenting. The unknown *is* the fun.

The whole chart is a small **pure function** — trivially unit-testable on the JVM (fits the
repo's "pure Kotlin, no Android calls in tested code" rule). This is the recommended first
thing to actually build.

## 7a. Leveling & permanence

- **Creatures never die and never retire.** Your garden is permanent — every creature you
  grow stays yours forever. This is the emotional backbone: you build a lifelong menagerie.
- **Levels are uncapped.** Creatures level **infinitely**; **level 500 is practically
  unreachable** — tens of thousands of hours of investment — so a maxed creature is a
  monument to dedication, not a normal goal.
- **Level is a slow, asymptotic axis — not the whole game.** So that infinite leveling +
  no death doesn't collapse into "whoever grinded longest always wins":
  - Per-level stat gains **diminish hard** at high levels (a level-500 is meaningfully but
    not absurdly stronger than a level-200).
  - **Type matchup and team comp can beat raw level.** A clever level-40 team should be
    able to upset a brute-forced level-90 — skill and authorship stay relevant.
  - The prestige of a high level is **social/personal** ("look what I raised"), not a
    pay-to-skip power wall.

## 8. Progression = essences, not creatures

Creatures are infinite; **essences are the finite collectible.** Rarer essences (earned
from tougher battles / deeper biomes) unlock bolder, stranger seeds — and the rare *hidden*
essence unlocks the Wild types. "Gotta collect every essence" replaces "gotta catch 'em
all," and every new essence multiplies what you can grow.

## 9. Generation approach (how the art actually happens)

For a mobile prototype, avoid live per-encounter image-gen (slow, costs money, needs
network). Instead:

- **Layered composite art.** The AI generates a big library of *parts* offline, keyed to
  essences (`Ember` heads, `Tide` bodies, `Ancient` textures). Creation **composites** them
  at runtime → instant, offline, free, still feels custom because the *combination* is
  yours. Growth stages = more/richer parts layered in.
- **Live image-gen becomes a rare "premium bloom" ritual** later, not the default path.

## 10. Build path (fits the Android/Compose repo)

- **v0 (weekend):** essences → seed → reveal from the layered composite system; the type
  chart as a tested pure function. Battle stubbed.
- **v1:** real turn-based 3v3 battle; growth-over-battles/training; branching Mature forms.
- **v2:** hidden types + discovery; catalysts; Awakened forms.
- **later:** biome exploration; live "premium bloom" generation; multiplayer battles.

## 11. Resolved & open

**Resolved**
- Training = **idle timer + puzzle**, player's choice, same growth currency (§5).
- Hidden types ≈ **5% / 1-in-20** sow chance (§7).
- Creatures **never die**, level **infinitely**, level 500 ≈ tens of thousands of hours;
  level is asymptotic so matchup/comp still matter (§7a).

**Still open**
- **Puzzle design:** what *is* the puzzle — match-3, logic/Sokoban, wordy, something themed
  to essences?
- **Move system:** fixed signature move per essence, or a small learnable movepool?
- **Multiplayer:** async battle vs. friends' gardens? Trading essences (not creatures)?
- **Economy of catalysts:** earned only, or the monetization surface?
- **Name / IP:** "Seedlings" is a placeholder.

---

*Pillars locked: player-as-creator · plant-and-grow (surprise over control) · battle · grow
through investment · clean triangle + rare hidden glass-cannon types.*
