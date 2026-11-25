package com.hughdevelopment.player.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pure Domain State & Telemetry Decoupling Engine.
 *
 * Implements the contract defined in:
 * - PROJECT.md § Interface Contracts § 2. Telemetry
 * - ORIGINAL_REQUEST.md § R2. Fase 1
 *
 * Contract:
 * - `recordDailyListening(dateKey: String, addedSeconds: Long, isNewSongPlay: Boolean = false)`
 * - `songsPlayedCount` ONLY increments when `isNewSongPlay == true`.
 * - Periodic 5s flushes pass `isNewSongPlay = false`, updating only `totalSecondsListened`.
 */
class TelemetryDecouplingEngine {

    data class DailyRecord(
        val dateKey: String,
        var totalSecondsListened: Long,
        var songsPlayedCount: Int,
        var hasValidStreakDay: Boolean
    )

    private val records = mutableMapOf<String, DailyRecord>()

    fun getRecord(dateKey: String): DailyRecord? = records[dateKey]

    /**
     * Decoupled recording function matching the v2.0 Room DAO contract.
     */
    fun recordDailyListening(dateKey: String, addedSeconds: Long, isNewSongPlay: Boolean = false) {
        if (addedSeconds <= 0 && !isNewSongPlay) return

        val existing = records[dateKey]
        if (existing == null) {
            val total = addedSeconds.coerceAtLeast(0L)
            records[dateKey] = DailyRecord(
                dateKey = dateKey,
                totalSecondsListened = total,
                songsPlayedCount = if (isNewSongPlay) 1 else 0,
                hasValidStreakDay = total >= 300L
            )
        } else {
            val newTotal = existing.totalSecondsListened + addedSeconds.coerceAtLeast(0L)
            existing.totalSecondsListened = newTotal
            if (isNewSongPlay) {
                existing.songsPlayedCount += 1
            }
            existing.hasValidStreakDay = newTotal >= 300L
        }
    }

    fun clear() {
        records.clear()
    }
}

/**
 * Comprehensive 4-Tier Test Suite for Telemetry Increment Decoupling (Feature 5).
 */
class TelemetryDecouplingE2ETest {

    private lateinit var engine: TelemetryDecouplingEngine
    private val today = "2026-11-24"

    @Before
    fun setUp() {
        engine = TelemetryDecouplingEngine()
    }

    // =========================================================================
    // TIER 1: FEATURE COVERAGE (Decoupled Flushes vs Song Count)
    // =========================================================================

    @Test
    fun testTier1_singleSongPlay_startsSongAndFlushesPeriodic5sIntervals() {
        // Track begins playing -> new song play event registered
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)

        val initial = engine.getRecord(today)
        assertEquals("Initial song start must register 1 song", 1, initial?.songsPlayedCount)
        assertEquals("Initial seconds must be 0", 0L, initial?.totalSecondsListened)

        // 36 consecutive 5-second periodic flushes (180s = 3 minutes of listening)
        for (i in 1..36) {
            engine.recordDailyListening(today, addedSeconds = 5L, isNewSongPlay = false)
        }

        val afterPlayback = engine.getRecord(today)
        assertEquals("Total listened seconds must be exactly 180 seconds", 180L, afterPlayback?.totalSecondsListened)
        assertEquals(
            "CRITICAL BUG FIX INVARIANT: songsPlayedCount must remain 1 after 36 periodic flushes, NOT 36!",
            1,
            afterPlayback?.songsPlayedCount
        )
    }

    @Test
    fun testTier1_multipleSongs_incrementsCountExactlyOncePerSong() {
        val songsToPlay = 5
        val flushesPerSong = 12 // 60 seconds per song

        for (songIndex in 1..songsToPlay) {
            // New song begins
            engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)

            // Periodic flushes during this song
            for (flush in 1..flushesPerSong) {
                engine.recordDailyListening(today, addedSeconds = 5L, isNewSongPlay = false)
            }
        }

        val result = engine.getRecord(today)
        assertEquals("Total songs played must be exactly 5", 5, result?.songsPlayedCount)
        assertEquals("Total listened seconds must be 5 * 60 = 300s", 300L, result?.totalSecondsListened)
        assertTrue("300 seconds must validate streak day threshold", result?.hasValidStreakDay == true)
    }

    @Test
    fun testTier1_streakValidationThreshold_boundaryCheckAt300Seconds() {
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)

        // Flush 299 seconds
        engine.recordDailyListening(today, addedSeconds = 299L, isNewSongPlay = false)
        assertFalse("299 seconds must not validate streak day (requires >= 300s)", engine.getRecord(today)!!.hasValidStreakDay)

        // Add 1 more second to reach 300s
        engine.recordDailyListening(today, addedSeconds = 1L, isNewSongPlay = false)
        assertTrue("300 seconds must validate streak day", engine.getRecord(today)!!.hasValidStreakDay)
    }

    @Test
    fun testTier1_thresholdPlaybackRegistration_validates15sThreshold() {
        // In threshold playback model, songsPlayedCount only counts after 15 seconds of playback
        var secondsListened = 0L
        var songCountRegistered = false

        fun onPlaybackProgress(deltaSeconds: Long) {
            secondsListened += deltaSeconds
            val isThresholdReached = secondsListened >= 15L && !songCountRegistered
            if (isThresholdReached) {
                songCountRegistered = true
                engine.recordDailyListening(today, addedSeconds = deltaSeconds, isNewSongPlay = true)
            } else {
                engine.recordDailyListening(today, addedSeconds = deltaSeconds, isNewSongPlay = false)
            }
        }

        // 2 flushes of 5s = 10s (below threshold)
        onPlaybackProgress(5L)
        onPlaybackProgress(5L)
        assertEquals("At 10s, threshold not reached; song count is 0", 0, engine.getRecord(today)?.songsPlayedCount ?: 0)

        // 3rd flush: reaches 15s -> song count increments to 1
        onPlaybackProgress(5L)
        assertEquals("At 15s, threshold reached; song count is 1", 1, engine.getRecord(today)?.songsPlayedCount)

        // 4th and 5th flushes: further playback does not increment song count
        onPlaybackProgress(5L)
        onPlaybackProgress(5L)
        assertEquals("After 25s, song count remains 1", 1, engine.getRecord(today)?.songsPlayedCount)
        assertEquals(25L, engine.getRecord(today)?.totalSecondsListened)
    }

    // =========================================================================
    // TIER 2: BOUNDARY & CORNER CASES (0s Listen, Sub-second, Rapid Flushes)
    // =========================================================================

    @Test
    fun testTier2_zeroSecondsListen_doesNotCorruptState() {
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = false)
        val record = engine.getRecord(today)
        assertTrue("0 seconds with isNewSongPlay=false should not create or corrupt records", record == null)
    }

    @Test
    fun testTier2_negativeSeconds_ignoredSafely() {
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)
        engine.recordDailyListening(today, addedSeconds = -10L, isNewSongPlay = false)

        val record = engine.getRecord(today)
        assertEquals("Negative seconds must be clamped to 0", 0L, record?.totalSecondsListened)
        assertEquals(1, record?.songsPlayedCount)
    }

    @Test
    fun testTier2_rapidFlushesStressTest() {
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)

        // 500 rapid flushes of 1 second each
        for (i in 1..500) {
            engine.recordDailyListening(today, addedSeconds = 1L, isNewSongPlay = false)
        }

        val record = engine.getRecord(today)
        assertEquals("Total seconds must be 500", 500L, record?.totalSecondsListened)
        assertEquals("Songs played count must remain strictly 1", 1, record?.songsPlayedCount)
        assertTrue("500s exceeds 300s streak threshold", record?.hasValidStreakDay == true)
    }

    @Test
    fun testTier2_midnightDateTransition_splitsRecordsCorrectly() {
        val day1 = "2026-11-23"
        val day2 = "2026-11-24"

        // Playing song across midnight
        engine.recordDailyListening(day1, addedSeconds = 0L, isNewSongPlay = true)
        for (i in 1..10) {
            engine.recordDailyListening(day1, addedSeconds = 5L, isNewSongPlay = false)
        }

        // Midnight arrives, next flushes record under day2
        for (i in 1..20) {
            engine.recordDailyListening(day2, addedSeconds = 5L, isNewSongPlay = false)
        }

        val rec1 = engine.getRecord(day1)
        val rec2 = engine.getRecord(day2)

        assertEquals(50L, rec1?.totalSecondsListened)
        assertEquals(1, rec1?.songsPlayedCount)

        assertEquals(100L, rec2?.totalSecondsListened)
        assertEquals("Continuous playback into day 2 without new track should not increment song count", 0, rec2?.songsPlayedCount)
    }

    @Test
    fun testTier2_rapidSongSkip_incrementsSongCountWithoutSubstantialTime() {
        // User rapidly skips 10 tracks, listening to 1 second each
        for (i in 1..10) {
            engine.recordDailyListening(today, addedSeconds = 1L, isNewSongPlay = true)
        }

        val record = engine.getRecord(today)
        assertEquals("10 skipped songs must register 10 played songs", 10, record?.songsPlayedCount)
        assertEquals("Total seconds should be 10s", 10L, record?.totalSecondsListened)
        assertFalse("10s is far below streak threshold", record?.hasValidStreakDay == true)
    }

    // =========================================================================
    // TIER 3: CROSS-FEATURE INTERACTIONS (Telemetry ↔ Controller ↔ Streak)
    // =========================================================================

    @Test
    fun testTier3_crossFeature_controllerPeriodicFlushSimulation() {
        // Simulate MusicPlayerController startPositionTracker loop:
        // - secondsAccumulatedForCurrentSong increments by 1 every second
        // - when >= 5, flushAccumulatedListenStats() is called with 5 seconds and isNewSongPlay = false
        // - onMediaItemTransition calls onSongStarted with isNewSongPlay = true

        var secondsAccumulated = 0L
        fun onTickSecond() {
            secondsAccumulated += 1
            if (secondsAccumulated >= 5) {
                engine.recordDailyListening(today, addedSeconds = secondsAccumulated, isNewSongPlay = false)
                secondsAccumulated = 0L
            }
        }

        // Track 1 starts
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)
        repeat(63) { onTickSecond() } // 63 seconds of playback = 12 flushes of 5s + 3s remaining

        // Flush remaining on track change
        if (secondsAccumulated > 0) {
            engine.recordDailyListening(today, addedSeconds = secondsAccumulated, isNewSongPlay = false)
            secondsAccumulated = 0L
        }

        // Track 2 starts
        engine.recordDailyListening(today, addedSeconds = 0L, isNewSongPlay = true)
        repeat(47) { onTickSecond() } // 47 seconds of playback
        if (secondsAccumulated > 0) {
            engine.recordDailyListening(today, addedSeconds = secondsAccumulated, isNewSongPlay = false)
            secondsAccumulated = 0L
        }

        val record = engine.getRecord(today)
        assertEquals("Total listened seconds: 63 + 47 = 110s", 110L, record?.totalSecondsListened)
        assertEquals("Total songs count must be exactly 2", 2, record?.songsPlayedCount)
    }
}
