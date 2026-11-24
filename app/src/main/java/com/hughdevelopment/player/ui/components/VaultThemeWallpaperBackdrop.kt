package com.hughdevelopment.player.ui.components

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.VaultThemeConfig
import com.hughdevelopment.player.ui.theme.VaultWallpaperStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * VaultThemeWallpaperBackdrop
 *
 * High-performance, GPU-accelerated procedurally rendered wallpaper engine.
 * Renders distinct aesthetic textures, architectural geometry, HUD telemetry,
 * scanlines, blueprints, and thematic artwork for all 10 curated Pantheon Store themes.
 */
@Composable
fun VaultThemeWallpaperBackdrop(
    modifier: Modifier = Modifier,
    wallpaperAlpha: Float = 0.35f,
    theme: VaultThemeConfig = LocalVaultTheme.current
) {
    val transition = rememberInfiniteTransition(label = "ThemeWallpaperTransition")

    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle"
    )

    val breathPulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathPulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val alphaMultiplier = wallpaperAlpha.coerceIn(0f, 1f)

            // Base Background Fill
            drawRect(color = theme.backgroundColor)

            if (alphaMultiplier <= 0.01f) return@Canvas

            when (theme.wallpaperStyle) {
                VaultWallpaperStyle.DOGE_MEME_ALTAR -> {
                    drawDogeMemeAltar(w, h, cx, cy, theme, alphaMultiplier, breathPulse, orbitAngle)
                }
                VaultWallpaperStyle.WIN95_VAPORWAVE_CRT -> {
                    drawWin95Vaporwave(w, h, cx, cy, theme, alphaMultiplier, breathPulse)
                }
                VaultWallpaperStyle.TERRA_BRASILIS_CONCRETE -> {
                    drawTerraBrasilis(w, h, cx, cy, theme, alphaMultiplier, breathPulse)
                }
                VaultWallpaperStyle.MONOLITH_NULL_ECLIPSE -> {
                    drawMonolithNull(w, h, cx, cy, theme, alphaMultiplier, breathPulse)
                }
                VaultWallpaperStyle.ACID_HAZARD_RADIATION -> {
                    drawAcidProtocol(w, h, cx, cy, theme, alphaMultiplier, breathPulse, orbitAngle)
                }
                VaultWallpaperStyle.ELDRITCH_GOTHIC_CATHEDRAL -> {
                    drawEldritchCathedral(w, h, cx, cy, theme, alphaMultiplier, breathPulse)
                }
                VaultWallpaperStyle.APOLLO_ANALOG_COCKPIT -> {
                    drawApolloCockpit(w, h, cx, cy, theme, alphaMultiplier, breathPulse)
                }
                VaultWallpaperStyle.NEO_KYOTO_CARBON_KATANA -> {
                    drawNeoKyotoKatana(w, h, cx, cy, theme, alphaMultiplier, breathPulse, orbitAngle)
                }
                VaultWallpaperStyle.WEYLAND_DEEP_SPACE_HUD -> {
                    drawWeylandDeepSpace(w, h, cx, cy, theme, alphaMultiplier, breathPulse)
                }
                VaultWallpaperStyle.SOVEREIGN_ARCHON_CELESTIAL -> {
                    drawSovereignArchon(w, h, cx, cy, theme, alphaMultiplier, breathPulse, orbitAngle)
                }
                VaultWallpaperStyle.SACRED_CYBERSIGIL -> {
                    drawClassicCybersigil(w, h, cx, cy, theme, alphaMultiplier, breathPulse, orbitAngle)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 1. DOGE SOBERANO // MUCH AUDIO (Meme Tier)
// ---------------------------------------------------------------------------
private fun DrawScope.drawDogeMemeAltar(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float, orbit: Float
) {
    // Golden cosmic aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.primaryAccent.copy(alpha = 0.25f * alpha * breath),
                theme.secondaryAccent.copy(alpha = 0.08f * alpha),
                Color.Transparent
            ),
            center = Offset(cx, cy * 0.7f),
            radius = w * 0.75f
        )
    )

    // Classical Roman temple columns on left and right
    val colStroke = Stroke(width = 1.8f)
    val colColor = theme.secondaryAccent.copy(alpha = 0.22f * alpha)

    // Left Column
    drawLine(colColor, Offset(w * 0.08f, h * 0.15f), Offset(w * 0.08f, h * 0.85f), strokeWidth = 1.5f)
    drawLine(colColor, Offset(w * 0.13f, h * 0.15f), Offset(w * 0.13f, h * 0.85f), strokeWidth = 1.5f)
    drawRect(colColor, Offset(w * 0.06f, h * 0.14f), Size(w * 0.09f, 12f))
    drawRect(colColor, Offset(w * 0.06f, h * 0.85f), Size(w * 0.09f, 14f))

    // Right Column
    drawLine(colColor, Offset(w * 0.87f, h * 0.15f), Offset(w * 0.87f, h * 0.85f), strokeWidth = 1.5f)
    drawLine(colColor, Offset(w * 0.92f, h * 0.15f), Offset(w * 0.92f, h * 0.85f), strokeWidth = 1.5f)
    drawRect(colColor, Offset(w * 0.85f, h * 0.14f), Size(w * 0.09f, 12f))
    drawRect(colColor, Offset(w * 0.85f, h * 0.85f), Size(w * 0.09f, 14f))

    // Doge Golden Laurel Wreath (Orbiting Center)
    val laurelRadius = w * 0.32f
    rotate(degrees = orbit * 0.5f, pivot = Offset(cx, cy * 0.65f)) {
        drawCircle(
            color = theme.primaryAccent.copy(alpha = 0.35f * alpha),
            radius = laurelRadius,
            center = Offset(cx, cy * 0.65f),
            style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f))
        )
        // Laurel Leaves Ticks
        for (i in 0 until 12) {
            val ang = (i * 30f) * (PI / 180f).toFloat()
            val px = cx + cos(ang) * laurelRadius
            val py = cy * 0.65f + sin(ang) * laurelRadius
            drawCircle(theme.secondaryAccent.copy(alpha = 0.45f * alpha), radius = 5f, center = Offset(px, py))
        }
    }

    // Stylized Doge Paw print at center
    val pawColor = theme.primaryAccent.copy(alpha = 0.28f * alpha)
    drawCircle(pawColor, radius = 24f, center = Offset(cx, cy * 0.65f + 10f)) // Main pad
    drawCircle(pawColor, radius = 9f, center = Offset(cx - 20f, cy * 0.65f - 18f))
    drawCircle(pawColor, radius = 10f, center = Offset(cx - 7f, cy * 0.65f - 26f))
    drawCircle(pawColor, radius = 10f, center = Offset(cx + 7f, cy * 0.65f - 26f))
    drawCircle(pawColor, radius = 9f, center = Offset(cx + 20f, cy * 0.65f - 18f))
}

// ---------------------------------------------------------------------------
// 2. WIN95 VAPORWAVE // BIOS ALTAR (Nostalgia Glitch)
// ---------------------------------------------------------------------------
private fun DrawScope.drawWin95Vaporwave(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float
) {
    val horizonY = h * 0.55f

    // 1. Vaporwave Sunset Gradient Sun
    drawCircle(
        brush = Brush.verticalGradient(
            listOf(theme.primaryAccent.copy(alpha = 0.55f * alpha * breath), theme.secondaryAccent.copy(alpha = 0.20f * alpha)),
            startY = horizonY - w * 0.35f,
            endY = horizonY
        ),
        radius = w * 0.35f,
        center = Offset(cx, horizonY)
    )

    // Sun horizontal blind cut lines
    val sunBlindColor = theme.backgroundColor
    for (i in 1..8) {
        val y = horizonY - i * 16f
        val lineThick = 2.5f + i * 0.6f
        drawLine(sunBlindColor, Offset(cx - w * 0.4f, y), Offset(cx + w * 0.4f, y), strokeWidth = lineThick)
    }

    // 2. Perspective Ground Grid (Neon Magenta & Cyan)
    val gridColor = theme.secondaryAccent.copy(alpha = 0.35f * alpha)
    // Horizontal perspective lines
    var step = 12f
    var currY = horizonY
    while (currY < h) {
        drawLine(gridColor, Offset(0f, currY), Offset(w, currY), strokeWidth = 1.2f)
        currY += step
        step *= 1.25f
    }
    // Vanishing perspective rays
    for (i in -6..6) {
        val bottomX = cx + (i * w * 0.16f)
        drawLine(gridColor, Offset(cx, horizonY), Offset(bottomX, h), strokeWidth = 1.3f)
    }

    // 3. CRT Scanlines
    val scanlineColor = Color.Black.copy(alpha = 0.30f * alpha)
    for (sy in 0 until h.toInt() step 6) {
        drawLine(scanlineColor, Offset(0f, sy.toFloat()), Offset(w, sy.toFloat()), strokeWidth = 1.0f)
    }

    // 4. Windows 95 Beveled Window Wireframe Floating
    val winColor = theme.primaryAccent.copy(alpha = 0.25f * alpha)
    drawRect(winColor, Offset(w * 0.15f, h * 0.18f), Size(w * 0.70f, h * 0.22f), style = Stroke(width = 1.8f))
    drawRect(theme.secondaryAccent.copy(alpha = 0.20f * alpha), Offset(w * 0.15f, h * 0.18f), Size(w * 0.70f, 22f))
}

// ---------------------------------------------------------------------------
// 3. TERRA BRASILIS // CONCRETO & FLORESTA (Tropical Brutalist)
// ---------------------------------------------------------------------------
private fun DrawScope.drawTerraBrasilis(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float
) {
    // Terracotta warm sun circle
    drawCircle(
        color = theme.secondaryAccent.copy(alpha = 0.22f * alpha),
        radius = w * 0.32f,
        center = Offset(w * 0.78f, h * 0.22f)
    )

    // Sweeping Niemeyer architectural cantilever concrete curve
    val pathNiemeyer = Path().apply {
        moveTo(0f, h * 0.40f)
        cubicTo(w * 0.35f, h * 0.28f, w * 0.65f, h * 0.55f, w, h * 0.42f)
        lineTo(w, h * 0.52f)
        cubicTo(w * 0.65f, h * 0.65f, w * 0.35f, h * 0.38f, 0f, h * 0.50f)
        close()
    }
    drawPath(
        path = pathNiemeyer,
        color = theme.surfaceColor.copy(alpha = 0.85f * alpha)
    )
    drawPath(
        path = pathNiemeyer,
        color = theme.primaryAccent.copy(alpha = 0.45f * alpha),
        style = Stroke(width = 1.8f)
    )

    // Woodcut Tropical Foliage / Palm & Root Silhouettes
    val leafColor = theme.primaryAccent.copy(alpha = 0.28f * alpha * breath)
    val rootPath = Path().apply {
        moveTo(0f, h * 0.85f)
        quadraticBezierTo(w * 0.25f, h * 0.70f, w * 0.45f, h * 0.88f)
        quadraticBezierTo(w * 0.65f, h * 0.68f, w, h * 0.82f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(rootPath, leafColor)
}

// ---------------------------------------------------------------------------
// 4. MONOLITH NULL // VANTABLACK ZERO (Abyssal Minimal)
// ---------------------------------------------------------------------------
private fun DrawScope.drawMonolithNull(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float
) {
    // Pure 100% OLED Void
    val ringRadius = w * 0.28f

    // Subtle Ultraviolet Corona Breathing Halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.secondaryAccent.copy(alpha = 0.35f * alpha * breath),
                theme.secondaryAccent.copy(alpha = 0.08f * alpha),
                Color.Transparent
            ),
            center = Offset(cx, cy * 0.72f),
            radius = ringRadius * 1.55f
        )
    )

    // Ultra-thin 0.5px Solar Eclipse Ring
    drawCircle(
        color = theme.textPrimary.copy(alpha = 0.85f * alpha),
        radius = ringRadius,
        center = Offset(cx, cy * 0.72f),
        style = Stroke(width = 1.0f)
    )

    // Fine Axis Hairlines (1px)
    val lineCol = theme.secondaryAccent.copy(alpha = 0.30f * alpha)
    drawLine(lineCol, Offset(cx - ringRadius - 32f, cy * 0.72f), Offset(cx - ringRadius + 14f, cy * 0.72f), strokeWidth = 1f)
    drawLine(lineCol, Offset(cx + ringRadius - 14f, cy * 0.72f), Offset(cx + ringRadius + 32f, cy * 0.72f), strokeWidth = 1f)
}

// ---------------------------------------------------------------------------
// 5. ACID PROTOCOL // BIOHAZARD (Radiação Gamma)
// ---------------------------------------------------------------------------
private fun DrawScope.drawAcidProtocol(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float, orbit: Float
) {
    // Toxic Gamma Radiation Flare
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.primaryAccent.copy(alpha = 0.32f * alpha * breath),
                theme.secondaryAccent.copy(alpha = 0.12f * alpha),
                Color.Transparent
            ),
            center = Offset(cx, cy * 0.65f),
            radius = w * 0.68f
        )
    )

    // Diagonal Quarantine Hazard Warning Stripes (Top & Bottom Rims)
    val stripeColor = theme.secondaryAccent.copy(alpha = 0.35f * alpha)
    for (i in -4..16) {
        val sx = i * 45f
        drawLine(stripeColor, Offset(sx, 0f), Offset(sx + 30f, 24f), strokeWidth = 8f)
        drawLine(stripeColor, Offset(sx, h - 24f), Offset(sx + 30f, h), strokeWidth = 8f)
    }

    // Radiation Trefoil Symbol & Concentric Geiger Rings
    val radRadius = w * 0.30f
    rotate(degrees = orbit * 0.8f, pivot = Offset(cx, cy * 0.65f)) {
        drawCircle(
            color = theme.primaryAccent.copy(alpha = 0.40f * alpha),
            radius = radRadius,
            center = Offset(cx, cy * 0.65f),
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), 0f))
        )
        // 3 Radiation Blades (120 deg apart)
        for (b in 0..2) {
            val startAng = b * 120f
            drawArc(
                color = theme.primaryAccent.copy(alpha = 0.28f * alpha),
                startAngle = startAng,
                sweepAngle = 60f,
                useCenter = true,
                topLeft = Offset(cx - radRadius * 0.85f, cy * 0.65f - radRadius * 0.85f),
                size = Size(radRadius * 1.7f, radRadius * 1.7f)
            )
        }
        drawCircle(theme.backgroundColor, radius = radRadius * 0.35f, center = Offset(cx, cy * 0.65f))
    }
}

// ---------------------------------------------------------------------------
// 6. ELDRITCH CATHEDRAL // BLOOD & ASH (Gótico Souls-like)
// ---------------------------------------------------------------------------
private fun DrawScope.drawEldritchCathedral(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float
) {
    // Blood Red Eclipse Moon
    val moonCenter = Offset(cx, h * 0.26f)
    val moonRadius = w * 0.28f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.primaryAccent.copy(alpha = 0.45f * alpha * breath),
                theme.secondaryAccent.copy(alpha = 0.15f * alpha),
                Color.Transparent
            ),
            center = moonCenter,
            radius = moonRadius * 1.6f
        )
    )
    drawCircle(theme.primaryAccent.copy(alpha = 0.65f * alpha), radius = moonRadius, center = moonCenter)

    // Gothic Pointed Ogival Arch Tracery Silhouette
    val archPath = Path().apply {
        moveTo(w * 0.15f, h * 0.90f)
        lineTo(w * 0.15f, h * 0.48f)
        cubicTo(w * 0.15f, h * 0.25f, cx, h * 0.15f, cx, h * 0.08f)
        cubicTo(cx, h * 0.15f, w * 0.85f, h * 0.25f, w * 0.85f, h * 0.48f)
        lineTo(w * 0.85f, h * 0.90f)
    }
    drawPath(archPath, theme.secondaryAccent.copy(alpha = 0.35f * alpha), style = Stroke(width = 2.2f))

    // Shattered Rose Window Circle inside arch
    drawCircle(
        color = theme.secondaryAccent.copy(alpha = 0.25f * alpha),
        radius = w * 0.22f,
        center = Offset(cx, h * 0.44f),
        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
    )
}

// ---------------------------------------------------------------------------
// 7. APOLLO-NASA 1970 // COCKPIT ANALÓGICO (Hardware Vintage)
// ---------------------------------------------------------------------------
private fun DrawScope.drawApolloCockpit(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float
) {
    // Amber Millimeter Graph Paper Grid
    val fineGridColor = theme.primaryAccent.copy(alpha = 0.12f * alpha)
    val boldGridColor = theme.primaryAccent.copy(alpha = 0.28f * alpha)

    for (gx in 0 until w.toInt() step 24) {
        val stroke = if (gx % 120 == 0) 1.5f else 0.7f
        val col = if (gx % 120 == 0) boldGridColor else fineGridColor
        drawLine(col, Offset(gx.toFloat(), 0f), Offset(gx.toFloat(), h), strokeWidth = stroke)
    }
    for (gy in 0 until h.toInt() step 24) {
        val stroke = if (gy % 120 == 0) 1.5f else 0.7f
        val col = if (gy % 120 == 0) boldGridColor else fineGridColor
        drawLine(col, Offset(0f, gy.toFloat()), Offset(w, gy.toFloat()), strokeWidth = stroke)
    }

    // Saturn V Orbital Trajectory Ellipse
    val trajColor = theme.secondaryAccent.copy(alpha = 0.38f * alpha * breath)
    drawOval(
        color = trajColor,
        topLeft = Offset(w * 0.10f, h * 0.20f),
        size = Size(w * 0.80f, h * 0.35f),
        style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f))
    )

    // Cockpit Gauge Bezel with Screw Rivets in 4 corners
    val rivetColor = theme.primaryAccent.copy(alpha = 0.55f * alpha)
    drawCircle(rivetColor, radius = 6f, center = Offset(24f, 24f))
    drawCircle(rivetColor, radius = 6f, center = Offset(w - 24f, 24f))
    drawCircle(rivetColor, radius = 6f, center = Offset(24f, h - 24f))
    drawCircle(rivetColor, radius = 6f, center = Offset(w - 24f, h - 24f))
}

// ---------------------------------------------------------------------------
// 8. NEO-KYOTO ZERO // LÂMINA DE CARBONO (Cyber-Shogunate)
// ---------------------------------------------------------------------------
private fun DrawScope.drawNeoKyotoKatana(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float, orbit: Float
) {
    // Twill Carbon-Fiber Texture Diagonals
    val carbonLine = Color(0xFF22222E).copy(alpha = 0.35f * alpha)
    for (d in -w.toInt() until (w + h).toInt() step 14) {
        drawLine(carbonLine, Offset(d.toFloat(), 0f), Offset(d.toFloat() + h, h), strokeWidth = 1.0f)
    }

    // Neon Katana Blade Slash Line
    val katanaGlow = theme.primaryAccent.copy(alpha = 0.65f * alpha * breath)
    val katanaCore = Color.White.copy(alpha = 0.90f * alpha)
    drawLine(katanaGlow, Offset(w * 0.05f, h * 0.20f), Offset(w * 0.95f, h * 0.75f), strokeWidth = 5.0f)
    drawLine(katanaCore, Offset(w * 0.05f, h * 0.20f), Offset(w * 0.95f, h * 0.75f), strokeWidth = 1.5f)

    // Japanese Cyber-Kanji Hexagon Emblem
    val hexRadius = w * 0.22f
    rotate(degrees = orbit * 0.3f, pivot = Offset(cx, cy * 0.55f)) {
        val hexPath = Path().apply {
            for (i in 0..5) {
                val a = (i * 60f) * (PI / 180f).toFloat()
                val x = cx + cos(a) * hexRadius
                val y = cy * 0.55f + sin(a) * hexRadius
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(hexPath, theme.secondaryAccent.copy(alpha = 0.30f * alpha), style = Stroke(width = 2f))
    }
}

// ---------------------------------------------------------------------------
// 9. WEYLAND-YUTANI // DEEP SPACE VESSEL (Sci-Fi Corp)
// ---------------------------------------------------------------------------
private fun DrawScope.drawWeylandDeepSpace(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float
) {
    // Airlock Pressurized Bulkhead Doors Frame
    val hullStroke = Stroke(width = 2.0f)
    val hullColor = theme.cardBorderColor.copy(alpha = 0.55f * alpha)

    drawRect(hullColor, Offset(w * 0.08f, h * 0.12f), Size(w * 0.84f, h * 0.76f), style = hullStroke)
    // Chamfered bulkhead corners
    drawLine(theme.secondaryAccent.copy(alpha = 0.6f * alpha), Offset(w * 0.08f, h * 0.18f), Offset(w * 0.14f, h * 0.12f), strokeWidth = 3f)
    drawLine(theme.secondaryAccent.copy(alpha = 0.6f * alpha), Offset(w * 0.92f, h * 0.18f), Offset(w * 0.86f, h * 0.12f), strokeWidth = 3f)

    // Technical Vector CRT Telemetry HUD Crosshairs
    val hudColor = theme.primaryAccent.copy(alpha = 0.40f * alpha * breath)
    drawCircle(hudColor, radius = w * 0.25f, center = Offset(cx, cy * 0.62f), style = Stroke(width = 1.2f))
    drawLine(hudColor, Offset(cx - w * 0.30f, cy * 0.62f), Offset(cx + w * 0.30f, cy * 0.62f), strokeWidth = 1f)
    drawLine(hudColor, Offset(cx, cy * 0.62f - w * 0.30f), Offset(cx, cy * 0.62f + w * 0.30f), strokeWidth = 1f)
}

// ---------------------------------------------------------------------------
// 10. SOVEREIGN ARCHON // OLYMPUS ASCENDED (Tier Supremo)
// ---------------------------------------------------------------------------
private fun DrawScope.drawSovereignArchon(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float, orbit: Float
) {
    // 1. Colossal Golden Starlight Nebula
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.primaryAccent.copy(alpha = 0.45f * alpha * breath),
                theme.secondaryAccent.copy(alpha = 0.18f * alpha),
                Color.Transparent
            ),
            center = Offset(cx, cy * 0.58f),
            radius = w * 0.85f
        )
    )

    // 2. Liquid Quartz Central Vertical Fissure
    val fissureGlow = theme.secondaryAccent.copy(alpha = 0.60f * alpha * breath)
    val fissureCore = Color.White.copy(alpha = 0.90f * alpha)
    drawLine(fissureGlow, Offset(cx, h * 0.10f), Offset(cx, h * 0.85f), strokeWidth = 4.0f)
    drawLine(fissureCore, Offset(cx, h * 0.10f), Offset(cx, h * 0.85f), strokeWidth = 1.5f)

    // 3. Double Golden Astrolabe Rings (Golden Ratio phi = 1.618)
    val outerR = w * 0.38f
    val innerR = outerR / 1.618f

    rotate(degrees = orbit, pivot = Offset(cx, cy * 0.58f)) {
        drawCircle(
            color = theme.primaryAccent.copy(alpha = 0.55f * alpha),
            radius = outerR,
            center = Offset(cx, cy * 0.58f),
            style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f))
        )
        // Cardinal Golden Ticks
        for (i in 0..3) {
            val ang = (i * 90f) * (PI / 180f).toFloat()
            val x1 = cx + cos(ang) * (outerR - 20f)
            val y1 = cy * 0.58f + sin(ang) * (outerR - 20f)
            val x2 = cx + cos(ang) * (outerR + 20f)
            val y2 = cy * 0.58f + sin(ang) * (outerR + 20f)
            drawLine(theme.primaryAccent.copy(alpha = 0.70f * alpha), Offset(x1, y1), Offset(x2, y2), strokeWidth = 2.0f)
        }
    }

    rotate(degrees = -orbit * 0.8f, pivot = Offset(cx, cy * 0.58f)) {
        drawCircle(
            color = theme.secondaryAccent.copy(alpha = 0.40f * alpha),
            radius = innerR,
            center = Offset(cx, cy * 0.58f),
            style = Stroke(width = 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f))
        )
    }
}

// ---------------------------------------------------------------------------
// Fallback: Classic Cybersigil
// ---------------------------------------------------------------------------
private fun DrawScope.drawClassicCybersigil(
    w: Float, h: Float, cx: Float, cy: Float,
    theme: VaultThemeConfig, alpha: Float, breath: Float, orbit: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                theme.primaryAccent.copy(alpha = 0.20f * alpha * breath),
                theme.secondaryAccent.copy(alpha = 0.06f * alpha),
                Color.Transparent
            ),
            center = Offset(cx, cy * 0.55f),
            radius = w * 0.65f
        )
    )
    rotate(degrees = orbit, pivot = Offset(cx, cy * 0.55f)) {
        drawCircle(
            color = theme.primaryAccent.copy(alpha = 0.28f * alpha),
            radius = w * 0.35f,
            center = Offset(cx, cy * 0.55f),
            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 14f), 0f))
        )
    }
}
