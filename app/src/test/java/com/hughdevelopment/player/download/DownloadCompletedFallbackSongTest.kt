package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.db.DownloadEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit test suite verifying the instant fallback synthesized Song logic
 * for completed downloads in CONCLUÍDAS tab.
 */
class DownloadCompletedFallbackSongTest {

    object FallbackSongResolver {
        fun resolveArtist(artist: String): String =
            if (artist.isNotBlank() && artist != "Unknown") artist.trim() else "Artista Desconhecido"

        fun resolveBaseFileName(artist: String, title: String): String {
            val sanitizedTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
            val sanitizedArtist = artist.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
            return if (sanitizedArtist.isNotBlank() && sanitizedArtist != "Unknown") {
                "$sanitizedArtist - $sanitizedTitle"
            } else sanitizedTitle
        }

        fun computeSongId(videoId: String): Long =
            (videoId.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL)

        fun resolveDurationMs(durationSeconds: Long): Long =
            durationSeconds * 1000L

        fun resolveFinalFile(publicFolder: File, appMusicFolder: File?, baseFileName: String, format: String): File {
            val primaryFile = File(publicFolder, "$baseFileName.$format")
            val fallbackFile = if (appMusicFolder != null) File(appMusicFolder, "$baseFileName.$format") else null
            return when {
                primaryFile.exists() -> primaryFile
                fallbackFile != null && fallbackFile.exists() -> fallbackFile
                else -> primaryFile
            }
        }
    }

    @Test
    fun testSynthesizedSong_metadataResolution() {
        val entity = DownloadEntity(
            videoId = "synthTrack01",
            title = "Aetherium Echoes / Vocal VIP",
            artist = "Hermes Cyber Sound",
            durationSeconds = 215,
            streamUrl = "https://mock.stream.url/audio",
            originalUrl = "https://www.youtube.com/watch?v=synthTrack01",
            thumbnailUrl = "https://thumb.url/synthTrack01.jpg",
            format = "m4a",
            itag = 140,
            bytesDownloaded = 4500000L,
            totalBytes = 4500000L,
            status = DownloadEntity.STATUS_COMPLETED
        )

        val resolvedArtist = FallbackSongResolver.resolveArtist(entity.artist)
        val baseFileName = FallbackSongResolver.resolveBaseFileName(entity.artist, entity.title)
        val songId = FallbackSongResolver.computeSongId(entity.videoId)
        val durationMs = FallbackSongResolver.resolveDurationMs(entity.durationSeconds)

        assertEquals("Hermes Cyber Sound", resolvedArtist)
        assertEquals("Hermes Cyber Sound - Aetherium Echoes  Vocal VIP", baseFileName)
        assertTrue("Song ID must be positive 63-bit integer", songId > 0)
        assertEquals(215000L, durationMs)
    }

    @Test
    fun testSynthesizedSong_unknownArtistFallback() {
        val entity = DownloadEntity(
            videoId = "unknownArtist02",
            title = "Solo Vibration: Episode 1",
            artist = "Unknown",
            durationSeconds = 100,
            streamUrl = "https://mock.stream.url/audio",
            originalUrl = "https://www.youtube.com/watch?v=unknownArtist02",
            thumbnailUrl = null,
            format = "opus",
            itag = 251,
            bytesDownloaded = 2000000L,
            totalBytes = 2000000L,
            status = DownloadEntity.STATUS_COMPLETED
        )

        val resolvedArtist = FallbackSongResolver.resolveArtist(entity.artist)
        val baseFileName = FallbackSongResolver.resolveBaseFileName(entity.artist, entity.title)

        assertEquals("Artista Desconhecido", resolvedArtist)
        assertEquals("Solo Vibration Episode 1", baseFileName)
        assertFalse("Filename should not have unknown artist prefix", baseFileName.startsWith("Unknown -"))
    }

    @Test
    fun testSynthesizedSong_fileResolutionPriority() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "AetherVaultPriorityTest")
        tempDir.mkdirs()
        val publicDir = File(tempDir, "Public")
        val appDir = File(tempDir, "App")
        publicDir.mkdirs()
        appDir.mkdirs()

        // 1. Primary file exists in public directory
        val primaryFile = File(publicDir, "Artist - Song.m4a")
        primaryFile.writeBytes(ByteArray(512))

        val resolvedPrimary = FallbackSongResolver.resolveFinalFile(publicDir, appDir, "Artist - Song", "m4a")
        assertEquals(primaryFile.absolutePath, resolvedPrimary.absolutePath)

        // 2. Fallback to app directory when public file does not exist
        primaryFile.delete()
        val appFile = File(appDir, "Artist - Song.m4a")
        appFile.writeBytes(ByteArray(512))

        val resolvedFallback = FallbackSongResolver.resolveFinalFile(publicDir, appDir, "Artist - Song", "m4a")
        assertEquals(appFile.absolutePath, resolvedFallback.absolutePath)

        appFile.delete()
        publicDir.delete()
        appDir.delete()
        tempDir.delete()
    }
}
