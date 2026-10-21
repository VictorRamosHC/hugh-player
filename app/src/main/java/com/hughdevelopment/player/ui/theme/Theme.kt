package com.hughdevelopment.player.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SigilWhite,
    secondary = CyberAmber,
    tertiary = HermeticCyan,
    background = VoidBlack,
    surface = CharcoalSurface,
    onPrimary = VoidBlack,
    onSecondary = VoidBlack,
    onTertiary = VoidBlack,
    onBackground = SigilSilver,
    onSurface = SigilSilver,
    surfaceVariant = DitherDark,
    outline = WireframeBorder
)

@Composable
fun HughPlayerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VoidBlack.toArgb()
            window.navigationBarColor = VoidBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
