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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.WireframeBorder

/**
 * StatueGlyphView
 * 
 * Neoclassical Statue & Cybernetic Glyph artwork rendered natively.
 * Used as a high-concept album art placeholder in the Hermes / Hugh aesthetic.
 */
@Composable
fun StatueGlyphView(
    modifier: Modifier = Modifier,
    accentColor: Color = HermeticCyan
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

            // 1. Concentric Cyber Rings (Halo)
            val strokeThin = Stroke(width = 1.2f)
            drawCircle(WireframeBorder, radius = w * 0.40f, center = Offset(cx, cy), style = strokeThin)
            drawCircle(WireframeBorder.copy(alpha = 0.5f), radius = w * 0.28f, center = Offset(cx, cy), style = strokeThin)

            // 2. Crosshair and Diamond Sigil
            val diamondPath = Path().apply {
                moveTo(cx, cy - h * 0.38f)
                lineTo(cx + w * 0.12f, cy)
                lineTo(cx, cy + h * 0.38f)
                lineTo(cx - w * 0.12f, cy)
                close()
            }
            drawPath(diamondPath, color = WireframeBorder.copy(alpha = 0.7f), style = strokeThin)

            // 3. Neoclassical Bust Wireframe Silhouette
            val bustPath = Path().apply {
                // Head outline (sculpture curve)
                moveTo(cx - w * 0.14f, cy - h * 0.18f)
                cubicTo(
                    cx - w * 0.16f, cy - h * 0.32f,
                    cx + w * 0.16f, cy - h * 0.32f,
                    cx + w * 0.14f, cy - h * 0.18f
                )
                // Jawline
                lineTo(cx + w * 0.08f, cy - h * 0.04f)
                lineTo(cx, cy + h * 0.02f)
                lineTo(cx - w * 0.08f, cy - h * 0.04f)
                close()

                // Shoulders & Pedestal
                moveTo(cx - w * 0.24f, cy + h * 0.30f)
                lineTo(cx - w * 0.12f, cy + h * 0.06f)
                lineTo(cx + w * 0.12f, cy + h * 0.06f)
                lineTo(cx + w * 0.24f, cy + h * 0.30f)
            }
            drawPath(bustPath, color = SigilSilver, style = Stroke(width = 1.8f))

            // 4. Accent Glyph dot in center forehead / crown
            drawCircle(
                color = accentColor,
                radius = 3.5f,
                center = Offset(cx, cy - h * 0.22f)
            )

            // 5. Cardinal coordinate ticks
            drawLine(accentColor, Offset(cx - 10f, cy), Offset(cx + 10f, cy), strokeWidth = 1.5f)
            drawLine(accentColor, Offset(cx, cy - 10f), Offset(cx, cy + 10f), strokeWidth = 1.5f)
        }
    }
}
