package com.bmeyer.appmanager

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import com.bmeyer.appmanager.ui.AppListViewModel
import com.bmeyer.appmanager.ui.AppManagerScreen
import com.bmeyer.appmanager.ui.AppManagerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppManagerTheme {
                // Returning from the Usage-access settings screen re-checks the
                // grant and reloads sizes/usage that were previously hidden.
                val settingsLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {
                    viewModel.refresh()
                }
                AppManagerScreen(
                    viewModel = viewModel,
                    onRequestUsageAccess = {
                        settingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                )
            }
        }
    }
}
