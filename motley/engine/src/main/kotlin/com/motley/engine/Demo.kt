package com.motley.engine

import kotlin.random.Random

/**
 * A tiny console demo of the essence engine — run with `./gradlew :engine:run`. It grows a few
 * creatures, shows that the stat budget is fixed no matter how many essences you fuse, and prints
 * the type triangle. Not part of the game; just a way to watch the engine breathe.
 */
private const val BAR_WIDTH = 24

fun main() {
    val f = StarterEssences.factory

    section("THREE CREATURES, GROWN FROM ESSENCES")
    show("Mad + Robo + Electric  (the angry robot)", f.grow(essences("mad", "robo", "electric")))
    show("Feathered + Storm  (a stormbird)", f.grow(essences("feathered", "storm")))
    show("Void + Mad  (a Wild horror)", f.grow(essences("void", "mad")))

    section("WILDER, NOT STRONGER — same budget, more essences = flatter spread")
    val specialist = f.grow(essences("mad", "electric"))
    val generalist = f.grow(essences("mad", "electric", "robo", "ancient", "tide", "feathered"))
    printLine("2 essences (specialist)", specialist.stats)
    printLine("6 essences (generalist)", generalist.stats)
    println("  both total exactly ${specialist.stats.total} — combining more spreads the budget, it doesn't inflate it.")

    section("THE TYPE TRIANGLE")
    for (a in listOf(Type.EMBER, Type.THORN, Type.TIDE, Type.WILD)) {
        val row = listOf(Type.EMBER, Type.THORN, Type.TIDE, Type.WILD).joinToString("  ") { d ->
            "%s>%s %4s".format(a.name.take(3), d.name.take(3), label(TypeChart.effectiveness(a, d)))
        }
        println("  $row")
    }

    section("A BATTLE — Ember team vs Thorn team (seed 7)")
    val teamA = listOf(battler("Blaze", "mad", "electric"), battler("Cinder", "ember", "solar"))
    val teamB = listOf(battler("Bulwark", "ancient", "giant"), battler("Bramble", "thorn", "fungal"))
    println("  A: ${teamA.joinToString(", ") { "${it.name} (${it.type})" }}")
    println("  B: ${teamB.joinToString(", ") { "${it.name} (${it.type})" }}")
    println()
    val result = BattleResolver(Random(7)).resolve(teamA, teamB)
    result.events.forEach { println("    ${narrate(it)}") }
    println()
    println("  ${result.outcome} in ${result.rounds} rounds  (survivors  A:${result.survivorsA}  B:${result.survivorsB})")

    section("GROWTH — raise a creature through investment")
    val base = StarterEssences.factory.grow(essences("beast", "thorn")).stats
    var progress = CreatureProgress()
    println("  fresh creature — base stats  ${base.asList()}  total ${base.total}")
    fun report(what: String) {
        val s = Growth.effectiveStats(base, progress)
        println("    after %-22s Lv %-3d %-9s stats %s (total %d)".format(
            what, progress.level, progress.stage, s.asList(), s.total))
    }
    progress = Leveling.gainXp(progress, BattleReward.xp(won = true, opponentLevel = 8)); report("a battle win")
    progress = Leveling.gainXp(progress, Training.puzzleXp(solves = 6)); report("6 puzzle solves")
    progress = Leveling.gainXp(progress, Training.idleXp(minutes = 120)); report("2h idle training")
    progress = Leveling.gainXp(progress, Catalyst("Sunburst", 5_000).xp); report("a Sunburst catalyst")

    println()
    println("  asymptotic leveling (why grinding alone can't win):")
    for (lv in listOf(1, 40, 90, 200, 500)) {
        println("    Lv %-3d  stat x%.2f".format(lv, Leveling.levelMultiplier(lv)))
    }

    println()
    val maxed = CreatureProgress(level = Leveling.BLOOM_MIN_LEVEL)
    val bloomed = Leveling.bloom(maxed)
    println("  Bloom: Lv ${maxed.level} -> reborn at Lv ${bloomed.level} with ${bloomed.blooms} Bloom " +
        "(permanent x%.2f forever)".format(Leveling.bloomBonus(bloomed.blooms)))

    section("A FULL SESSION — collect, grow, battle, level, save")
    var player = Player()
        .collect("mad").collect("electric").collect("ember").collect("solar").collect("beast").collect("thorn")
        .grow("Blaze", listOf("mad", "electric"))
        .grow("Cinder", listOf("ember", "solar"))
        .grow("Fang", listOf("beast", "thorn"))
    println("  Player's roster:")
    player.roster.forEach { println("    #${it.serial} ${it.nickname} (${it.creature.type}) — ${it.recipe.joinToString(" + ")}") }
    println()

    // Train the team first (idle + puzzle), then head out — investment before battle.
    val trainingXp = Training.idleXp(minutes = 120) + Training.puzzleXp(solves = 15)
    player.roster.forEach { player = player.awardXp(it.serial, trainingXp) }
    println("  after training: ${player.roster.joinToString("  ") { "${it.nickname} Lv${it.progress.level} (${it.progress.stage})" }}")
    println()

    repeat(4) { i ->
        val foeLevel = 2 + i
        val enc = Session.encounter(player, Random((i + 1).toLong()), opponentLevel = foeLevel)
        player = enc.player
        val levels = player.roster.joinToString(" ") { "${it.nickname[0]}:Lv${it.progress.level}" }
        println("    encounter ${i + 1} (foe Lv $foeLevel): ${if (enc.won) "WON " else "lost"}  ->  roster $levels")
    }

    println()
    val save = SaveCodec.encode(player)
    val reloaded = SaveCodec.decode(save)
    println("  saved ${save.length} chars; reloaded roster matches original: ${reloaded == player}")
    println()
}

private fun battler(name: String, vararg ids: String): Battler =
    Battler(name, StarterEssences.factory.grow(ids.map { StarterEssences.require(it) }))

private fun narrate(e: BattleEvent): String = when (e) {
    is BattleEvent.Attack -> "%s hits %s for %d%s%s".format(
        e.attacker, e.defender, e.damage,
        when {
            e.effectiveness > 1.0 -> "  (super!)"
            e.effectiveness < 1.0 -> "  (resisted)"
            else -> ""
        },
        if (e.crit) "  CRIT" else "",
    )
    is BattleEvent.Faint -> "-- ${e.name} faints --"
    is BattleEvent.Burst -> ">> Momentum burst! ${e.name} acts again"
    is BattleEvent.Victory -> "== ${e.outcome} =="
}

private fun essences(vararg ids: String): List<Essence> = ids.map { StarterEssences.require(it) }

private fun show(title: String, c: Creature) {
    println("  $title")
    println("    type:      ${c.type}${if (c.isWild) "  (glass cannon, off-triangle)" else ""}")
    printLine("    stats", c.stats, indent = "    ")
    println("    abilities: ${c.abilities.joinToString(", ").ifEmpty { "—" }}")
    if (c.synergies.isNotEmpty()) println("    synergy:   ${c.synergies.joinToString(", ") { it.name }}")
    println("    recipe:    ${c.recipe.joinToString(" + ")}")
    println("    art:       \"${c.artPrompt}\"")
    println()
}

private fun printLine(label: String, s: Stats, indent: String = "  ") {
    val parts = listOf("HP" to s.hp, "ATK" to s.atk, "DEF" to s.def, "SPD" to s.spd)
        .joinToString("  ") { (n, v) -> "%s %3d %s".format(n, v, bar(v)) }
    println("$indent%-26s %s  (sum %d)".format(label, parts, s.total))
}

private fun bar(v: Int): String {
    val filled = (v * BAR_WIDTH / 120).coerceIn(0, BAR_WIDTH)
    return "#".repeat(filled)
}

private fun label(m: Double): String = when {
    m > 1.0 -> "2x"
    m < 1.0 -> "0.5x"
    else -> "1x"
}

private fun section(title: String) {
    println()
    println("-- $title ".padEnd(72, '-'))
    println()
}
