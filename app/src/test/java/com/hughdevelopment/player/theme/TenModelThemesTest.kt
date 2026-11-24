package com.hughdevelopment.player.theme

import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.ui.theme.AcidGreen
import com.hughdevelopment.player.ui.theme.ApolloAmber
import com.hughdevelopment.player.ui.theme.ArchonGold
import com.hughdevelopment.player.ui.theme.DogeYellow
import com.hughdevelopment.player.ui.theme.EldritchBlood
import com.hughdevelopment.player.ui.theme.KatanaCrimson
import com.hughdevelopment.player.ui.theme.NullWhite
import com.hughdevelopment.player.ui.theme.TerraEmerald
import com.hughdevelopment.player.ui.theme.VaporMagenta
import com.hughdevelopment.player.ui.theme.VaultFontCategory
import com.hughdevelopment.player.ui.theme.VaultLayoutShape
import com.hughdevelopment.player.ui.theme.VaultThemeId
import com.hughdevelopment.player.ui.theme.VaultThemes
import com.hughdevelopment.player.ui.theme.VaultVisualizerStyle
import com.hughdevelopment.player.ui.theme.VaultWallpaperStyle
import com.hughdevelopment.player.ui.theme.WeylandCyan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TenModelThemesTest
 *
 * Comprehensive unit test verification for all 10 curated Pantheon Store themes.
 * Validates IDs, aliases, geometric corner radii, visualizer styles, wallpaper styles,
 * and custom Hermes persona greetings.
 */
class TenModelThemesTest {

    @Test
    fun testAllThemes_containsAllTenCuratedThemes() {
        val allThemes = VaultThemes.AllThemes
        assertEquals(16, allThemes.size)

        assertTrue("Must contain SovereignArchon", allThemes.contains(VaultThemes.SovereignArchon))
        assertTrue("Must contain DogeSovereign", allThemes.contains(VaultThemes.DogeSovereign))
        assertTrue("Must contain Win95Vaporwave", allThemes.contains(VaultThemes.Win95Vaporwave))
        assertTrue("Must contain TerraBrasilis", allThemes.contains(VaultThemes.TerraBrasilis))
        assertTrue("Must contain MonolithNull", allThemes.contains(VaultThemes.MonolithNull))
        assertTrue("Must contain AcidProtocol", allThemes.contains(VaultThemes.AcidProtocol))
        assertTrue("Must contain EldritchCathedral", allThemes.contains(VaultThemes.EldritchCathedral))
        assertTrue("Must contain Apollo1970", allThemes.contains(VaultThemes.Apollo1970))
        assertTrue("Must contain NeoKyotoZero", allThemes.contains(VaultThemes.NeoKyotoZero))
        assertTrue("Must contain WeylandDeepSpace", allThemes.contains(VaultThemes.WeylandDeepSpace))
    }

    @Test
    fun testDogeSovereign_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("DOGE_SOVEREIGN")
        assertEquals(VaultThemeId.DOGE_SOVEREIGN, theme.id)
        assertEquals(DogeYellow, theme.primaryAccent)
        assertEquals(22.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.DOGE_MEME_ALTAR, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.DOGE_PAWS_BUBBLE, theme.visualizerStyle)
        assertEquals("🐕", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("lossless"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("doge"))
        assertEquals(theme, VaultThemes.fromId("shiba"))
    }

    @Test
    fun testWin95Vaporwave_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("WIN95_VAPORWAVE")
        assertEquals(VaultThemeId.WIN95_VAPORWAVE, theme.id)
        assertEquals(VaporMagenta, theme.primaryAccent)
        assertEquals(2.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.WIN95_VAPORWAVE_CRT, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.WIN95_PIXEL_BLOCKS, theme.visualizerStyle)
        assertEquals("📺", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("DRIVE A:"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("win95"))
        assertEquals(theme, VaultThemes.fromId("vaporwave"))
    }

    @Test
    fun testTerraBrasilis_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("TERRA_BRASILIS")
        assertEquals(VaultThemeId.TERRA_BRASILIS, theme.id)
        assertEquals(TerraEmerald, theme.primaryAccent)
        assertEquals(8.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.TERRA_BRASILIS_CONCRETE, theme.wallpaperStyle)
        assertEquals("🇧🇷", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("Concreto armado"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("terra"))
        assertEquals(theme, VaultThemes.fromId("brasilis"))
    }

    @Test
    fun testMonolithNull_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("MONOLITH_NULL")
        assertEquals(VaultThemeId.MONOLITH_NULL, theme.id)
        assertEquals(NullWhite, theme.primaryAccent)
        assertEquals(0.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.MONOLITH_NULL_ECLIPSE, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.NULL_HAIRLINE, theme.visualizerStyle)
        assertEquals("🕳️", theme.iconEmoji)
        // Alias check
        assertEquals(theme, VaultThemes.fromId("null"))
        assertEquals(theme, VaultThemes.fromId("vantablack"))
    }

    @Test
    fun testAcidProtocol_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("ACID_PROTOCOL")
        assertEquals(VaultThemeId.ACID_PROTOCOL, theme.id)
        assertEquals(AcidGreen, theme.primaryAccent)
        assertEquals(4.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.ACID_HAZARD_RADIATION, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.ACID_GEIGER_SPIKES, theme.visualizerStyle)
        assertEquals("☣️", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("RADIAÇÃO"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("acid"))
        assertEquals(theme, VaultThemes.fromId("biohazard"))
    }

    @Test
    fun testEldritchCathedral_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("ELDRITCH_CATHEDRAL")
        assertEquals(VaultThemeId.ELDRITCH_CATHEDRAL, theme.id)
        assertEquals(EldritchBlood, theme.primaryAccent)
        assertEquals(6.dp, theme.cardCornerRadius)
        assertEquals(VaultFontCategory.SERIF, theme.fontCategory)
        assertEquals(VaultWallpaperStyle.ELDRITCH_GOTHIC_CATHEDRAL, theme.wallpaperStyle)
        assertEquals("⚔️", theme.iconEmoji)
        // Alias check
        assertEquals(theme, VaultThemes.fromId("gothic"))
        assertEquals(theme, VaultThemes.fromId("eldritch"))
    }

    @Test
    fun testApollo1970_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("APOLLO_1970")
        assertEquals(VaultThemeId.APOLLO_1970, theme.id)
        assertEquals(ApolloAmber, theme.primaryAccent)
        assertEquals(3.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.APOLLO_ANALOG_COCKPIT, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.APOLLO_VU_METERS, theme.visualizerStyle)
        assertEquals("🚀", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("Houston"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("apollo"))
        assertEquals(theme, VaultThemes.fromId("nasa"))
    }

    @Test
    fun testNeoKyotoZero_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("NEO_KYOTO_ZERO")
        assertEquals(VaultThemeId.NEO_KYOTO_ZERO, theme.id)
        assertEquals(KatanaCrimson, theme.primaryAccent)
        assertEquals(4.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.NEO_KYOTO_CARBON_KATANA, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.KATANA_SLICES, theme.visualizerStyle)
        assertEquals("⛩️", theme.iconEmoji)
        // Alias check
        assertEquals(theme, VaultThemes.fromId("kyoto"))
        assertEquals(theme, VaultThemes.fromId("katana"))
    }

    @Test
    fun testWeylandDeepSpace_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("WEYLAND_DEEP_SPACE")
        assertEquals(VaultThemeId.WEYLAND_DEEP_SPACE, theme.id)
        assertEquals(WeylandCyan, theme.primaryAccent)
        assertEquals(4.dp, theme.cardCornerRadius)
        assertEquals(VaultWallpaperStyle.WEYLAND_DEEP_SPACE_HUD, theme.wallpaperStyle)
        assertEquals("🪐", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("937"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("weyland"))
        assertEquals(theme, VaultThemes.fromId("alien"))
    }

    @Test
    fun testSovereignArchon_resolutionAndSpecs() {
        val theme = VaultThemes.fromId("SOVEREIGN_ARCHON")
        assertEquals(VaultThemeId.SOVEREIGN_ARCHON, theme.id)
        assertEquals(ArchonGold, theme.primaryAccent)
        assertEquals(18.dp, theme.cardCornerRadius)
        assertEquals(VaultFontCategory.SERIF, theme.fontCategory)
        assertEquals(VaultWallpaperStyle.SOVEREIGN_ARCHON_CELESTIAL, theme.wallpaperStyle)
        assertEquals(VaultVisualizerStyle.ARCHON_GOLDEN_HALO, theme.visualizerStyle)
        assertEquals("👑", theme.iconEmoji)
        assertTrue(theme.lexicon.greetingPhrase.contains("Arconte Supremo"))
        // Alias check
        assertEquals(theme, VaultThemes.fromId("archon"))
        assertEquals(theme, VaultThemes.fromId("pica"))
        assertEquals(theme, VaultThemes.fromId("supremo"))
    }
}
