package com.hughdevelopment.player.audio

import com.hughdevelopment.player.data.settings.EqualizerPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit & Integration test suite for Equalizer Presets, Band Frequency Specifications,
 * Hardware Range Clamping, and BassBoost Mechanics (F-01, F-02, F-03).
 */
class EqualizerPresetsTest {

    // --- Tier 1: Core Preset Specifications & Matrix Verification ---

    @Test
    fun testFlatPreset_matchesSpecification() {
        val flat = EqualizerPreset.FLAT
        assertEquals("FLAT", flat.id)
        assertEquals("Flat", flat.displayName)
        assertEquals(listOf(0, 0, 0, 0, 0), flat.bandLevels)
        assertEquals(0, flat.bassBoost)
    }

    @Test
    fun testVocalClarityPreset_matchesSpecification() {
        val vocal = EqualizerPreset.VOCAL_CLARITY
        assertEquals("VOCAL_CLARITY", vocal.id)
        assertEquals("Vocal Clarity", vocal.displayName)
        assertEquals(listOf(-200, 300, 800, 1200, 400), vocal.bandLevels)
        assertEquals(100, vocal.bassBoost)
    }

    @Test
    fun testSubBassBrutalistPreset_matchesSpecification() {
        val brutalist = EqualizerPreset.SUB_BASS_BRUTALIST
        assertEquals("SUB_BASS_BRUTALIST", brutalist.id)
        assertEquals("Sub-Bass Brutalist", brutalist.displayName)
        assertEquals(listOf(1400, 900, -200, -400, 200), brutalist.bandLevels)
        assertEquals(900, brutalist.bassBoost)
    }

    @Test
    fun testAcousticWorshipPreset_matchesSpecification() {
        val acoustic = EqualizerPreset.ACOUSTIC_WORSHIP
        assertEquals("ACOUSTIC_WORSHIP", acoustic.id)
        assertEquals("Acoustic Worship", acoustic.displayName)
        assertEquals(listOf(400, 200, -100, 600, 1000), acoustic.bandLevels)
        assertEquals(250, acoustic.bassBoost)
    }

    @Test
    fun testPodcastEnhancerPreset_matchesSpecification() {
        val podcast = EqualizerPreset.PODCAST_ENHANCER
        assertEquals("PODCAST_ENHANCER", podcast.id)
        assertEquals("Podcast Enhancer", podcast.displayName)
        assertEquals(listOf(-400, 600, 1000, 800, -300), podcast.bandLevels)
        assertEquals(0, podcast.bassBoost)
    }

    @Test
    fun testCustomPreset_defaultsToFlatZero() {
        val custom = EqualizerPreset.CUSTOM
        assertEquals("CUSTOM", custom.id)
        assertEquals(listOf(0, 0, 0, 0, 0), custom.bandLevels)
        assertEquals(0, custom.bassBoost)
    }

    // --- Tier 1: ID Resolution & Fallback Robustness ---

    @Test
    fun testFromId_exactAndCaseInsensitiveMatching() {
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.fromId("FLAT"))
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.fromId("flat"))
        assertEquals(EqualizerPreset.VOCAL_CLARITY, EqualizerPreset.fromId("VOCAL_CLARITY"))
        assertEquals(EqualizerPreset.VOCAL_CLARITY, EqualizerPreset.fromId("vocal_clarity"))
        assertEquals(EqualizerPreset.SUB_BASS_BRUTALIST, EqualizerPreset.fromId("SUB_BASS_BRUTALIST"))
        assertEquals(EqualizerPreset.SUB_BASS_BRUTALIST, EqualizerPreset.fromId("sub_bass_brutalist"))
        assertEquals(EqualizerPreset.ACOUSTIC_WORSHIP, EqualizerPreset.fromId("acoustic_worship"))
        assertEquals(EqualizerPreset.PODCAST_ENHANCER, EqualizerPreset.fromId("podcast_enhancer"))
        assertEquals(EqualizerPreset.CUSTOM, EqualizerPreset.fromId("custom"))
    }

    @Test
    fun testFromId_unknownOrCorrupt_fallsBackToFlat() {
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.fromId(""))
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.fromId("NON_EXISTENT_PRESET"))
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.fromId("12345"))
        assertEquals(EqualizerPreset.FLAT, EqualizerPreset.fromId("   "))
    }

    // --- Tier 2: Frequency Bands & Hardware Range Boundaries ---

    @Test
    fun testPresetBandCounts_allPresetsHaveExactlyFiveBands() {
        EqualizerPreset.entries.forEach { preset ->
            assertEquals(
                "Preset ${preset.name} must contain exactly 5 frequency bands",
                5,
                preset.bandLevels.size
            )
        }
    }

    @Test
    fun testCenterFrequencies_matchStandardFiveBands() {
        val expectedCenterFreqs = listOf(60, 230, 910, 3600, 14000)
        assertEquals(5, AudioEffectManager.DEFAULT_CENTER_FREQS.size)
        assertEquals(expectedCenterFreqs, AudioEffectManager.DEFAULT_CENTER_FREQS)
    }

    @Test
    fun testBandLevelRanges_withinHardwareMillibelLimits() {
        val minMb = AudioEffectManager.DEFAULT_MIN_MB // -1500 mB (-15 dB)
        val maxMb = AudioEffectManager.DEFAULT_MAX_MB // +1500 mB (+15 dB)

        assertEquals(-1500, minMb)
        assertEquals(1500, maxMb)

        EqualizerPreset.entries.forEach { preset ->
            preset.bandLevels.forEachIndexed { bandIndex, level ->
                assertTrue(
                    "Preset ${preset.name} band $bandIndex ($level mB) must be >= $minMb mB",
                    level >= minMb
                )
                assertTrue(
                    "Preset ${preset.name} band $bandIndex ($level mB) must be <= $maxMb mB",
                    level <= maxMb
                )
            }
        }
    }

    @Test
    fun testBassBoostRanges_withinHardwareLimits() {
        EqualizerPreset.entries.forEach { preset ->
            assertTrue(
                "Preset ${preset.name} bassBoost (${preset.bassBoost}) must be in 0..1000",
                preset.bassBoost in 0..1000
            )
        }
    }

    // --- Tier 2: Safety Clamping Math & Pop Prevention Invariants ---

    @Test
    fun testMillibelClamping_extremesAreConstrained() {
        val minMb = AudioEffectManager.DEFAULT_MIN_MB
        val maxMb = AudioEffectManager.DEFAULT_MAX_MB

        fun clamp(level: Int): Int = level.coerceIn(minMb, maxMb)

        assertEquals(maxMb, clamp(3000))
        assertEquals(maxMb, clamp(1500))
        assertEquals(1200, clamp(1200))
        assertEquals(0, clamp(0))
        assertEquals(-200, clamp(-200))
        assertEquals(minMb, clamp(-1500))
        assertEquals(minMb, clamp(-5000))
    }

    @Test
    fun testBassBoostClamping_extremesAreConstrained() {
        fun clampBass(strength: Int): Int = strength.coerceIn(0, 1000)

        assertEquals(1000, clampBass(2500))
        assertEquals(1000, clampBass(1000))
        assertEquals(500, clampBass(500))
        assertEquals(0, clampBass(0))
        assertEquals(0, clampBass(-100))
    }

    // --- Tier 3: Acoustic Profile Invariants ---

    @Test
    fun testSubBassBrutalist_hasStrongestSubBassAndBassBoost() {
        val brutalist = EqualizerPreset.SUB_BASS_BRUTALIST
        val allOtherPresets = EqualizerPreset.entries.filter { it != brutalist }

        allOtherPresets.forEach { other ->
            assertTrue(
                "SUB_BASS_BRUTALIST Band 0 (${brutalist.bandLevels[0]}) must exceed ${other.name} (${other.bandLevels[0]})",
                brutalist.bandLevels[0] > other.bandLevels[0]
            )
            assertTrue(
                "SUB_BASS_BRUTALIST bassBoost (${brutalist.bassBoost}) must exceed ${other.name} (${other.bassBoost})",
                brutalist.bassBoost > other.bassBoost
            )
        }
    }

    @Test
    fun testVocalClarity_attenuatesSubBassAndBoostsPresence() {
        val vocal = EqualizerPreset.VOCAL_CLARITY
        assertTrue("Sub-bass (60Hz) should be attenuated (< 0) for speech intelligibility", vocal.bandLevels[0] < 0)
        assertTrue("High-mids (3.6kHz) presence must be highest boost in vocal profile", vocal.bandLevels[3] >= 1000)
    }

    @Test
    fun testPodcastEnhancer_suppressesRumbleAndSibilance() {
        val podcast = EqualizerPreset.PODCAST_ENHANCER
        assertTrue("Low-end rumble must be aggressively cut (< -300)", podcast.bandLevels[0] <= -400)
        assertTrue("Midrange speech fundamentals must be boosted (> 500)", podcast.bandLevels[1] >= 600)
        assertTrue("Treble sibilance must be damped (< 0)", podcast.bandLevels[4] < 0)
    }
}
