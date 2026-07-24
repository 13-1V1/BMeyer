package com.motley.engine

/**
 * The authored starter essence catalog — Motley's first collectible vocabulary. Each entry sets a
 * stat *shape*, an [Element] for the type triangle, an ability seed, and an art fragment. Hidden
 * (Wild) essences are glass cannons by shape (attack/speed heavy, defence/HP light).
 *
 * This is game *content*, not engine logic — it lives here so tests and the demo have a real world
 * to play with, and so balance lives in data a designer can tune without touching the factory.
 */
object StarterEssences {

    // --- EMBER — heat, energy, aggression -----------------------------------------------------
    val EMBER = Essence("ember", "Ember", Element.EMBER, StatWeights(1.0, 3.0, 1.0, 2.0),
        "wreathed in living flame, molten cracks, ember glow", Rarity.COMMON, ability = "Ignite")
    val ELECTRIC = Essence("electric", "Electric", Element.EMBER, StatWeights(1.0, 3.0, 0.0, 3.0),
        "crackling with electricity, arcing sparks, neon filaments", Rarity.COMMON, ability = "Shock")
    val MAD = Essence("mad", "Mad", Element.EMBER, StatWeights(1.0, 3.0, 1.0, 2.0),
        "wild-eyed and furious, bared teeth, manic energy", Rarity.COMMON, ability = "Frenzy")
    val ROBO = Essence("robo", "Robo", Element.EMBER, StatWeights(2.0, 2.0, 3.0, 0.0),
        "robotic, chrome plating, exposed circuitry, glowing eyes", Rarity.UNCOMMON, ability = "Overclock")
    val SOLAR = Essence("solar", "Solar", Element.EMBER, StatWeights(2.0, 2.0, 1.0, 2.0),
        "radiant, sun-gold aura, blinding corona", Rarity.RARE, ability = "Flare")

    // --- THORN — nature, earth, beast ---------------------------------------------------------
    val THORN = Essence("thorn", "Thorn", Element.THORN, StatWeights(2.0, 2.0, 2.0, 1.0),
        "bristling with thorns, bramble hide, woody bark", Rarity.COMMON, ability = "Barbs")
    val FEATHERED = Essence("feathered", "Feathered", Element.THORN, StatWeights(1.0, 1.0, 1.0, 3.0),
        "covered in soft feathers, plumed crest, avian grace", Rarity.COMMON, ability = "Gust")
    val ANCIENT = Essence("ancient", "Ancient", Element.THORN, StatWeights(3.0, 1.0, 3.0, 0.0),
        "ancient and mossy, weathered stone skin, ruin-worn", Rarity.UNCOMMON, ability = "Endure")
    val GIANT = Essence("giant", "Giant", Element.THORN, StatWeights(3.0, 2.0, 2.0, 0.0),
        "colossal, hulking, ground-shaking bulk", Rarity.UNCOMMON, ability = "Stomp")
    val FUNGAL = Essence("fungal", "Fungal", Element.THORN, StatWeights(3.0, 1.0, 2.0, 1.0),
        "fungal caps, spongy flesh, drifting spores", Rarity.COMMON, ability = "Spore")
    val BEAST = Essence("beast", "Beast", Element.THORN, StatWeights(2.0, 3.0, 1.0, 2.0),
        "fanged and furred, feral beast, powerful haunches", Rarity.COMMON, ability = "Maul")

    // --- TIDE — water, ice, flow --------------------------------------------------------------
    val TIDE = Essence("tide", "Tide", Element.TIDE, StatWeights(2.0, 1.0, 2.0, 2.0),
        "a flowing body of water, translucent, rippling currents", Rarity.COMMON, ability = "Douse")
    val FROST = Essence("frost", "Frost", Element.TIDE, StatWeights(2.0, 1.0, 3.0, 1.0),
        "rimed in frost, icicle spines, frozen breath", Rarity.COMMON, ability = "Chill")
    val MIST = Essence("mist", "Mist", Element.TIDE, StatWeights(1.0, 1.0, 2.0, 3.0),
        "half-dissolved into mist, ghostly, diffuse edges", Rarity.UNCOMMON, ability = "Veil")
    val VENOM = Essence("venom", "Venom", Element.TIDE, StatWeights(1.0, 3.0, 1.0, 2.0),
        "venomous, dripping toxins, sickly iridescence", Rarity.COMMON, ability = "Envenom")
    val DEEP = Essence("deep", "Deep", Element.TIDE, StatWeights(3.0, 2.0, 2.0, 0.0),
        "abyssal, bioluminescent lures, deep-sea armor", Rarity.RARE, ability = "Pressure")
    val STORM = Essence("storm", "Storm", Element.TIDE, StatWeights(1.0, 2.0, 1.0, 3.0),
        "wreathed in storm clouds, rain-lashed, wind-torn", Rarity.UNCOMMON, ability = "Squall")

    // --- HIDDEN — the rare Wild family: glass cannons, off the triangle ------------------------
    val VOID = Essence("void", "Void", Element.THORN, StatWeights(1.0, 3.0, 0.0, 3.0),
        "a hole in reality, starless void, edges that hurt to look at",
        Rarity.HIDDEN, ability = "Unmake", hidden = true)
    val DREAM = Essence("dream", "Dream", Element.TIDE, StatWeights(1.0, 3.0, 1.0, 3.0),
        "shifting dream-stuff, impossible colors, half-formed shapes",
        Rarity.HIDDEN, ability = "Lucid", hidden = true)
    val GLITCH = Essence("glitch", "Glitch", Element.EMBER, StatWeights(1.0, 3.0, 0.0, 3.0),
        "glitching and datamoshed, flickering artifacts, corrupted texture",
        Rarity.HIDDEN, ability = "Corrupt", hidden = true)

    /** Every starter essence, common through hidden. */
    val all: List<Essence> = listOf(
        EMBER, ELECTRIC, MAD, ROBO, SOLAR,
        THORN, FEATHERED, ANCIENT, GIANT, FUNGAL, BEAST,
        TIDE, FROST, MIST, VENOM, DEEP, STORM,
        VOID, DREAM, GLITCH,
    )

    val byId: Map<String, Essence> = all.associateBy { it.id }

    /** Look an essence up by id, or fail loudly (ids come from the curated catalog). */
    fun require(id: String): Essence =
        byId[id] ?: throw IllegalArgumentException("no such essence: $id")

    /**
     * Starter synergies — named combos certain essence pairings unlock. Those with a mechanical
     * [SynergyEffect] change how the creature fights; the rest are flavor whose effects are still
     * to be wired into the battle system (status, damage reduction, etc.).
     */
    val synergies: List<Synergy> = listOf(
        Synergy("Overload", setOf("mad", "electric"),
            "Builds Momentum faster — anger and current, feeding each other.",
            effects = setOf(SynergyEffect.FAST_MOMENTUM)),
        Synergy("Skyborne", setOf("feathered", "storm"),
            "Rides the storm: always acts first, whatever its Speed.",
            effects = setOf(SynergyEffect.ACTS_FIRST)),
        Synergy("War Machine", setOf("robo", "mad"),
            "Gains Defence each turn it attacks — anger, weaponized.",
            effects = setOf(SynergyEffect.FORTIFY)),
        Synergy("Miasma", setOf("fungal", "venom"),
            "Its poisons and plagues cling longer — status effects last extra turns.",
            effects = setOf(SynergyEffect.CONTAGION)),
        Synergy("Titan", setOf("ancient", "giant"),
            "Immovable: takes reduced damage from Momentum bursts."),     // effect: TODO (braced)
    )

    /** A [CreatureFactory] preloaded with the starter synergies. */
    val factory: CreatureFactory = CreatureFactory(synergies)
}
