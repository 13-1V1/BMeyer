package com.bmeyer.assetgen.data

/**
 * Art-style presets. Each contributes a positive-prompt fragment plus a few
 * negative terms, and a recommended step count (on-device diffusion trades
 * steps for speed). Pure data — unit-testable, no Android.
 */
enum class StylePreset(
    val label: String,
    /** Positive style cue injected into the prompt. */
    val positive: String,
    /** Style-specific things to steer away from. */
    val negative: String,
    /** Suggested diffusion steps for a good speed/quality balance on-device. */
    val steps: Int,
) {
    PIXEL_ART(
        label = "Pixel Art",
        positive = "16-bit pixel art, crisp pixels, limited palette, dithering, retro game sprite",
        negative = "blurry, smooth gradients, anti-aliased, photorealistic, 3d render",
        steps = 20,
    ),
    LOW_POLY(
        label = "Low Poly",
        positive = "low-poly 3d render, flat-shaded faceted geometry, simple gradients, isometric game asset",
        negative = "high detail, realistic textures, noise, pixel art",
        steps = 22,
    ),
    HAND_DRAWN(
        label = "Hand-Drawn",
        positive = "hand-drawn illustration, clean ink outlines, cel shaded, storybook game art",
        negative = "photorealistic, 3d render, noisy, jpeg artifacts",
        steps = 24,
    ),
    PAINTERLY(
        label = "Painterly",
        positive = "digital painting, soft brush strokes, rich lighting, concept art, fantasy game illustration",
        negative = "flat colors, pixelated, hard edges, lineart",
        steps = 26,
    ),
    FLAT_ICON(
        label = "Flat Icon",
        positive = "flat vector icon, bold shapes, minimal shading, high contrast, mobile game UI icon",
        negative = "photorealistic, gradient mesh, texture, noise, 3d",
        steps = 18,
    );

    companion object {
        val DEFAULT = PIXEL_ART
    }
}
