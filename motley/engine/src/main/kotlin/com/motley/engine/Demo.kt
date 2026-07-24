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
