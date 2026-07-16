package com.bmeyer.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bmeyer.appmanager.data.AppInfo
import com.bmeyer.appmanager.data.AppRepository
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
    val includeSystem: Boolean = false,
    val selected: Set<String> = emptySet(),
) {
    /** The list actually shown: filtered by [query] then ordered by [sort]. */
    val visibleApps: List<AppInfo> by lazy {
        val q = query.trim().lowercase()
        allApps
            .asSequence()
            .filter { q.isEmpty() || it.label.lowercase().contains(q) || it.packageName.contains(q) }
            .sortedWith(sort.comparator())
            .toList()
    }

    val selectedCount: Int get() = selected.size
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
    private val _state = MutableStateFlow(UiState())
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
            val apps = repo.loadApps(
                includeSystem = _state.value.includeSystem,
                hasUsageAccess = hasAccess,
                nowMillis = System.currentTimeMillis(),
            )
            // Drop any selections that no longer exist after a reload.
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

    fun setSort(sort: SortOption) = _state.update { it.copy(sort = sort) }

    fun toggleIncludeSystem() {
        _state.update { it.copy(includeSystem = !it.includeSystem) }
        refresh()
    }

    fun toggleSelection(packageName: String) = _state.update { s ->
        val next = s.selected.toMutableSet()
        if (!next.add(packageName)) next.remove(packageName)
        s.copy(selected = next)
    }

    /** Select every app currently visible (respecting the active filter). */
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
}
