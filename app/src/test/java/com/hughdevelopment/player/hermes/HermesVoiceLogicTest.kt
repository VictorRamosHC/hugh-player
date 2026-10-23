package com.hughdevelopment.player.hermes

import com.hughdevelopment.player.data.ai.HermesPetEngine
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.SovereignRank
import com.hughdevelopment.player.data.model.StreakInfo
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.ui.screens.RankPromotionVoiceDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Domain voice synthesis & phrase generation oracle for Hermes Voice Engine (F-13, F-14, F-15).
 * Delegates directly to the production HermesVoiceManager to verify genuine implementation.
 */
object HermesVoiceSpeechOracle {

    fun sanitizeForSpeech(input: String): String {
        return HermesVoiceManager.sanitize(input)
    }

    fun getStreakCelebrationPhrase(streakDays: Int): String {
        return HermesVoiceManager.getStreakCelebrationPhrase(streakDays)
    }

    fun getRankPromotionPhrase(rank: SovereignRank): String {
        return HermesVoiceManager.getRankPromotionPhrase(rank)
    }

    fun shouldDispatchSpeech(isVoiceEnabled: Boolean, text: String): Boolean {
        return HermesVoiceManager.shouldDispatchSpeech(isVoiceEnabled, text)
    }
}

/**
 * Comprehensive Unit Test Suite for Hermes Voice Engine & Speech Synthesis Logic (F-13, F-14, F-15).
 */
open class HermesVoiceLogicTest {

    // --- Tier 1: Text Sanitization Precision (F-14) ---

    @Test
    fun testSanitizeForSpeech_stripsEmojisAndCyberGlyphs() {
        val raw = "Ofensiva de 5 dias mantida 🔥! A disciplina no Vault 🏛️ gera poder ⚡."
        val sanitized = HermesVoiceSpeechOracle.sanitizeForSpeech(raw)
        assertEquals(
            "Ofensiva de 5 dias mantida ! A disciplina no Vault gera poder .",
            sanitized
        )
        assertFalse(sanitized.contains("🔥"))
        assertFalse(sanitized.contains("🏛️"))
        assertFalse(sanitized.contains("⚡"))
    }

    @Test
    fun testSanitizeForSpeech_stripsMarkdownAndBrackets() {
        val raw = "[TRANSPORTE // ATIVO] “Não nos perturbam as coisas” — Epicteto."
        val sanitized = HermesVoiceSpeechOracle.sanitizeForSpeech(raw)
        assertEquals(
            "TRANSPORTE // ATIVO Não nos perturbam as coisas Epicteto.",
            sanitized
        )
        assertFalse(sanitized.contains("["))
        assertFalse(sanitized.contains("]"))
        assertFalse(sanitized.contains("“"))
        assertFalse(sanitized.contains("”"))
        assertFalse(sanitized.contains("—"))
    }

    @Test
    fun testSanitizeForSpeech_handlesAccentsAndPortugueseCharacters() {
        val raw = "Grandioso és Tu! Coração, bênção, pontuação e silêncio."
        val sanitized = HermesVoiceSpeechOracle.sanitizeForSpeech(raw)
        assertEquals("Grandioso és Tu! Coração, bênção, pontuação e silêncio.", sanitized)
    }

    @Test
    fun testSanitizeForSpeech_collapsesMultipleWhitespace() {
        val raw = "  Texto   com    espaços   duplos   \t e quebras.  "
        val sanitized = HermesVoiceSpeechOracle.sanitizeForSpeech(raw)
        assertEquals("Texto com espaços duplos e quebras.", sanitized)
    }

    // --- Tier 1: Streak Celebration Phrase Matrix (F-14) ---

    @Test
    fun testStreakCelebration_singleDayMilestone() {
        val phrase = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(1)
        assertEquals("Meta diária batida no Vault! Disciplina ativada.", phrase)
    }

    @Test
    fun testStreakCelebration_shortStreakTwoToSixDays() {
        val phrase3 = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(3)
        assertEquals("Ofensiva de 3 dias mantida! A consistência constrói o mestre.", phrase3)

        val phrase6 = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(6)
        assertEquals("Ofensiva de 6 dias mantida! A consistência constrói o mestre.", phrase6)
    }

    @Test
    fun testStreakCelebration_mediumStreakSevenToTwentyNineDays() {
        val phrase7 = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(7)
        assertEquals("Impressionante: 7 dias de ofensiva ininterrupta no Aether Vault!", phrase7)

        val phrase21 = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(21)
        assertEquals("Impressionante: 21 dias de ofensiva ininterrupta no Aether Vault!", phrase21)
    }

    @Test
    fun testStreakCelebration_legendaryStreakThirtyPlusDays() {
        val phrase30 = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(30)
        assertEquals("Lendário! 30 dias de disciplina absoluta. Você é um verdadeiro Arconte!", phrase30)

        val phrase100 = HermesVoiceSpeechOracle.getStreakCelebrationPhrase(100)
        assertEquals("Lendário! 100 dias de disciplina absoluta. Você é um verdadeiro Arconte!", phrase100)
    }

    // --- Tier 2: Rank Promotion Announcements (F-14) ---

    @Test
    fun testRankPromotionPhrases_allFourSovereignRanks() {
        val ranks = listOf(
            SovereignRank(level = 1, title = "INICIADO DO AETHER", nextThreshold = 500, colorHex = "#06B6D4"),
            SovereignRank(level = 2, title = "GUARDIÃO DO VAULT", nextThreshold = 1500, colorHex = "#F59E0B"),
            SovereignRank(level = 3, title = "ALQUIMISTA DO SOM", nextThreshold = 3500, colorHex = "#0022FF"),
            SovereignRank(level = 4, title = "ARCONTE SOBERANO", nextThreshold = 6000, colorHex = "#A855F7")
        )

        ranks.forEach { rank ->
            val phrase = HermesVoiceSpeechOracle.getRankPromotionPhrase(rank)
            assertTrue("Phrase must contain rank level ${rank.level}", phrase.contains("nível ${rank.level}"))
            assertTrue("Phrase must contain rank title ${rank.title}", phrase.contains(rank.title))
            assertTrue(phrase.startsWith("Atenção Fundador: Promoção de Rank confirmada!"))
        }
    }

    // --- Tier 2: Voice Settings Switch & Speech Guard (F-15) ---

    @Test
    fun testVoiceToggleGuard_suppressesSpeechWhenDisabled() {
        val text = "Meta diária batida!"

        // Enabled -> Allowed
        assertTrue(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = true, text = text))

        // Disabled -> Suppressed (Silent Mode)
        assertFalse(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = false, text = text))
    }

    @Test
    fun testVoiceToggleGuard_suppressesBlankOrWhitespaceOnlyInput() {
        assertFalse(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = true, text = ""))
        assertFalse(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = true, text = "   "))
        assertFalse(HermesVoiceSpeechOracle.shouldDispatchSpeech(isVoiceEnabled = true, text = "🔥⚡🏛️")) // Emojis stripped to blank
    }

    // --- Tier 3: Conversational Heuristic Quips (HermesPetEngine) ---

    @Test
    fun testHermesPetEngine_respondToStreakQuery() {
        val overview = ListeningOverview(
            streakInfo = StreakInfo(currentStreak = 5, isTodayCompleted = true),
            totalSecondsListened = 120 * 60L
        )

        val response = HermesPetEngine.respondToUserQuery(
            query = "Como está minha ofensiva?",
            overview = overview,
            songsCount = 50,
            playlists = emptyList()
        )

        assertEquals("Hermes", response.sender)
        assertTrue(response.text.contains("5 dias seguidos"))
        assertEquals(HermesPetEngine.HermesActionType.SHOW_STREAK, response.actionType)
    }

    @Test
    fun testHermesPetEngine_respondToOrganizeQuery() {
        val overview = ListeningOverview()
        val response = HermesPetEngine.respondToUserQuery(
            query = "Por favor organize minhas pastas",
            overview = overview,
            songsCount = 20,
            playlists = emptyList()
        )

        assertEquals(HermesPetEngine.HermesActionType.AUTO_ORGANIZE_FOLDERS, response.actionType)
        assertTrue(response.text.contains("4 categorias de elite"))
    }

    // --- Tier 4: HermesVoiceManager Direct API & Settings Verification ---

    @Test
    fun testHermesVoiceManager_instanceSanitizationAndDefaults() {
        val manager = HermesVoiceManager(context = null, enabled = true)
        assertTrue(manager.isHermesVoiceEnabled)
        assertFalse(manager.isInitialized)

        val clean = manager.sanitizeForSpeech("⚡ Alquimia do Som [LVL 3] 🔥")
        assertEquals("Alquimia do Som LVL 3", clean)
    }

    @Test
    fun testHermesVoiceManager_streakCelebrationPhrasesDirect() {
        val p1 = HermesVoiceManager.getStreakCelebrationPhrase(1)
        assertTrue(p1.contains("Meta diária batida"))

        val p5 = HermesVoiceManager.getStreakCelebrationPhrase(5)
        assertTrue(p5.contains("Ofensiva de 5 dias"))

        val p14 = HermesVoiceManager.getStreakCelebrationPhrase(14)
        assertTrue(p14.contains("Impressionante: 14 dias"))

        val p60 = HermesVoiceManager.getStreakCelebrationPhrase(60)
        assertTrue(p60.contains("Lendário! 60 dias"))
    }

    @Test
    fun testHermesVoiceManager_rankPromotionDirect() {
        val rank = SovereignRank(level = 3, title = "ALQUIMISTA DO SOM", nextThreshold = 3500, colorHex = "#0022FF")
        val phrase = HermesVoiceManager.getRankPromotionPhrase(rank)
        assertTrue(phrase.contains("nível 3"))
        assertTrue(phrase.contains("ALQUIMISTA DO SOM"))
    }

    @Test
    fun testSettingsState_isHermesVoiceEnabledDefaultIsTrue() {
        val settings = SettingsState()
        assertTrue("Hermes Voice must be enabled by default", settings.isHermesVoiceEnabled)
    }

    @Test
    fun testRankPromotionVoiceDispatcher_ascensionLogic() {
        RankPromotionVoiceDispatcher.resetBaseline(1)
        assertEquals(1, RankPromotionVoiceDispatcher.getLastRecordedRankLevel())

        // Same level -> no ascension
        assertFalse(RankPromotionVoiceDispatcher.isAscension(1, 1))

        // Level up -> ascension
        assertTrue(RankPromotionVoiceDispatcher.isAscension(1, 2))
        assertTrue(RankPromotionVoiceDispatcher.isAscension(2, 3))
        assertTrue(RankPromotionVoiceDispatcher.isAscension(3, 4))

        // Demotion / zero -> no ascension
        assertFalse(RankPromotionVoiceDispatcher.isAscension(3, 2))
        assertFalse(RankPromotionVoiceDispatcher.isAscension(0, 1))
    }

    @Test
    fun testHermesPetEngine_voiceDelegation() {
        HermesPetEngine.isVoiceEnabled = true
        assertTrue(HermesPetEngine.isVoiceEnabled)

        val clean = HermesPetEngine.sanitizeForSpeech("🔥 Teste 🏛️")
        assertEquals("Teste", clean)
    }
}
