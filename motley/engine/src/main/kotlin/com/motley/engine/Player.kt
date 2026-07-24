package com.motley.engine

/**
 * A creature the player owns — the **signal / attachment layer** from the design (D2). It is not an
 * anonymous stat block: it has a permanent [serial] (no one else has this one), a player-chosen
 * [nickname], and it remembers the exact [recipe] it was grown from. That is what turns a generated
 * creature into *your* creature.
 */
data class OwnedCreature(
    val serial: Int,
    val nickname: String,
    val creature: Creature,
    val progress: CreatureProgress = CreatureProgress(),
) {
    init {
        require(serial >= 1) { "serial starts at 1" }
        require(nickname.isNotBlank()) { "a creature needs a nickname" }
        require(';' !in nickname && '\n' !in nickname) { "nickname can't contain ';' or newlines" }
    }

    val recipe: List<String> get() = creature.recipe

    /** A battle-ready [Battler] at this creature's current level. */
    fun toBattler(): Battler = Battler(nickname, creature, progress)
}

/**
 * The player's persistent state: which essences they've collected (the finite collectible), their
 * [roster] of owned creatures, and the [nextSerial] to stamp on the next creature grown. Immutable
 * — every action returns a new [Player], which keeps state changes explicit and easy to test.
 */
data class Player(
    val essences: Set<String> = emptySet(),
    val roster: List<OwnedCreature> = emptyList(),
    val nextSerial: Int = 1,
) {
    private val factory get() = StarterEssences.factory

    /** Collect (unlock) an essence. Owning an essence lets you use it in any number of creatures. */
    fun collect(essenceId: String): Player {
        require(essenceId in StarterEssences.byId) { "no such essence: $essenceId" }
        return copy(essences = essences + essenceId)
    }

    /**
     * Grow a new creature from collected essences. Requires that every essence is owned (you can
     * only build from your collection) and the usual 2+ rule. Stamps a fresh serial.
     */
    fun grow(nickname: String, essenceIds: List<String>): Player {
        require(essenceIds.size >= CreatureFactory.MIN_ESSENCES) {
            "need at least ${CreatureFactory.MIN_ESSENCES} essences"
        }
        val missing = essenceIds.filterNot { it in essences }
        require(missing.isEmpty()) { "you haven't collected: $missing" }

        val creature = factory.grow(essenceIds.map { StarterEssences.require(it) })
        val owned = OwnedCreature(nextSerial, nickname, creature)
        return copy(roster = roster + owned, nextSerial = nextSerial + 1)
    }

    fun bySerial(serial: Int): OwnedCreature? = roster.firstOrNull { it.serial == serial }

    /** Award XP to one creature (from a battle win, training, or a catalyst), leveling it as earned. */
    fun awardXp(serial: Int, amount: Long): Player {
        val updated = roster.map {
            if (it.serial == serial) it.copy(progress = Leveling.gainXp(it.progress, amount)) else it
        }
        return copy(roster = updated)
    }

    /** Bloom a creature (prestige rebirth); throws if it isn't ready (see [Leveling.canBloom]). */
    fun bloom(serial: Int): Player {
        val updated = roster.map {
            if (it.serial == serial) it.copy(progress = Leveling.bloom(it.progress)) else it
        }
        return copy(roster = updated)
    }
}
