package com.bmeyer.assetgen.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bmeyer.assetgen.data.AssetType
import com.bmeyer.assetgen.data.PromptComposer
import com.bmeyer.assetgen.data.StylePreset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetGenScreen(vm: AssetGenViewModel) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.dismissMessage()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) vm.importModel(uri) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sprite Forge") },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = { Text(state.engineLabel, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PromptField(state.prompt, enabled = !state.isGenerating, onChange = vm::setPrompt)

            SectionLabel("Style")
            ChipRow(
                options = StylePreset.entries,
                selected = state.style,
                label = { it.label },
                enabled = !state.isGenerating,
                onSelect = vm::setStyle,
            )

            SectionLabel("Asset type")
            ChipRow(
                options = AssetType.entries,
                selected = state.type,
                label = { it.label },
                enabled = !state.isGenerating,
                onSelect = vm::setType,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Switch(
                    checked = state.removeBackground && state.backgroundToggleEnabled,
                    onCheckedChange = { vm.toggleRemoveBackground() },
                    enabled = state.backgroundToggleEnabled && !state.isGenerating,
                )
                Text(
                    "Transparent background",
                    color = if (state.backgroundToggleEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            StepsSlider(
                steps = state.steps,
                enabled = !state.isGenerating,
                onChange = vm::setSteps,
            )

            GenerateButton(state, onClick = vm::generate)

            ResultPreview(state.result)

            if (state.result != null) {
                OutlinedButton(
                    onClick = vm::saveCurrent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Save PNG to gallery")
                }
            }

            ModelSection(
                installed = state.modelInstalled,
                modelUrl = state.modelUrl,
                isDownloading = state.isDownloading,
                downloadFraction = state.downloadFraction,
                downloadedBytes = state.downloadedBytes,
                totalBytes = state.totalBytes,
                onUrlChange = vm::setModelUrl,
                onDownload = vm::downloadModel,
                onCancel = vm::cancelDownload,
                onImport = { importLauncher.launch("*/*") },
            )

            if (state.gallery.isNotEmpty()) {
                SectionLabel("Session gallery")
                GalleryGrid(state.gallery)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun StepsSlider(steps: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel("Detail steps")
            Text("$steps", style = MaterialTheme.typography.titleSmall)
        }
        Slider(
            value = steps.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = PromptComposer.MIN_STEPS.toFloat()..PromptComposer.MAX_STEPS.toFloat(),
            enabled = enabled,
        )
        Text(
            "More steps = higher quality, slower generation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PromptField(value: String, enabled: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        label = { Text("Describe the asset") },
        placeholder = { Text("e.g. fire knight holding a shield") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    enabled: Boolean,
    onSelect: (T) -> Unit,
) {
    // Horizontally scrollable so presets never overflow narrow screens.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                enabled = enabled,
                label = { Text(label(option)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
private fun GenerateButton(state: UiState, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = state.canGenerate,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (state.isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text("  Generating… ${(state.progress * 100).toInt()}%")
        } else {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("  Generate")
        }
    }
    if (state.isGenerating) {
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}

@Composable
private fun ResultPreview(bitmap: Bitmap?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .checkerboard(),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Generated asset",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                )
            } else {
                Text(
                    "Your asset will appear here",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ModelSection(
    installed: Boolean,
    modelUrl: String,
    isDownloading: Boolean,
    downloadFraction: Float?,
    downloadedBytes: Long,
    totalBytes: Long,
    onUrlChange: (String) -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onImport: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("On-device engine", style = MaterialTheme.typography.titleSmall)
            Text(
                if (installed)
                    "Stable Diffusion model installed — generations run fully offline."
                else
                    "No model yet. Generations use the built-in preview. Add a converted " +
                        "SD 1.5 model bundle (.zip) — download from a link or import a local file — " +
                        "to generate real images on-device. Bundles are large (~1–2 GB).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = modelUrl,
                onValueChange = onUrlChange,
                enabled = !isDownloading,
                singleLine = true,
                label = { Text("Model bundle URL (.zip)") },
                placeholder = { Text("https://…/sd15-mediapipe.zip") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (isDownloading) {
                if (downloadFraction != null) {
                    LinearProgressIndicator(
                        progress = { downloadFraction },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Downloading… ${(downloadFraction * 100).toInt()}%  " +
                            "(${mb(downloadedBytes)} / ${mb(totalBytes)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Downloading… ${mb(downloadedBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel download")
                }
            } else {
                Button(
                    onClick = onDownload,
                    enabled = modelUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(if (installed) "  Download & replace model" else "  Download model")
                }
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                    Text(if (installed) "Import local .zip instead" else "Import local .zip instead")
                }
            }
        }
    }
}

/** Bytes -> compact "123 MB" string (or "?" when size is unknown). */
private fun mb(bytes: Long): String =
    if (bytes < 0) "?" else "${(bytes / 1_000_000.0).toInt()} MB"

@Composable
private fun GalleryGrid(entries: List<GalleryEntry>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(((entries.size + 2) / 3 * 120).dp.coerceAtMost(360.dp)),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(entries) { entry ->
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.aspectRatio(1f),
            ) {
                Box(modifier = Modifier.fillMaxSize().checkerboard()) {
                    Image(
                        bitmap = entry.bitmap.asImageBitmap(),
                        contentDescription = entry.prompt.ifEmpty { "asset" },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                    )
                }
            }
        }
    }
}

/** Subtle checkerboard so transparent pixels read as transparent. */
private fun Modifier.checkerboard(): Modifier = this.then(
    Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0xFFE8E8E8))
)
