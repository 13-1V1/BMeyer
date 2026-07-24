# 🌱 MOTLEY — Game Design Doc (v0.1)

*A creature-collector where you **combine, grow, and battle** creatures no one has ever seen.*

> Name **locked: Motley** (a *motley* = a diverse mix of mismatched things — exactly what a
> creature fused from clashing essences is). Creatures are called **Motleys**. Core pillars and
> all major systems are locked; see §15 for what remains open.

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
- Depth comes from **team composition + synergy**, not a huge move database.

> **Design note (see decision D1):** a bare 3-type triangle is too shallow to hold 50+
> hours on its own — genre research is unambiguous. So depth lives on **two orthogonal
> axes**, added *instead of* more types (type bloat is where balance rots).

### Depth axis 1 — Momentum (reward smart play with tempo)

Inspired by SMT's Press Turn: **exploiting a weakness earns tempo.** When you hit a type
weakness or a creature's exposed vulnerability, you gain **Momentum** — an extra action or a
charge toward a burst; whiffing or hitting a resist loses it. This makes the *small* chart
feel deep, because exploiting it **compounds**: a well-sequenced turn snowballs, a sloppy one
stalls. Skill and read-the-board matter more than raw stats — which is exactly what keeps
infinite leveling (§7a) from flattening strategy.

### Depth axis 2 — Essence synergy (team-building = combining ideas)

Each essence grants a **passive/trait**, and essences **combine into synergy combos** — e.g.
`Mad + Electric` → an "Overload" stack that pays off over several turns. Because a creature's
essences drive *both* its look and its kit, **building a team is combining ideas**, not
memorizing a movepool. This collapses collection, breeding, and theorycrafting into one novel
loop — the genre's open, ownable gap (no shipped game pairs one-of-a-kind generated creatures
with deep synergy combat).

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
- **Glass cannon with a real drawback (D4).** Generally **high attack, low defense**, *and*
  **hard-countered by common status** — a status-teching team reliably beats them. Rare must
  never mean *strictly stronger*: that's the power-creep spiral that turns 90% of the roster
  into dead content. Fielding a Wild is a high-risk *decision*, not a free upgrade.
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
- **Bloom (prestige rebirth, D11).** A *voluntary* reset: sacrifice a maxed creature's levels
  for a **permanent essence bonus** or a **rare cosmetic evolution**. Gives infinite leveling
  a purpose (the idle-game ascension loop) — and fits "your garden is forever": you don't lose
  the creature, you *bloom* it.

### 7b. Why players will love a *specific* creature (attachment — D2)

Research is blunt here: players bond with **their individual creature**, not a species, and
generated content only feels *authored* when it visibly encodes their choices ("AI lacks
signal, not soul"). So every creature carries **signal**:

- **Its recipe** — the exact essences you combined, shown on its card.
- **A unique serial / discovery ID**, **your name** as its creator, and a **birth date**.
- **A player-chosen nickname.**
- **Permanence + earned-ness:** creatures never die (above), and **generation is gated by
  cost/effort — never spammable.** A creature is a companion you grew, not a slot-machine pull.

This is the single biggest attachment lever *and* the anti-slop moat — protect it.

## 8. Progression = essences, not creatures

Creatures are infinite; **your essence library is the finite collectible.** Rarer essences
(earned from tougher battles / deeper biomes) unlock bolder, stranger creatures — and rare
*hidden essences* unlock the Wild types. "Gotta collect every essence" replaces "gotta catch
'em all," and every new essence multiplies what you can grow. Because combination has no
cap, even a modest library yields a combinatorial explosion of possible creatures.

## 9. Generation approach (how the art actually happens)

Because creatures are **open-ended essence combinations** (`Mad + Robo + Electric`), a
pre-built composite-parts library can't cover the space. So generation is **cloud image-gen
through a consistent house-style model** (LoRA / fixed style), driven by the concatenated
**art-prompt fragments** of the chosen essences (§4a). Decision D7:

- **One house style, always.** Every creature runs through the same style model **and** the
  cohesion pipeline in §10, so a chaotic generated menagerie still looks like *one game*.
- **Pay once, own forever.** Generate at seed-time (~$0.01–0.08/image — our dominant variable
  cost, modeled against monetization), then **cache permanently** as that creature's asset.
  Creatures never die (§7a), so the cost amortizes over the creature's whole life.
- **Hide latency inside the hatch ritual.** The 3–8s generation happens *behind* the
  hatch set-piece (§11) — the wait becomes anticipation, not a spinner. Pre-generate popular
  combos server-side for instant serves.
- **On-device gen is later, not launch.** Quantized mobile models can't match the quality a
  creature-beauty game lives on; cloud + caching is the launch path.

**MVP fallback:** a composite-parts library keyed to *stat-tags* can render stylized
creatures offline for v0, before live gen is wired in v2.

Moderation is **not** optional and not "mostly structural" — see the full policy in §13.

## 10. Art direction (D8)

House style is **flat/vector + minimalist** — cheap to iterate, tiny files, scales across
devices, and reads bright and high-contrast on small phone screens. **Creatures are the only
fully-saturated element** on screen; the UI is a calm, rounded, botanical frame with generous
whitespace and large tap targets, so the creature always pops.

**The cohesion pipeline (make-or-break).** Unconstrained procedural variety reads as *noise*
(Spore, early No Man's Sky). Every generated creature is forced through the **same runtime
treatment** — ideally a **shader + palette LUT** so it's automatic and free:

1. **Locked master palette** (~12–16 colors) — quantize/recolor every creature to it.
2. **One consistent outline** (uniform stroke) on all creatures.
3. **Single fixed light direction + 3-shade cel structure**, identical everywhere.
4. **Identical card framing / vignette** — every creature sits in the same "container."

Consistent *palette + outline + light-source* are the three documented cohesion levers;
enforcing them programmatically is what makes the menagerie feel authored.

**Palette theme ("growing living essence"):** organic neutrals (moss, cream, loam) + luminous
accent gradients (teal→lime, amber→magenta) reserved for essence energy, **rarity glow**, and
the hatch. Rarity is read as glow intensity / palette tier, consistent across all creatures.

## 11. Audio & game feel (D12, D13)

- **Adaptive music via FMOD** (free indie tier): one garden ambient bed that layers in
  percussion as battle intensity rises and resolves to a short victory sting. Adaptive stems
  from one composition beat many static loops on a tiny budget (~5–10% of scope).
- **Spend on the feel-SFX cluster:** UI/collect "pop," attack impacts, level-up chime, and
  above all the **hatch stinger** — high-frequency micro-sounds drive satisfaction more than
  music does.

**The hatch/reveal is the signature set-piece** — the five most-polished seconds in the game,
where "a creature no one has ever seen" pays off emotionally. Structure = **anticipation →
held silence → burst → settle**:
1. **Anticipation:** shell shakes, cracks, light leaks; audio rises, then **drops to silence.**
2. **Burst:** ~0.05s screen freeze, screen shake, radial particle burst, palette-glow bloom,
   rising chime + bass hit.
3. **Settle:** the creature lands into its card with a soft chime.

Rarity is gated into the cadence (rarer = longer anticipation, brighter burst, unique sting).
**Reuse this exact juice recipe** for battle crits and collection-completion so the whole game
speaks one satisfying language.

## 12. Retention & monetization (D9, D10, D11)

**Retention — build the 20–50h layer *before* launch** (the Temtem lesson; use generation as
the infinite mid-game content engine):
- **Daily loop = low-friction return-to-collect**, *not* a punitive energy gate: offline
  training accrual (§5 idle) + a "check on your Motleys" moment.
- **One seasonal battle pass** with daily/weekly quests — highest retention-per-eng-hour for a
  solo dev.
- **Collection codex:** completing essence-families grants small **permanent** buffs — turns
  "collect 'em all" into long-term retention.
- **Bloom** prestige loop (§7a) for the endless soft-cap → reset → boost cadence.

**Monetization — cosmetic-first, as a brand differentiator. Money buys time and looks, never
power:**
- One-time **"Gardener's Pack"** — removes ads + a cosmetic set (cleanest goodwill move).
- **~$5 seasonal pass** — cosmetic + convenience.
- **Light gacha ONLY on cosmetic essence-skins/colorways**, with a **visible pity counter +
  published odds**. Anything touching battle strength is **earnable through play.**

> Because the game's soul is *your* unique creature, pay-to-win would poison the well — ethical
> monetization is on-brand, not just nice. **Business model: F2P, locked.**

## 13. AI content: policy & moderation (D5, D6)

- **Framing (D5):** generation is **invisible plumbing** — we neither hide it nor sell it.
  Player-facing language never says "AI"; creatures are simply **grown** (from essences). The
  public promise is "**every creature is one-of-a-kind — and yours**," not "AI-powered." We
  still tick the mandatory store **disclosure checkbox** at submission — compliance, not
  marketing.
- **Moderation is a launch blocker, shipped with generation — two layers:**
  1. **Pre-generation:** the essence vocabulary is **fully pre-vetted**; unusual *combinations*
     are screened. (Curated words instead of free text is our structural safety edge.)
  2. **Post-generation:** an image classifier auto-hides anything over threshold; **user
     flag/block** on every shared creature; **age-gating** per store policy.
- **Why non-negotiable:** platforms now hold us liable for user-generated AI output and require
  *proactive* prevention + in-app flagging. An unmoderated generator gets rejected or pulled.

## 14. Build path (fits the Android/Compose repo)

> **Progress:** the `motley/engine` module now implements the tested pure-Kotlin core of v0 and
> most of v1's systems — `CreatureFactory` (fixed-budget combination), `TypeChart`, `Momentum`,
> `Synergy`, `StarterEssences`, the 3v3 `BattleResolver`, and the growth/leveling system
> (`Leveling`/`Growth`/`Training`/`BattleReward`/`Catalyst`, incl. Bloom). The whole core loop —
> grow → fight → invest → grow stronger — is real and green in CI. Remaining v1 work:
> branching Mature forms, the hatch juice recipe, and the UI layer.

- **v0 (weekend):** essences → combine → seed → reveal (composite art for the MVP); the
  **type chart + Momentum + essence-combination/stat-budget math** as tested pure functions.
  ✅ *engine done.* The reveal already carries signal (recipe + serial + nickname).
- **v1:** real turn-based 3v3 battle with **Momentum + essence synergy** ✅ *resolver done*;
  growth-over-battles/training ✅ *system done*; branching Mature forms; the hatch juice recipe (§11).
- **v2:** live generative image-gen (house-style model, cache-once) **+ the full moderation
  layer (§13, ships together)**; the cohesion pipeline (§10); essence-collection codex.
- **v3:** hidden essences/types + discovery; catalysts; Awakened + Bloom forms; daily loop +
  seasonal pass.
- **later:** biome exploration; multiplayer battles; async garden-vs-garden; on-device gen.

## 15. Resolved & open

**Resolved** (full rationale in `motley-decisions.md`)
- Creation = **combine 2+ collectible essences, no maximum**; essences carry stat-tags +
  art-prompt fragment; the essence **library** is the collectible (§4, §4a, §6, §8).
- **Fixed stat budget** → more essences = wilder, not stronger (§6).
- Battle depth = **Momentum (tempo) + essence synergy**, not more types (§7, D1).
- Hidden types: **glass cannon + hard-countered by status**, ≈5% sow chance (§7, D4).
- **Attachment via signal:** recipe + serial + creator + nickname + permanence; generation
  gated, never spammable (§7b, D2).
- Creatures **never die**, level **infinitely** (asymptotic); **Bloom** prestige loop (§7a).
- **Art:** flat/vector + programmatic cohesion pipeline over house-style live gen (§9, §10).
- **Audio/juice:** FMOD adaptive layers; the **hatch** is the signature set-piece (§11).
- **Retention:** idle daily loop + seasonal pass + codex, built pre-launch (§12).
- **Monetization:** cosmetic-first, money buys time & looks, never power (§12).
- **AI policy:** "AI as the toy," honest disclosure; two-layer moderation is a launch blocker
  (§13).

**Still open**
- **Essence stat-tag design:** how many tag dimensions; how the stat-budget splits across N
  essences (even? rarity-weighted?). *← the studio's next build target.*
- **Momentum tuning:** how much tempo a weakness-hit grants before it snowballs too hard.
- **Puzzle design:** what *is* the training puzzle — match-3, logic, word-themed?
- **Multiplayer:** async garden-vs-garden? Trading essences (never creatures)?
- *(Name **Motley**, F2P, and AI-framing all locked — see `motley-decisions.md`.)*

---

*North-star: grow a one-of-a-kind creature from ideas you combine — then win by out-thinking,
not out-grinding.*

*Pillars locked: anything-can-be-grown (combine essences, no cap) · player-as-creator ·
plant-and-grow (surprise over control) · out-think-don't-out-grind battle (Momentum + synergy)
· grow through investment · signal-rich, permanent, beloved creatures · one cohesive look over
a chaotic menagerie · ethical cosmetic-first monetization.*
