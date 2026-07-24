package com.motley.engine

enum class Rarity { COMMON, UNCOMMON, RARE, HIDDEN }

/**
 * An **essence** is a collectible concept-word — the single unit that ties Motley together. Each
 * authored essence carries everything a creature needs:
 *
 *  - [weights]     — the *shape* of its stat contribution (balance).
 *  - [element]     — its pull on the type triangle (see [deriveType][CreatureFactory]).
 *  - [ability]     — an optional signature-move / passive seed (kit).
 *  - [artFragment] — a descriptive fragment fed to image generation (the look).
 *  - [rarity]      — progression / how hard it is to collect.
 *
 * Setting [hidden] marks a rare **Wild** essence: creatures grown with one become [Type.WILD]
 * glass cannons that sit outside the triangle.
 *
 * Because players combine from this *curated* set rather than typing free text, the vocabulary is
 * also the moderation layer — the abuse surface of open-prompt generation largely disappears.
 */
data class Essence(
    val id: String,
    val displayName: String,
    val element: Element,
    val weights: StatWeights,
    val artFragment: String,
    val rarity: Rarity = Rarity.COMMON,
    val ability: String? = null,
    val hidden: Boolean = false,
) {
    init {
        require(id.isNotBlank()) { "essence id must not be blank" }
        require(!hidden || rarity == Rarity.HIDDEN) { "hidden essences must have HIDDEN rarity" }
    }
}
