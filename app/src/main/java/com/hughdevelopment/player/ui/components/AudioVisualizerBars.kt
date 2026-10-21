package com.hughdevelopment.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.WireframeBorder
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun AudioVisualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    height: Dp = 38.dp,
    activeColor: Color = CyberAmber,
    secondaryColor: Color = HermeticCyan
) {
    val transition = rememberInfiniteTransition(label = "VisualizerTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnimation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barSpacing = 4f
        val totalSpacing = (barCount - 1) * barSpacing
        val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(3f)

        for (i in 0 until barCount) {
            val normIndex = i.toFloat() / barCount
            val x = i * (barWidth + barSpacing)

            val heightFraction = if (isPlaying) {
                // Harmonic synthesis wave for realistic audio analyzer behavior
                val wave1 = sin(phase + normIndex * 4.2f)
                val wave2 = sin(phase * 1.8f + normIndex * 7.5f)
                val raw = (abs(wave1 * 0.6f + wave2 * 0.4f)).coerceIn(0.12f, 1.0f)
                raw
            } else {
                0.08f
            }

            val barHeight = (canvasHeight * heightFraction).coerceAtLeast(4f)
            val y = canvasHeight - barHeight

            val brush = Brush.verticalGradient(
                colors = if (isPlaying) listOf(activeColor, secondaryColor) else listOf(WireframeBorder, WireframeBorder),
                startY = y,
                endY = canvasHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}
