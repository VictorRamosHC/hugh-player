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
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SpecularGlassRimBottom
import com.hughdevelopment.player.ui.theme.SpecularGlassRimMid
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop

/**
 * Neo-Glass Brutalism Modifiers
 *
 * Implements Apple-inspired Frosted Glassmorphism with physical specular light refraction
 * and brutalist high-contrast hairlines.
 * Uses static cached brushes to prevent GC allocation churn during 120 FPS scrolling.
 */

private val cachedCardShape8 = RoundedCornerShape(8.dp)
private val cachedCardShape10 = RoundedCornerShape(10.dp)

private val cachedDefaultBrush = Brush.verticalGradient(
    colors = listOf(
        SpecularGlassRimTop,
        SpecularGlassRimMid,
        SpecularGlassRimBottom
    )
)

private val cachedActivePlayingBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.85f),
        KleinBlue,
        KleinBlue
    )
)

fun Modifier.neoGlassCard(
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = ObsidianGlass,
    borderWidth: Dp = 1.dp,
    topRimColor: Color = SpecularGlassRimTop,
    bottomRimColor: Color = SpecularGlassRimBottom
): Modifier {
    val shape = if (cornerRadius == 8.dp) cachedCardShape8 else if (cornerRadius == 10.dp) cachedCardShape10 else RoundedCornerShape(cornerRadius)
    val brush = if (topRimColor == SpecularGlassRimTop && bottomRimColor == SpecularGlassRimBottom) {
        cachedDefaultBrush
    } else {
        Brush.verticalGradient(colors = listOf(topRimColor, SpecularGlassRimMid, bottomRimColor))
    }
    return this
        .clip(shape)
        .background(backgroundColor)
        .border(width = borderWidth, brush = brush, shape = shape)
}

fun Modifier.activePlayingGlassCard(
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = ObsidianGlass
): Modifier {
    val shape = if (cornerRadius == 8.dp) cachedCardShape8 else RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(backgroundColor)
        .border(width = 1.5.dp, brush = cachedActivePlayingBrush, shape = shape)
}
