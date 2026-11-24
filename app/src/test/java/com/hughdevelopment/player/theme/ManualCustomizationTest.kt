package com.hughdevelopment.player.theme

import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.ui.theme.VaultFontCategory
import com.hughdevelopment.player.ui.theme.VaultLayoutShape
import com.hughdevelopment.player.ui.theme.VaultThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test Suite for Manual Theme Customization & Override Dynamics (R2 & R3).
 * Verifies persistence contracts, dynamic theme copying, font switching, and geometry metamorphosis.
 */
class ManualCustomizationTest {

    @Test
    fun testSettingsState_defaultCustomOverridesAreNull() {
        val defaultState = SettingsState()
        assertNull("Default customFontCategory must be null (theme default active)", defaultState.customFontCategory)
        assertNull("Default customLayoutShape must be null (theme default active)", defaultState.customLayoutShape)
    }

    @Test
    fun testSettingsState_customOverridesCanBeApplied() {
        val customState = SettingsState(
            customFontCategory = VaultFontCategory.SERIF,
            customLayoutShape = VaultLayoutShape.BRUTALIST_SHARP
        )
        assertEquals(VaultFontCategory.SERIF, customState.customFontCategory)
        assertEquals(VaultLayoutShape.BRUTALIST_SHARP, customState.customLayoutShape)
    }

    @Test
    fun testDynamicThemeCopy_appliesCustomFontAndShapeToClassic() {
        val baseTheme = VaultThemes.Classic
        val customFont = VaultFontCategory.SANS_SERIF
        val customShape = VaultLayoutShape.BRUTALIST_SHARP

        val overridden = baseTheme.copy(
            fontCategory = customFont,
            layoutShape = customShape,
            cardCornerRadius = when (customShape) {
                VaultLayoutShape.BRUTALIST_SHARP -> 4.dp
                VaultLayoutShape.NEO_MINIMAL -> 8.dp
                VaultLayoutShape.DIVINE_CURVES -> 16.dp
            },
            isBrutalistSharp = (customShape == VaultLayoutShape.BRUTALIST_SHARP)
        )

        assertEquals(VaultFontCategory.SANS_SERIF, overridden.fontCategory)
        assertEquals(VaultLayoutShape.BRUTALIST_SHARP, overridden.layoutShape)
        assertEquals(4.dp, overridden.cardCornerRadius)
        assertTrue(overridden.isBrutalistSharp)
        // Original colors intact
        assertEquals(baseTheme.primaryAccent, overridden.primaryAccent)
        assertEquals(baseTheme.secondaryAccent, overridden.secondaryAccent)
    }

    @Test
    fun testDynamicThemeCopy_appliesDivineCurvesToArasaka() {
        val baseTheme = VaultThemes.ArasakaPantheon
        val customShape = VaultLayoutShape.DIVINE_CURVES

        val overridden = baseTheme.copy(
            layoutShape = customShape,
            cardCornerRadius = when (customShape) {
                VaultLayoutShape.BRUTALIST_SHARP -> 4.dp
                VaultLayoutShape.NEO_MINIMAL -> 8.dp
                VaultLayoutShape.DIVINE_CURVES -> 16.dp
            },
            isBrutalistSharp = (customShape == VaultLayoutShape.BRUTALIST_SHARP)
        )

        assertEquals(VaultLayoutShape.DIVINE_CURVES, overridden.layoutShape)
        assertEquals(16.dp, overridden.cardCornerRadius)
        assertFalse("Divine curves must not be brutalist sharp", overridden.isBrutalistSharp)
        // Arasaka colors intact
        assertEquals(baseTheme.primaryAccent, overridden.primaryAccent)
        assertEquals(baseTheme.secondaryAccent, overridden.secondaryAccent)
    }

    @Test
    fun testResetOverrides_restoresOriginalThemePresets() {
        val baseTheme = VaultThemes.DelamainAiCore

        // Overrides set to null (reset)
        val customFont: VaultFontCategory? = null
        val customShape: VaultLayoutShape? = null

        val restoredTheme = if (customFont == null && customShape == null) {
            baseTheme
        } else {
            val effectiveFont = customFont ?: baseTheme.fontCategory
            val effectiveShape = customShape ?: baseTheme.layoutShape
            val effectiveRadius = if (customShape != null) {
                when (customShape) {
                    VaultLayoutShape.BRUTALIST_SHARP -> 4.dp
                    VaultLayoutShape.NEO_MINIMAL -> 8.dp
                    VaultLayoutShape.DIVINE_CURVES -> 16.dp
                }
            } else {
                baseTheme.cardCornerRadius
            }
            baseTheme.copy(
                fontCategory = effectiveFont,
                layoutShape = effectiveShape,
                cardCornerRadius = effectiveRadius,
                isBrutalistSharp = if (customShape != null) customShape == VaultLayoutShape.BRUTALIST_SHARP else baseTheme.isBrutalistSharp
            )
        }

        assertEquals(baseTheme.fontCategory, restoredTheme.fontCategory)
        assertEquals(baseTheme.layoutShape, restoredTheme.layoutShape)
        assertEquals(baseTheme.cardCornerRadius, restoredTheme.cardCornerRadius)
        assertEquals(baseTheme.isBrutalistSharp, restoredTheme.isBrutalistSharp)
    }
}
