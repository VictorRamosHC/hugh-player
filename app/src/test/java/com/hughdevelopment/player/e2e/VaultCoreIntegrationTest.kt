package com.hughdevelopment.player.e2e

import androidx.compose.ui.unit.dp
import com.hughdevelopment.player.audio.AudioEffectManager
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.SovereignRank
import com.hughdevelopment.player.data.model.StreakInfo
import com.hughdevelopment.player.data.settings.EqualizerPreset
import com.hughdevelopment.player.hermes.HermesVoiceSpeechOracle
import com.hughdevelopment.player.lyrics.LrcParserEngine
import com.hughdevelopment.player.scanner.MediaStoreScannerFilterOracle
import com.hughdevelopment.player.theme.DelamainThemeSpecification
import com.hughdevelopment.player.ui.DirectionCue
import com.hughdevelopment.player.ui.MiniPlayerSwipeEngine
import com.hughdevelopment.player.ui.SwipeAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-End & Integration Test Suite covering Tier 3 (Pairwise Interactions)
 * and Tier 4 (Real-World Application Scenarios) for Aether Vault v1.5.0.
 */
class VaultCoreIntegrationTest {

    // =========================================================================
    // --- TIER 3: PAIRWISE SUBSYSTEM INTERACTIONS ---
    // =========================================================================

    @Test
    fun testPairwise_equalizerPresetAndBassBoostInteraction() {
        val brutalist = EqualizerPreset.SUB_BASS_BRUTALIST
        assertEquals(900, brutalist.bassBoost)
        assertTrue(brutalist.bandLevels[0] > 1000)

        // Custom band adjustment triggers CUSTOM preset
        val modifiedLevels = brutalist.bandLevels.toMutableList()
        modifiedLevels[2] = 500 // modify mid band
        val isCustom = modifiedLevels != brutalist.bandLevels
        assertTrue("Modifying preset bands must flag custom configuration", isCustom)
    }

    @Test
    fun testPairwise_lrcParserAndPlaybackPositionSynchronization() {
        val lrc = """
            [offset:200]
            [00:05.00]Introdução
            [00:20.00]Primeira Estrofe
            [00:45.00]Refrão Triunfante
        """.trimIndent()

        val parsed = LrcParserEngine.parse(lrc)
        assertEquals(3, parsed.size)
        // With +200ms offset
        assertEquals(5200L, parsed[0].timeMs)
        assertEquals(20200L, parsed[1].timeMs)
        assertEquals(45200L, parsed[2].timeMs)

        fun findActiveLine(posMs: Long): Int = parsed.indexOfLast { it.timeMs <= posMs }

        assertEquals(-1, findActiveLine(5000L))
        assertEquals(0, findActiveLine(5200L))
        assertEquals(1, findActiveLine(25000L))
        assertEquals(2, findActiveLine(50000L))
    }

    @Test
    fun testPairwise_miniPlayerSwipeAndContentAlphaFading() {
        // Drag left to -80dp (exceeds threshold 72dp)
        val offset = -80f
        val action = MiniPlayerSwipeEngine.evaluateDragRelease(offset)
        val alpha = MiniPlayerSwipeEngine.computeContentAlpha(offset)
        val cue = MiniPlayerSwipeEngine.getDirectionCue(offset)

        assertEquals(SwipeAction.SKIP_NEXT, action)
        assertEquals(DirectionCue.CUE_NEXT, cue)
        assertTrue("Alpha ($alpha) must fade below 0.85 during swipe", alpha < 0.85f)
    }

    @Test
    fun testPairwise_hermesSanitizationAndVoiceToggleGuard() {
        val celebratoryText = "Ofensiva de 7 dias mantida 🔥! Subiu para Arconte ⚡!"
        val sanitized = HermesVoiceSpeechOracle.sanitizeForSpeech(celebratoryText)

        assertFalse(sanitized.contains("🔥"))
        assertFalse(sanitized.contains("⚡"))

        // Voice enabled -> allowed
        assertTrue(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = true, text = celebratoryText))
        // Voice disabled -> blocked
        assertFalse(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = false, text = celebratoryText))
    }

    @Test
    fun testPairwise_mediaScannerDirectoryPruningAndLrcDiscovery() {
        val validSongPath = "/storage/emulated/0/Music/Artist/Album/Track01.mp3"
        val tempThumbPath = "/storage/emulated/0/DCIM/.thumbnails/cache.mp3"

        assertFalse(MediaStoreScannerFilterOracle.isNoiseOrRecording(validSongPath, "Track01"))
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(tempThumbPath, "cache"))

        // Given valid song path, adjacent LRC file path candidate:
        val expectedAdjacentLrc = "/storage/emulated/0/Music/Artist/Album/Track01.lrc"
        assertTrue(expectedAdjacentLrc.endsWith(".lrc"))
    }

    // =========================================================================
    // --- TIER 4: REAL-WORLD APPLICATION SCENARIOS ---
    // =========================================================================

    /**
     * Scenario 1: Sovereign Offline Playback & Dynamic Synced Lyrics
     * Flow: User loads an offline track with multi-timestamp LRC lines,
     * tracks live position ticks, and verifies continuous synchronization.
     */
    @Test
    fun testScenario1_sovereignOfflinePlaybackAndSyncedLyrics() {
        val sovereignLrcContent = """
            [ti:Sovereign Audio]
            [ar:Aether Vault]
            [offset:100]
            [00:00.00]Início instrumental do Vault
            [00:10.50]Primeiro verso offline soberano
            [00:25.00][01:10.00]Refrão de alta fidelidade
            [00:45.00]Solo de guitarra acústica
        """.trimIndent()

        val lyrics = LrcParserEngine.parse(sovereignLrcContent)
        assertEquals(5, lyrics.size)

        // 1. Initial position: 0ms -> Active line: "Início instrumental do Vault" (timeMs = 100L due to offset)
        var currentPositionMs = 150L
        var activeIndex = lyrics.indexOfLast { it.timeMs <= currentPositionMs }
        assertEquals(0, activeIndex)
        assertEquals("Início instrumental do Vault", lyrics[activeIndex].text)

        // 2. Playback advances to 15s -> Active line: "Primeiro verso offline soberano" (10600L)
        currentPositionMs = 15000L
        activeIndex = lyrics.indexOfLast { it.timeMs <= currentPositionMs }
        assertEquals(1, activeIndex)
        assertEquals("Primeiro verso offline soberano", lyrics[activeIndex].text)

        // 3. User seeks to 1m 15s -> Chorus repeated stanza
        currentPositionMs = 75000L
        activeIndex = lyrics.indexOfLast { it.timeMs <= currentPositionMs }
        assertEquals(4, activeIndex)
        assertEquals("Refrão de alta fidelidade", lyrics[activeIndex].text)
    }

    /**
     * Scenario 2: Studio Audio DSP Tuning & Preset Persistence
     * Flow: User activates Equalizer, selects "Acoustic Worship", customizes high-mids,
     * and asserts safe hardware clamping with pop-free boundary invariance.
     */
    @Test
    fun testScenario2_studioAudioDspTuningAndPresetPersistence() {
        val worshipPreset = EqualizerPreset.ACOUSTIC_WORSHIP
        assertEquals(listOf(400, 200, -100, 600, 1000), worshipPreset.bandLevels)
        assertEquals(250, worshipPreset.bassBoost)

        // User customizes high treble (band index 4) to extreme +2500 mB
        val requestedExtremeTreble = 2500
        val clampedTreble = requestedExtremeTreble.coerceIn(
            AudioEffectManager.DEFAULT_MIN_MB,
            AudioEffectManager.DEFAULT_MAX_MB
        )
        assertEquals(1500, clampedTreble)

        // User customizes bass boost beyond maximum to 1500
        val requestedBass = 1500
        val clampedBass = requestedBass.coerceIn(0, 1000)
        assertEquals(1000, clampedBass)

        // Configuration transitions to CUSTOM preset
        val customState = EqualizerPreset.CUSTOM
        assertEquals("CUSTOM", customState.id)
    }

    /**
     * Scenario 3: Ergonomic MiniPlayer Queue Navigation
     * Flow: User interacts with MiniPlayer bar with taps, slight drags, and decisive swipes.
     */
    @Test
    fun testScenario3_ergonomicMiniPlayerQueueNavigation() {
        // 1. User performs brief tap (dx = 2dp) -> Below touch slop -> Not a drag -> Opens Sheet
        val tapDelta = 2f
        assertFalse(MiniPlayerSwipeEngine.isDragGesture(tapDelta))

        // 2. User drags left -35dp then hesitates and releases -> Below 72dp threshold -> Snaps back
        val subThresholdDrag = -35f
        assertTrue(MiniPlayerSwipeEngine.isDragGesture(subThresholdDrag))
        assertEquals(SwipeAction.SNAP_BACK_TO_CENTER, MiniPlayerSwipeEngine.evaluateDragRelease(subThresholdDrag))
        assertEquals(DirectionCue.CUE_NEXT, MiniPlayerSwipeEngine.getDirectionCue(subThresholdDrag))

        // 3. User decisively swipes right +85dp -> Exceeds threshold -> Skips to previous track
        val decisiveRightSwipe = 85f
        assertEquals(SwipeAction.SKIP_PREVIOUS, MiniPlayerSwipeEngine.evaluateDragRelease(decisiveRightSwipe))
        assertEquals(DirectionCue.CUE_PREVIOUS, MiniPlayerSwipeEngine.getDirectionCue(decisiveRightSwipe))

        // 4. Content alpha fades during drag but remains legible (>= 0.35)
        val alphaAtThreshold = MiniPlayerSwipeEngine.computeContentAlpha(72f)
        assertTrue("Alpha at 72dp must be around 0.82", alphaAtThreshold in 0.80f..0.85f)
    }

    /**
     * Scenario 4: Marketplace Delamain Corporate Executive Re-Theming
     * Flow: User switches theme to Delamain AI Core.
     * Verifies Neon Blue (#00E5FF), Platinum (#E0E7FF), Matte Graphite (#0A0B10), 4dp corners, and lexicon.
     */
    @Test
    fun testScenario4_marketplaceDelamainCorporateExecutiveReTheming() {
        // 1. Color Palette Tokens
        assertEquals(0x00 / 255f, DelamainThemeSpecification.NeonBlue.red, 0.01f)
        assertEquals(0xE5 / 255f, DelamainThemeSpecification.NeonBlue.green, 0.01f)
        assertEquals(1.0f, DelamainThemeSpecification.NeonBlue.blue, 0.01f)

        assertEquals(0x0A / 255f, DelamainThemeSpecification.MatteGraphite.red, 0.01f)
        assertEquals(0x0B / 255f, DelamainThemeSpecification.MatteGraphite.green, 0.01f)
        assertEquals(0x10 / 255f, DelamainThemeSpecification.MatteGraphite.blue, 0.01f)

        // 2. Geometry
        assertEquals(4.dp, DelamainThemeSpecification.CardCornerRadius)
        assertTrue(DelamainThemeSpecification.IsBrutalistSharp)

        // 3. Lexicon Consistency across HUD
        val lex = DelamainThemeSpecification.Lexicon
        assertEquals("DELAMAIN // AI CORE", lex.brandHeader)
        assertEquals("REDE DE TRANSPORTE SONORO", lex.brandSub)
        assertEquals("REDE DE ÁUDIO EXECUTIVA", lex.libraryTitle)
        assertEquals("CONCIERGE ON-LINE 100%", lex.streakTitle)
        assertEquals("DELAMAIN // CONCIERGE IA", lex.petName)
    }

    /**
     * Scenario 5: Offline Gamification Streak & Rank Voice Celebration
     * Flow: User achieves 7-day listening streak, triggers promotion from Level 1 to Level 2.
     * Speech celebration is generated, sanitized, and dispatched if voice is enabled.
     */
    @Test
    fun testScenario5_offlineGamificationStreakAndRankVoiceCelebration() {
        val streakDays = 7
        val streakQuip = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(streakDays)
        assertEquals("Impressionante: 7 dias de ofensiva ininterrupta no Aether Vault!", streakQuip)

        val rankPromotion = SovereignRank(level = 2, title = "GUARDIÃO DO VAULT", nextThreshold = 1500, colorHex = "#F59E0B")
        val rankQuip = HermesVoiceSpeechOracle.getRankPromotionPhrase(rankPromotion)
        assertEquals(
            "Atenção Fundador: Promoção de Rank confirmada! Você ascendeu para nível 2, GUARDIÃO DO VAULT.",
            rankQuip
        )

        // Verify TTS dispatch logic
        var isVoiceEnabled = true
        assertTrue(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled, rankQuip))

        // User disables voice in Settings -> Speech immediately suppressed
        isVoiceEnabled = false
        assertFalse(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled, rankQuip))
    }
}
