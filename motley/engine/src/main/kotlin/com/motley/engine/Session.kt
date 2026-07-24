package com.motley.engine

import kotlin.random.Random

/** The outcome of one encounter: the (XP-updated) player, the battle log, and whether they won. */
data class EncounterResult(
    val player: Player,
    val battle: BattleResult,
    val won: Boolean,
)

/**
 * Ties the pieces into one turn of the actual game loop: take the player's team, generate an
 * opponent, resolve the battle, and award XP to the creatures that fought. This is the seam the UI
 * will drive — everything below it is already built and tested.
 */
object Session {
    const val TEAM_SIZE = 3

    /**
     * Run one encounter. With [scout] on, the player sees the enemy's lead and reorders their team
     * for the best opening matchup (D15 matchup agency); off, they field roster order as-is.
     */
    fun encounter(
        player: Player,
        random: Random,
        opponentLevel: Int,
        scout: Boolean = false,
    ): EncounterResult {
        require(player.roster.isNotEmpty()) { "the player needs at least one creature to battle" }

        val enemy = Opponents.team(random, opponentLevel, size = TEAM_SIZE)
        val bench = player.roster.take(TEAM_SIZE)
        val fielded = if (scout) TeamPlanner.bestOrder(bench, TeamPlanner.scoutLead(enemy)) else bench
        val team = fielded.map { it.toBattler() }

        val result = BattleResolver(random).resolve(team, enemy)
        val won = result.outcome == Outcome.A_WINS

        // Everyone who was fielded shares in the XP (winning is worth far more).
        var updated = player
        val reward = BattleReward.xp(won, opponentLevel)
        for (creature in fielded) updated = updated.awardXp(creature.serial, reward)

        return EncounterResult(updated, result, won)
    }
}
