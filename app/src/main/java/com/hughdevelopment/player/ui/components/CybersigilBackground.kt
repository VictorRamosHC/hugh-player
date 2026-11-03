package com.hughdevelopment.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.WireframeBorder

/**
 * CybersigilBackground
 *
 * High-performance Sacred Cyber Geometry & Neoclassical Astrolabe background.
 * Uses GPU-accelerated canvas draw calls with rotational infinite transitions
 * to evoke an ancient celestial observatory infused with corporate cold luxury.
 */
@Composable
fun CybersigilBackground(
    modifier: Modifier = Modifier,
    showHalo: Boolean = true,
    isRotating: Boolean = true
) {
    val theme = LocalVaultTheme.current
    val transition = rememberInfiniteTransition(label = "CelestialSigilTransition")

    val orbitAngle1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle1"
    )

    val orbitAngle2 by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val wireColor = WireframeBorder.copy(alpha = 0.45f)
        val strokeFine = Stroke(width = 1.0f)
        val strokeDashed = Stroke(
            width = 1.1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
        )
        val strokeBold = Stroke(width = 1.8f)

        // 1. Neoclassical Hermetic Halo & Sacred Cyber Geometry (Center Background)
        if (showHalo) {
            val haloRadius = w * 0.42f

            // Inner Fixed Wire Ring
            drawCircle(wireColor.copy(alpha = 0.20f), radius = haloRadius * 0.70f, center = Offset(cx, cy), style = strokeFine)

            // Outer Orbit 1 (Clockwise)
            rotate(degrees = if (isRotating) orbitAngle1 else 0f, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = theme.primaryAccent.copy(alpha = 0.22f),
                    radius = haloRadius,
                    center = Offset(cx, cy),
                    style = strokeDashed
                )
                // Celestial Cardinal Ticks
                val tickLen = 28f
                drawLine(
                    theme.primaryAccent.copy(alpha = 0.45f),
                    Offset(cx - haloRadius - tickLen, cy),
                    Offset(cx - haloRadius + tickLen, cy),
                    strokeWidth = 1.5f
                )
                drawLine(
                    theme.primaryAccent.copy(alpha = 0.45f),
                    Offset(cx + haloRadius - tickLen, cy),
                    Offset(cx + haloRadius + tickLen, cy),
                    strokeWidth = 1.5f
                )
            }

            // Outer Orbit 2 (Counter-Clockwise)
            rotate(degrees = if (isRotating) orbitAngle2 else 0f, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = theme.secondaryAccent.copy(alpha = 0.16f),
                    radius = haloRadius * 1.18f,
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 0.9f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                    )
                )
                val tickLen = 18f
                drawLine(
                    theme.secondaryAccent.copy(alpha = 0.4f),
                    Offset(cx, cy - haloRadius * 1.18f - tickLen),
                    Offset(cx, cy - haloRadius * 1.18f + tickLen),
                    strokeWidth = 1.2f
                )
                drawLine(
                    theme.secondaryAccent.copy(alpha = 0.4f),
                    Offset(cx, cy + haloRadius * 1.18f - tickLen),
                    Offset(cx, cy + haloRadius * 1.18f + tickLen),
                    strokeWidth = 1.2f
                )
            }
        }

        // 2. Cybernetic HUD Corner Sigils (Precision Border Alignment)
        val cornerOffset = 20f
        val bracketSize = 36f

        // Top Left [ + ]
        val pathTL = Path().apply {
            moveTo(cornerOffset, cornerOffset + bracketSize)
            lineTo(cornerOffset, cornerOffset)
            lineTo(cornerOffset + bracketSize, cornerOffset)
        }
        drawPath(pathTL, wireColor, style = strokeBold)
        drawLine(
            theme.primaryAccent.copy(alpha = 0.7f),
            Offset(cornerOffset, cornerOffset),
            Offset(cornerOffset + 14f, cornerOffset + 14f),
            strokeWidth = 1.5f
        )

        // Top Right
        val pathTR = Path().apply {
            moveTo(w - cornerOffset - bracketSize, cornerOffset)
            lineTo(w - cornerOffset, cornerOffset)
            lineTo(w - cornerOffset, cornerOffset + bracketSize)
        }
        drawPath(pathTR, wireColor, style = strokeBold)
        drawLine(
            theme.secondaryAccent.copy(alpha = 0.7f),
            Offset(w - cornerOffset, cornerOffset),
            Offset(w - cornerOffset - 14f, cornerOffset + 14f),
            strokeWidth = 1.5f
        )

        // Bottom Left
        val pathBL = Path().apply {
            moveTo(cornerOffset, h - cornerOffset - bracketSize)
            lineTo(cornerOffset, h - cornerOffset)
            lineTo(cornerOffset + bracketSize, h - cornerOffset)
        }
        drawPath(pathBL, wireColor, style = strokeBold)

        // Bottom Right
        val pathBR = Path().apply {
            moveTo(w - cornerOffset - bracketSize, h - cornerOffset)
            lineTo(w - cornerOffset, h - cornerOffset)
            lineTo(w - cornerOffset, h - cornerOffset - bracketSize)
        }
        drawPath(pathBR, wireColor, style = strokeBold)
    }
}
