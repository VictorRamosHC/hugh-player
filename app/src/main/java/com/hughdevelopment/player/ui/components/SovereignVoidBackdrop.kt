package com.hughdevelopment.player.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.hughdevelopment.player.ui.theme.LocalVaultTheme

/**
 * SovereignVoidBackdrop // Divine Obsidian Void & Radiant Auras
 *
 * Provides a clean, ultra-luxury backdrop that eliminates visual clutter and
 * guarantees 100% typographic contrast across all themes and screens.
 */
@Composable
fun SovereignVoidBackdrop(
    modifier: Modifier = Modifier,
    showCybersigil: Boolean = true,
    auraScale: Float = 1.0f
) {
    val theme = LocalVaultTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "SovereignBackdropBreath")
    val auraBreath by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraBreath"
    )

    val primaryColor = theme.primary
    val secondaryColor = theme.secondaryAccent

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Deep Obsidian Void
            drawRect(color = Color(0xFF07070B))

            // 2. Primary Radiant Ambient Aura (Top-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.16f * auraBreath * auraScale),
                        primaryColor.copy(alpha = 0.05f * auraBreath * auraScale),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.15f),
                    radius = size.width * 0.85f
                )
            )

            // 3. Secondary Radiant Ambient Aura (Mid-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.11f * auraBreath * auraScale),
                        secondaryColor.copy(alpha = 0.03f * auraBreath * auraScale),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.82f, size.height * 0.52f),
                    radius = size.width * 0.70f
                )
            )
        }

        // 4. Subtle sacred geometry overlay
        if (showCybersigil) {
            CybersigilBackground(
                modifier = Modifier.fillMaxSize(),
                showHalo = false,
                isRotating = true
            )
        }
    }
}
