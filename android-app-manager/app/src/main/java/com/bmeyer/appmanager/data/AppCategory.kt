package com.bmeyer.appmanager.data

import android.content.pm.ApplicationInfo

/**
 * Wraps `ApplicationInfo.category` as a filterable enum. [ANY] disables the
 * filter; [OTHER] catches apps that declare no category (the common case, since
 * the value is developer-supplied and often left undefined).
 */
enum class AppCategory(val label: String, val value: Int) {
    ANY("All categories", Int.MIN_VALUE),
    GAME("Games", ApplicationInfo.CATEGORY_GAME),
    AUDIO("Audio", ApplicationInfo.CATEGORY_AUDIO),
    VIDEO("Video", ApplicationInfo.CATEGORY_VIDEO),
    IMAGE("Photos & images", ApplicationInfo.CATEGORY_IMAGE),
    SOCIAL("Social", ApplicationInfo.CATEGORY_SOCIAL),
    NEWS("News", ApplicationInfo.CATEGORY_NEWS),
    MAPS("Maps & navigation", ApplicationInfo.CATEGORY_MAPS),
    PRODUCTIVITY("Productivity", ApplicationInfo.CATEGORY_PRODUCTIVITY),
    OTHER("Uncategorized", ApplicationInfo.CATEGORY_UNDEFINED);

    fun matches(app: AppInfo): Boolean = when (this) {
        ANY -> true
        // Catch undefined plus any category not represented by a named entry.
        OTHER -> entries.none { it != ANY && it != OTHER && it.value == app.category }
        else -> app.category == value
    }

    companion object {
        /** The category enum for a raw ApplicationInfo.category value. */
        fun of(value: Int): AppCategory = entries.firstOrNull { it != ANY && it.value == value } ?: OTHER
    }
}
