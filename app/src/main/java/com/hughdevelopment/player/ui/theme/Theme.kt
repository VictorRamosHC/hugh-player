package com.hughdevelopment.player.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun HughPlayerTheme(
    themeConfig: VaultThemeConfig = VaultThemes.Classic,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = themeConfig.backgroundColor.toArgb()
            window.navigationBarColor = themeConfig.backgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    val dynamicColorScheme = darkColorScheme(
        primary = themeConfig.primaryAccent,
        secondary = themeConfig.secondaryAccent,
        tertiary = themeConfig.tertiaryAccent,
        background = themeConfig.backgroundColor,
        surface = themeConfig.surfaceColor,
        onPrimary = themeConfig.backgroundColor,
        onSecondary = themeConfig.backgroundColor,
        onTertiary = themeConfig.backgroundColor,
        onBackground = themeConfig.textPrimary,
        onSurface = themeConfig.textPrimary,
        surfaceVariant = themeConfig.surfaceColor,
        outline = themeConfig.cardBorderColor
    )

    CompositionLocalProvider(
        LocalVaultTheme provides themeConfig
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            content = content
        )
    }
}
