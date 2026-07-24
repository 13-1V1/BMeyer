package com.motley.engine

/**
 * A tiny, dependency-free save format for [Player] state. Deliberately hand-rolled and human-
 * readable (no serialization library) so the engine stays lean and the save format stays easy to
 * inspect and version. Creature *kits* aren't stored — only the recipe + progress — and the kit is
 * rebuilt deterministically on load via the [CreatureFactory], so saves stay small and can't drift
 * from the current balance data.
 *
 * Format (line-based):
 * ```
 * MOTLEY1
 * serial=<nextSerial>
 * ess=<id,id,...>
 * c=<serial>;<nickname>;<level>;<xp>;<blooms>;<id,id,...>   (one per owned creature)
 * ```
 */
object SaveCodec {
    private const val HEADER = "MOTLEY1"

    fun encode(player: Player): String = buildString {
        appendLine(HEADER)
        appendLine("serial=${player.nextSerial}")
        appendLine("ess=${player.essences.sorted().joinToString(",")}")
        for (c in player.roster) {
            val recipe = c.recipe.joinToString(",")
            appendLine("c=${c.serial};${c.nickname};${c.progress.level};${c.progress.xp};${c.progress.blooms};$recipe")
        }
    }.trimEnd('\n')

    fun decode(text: String): Player {
        val lines = text.lines().filter { it.isNotBlank() }
        require(lines.isNotEmpty() && lines.first() == HEADER) { "not a Motley save (bad header)" }

        var nextSerial = 1
        var essences = emptySet<String>()
        val roster = mutableListOf<OwnedCreature>()

        for (line in lines.drop(1)) {
            when {
                line.startsWith("serial=") -> nextSerial = line.removePrefix("serial=").trim().toInt()
                line.startsWith("ess=") ->
                    essences = line.removePrefix("ess=").split(",").filter { it.isNotBlank() }.toSet()
                line.startsWith("c=") -> roster += decodeCreature(line.removePrefix("c="))
                else -> error("unrecognized save line: $line")
            }
        }
        return Player(essences = essences, roster = roster, nextSerial = nextSerial)
    }

    private fun decodeCreature(body: String): OwnedCreature {
        val parts = body.split(";")
        require(parts.size == 6) { "malformed creature record: $body" }
        val (serial, nickname, level, xp, blooms) = parts
        val recipe = parts[5].split(",").filter { it.isNotBlank() }
        val creature = StarterEssences.factory.grow(recipe.map { StarterEssences.require(it) })
        return OwnedCreature(
            serial = serial.toInt(),
            nickname = nickname,
            creature = creature,
            progress = CreatureProgress(level = level.toInt(), xp = xp.toLong(), blooms = blooms.toInt()),
        )
    }
}
