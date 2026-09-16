package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularTimerGauge(
    progress: Float,
    countdownText: String,
    elapsedText: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 270.dp,
    strokeWidth: Dp = 16.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .size(size)
            .testTag("circular_timer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            // Background full circular track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active progress arc (starts at -90 degrees, i.e. 12 o'clock)
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0.5f) {
                val gradient = Brush.sweepGradient(
                    0.0f to primaryColor.copy(alpha = 0.85f),
                    0.7f to primaryColor,
                    1.0f to secondaryColor,
                    center = Offset(this.size.width / 2f, this.size.height / 2f)
                )

                drawArc(
                    brush = gradient,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glowing indicator dot at current head of the progress
                val radius = diameter / 2f
                val centerX = this.size.width / 2f
                val centerY = this.size.height / 2f
                val angleRad = Math.toRadians((-90f + sweepAngle).toDouble())
                val headX = (centerX + radius * cos(angleRad)).toFloat()
                val headY = (centerY + radius * sin(angleRad)).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.38f,
                    center = Offset(headX, headY)
                )
            }
        }

        // Central timer information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (isCompleted) Color(0xFF2E7D32) else primaryColor,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (isCompleted) "  GOAL REACHED" else "  FASTING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Big Monospace Countdown
            Text(
                text = countdownText,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("countdown_text")
            )

            Text(
                text = if (isCompleted) "Overtime" else "Remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage & Elapsed
            Text(
                text = "${(progress * 100).toInt()}% · $elapsedText elapsed",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
