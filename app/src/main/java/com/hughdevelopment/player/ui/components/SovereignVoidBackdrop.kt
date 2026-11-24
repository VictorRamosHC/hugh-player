package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.VaultWallpaperStyle

/**
 * SovereignVoidBackdrop // Divine Obsidian Void, Radiant Auras & Thematic Wallpapers
 *
 * Renders the theme-specific procedural wallpaper (Doge Altar, Win95 CRT, Terra Brasilis,
 * Monolith Null, Acid Protocol, Eldritch Cathedral, Apollo Cockpit, Neo-Kyoto Katana,
 * Weyland Deep Space, Sovereign Archon) or classic celestial cybersigil.
 */
@Composable
fun SovereignVoidBackdrop(
    modifier: Modifier = Modifier,
    showCybersigil: Boolean = true,
    auraScale: Float = 1.0f,
    centralizeAura: Boolean = false
) {
    val theme = LocalVaultTheme.current

    Box(modifier = modifier.fillMaxSize()) {
        VaultThemeWallpaperBackdrop(
            modifier = Modifier.fillMaxSize(),
            wallpaperAlpha = auraScale * 0.50f,
            theme = theme
        )

        // Corner cyber HUD sigils for classic and sacred themes
        if (showCybersigil && (theme.wallpaperStyle == VaultWallpaperStyle.SACRED_CYBERSIGIL || theme.wallpaperStyle == VaultWallpaperStyle.SOVEREIGN_ARCHON_CELESTIAL)) {
            CybersigilBackground(
                modifier = Modifier.fillMaxSize(),
                showHalo = false,
                isRotating = true
            )
        }
    }
}
