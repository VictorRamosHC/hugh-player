package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.ui.theme.KleinBlue
import com.hughdevelopment.player.ui.theme.KleinBlueGlow
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SpecularGlassRimBottom
import com.hughdevelopment.player.ui.theme.SpecularGlassRimMid
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop

/**
 * Neo-Glass Brutalism Modifiers
 *
 * Implements Apple-inspired Frosted Glassmorphism with physical specular light refraction
 * and brutalist high-contrast hairlines.
 */

fun Modifier.neoGlassCard(
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = ObsidianGlass,
    borderWidth: Dp = 1.dp,
    topRimColor: Color = SpecularGlassRimTop,
    bottomRimColor: Color = SpecularGlassRimBottom
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = listOf(
                topRimColor,
                SpecularGlassRimMid,
                bottomRimColor
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

fun Modifier.activePlayingGlassCard(
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = ObsidianGlass
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(
        width = 1.5.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                KleinBlue,
                KleinBlue
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
