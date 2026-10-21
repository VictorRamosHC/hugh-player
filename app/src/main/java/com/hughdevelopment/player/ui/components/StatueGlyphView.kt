package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.WireframeBorder

/**
 * StatueGlyphView
 *
 * Neoclassical Statue & Cybernetic Sigil artwork rendered natively on GPU Canvas.
 * Acts as an iconic high-concept placeholder for tracks lacking artwork.
 */
@Composable
fun StatueGlyphView(
    modifier: Modifier = Modifier,
    accentColor: Color = CyberAmber
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurface)
            .border(1.dp, WireframeBorder, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Concentric Cyber Rings (Neoclassical Halo)
            val strokeThin = Stroke(width = 1.0f)
            drawCircle(WireframeBorder.copy(alpha = 0.6f), radius = w * 0.44f, center = Offset(cx, cy), style = strokeThin)
            drawCircle(WireframeBorder.copy(alpha = 0.35f), radius = w * 0.32f, center = Offset(cx, cy), style = strokeThin)

            // 2. Diamond Crosshair Sigil
            val diamondPath = Path().apply {
                moveTo(cx, cy - h * 0.42f)
                lineTo(cx + w * 0.20f, cy)
                lineTo(cx, cy + h * 0.42f)
                lineTo(cx - w * 0.20f, cy)
                close()
            }
            drawPath(diamondPath, color = WireframeBorder.copy(alpha = 0.4f), style = strokeThin)

            // 3. Neoclassical Classical Bust Sculpture Silhouette (Hermes / Apollo Inspired)
            val bustPath = Path().apply {
                // Head and Curls
                moveTo(cx - w * 0.16f, cy - h * 0.12f)
                cubicTo(
                    cx - w * 0.22f, cy - h * 0.36f,
                    cx + w * 0.22f, cy - h * 0.36f,
                    cx + w * 0.16f, cy - h * 0.12f
                )
                // Chiseled Jawline
                lineTo(cx + w * 0.09f, cy + h * 0.05f)
                lineTo(cx, cy + h * 0.10f)
                lineTo(cx - w * 0.09f, cy + h * 0.05f)
                close()

                // Marble Robe & Pedestal Shoulders
                moveTo(cx - w * 0.30f, cy + h * 0.38f)
                lineTo(cx - w * 0.14f, cy + h * 0.14f)
                lineTo(cx + w * 0.14f, cy + h * 0.14f)
                lineTo(cx + w * 0.30f, cy + h * 0.38f)
            }
            drawPath(bustPath, color = SigilWhite, style = Stroke(width = 1.8f))

            // 4. Accent Mystic Core Glyph
            drawCircle(
                color = accentColor,
                radius = 3.5f,
                center = Offset(cx, cy - h * 0.18f)
            )

            // 5. Crosshair Ticks
            drawLine(accentColor.copy(alpha = 0.8f), Offset(cx - 8f, cy), Offset(cx + 8f, cy), strokeWidth = 1.5f)
            drawLine(accentColor.copy(alpha = 0.8f), Offset(cx, cy - 8f), Offset(cx, cy + 8f), strokeWidth = 1.5f)
        }
    }
}
