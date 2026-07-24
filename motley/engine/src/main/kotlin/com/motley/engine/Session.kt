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

    fun encounter(player: Player, random: Random, opponentLevel: Int): EncounterResult {
        require(player.roster.isNotEmpty()) { "the player needs at least one creature to battle" }

        val fielded = player.roster.take(TEAM_SIZE)
        val team = fielded.map { it.toBattler() }
        val enemy = Opponents.team(random, opponentLevel, size = TEAM_SIZE)

        val result = BattleResolver(random).resolve(team, enemy)
        val won = result.outcome == Outcome.A_WINS

        // Everyone who was fielded shares in the XP (winning is worth far more).
        var updated = player
        val reward = BattleReward.xp(won, opponentLevel)
        for (creature in fielded) updated = updated.awardXp(creature.serial, reward)

        return EncounterResult(updated, result, won)
    }
}
