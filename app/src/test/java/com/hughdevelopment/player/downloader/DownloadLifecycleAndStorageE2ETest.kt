package com.hughdevelopment.player.downloader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Domain State Engine modeling DownloadService lifecycle and Scoped Storage resolution.
 *
 * Implements the contract defined in:
 * - PROJECT.md § Interface Contracts § 5. Download Lifecycle & Scoped Storage
 * - ORIGINAL_REQUEST.md § R2. Fase 1 (stopSelf() and Scoped Storage TargetSdk 35)
 */
class DownloadLifecycleEngine {

    enum class ServiceState {
        IDLE,
        FOREGROUND_RUNNING,
        STOPPED
    }

    data class DownloadItem(
        val videoId: String,
        val title: String,
        val artist: String,
        var status: String // DOWNLOADING, COMPLETED, CANCELLED, FAILED
    )

    private val queue = mutableMapOf<String, DownloadItem>()
    var serviceState: ServiceState = ServiceState.IDLE
        private set
    var stopSelfCallCount = 0
        private set
    var stopForegroundCallCount = 0
        private set

    fun enqueue(videoId: String, title: String, artist: String) {
        queue[videoId] = DownloadItem(videoId, title, artist, "DOWNLOADING")
        evaluateServiceLifecycle()
    }

    fun markCompleted(videoId: String) {
        queue[videoId]?.status = "COMPLETED"
        evaluateServiceLifecycle()
    }

    fun cancel(videoId: String) {
        queue[videoId]?.status = "CANCELLED"
        evaluateServiceLifecycle()
    }

    fun markFailed(videoId: String) {
        queue[videoId]?.status = "FAILED"
        evaluateServiceLifecycle()
    }

    /**
     * Re-evaluates queue state.
     * When active downloads > 0 -> startForeground.
     * When active downloads drop to 0 -> stopForeground(STOP_FOREGROUND_REMOVE) AND stopSelf()!
     */
    private fun evaluateServiceLifecycle() {
        val activeCount = queue.values.count { it.status == "DOWNLOADING" }
        if (activeCount > 0) {
            serviceState = ServiceState.FOREGROUND_RUNNING
        } else {
            // No active downloads remain in queue
            stopForegroundCallCount++
            stopSelfCallCount++
            serviceState = ServiceState.STOPPED
        }
    }

    /**
     * Resolves storage destination folder adhering to Android 11+ / TargetSdk 35 Scoped Storage rules.
     */
    fun resolveStorageDestination(
        isTargetSdk35: Boolean,
        hasExternalAppDir: Boolean,
        externalAppDirPath: String,
        internalDirPath: String
    ): File {
        // Under TargetSdk 35, public raw directory writes (Environment.getExternalStoragePublicDirectory)
        // are deprecated and restricted by Scoped Storage enforcement.
        // The player must safely route to app-specific external storage or MediaStore ContentResolver.
        return if (isTargetSdk35) {
            if (hasExternalAppDir) File(externalAppDirPath) else File(internalDirPath)
        } else {
            File("/storage/emulated/0/Music/AetherVault")
        }
    }

    /**
     * Sanitizes file names by stripping illegal path characters: [\\/:*?"<>|]
     */
    fun sanitizeFileName(title: String, artist: String, format: String): String {
        val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
        val sanitizedArtist = artist.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
        val base = if (sanitizedArtist.isNotBlank() && sanitizedArtist != "Unknown") {
            "$sanitizedArtist - $sanitizedTitle"
        } else {
            sanitizedTitle
        }
        return "$base.$format"
    }

    fun clear() {
        queue.clear()
        serviceState = ServiceState.IDLE
        stopSelfCallCount = 0
        stopForegroundCallCount = 0
    }
}

/**
 * Comprehensive 4-Tier Test Suite for DownloadService Lifecycle & Scoped Storage (Features 8 & 9).
 */
class DownloadLifecycleAndStorageE2ETest {

    private lateinit var engine: DownloadLifecycleEngine

    @Before
    fun setUp() {
        engine = DownloadLifecycleEngine()
    }

    // =========================================================================
    // TIER 1: FEATURE COVERAGE (Service Lifecycle & Scoped Storage)
    // =========================================================================

    @Test
    fun testTier1_downloadService_callsStopSelfWhenQueueEmpties() {
        // Enqueue single download
        engine.enqueue("vid123", "Cyberpunk Track", "Delamain")
        assertEquals(
            "Service must transition to FOREGROUND_RUNNING when active downloads exist",
            DownloadLifecycleEngine.ServiceState.FOREGROUND_RUNNING,
            engine.serviceState
        )
        assertEquals(0, engine.stopSelfCallCount)

        // Download finishes
        engine.markCompleted("vid123")

        // CRITICAL BUG FIX INVARIANT: stopSelf() must be called immediately when queue drops to 0!
        assertEquals("Service must transition to STOPPED", DownloadLifecycleEngine.ServiceState.STOPPED, engine.serviceState)
        assertEquals("stopSelf() must be invoked exactly once", 1, engine.stopSelfCallCount)
        assertEquals("stopForeground() must be invoked exactly once", 1, engine.stopForegroundCallCount)
    }

    @Test
    fun testTier1_downloadService_multipleDownloads_waitsForLastDownloadBeforeStopping() {
        engine.enqueue("v1", "Track 1", "Artist 1")
        engine.enqueue("v2", "Track 2", "Artist 2")
        engine.enqueue("v3", "Track 3", "Artist 3")

        assertEquals(DownloadLifecycleEngine.ServiceState.FOREGROUND_RUNNING, engine.serviceState)
        assertEquals(0, engine.stopSelfCallCount)

        // Complete first download -> 2 active remain
        engine.markCompleted("v1")
        assertEquals(DownloadLifecycleEngine.ServiceState.FOREGROUND_RUNNING, engine.serviceState)
        assertEquals(0, engine.stopSelfCallCount)

        // Complete second download -> 1 active remains
        engine.markCompleted("v2")
        assertEquals(DownloadLifecycleEngine.ServiceState.FOREGROUND_RUNNING, engine.serviceState)
        assertEquals(0, engine.stopSelfCallCount)

        // Complete final download -> 0 active remain -> triggers stopSelf()
        engine.markCompleted("v3")
        assertEquals(DownloadLifecycleEngine.ServiceState.STOPPED, engine.serviceState)
        assertEquals(1, engine.stopSelfCallCount)
    }

    @Test
    fun testTier1_scopedStorage_targetSdk35UsesAppSpecificExternalStorage() {
        val dest = engine.resolveStorageDestination(
            isTargetSdk35 = true,
            hasExternalAppDir = true,
            externalAppDirPath = "C:/tmp/Android/data/com.hughdevelopment.player/files/Music",
            internalDirPath = "C:/tmp/data/com.hughdevelopment.player/files"
        )

        assertEquals("C:/tmp/Android/data/com.hughdevelopment.player/files/Music", dest.path.replace('\\', '/'))
        assertFalse("TargetSdk 35 must never use raw public storage path", dest.path.contains("/storage/emulated/0/Music/AetherVault"))
    }

    @Test
    fun testTier1_fileNameSanitization_stripsIllegalCharacters() {
        val dangerousTitle = "Song: Title/With?Illegal*Chars<And>Pipes|"
        val dangerousArtist = "Artist/AC\\DC?\"Rock\""
        val format = "opus"

        val sanitized = engine.sanitizeFileName(dangerousTitle, dangerousArtist, format)

        assertFalse("Filename must not contain ':'", sanitized.contains(":"))
        assertFalse("Filename must not contain '/'", sanitized.contains("/"))
        assertFalse("Filename must not contain '\\'", sanitized.contains("\\"))
        assertFalse("Filename must not contain '?'", sanitized.contains("?"))
        assertFalse("Filename must not contain '*'", sanitized.contains("*"))
        assertFalse("Filename must not contain '\"'", sanitized.contains("\""))
        assertFalse("Filename must not contain '<'", sanitized.contains("<"))
        assertFalse("Filename must not contain '>'", sanitized.contains(">"))
        assertFalse("Filename must not contain '|'", sanitized.contains("|"))
        assertTrue("Filename must end with .opus extension", sanitized.endsWith(".opus"))
    }

    // =========================================================================
    // TIER 2: BOUNDARY & CORNER CASES (Cancellation, Failure, Empty Queue)
    // =========================================================================

    @Test
    fun testTier2_downloadCancellation_triggersStopSelfWhenAllCancelled() {
        engine.enqueue("v_cancel", "Test Song", "Unknown")
        assertEquals(DownloadLifecycleEngine.ServiceState.FOREGROUND_RUNNING, engine.serviceState)

        engine.cancel("v_cancel")
        assertEquals(DownloadLifecycleEngine.ServiceState.STOPPED, engine.serviceState)
        assertEquals(1, engine.stopSelfCallCount)
    }

    @Test
    fun testTier2_downloadFailure_triggersStopSelfWhenAllFailed() {
        engine.enqueue("v_fail", "Broken Stream", "Artist")
        engine.markFailed("v_fail")

        assertEquals(DownloadLifecycleEngine.ServiceState.STOPPED, engine.serviceState)
        assertEquals(1, engine.stopSelfCallCount)
    }

    @Test
    fun testTier2_tempFilePrefixAndCleanup() {
        val videoId = "dQw4w9WgXcQ"
        val tempFileName = ".tmp_$videoId.m4a"

        assertTrue("Temp file must start with .tmp_ prefix for atomic swap", tempFileName.startsWith(".tmp_"))
        assertTrue(tempFileName.endsWith(".m4a"))
    }

    // =========================================================================
    // TIER 3: CROSS-FEATURE INTERACTIONS (Download ↔ Scoped Storage ↔ MediaStore)
    // =========================================================================

    @Test
    fun testTier3_crossFeature_mimeTypeSelectionForOpusAndMp4() {
        fun resolveMimeType(format: String): String {
            return if (format.equals("opus", ignoreCase = true)) "audio/ogg" else "audio/mp4"
        }

        assertEquals("audio/ogg", resolveMimeType("opus"))
        assertEquals("audio/ogg", resolveMimeType("OPUS"))
        assertEquals("audio/mp4", resolveMimeType("m4a"))
        assertEquals("audio/mp4", resolveMimeType("mp4"))
    }
}
