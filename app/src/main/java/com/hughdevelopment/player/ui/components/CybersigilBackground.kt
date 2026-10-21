package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hughdevelopment.player.ui.theme.DitherDark
import com.hughdevelopment.player.ui.theme.WireframeBorder

/**
 * CybersigilBackground
 * 
 * Draws hardware-accelerated stippled dot grid and wireframe sigils
 * inspired by the Hermes Agent / Nous Research aesthetic.
 */
@Composable
fun CybersigilBackground(
    modifier: Modifier = Modifier,
    dotSpacing: Float = 48f,
    dotRadius: Float = 1.4f,
    dotColor: Color = DitherDark,
    wireframeColor: Color = WireframeBorder
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Stippled Halftone Dot Grid
        var x = 0f
        while (x < width) {
            var y = 0f
            while (y < height) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                y += dotSpacing
            }
            x += dotSpacing
        }

        // 2. Geometric Corner Sigils (Cybernetic frames)
        val cornerSize = 40f
        val stroke = Stroke(width = 1.2f)

        // Top-Left Frame & Tick
        val pathTL = Path().apply {
            moveTo(16f, 16f + cornerSize)
            lineTo(16f, 16f)
            lineTo(16f + cornerSize, 16f)
        }
        drawPath(pathTL, wireframeColor, style = stroke)
        drawLine(wireframeColor, Offset(16f, 16f), Offset(28f, 28f), strokeWidth = 1.2f)

        // Top-Right Frame & Tick
        val pathTR = Path().apply {
            moveTo(width - 16f - cornerSize, 16f)
            lineTo(width - 16f, 16f)
            lineTo(width - 16f, 16f + cornerSize)
        }
        drawPath(pathTR, wireframeColor, style = stroke)
        drawLine(wireframeColor, Offset(width - 16f, 16f), Offset(width - 28f, 28f), strokeWidth = 1.2f)

        // Bottom-Left Frame
        val pathBL = Path().apply {
            moveTo(16f, height - 16f - cornerSize)
            lineTo(16f, height - 16f)
            lineTo(16f + cornerSize, height - 16f)
        }
        drawPath(pathBL, wireframeColor, style = stroke)

        // Bottom-Right Frame
        val pathBR = Path().apply {
            moveTo(width - 16f - cornerSize, height - 16f)
            lineTo(width - 16f, height - 16f)
            lineTo(width - 16f, height - 16f - cornerSize)
        }
        drawPath(pathBR, wireframeColor, style = stroke)
    }
}
