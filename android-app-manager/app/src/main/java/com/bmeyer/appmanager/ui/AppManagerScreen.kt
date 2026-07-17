package com.bmeyer.appmanager.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bmeyer.appmanager.data.AdvancedFilter
import com.bmeyer.appmanager.data.AppCategory
import com.bmeyer.appmanager.data.AppInfo
import com.bmeyer.appmanager.data.QuickFilter
import com.bmeyer.appmanager.data.SortOption
import com.bmeyer.appmanager.data.TimeMode
import com.bmeyer.appmanager.shizuku.ShizukuManager
import com.bmeyer.appmanager.uninstall.UninstallAutoConfirmService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION") // ACTION_UNINSTALL_PACKAGE: intentional per-app confirm flow
@Composable
fun AppManagerScreen(
    viewModel: AppListViewModel,
    onRequestUsageAccess: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val nowMillis = remember { System.currentTimeMillis() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val openAppInfo: (String) -> Unit = { pkg ->
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
    val openAccessibilitySettings: () -> Unit = {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    var confirmVisible by remember { mutableStateOf(false) }
    var filtersVisible by remember { mutableStateOf(false) }
    var overflowOpen by remember { mutableStateOf(false) }
    var shizukuMode by remember { mutableStateOf(ShizukuManager.Availability.UNAVAILABLE) }
    var autoConfirmEnabled by remember { mutableStateOf(false) }

    // Non-null while a silent Shizuku batch is running: (done, total).
    var silentProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Sequential uninstall queue (manual or accessibility auto-confirm).
    var queue by remember { mutableStateOf<List<String>>(emptyList()) }
    var queueIndex by remember { mutableIntStateOf(0) }
    var autoRunning by remember { mutableStateOf(false) }

    val uninstallLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val pkg = queue.getOrNull(queueIndex)
        if (result.resultCode == Activity.RESULT_OK && pkg != null) {
            viewModel.onUninstalled(pkg)
        }
        queueIndex += 1
    }

    LaunchedEffect(queue, queueIndex) {
        if (queue.isEmpty()) return@LaunchedEffect
        val pkg = queue.getOrNull(queueIndex)
        if (pkg != null) {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:$pkg"))
                .putExtra(Intent.EXTRA_RETURN_RESULT, true)
            uninstallLauncher.launch(intent)
        } else {
            // Batch finished — stop auto-confirming and reset.
            UninstallAutoConfirmService.active.set(false)
            autoRunning = false
            queue = emptyList()
            queueIndex = 0
        }
    }

    fun startQueue(packages: List<String>, autoConfirm: Boolean) {
        if (packages.isEmpty()) return
        autoRunning = autoConfirm
        UninstallAutoConfirmService.active.set(autoConfirm)
        queue = packages
        queueIndex = 0
    }

    fun runSilentUninstall(packages: List<String>) {
        scope.launch {
            silentProgress = 0 to packages.size
            val results = ShizukuManager.uninstall(context, packages) { done, total ->
                silentProgress = done to total
            }
            val succeeded = results.filterValues { it.equals("Success", ignoreCase = true) }.keys
            viewModel.onUninstalledBatch(succeeded)
            viewModel.clearSelection()
            silentProgress = null
            val failed = results.size - succeeded.size
            snackbarHostState.showSnackbar(
                if (failed == 0) "Uninstalled ${succeeded.size} app${plural(succeeded.size)}"
                else "Uninstalled ${succeeded.size}, $failed failed"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BEZ App Manager") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    Box {
                        IconButton(onClick = { overflowOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(if (state.includeSystem) "Hide system apps" else "Show system apps") },
                                onClick = { overflowOpen = false; viewModel.toggleIncludeSystem() },
                            )
                            DropdownMenuItem(
                                text = { Text("Reset filters") },
                                onClick = { overflowOpen = false; viewModel.resetFilters() },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.selectedCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        shizukuMode = ShizukuManager.availability()
                        autoConfirmEnabled = UninstallAutoConfirmService.isEnabled(context)
                        confirmVisible = true
                    },
                    icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    text = { Text("Uninstall ${state.selectedCount}") },
                )
            }
        },
        bottomBar = {
            if (state.selectedCount > 0) {
                BottomAppBar {
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${state.selectedCount} selected", style = MaterialTheme.typography.bodyLarge)
                        val reclaim = state.selectedReclaimableBytes
                        if (reclaim > 0) {
                            Text(
                                "${formatSize(reclaim)} reclaimable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.clearSelection() }) { Text("Clear") }
                    Spacer(Modifier.width(8.dp))
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            if (state.allApps.isNotEmpty()) {
                DashboardCard(
                    totalApps = state.allApps.size,
                    totalBytes = state.totalBytes,
                    unusedCount = state.unusedCount,
                    unusedBytes = state.unusedReclaimableBytes,
                    hasUsageAccess = state.hasUsageAccess,
                    onReviewUnused = { viewModel.setQuickFilter(QuickFilter.UNUSED_90) },
                )
            }

            SearchBar(query = state.query, onQueryChange = viewModel::setQuery)

            FilterChipsRow(
                selected = state.quickFilter,
                usageAccess = state.hasUsageAccess,
                onPick = viewModel::setQuickFilter,
            )

            ControlsRow(
                sort = state.sort,
                category = state.category,
                advancedCount = state.advanced.activeCount,
                onPickSort = viewModel::setSort,
                onPickCategory = viewModel::setCategory,
                onOpenFilters = { filtersVisible = true },
            )

            SelectionBar(
                resultCount = state.visibleApps.size,
                totalBytes = state.visibleTotalBytes,
                allSelected = state.visibleApps.isNotEmpty() &&
                    state.visibleApps.all { it.packageName in state.selected },
                enabled = state.visibleApps.isNotEmpty(),
                onSelectAll = { viewModel.selectAllVisible() },
                onUnselectAll = { viewModel.clearSelection() },
            )

            if (!state.hasUsageAccess) {
                UsageAccessBanner(onGrant = onRequestUsageAccess)
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.visibleApps.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No apps match.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { viewModel.resetFilters() }) { Text("Clear filters") }
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.visibleApps, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            selected = app.packageName in state.selected,
                            nowMillis = nowMillis,
                            onToggle = { viewModel.toggleSelection(app.packageName) },
                            onInfo = { openAppInfo(app.packageName) },
                        )
                    }
                }
            }
        }
    }

    if (filtersVisible) {
        AdvancedFilterSheet(
            current = state.advanced,
            onApply = { viewModel.setAdvancedFilter(it); filtersVisible = false },
            onDismiss = { filtersVisible = false },
        )
    }

    if (confirmVisible) {
        UninstallConfirmDialog(
            count = state.selectedCount,
            shizukuReady = shizukuMode == ShizukuManager.Availability.READY,
            shizukuNeedsPermission = shizukuMode == ShizukuManager.Availability.NEEDS_PERMISSION,
            autoConfirmEnabled = autoConfirmEnabled,
            onEnableAuto = { confirmVisible = false; openAccessibilitySettings() },
            onEnableShizuku = {
                scope.launch {
                    val granted = ShizukuManager.ensurePermission()
                    shizukuMode = if (granted) ShizukuManager.Availability.READY
                    else ShizukuManager.availability()
                }
            },
            onConfirm = {
                confirmVisible = false
                val selection = state.selected.toList()
                when {
                    shizukuMode == ShizukuManager.Availability.READY -> runSilentUninstall(selection)
                    autoConfirmEnabled -> startQueue(selection, autoConfirm = true)
                    else -> startQueue(selection, autoConfirm = false)
                }
            },
            onDismiss = { confirmVisible = false },
        )
    }

    if (autoRunning && queue.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Uninstalling hands-free") },
            text = {
                Column {
                    Text("${queueIndex.coerceAtMost(queue.size)} / ${queue.size} — auto-confirming each. Don't touch the screen.")
                    Spacer(Modifier.size(8.dp))
                    val fraction = if (queue.isEmpty()) 0f else queueIndex.toFloat() / queue.size
                    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {},
        )
    }

    silentProgress?.let { (done, total) ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Uninstalling silently") },
            text = {
                Column {
                    Text("$done / $total")
                    Spacer(Modifier.size(8.dp))
                    val fraction = if (total == 0) 0f else done.toFloat() / total
                    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun UninstallConfirmDialog(
    count: Int,
    shizukuReady: Boolean,
    shizukuNeedsPermission: Boolean,
    autoConfirmEnabled: Boolean,
    onEnableAuto: () -> Unit,
    onEnableShizuku: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val primaryLabel = when {
        shizukuReady -> "Uninstall silently"
        autoConfirmEnabled -> "Uninstall hands-free"
        else -> "Uninstall one-by-one"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uninstall $count app${plural(count)}?") },
        text = {
            Column {
                when {
                    shizukuReady -> Text("Shizuku is active — removed silently, no taps at all.")
                    autoConfirmEnabled -> Text(
                        "Hands-free: the app taps each system confirmation for you. Keep the " +
                            "screen on and don't touch it until it finishes."
                    )
                    else -> {
                        Text(
                            "Android confirms each removal individually. Turn on hands-free " +
                                "auto-confirm so you only tap once and walk away:"
                        )
                        TextButton(onClick = onEnableAuto) { Text("Enable hands-free (recommended)") }
                        if (shizukuNeedsPermission) {
                            Text(
                                "Shizuku is running — grant permission for fully silent removal:",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            TextButton(onClick = onEnableShizuku) { Text("Enable silent (Shizuku)") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(primaryLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFilterSheet(
    current: AdvancedFilter,
    onApply: (AdvancedFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf(current) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text("Advanced filters", style = MaterialTheme.typography.titleLarge)
            Text(
                "Combine any criteria — every one you set must match.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(14.dp))

            TimeModeRow(
                label = "Last used",
                mode = draft.lastUsedMode,
                days = draft.lastUsedDays,
                allowNever = true,
                onMode = { draft = draft.copy(lastUsedMode = it) },
                onDays = { draft = draft.copy(lastUsedDays = it) },
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            TimeModeRow(
                label = "Installed",
                mode = draft.installedMode,
                days = draft.installedDays,
                allowNever = false,
                onMode = { draft = draft.copy(installedMode = it) },
                onDays = { draft = draft.copy(installedDays = it) },
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            MinMaxRow(
                label = "Total usage time (minutes)",
                min = draft.minUsageMinutes,
                max = draft.maxUsageMinutes,
                onMin = { draft = draft.copy(minUsageMinutes = it) },
                onMax = { draft = draft.copy(maxUsageMinutes = it) },
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            MinMaxRow(
                label = "Times opened (last 30 days)",
                min = draft.minOpenCount,
                max = draft.maxOpenCount,
                onMin = { draft = draft.copy(minOpenCount = it) },
                onMax = { draft = draft.copy(maxOpenCount = it) },
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            MinMaxRow(
                label = "Storage size (MB)",
                min = draft.minSizeMb,
                max = draft.maxSizeMb,
                onMin = { draft = draft.copy(minSizeMb = it) },
                onMax = { draft = draft.copy(maxSizeMb = it) },
            )
            Spacer(Modifier.size(20.dp))
            Row {
                OutlinedButton(onClick = { draft = AdvancedFilter() }, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
                Spacer(Modifier.width(12.dp))
                Button(onClick = { onApply(draft) }, modifier = Modifier.weight(1f)) { Text("Apply") }
            }
        }
    }
}

@Composable
private fun TimeModeRow(
    label: String,
    mode: TimeMode,
    days: Int,
    allowNever: Boolean,
    onMode: (TimeMode) -> Unit,
    onDays: (Int) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            var open by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { open = true }) { Text(mode.label) }
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    val modes = TimeMode.entries.filter { allowNever || it != TimeMode.NEVER }
                    modes.forEach { m ->
                        DropdownMenuItem(text = { Text(m.label) }, onClick = { open = false; onMode(m) })
                    }
                }
            }
            if (mode == TimeMode.WITHIN || mode == TimeMode.OLDER_THAN) {
                Spacer(Modifier.width(10.dp))
                NumberField(
                    value = days,
                    onChange = { onDays(it ?: 0) },
                    placeholder = "days",
                    modifier = Modifier.width(110.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("days", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MinMaxRow(
    label: String,
    min: Int?,
    max: Int?,
    onMin: (Int?) -> Unit,
    onMax: (Int?) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(value = min, onChange = onMin, placeholder = "min", modifier = Modifier.weight(1f))
            Text("to", color = MaterialTheme.colorScheme.onSurfaceVariant)
            NumberField(value = max, onChange = onMax, placeholder = "max", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NumberField(
    value: Int?,
    onChange: (Int?) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }.take(9)
            onChange(if (digits.isEmpty()) null else digits.toIntOrNull())
        },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun SelectionBar(
    resultCount: Int,
    totalBytes: Long,
    allSelected: Boolean,
    enabled: Boolean,
    onSelectAll: () -> Unit,
    onUnselectAll: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            buildString {
                append("$resultCount apps")
                if (totalBytes > 0) append(" · ${formatSize(totalBytes)}")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = { if (allSelected) onUnselectAll() else onSelectAll() },
            enabled = enabled,
        ) {
            Text(if (allSelected) "Unselect all" else "Select all")
        }
    }
}

@Composable
private fun DashboardCard(
    totalApps: Int,
    totalBytes: Long,
    unusedCount: Int,
    unusedBytes: Long,
    hasUsageAccess: Boolean,
    onReviewUnused: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("$totalApps apps installed", style = MaterialTheme.typography.titleMedium)
            if (hasUsageAccess && totalBytes > 0) {
                Text(
                    "${formatSize(totalBytes)} used by apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (hasUsageAccess && unusedCount > 0) {
                Spacer(Modifier.size(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("$unusedCount unused (90d+)", fontWeight = FontWeight.Bold)
                        Text(
                            "~${formatSize(unusedBytes)} reclaimable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = onReviewUnused) { Text("Review") }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text("Search apps") },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlsRow(
    sort: SortOption,
    category: AppCategory,
    advancedCount: Int,
    onPickSort: (SortOption) -> Unit,
    onPickCategory: (AppCategory) -> Unit,
    onOpenFilters: () -> Unit,
) {
    var sortOpen by remember { mutableStateOf(false) }
    var catOpen by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            TextButton(onClick = { sortOpen = true }) {
                Icon(Icons.Filled.Sort, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(sort.label)
            }
            DropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { sortOpen = false; onPickSort(option) },
                    )
                }
            }
        }
        Box {
            TextButton(onClick = { catOpen = true }) {
                Icon(Icons.Filled.Category, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(category.label)
            }
            DropdownMenu(expanded = catOpen, onDismissRequest = { catOpen = false }) {
                AppCategory.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { catOpen = false; onPickCategory(option) },
                    )
                }
            }
        }
        TextButton(onClick = onOpenFilters) {
            Icon(Icons.Filled.FilterList, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (advancedCount > 0) "Filters • $advancedCount" else "Filters")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selected: QuickFilter,
    usageAccess: Boolean,
    onPick: (QuickFilter) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuickFilter.entries.forEach { filter ->
            val enabled = usageAccess || !filter.requiresUsageAccess
            FilterChip(
                selected = selected == filter,
                onClick = { onPick(filter) },
                enabled = enabled,
                label = { Text(filter.label) },
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun UsageAccessBanner(onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Usage access needed", fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(4.dp))
            Text(
                "Grant \"Usage access\" to see last-used time, usage time, opens and app sizes.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.size(4.dp))
            TextButton(onClick = onGrant) { Text("Open settings") }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    selected: Boolean,
    nowMillis: Long,
    onToggle: () -> Unit,
    onInfo: () -> Unit,
) {
    val icon = rememberAppIcon(app.packageName)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(4.dp))
        if (icon != null) {
            androidx.compose.foundation.Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        } else {
            Box(Modifier.size(40.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                app.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                buildString {
                    append(formatSize(app.sizeBytes))
                    append(" · used ")
                    append(formatLastUsed(app.lastUsedMillis, nowMillis))
                    append(" · ")
                    append(formatDuration(app.usageMillis))
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                buildString {
                    append("${app.openCount} open${plural(app.openCount)}")
                    append(" · updated ")
                    append(formatLastUsed(app.lastUpdateMillis, nowMillis))
                    if (app.isSystemApp) append(" · system")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onInfo) {
            Icon(
                Icons.Filled.Info,
                contentDescription = "App info",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Loads an app's launcher icon off the main thread and caches it per package. */
@Composable
private fun rememberAppIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager.getApplicationIcon(packageName)
                    .toBitmap(width = 96, height = 96)
                    .asImageBitmap()
            }.getOrNull()
        }
    }.value
}

private fun plural(count: Int): String = if (count == 1) "" else "s"
