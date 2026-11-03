package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.WireframeBorder

/**
 * WaveformScrubber // Interactive 42-Band Real Audio Amplitude Scrubber.
 *
 * Implements Option 3 & Prototype Parity:
 * Replaces the generic Material Slider with an interactive hardware-grade
 * waveform spectrum displaying actual track dynamics, tactile dragging and
 * floating HUD seek timestamp tooltips.
 */
@Composable
fun WaveformScrubber(
    points: List<Float>,
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalVaultTheme.current
    val effectiveDuration = durationMs.coerceAtLeast(1L)
    val playbackFraction = (currentPositionMs.toFloat() / effectiveDuration).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val activeFraction = if (isDragging) dragFraction else playbackFraction

    // Fallback if points are empty
    val safePoints = remember(points) {
        if (points.size >= 42) points.take(42)
        else List(42) { 0.25f + ((it % 5) * 0.12f) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            // Floating HUD Tooltip when scrubbing
            if (isDragging) {
                val seekTimeMs = (dragFraction * effectiveDuration).toLong()
                val min = seekTimeMs / 60000
                val sec = (seekTimeMs % 60000) / 1000
                val tooltipText = "%02d:%02d // SEEK".format(min, sec)

                Surface(
                    color = theme.primaryAccent,
                    shape = RoundedCornerShape(4.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(x = 0, y = -14) }
                ) {
                    Text(
                        text = tooltipText,
                        color = SigilWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Interactive Canvas Waveform
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .align(Alignment.Center)
                    .pointerInput(durationMs) {
                        detectTapGestures { offset ->
                            val frac = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeekTo((frac * effectiveDuration).toLong())
                        }
                    }
                    .pointerInput(durationMs) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            },
                            onDragEnd = {
                                onSeekTo((dragFraction * effectiveDuration).toLong())
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
            ) {
                val totalWidth = size.width
                val canvasHeight = size.height
                val barCount = safePoints.size
                val barSpacing = 2.5f
                val totalSpacing = (barCount - 1) * barSpacing
                val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(2f)

                val activeColor = theme.primaryAccent
                val inactiveColor = WireframeBorder.copy(alpha = 0.55f)

                for (i in 0 until barCount) {
                    val barFraction = i.toFloat() / (barCount - 1).coerceAtLeast(1)
                    val isPlayed = barFraction <= activeFraction

                    val pointAmp = safePoints[i].coerceIn(0.15f, 1.0f)
                    val barHeight = (canvasHeight * pointAmp * 0.85f).coerceAtLeast(4f)
                    val x = i * (barWidth + barSpacing)
                    val y = (canvasHeight - barHeight) / 2f

                    drawRoundRect(
                        color = if (isPlayed) activeColor else inactiveColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(1.5f, 1.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time Indicators Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayedMs = if (isDragging) (dragFraction * effectiveDuration).toLong() else currentPositionMs
            val curMin = displayedMs / 60000
            val curSec = (displayedMs % 60000) / 1000

            val durMin = durationMs / 60000
            val durSec = (durationMs % 60000) / 1000

            Text(
                text = "%02d:%02d".format(curMin, curSec),
                color = theme.primaryAccent,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "FLAC • 42-BAND WAVEFORM",
                color = theme.textMuted.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "%02d:%02d".format(durMin, durSec),
                color = theme.textMuted,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
