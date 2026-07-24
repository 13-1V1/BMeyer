package com.motley.engine

/**
 * Where growth XP comes from (GDD §5) — creatures grow through *what the player does*, so raising
 * one is itself an act of authorship. Three paths, three playstyles, one shared currency (XP):
 *
 *  - **Battling** — organic but risky; the "raise it in the field" path ([BattleReward]).
 *  - **Training** — safe, player-chosen: an idle timer *or* an active puzzle ([Training]).
 *  - **Catalysts** — consumables that grant an instant burst ([Catalyst]).
 */

/** XP awarded for a battle. Winning is where the growth is; the loser gets a small consolation. */
object BattleReward {
    const val BASE_WIN_XP = 30L
    const val PER_OPPONENT_LEVEL = 6L
    const val CONSOLATION_FRACTION = 0.25

    fun xp(won: Boolean, opponentLevel: Int): Long {
        val full = BASE_WIN_XP + PER_OPPONENT_LEVEL * opponentLevel
        return if (won) full else (full * CONSOLATION_FRACTION).toLong()
    }
}

/**
 * Safe training — the player picks the pace. Idle accrues slowly over real time and is capped per
 * session so it *supplements* battling rather than replacing it; the puzzle rewards active
 * engagement with a burst per solve. Same XP currency either way.
 */
object Training {
    const val IDLE_XP_PER_MIN = 5L
    const val IDLE_SESSION_CAP_MIN = 120       // idle tops out, so it never trivializes progression
    const val PUZZLE_XP_PER_SOLVE = 40L

    fun idleXp(minutes: Int): Long {
        require(minutes >= 0) { "minutes cannot be negative" }
        return IDLE_XP_PER_MIN * minutes.coerceAtMost(IDLE_SESSION_CAP_MIN)
    }

    fun puzzleXp(solves: Int): Long {
        require(solves >= 0) { "solves cannot be negative" }
        return PUZZLE_XP_PER_SOLVE * solves
    }
}

/** A consumable that grants an instant XP burst (earned through play; never sold for power). */
data class Catalyst(val name: String, val xp: Long) {
    init {
        require(xp > 0) { "a catalyst must grant XP" }
    }
}
