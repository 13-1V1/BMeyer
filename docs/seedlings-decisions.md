# 🌱 Seedlings — Decision Log

Studio memory. Each entry: **what** we decided, **why** (grounded in research), what we
**rejected**, and what would make us **revisit**. Newest decisions build on the GDD
(`seedlings-gdd.md`); this file holds the reasoning so we don't re-litigate settled calls.

**North-star (the creative promise):**
> *Grow a one-of-a-kind creature from ideas you combine — then win by out-thinking, not
> out-grinding. Every creature is proof you made it; every battle is a puzzle of synergy.*

The ownable hook, confirmed by research: **no shipped game pairs truly one-of-a-kind
generated creatures with deep synergy combat.** Essences drive both a creature's *look* and
its *kit* — so collecting, "breeding," and team-building collapse into one act: combining
ideas. Guard this above all else.

---

## D1 — Battle depth comes from a second axis ("Momentum" + essence synergy), never more types
**Decided:** Keep the tight 3-type triangle + hidden Wild (no type bloat). Add depth on two
orthogonal axes instead: **(a) a Press-Turn-style tempo mechanic — "Momentum":** hitting a
type weakness or a creature's exposed vulnerability grants an extra action / builds toward a
burst, so exploiting the small chart *compounds*; **(b) essence synergy:** each essence
grants a passive/trait, and combinations create combos (e.g. `Mad + Electric` → an
"Overload" stack). Team-building = combining ideas.
**Why:** Monster Sanctuary's praised 3v3 depth comes from skill-trees + synergy, not its
element chart; SMT's Press Turn makes a small chart feel deep by rewarding smart play with
tempo. A bare 3-type RPS is too shallow to hold 50+ hours. This is also exactly where our
essence system already points — synergy *is* the moat.
**Rejected:** Adding a 4th–12th visible type for depth (that's where Pokémon balance rots).
**Revisit if:** Momentum proves fiddly/unfun in a prototype — then simplify to
status-effects + switching + a turn economy as the depth floor.

## D2 — Creatures must carry *signal*: lineage, serial, nickname, permanence
**Decided:** Every grown creature surfaces **its recipe** (the essences combined), a
**unique serial / discovery ID**, **its creator's name**, and a **birth date**, and is
**player-nicknamed**. Generation is **gated by cost/effort** — never spammable.
**Why:** The anti-slop insight — *"AI art lacks signal, not soul."* Generated content feels
dead when it's the statistical average of a prompt; it feels *authored* when it visibly
encodes the player's specific choices. Players bond with *their individual* creature
(shinies, nicknames, Nuzlocke), not a species. Spam-generation turns companions into
slot-machine pulls and kills attachment. This is our single biggest attachment lever.
**Rejected:** Free/infinite re-rolling; disposable "Palworld-dark" framing (opposite of the
bond we want).
**Revisit if:** never lightly — this is load-bearing.

## D3 — Level is soft-capped in impact; power lives in build & synergy
**Decided:** Reaffirm GDD §7a — per-level stat gains diminish hard; a clever low-level
synergy team can beat a brute-forced high-level one. Infinite leveling is a *prestige/idle*
axis, not the win condition.
**Why:** Siralim's uncapped leveling works only because power is horizontal (build/synergy).
If level alone wins, PvP dies and team-building is pointless. Preserves "never die + level
forever" without letting it flatten strategy.

## D4 — Hidden (Wild) types gated by a real drawback, not just rarity
**Decided:** Wild types stay glass cannons **and are hard-countered by common status**
(so a status-teching team reliably beats them). Rare must never mean *strictly stronger*.
**Why:** In every game studied, the highest-power option becomes the *only* option unless it
carries a genuine weakness; "rare = better" is the power-creep spiral that turns 90% of the
roster into dead content.

## D5 — Framing: "AI as the toy," never "AI instead of artists"
**Decided:** Public posture is **"infinite creatures only *you* can make."** A human-authored
art style and curation visibly wrap every generation. Disclose AI use honestly.
**Why:** Steam-disclosed AI titles see up to ~53% fewer reviews/sales and concealment is
worse (Postal: Bullet Paradise cancelled a day after a hidden-AI reveal). Players
review-bomb AI that *replaces* craft, but embrace AI that *is the interactive mechanic*
(Suck Up!). Our generation is the toy, so we win by leaning in, not hiding.
**Revisit if:** platform sentiment shifts materially.

## D6 — Moderation is a launch blocker, and our curated vocabulary is the advantage
**Decided:** Two-layer safety, shipped at v1-with-generation, not later: **(1)** pre-vet the
entire essence vocabulary + screen unusual combinations; **(2)** post-generation image
classifier auto-hides over threshold, plus **user flag/block** on any shared creature, plus
age-gating per store rules.
**Why:** Google Play now holds us liable for *user-generated* AI outputs and requires
*proactive* prevention + in-app flagging (AI-content removals +190% YoY H1 2025); Apple/Steam
similar. Because players combine *our* curated words (not free text), we can pre-vet the
input space and design most abuse out — a structural edge over open-prompt tools.

## D7 — Generation tech: cloud + house-style model, cache forever, hide latency in the hatch
**Decided:** **Cloud image-gen** through a **consistent style model (LoRA/house style)** so
every creature shares a brand look; **generate once, cache permanently** as a static asset;
**hide the 3–8s latency inside the hatch ritual**; pre-generate popular combos server-side
for instant serves. On-device gen is a later "nice to have," not the launch path.
**Why:** Cloud is ~$0.01–0.08/image (our dominant variable cost — must be modeled against
monetization). On-device is ~free but needs high-end NPUs + 6–8GB and quantized models can't
match the quality a creature-beauty game lives on. Caching + a summoning ritual turns a
technical wait into anticipation.

## D8 — Art: flat/vector base + a programmatic cohesion pipeline on every creature
**Decided:** House style is **flat/vector + minimalist**, bright and high-contrast for
phones; **creatures are the only fully-saturated element** against a calm botanical UI.
Every generated creature is forced through the **same runtime treatment**: a **locked master
palette (~12–16 colors, quantized)**, **one consistent outline**, a **single fixed light
direction + 3-shade cel structure**, and **identical card framing/vignette** — ideally baked
into a **shader + palette LUT** so cohesion is automatic and free.
**Why:** This is make-or-break. Unconstrained procedural variety reads as noise (Spore, early
No Man's Sky). The three documented cohesion levers — consistent palette, outline, and
light-source — are what make a generated menagerie look like *one game*. Baking them into a
shader is the correct move when assets are unpredictable.
**Palette direction:** organic neutrals (moss, cream, loam) + luminous accent gradients
(teal→lime, amber→magenta) reserved for essence energy, rarity glow, and the hatch. Rarity =
glow intensity/palette tier, consistent across all creatures.

## D9 — Retention: idle-collection daily loop + seasonal pass + collection codex
**Decided:** Daily loop is **low-friction return-to-collect** (offline training accrual +
a "check on your Seedlings" moment) — **no punitive energy gate**. Ship **one seasonal
battle pass** (daily/weekly quests) and a **codex** where completing essence-families grants
small **permanent** buffs. Build this 20–50h retention layer **before launch** (the Temtem
lesson), using generation as the infinite mid-game content engine.
**Why:** Battle passes are in ~60% of top-grossing US iOS titles and are the highest
revenue-per-eng-hour retention tool for a solo dev; low-friction daily rewards beat stamina
walls; collection-completion buffs (AFK Arena) convert "catch 'em all" into long-term
retention. Temtem collapsed (~2k daily) from a thin endgame bolted on late — we front-load it.

## D10 — Monetization: cosmetic-first hybrid as a brand differentiator
**Decided:** **Money buys time and looks, never power.** (a) One-time **"Gardener's Pack"**
IAP — removes ads + unlocks a cosmetic set; (b) **~$5 seasonal pass** (cosmetic + convenience);
(c) **light gacha ONLY on cosmetic essence-skins/colorways**, with a **visible pity counter +
published odds**. Anything affecting battle strength is earnable through play.
**Why:** Cosmetics are the accepted "ethical" lever; Fortnite/Genshin are the reputational
templates. For a game whose soul is *your* unique creature, pay-to-win would poison the well —
so ethical monetization isn't just nice, it's on-brand.
**Revisit if:** the user wants a premium (paid-once, no F2P) model instead — this is a
values/business fork worth a direct check-in.

## D11 — "Bloom" (prestige rebirth) gives infinite leveling a purpose
**Decided:** A **voluntary** reset: sacrifice a maxed creature's levels for a **permanent
essence bonus** or a **rare cosmetic evolution**. The classic idle ascension loop.
**Why:** Infinite leveling needs a *reason* beyond a bigger number; prestige creates the
endless soft-cap → reset → boost loop that sustains idle games — and it fits "your garden is
forever" (you don't lose the creature, you *bloom* it).

## D12 — Audio: FMOD adaptive layers; spend on feel-SFX and the hatch stinger
**Decided:** **FMOD** (free indie tier) for **adaptive music** — one garden bed that layers
in percussion as battle intensity rises and resolves to a victory sting. Prioritize the
**feel-SFX cluster**: UI/collect "pop," attack impacts, level-up chime, and above all the
**hatch stinger**.
**Why:** Adaptive stems from one composition outperform many static loops on a tiny budget
(audio is ~5–10% of scope); high-frequency micro-sounds drive moment-to-moment satisfaction
more than music does.

## D13 — The hatch/reveal is the signature set-piece (the euphoria moment)
**Decided:** Engineer the hatch as escalation with **anticipation → held silence → burst →
settle**: shell shakes/cracks/light-leaks and rising audio that **drops to silence**, then a
~0.05s freeze, screen shake, radial particle burst, palette-glow bloom, rising chime + bass
hit, then the creature settles into its card. **Rarity is gated into the cadence** (rarer =
longer anticipation, brighter burst, unique sting). Reuse this exact juice recipe for battle
crits and collection-completion so the whole game speaks one satisfying language.
**Why:** The documented gacha secret is *the pause* before the reveal. This moment is where
our whole premise (a creature no one has ever seen) pays off emotionally — it deserves to be
the most polished five seconds in the game.

---

*Open forks flagged for the user (values/business, not craft): premium vs. F2P model (D10),
and the AI-content disclosure copy. Everything else the studio is driving.*
