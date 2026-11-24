package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.download.AetherJson
import com.hughdevelopment.player.data.download.YouTubeExtractorHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Challenger 1 Adversarial Stress Test Suite:
 * Rigorously attacks and stress-tests:
 * 1. YouTubeExtractorHelper URL regex matching against spoofed domains,
 *    path manipulations, query parameter trickery, and boundary edge cases.
 * 2. AetherJson numeric parsing for 64-bit integer precision, floating-point
 *    truncation, scientific notation, and non-finite number rejection.
 */
class Challenger1RegexJsonStressTest {

    // =========================================================================
    // 1. ADVERSARIAL URL PARSING STRESS TESTS
    // =========================================================================

    @Test
    fun testAdversarialDomainSpoofing_mustAllBeRejected() {
        val spoofedUrls = listOf(
            // Suffix attacks (domain ends with youtube.com or youtu.be)
            "https://notyoutube.com/watch?v=dQw4w9WgXcQ",
            "https://fakeyoutube.com/watch?v=dQw4w9WgXcQ",
            "https://phishing-youtube.com/watch?v=dQw4w9WgXcQ",
            "http://maliciousyoutube.com/watch?v=dQw4w9WgXcQ",
            "https://attackeryoutube.com/shorts/dQw4w9WgXcQ",
            "https://my-youtube.com/v/dQw4w9WgXcQ",
            "https://fakeyoutu.be/dQw4w9WgXcQ",
            "https://not-youtu.be/dQw4w9WgXcQ",
            "https://maliciousyoutu.be/dQw4w9WgXcQ",

            // Prefix attacks (youtube.com as subdomain of attacker domain)
            "https://youtube.com.attacker.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com.phishing.org/watch?v=dQw4w9WgXcQ",
            "https://youtu.be.attacker.com/dQw4w9WgXcQ",
            "https://m.youtube.com.evil.co/watch?v=dQw4w9WgXcQ",

            // Attacker path containing youtube.com
            "https://attacker.com/youtube.com/watch?v=dQw4w9WgXcQ",
            "https://attacker.com/www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://attacker.com/youtu.be/dQw4w9WgXcQ",

            // Disallowed subdomains
            "https://evil.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://fake.sub.youtube.com/watch?v=dQw4w9WgXcQ"
        )

        for (url in spoofedUrls) {
            val extracted = YouTubeExtractorHelper.extractVideoId(url)
            assertNull("Spoofed domain must not extract a videoId: '$url'", extracted)
            assertFalse("isYouTubeUrl must return false for spoofed domain: '$url'", YouTubeExtractorHelper.isYouTubeUrl(url))
        }
    }

    @Test
    fun testLegitimateYouTubeUrls_mustAllExtractExactVideoId() {
        val legitimateCases = mapOf(
            // Standard watch URLs
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Valid subdomains
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://m.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://gaming.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Short links (youtu.be)
            "https://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Shorts, Live, Embed, and V
            "https://www.youtube.com/shorts/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtube.com/shorts/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://m.youtube.com/shorts/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/live/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtube.com/live/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Complex query parameters preceding 'v='
            "https://www.youtube.com/watch?feature=share&v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/watch?app=desktop&gl=US&hl=en&v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/watch?prev_v=ignored1234&v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",

            // Query parameters following 'v='
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PL123&index=5&t=100s" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?si=abcdef123456" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?t=42" to "dQw4w9WgXcQ",

            // Hyphenated and underscored video IDs
            "https://www.youtube.com/watch?v=_a-1B_2c-3D" to "_a-1B_2c-3D",
            "https://youtu.be/-----------" to "-----------",
            "https://youtu.be/___________" to "___________",
            "https://youtu.be/a-b_c-d_e-1" to "a-b_c-d_e-1",

            // Direct 11-char strings
            "dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "_a-1B_2c-3D" to "_a-1B_2c-3D"
        )

        for ((url, expected) in legitimateCases) {
            val extracted = YouTubeExtractorHelper.extractVideoId(url)
            assertEquals("Failed extraction for: '$url'", expected, extracted)
            assertTrue("isYouTubeUrl should return true for: '$url'", YouTubeExtractorHelper.isYouTubeUrl(url))
        }
    }

    @Test
    fun testSurroundingPunctuationAndTextExtraction() {
        // Natural language / Share sheet / Markdown embeddings
        val cases = mapOf(
            "Listen to this track: https://www.youtube.com/watch?v=dQw4w9WgXcQ!" to "dQw4w9WgXcQ",
            "[Link](https://youtu.be/dQw4w9WgXcQ)" to "dQw4w9WgXcQ",
            "<https://www.youtube.com/watch?v=dQw4w9WgXcQ>" to "dQw4w9WgXcQ",
            "\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"" to "dQw4w9WgXcQ",
            "'https://youtu.be/dQw4w9WgXcQ'" to "dQw4w9WgXcQ",
            "Watch at https://www.youtube.com/shorts/dQw4w9WgXcQ." to "dQw4w9WgXcQ",
            "Video (https://www.youtube.com/watch?v=dQw4w9WgXcQ) is great" to "dQw4w9WgXcQ"
        )

        for ((text, expected) in cases) {
            val extracted = YouTubeExtractorHelper.extractVideoId(text)
            assertEquals("Failed extraction from embedded text: '$text'", expected, extracted)
        }
    }

    @Test
    fun testInvalidLengthsAndCorruptPatterns_mustReturnNull() {
        val corruptCases = listOf(
            "",
            "   ",
            "\t\n",
            "https://www.youtube.com/watch",
            "https://www.youtube.com/watch?",
            "https://www.youtube.com/watch?v=",
            "https://www.youtube.com/watch?v=12345", // 5 chars
            "https://www.youtube.com/watch?v=1234567890", // 10 chars
            "https://www.youtube.com/watch?v=123456789012", // 12 chars
            "https://www.youtube.com/watch?v=dQw4w!WgXcQ", // invalid char '!'
            "https://www.youtube.com/watch?v=dQw4w@WgXcQ", // invalid char '@'
            "https://www.youtube.com/shorts/",
            "https://www.youtube.com/shorts/short", // 5 chars
            "https://www.youtube.com/shorts/waytoolongvideoid123", // 20 chars
            "https://youtu.be/",
            "https://youtu.be/short",
            "https://youtu.be/toolongvideoid123",
            "1234567890", // 10 chars raw
            "123456789012", // 12 chars raw
            "https://vimeo.com/123456789",
            "https://soundcloud.com/artist/track"
        )

        for (case in corruptCases) {
            val extracted = YouTubeExtractorHelper.extractVideoId(case)
            assertNull("Corrupt input should yield null: '$case'", extracted)
            assertFalse("isYouTubeUrl should return false: '$case'", YouTubeExtractorHelper.isYouTubeUrl(case))
        }
    }

    // =========================================================================
    // 2. AETHERJSON NUMERIC PARSING ADVERSARIAL STRESS TESTS
    // =========================================================================

    @Test
    fun testAetherJson_64BitIntegerExactPrecision() {
        val rawJson = """
        {
          "maxLong": 9223372036854775807,
          "minLong": -9223372036854775808,
          "aboveDoublePrecision": 9007199254740993,
          "maxInt": 2147483647,
          "minInt": -2147483648
        }
        """.trimIndent()

        val obj = AetherJson.parse(rawJson) as AetherJson.Obj

        // Exact Long boundaries must not lose precision to 53-bit Double mantissa
        assertEquals(Long.MAX_VALUE, obj.getLong("maxLong"))
        assertEquals(Long.MIN_VALUE, obj.getLong("minLong"))
        assertEquals(9007199254740993L, obj.getLong("aboveDoublePrecision"))

        // Exact Int boundaries
        assertEquals(Int.MAX_VALUE, obj.getInt("maxInt"))
        assertEquals(Int.MIN_VALUE, obj.getInt("minInt"))
    }

    @Test
    fun testAetherJson_decimalsAndScientificNotationParsing() {
        val rawJson = """
        {
          "pi": 3.1415926535,
          "rate": 240.0,
          "negDecimal": -99.9,
          "expPositive": 1.5e4,
          "expNegative": 2.5e-2,
          "expCapital": 3E+2,
          "expSmall": 1e6
        }
        """.trimIndent()

        val obj = AetherJson.parse(rawJson) as AetherJson.Obj

        // getInt truncates / converts decimals
        assertEquals(3, obj.getInt("pi"))
        assertEquals(240, obj.getInt("rate"))
        assertEquals(-99, obj.getInt("negDecimal"))

        // getLong converts scientific notation and decimals
        assertEquals(3L, obj.getLong("pi"))
        assertEquals(240L, obj.getLong("rate"))
        assertEquals(-99L, obj.getLong("negDecimal"))
        assertEquals(15000L, obj.getLong("expPositive"))
        assertEquals(300L, obj.getLong("expCapital"))
        assertEquals(1000000L, obj.getLong("expSmall"))

        // getDouble preserves precision
        assertEquals(3.1415926535, obj.getDouble("pi"), 0.000001)
        assertEquals(240.0, obj.getDouble("rate"), 0.001)
        assertEquals(-99.9, obj.getDouble("negDecimal"), 0.001)
        assertEquals(15000.0, obj.getDouble("expPositive"), 0.001)
        assertEquals(0.025, obj.getDouble("expNegative"), 0.0001)
    }

    @Test
    fun testAetherJson_nonFiniteAndCorruptedNumberRejection() {
        val rawJson = """
        {
          "strNaN": "NaN",
          "strInf": "Infinity",
          "strNegInf": "-Infinity",
          "emptyStr": "",
          "corruptNum": "1.2.3.4",
          "alphaNum": "123a",
          "boolVal": true,
          "nullVal": null
        }
        """.trimIndent()

        val obj = AetherJson.parse(rawJson) as AetherJson.Obj

        // Non-finite strings must be rejected and return the default value
        assertEquals(99L, obj.getLong("strNaN", 99L))
        assertEquals(99L, obj.getLong("strInf", 99L))
        assertEquals(99L, obj.getLong("strNegInf", 99L))
        assertEquals(99L, obj.getLong("emptyStr", 99L))
        assertEquals(99L, obj.getLong("corruptNum", 99L))
        assertEquals(99L, obj.getLong("alphaNum", 99L))
        assertEquals(99L, obj.getLong("boolVal", 99L))
        assertEquals(99L, obj.getLong("nullVal", 99L))

        assertEquals(42, obj.getInt("strNaN", 42))
        assertEquals(42, obj.getInt("strInf", 42))
        assertEquals(42, obj.getInt("corruptNum", 42))

        assertEquals(1.5, obj.getDouble("strNaN", 1.5), 0.001)
        assertEquals(1.5, obj.getDouble("strInf", 1.5), 0.001)
    }

    @Test
    fun testAetherJson_arrayNumericGettersSymmetry() {
        val rawJson = """
        [
          100,
          200.75,
          "300",
          "1.25e3",
          -50,
          "NaN",
          null,
          true
        ]
        """.trimIndent()

        val arr = AetherJson.parse(rawJson) as AetherJson.Arr
        assertEquals(8, arr.size)
        assertTrue(arr.isNotEmpty())
        assertFalse(arr.isEmpty())

        // Index 0: 100
        assertEquals(100L, arr.getLong(0))
        assertEquals(100, arr.getInt(0))
        assertEquals(100.0, arr.getDouble(0), 0.001)

        // Index 1: 200.75
        assertEquals(200L, arr.getLong(1))
        assertEquals(200, arr.getInt(1))
        assertEquals(200.75, arr.getDouble(1), 0.001)

        // Index 2: "300" (string number)
        assertEquals(300L, arr.getLong(2))
        assertEquals(300, arr.getInt(2))
        assertEquals(300.0, arr.getDouble(2), 0.001)

        // Index 3: "1.25e3" (scientific string)
        assertEquals(1250L, arr.getLong(3))
        assertEquals(1250, arr.getInt(3))
        assertEquals(1250.0, arr.getDouble(3), 0.001)

        // Index 4: -50
        assertEquals(-50L, arr.getLong(4))
        assertEquals(-50, arr.getInt(4))

        // Index 5: "NaN" -> rejected to default
        assertEquals(888L, arr.getLong(5, 888L))
        assertEquals(777, arr.getInt(5, 777))
        assertEquals(9.9, arr.getDouble(5, 9.9), 0.001)

        // Index 6: null -> rejected to default
        assertEquals(55L, arr.getLong(6, 55L))

        // Out-of-bounds index handling
        assertEquals(-1L, arr.getLong(-1, -1L))
        assertEquals(-1L, arr.getLong(999, -1L))
        assertEquals(-1, arr.getInt(999, -1))
        assertEquals(-1.0, arr.getDouble(999, -1.0), 0.001)
        assertNull(arr.getString(999))
        assertNull(arr.getObj(999))
    }

    @Test
    fun testAetherJson_nestedDeepStructures() {
        val rawJson = """
        {
          "tier1": {
            "tier2": {
              "items": [
                { "id": 1, "bitrate": 128000, "sampleRate": 44.1 },
                { "id": 2, "bitrate": 256000, "sampleRate": 48.0 }
              ]
            }
          }
        }
        """.trimIndent()

        val root = AetherJson.parse(rawJson) as AetherJson.Obj
        val tier1 = root.getObj("tier1")
        assertNotNull(tier1)
        val tier2 = tier1!!.getObj("tier2")
        assertNotNull(tier2)
        val items = tier2!!.getArr("items")
        assertNotNull(items)
        assertEquals(2, items!!.size)

        val item1 = items.getObj(0)
        assertNotNull(item1)
        assertEquals(1L, item1!!.getLong("id"))
        assertEquals(128000, item1.getInt("bitrate"))
        assertEquals(44.1, item1.getDouble("sampleRate"), 0.01)

        val item2 = items.getObj(1)
        assertNotNull(item2)
        assertEquals(256000L, item2!!.getLong("bitrate"))
        assertEquals(48.0, item2.getDouble("sampleRate"), 0.01)
    }
}
