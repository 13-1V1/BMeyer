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

1. **Anything can be grown.** Creatures are free-form — `dog with feathers`, `angry robot
   tv`, `man bear pig`. The AI-grown premise only pays off if the space is truly open.
2. **You steer, the world surprises.** The player controls the *concepts* (which words, how
   it's raised); the game controls the *execution* (what actually grows). The gap between
   "what I described" and "what I got" is the game.
3. **Authorship through investment.** Attachment comes from effort — sowing the seed *and*
   raising it — not from a lucky pull.
4. **AI is diegetic.** Creatures are grown from raw living essence, so of course each is
   unique and strange. The fiction *explains* the procedural weirdness instead of
   apologizing for it.
5. **Discovery never runs out.** Creatures are infinite; the collectible is your finite,
   growing **vocabulary of concept-words**, and the hidden words keep mystery alive deep
   into the game.

## 3. Core loop

```
Explore → Unlock concept-words → Sow a seed (describe a creature) → Raise it (battle / train / catalysts)
   → Watch it grow through stages → Battle with your team → earn rarer words → sow bolder creatures
```

## 4. Creation: the seed ritual

1. **Sow.** Describe the creature you want with **~3 concept-words** — free-form, from your
   unlocked vocabulary (see §4a). Creatures can be *anything*: `dog with feathers`,
   `angry robot tv`, `man bear pig`, `spiky lava crab`. This is the player's steering — you
   pick the concepts, the world decides how they fuse.
2. **Grow.** The seed matures through **investment**, not passive time (see §5).
3. **Reveal.** At each growth milestone the art gains detail — a real reveal moment each
   stage, ending in a unique AI-authored **art + name + flavor** that is yours alone.

### 4a. Concepts are the input — and the collectible

The old "fixed essence palette" is replaced by **free-form concept-words**, because
AI-grown creatures only pay off if they can be *anything*. Two rules keep it a game, not a
text box:

- **Soft cap of ~3 concepts** (not a rigid word count — `man bear pig` = 3, `dog with
  feathers` = 2, "with" is free). Three concepts keep the generated art **coherent** and
  map cleanly onto ~3 stat-tags for balance. It's the same "2–3 essences" slot as before —
  the words simply *are* the essences, free-form instead of from a menu.
- **Vocabulary is the collectible.** You don't start able to type anything. You **unlock
  concept-words** by exploring and battling: begin with animals + basic traits
  (`feathered dog`), later earn `robot`, `cosmic`, `undead`, `tv`, `ancient`… so late-game
  you can sow `angry robot tv`. *"Gotta collect all the words"* replaces *"gotta catch 'em
  all"* — creativity and power both grow with progression, and the **hidden types** are
  just rare **hidden words** you discover.

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

**The concept-words carry the numbers; the AI only carries the soul.** Each concept the
player types is distilled into **stat-tags** that deterministically contribute to type,
stat curve, and a signature move. The words do double duty: art prompt *and* balanced kit.

> **"angry robot tv"**
> - `angry` → aggressive temperament, +ATK
> - `robot` → Metal-leaning, +DEF, slow
> - `tv` → quirky signature ability ("Broadcast" — a confuse/status move)
>
> → **Art & soul:** whatever the generator dreams up. **Kit:** balanced, derived, predictable.

Under the hood this is a **keyword → stat-tag table** (a `robot`-like word grants the
"mechanical" tag, etc.), with a sensible default for words the table hasn't seen. So the
**build is fully player-authored and always balanced**, while the **appearance is a genuine
surprise**. The magic sentence: *"It plays exactly how I designed it, but it looks like
nothing I expected."* Balance stays under the designer's control — never the model's.

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

## 8. Progression = vocabulary, not creatures

Creatures are infinite; **your concept-word vocabulary is the finite collectible.** Rarer
words (earned from tougher battles / deeper biomes) unlock bolder, stranger creatures — and
rare *hidden words* unlock the Wild types. "Gotta collect every word" replaces "gotta catch
'em all," and every new word multiplies what you can grow. Full vocabulary unlocked = you
can describe truly anything.

## 9. Generation approach (how the art actually happens)

Because creatures can be **anything** (`angry robot tv`), a pre-built composite-parts
library can't cover the space — you can't pre-draw a "tv." So free-form concepts push
toward **live generative image-gen** as the core path. Two facts make that affordable:

- **Pay once, own forever.** Generate the image at seed-time, then **cache it as that
  creature's permanent asset**. Creatures never die (§7a), so the one-time cost amortizes
  over the creature's entire life. Growth stages = a few re-gens (sprout → mature), not one
  per battle.
- **Moderation is mandatory, not optional.** Free-form user text → generated images *will*
  attract abuse. A **content filter on the concept-words** (allow/deny + classifier) gates
  what can be sown. The vocabulary-unlock system (§4a) helps: the pool of usable words is
  curated, not the open dictionary.

**Fallbacks / hybrids to de-risk cost & latency:**
- **Composite art for a stylized MVP.** A parts library keyed to *stat-tags* (not exact
  words) can still render a recognizable creature offline for v0, before wiring live gen.
- **Deferred reveal.** Sowing kicks off async generation; the creature "grows" while the
  image renders in the background — the wait is diegetic, not a spinner.

## 10. Build path (fits the Android/Compose repo)

- **v0 (weekend):** concept-words → stat-tags → seed → reveal (composite art for the MVP);
  the type chart + the keyword→stat-tag mapping as tested pure functions. Battle stubbed.
- **v1:** real turn-based 3v3 battle; growth-over-battles/training; branching Mature forms.
- **v2:** live generative image-gen (cache-once) + prompt moderation; vocabulary-unlock
  progression.
- **v3:** hidden words/types + discovery; catalysts; Awakened forms.
- **later:** biome exploration; multiplayer battles; async garden-vs-garden.

## 11. Resolved & open

**Resolved**
- Creatures are **free-form** — describe anything with ~3 concept-words; words are both art
  prompt and stat-tags; **vocabulary** is the collectible (§4, §4a, §6, §8).
- Training = **idle timer + puzzle**, player's choice, same growth currency (§5).
- Hidden types ≈ **5% / 1-in-20** sow chance (§7).
- Creatures **never die**, level **infinitely**, level 500 ≈ tens of thousands of hours;
  level is asymptotic so matchup/comp still matter (§7a).
- Art via **live gen, cached once per creature**, with mandatory prompt moderation;
  composite art as the v0 fallback (§9).

**Still open**
- **Keyword → stat-tag table:** how many tags, and how do unseen/novel words default?
- **Puzzle design:** what *is* the puzzle — match-3, logic/Sokoban, word-themed?
- **Move system:** fixed signature move per concept, or a small learnable movepool?
- **Multiplayer:** async battle vs. friends' gardens? Trading words (not creatures)?
- **Economy of catalysts:** earned only, or the monetization surface?
- **Name / IP:** "Seedlings" is a placeholder.

---

*Pillars locked: anything-can-be-grown (free-form concepts) · player-as-creator ·
plant-and-grow (surprise over control) · battle · grow through investment · clean triangle +
rare hidden glass-cannon types.*
