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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.bmeyer.appmanager.data.AppInfo
import com.bmeyer.appmanager.data.QuickFilter
import com.bmeyer.appmanager.data.SortOption
import kotlinx.coroutines.Dispatchers
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
    var sortMenuOpen by remember { mutableStateOf(false) }
    var overflowOpen by remember { mutableStateOf(false) }

    // Sequential uninstall queue: one system confirmation dialog per package.
    var queue by remember { mutableStateOf<List<String>>(emptyList()) }
    var queueIndex by remember { mutableIntStateOf(0) }

    val uninstallLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val pkg = queue.getOrNull(queueIndex)
        if (result.resultCode == Activity.RESULT_OK && pkg != null) {
            viewModel.onUninstalled(pkg)
        }
        queueIndex += 1
    }

    // Drives the queue forward each time queueIndex advances.
    androidx.compose.runtime.LaunchedEffect(queue, queueIndex) {
        if (queue.isEmpty()) return@LaunchedEffect
        val pkg = queue.getOrNull(queueIndex)
        if (pkg != null) {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:$pkg"))
                .putExtra(Intent.EXTRA_RETURN_RESULT, true)
            uninstallLauncher.launch(intent)
        } else {
            // Finished the whole batch.
            queue = emptyList()
            queueIndex = 0
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
        floatingActionButton = {
            if (state.selectedCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = { confirmVisible = true },
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

            SearchBar(query = state.query, onQueryChange = viewModel::setQuery)

            FilterChipsRow(
                selected = state.quickFilter,
                usageAccess = state.hasUsageAccess,
                onPick = viewModel::setQuickFilter,
            )

            SortRow(
                sort = state.sort,
                menuOpen = sortMenuOpen,
                onOpen = { sortMenuOpen = true },
                onDismiss = { sortMenuOpen = false },
                onPick = { sortMenuOpen = false; viewModel.setSort(it) },
                resultCount = state.visibleApps.size,
                totalBytes = state.visibleTotalBytes,
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
        val count = state.selectedCount
        AlertDialog(
            onDismissRequest = { confirmVisible = false },
            title = { Text("Uninstall $count app${if (count == 1) "" else "s"}?") },
            text = {
                Text(
                    "Android will show a confirmation for each app in turn — tap OK on each. " +
                        "You can cancel any individual one.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmVisible = false
                    // Snapshot selection into the queue and kick it off.
                    queue = state.selected.toList()
                    queueIndex = 0
                }) { Text("Start") }
            },
            dismissButton = { TextButton(onClick = { confirmVisible = false }) { Text("Cancel") } },
        )
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

@Composable
private fun SortRow(
    sort: SortOption,
    menuOpen: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onPick: (SortOption) -> Unit,
    resultCount: Int,
    totalBytes: Long,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            TextButton(onClick = onOpen) {
                Icon(Icons.Filled.Sort, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(sort.label)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = onDismiss) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(text = { Text(option.label) }, onClick = { onPick(option) })
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
