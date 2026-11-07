package org.adaway.ui.update

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.noties.markwon.Markwon
import org.adaway.R
import org.adaway.model.update.ApkInstaller
import org.adaway.model.update.UpdateModel
import org.adaway.ui.compose.ExpressiveActionCard
import org.adaway.ui.compose.ExpressiveAppContainer
import org.adaway.ui.compose.ExpressiveAsymmetricShape1
import org.adaway.ui.compose.ExpressiveAsymmetricShape2
import org.adaway.ui.compose.ExpressivePage
import org.adaway.ui.compose.ExpressiveScaffold
import org.adaway.ui.compose.ExpressiveSection
import org.adaway.ui.compose.ExpressiveTopBar
import org.adaway.ui.compose.ScallopedShape
import org.adaway.ui.compose.WavyProgressIndicator

private data class UpdateScreenState(
    @param:StringRes @field:StringRes val headerRes: Int = R.string.update_up_to_date_header,
    val changelog: String = "",
    val showUpdateButton: Boolean = false,
    val showProgress: Boolean = false,
    val progress: Int? = null,
    val progressLabel: String = ""
)

@Composable
internal fun UpdateRoute(
    viewModel: UpdateViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val manifest by viewModel.appManifest.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val downloading by viewModel.downloading.collectAsStateWithLifecycle()
    val progressLabel = downloadStatus?.format(context).orEmpty()
    val state = remember(manifest, downloadStatus, downloading, progressLabel) {
        val updateAvailable = manifest?.updateAvailable == true
        val showProgress = downloading || downloadStatus != null
        UpdateScreenState(
            headerRes = if (updateAvailable) {
                R.string.update_update_available_header
            } else {
                R.string.update_up_to_date_header
            },
            changelog = manifest?.changelog.orEmpty(),
            showUpdateButton = updateAvailable && !showProgress,
            showProgress = showProgress,
            progress = downloadStatus?.progress,
            progressLabel = progressLabel
        )
    }

    UpdateScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onUpdate = viewModel::update
    )
}

@Composable
private fun UpdateScreen(
    state: UpdateScreenState,
    onNavigateBack: () -> Unit,
    onUpdate: () -> Unit
) {
    val context = LocalContext.current

    ExpressiveScaffold(
        topBar = {
            ExpressiveTopBar(
                title = stringResource(R.string.update_title),
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        ExpressivePage(
            modifier = Modifier.padding(innerPadding)
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.app_logo),
                modifier = Modifier.size(140.dp)
            )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = state.headerRes,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
            },
            label = "headerTransition"
        ) { targetHeader ->
            Text(
                text = stringResource(targetHeader),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (state.showUpdateButton) {
            Button(
                onClick = onUpdate,
                modifier = Modifier.padding(top = 24.dp),
                shape = ExpressiveAsymmetricShape1
            ) {
                Text(
                    text = stringResource(R.string.update_update_button),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (state.showProgress) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = state.progress
                if (progress == null) {
                    WavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                } else {
                    WavyProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                if (state.progressLabel.isNotEmpty()) {
                    Text(
                        text = state.progressLabel,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        ExpressiveSection(
            modifier = Modifier.padding(top = 32.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = ExpressiveAsymmetricShape1
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.update_last_changelog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )

                // The changelog is GitHub release Markdown: render it with Markwon, as the
                // View-based screen did, instead of showing raw markup characters.
                val markwon = remember(context) { Markwon.create(context) }
                val changelogColor = MaterialTheme.colorScheme.onSurface
                AndroidView(
                    factory = { ctx ->
                        TextView(ctx).apply {
                            movementMethod = LinkMovementMethod.getInstance()
                        }
                    },
                    update = { textView ->
                        textView.setTextColor(changelogColor.toArgb())
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        markwon.setMarkdown(textView, state.changelog)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Standalone update screen, kept for Android TV and legacy entry points that start an
 * Activity by class name instead of navigating through the Compose nav host.
 */
class UpdateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ExpressiveAppContainer {
                UpdateRoute(
                    viewModel = viewModel(),
                    onNavigateBack = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tryResumePendingInstall(this)
    }

    companion object {
        /**
         * Resume an install that was queued before the system "install unknown apps"
         * permission screen was opened. Safe to call from any Activity's onResume:
         * does nothing if no install is pending, the permission is still denied, or
         * the APK file is missing. Clears the pending flag whenever it acts.
         */
        @JvmStatic
        fun tryResumePendingInstall(activity: Activity): Boolean {
            if (!UpdateModel.isInstallPending(activity)) {
                return false
            }
            UpdateModel.setInstallPending(activity, false)
            if (!activity.packageManager.canRequestPackageInstalls()) {
                return false
            }
            // Verified before install: the staged APK sat on shared storage while the user was in
            // the Settings screen, so it cannot be trusted just because it is there.
            return ApkInstaller.verifyAndInstall(activity, ApkInstaller.getStagedApk(activity))
        }
    }
}
