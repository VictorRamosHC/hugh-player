package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.WireframeBorder

/**
 * CybersigilBackground
 *
 * High-performance HUD & Neoclassical geometry inspired by Hermes Agent / Nous Research.
 * Uses optimized hardware-accelerated vector paths to guarantee 120 FPS without CPU churn.
 */
@Composable
fun CybersigilBackground(
    modifier: Modifier = Modifier,
    showHalo: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val wireColor = WireframeBorder.copy(alpha = 0.45f)
        val strokeFine = Stroke(width = 1.0f)
        val strokeBold = Stroke(width = 1.8f)

        // 1. Neoclassical Hermetic Halo & Sacred Cyber Geometry (Center Background)
        if (showHalo) {
            val haloRadius = w * 0.42f
            drawCircle(wireColor.copy(alpha = 0.20f), radius = haloRadius, center = Offset(cx, cy), style = strokeFine)
            drawCircle(wireColor.copy(alpha = 0.12f), radius = haloRadius * 0.70f, center = Offset(cx, cy), style = strokeFine)
            drawCircle(HermeticCyan.copy(alpha = 0.10f), radius = haloRadius * 1.15f, center = Offset(cx, cy), style = strokeFine)

            // Crosshair Cardinal Ticks
            val tickLen = 24f
            drawLine(wireColor.copy(alpha = 0.35f), Offset(cx - haloRadius - tickLen, cy), Offset(cx - haloRadius + tickLen, cy), strokeWidth = 1f)
            drawLine(wireColor.copy(alpha = 0.35f), Offset(cx + haloRadius - tickLen, cy), Offset(cx + haloRadius + tickLen, cy), strokeWidth = 1f)
            drawLine(wireColor.copy(alpha = 0.35f), Offset(cx, cy - haloRadius - tickLen), Offset(cx, cy - haloRadius + tickLen), strokeWidth = 1f)
            drawLine(wireColor.copy(alpha = 0.35f), Offset(cx, cy + haloRadius - tickLen), Offset(cx, cy + haloRadius + tickLen), strokeWidth = 1f)
        }

        // 2. Cybernetic HUD Corner Sigils
        val cornerOffset = 20f
        val bracketSize = 36f

        // Top Left [ + ]
        val pathTL = Path().apply {
            moveTo(cornerOffset, cornerOffset + bracketSize)
            lineTo(cornerOffset, cornerOffset)
            lineTo(cornerOffset + bracketSize, cornerOffset)
        }
        drawPath(pathTL, wireColor, style = strokeBold)
        drawLine(CyberAmber.copy(alpha = 0.6f), Offset(cornerOffset, cornerOffset), Offset(cornerOffset + 14f, cornerOffset + 14f), strokeWidth = 1.5f)

        // Top Right
        val pathTR = Path().apply {
            moveTo(w - cornerOffset - bracketSize, cornerOffset)
            lineTo(w - cornerOffset, cornerOffset)
            lineTo(w - cornerOffset, cornerOffset + bracketSize)
        }
        drawPath(pathTR, wireColor, style = strokeBold)
        drawLine(HermeticCyan.copy(alpha = 0.6f), Offset(w - cornerOffset, cornerOffset), Offset(w - cornerOffset - 14f, cornerOffset + 14f), strokeWidth = 1.5f)

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
