package com.bmeyer.appmanager.data

/** Sort dimensions exposed in the UI. Each carries a human label for the menu. */
enum class SortOption(val label: String) {
    LEAST_RECENTLY_USED("Least recently used"),
    MOST_RECENTLY_USED("Most recently used"),
    LEAST_USED_TIME("Least usage time"),
    MOST_USED_TIME("Most usage time"),
    LARGEST("Largest size"),
    SMALLEST("Smallest size"),
    NAME_ASC("Name (A–Z)"),
    NEWEST_INSTALL("Newest install"),
    OLDEST_INSTALL("Oldest install"),
}
