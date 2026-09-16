package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FastingSession
import com.example.ui.FastingViewModel
import com.example.ui.components.CircularTimerGauge
import com.example.ui.components.TargetEndTimeCard

@Composable
fun ActiveFastingScreen(
    session: FastingSession,
    nowMillis: Long,
    viewModel: FastingViewModel,
    onEndFastClick: () -> Unit,
    onCompleteFastClick: () -> Unit,
    onAdjustStartTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val elapsedMillis = session.elapsedMillis(nowMillis)
    val remainingMillis = session.remainingMillis(nowMillis)
    val isCompleted = session.isCompleted(nowMillis)
    val progressFraction = session.progressFraction(nowMillis)
    val currentStage = session.currentFastingStage(nowMillis)

    val countdownDisplay = if (isCompleted) {
        val overtimeMillis = nowMillis - session.targetEndTimeMillis
        "+" + viewModel.formatCountdown(overtimeMillis)
    } else {
        viewModel.formatCountdown(remainingMillis)
    }

    val elapsedDisplay = viewModel.formatDurationShort(elapsedMillis)
    val formattedTarget = viewModel.formatTargetTime(session.targetEndTimeMillis)
    val targetEatTimeOnly = viewModel.formatTargetEatTimeOnly(session.targetEndTimeMillis)
    val eatingWindowEndMillis = session.targetEndTimeMillis + (session.protocol.eatHours * 3600 * 1000L)
    val eatingWindowEndOnly = viewModel.formatTimeOnly(eatingWindowEndMillis)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Gauge
        CircularTimerGauge(
            progress = progressFraction,
            countdownText = countdownDisplay,
            elapsedText = elapsedDisplay,
            isCompleted = isCompleted,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        // Summary Quick Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                label = "Started",
                value = viewModel.formatTimeOnly(session.startTimeMillis),
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "Elapsed",
                value = elapsedDisplay,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                label = "Goal",
                value = "${session.protocol.fastHours}h (${session.protocol.label})",
                modifier = Modifier.weight(1f)
            )
        }

        // Target End Time Card (Requirement 4)
        TargetEndTimeCard(
            formattedTargetDateTime = formattedTarget,
            eatTimeOnly = targetEatTimeOnly,
            eatingWindowEnd = eatingWindowEndOnly,
            eatingWindowHours = session.protocol.eatHours,
            isCompleted = isCompleted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Biological Stage / Fasting Phase Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentStage.stageName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = currentStage.timeline,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentStage.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons
        if (isCompleted) {
            Button(
                onClick = onCompleteFastClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("finish_fast_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Finish & Break Fast",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEndFastClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("end_fast_early_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "End Fast Early",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                OutlinedButton(
                    onClick = onAdjustStartTimeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("adjust_start_time_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Adjust Start",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuickStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
