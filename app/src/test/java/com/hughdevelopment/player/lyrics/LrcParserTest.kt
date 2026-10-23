package com.hughdevelopment.player.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Data contract representing a single synchronized lyric line.
 */
data class TestLrcLine(
    val timeMs: Long,
    val text: String
)

/**
 * Reference Oracle and high-precision parser for standard .LRC files (F-04).
 * Supports [mm:ss.xx], [mm:ss.xxx], [mm:ss], multi-timestamps per line, and [offset:ms].
 */
object LrcParserEngine {

    private val TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?\]""")
    private val OFFSET_REGEX = Regex("""\[offset:\s*([+-]?\d+)\s*\]""", RegexOption.IGNORE_CASE)

    fun parse(content: String): List<TestLrcLine> {
        if (content.isBlank()) return emptyList()

        val lines = content.lines()
        var offsetMs = 0L

        // 1. Extract global [offset:ms] tag if present
        for (line in lines) {
            val offsetMatch = OFFSET_REGEX.find(line)
            if (offsetMatch != null) {
                offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                break
            }
        }

        val result = mutableListOf<TestLrcLine>()

        // 2. Parse timestamped lyric lines
        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isBlank() || trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") ||
                trimmed.startsWith("[al:") || trimmed.startsWith("[by:") || trimmed.startsWith("[offset:")
            ) {
                continue
            }

            val matches = TIMESTAMP_REGEX.findAll(trimmed).toList()
            if (matches.isEmpty()) continue

            // Extract lyric text after all timestamps
            val lastMatch = matches.last()
            val text = trimmed.substring(lastMatch.range.last + 1).trim()

            for (m in matches) {
                val minutes = m.groupValues[1].toLong()
                val seconds = m.groupValues[2].toLong()
                val fractionStr = m.groupValues[3]

                val fractionMs = when (fractionStr.length) {
                    1 -> fractionStr.toLong() * 100L
                    2 -> fractionStr.toLong() * 10L
                    3 -> fractionStr.toLong()
                    else -> 0L
                }

                // Standard LRC: positive offset delays or shifts lyrics (timeMs = rawMs + offsetMs)
                val rawTimeMs = (minutes * 60L + seconds) * 1000L + fractionMs
                val adjustedTimeMs = (rawTimeMs + offsetMs).coerceAtLeast(0L)

                result.add(TestLrcLine(timeMs = adjustedTimeMs, text = text))
            }
        }

        // 3. Sort chronologically
        return result.sortedBy { it.timeMs }
    }
}

/**
 * Comprehensive Unit Test Suite for Offline LRC Lyrics Parser (F-04, F-05, F-06, F-07).
 */
class LrcParserTest {

    // --- Tier 1: Standard Timestamps & Formatting ---

    @Test
    fun testParseStandardCentisecondFormat() {
        val lrc = """
            [00:12.50]Primeira estrofe do hino
            [00:15.80]Segunda linha sincronizada
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(2, lines.size)
        assertEquals(12500L, lines[0].timeMs)
        assertEquals("Primeira estrofe do hino", lines[0].text)
        assertEquals(15800L, lines[1].timeMs)
        assertEquals("Segunda linha sincronizada", lines[1].text)
    }

    @Test
    fun testParseMillisecondFormat() {
        val lrc = """
            [01:05.123]Alta precisão em milissegundos
            [02:30.999]Final da faixa estendida
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(2, lines.size)
        assertEquals((65 * 1000L) + 123L, lines[0].timeMs)
        assertEquals("Alta precisão em milissegundos", lines[0].text)
        assertEquals((150 * 1000L) + 999L, lines[1].timeMs)
        assertEquals("Final da faixa estendida", lines[1].text)
    }

    @Test
    fun testParseSingleDigitFractionAndNoFraction() {
        val lrc = """
            [00:05.4]Fração de um dígito
            [00:10]Sem fração de segundos
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(2, lines.size)
        assertEquals(5400L, lines[0].timeMs)
        assertEquals("Fração de um dígito", lines[0].text)
        assertEquals(10000L, lines[1].timeMs)
        assertEquals("Sem fração de segundos", lines[1].text)
    }

    // --- Tier 1: Offset Tag Adjustments ---

    @Test
    fun testParseWithPositiveOffset() {
        val lrc = """
            [offset:500]
            [00:10.00]Letra com atraso compensado
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(1, lines.size)
        // 10000 + 500 = 10500 ms
        assertEquals(10500L, lines[0].timeMs)
        assertEquals("Letra com atraso compensado", lines[0].text)
    }

    @Test
    fun testParseWithNegativeOffset() {
        val lrc = """
            [offset:-200]
            [00:10.00]Letra adiantada
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(1, lines.size)
        // 10000 - 200 = 9800 ms
        assertEquals(9800L, lines[0].timeMs)
        assertEquals("Letra adiantada", lines[0].text)
    }

    @Test
    fun testNegativeOffset_clampsAtZero() {
        val lrc = """
            [offset:-5000]
            [00:02.00]Início imediato
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(1, lines.size)
        // 2000 - 5000 coerced to 0L
        assertEquals(0L, lines[0].timeMs)
    }

    // --- Tier 2: Multi-Timestamp Support ---

    @Test
    fun testMultiTimestampPerLine_emitsMultipleChronologicalLines() {
        val lrc = """
            [00:10.00][00:30.00][01:00.00]Refrão repetido três vezes
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(3, lines.size)
        assertEquals(10000L, lines[0].timeMs)
        assertEquals("Refrão repetido três vezes", lines[0].text)
        assertEquals(30000L, lines[1].timeMs)
        assertEquals("Refrão repetido três vezes", lines[1].text)
        assertEquals(60000L, lines[2].timeMs)
        assertEquals("Refrão repetido três vezes", lines[2].text)
    }

    @Test
    fun testUnsortedLines_sortedChronologically() {
        val lrc = """
            [01:00.00]Terceira parte
            [00:10.00]Primeira parte
            [00:35.50]Segunda parte
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(3, lines.size)
        assertEquals(10000L, lines[0].timeMs)
        assertEquals("Primeira parte", lines[0].text)
        assertEquals(35500L, lines[1].timeMs)
        assertEquals("Segunda parte", lines[1].text)
        assertEquals(60000L, lines[2].timeMs)
        assertEquals("Terceira parte", lines[2].text)
    }

    // --- Tier 2: Edge Cases & Resiliency ---

    @Test
    fun testMetadataTagsIgnored_doesNotEmitLyricLines() {
        val lrc = """
            [ti:Sovereign Audio]
            [ar:Aether Vault]
            [al:Hugh & Atom]
            [by:Hermes AI]
            [00:05.00]Primeira estrofe real
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(1, lines.size)
        assertEquals(5000L, lines[0].timeMs)
        assertEquals("Primeira estrofe real", lines[0].text)
    }

    @Test
    fun testCorruptLinesAndEmptyInput_handledGracefully() {
        val emptyResult = LrcParserEngine.parse("")
        assertTrue(emptyResult.isEmpty())

        val whitespaceResult = LrcParserEngine.parse("   \n\n\t  ")
        assertTrue(whitespaceResult.isEmpty())

        val corruptLrc = """
            Texto sem timestamp nenhum
            [invalid:tag]
            [99:99.999]Linha com timestamp extremo
            [00:01.00]Linha válida
        """.trimIndent()

        val lines = LrcParserEngine.parse(corruptLrc)
        assertEquals(2, lines.size)
        assertEquals(1000L, lines[0].timeMs)
        assertEquals("Linha válida", lines[0].text)
        assertEquals((99 * 60 + 99) * 1000L + 999L, lines[1].timeMs)
    }

    @Test
    fun testUtf8AndSpecialCharactersInLyrics() {
        val lrc = """
            [00:04.20]Grandioso és Tu, Senhor Deus Todo-Poderoso! 🏛️
            [00:08.50]“Coração quebrantado e espírito contrito” • Salmo 51
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)
        assertEquals(2, lines.size)
        assertEquals("Grandioso és Tu, Senhor Deus Todo-Poderoso! 🏛️", lines[0].text)
        assertEquals("“Coração quebrantado e espírito contrito” • Salmo 51", lines[1].text)
    }

    // --- Tier 3: Real-Time Playback Synchronization Math (F-06) ---

    @Test
    fun testActiveLineIndexCalculation_matchingPlaybackPosition() {
        val lrc = """
            [00:05.00]Intro
            [00:15.00]Primeiro verso
            [00:25.00]Refrão
            [00:40.00]Outro
        """.trimIndent()

        val lines = LrcParserEngine.parse(lrc)

        fun getActiveIndex(positionMs: Long): Int {
            return lines.indexOfLast { it.timeMs <= positionMs }
        }

        // Before first line
        assertEquals(-1, getActiveIndex(0L))
        assertEquals(-1, getActiveIndex(4999L))

        // Exactly at first line
        assertEquals(0, getActiveIndex(5000L))
        assertEquals(0, getActiveIndex(10000L))
        assertEquals(0, getActiveIndex(14999L))

        // At second line
        assertEquals(1, getActiveIndex(15000L))
        assertEquals(1, getActiveIndex(20000L))

        // At chorus
        assertEquals(2, getActiveIndex(25000L))
        assertEquals(2, getActiveIndex(35000L))

        // At outro and beyond
        assertEquals(3, getActiveIndex(40000L))
        assertEquals(3, getActiveIndex(100000L))
    }
}
