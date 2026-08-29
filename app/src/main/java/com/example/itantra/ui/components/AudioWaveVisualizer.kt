package com.example.itantra.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.itantra.theme.TacticalGreenLight
import com.example.itantra.theme.WaveformGreen
import com.example.itantra.theme.WaveformRed
import kotlin.math.sin

/**
 * Real-time audio level visualizer that renders animated bars during recording.
 * Uses Canvas/DrawScope for efficient rendering.
 */
@Composable
fun AudioWaveVisualizer(
    audioLevel: Float, // 0.0 to 1.0
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 24
) {
    val animatedLevel by animateFloatAsState(
        targetValue = if (isActive) audioLevel else 0f,
        animationSpec = tween(50),
        label = "audioLevel"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val barWidth = (size.width / barCount) * 0.6f
        val barGap = (size.width / barCount) * 0.4f
        val centerY = size.height / 2

        for (i in 0 until barCount) {
            // Create natural variation in bar heights
            val variance = sin((i * 0.7f + animatedLevel * 10f).toDouble()).toFloat()
            val barLevel = (animatedLevel * (0.4f + 0.6f * ((variance + 1f) / 2f)))
                .coerceIn(0.02f, 1f)

            val barHeight = size.height * barLevel
            val x = i * (barWidth + barGap) + barGap / 2

            // Color gradient from green to red based on level
            val color = if (barLevel > 0.7f) {
                Color(
                    red = WaveformRed.red * barLevel + WaveformGreen.red * (1f - barLevel),
                    green = WaveformRed.green * barLevel + WaveformGreen.green * (1f - barLevel),
                    blue = WaveformRed.blue * barLevel + WaveformGreen.blue * (1f - barLevel),
                    alpha = if (isActive) 0.9f else 0.2f
                )
            } else {
                WaveformGreen.copy(alpha = if (isActive) 0.7f else 0.15f)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}
