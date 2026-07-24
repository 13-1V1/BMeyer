package com.motley.engine

import kotlin.random.Random

/**
 * Generates opponent teams to battle — the foils the player's roster tests itself against. Enemies
 * are grown from the same essence catalog and the same [CreatureFactory], so they obey the same
 * balance rules the player does (no stat cheating). Deterministic given a seed, for reproducible
 * encounters and tests.
 */
object Opponents {
    private const val DEFAULT_TEAM_SIZE = 3

    /**
     * Build a team of [size] creatures at the given [level]. Each is grown from 2–3 random
     * *non-hidden* essences (Wild types stay rare and special — they aren't handed out as fodder).
     */
    fun team(
        random: Random,
        level: Int,
        size: Int = DEFAULT_TEAM_SIZE,
        namePrefix: String = "Wild",
    ): List<Battler> {
        require(level >= 1) { "level starts at 1" }
        require(size >= 1) { "a team needs at least one creature" }
        val pool = StarterEssences.all.filter { !it.hidden }

        return (1..size).map { i ->
            val count = 2 + random.nextInt(2) // 2 or 3 essences
            val picks = pool.shuffled(random).take(count)
            Battler(
                name = "$namePrefix-$i",
                creature = StarterEssences.factory.grow(picks),
                progress = CreatureProgress(level = level),
            )
        }
    }
}
