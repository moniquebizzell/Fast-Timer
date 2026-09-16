package com.example.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.NotificationHelper
import com.example.ui.components.AdjustStartTimeDialog
import com.example.ui.components.ConfirmEndFastDialog
import com.example.ui.screens.ActiveFastingScreen
import com.example.ui.screens.IdleFastingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastTimerApp(
    viewModel: FastingViewModel = viewModel()
) {
    val context = LocalContext.current
    val session by viewModel.currentSession.collectAsStateWithLifecycle()
    val nowMillis by viewModel.nowMillis.collectAsStateWithLifecycle()
    val selectedProtocol by viewModel.selectedProtocol.collectAsStateWithLifecycle()
    val historyList by viewModel.history.collectAsStateWithLifecycle()

    val showCancelDialog by viewModel.showCancelDialog.collectAsStateWithLifecycle()
    val showAdjustDialog by viewModel.showAdjustDialog.collectAsStateWithLifecycle()

    var showStartEarlierDialog by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    // Permission launcher for Android 13+ (POST_NOTIFICATIONS)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(context)
        hasNotificationPermission = NotificationHelper.hasNotificationPermission(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "FastTimer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "FastTimer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = session.isActive,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "fast_screen_transition"
            ) { isActive ->
                if (isActive) {
                    ActiveFastingScreen(
                        session = session,
                        nowMillis = nowMillis,
                        viewModel = viewModel,
                        onEndFastClick = { viewModel.openCancelDialog() },
                        onCompleteFastClick = { viewModel.completeFast() },
                        onAdjustStartTimeClick = { viewModel.openAdjustDialog() }
                    )
                } else {
                    IdleFastingScreen(
                        selectedProtocol = selectedProtocol,
                        nowMillis = nowMillis,
                        historyList = historyList,
                        viewModel = viewModel,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onProtocolSelected = { protocol ->
                            viewModel.selectProtocol(protocol)
                        },
                        onStartFastClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.startFast()
                        },
                        onStartEarlierClick = {
                            showStartEarlierDialog = true
                        }
                    )
                }
            }

            // Dialog for ending fast early
            if (showCancelDialog) {
                ConfirmEndFastDialog(
                    onConfirm = { viewModel.endFastEarly() },
                    onDismiss = { viewModel.dismissCancelDialog() }
                )
            }

            // Dialog for adjusting start time during active fast
            if (showAdjustDialog) {
                AdjustStartTimeDialog(
                    currentStartTimeMillis = session.startTimeMillis,
                    onConfirm = { newStart -> viewModel.adjustStartTime(newStart) },
                    onDismiss = { viewModel.dismissAdjustDialog() }
                )
            }

            // Dialog for starting fast with an earlier timestamp
            if (showStartEarlierDialog) {
                AdjustStartTimeDialog(
                    currentStartTimeMillis = System.currentTimeMillis() - (15 * 60 * 1000L),
                    onConfirm = { customStart ->
                        showStartEarlierDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.startFast(customStartTime = customStart)
                    },
                    onDismiss = { showStartEarlierDialog = false }
                )
            }
        }
    }
}
