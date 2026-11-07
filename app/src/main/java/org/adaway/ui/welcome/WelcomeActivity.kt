package org.adaway.ui.welcome

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.adaway.R
import org.adaway.ui.home.HomeViewModel

private enum class WelcomeStep {
    METHOD,
    SYNC,
    BATTERY,
    OEM_BATTERY
}

@Composable
internal fun WelcomeRoute(
    homeViewModel: HomeViewModel,
    onFinish: () -> Unit
) {
    val steps = remember { WelcomeStep.entries }
    val pagerState = rememberPagerState(initialPage = 0) { steps.size }
    val coroutineScope = rememberCoroutineScope()
    // The two battery guidance steps are informational only and never block setup.
    val canProceed = remember { mutableStateListOf(false, false, true, true) }

    val currentPage = pagerState.currentPage
    val currentStep = steps[currentPage]
    val showBackButton = currentPage > 0
    // Syncing keeps running in the background regardless of which page is on screen, so the
    // sync step keeps "Next" visible and asks for confirmation instead of trapping the user.
    val showNextButton = canProceed.getOrElse(currentPage) { false } ||
        currentStep == WelcomeStep.SYNC
    var showLeaveEarlyDialog by remember { mutableStateOf(false) }
    val nextButtonTextRes = if (currentPage == steps.lastIndex) {
        R.string.welcome_finish_button
    } else {
        R.string.welcome_next_button
    }

    val advance: () -> Unit = {
        if (currentPage == steps.lastIndex) {
            onFinish()
        } else {
            coroutineScope.launch {
                pagerState.animateScrollToPage(currentPage + 1)
            }
        }
    }

    BackHandler(enabled = showBackButton) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(currentPage - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            userScrollEnabled = false
        ) { page ->
            when (steps[page]) {
                WelcomeStep.METHOD -> WelcomeMethodStep(
                    onCanProceedChange = { canProceed[WelcomeStep.METHOD.ordinal] = it }
                )

                WelcomeStep.SYNC -> WelcomeSyncStep(
                    onCanProceedChange = { canProceed[WelcomeStep.SYNC.ordinal] = it },
                    homeViewModel = homeViewModel
                )

                WelcomeStep.BATTERY -> WelcomeBatteryStep(
                    onCanProceedChange = { canProceed[WelcomeStep.BATTERY.ordinal] = it }
                )

                WelcomeStep.OEM_BATTERY -> WelcomeOemBatteryStep(
                    onCanProceedChange = { canProceed[WelcomeStep.OEM_BATTERY.ordinal] = it }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.width(100.dp)) {
                if (showBackButton) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage - 1)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.welcome_back_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(steps.size) { index ->
                    val selected = index == currentPage
                    val dotColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        },
                        label = "dotColor"
                    )
                    val dotWidth by animateDpAsState(
                        targetValue = if (selected) 24.dp else 8.dp,
                        label = "dotWidth"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = dotWidth, height = 8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .semantics {
                                contentDescription = "Step ${index + 1}"
                            }
                    )
                }
            }

            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.CenterEnd) {
                if (showNextButton) {
                    TextButton(
                        onClick = {
                            if (
                                currentStep == WelcomeStep.SYNC &&
                                !canProceed[WelcomeStep.SYNC.ordinal]
                            ) {
                                showLeaveEarlyDialog = true
                            } else {
                                advance()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(nextButtonTextRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showLeaveEarlyDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveEarlyDialog = false },
            title = { Text(stringResource(R.string.welcome_sync_leave_early_title)) },
            text = { Text(stringResource(R.string.welcome_sync_leave_early_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveEarlyDialog = false
                        advance()
                    }
                ) {
                    Text(stringResource(R.string.welcome_sync_leave_early_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveEarlyDialog = false }) {
                    Text(stringResource(R.string.welcome_sync_leave_early_wait))
                }
            }
        )
    }
}
