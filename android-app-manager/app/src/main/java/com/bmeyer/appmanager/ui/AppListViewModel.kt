package com.bmeyer.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bmeyer.appmanager.data.AppCategory
import com.bmeyer.appmanager.data.AppInfo
import com.bmeyer.appmanager.data.AppRepository
import com.bmeyer.appmanager.data.Prefs
import com.bmeyer.appmanager.data.QuickFilter
import com.bmeyer.appmanager.data.SortOption
import com.bmeyer.appmanager.data.UsageAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the screen renders, in one immutable snapshot. */
data class UiState(
    val loading: Boolean = true,
    val hasUsageAccess: Boolean = false,
    val allApps: List<AppInfo> = emptyList(),
    val query: String = "",
    val sort: SortOption = SortOption.LARGEST,
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val category: AppCategory = AppCategory.ANY,
    val includeSystem: Boolean = false,
    val selected: Set<String> = emptySet(),
) {
    /** Filtered (search + quick filter + category) then ordered by [sort]. */
    val visibleApps: List<AppInfo> by lazy {
        val now = System.currentTimeMillis()
        val q = query.trim().lowercase()
        allApps
            .asSequence()
            .filter { q.isEmpty() || it.label.lowercase().contains(q) || it.packageName.contains(q) }
            .filter { quickFilter.matches(it, now) }
            .filter { category.matches(it) }
            .sortedWith(sort.comparator())
            .toList()
    }

    val selectedCount: Int get() = selected.size

    /** Combined size of the current selection (unknown sizes count as 0). */
    val selectedReclaimableBytes: Long by lazy {
        allApps.filter { it.packageName in selected && it.sizeBytes > 0 }.sumOf { it.sizeBytes }
    }

    /** Combined size of everything currently shown. */
    val visibleTotalBytes: Long by lazy {
        visibleApps.filter { it.sizeBytes > 0 }.sumOf { it.sizeBytes }
    }

    // ---- Dashboard stats (computed over the whole inventory) ----

    /** Combined size of every app in the inventory. */
    val totalBytes: Long by lazy {
        allApps.filter { it.sizeBytes > 0 }.sumOf { it.sizeBytes }
    }

    /** Apps not opened in 90+ days (or never), the primary cleanup target. */
    private val unusedApps: List<AppInfo> by lazy {
        val now = System.currentTimeMillis()
        allApps.filter { QuickFilter.UNUSED_90.matches(it, now) }
    }

    val unusedCount: Int get() = unusedApps.size

    /** Estimated storage reclaimable by removing all unused apps. */
    val unusedReclaimableBytes: Long by lazy {
        unusedApps.filter { it.sizeBytes > 0 }.sumOf { it.sizeBytes }
    }
}

private fun SortOption.comparator(): Comparator<AppInfo> = when (this) {
    SortOption.LEAST_RECENTLY_USED -> compareBy { it.lastUsedMillis }
    SortOption.MOST_RECENTLY_USED -> compareByDescending { it.lastUsedMillis }
    SortOption.LEAST_USED_TIME -> compareBy { it.usageMillis }
    SortOption.MOST_USED_TIME -> compareByDescending { it.usageMillis }
    SortOption.LARGEST -> compareByDescending { it.sizeBytes }
    SortOption.SMALLEST -> compareBy { it.sizeBytes }
    SortOption.NAME_ASC -> compareBy { it.label.lowercase() }
    SortOption.NEWEST_INSTALL -> compareByDescending { it.firstInstallMillis }
    SortOption.OLDEST_INSTALL -> compareBy { it.firstInstallMillis }
}

class AppListViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app)
    private val prefs = Prefs(app)

    private val _state = MutableStateFlow(
        UiState(
            sort = prefs.sort,
            quickFilter = prefs.quickFilter,
            includeSystem = prefs.includeSystem,
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Reloads the inventory, re-checking usage access each time. */
    fun refresh() {
        val ctx = getApplication<Application>()
        val hasAccess = UsageAccess.isGranted(ctx)
        _state.update { it.copy(loading = true, hasUsageAccess = hasAccess) }
        viewModelScope.launch {
            // Never let a load failure crash the app; surface an empty list instead.
            val apps = runCatching {
                repo.loadApps(
                    includeSystem = _state.value.includeSystem,
                    hasUsageAccess = hasAccess,
                    nowMillis = System.currentTimeMillis(),
                )
            }.getOrDefault(emptyList())
            val stillPresent = apps.mapTo(HashSet()) { it.packageName }
            _state.update {
                it.copy(
                    loading = false,
                    allApps = apps,
                    selected = it.selected.filterTo(HashSet()) { pkg -> pkg in stillPresent },
                )
            }
        }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun setSort(sort: SortOption) {
        prefs.sort = sort
        _state.update { it.copy(sort = sort) }
    }

    fun setQuickFilter(filter: QuickFilter) {
        prefs.quickFilter = filter
        _state.update { it.copy(quickFilter = filter) }
    }

    fun setCategory(category: AppCategory) = _state.update { it.copy(category = category) }

    fun toggleIncludeSystem() {
        val next = !_state.value.includeSystem
        prefs.includeSystem = next
        _state.update { it.copy(includeSystem = next) }
        refresh()
    }

    fun toggleSelection(packageName: String) = _state.update { s ->
        val next = s.selected.toMutableSet()
        if (!next.add(packageName)) next.remove(packageName)
        s.copy(selected = next)
    }

    /** Select every app currently visible (respecting search + quick filter). */
    fun selectAllVisible() = _state.update { s ->
        s.copy(selected = s.visibleApps.mapTo(HashSet()) { it.packageName })
    }

    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    /** Package removed from the device — drop it from the list and selection. */
    fun onUninstalled(packageName: String) = _state.update { s ->
        s.copy(
            allApps = s.allApps.filterNot { it.packageName == packageName },
            selected = s.selected - packageName,
        )
    }

    /** Several packages removed at once (silent Shizuku batch). */
    fun onUninstalledBatch(packageNames: Collection<String>) = _state.update { s ->
        val removed = packageNames.toHashSet()
        s.copy(
            allApps = s.allApps.filterNot { it.packageName in removed },
            selected = s.selected - removed,
        )
    }
}
