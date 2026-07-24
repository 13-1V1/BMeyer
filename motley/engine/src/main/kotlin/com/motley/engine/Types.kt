package com.motley.engine

/**
 * The three elemental affinities every essence carries. Elements are intentionally *broad* so any
 * concept-word maps cleanly onto one:
 *
 *  - [EMBER] — heat, energy, aggression, electricity, machinery-that-runs-hot.
 *  - [THORN] — nature, earth, beast, growth, bone, stone.
 *  - [TIDE]  — water, ice, mist, flow, evasion, the cold and the deep.
 */
enum class Element { EMBER, THORN, TIDE }

/**
 * A creature's battle type: the three-element triangle plus the hidden [WILD] family.
 *
 * `EMBER > THORN > TIDE > EMBER` (attacker beats). [WILD] sits *outside* the triangle — it has no
 * matchup advantage or disadvantage and wins or loses on raw stats and abilities alone.
 */
enum class Type { EMBER, THORN, TIDE, WILD }
