package com.hughdevelopment.player.v170

import androidx.compose.ui.graphics.Color
import com.hughdevelopment.player.R
import com.hughdevelopment.player.ui.screens.getPlaylistCoverResource
import com.hughdevelopment.player.ui.theme.ArasakaCrimson
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.DelamainCyan
import com.hughdevelopment.player.ui.theme.VaultThemeId
import com.hughdevelopment.player.ui.theme.VaultThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verification Test Suite for Aether Vault v1.7.0 Features and Aesthetic Invariants
 */
class VaultV170FeaturesTest {

    @Test
    fun testThemes_containValidPaletteTokens() {
        val classic = VaultThemes.Classic
        assertNotNull(classic)
        assertEquals(VaultThemeId.AETHER_CLASSIC, classic.id)

        val arasaka = VaultThemes.ArasakaPantheon
        assertNotNull(arasaka)
        assertEquals(VaultThemeId.ARASAKA_PANTHEON, arasaka.id)
        assertEquals(ArasakaCrimson, arasaka.primaryAccent)

        val delamain = VaultThemes.DelamainAiCore
        assertNotNull(delamain)
        assertEquals(VaultThemeId.DELAMAIN_AI_CORE, delamain.id)
        assertEquals(DelamainCyan, delamain.primaryAccent)
    }

    @Test
    fun testAllThemes_registeredInAllThemesList() {
        assertEquals(3, VaultThemes.AllThemes.size)
        assertTrue(VaultThemes.AllThemes.contains(VaultThemes.Classic))
        assertTrue(VaultThemes.AllThemes.contains(VaultThemes.ArasakaPantheon))
        assertTrue(VaultThemes.AllThemes.contains(VaultThemes.DelamainAiCore))
    }

    @Test
    fun testFromId_resolvesThemesCorrectly() {
        assertEquals(VaultThemes.Classic, VaultThemes.fromId("AETHER_CLASSIC"))
        assertEquals(VaultThemes.ArasakaPantheon, VaultThemes.fromId("ARASAKA_PANTHEON"))
        assertEquals(VaultThemes.DelamainAiCore, VaultThemes.fromId("DELAMAIN_AI_CORE"))
        assertEquals(VaultThemes.DelamainAiCore, VaultThemes.fromId("delamain"))
        // Fallback
        assertEquals(VaultThemes.Classic, VaultThemes.fromId("UNKNOWN_THEME"))
    }

    @Test
    fun testPlaylistCoverResolver_normalizedCategories() {
        // Test diacritic and accent-insensitivity
        assertEquals(
            R.drawable.cover_christian_vocal,
            getPlaylistCoverResource("Qualquer Nome", "CRISTÃS (CANTADO)")
        )
        assertEquals(
            R.drawable.cover_christian_playback,
            getPlaylistCoverResource("Qualquer Nome", "CRISTÃS (PLAYBACK)")
        )
        assertEquals(
            R.drawable.cover_daily_music,
            getPlaylistCoverResource("Qualquer Nome", "DIA A DIA")
        )
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Qualquer Nome", "REUNIÕES")
        )
    }

    @Test
    fun testCyberAmber_hexRepresentation() {
        // Solar CyberAmber #F59E0B
        assertEquals(0xF5 / 255f, CyberAmber.red, 0.01f)
        assertEquals(0x9E / 255f, CyberAmber.green, 0.01f)
        assertEquals(0x0B / 255f, CyberAmber.blue, 0.01f)
    }
}
