package com.bmeyer.assetgen.data

/**
 * What the user is generating. Drives the default canvas size, the prompt
 * scaffolding, and whether background removal / tiling makes sense.
 *
 * Pure data + constants only — unit-testable on the JVM with no Android calls.
 */
enum class AssetType(
    val label: String,
    /** Square edge length in pixels the generator targets for this type. */
    val defaultSize: Int,
    /** Prompt fragment appended to describe the shot/framing. */
    val framing: String,
    /** Whether a transparent cut-out is the usual want for this type. */
    val wantsTransparency: Boolean,
    /** Whether the result should tile seamlessly (affects prompt + hints). */
    val wantsSeamless: Boolean,
) {
    CHARACTER(
        label = "Character",
        defaultSize = 512,
        framing = "full-body character, centered, single subject, clean silhouette",
        wantsTransparency = true,
        wantsSeamless = false,
    ),
    ITEM(
        label = "Item / Icon",
        defaultSize = 256,
        framing = "single game item, centered, icon composition, no scene",
        wantsTransparency = true,
        wantsSeamless = false,
    ),
    TILE(
        label = "Tile / Texture",
        defaultSize = 256,
        framing = "seamless repeating texture, top-down, evenly lit, no focal point",
        wantsTransparency = false,
        wantsSeamless = true,
    ),
    BACKGROUND(
        label = "Background",
        defaultSize = 512,
        framing = "wide environment background, atmospheric depth, no characters",
        wantsTransparency = false,
        wantsSeamless = false,
    );

    companion object {
        val DEFAULT = CHARACTER
    }
}
