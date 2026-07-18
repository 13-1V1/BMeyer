package com.bmeyer.appmanager.data

import android.content.Context

/** Small wrapper persisting the user's view preferences across launches. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("app_manager_prefs", Context.MODE_PRIVATE)

    var sort: SortOption
        get() = read("sort", SortOption.LARGEST) { SortOption.valueOf(it) }
        set(value) = sp.edit().putString("sort", value.name).apply()

    var includeSystem: Boolean
        get() = sp.getBoolean("include_system", false)
        set(value) = sp.edit().putBoolean("include_system", value).apply()

    var quickFilter: QuickFilter
        get() = read("filter", QuickFilter.ALL) { QuickFilter.valueOf(it) }
        set(value) = sp.edit().putString("filter", value.name).apply()

    var category: AppCategory
        get() = read("category", AppCategory.ANY) { AppCategory.valueOf(it) }
        set(value) = sp.edit().putString("category", value.name).apply()

    var advanced: AdvancedFilter
        get() = AdvancedFilter.decode(sp.getString("advanced", "") ?: "")
        set(value) = sp.edit().putString("advanced", AdvancedFilter.encode(value)).apply()

    private inline fun <T> read(key: String, default: T, parse: (String) -> T): T =
        sp.getString(key, null)?.let { runCatching { parse(it) }.getOrNull() } ?: default
}
