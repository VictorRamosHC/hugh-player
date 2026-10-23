package com.hughdevelopment.player.lyrics

import java.io.File
import java.nio.charset.Charset

/**
 * Data model representing a single synchronized lyric line with millisecond timestamp.
 */
data class LrcLine(
    val timeMs: Long,
    val text: String
)

/**
 * Metadata and synchronized lines parsed from an .LRC file.
 */
data class LrcLyrics(
    val lines: List<LrcLine>,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val offsetMs: Long = 0L
)

/**
 * High-precision, offline-first LRC Parser for synchronized lyrics (Requirement R2).
 *
 * Supports:
 * - High-precision timestamps: [mm:ss.xx] (centiseconds) and [mm:ss.xxx] (milliseconds)
 * - Standard seconds-only timestamps: [mm:ss] and single-digit fractions: [mm:ss.x]
 * - Multi-timestamp per line (e.g. repeated choruses: [00:10.00][00:30.00]Chorus)
 * - Global [offset:+/-ms] tag for millisecond-level sync calibration
 * - Chronological sorting of all lyric lines
 * - Resilient charset fallback (UTF-8 -> Windows-1252 -> ISO-8859-1)
 */
object LrcParser {

    private val TIMESTAMP_REGEX = Regex("""\[(\d{1,3}):(\d{2})(?:\.(\d{1,3}))?\]""")
    private val OFFSET_REGEX = Regex("""\[offset:\s*([+-]?\d+)\s*\]""", RegexOption.IGNORE_CASE)
    private val METADATA_TAG_REGEX = Regex("""^\[(ti|ar|al|by):\s*(.*)\s*\]$""", RegexOption.IGNORE_CASE)

    /**
     * Parses raw LRC string content into a sorted list of [LrcLine].
     */
    fun parse(content: String): List<LrcLine> {
        return parseFull(content).lines
    }

    /**
     * Parses raw LRC string content extracting metadata and sorted lyric lines.
     */
    fun parseFull(content: String): LrcLyrics {
        if (content.isBlank()) {
            return LrcLyrics(lines = emptyList())
        }

        val lines = content.lines()
        var offsetMs = 0L
        var title: String? = null
        var artist: String? = null
        var album: String? = null

        // 1. First pass: extract global offset and metadata tags
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val offsetMatch = OFFSET_REGEX.find(trimmed)
            if (offsetMatch != null) {
                offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                continue
            }

            val metaMatch = METADATA_TAG_REGEX.find(trimmed)
            if (metaMatch != null) {
                val tag = metaMatch.groupValues[1].lowercase()
                val value = metaMatch.groupValues[2].trim()
                when (tag) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                }
            }
        }

        val resultLines = mutableListOf<LrcLine>()

        // 2. Second pass: parse timestamped lines
        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // Skip metadata and offset tags
            if (trimmed.startsWith("[offset:", ignoreCase = true) ||
                trimmed.startsWith("[ti:", ignoreCase = true) ||
                trimmed.startsWith("[ar:", ignoreCase = true) ||
                trimmed.startsWith("[al:", ignoreCase = true) ||
                trimmed.startsWith("[by:", ignoreCase = true)
            ) {
                continue
            }

            val matches = TIMESTAMP_REGEX.findAll(trimmed).toList()
            if (matches.isEmpty()) continue

            // Extract lyric text after all timestamp tags
            val lastMatch = matches.last()
            val text = trimmed.substring(lastMatch.range.last + 1).trim()

            for (m in matches) {
                val minutes = m.groupValues[1].toLongOrNull() ?: 0L
                val seconds = m.groupValues[2].toLongOrNull() ?: 0L
                val fractionStr = m.groupValues[3]

                val fractionMs = when (fractionStr.length) {
                    1 -> (fractionStr.toLongOrNull() ?: 0L) * 100L
                    2 -> (fractionStr.toLongOrNull() ?: 0L) * 10L
                    3 -> fractionStr.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val rawTimeMs = (minutes * 60L + seconds) * 1000L + fractionMs
                val adjustedTimeMs = (rawTimeMs + offsetMs).coerceAtLeast(0L)

                resultLines.add(LrcLine(timeMs = adjustedTimeMs, text = text))
            }
        }

        // 3. Sort chronologically
        val sortedLines = resultLines.sortedBy { it.timeMs }

        return LrcLyrics(
            lines = sortedLines,
            title = title,
            artist = artist,
            album = album,
            offsetMs = offsetMs
        )
    }

    /**
     * Parses an LRC [File] into a sorted list of [LrcLine].
     */
    fun parseFile(file: File): List<LrcLine> {
        return parseFileFull(file)?.lines ?: emptyList()
    }

    /**
     * Parses an LRC [File] into [LrcLyrics] with encoding fallback.
     */
    fun parseFileFull(file: File): LrcLyrics? {
        if (!file.exists() || !file.canRead() || file.isDirectory || file.length() == 0L) {
            return null
        }

        return try {
            val content = try {
                file.readText(Charsets.UTF_8)
            } catch (e: Exception) {
                try {
                    file.readText(Charset.forName("windows-1252"))
                } catch (e2: Exception) {
                    file.readText(Charsets.ISO_8859_1)
                }
            }
            parseFull(content)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Finds the index of the currently active line for a given playback position in milliseconds.
     * Returns -1 if playback is before the first line or if lines list is empty.
     */
    fun getActiveLineIndex(lines: List<LrcLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        return lines.indexOfLast { it.timeMs <= positionMs }
    }
}
