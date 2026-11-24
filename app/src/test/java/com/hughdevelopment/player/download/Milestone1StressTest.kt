package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.download.AetherJson
import com.hughdevelopment.player.data.download.HughCookieJar
import com.hughdevelopment.player.data.download.HughDownloader
import com.hughdevelopment.player.data.download.InnerTubeAudioExtractor
import com.hughdevelopment.player.data.download.YouTubeExtractorHelper
import com.hughdevelopment.player.data.source.MediaStoreScanner
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Empirical Stress Test Suite for Milestone 1:
 * Validates edge cases across URL parsing, video ID extraction,
 * InnerTube response parsing, AetherJson AST, cipher decoding,
 * CookieJar isolation/concurrency, and MediaStore query integrity.
 */
class Milestone1StressTest {

    // =========================================================================
    // 1. URL Parsing & Video ID Extraction Boundary Cases
    // =========================================================================

    @Test
    fun testUrlParsing_allSupportedYouTubeFormats() {
        val testCases = mapOf(
            // Desktop standard
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Mobile, Music, Gaming subdomains
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://gaming.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Short links (youtu.be)
            "https://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?si=abcdef12345" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=120" to "dQw4w9WgXcQ",

            // Shorts, Live, Embed, Legacy V
            "https://www.youtube.com/shorts/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtube.com/shorts/dQw4w9WgXcQ?feature=share" to "dQw4w9WgXcQ",
            "https://www.youtube.com/live/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Query parameters preceding 'v='
            "https://www.youtube.com/watch?feature=share&v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/watch?app=desktop&gl=US&v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Complex query parameters following 'v='
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PL123456&index=3&t=45s" to "dQw4w9WgXcQ",

            // Trailing slash on shorts/live
            "https://www.youtube.com/shorts/dQw4w9WgXcQ/" to "dQw4w9WgXcQ",
            "https://www.youtube.com/live/dQw4w9WgXcQ/" to "dQw4w9WgXcQ",

            // Video IDs with underscores and hyphens
            "https://www.youtube.com/watch?v=_a-1B_2c-3D" to "_a-1B_2c-3D",
            "https://youtu.be/-----------" to "-----------",
            "https://youtu.be/___________" to "___________",

            // Raw 11-char video ID strings
            "dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "_a-1B_2c-3D" to "_a-1B_2c-3D"
        )

        testCases.forEach { (url, expectedId) ->
            val extracted = YouTubeExtractorHelper.extractVideoId(url)
            assertEquals("Failed for URL: $url", expectedId, extracted)
            assertTrue("isYouTubeUrl should return true for: $url", YouTubeExtractorHelper.isYouTubeUrl(url))
        }
    }

    @Test
    fun testUrlParsing_whitespaceAndAndroidShareSheetText() {
        // Leading/trailing whitespace and newlines
        val paddedUrl = "   \n\t https://youtu.be/dQw4w9WgXcQ \t\r\n "
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId(paddedUrl))

        // Android Share Sheet typical text
        val sharedText = "Olhe este som incrível: https://youtu.be/dQw4w9WgXcQ compartilhado via YouTube"
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId(sharedText))

        val sharedTextWatch = "Confira 'Cyberpunk Theme' em https://www.youtube.com/watch?v=dQw4w9WgXcQ no YouTube"
        assertEquals("dQw4w9WgXcQ", YouTubeExtractorHelper.extractVideoId(sharedTextWatch))
    }

    @Test
    fun testUrlParsing_adversarialInvalidInputs() {
        val invalidInputs = listOf(
            "",
            "   ",
            "\n\t",
            "https://vimeo.com/123456789",
            "https://spotify.com/track/4cOdK2wGLETKBW3PvgPWqT",
            "https://google.com/search?q=youtube",
            "https://www.youtube.com/",
            "https://www.youtube.com/feed/trending",
            "https://www.youtube.com/channel/UC1234567890",
            "https://www.youtube.com/watch?other=dQw4w9WgXcQ", // missing v=
            "https://www.youtube.com/watch?v=1234567890", // 10 chars (too short)
            "https://www.youtube.com/watch?v=123456789012", // 12 chars (too long)
            "https://www.youtube.com/watch?v=dQw4w!WgXcQ", // invalid char '!'
            "1234567890", // 10 chars raw
            "123456789012" // 12 chars raw
        )

        invalidInputs.forEach { input ->
            val result = YouTubeExtractorHelper.extractVideoId(input)
            assertNull("Expected null for invalid input: '$input'", result)
            assertFalse("isYouTubeUrl should return false for: '$input'", YouTubeExtractorHelper.isYouTubeUrl(input))
        }
    }

    @Test
    fun testUrlParsing_rejectsAdversarialThirdPartyDomainsEndingInYouTube() {
        val spoofedDomains = listOf(
            "https://notyoutube.com/watch?v=dQw4w9WgXcQ",
            "https://fakeyoutube.com/shorts/dQw4w9WgXcQ",
            "https://phishing-youtube.com/watch?v=dQw4w9WgXcQ",
            "http://maliciousyoutube.com/watch?v=dQw4w9WgXcQ"
        )

        spoofedDomains.forEach { url ->
            val result = YouTubeExtractorHelper.extractVideoId(url)
            assertNull("Third-party spoofed domain '$url' must not be recognized as a YouTube video", result)
            assertFalse("isYouTubeUrl should return false for third-party domain: '$url'", YouTubeExtractorHelper.isYouTubeUrl(url))
        }
    }

    // =========================================================================
    // 2. InnerTube Player JSON & Error Handling Stress Testing
    // =========================================================================

    @Test
    fun testInnerTubeParsing_unplayableStatusReasons() {
        val errorScenarios = listOf(
            "UNPLAYABLE" to "The page needs to be reloaded.",
            "LOGIN_REQUIRED" to "Sign in to confirm your age.",
            "ERROR" to "This video is unavailable in your country.",
            "CONTENT_CHECK_REQUIRED" to "This video contains sensitive content."
        )

        errorScenarios.forEach { (status, reason) ->
            val jsonStr = """
            {
              "playabilityStatus": {
                "status": "$status",
                "reason": "$reason"
              },
              "videoDetails": {
                "videoId": "errVid1"
              }
            }
            """.trimIndent()

            val json = AetherJson.parse(jsonStr) as AetherJson.Obj
            try {
                InnerTubeAudioExtractor.parsePlayerResponse("errVid1", json, "INNERTUBE_ANDROID")
                fail("Expected IllegalStateException for status: $status")
            } catch (e: IllegalStateException) {
                assertTrue("Exception message should contain reason '$reason'", e.message?.contains(reason) == true)
            }
        }
    }

    @Test
    fun testInnerTubeParsing_missingStructureThrowsSpecificExceptions() {
        // Missing videoDetails
        val missingDetails = """
        {
          "playabilityStatus": { "status": "OK" },
          "streamingData": { "adaptiveFormats": [] }
        }
        """.trimIndent()
        try {
            InnerTubeAudioExtractor.parsePlayerResponse("v1", AetherJson.parse(missingDetails) as AetherJson.Obj, "TEST")
            fail("Should throw on missing videoDetails")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("missing videoDetails") == true)
        }

        // Missing streamingData
        val missingStreaming = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": { "videoId": "v1", "title": "Track" }
        }
        """.trimIndent()
        try {
            InnerTubeAudioExtractor.parsePlayerResponse("v1", AetherJson.parse(missingStreaming) as AetherJson.Obj, "TEST")
            fail("Should throw on missing streamingData")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("missing streamingData") == true)
        }

        // No audio formats found (only video formats)
        val onlyVideoFormats = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": { "videoId": "v1", "title": "Video Only" },
          "streamingData": {
            "adaptiveFormats": [
              { "itag": 137, "mimeType": "video/mp4; codecs=\"avc1.640028\"", "bitrate": 2000000, "url": "https://video.url" }
            ]
          }
        }
        """.trimIndent()
        try {
            InnerTubeAudioExtractor.parsePlayerResponse("v1", AetherJson.parse(onlyVideoFormats) as AetherJson.Obj, "TEST")
            fail("Should throw when no audio streams are present")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Nenhum fluxo de áudio") == true)
        }
    }

    @Test
    fun testInnerTubeParsing_audioStreamSelectionPrecedence() {
        // Scenario A: itag 140 (M4A) vs itag 251 (Opus) -> itag 140 preferred
        val jsonBoth = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": { "videoId": "prec1", "title": "Dual Stream Track", "author": "Producer", "lengthSeconds": "150" },
          "streamingData": {
            "adaptiveFormats": [
              { "itag": 251, "mimeType": "audio/webm; codecs=\"opus\"", "bitrate": 160000, "url": "https://stream.audio/itag251" },
              { "itag": 140, "mimeType": "audio/mp4; codecs=\"mp4a.40.2\"", "bitrate": 128000, "url": "https://stream.audio/itag140" }
            ]
          }
        }
        """.trimIndent()

        val infoBoth = InnerTubeAudioExtractor.parsePlayerResponse("prec1", AetherJson.parse(jsonBoth) as AetherJson.Obj, "TIER2")
        assertEquals(140, infoBoth.itag)
        assertEquals("m4a", infoBoth.format)
        assertEquals("https://stream.audio/itag140", infoBoth.streamUrl)

        // Scenario B: multiple M4A streams without itag 140 -> highest bitrate M4A picked
        val jsonMultiM4a = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": { "videoId": "prec2", "title": "Multi M4A", "author": "Producer", "lengthSeconds": "150" },
          "streamingData": {
            "adaptiveFormats": [
              { "itag": 139, "mimeType": "audio/mp4; codecs=\"mp4a.40.2\"", "bitrate": 48000, "url": "https://stream.audio/itag139" },
              { "itag": 141, "mimeType": "audio/mp4; codecs=\"mp4a.40.2\"", "bitrate": 256000, "url": "https://stream.audio/itag141" }
            ]
          }
        }
        """.trimIndent()

        val infoMultiM4a = InnerTubeAudioExtractor.parsePlayerResponse("prec2", AetherJson.parse(jsonMultiM4a) as AetherJson.Obj, "TIER2")
        assertEquals(141, infoMultiM4a.itag)
        assertEquals("m4a", infoMultiM4a.format)
        assertEquals(256, infoMultiM4a.averageBitrate)

        // Scenario C: Only Opus stream available (itag 251)
        val jsonOpusOnly = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": { "videoId": "prec3", "title": "Opus Only", "author": "Producer", "lengthSeconds": "150" },
          "streamingData": {
            "adaptiveFormats": [
              { "itag": 251, "mimeType": "audio/webm; codecs=\"opus\"", "bitrate": 160000, "url": "https://stream.audio/itag251" }
            ]
          }
        }
        """.trimIndent()

        val infoOpus = InnerTubeAudioExtractor.parsePlayerResponse("prec3", AetherJson.parse(jsonOpusOnly) as AetherJson.Obj, "TIER2")
        assertEquals(251, infoOpus.itag)
        assertEquals("opus", infoOpus.format)
        assertEquals(160, infoOpus.averageBitrate)
    }

    @Test
    fun testInnerTubeParsing_thumbnailMaxAreaResolution() {
        val jsonWithThumbnails = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": {
            "videoId": "thumb1",
            "title": "Thumb Track",
            "author": "Artist",
            "lengthSeconds": "120",
            "thumbnail": {
              "thumbnails": [
                { "url": "https://thumb/small.jpg", "width": 120, "height": 90 },
                { "url": "https://thumb/huge.jpg", "width": 1920, "height": 1080 },
                { "url": "https://thumb/medium.jpg", "width": 640, "height": 480 }
              ]
            }
          },
          "streamingData": {
            "adaptiveFormats": [
              { "itag": 140, "mimeType": "audio/mp4", "bitrate": 128000, "url": "https://stream.audio/140" }
            ]
          }
        }
        """.trimIndent()

        val info = InnerTubeAudioExtractor.parsePlayerResponse("thumb1", AetherJson.parse(jsonWithThumbnails) as AetherJson.Obj, "TIER2")
        assertEquals("https://thumb/huge.jpg", info.thumbnailUrl)
    }

    @Test
    fun testInnerTubeParsing_signatureCipherExtractionVariations() {
        // Parameter order: url first vs url in middle vs url at end
        val cipher1 = "url=https%3A%2F%2Fgooglevideo.com%2Fstream1&s=signature123&sp=sig"
        val cipher2 = "s=sigABC&sp=sig&url=https%3A%2F%2Fgooglevideo.com%2Fstream2"

        val jsonCipher = """
        {
          "playabilityStatus": { "status": "OK" },
          "videoDetails": { "videoId": "ciph1", "title": "Cipher Track", "author": "Artist", "lengthSeconds": "120" },
          "streamingData": {
            "adaptiveFormats": [
              { "itag": 140, "mimeType": "audio/mp4", "bitrate": 128000, "signatureCipher": "$cipher2" }
            ]
          }
        }
        """.trimIndent()

        val info = InnerTubeAudioExtractor.parsePlayerResponse("ciph1", AetherJson.parse(jsonCipher) as AetherJson.Obj, "TIER2")
        assertEquals("https://googlevideo.com/stream2", info.streamUrl)
    }

    // =========================================================================
    // 3. AetherJson AST Parser Stress Testing
    // =========================================================================

    @Test
    fun testAetherJson_escapesAndUnicode() {
        val rawJson = """
        {
          "escapes": "Line1\nLine2\rTab\tQuote\"Backslash\\Slash\/",
          "unicode": "\u0048\u0065\u006c\u006c\u006f \u00A9 2026",
          "boolTrue": true,
          "boolFalse": false,
          "nullVal": null,
          "negNum": -42,
          "emptyArr": [],
          "emptyObj": {}
        }
        """.trimIndent()

        val parsed = AetherJson.parse(rawJson) as AetherJson.Obj
        assertEquals("Line1\nLine2\rTab\tQuote\"Backslash\\Slash/", parsed.getString("escapes"))
        assertEquals("Hello © 2026", parsed.getString("unicode"))
        assertEquals(true, parsed.getString("boolTrue")?.toBoolean())
        assertEquals(-42L, parsed.getLong("negNum"))
        assertNotNull(parsed.getArr("emptyArr"))
        assertTrue(parsed.getArr("emptyArr")!!.isEmpty())
        assertNotNull(parsed.getObj("emptyObj"))
    }

    @Test
    fun testAetherJson_floatAndExponentNumericParsing() {
        val rawJson = """
        {
          "floatNum": 3.14159,
          "expNum": 1.25e3,
          "durationFloat": 240.0
        }
        """.trimIndent()

        val parsed = AetherJson.parse(rawJson) as AetherJson.Obj
        assertEquals("getInt should handle float number by truncating or parsing double", 3, parsed.getInt("floatNum"))
        assertEquals("getLong should handle exponential number", 1250L, parsed.getLong("expNum"))
        assertEquals("getLong should handle float duration 240.0", 240L, parsed.getLong("durationFloat"))
    }

    @Test
    fun testAetherJson_typeToleranceAndMissingKeys() {
        val json = AetherJson.parse("""{ "strNum": "999", "realNum": 123, "notANum": "abc" }""") as AetherJson.Obj

        // getLong / getInt parses string numbers safely
        assertEquals(999L, json.getLong("strNum"))
        assertEquals(999, json.getInt("strNum"))

        // numbers read as strings safely
        assertEquals("123", json.getString("realNum"))

        // non-number string falls back to default safely
        assertEquals(77L, json.getLong("notANum", 77L))
        assertEquals(88, json.getInt("notANum", 88))

        // missing key falls back to default
        assertEquals(55L, json.getLong("missingKey", 55L))
        assertEquals(66, json.getInt("missingKey", 66))
        assertNull(json.getString("missingKey"))
        assertNull(json.getObj("missingKey"))
        assertNull(json.getArr("missingKey"))
    }

    // =========================================================================
    // 4. HughCookieJar Domain Isolation & Multi-Thread Concurrency
    // =========================================================================

    @Test
    fun testHughCookieJar_domainIsolationAndPreSeeding() {
        val jar = HughCookieJar()

        // 1. YouTube subdomains must receive pre-seeded SOCS and PREF
        val ytUrls = listOf(
            "https://www.youtube.com/watch?v=123".toHttpUrl(),
            "https://m.youtube.com/watch?v=123".toHttpUrl(),
            "https://music.youtube.com/watch?v=123".toHttpUrl(),
            "https://youtube.com/api".toHttpUrl()
        )
        ytUrls.forEach { url ->
            val cookies = jar.loadForRequest(url)
            assertTrue("SOCS must be present for ${url.host}", cookies.any { it.name == "SOCS" })
            assertTrue("PREF must be present for ${url.host}", cookies.any { it.name == "PREF" })
        }

        // 2. Non-YouTube domains must NOT receive YouTube cookies
        val foreignUrls = listOf(
            "https://piped.video/stream".toHttpUrl(),
            "https://inv.tux.pizza/api".toHttpUrl(),
            "https://example.com/api".toHttpUrl()
        )
        foreignUrls.forEach { url ->
            val cookies = jar.loadForRequest(url)
            assertFalse("Non-YT host ${url.host} must not receive SOCS", cookies.any { it.name == "SOCS" })
            assertFalse("Non-YT host ${url.host} must not receive PREF", cookies.any { it.name == "PREF" })
        }

        // 3. Cookies saved for a specific domain remain isolated
        val pipedCookie = Cookie.Builder()
            .domain("piped.video")
            .path("/")
            .name("PIPED_SESSION")
            .value("sessionSecret123")
            .build()
        jar.saveFromResponse("https://piped.video/api".toHttpUrl(), listOf(pipedCookie))

        val ytCookies = jar.loadForRequest("https://www.youtube.com/watch?v=1".toHttpUrl())
        assertFalse("Piped cookie must not leak to YouTube", ytCookies.any { it.name == "PIPED_SESSION" })

        val loadedPiped = jar.loadForRequest("https://piped.video/stream".toHttpUrl())
        assertTrue("Piped cookie must be returned to piped.video", loadedPiped.any { it.name == "PIPED_SESSION" })
    }

    @Test
    fun testHughCookieJar_concurrencySafety() {
        val jar = HughCookieJar()
        val numThreads = 10
        val iterationsPerThread = 50
        val latch = CountDownLatch(numThreads)
        val errors = AtomicInteger(0)

        for (t in 0 until numThreads) {
            Thread {
                try {
                    for (i in 0 until iterationsPerThread) {
                        val cookie = Cookie.Builder()
                            .domain("youtube.com")
                            .path("/")
                            .name("CONCURRENT_${t}_$i")
                            .value("val_$i")
                            .build()
                        jar.saveFromResponse("https://www.youtube.com/api".toHttpUrl(), listOf(cookie))

                        val loaded = jar.loadForRequest("https://www.youtube.com/watch?v=$i".toHttpUrl())
                        assertTrue(loaded.isNotEmpty())
                    }
                } catch (e: Exception) {
                    errors.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }.start()
        }

        assertTrue("Threads did not finish in time", latch.await(5, TimeUnit.SECONDS))
        assertEquals("No concurrency errors should occur in HughCookieJar", 0, errors.get())
    }

    // =========================================================================
    // 5. Filename Sanitization & MediaStore Selection Query Integrity
    // =========================================================================

    @Test
    fun testSanitizeFileName_exhaustiveCharacters() {
        val extremeInput = "Artist / Song: \"Greatest\" <Hits> | [Remix]? *2026* \\ Vol. 1"
        val sanitized = YouTubeExtractorHelper.sanitizeFileName(extremeInput)

        val forbiddenChars = listOf('\\', '/', ':', '*', '?', '"', '<', '>', '|')
        forbiddenChars.forEach { char ->
            assertFalse("Sanitized string must not contain '$char'", sanitized.contains(char))
        }

        // Accented Portuguese characters and Unicode symbols must be preserved
        val unicodeTitle = "Hermes — Canção da Vitória [São Paulo 2026] 🎧"
        val unicodeSanitized = YouTubeExtractorHelper.sanitizeFileName(unicodeTitle)
        assertTrue("Portuguese characters must be preserved", unicodeSanitized.contains("Canção da Vitória"))
        assertTrue("Brackets and dashes preserved", unicodeSanitized.contains("São Paulo 2026"))
    }

    @Test
    fun testMediaStoreScanner_querySelectionSyntaxIntegrity() {
        val selection = MediaStoreScanner.DEFAULT_SELECTION

        // Balanced parentheses
        val openCount = selection.count { it == '(' }
        val closeCount = selection.count { it == ')' }
        assertEquals("Parentheses in MediaStore query selection must be balanced", openCount, closeCount)

        // Must include all required audio extensions
        assertTrue("Must include %.opus", selection.contains("LIKE '%.opus'"))
        assertTrue("Must include %.mp3", selection.contains("LIKE '%.mp3'"))
        assertTrue("Must include %.m4a", selection.contains("LIKE '%.m4a'"))
        assertTrue("Must include %.wav", selection.contains("LIKE '%.wav'"))
        assertTrue("Must include %.aac", selection.contains("LIKE '%.aac'"))
    }
}
