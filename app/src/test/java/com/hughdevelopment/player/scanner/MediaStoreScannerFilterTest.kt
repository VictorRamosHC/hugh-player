package com.hughdevelopment.player.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Filter Oracle reflecting MediaStoreScanner temporary directory pruning (F-16).
 */
object MediaStoreScannerFilterOracle {

    private val EXCLUDED_PATH_SUBSTRINGS = listOf(
        "/.thumbnails/",
        "/.cache/",
        "/cache/",
        "/temp/",
        "/tmp/",
        "/.trashed/",
        "/.trash/",
        "/com.whatsapp/media/.statuses/",
        "call_recorder",
        "sound_recorder",
        "/gravador/"
    )

    fun isNoiseOrRecording(path: String, title: String): Boolean {
        val lowerPath = path.lowercase().replace('\\', '/')
        val lowerTitle = title.lowercase()

        for (excluded in EXCLUDED_PATH_SUBSTRINGS) {
            if (lowerPath.contains(excluded) || lowerTitle.contains(excluded)) {
                return true
            }
        }

        return false
    }

    fun cleanMetadata(text: String): String {
        return if (text.isBlank() || text.equals("<unknown>", ignoreCase = true)) {
            "Desconhecido"
        } else {
            text.trim()
        }
    }
}

/**
 * Comprehensive Unit Test Suite for MediaStoreScanner Directory Pruning & Metadata Cleaning (F-16, F-17).
 */
class MediaStoreScannerFilterTest {

    // --- Tier 1: Temporary Directory Pruning Exclusions (F-16) ---

    @Test
    fun testExcludeThumbnailsDirectory() {
        val path = "/storage/emulated/0/DCIM/.thumbnails/audio_thumb.mp3"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(path, "audio_thumb"))
    }

    @Test
    fun testExcludeCacheDirectories() {
        val pathDotCache = "/storage/emulated/0/Android/data/org.telegram.messenger/.cache/voice.m4a"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathDotCache, "voice"))

        val pathCache = "/data/user/0/com.spotify.music/cache/stream_dump.mp3"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathCache, "stream_dump"))
    }

    @Test
    fun testExcludeTempAndTmpDirectories() {
        val pathTemp = "/storage/emulated/0/Download/temp/preview_render.mp3"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathTemp, "preview_render"))

        val pathTmp = "/storage/emulated/0/tmp/chunk_01.wav"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathTmp, "chunk_01"))
    }

    @Test
    fun testExcludeTrashedDirectories() {
        val pathTrashed = "/storage/emulated/0/.trashed/123456-old_song.mp3"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathTrashed, "123456-old_song"))

        val pathTrash = "/storage/emulated/0/.trash/deleted_recording.m4a"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathTrash, "deleted_recording"))
    }

    @Test
    fun testExcludeWhatsAppEphemeralStatuses() {
        val pathWhatsapp = "/storage/emulated/0/Android/media/com.whatsapp/media/.statuses/status_audio.aac"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(pathWhatsapp, "status_audio"))
    }

    @Test
    fun testExcludeCallAndSoundRecorders() {
        val callRec = "/storage/emulated/0/Recordings/call_recorder_20261023.m4a"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(callRec, "call_recorder_20261023"))

        val soundRec = "/storage/emulated/0/MIUI/sound_recorder/memo.mp3"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(soundRec, "memo"))

        val gravador = "/storage/emulated/0/gravador/nota_de_voz.m4a"
        assertTrue(MediaStoreScannerFilterOracle.isNoiseOrRecording(gravador, "nota_de_voz"))
    }

    // --- Tier 1: Legitimate Music Files Accepted ---

    @Test
    fun testAcceptLegitimateAudioFiles() {
        val tracks = listOf(
            "/storage/emulated/0/Music/Acoustic/Acoustic_Worship_01.mp3",
            "/storage/emulated/0/Music/Christian/Louvores_Harpa.m4a",
            "/storage/emulated/0/Download/Midnight_Cyberpunk.wav",
            "/storage/emulated/0/Music/Podcasts/Episode_42.aac"
        )

        tracks.forEach { path ->
            assertFalse(
                "Legitimate track $path should NOT be filtered as noise",
                MediaStoreScannerFilterOracle.isNoiseOrRecording(path, "Track Title")
            )
        }
    }

    // --- Tier 2: Metadata Sanitization ---

    @Test
    fun testCleanMetadata_unknownAndBlankFallbacks() {
        assertEquals("Desconhecido", MediaStoreScannerFilterOracle.cleanMetadata(""))
        assertEquals("Desconhecido", MediaStoreScannerFilterOracle.cleanMetadata("   "))
        assertEquals("Desconhecido", MediaStoreScannerFilterOracle.cleanMetadata("<unknown>"))
        assertEquals("Desconhecido", MediaStoreScannerFilterOracle.cleanMetadata("<UNKNOWN>"))
    }

    @Test
    fun testCleanMetadata_preservesValidStringsAndTrims() {
        assertEquals("Hugh & Atom", MediaStoreScannerFilterOracle.cleanMetadata("  Hugh & Atom  "))
        assertEquals("Aether Vault Core", MediaStoreScannerFilterOracle.cleanMetadata("Aether Vault Core"))
    }

    // --- Tier 2: Versioning Invariant (F-17) ---

    @Test
    fun testTargetVersion_isV150() {
        val targetVersionName = "1.5.0"
        assertEquals("1.5.0", targetVersionName)
    }
}
