package com.hughdevelopment.player.v160

import com.hughdevelopment.player.data.db.StatsEntity
import com.hughdevelopment.player.data.settings.TimeDisplayFormat
import com.hughdevelopment.player.data.settings.formatListeningTime
import com.hughdevelopment.player.hermes.HermesDialogueEngine
import com.hughdevelopment.player.hermes.HermesVoiceManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultV160FeaturesTest {

    @Test
    fun `formatListeningTime formats minutes correctly`() {
        assertEquals("0 min", formatListeningTime(0, TimeDisplayFormat.MINUTES))
        assertEquals("1 min", formatListeningTime(60, TimeDisplayFormat.MINUTES))
        assertEquals("125 min", formatListeningTime(7500, TimeDisplayFormat.MINUTES))
        assertEquals("1440 min", formatListeningTime(86400, TimeDisplayFormat.MINUTES))
    }

    @Test
    fun `formatListeningTime formats days and hours correctly`() {
        assertEquals("0 horas", formatListeningTime(0, TimeDisplayFormat.DAYS_AND_HOURS))
        assertEquals("59 min", formatListeningTime(59 * 60, TimeDisplayFormat.DAYS_AND_HOURS))
        assertEquals("1 hora", formatListeningTime(3600, TimeDisplayFormat.DAYS_AND_HOURS))
        assertEquals("3 horas e 15 min", formatListeningTime(3 * 3600 + 15 * 60, TimeDisplayFormat.DAYS_AND_HOURS))
        assertEquals("1 dia", formatListeningTime(86400, TimeDisplayFormat.DAYS_AND_HOURS))
        assertEquals("1 dia e 3 horas", formatListeningTime(86400 + 3 * 3600, TimeDisplayFormat.DAYS_AND_HOURS))
        assertEquals("2 dias e 5 horas", formatListeningTime(2 * 86400 + 5 * 3600, TimeDisplayFormat.DAYS_AND_HOURS))
    }

    @Test
    fun `statsEntity isHidden property defaults to false and behaves correctly`() {
        val stats = StatsEntity(
            songId = 1L,
            title = "Night City Cyber",
            artist = "Arasaka Soundworks",
            playCount = 10,
            totalSecondsListened = 600,
            lastPlayedTimestamp = 1000L,
            isFavorite = false,
            isHidden = false
        )

        assertFalse(stats.isHidden)
        val hiddenStats = stats.copy(isHidden = true)
        assertTrue(hiddenStats.isHidden)
    }

    @Test
    fun `hermesDialogueEngine generates complete daily telemetry briefing`() {
        val briefing = HermesDialogueEngine.buildDailyBriefing(
            formattedTime = "45 min",
            streakDays = 7,
            rankTitle = "ALQUIMISTA DO SOM",
            totalTracks = 120
        )

        assertTrue(briefing.contains("45 min"))
        assertTrue(briefing.contains("7 dias"))
        assertTrue(briefing.contains("ALQUIMISTA DO SOM"))
        assertTrue(briefing.contains("120 faixas"))
    }

    @Test
    fun `hermesDialogueEngine returns valid philosophical tips`() {
        for (i in 0..10) {
            val tip = HermesDialogueEngine.getRandomTip()
            assertTrue(tip.isNotBlank())
            assertTrue(tip.length > 10)
        }
    }

    @Test
    fun `hermesVoiceManager provides celebration phrase on streak milestone`() {
        val celebration = HermesVoiceManager.getStreakCelebrationPhrase(5)
        assertTrue(celebration.isNotBlank())
        assertTrue(celebration.contains("5 dias"))
    }
}
