# 🌱 SEEDLINGS — Game Design Doc (v0.1)

*A creature-collector where you **plant, grow, and battle** monsters no one has ever seen.*

> Status: brainstorm / early design. Working title. Nothing here is locked except the pillars.

---

## 1. Pitch

Pokémon, but the creatures are **grown, not caught** — and every one is unique, its art
and soul authored on the fly. You're a **Cultivator**: you describe a creature in a few
words (`angry robot tv`), sow it as a seed, raise what sprouts, and battle to prove your
garden is stronger than theirs.

The hook is **authorship**. In Pokémon you love your Charizard because you *earned* it.
Here you love your creature because *you made it exist* — nobody else in the world has it.

## 2. Design pillars

1. **Anything can be grown.** Creatures are open-ended combinations of essences — `Feathered
   + Dog`, `Mad + Robo + Electric`, `Man + Bear + Pig` — with **no cap**. The AI-grown
   premise only pays off if the space is truly open.
2. **You steer, the world surprises.** The player controls the *essences* (which ones, how
   it's raised); the game controls the *execution* (what actually grows). The gap between
   "what I combined" and "what I got" is the game.
3. **Authorship through investment.** Attachment comes from effort — sowing the seed *and*
   raising it — not from a lucky pull.
4. **AI is diegetic.** Creatures are grown from raw living essence, so of course each is
   unique and strange. The fiction *explains* the procedural weirdness instead of
   apologizing for it.
5. **Discovery never runs out.** Creatures are infinite; the collectible is your finite,
   growing **library of essences**, and the hidden essences keep mystery alive deep into
   the game.

## 3. Core loop

```
Explore → Collect essences → Sow a seed (combine 2+ essences) → Raise it (battle / train / catalysts)
   → Watch it grow through stages → Battle with your team → earn rarer essences → sow bolder creatures
```

## 4. Creation: the seed ritual

1. **Sow.** Combine **2 or more essences** from your collection into a seed. Each essence is
   a collectible **concept-word** (`Mad`, `Robo`, `Electric`, `Feathered`, `Cosmic`…).
   `Mad + Robo + Electric` → an angry robot TV-thing. Creatures can be *anything*, and
   there's **no maximum** — stack as many essences as you dare for gloriously weird results.
   You pick the essences; the world decides how they fuse.
2. **Grow.** The seed matures through **investment**, not passive time (see §5).
3. **Reveal.** At each growth milestone the art gains detail — a real reveal moment each
   stage, ending in a unique AI-authored **art + name + flavor** that is yours alone.

### 4a. Essences — the input, the collectible, and the balance layer

An **essence is a collectible concept-word** (an adjective, adverb, or noun-ish theme). It
is the single unit that ties the whole game together, because each authored essence carries
*three* things:

```
Essence "Robo":
  stat-tags:    +DEF, -SPD, Metal-leaning        # → balance
  ability-seed: "Overclock" (a grantable move)   # → kit
  art-prompt:   "robotic, chrome plating, glowing eyes"   # → generation
  rarity:       uncommon                          # → progression
```

Sowing a seed **sums the essences' stat-tags** into a balanced kit and **concatenates their
art-prompts** into one generation prompt. Battle-fairness and appearance both come straight
from authored data — no parsing arbitrary text, no undefined words.

**Design rules:**

- **No maximum essences per creature.** Combining more makes a creature *wilder*, not
  *stronger* — see §6 (fixed stat budget). The only soft limit is art coherence at very
  high counts, which is emergent, not enforced.
- **Essences are the finite collectible.** You start with a handful (animals + basic
  traits → `Feathered Dog`) and **unlock more** by exploring and battling (`Robo`, `Cosmic`,
  `Undead`, `Electric`, `Ancient`…). *"Gotta collect every essence"* replaces *"gotta catch
  'em all"* — creativity and power both grow with progression.
- **Curated pool = built-in moderation.** Because players combine from a designer-vetted
  essence set (not raw typed text), the abuse surface of free-form image generation
  largely disappears. The vocabulary *is* the safety layer.
- **Hidden types are hidden essences.** The rare Wild variants (§7) come from rare essences
  you discover — the mystery lives in the collection.

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

**The essences carry the numbers; the AI only carries the soul.** Each essence has authored
**stat-tags** that contribute to type, stat curve, and a signature move. The essence does
double duty: art prompt *and* balanced kit.

> **`Mad + Robo + Electric`**
> - `Mad` → aggressive temperament, +ATK
> - `Robo` → Metal-leaning, +DEF, −SPD
> - `Electric` → Electric type, a shock/paralyze move
>
> → **Art & soul:** whatever the generator dreams up. **Kit:** balanced, derived, predictable.

### The fixed-budget rule (why "no max" doesn't break balance)

Every creature is built from the **same total stat budget**, distributed across its
essences — so combining *more* essences makes a creature **wilder, not stronger**:

- **3 essences** → focused: the budget splits 3 ways, punchy and specialized.
- **6 essences** → chaotic generalist: same budget spread thin, huge type coverage and many
  abilities but master of none.

Stacking essences is a genuine strategic tradeoff (focused vs. versatile), never a power
cheat. So the **build is player-authored and always balanced**, while the **appearance is a
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

Creatures are infinite; **your essence library is the finite collectible.** Rarer essences
(earned from tougher battles / deeper biomes) unlock bolder, stranger creatures — and rare
*hidden essences* unlock the Wild types. "Gotta collect every essence" replaces "gotta catch
'em all," and every new essence multiplies what you can grow. Because combination has no
cap, even a modest library yields a combinatorial explosion of possible creatures.

## 9. Generation approach (how the art actually happens)

Because creatures are **open-ended essence combinations** (`Mad + Robo + Electric`), a
pre-built composite-parts library can't cover the space. So generation leans on **live
generative image-gen**, driven by the concatenated **art-prompt fragments** of the chosen
essences (§4a). Three facts make that workable:

- **Pay once, own forever.** Generate the image at seed-time, then **cache it as that
  creature's permanent asset**. Creatures never die (§7a), so the one-time cost amortizes
  over the creature's entire life. Growth stages = a few re-gens (sprout → mature), not one
  per battle.
- **Moderation is mostly structural.** Players combine from a **designer-vetted essence
  pool**, not raw typed text, so the abuse surface is small by construction. A light check
  on unusual essence *combinations* is the only extra guard needed.
- **Prompts are semi-structured, so art is more consistent.** Each essence contributes a
  reliable descriptive fragment, giving the model coherent, art-directed input instead of
  whatever a player free-typed.

**Fallbacks / hybrids to de-risk cost & latency:**
- **Composite art for a stylized MVP.** A parts library keyed to *stat-tags* (not exact
  essences) can still render a recognizable creature offline for v0, before wiring live gen.
- **Deferred reveal.** Sowing kicks off async generation; the creature "grows" while the
  image renders in the background — the wait is diegetic, not a spinner.

## 10. Build path (fits the Android/Compose repo)

- **v0 (weekend):** essences → combine → seed → reveal (composite art for the MVP); the
  type chart + the essence-combination/stat-budget math as tested pure functions. Battle
  stubbed.
- **v1:** real turn-based 3v3 battle; growth-over-battles/training; branching Mature forms.
- **v2:** live generative image-gen (cache-once); essence-collection progression.
- **v3:** hidden essences/types + discovery; catalysts; Awakened forms.
- **later:** biome exploration; multiplayer battles; async garden-vs-garden.

## 11. Resolved & open

**Resolved**
- Creation = **combine 2+ collectible essences, no maximum**. Each essence is a concept-word
  carrying stat-tags + an art-prompt fragment; the essence **library** is the collectible
  (§4, §4a, §6, §8).
- **Fixed stat budget** → more essences = wilder, not stronger; keeps "no max" balanced (§6).
- Curated essence pool = **structural moderation** (no raw text input) (§9).
- Training = **idle timer + puzzle**, player's choice, same growth currency (§5).
- Hidden types ≈ **5% / 1-in-20** sow chance (§7).
- Creatures **never die**, level **infinitely**, level 500 ≈ tens of thousands of hours;
  level is asymptotic so matchup/comp still matter (§7a).
- Art via **live gen, cached once per creature**, prompt built from essence fragments (§9).

**Still open**
- **Essence stat-tag design:** how many tag dimensions, and how does the stat-budget split
  across N essences (even? weighted by rarity?)?
- **Puzzle design:** what *is* the puzzle — match-3, logic/Sokoban, word-themed?
- **Move system:** fixed signature move per essence, or a small learnable movepool?
- **Multiplayer:** async battle vs. friends' gardens? Trading essences (not creatures)?
- **Economy of catalysts:** earned only, or the monetization surface?
- **Name / IP:** "Seedlings" is a placeholder.

---

*Pillars locked: anything-can-be-grown (combine collectible essences, no cap) ·
player-as-creator · plant-and-grow (surprise over control) · battle · grow through
investment · clean triangle + rare hidden glass-cannon types.*
