package com.hughdevelopment.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.ui.theme.LocalVaultTheme

/**
 * HermesAstrolabeOracleEye
 *
 * Implements the Divine Sovereign Oracle face from the v2.0 prototype:
 * Three concentric rotating celestial astrolabe rings with a pulsing luminous core,
 * reacting to music playback and TTS speech synthesis.
 */
@Composable
fun HermesAstrolabeOracleEye(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    isActive: Boolean = true
) {
    val theme = LocalVaultTheme.current
    val transition = rememberInfiniteTransition(label = "AstrolabeTransition")

    // Slow Clockwise Celestial Orbit
    val angleClockwise by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleClockwise"
    )

    // Counter-Clockwise Intermediate Orbit
    val angleCounterClockwise by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleCounterClockwise"
    )

    // Core Pulse
    val corePulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val outerRadius = w * 0.46f
            val midRadius = w * 0.32f
            val innerRadius = w * 0.18f

            val strokeThin = Stroke(
                width = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
            val strokeDotted = Stroke(
                width = 1.0f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
            )

            // 1. Outer Astrolabe Ring (Theme Primary Accent, Clockwise)
            rotate(degrees = if (isActive) angleClockwise else 0f, pivot = center) {
                drawCircle(
                    color = theme.primaryAccent.copy(alpha = 0.65f),
                    radius = outerRadius,
                    center = center,
                    style = strokeThin
                )
                // Small celestial cardinal tick
                drawLine(
                    color = theme.primaryAccent,
                    start = Offset(center.x, center.y - outerRadius - 3f),
                    end = Offset(center.x, center.y - outerRadius + 3f),
                    strokeWidth = 1.5f
                )
            }

            // 2. Middle Celestial Ring (Secondary Accent, Counter-Clockwise)
            rotate(degrees = if (isActive) angleCounterClockwise else 0f, pivot = center) {
                drawCircle(
                    color = theme.secondaryAccent.copy(alpha = 0.55f),
                    radius = midRadius,
                    center = center,
                    style = strokeDotted
                )
            }

            // 3. Inner Boundary Ring
            drawCircle(
                color = theme.textPrimary.copy(alpha = 0.25f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 0.8f)
            )

            // 4. Radiant Luminous Core
            val pulseRadius = (innerRadius * 0.55f) * if (isActive) corePulse else 1f
            // Soft glow aura
            drawCircle(
                color = theme.accentGlow.copy(alpha = 0.7f),
                radius = pulseRadius * 1.8f,
                center = center
            )
            // Pure core
            drawCircle(
                color = theme.textPrimary,
                radius = pulseRadius,
                center = center
            )
        }
    }
}
