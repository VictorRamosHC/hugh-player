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
private val cachedCardShape14 = RoundedCornerShape(14.dp)
private val cachedCardShape16 = RoundedCornerShape(16.dp)

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
    cornerRadius: Dp = 14.dp,
    backgroundColor: Color = ObsidianGlass,
    borderWidth: Dp = 1.dp,
    topRimColor: Color = SpecularGlassRimTop,
    bottomRimColor: Color = SpecularGlassRimBottom
): Modifier {
    val shape = when (cornerRadius) {
        8.dp -> cachedCardShape8
        10.dp -> cachedCardShape10
        14.dp -> cachedCardShape14
        16.dp -> cachedCardShape16
        else -> RoundedCornerShape(cornerRadius)
    }
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
    cornerRadius: Dp = 14.dp,
    backgroundColor: Color = ObsidianGlass,
    accentColor: Color? = null
): Modifier {
    val shape = when (cornerRadius) {
        8.dp -> cachedCardShape8
        10.dp -> cachedCardShape10
        14.dp -> cachedCardShape14
        16.dp -> cachedCardShape16
        else -> RoundedCornerShape(cornerRadius)
    }
    val brush = if (accentColor == null || accentColor == KleinBlue) {
        cachedActivePlayingBrush
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                accentColor,
                accentColor
            )
        )
    }
    return this
        .clip(shape)
        .background(backgroundColor)
        .border(width = 1.5.dp, brush = brush, shape = shape)
}

fun Modifier.quartzGlassCapsule(
    cornerRadius: Dp = 14.dp,
    backgroundColor: Color = ObsidianGlass,
    accentColor: Color = SpecularGlassRimTop,
    topRimColor: Color = SpecularGlassRimTop,
    borderWidth: Dp = 1.dp
): Modifier {
    val shape = when (cornerRadius) {
        14.dp -> cachedCardShape14
        16.dp -> cachedCardShape16
        else -> RoundedCornerShape(cornerRadius)
    }
    val brush = Brush.verticalGradient(
        colors = listOf(
            topRimColor,
            accentColor.copy(alpha = 0.55f),
            accentColor.copy(alpha = 0.20f)
        )
    )
    return this
        .clip(shape)
        .background(backgroundColor)
        .border(width = borderWidth, brush = brush, shape = shape)
}
