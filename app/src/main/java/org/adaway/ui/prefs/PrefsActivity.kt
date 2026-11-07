package org.adaway.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import org.adaway.ui.compose.ExpressiveAppContainer
import org.adaway.ui.compose.ExpressiveScaffold
import org.adaway.ui.compose.ExpressiveTopBar
import org.adaway.ui.prefs.exclusion.PrefsVpnExcludedAppsActivity

@Composable
internal fun PrefsRoute(
    destination: PrefsDestination,
    onNavigateBack: () -> Unit,
    onNavigate: (PrefsDestination) -> Unit,
    onOpenVpnExcludedApps: () -> Unit,
    viewModel: PrefsViewModel = viewModel()
) {
    ExpressiveScaffold(
        topBar = {
            ExpressiveTopBar(
                title = stringResource(destination.titleRes),
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrefsContent(
                destination = destination,
                viewModel = viewModel,
                onNavigate = onNavigate,
                onOpenVpnExcludedApps = onOpenVpnExcludedApps
            )
        }
    }
}

/**
 * Standalone preferences screen, kept for Android TV and legacy entry points that start an
 * Activity by class name instead of navigating through the Compose nav host.
 */
class PrefsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ExpressiveAppContainer {
                var destination by rememberSaveable { mutableStateOf(PrefsDestination.MAIN) }
                BackHandler(enabled = destination != PrefsDestination.MAIN) {
                    destination = PrefsDestination.MAIN
                }
                PrefsRoute(
                    destination = destination,
                    onNavigateBack = {
                        if (destination == PrefsDestination.MAIN) {
                            finish()
                        } else {
                            destination = PrefsDestination.MAIN
                        }
                    },
                    onNavigate = { destination = it },
                    onOpenVpnExcludedApps = {
                        startActivity(Intent(this, PrefsVpnExcludedAppsActivity::class.java))
                    }
                )
            }
        }
    }
}
