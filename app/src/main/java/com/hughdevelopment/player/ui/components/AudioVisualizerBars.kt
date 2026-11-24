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
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.VaultVisualizerStyle
import com.hughdevelopment.player.ui.theme.WireframeBorder
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun AudioVisualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    height: Dp = 38.dp,
    activeColor: Color = Color.Unspecified,
    secondaryColor: Color = Color.Unspecified
) {
    val theme = LocalVaultTheme.current
    val resolvedActiveColor = if (activeColor != Color.Unspecified) activeColor else theme.primaryAccent
    val resolvedSecondaryColor = if (secondaryColor != Color.Unspecified) secondaryColor else theme.secondaryAccent
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
        val barSpacing = when (theme.visualizerStyle) {
            VaultVisualizerStyle.NULL_HAIRLINE -> 6f
            VaultVisualizerStyle.WIN95_PIXEL_BLOCKS -> 3f
            else -> 4f
        }
        val totalSpacing = (barCount - 1) * barSpacing
        val barWidth = when (theme.visualizerStyle) {
            VaultVisualizerStyle.NULL_HAIRLINE -> 1.5f
            else -> ((totalWidth - totalSpacing) / barCount).coerceAtLeast(3f)
        }

        for (i in 0 until barCount) {
            val normIndex = i.toFloat() / barCount
            val x = i * (barWidth + barSpacing)

            val heightFraction = if (isPlaying) {
                val wave1 = sin(phase + normIndex * 4.2f)
                val wave2 = sin(phase * 1.8f + normIndex * 7.5f)
                (abs(wave1 * 0.6f + wave2 * 0.4f)).coerceIn(0.12f, 1.0f)
            } else {
                0.08f
            }

            val barHeight = (canvasHeight * heightFraction).coerceAtLeast(4f)
            val y = canvasHeight - barHeight

            val brush = Brush.verticalGradient(
                colors = if (isPlaying) listOf(resolvedActiveColor, resolvedSecondaryColor) else listOf(WireframeBorder, WireframeBorder),
                startY = y,
                endY = canvasHeight
            )

            when (theme.visualizerStyle) {
                VaultVisualizerStyle.DOGE_PAWS_BUBBLE -> {
                    // Full rounded pill/capsule
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
                VaultVisualizerStyle.WIN95_PIXEL_BLOCKS -> {
                    // Chunky retro stepped square blocks with 2px vertical gap
                    val blockSize = 5f
                    val blockGap = 2f
                    var by = canvasHeight - blockSize
                    while (by >= y) {
                        drawRect(
                            brush = brush,
                            topLeft = Offset(x, by),
                            size = Size(barWidth, blockSize)
                        )
                        by -= (blockSize + blockGap)
                    }
                }
                VaultVisualizerStyle.ACID_GEIGER_SPIKES -> {
                    // Sharp Geiger radioactive spike needle
                    drawLine(
                        brush = brush,
                        start = Offset(x + barWidth / 2f, canvasHeight),
                        end = Offset(x + barWidth / 2f, y),
                        strokeWidth = (barWidth * 0.7f).coerceAtLeast(2f)
                    )
                    drawCircle(
                        color = resolvedActiveColor,
                        radius = (barWidth * 0.6f).coerceAtLeast(2.5f),
                        center = Offset(x + barWidth / 2f, y)
                    )
                }
                VaultVisualizerStyle.NULL_HAIRLINE -> {
                    // Ultra-minimal 1.5px laser hairlines
                    drawLine(
                        brush = brush,
                        start = Offset(x, canvasHeight),
                        end = Offset(x, y),
                        strokeWidth = 1.5f
                    )
                }
                else -> {
                    // Default / Harmonic Spectrum / Archon Golden Halo / Katana Slices
                    val cornerRadius = when {
                        theme.isBrutalistSharp -> CornerRadius(1f, 1f)
                        theme.cardCornerRadius >= 16.dp -> CornerRadius(barWidth / 2f, barWidth / 2f)
                        else -> CornerRadius(3f, 3f)
                    }
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}
