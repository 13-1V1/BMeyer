package com.motley.engine

/**
 * Pre-battle **matchup agency** (D15). Early fights felt swingy because a random lead could walk
 * into a bad type matchup. Rather than flatten the type chart or steepen leveling (which would let
 * grinding beat smart play), we give the player a decision: **scout the foe and order your team**
 * so a favourable matchup leads. Swinginess becomes strategy — exactly the "out-think" pillar.
 */
object TeamPlanner {
    /** The type of the opponent you'll face first — what you'd scout before committing an order. */
    fun scoutLead(enemyTeam: List<Battler>): Type = enemyTeam.first().type

    /**
     * Order [team] so the creature with the best type matchup against [enemyLeadType] leads.
     * Stable otherwise, so it only changes what it needs to.
     */
    fun bestOrder(team: List<OwnedCreature>, enemyLeadType: Type): List<OwnedCreature> =
        team.sortedByDescending { TypeChart.effectiveness(it.creature.type, enemyLeadType) }
}
