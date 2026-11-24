package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.db.DownloadEntity
import com.hughdevelopment.player.data.download.DownloadManager
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.source.MediaStoreScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.URI
import java.util.Locale

/**
 * Challenger M1-2 Empirical Stress Test Suite
 * Rigorously challenges:
 * 1. DownloadCompletedCard synthesized fallback Song logic & collision vulnerabilities
 * 2. MediaStore query selection filter regex/syntax with Opus and standard codecs
 * 3. Telemetry calculations (Speed, ETA, Byte formatting)
 */
class Challenger2PlaybackStressTest {

    // Mirror of DownloadsScreen fallback resolver logic
    object FallbackEngine {
        fun sanitize(text: String): String =
            text.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()

        fun resolveBaseFileName(artist: String, title: String): String {
            val sanitizedTitle = sanitize(title)
            val sanitizedArtist = sanitize(artist)
            return if (sanitizedArtist.isNotBlank() && sanitizedArtist != "Unknown") {
                "$sanitizedArtist - $sanitizedTitle"
            } else sanitizedTitle
        }

        fun computeSongId(videoId: String): Long =
            (videoId.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL)

        fun resolveFinalFile(publicFolder: File, appMusicFolder: File?, baseFileName: String, format: String): File {
            val primaryFile = File(publicFolder, "$baseFileName.$format")
            val fallbackFile = if (appMusicFolder != null) File(appMusicFolder, "$baseFileName.$format") else null
            return when {
                primaryFile.exists() -> primaryFile
                fallbackFile != null && fallbackFile.exists() -> fallbackFile
                else -> primaryFile
            }
        }

        fun findMatchingSong(allSongs: List<Song>, entity: DownloadEntity): Song? {
            return allSongs.find { song ->
                song.title.contains(entity.title, ignoreCase = true) ||
                entity.title.contains(song.title, ignoreCase = true)
            }
        }

        fun findMatchingSongPrecise(allSongs: List<Song>, entity: DownloadEntity, expectedPath: String): Song? {
            // Precise matcher testing path equality first, then exact title
            return allSongs.find { it.dataPath.equals(expectedPath, ignoreCase = true) }
                ?: allSongs.find { it.title.equals(entity.title, ignoreCase = true) }
                ?: findMatchingSong(allSongs, entity)
        }
    }

    // =========================================================================
    // 1. Fallback Synthesized Song: Sanitization & File Resolution Stress Tests
    // =========================================================================

    @Test
    fun testFallback_extremeFilenameSanitization() {
        val testCases = listOf(
            // (artist, title, expectedBase)
            Triple("Artist/Slash\\Back", "Track: Colon*Star?Question\"Quote<Less>Greater|Pipe", "ArtistSlashBack - Track ColonStarQuestionQuoteLessGreaterPipe"),
            Triple("  LeadingSpaces  ", "  TrailingSpaces  ", "LeadingSpaces - TrailingSpaces"),
            Triple("João & Maria", "Ao Vivo (São Paulo) // 2026", "João & Maria - Ao Vivo (São Paulo)  2026"),
            Triple("Unknown", "Simple Track", "Simple Track"),
            Triple("", "Simple Track", "Simple Track"),
            Triple("   ", "Simple Track", "Simple Track"),
            Triple("Unknown", ":::*?<>|", "") // Entirely invalid chars
        )

        for ((artist, title, expected) in testCases) {
            val result = FallbackEngine.resolveBaseFileName(artist, title)
            assertEquals("Failed base filename resolution for artist='$artist', title='$title'", expected, result)
        }
    }

    @Test
    fun testFallback_idGenerationProperties() {
        // Must be non-negative 63-bit integer
        val videoIds = listOf(
            "dQw4w9WgXcQ",
            "a",
            "",
            "longVideoIdWithManyCharacters_1234567890_ABCDEFGHIJK",
            "hashCodeNegativeCase123",
            "hashCodeZeroTest"
        )

        for (id in videoIds) {
            val songId = FallbackEngine.computeSongId(id)
            assertTrue("Song ID must be >= 0 for id='$id'", songId >= 0L)
            assertTrue("Song ID must fit in positive 63-bit range", songId <= Long.MAX_VALUE)
        }
    }

    @Test
    fun testFallback_priorityDiskResolution() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "AetherChallenger2DiskTest_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        val publicDir = File(tempDir, "PublicMusic")
        val appDir = File(tempDir, "AppMusic")
        publicDir.mkdirs()
        appDir.mkdirs()

        try {
            // Case 1: Primary exists -> returns primary
            val primary = File(publicDir, "Hermes - Echo.opus")
            primary.writeBytes(ByteArray(100))
            var resolved = FallbackEngine.resolveFinalFile(publicDir, appDir, "Hermes - Echo", "opus")
            assertEquals(primary.absolutePath, resolved.absolutePath)

            // Case 2: Only fallback exists -> returns fallback
            primary.delete()
            val fallback = File(appDir, "Hermes - Echo.opus")
            fallback.writeBytes(ByteArray(100))
            resolved = FallbackEngine.resolveFinalFile(publicDir, appDir, "Hermes - Echo", "opus")
            assertEquals(fallback.absolutePath, resolved.absolutePath)

            // Case 3: Neither exists -> defaults to primary target for new write
            fallback.delete()
            resolved = FallbackEngine.resolveFinalFile(publicDir, appDir, "Hermes - Echo", "opus")
            assertEquals(primary.absolutePath, resolved.absolutePath)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    // =========================================================================
    // 2. Playback Matching: Substring Collision Vulnerability Challenge
    // =========================================================================

    data class MatchableTrack(val title: String, val artist: String, val path: String)

    @Test
    fun testMatchingSong_substringCollisionVulnerability() {
        // Construct a realistic library
        val library = listOf(
            MatchableTrack("One", "Metallica", "/storage/emulated/0/Music/Metallica - One.mp3"),
            MatchableTrack("Someone Like You", "Adele", "/storage/emulated/0/Music/Adele - Someone Like You.mp3"),
            MatchableTrack("Take Me Home, Country Roads", "John Denver", "/storage/emulated/0/Music/John Denver - Take Me Home.mp3")
        )

        // Scenario 1: User downloads "One" by U2
        val downloadedTitleU2 = "One"
        val matchU2 = library.find { song ->
            song.title.contains(downloadedTitleU2, ignoreCase = true) ||
            downloadedTitleU2.contains(song.title, ignoreCase = true)
        }
        assertNotNull(matchU2)
        // Substring matcher finds "One" by Metallica because title is identical or substring
        assertEquals("Metallica", matchU2?.artist)

        // Scenario 2: Reverse substring collision
        // User downloads "Home" by Edward Sharpe
        val downloadedTitleHome = "Home"
        val matchHome = library.find { song ->
            song.title.contains(downloadedTitleHome, ignoreCase = true) ||
            downloadedTitleHome.contains(song.title, ignoreCase = true)
        }
        // Vulnerability: "Take Me Home, Country Roads".contains("Home") == true!
        // The user downloaded Edward Sharpe's "Home", but substring match incorrectly hijacked John Denver!
        assertNotNull("Substring matcher hijacks John Denver because title contains 'Home'", matchHome)
        assertEquals("Take Me Home, Country Roads", matchHome?.title)
        assertEquals("John Denver", matchHome?.artist)

        // Scenario 3: Precise match defense verification (matching on exact path or exact title + artist)
        val expectedDownloadedPath = "/storage/emulated/0/Music/AetherVault/Edward Sharpe - Home.m4a"
        val preciseMatch = library.find { it.path.equals(expectedDownloadedPath, ignoreCase = true) }
            ?: library.find { it.title.equals(downloadedTitleHome, ignoreCase = true) && it.artist.equals("Edward Sharpe", ignoreCase = true) }
        
        // Under precise matching, it correctly returns null (triggering synthesized fallback) rather than hijacking John Denver!
        org.junit.Assert.assertNull("Precise matcher correctly avoids hijacking unrelated artist's track", preciseMatch)
    }

    private fun matchThreeTier(
        tracks: List<MatchableTrack>,
        entityTitle: String,
        entityArtist: String,
        finalPath: String
    ): MatchableTrack? {
        val hasValidArtist = entityArtist.isNotBlank() && entityArtist != "Unknown"
        // Tier 1: Match por caminho absoluto no disco
        return tracks.find { it.path.equals(finalPath, ignoreCase = true) }
            // Tier 2: Match exato por Título e Artista
            ?: tracks.find { song ->
                song.title.equals(entityTitle, ignoreCase = true) &&
                    (!hasValidArtist || song.artist.equals(entityArtist, ignoreCase = true))
            }
            // Tier 3: Match por substring de título protegido por artista conhecido e tamanho de título >= 4
            ?: if (hasValidArtist && entityTitle.length >= 4) {
                tracks.find { song ->
                    song.artist.equals(entityArtist, ignoreCase = true) &&
                        (song.title.contains(entityTitle, ignoreCase = true) ||
                         entityTitle.contains(song.title, ignoreCase = true))
                }
            } else null
    }

    @Test
    fun testThreeTierMatching_exactPathAndGuardedSubstringCollisionPrevention() {
        val library = listOf(
            MatchableTrack("Take Me Home, Country Roads", "John Denver", "/storage/emulated/0/Music/John Denver - Take Me Home.mp3"),
            MatchableTrack("In The End", "Linkin Park", "/storage/emulated/0/Music/Linkin Park - In The End.mp3"),
            MatchableTrack("Starboy (Official Video)", "The Weeknd", "/storage/emulated/0/Music/The Weeknd - Starboy Video.mp3"),
            MatchableTrack("Echoes", "Pink Floyd", "/storage/emulated/0/Music/AetherVault/Pink Floyd - Echoes.opus"),
            MatchableTrack("Echoes (Live at Pompeii)", "Pink Floyd", "/storage/emulated/0/Music/Live/Echoes.mp3")
        )

        // 1. Tier 1: Exact disk path priority
        val tier1Result = matchThreeTier(
            tracks = library,
            entityTitle = "Echoes",
            entityArtist = "Pink Floyd",
            finalPath = "/storage/emulated/0/Music/AetherVault/Pink Floyd - Echoes.opus"
        )
        assertNotNull("Tier 1 must match exact path", tier1Result)
        assertEquals("/storage/emulated/0/Music/AetherVault/Pink Floyd - Echoes.opus", tier1Result?.path)

        // 1b. Tier 1: Case-insensitive path match
        val tier1CaseResult = matchThreeTier(
            tracks = library,
            entityTitle = "Echoes",
            entityArtist = "Pink Floyd",
            finalPath = "/STORAGE/EMULATED/0/music/aethervault/pink floyd - echoes.opus"
        )
        assertNotNull("Tier 1 must match case-insensitively on path", tier1CaseResult)
        assertEquals("/storage/emulated/0/Music/AetherVault/Pink Floyd - Echoes.opus", tier1CaseResult?.path)

        // 2. Tier 2: Exact Title + Artist when path does not match
        val tier2Result = matchThreeTier(
            tracks = library,
            entityTitle = "Take Me Home, Country Roads",
            entityArtist = "John Denver",
            finalPath = "/storage/emulated/0/Music/AetherVault/John Denver - Take Me Home, Country Roads.m4a"
        )
        assertNotNull("Tier 2 must match exact title and artist", tier2Result)
        assertEquals("Take Me Home, Country Roads", tier2Result?.title)
        assertEquals("John Denver", tier2Result?.artist)

        // 3. Tier 3: Guarded Substring match when artist matches and title.length >= 4
        val tier3Result = matchThreeTier(
            tracks = library,
            entityTitle = "Starboy",
            entityArtist = "The Weeknd",
            finalPath = "/storage/emulated/0/Music/AetherVault/The Weeknd - Starboy.m4a"
        )
        assertNotNull("Tier 3 must match substring when artist matches and length >= 4", tier3Result)
        assertEquals("Starboy (Official Video)", tier3Result?.title)

        // 4. Collision Defense: Substring collision with different artist is blocked
        val blockedCollision = matchThreeTier(
            tracks = library,
            entityTitle = "Home",
            entityArtist = "Edward Sharpe",
            finalPath = "/storage/emulated/0/Music/AetherVault/Edward Sharpe - Home.m4a"
        )
        org.junit.Assert.assertNull("Collision defense: Must NOT hijack John Denver track", blockedCollision)

        // 5. Collision Defense: Short title (< 4 chars) without valid artist is blocked
        val shortTitleCollision = matchThreeTier(
            tracks = library,
            entityTitle = "In",
            entityArtist = "Unknown",
            finalPath = "/storage/emulated/0/Music/AetherVault/In.m4a"
        )
        org.junit.Assert.assertNull("Short title (< 4 chars) without valid artist must NOT match In The End", shortTitleCollision)

        // 6. Collision Defense: Unknown artist does NOT trigger Tier 3 substring
        val unknownArtistSubstring = matchThreeTier(
            tracks = library,
            entityTitle = "Echoes",
            entityArtist = "Unknown",
            finalPath = "/storage/emulated/0/Music/Other/Echoes.m4a"
        )
        // Exact title matches "Echoes" on Tier 2 because hasValidArtist is false
        assertNotNull("Exact title matches on Tier 2 even if artist is unknown", unknownArtistSubstring)
        assertEquals("Echoes", unknownArtistSubstring?.title)
    }

    // =========================================================================
    // 3. MediaStore Query Selection Syntax & Opus Extension Verification
    // =========================================================================

    @Test
    fun testMediaStoreSelection_sqlStructureAndSyntaxIntegrity() {
        val selection = MediaStoreScanner.DEFAULT_SELECTION

        // 1. Verify required operators and clauses
        assertTrue("Selection must contain IS_MUSIC condition", selection.contains("is_music != 0") || selection.contains("is_music!=0") || selection.contains("IS_MUSIC"))
        assertTrue("Selection must contain %.opus filter", selection.contains("LIKE '%.opus'"))
        assertTrue("Selection must contain %.mp3 filter", selection.contains("LIKE '%.mp3'"))
        assertTrue("Selection must contain %.m4a filter", selection.contains("LIKE '%.m4a'"))
        assertTrue("Selection must contain %.wav filter", selection.contains("LIKE '%.wav'"))
        assertTrue("Selection must contain %.aac filter", selection.contains("LIKE '%.aac'"))
        assertTrue("Selection must filter short audio >= 5000ms", selection.contains(">= 5000") || selection.contains(">=5000"))
        assertTrue("Selection must exclude ringtones", selection.contains("is_ringtone == 0") || selection.contains("is_ringtone = 0") || selection.contains("IS_RINGTONE"))
        assertTrue("Selection must exclude notifications", selection.contains("is_notification == 0") || selection.contains("is_notification = 0") || selection.contains("IS_NOTIFICATION"))
        assertTrue("Selection must exclude alarms", selection.contains("is_alarm == 0") || selection.contains("is_alarm = 0") || selection.contains("IS_ALARM"))

        // 2. Verify parenthesis balancing (crucial for SQL operator precedence)
        val openParenCount = selection.count { it == '(' }
        val closeParenCount = selection.count { it == ')' }
        assertEquals("SQL parenthesis must be balanced", openParenCount, closeParenCount)

        // 3. Verify format group is properly parenthesized so OR does not greedily bind outside
        val formatGroupRegex = Regex("\\([^)]*LIKE '%.opus'[^)]*\\)")
        assertTrue("Format extension filters must be enclosed within parenthesized group", formatGroupRegex.containsMatchIn(selection))
    }

    // =========================================================================
    // 4. Download Telemetry Formatting Stress Tests
    // =========================================================================

    @Test
    fun testDownloadTelemetry_speedFormattingBoundaries() {
        assertEquals("0 KB/s", DownloadManager.formatSpeed(0L))
        assertEquals("0 KB/s", DownloadManager.formatSpeed(-100L))
        assertEquals("500 B/s", DownloadManager.formatSpeed(500L))
        assertEquals("1023 B/s", DownloadManager.formatSpeed(1023L))
        assertEquals("1 KB/s", DownloadManager.formatSpeed(1024L))
        assertEquals("500 KB/s", DownloadManager.formatSpeed(512000L))
        assertEquals("1.0 MB/s", DownloadManager.formatSpeed(1024L * 1024L))
        assertEquals("2.5 MB/s", DownloadManager.formatSpeed((2.5 * 1024 * 1024).toLong()))
        assertEquals("10.8 MB/s", DownloadManager.formatSpeed((10.8 * 1024 * 1024).toLong()))
    }

    @Test
    fun testDownloadTelemetry_etaFormattingBoundaries() {
        // Invalid or edge inputs -> "--:--"
        assertEquals("--:--", DownloadManager.formatEta(0L, 0L, 0L))
        assertEquals("--:--", DownloadManager.formatEta(500L, 0L, 100L))
        assertEquals("--:--", DownloadManager.formatEta(100L, 100L, 100L))
        assertEquals("--:--", DownloadManager.formatEta(150L, 100L, 100L))
        assertEquals("--:--", DownloadManager.formatEta(0L, 1000L, 0L))
        assertEquals("--:--", DownloadManager.formatEta(0L, 1000L, -10L))

        // Normal seconds (< 60s)
        assertEquals("00:10", DownloadManager.formatEta(0L, 1000L, 100L)) // 10s remaining
        assertEquals("00:45", DownloadManager.formatEta(100L, 1000L, 20L)) // 45s remaining

        // Minutes & seconds (< 1 hour)
        assertEquals("02:05", DownloadManager.formatEta(0L, 125000L, 1000L)) // 125s = 2m 5s
        assertEquals("59:59", DownloadManager.formatEta(0L, 3599000L, 1000L)) // 3599s = 59m 59s

        // Hours format (>= 1 hour)
        assertEquals("1h 00m", DownloadManager.formatEta(0L, 3600000L, 1000L)) // 3600s = 1h 0m
        assertEquals("2h 15m", DownloadManager.formatEta(0L, 8130000L, 1000L)) // 8130s = 2h 15m
    }

    @Test
    fun testDownloadTelemetry_byteFormattingBoundaries() {
        assertEquals("0 B", DownloadManager.formatBytes(0L))
        assertEquals("512 B", DownloadManager.formatBytes(512L))
        assertEquals("1 KB", DownloadManager.formatBytes(1024L))
        assertEquals("1 KB", DownloadManager.formatBytes(1536L))
        assertEquals("1.0 MB", DownloadManager.formatBytes(1024L * 1024L))
        assertEquals("12.4 MB", DownloadManager.formatBytes((12.4 * 1024 * 1024).toLong()))
        assertEquals("1.00 GB", DownloadManager.formatBytes(1024L * 1024L * 1024L))
    }
}
