package com.bmeyer.assetgen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmeyer.assetgen.ui.AssetGenScreen
import com.bmeyer.assetgen.ui.AssetGenViewModel

/**
 * Single-activity Compose app. The whole UI lives in [AssetGenScreen] and all
 * state flows from [AssetGenViewModel] — same one-directional pattern the
 * sibling app-manager uses.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                val vm: AssetGenViewModel = viewModel(
                    factory = AssetGenViewModel.factory(applicationContext)
                )
                AssetGenScreen(vm)
            }
        }
    }
}
