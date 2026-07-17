package com.bmeyer.appmanager.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bmeyer.appmanager.data.AppCategory
import com.bmeyer.appmanager.data.AppInfo
import com.bmeyer.appmanager.data.QuickFilter
import com.bmeyer.appmanager.data.SortOption
import com.bmeyer.appmanager.shizuku.ShizukuManager
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

    // Opens the system "App info" page so the user can force-stop, clear cache,
    // or manage permissions — things a normal app can't do for others itself.
    val openAppInfo: (String) -> Unit = { pkg ->
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    var confirmVisible by remember { mutableStateOf(false) }
    var shizukuMode by remember { mutableStateOf(ShizukuManager.Availability.UNAVAILABLE) }
    var overflowOpen by remember { mutableStateOf(false) }

    // Non-null while a silent Shizuku batch is running: (done, total).
    var silentProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Sequential uninstall queue (fallback): one system confirmation per package.
    var queue by remember { mutableStateOf<List<String>>(emptyList()) }
    var queueIndex by remember { mutableIntStateOf(0) }

    val uninstallLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val pkg = queue.getOrNull(queueIndex)
        if (result.resultCode == Activity.RESULT_OK && pkg != null) {
            viewModel.onUninstalled(pkg)
        }
        queueIndex += 1
    }

    // Drives the fallback queue forward each time queueIndex advances.
    LaunchedEffect(queue, queueIndex) {
        if (queue.isEmpty()) return@LaunchedEffect
        val pkg = queue.getOrNull(queueIndex)
        if (pkg != null) {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:$pkg"))
                .putExtra(Intent.EXTRA_RETURN_RESULT, true)
            uninstallLauncher.launch(intent)
        } else {
            queue = emptyList()
            queueIndex = 0
        }
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
                title = { Text("App Manager") },
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
                                text = { Text("Select all shown") },
                                onClick = { overflowOpen = false; viewModel.selectAllVisible() },
                            )
                            DropdownMenuItem(
                                text = { Text("Clear selection") },
                                onClick = { overflowOpen = false; viewModel.clearSelection() },
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
                        Text(
                            "${state.selectedCount} selected",
                            style = MaterialTheme.typography.bodyLarge,
                        )
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
                resultCount = state.visibleApps.size,
                totalBytes = state.visibleTotalBytes,
                onPickSort = viewModel::setSort,
                onPickCategory = viewModel::setCategory,
            )

            if (!state.hasUsageAccess) {
                UsageAccessBanner(onGrant = onRequestUsageAccess)
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.visibleApps.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No apps match.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    if (confirmVisible) {
        UninstallConfirmDialog(
            count = state.selectedCount,
            mode = shizukuMode,
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
                if (shizukuMode == ShizukuManager.Availability.READY) {
                    runSilentUninstall(selection)
                } else {
                    queue = selection
                    queueIndex = 0
                }
            },
            onDismiss = { confirmVisible = false },
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
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun UninstallConfirmDialog(
    count: Int,
    mode: ShizukuManager.Availability,
    onEnableShizuku: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val silent = mode == ShizukuManager.Availability.READY
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uninstall $count app${plural(count)}?") },
        text = {
            Column {
                if (silent) {
                    Text("Shizuku is active — these will be removed silently, no per-app taps.")
                } else {
                    Text(
                        "Android will show a confirmation for each app in turn — tap OK on each. " +
                            "You can cancel any individual one."
                    )
                    if (mode == ShizukuManager.Availability.NEEDS_PERMISSION) {
                        Spacer(Modifier.size(8.dp))
                        Text(
                            "Shizuku is running. Grant permission to remove them all silently instead:",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        TextButton(onClick = onEnableShizuku) { Text("Enable silent mode") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(if (silent) "Uninstall silently" else "Start") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
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
    resultCount: Int,
    totalBytes: Long,
    onPickSort: (SortOption) -> Unit,
    onPickCategory: (AppCategory) -> Unit,
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
        Spacer(Modifier.width(4.dp))
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
        Spacer(Modifier.width(8.dp))
        Text(
            buildString {
                append("$resultCount apps")
                if (totalBytes > 0) append(" · ${formatSize(totalBytes)}")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            // Usage/size-based presets are meaningless without the grant.
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
                "Grant \"Usage access\" to see last-used time, usage time and app sizes.",
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
