package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.download.ExtractedAudioInfo
import com.hughdevelopment.player.data.download.YouTubeExtractorHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeExtractorHelperFallbackTest {

    @Test
    fun testExtractVideoId_variousFormats() {
        // Standard watch
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("http://youtube.com/watch?v=dQw4w9WgXcQ&feature=share"))

        // Short link
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("https://youtu.be/dQw4w9WgXcQ?si=abcdef12345"))

        // Shorts
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ"))

        // Live / Embed
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("https://www.youtube.com/live/dQw4w9WgXcQ"))

        // Raw 11-character video ID
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId("dQw4w9WgXcQ"))
    }

    @Test
    fun testExtractVideoId_invalidReturnsNull() {
        assertNull(YouTubeExtractorHelper.extractVideoId("https://google.com"))
        assertNull(YouTubeExtractorHelper.extractVideoId("not-a-valid-youtube-url"))
        assertNull(YouTubeExtractorHelper.extractVideoId(""))
        assertNull(YouTubeExtractorHelper.extractVideoId("12345")) // too short
    }

    @Test
    fun testIsYouTubeUrl() {
        assertTrue(YouTubeExtractorHelper.isYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(YouTubeExtractorHelper.isYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        assertFalse(YouTubeExtractorHelper.isYouTubeUrl("https://spotify.com/track/123"))
    }

    @Test
    fun testSanitizeFileName_removesInvalidCharacters() {
        val dirty = "Artist / Song: \"Best\" <Live> | 2026? *Remix*"
        val clean = YouTubeExtractorHelper.sanitizeFileName(dirty)

        assertFalse("Clean string must not contain slashes", clean.contains("/"))
        assertFalse("Clean string must not contain colons", clean.contains(":"))
        assertFalse("Clean string must not contain quotes", clean.contains("\""))
        assertFalse("Clean string must not contain angle brackets", clean.contains("<") || clean.contains(">"))
        assertFalse("Clean string must not contain pipes", clean.contains("|"))
        assertFalse("Clean string must not contain question marks", clean.contains("?"))
        assertFalse("Clean string must not contain asterisks", clean.contains("*"))
        assertEquals("Artist  Song Best Live  2026 Remix", clean)
    }

    @Test
    fun testExtractedAudioInfo_defaultContract() {
        val info = ExtractedAudioInfo(
            videoId = "vid12345678",
            title = "Aether Track",
            artist = "Hermes",
            durationSeconds = 120,
            streamUrl = "https://stream.audio/test",
            format = "m4a",
            thumbnailUrl = "https://thumb.url/1.jpg"
        )

        assertEquals("https://www.youtube.com/watch?v=vid12345678", info.originalUrl)
        assertEquals(140, info.itag)
        assertEquals(128, info.averageBitrate)
        assertEquals("PRIMARY", info.extractorTier)
    }
}
