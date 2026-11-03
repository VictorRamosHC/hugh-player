package com.hughdevelopment.player.audio

import com.hughdevelopment.player.data.db.PlayerDatabase
import com.hughdevelopment.player.data.db.WaveformEntity
import com.hughdevelopment.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * WaveformExtractor // Sovereign Offline Audio Waveform Analyzer & Cache.
 *
 * Implements Option 3:
 * Analyzes audio files on-device to produce 42 normalized amplitude peaks (0.15f .. 1.0f).
 * Caches persistent results in SQLite via Room (song_waveforms) and maintains a fast memory cache.
 */
object WaveformExtractor {

    const val BAR_COUNT = 42

    private val memoryCache = ConcurrentHashMap<Long, List<Float>>()

    /**
     * Retrieves the 42 waveform points for the specified song.
     * Looks up memory cache -> Room database -> on-device file extraction -> fallback synthesis.
     */
    suspend fun getWaveformForSong(song: Song, db: PlayerDatabase): List<Float> = withContext(Dispatchers.IO) {
        // 1. Fast in-memory lookup
        memoryCache[song.id]?.let { return@withContext it }

        // 2. Room database lookup
        try {
            val cachedPointsStr = db.waveformDao().getWaveformPoints(song.id)
            if (!cachedPointsStr.isNullOrBlank()) {
                val parsed = parsePoints(cachedPointsStr)
                if (parsed.size == BAR_COUNT) {
                    memoryCache[song.id] = parsed
                    return@withContext parsed
                }
            }
        } catch (_: Exception) {}

        // 3. Extract from audio file or generate deterministic signature
        val points = extractOrSynthesize(song)

        // 4. Save to Room database
        try {
            val serialized = points.joinToString(",") { "%.3f".format(it) }
            db.waveformDao().insertWaveform(
                WaveformEntity(songId = song.id, points = serialized)
            )
        } catch (_: Exception) {}

        memoryCache[song.id] = points
        points
    }

    private fun extractOrSynthesize(song: Song): List<Float> {
        val file = File(song.dataPath)
        if (file.exists() && file.canRead() && file.length() > 8192) {
            try {
                return extractFromFileBytes(file, song.id)
            } catch (_: Exception) {}
        }
        return synthesizeSignature(song)
    }

    /**
     * Samples real byte energy across 42 evenly spaced sectors in the audio file.
     */
    private fun extractFromFileBytes(file: File, songId: Long): List<Float> {
        val totalLength = file.length()
        val sectorSize = (totalLength / BAR_COUNT).coerceAtLeast(512L)
        val buffer = ByteArray(512)
        val rawPeaks = mutableListOf<Float>()

        RandomAccessFile(file, "r").use { raf ->
            for (i in 0 until BAR_COUNT) {
                val position = (i * sectorSize).coerceIn(0L, totalLength - 512)
                raf.seek(position)
                val read = raf.read(buffer, 0, buffer.size)
                if (read > 0) {
                    var sum = 0.0
                    for (b in 0 until read) {
                        val sample = buffer[b].toInt()
                        sum += sample * sample
                    }
                    val rms = sqrt(sum / read).toFloat()
                    rawPeaks.add(rms)
                } else {
                    rawPeaks.add(10f)
                }
            }
        }

        // Normalize between 0.15f and 1.0f
        val maxPeak = rawPeaks.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val minPeak = rawPeaks.minOrNull() ?: 0f
        val range = (maxPeak - minPeak).coerceAtLeast(0.001f)

        return rawPeaks.map { peak ->
            val norm = (peak - minPeak) / range
            (0.18f + norm * 0.82f).coerceIn(0.15f, 1.0f)
        }
    }

    /**
     * Deterministic psychoacoustic wave synthesis based on track hash.
     * Guarantees that every track has a rich, unique, consistent visual signature even if offline.
     */
    fun synthesizeSignature(songId: Long, songTitle: String): List<Float> {
        val seed = (songId.hashCode() xor songTitle.hashCode()).toFloat()
        val points = mutableListOf<Float>()

        for (i in 0 until BAR_COUNT) {
            val t = i.toFloat() / BAR_COUNT
            val h1 = sin(t * 7.2f + seed * 0.01f)
            val h2 = cos(t * 13.5f + seed * 0.03f)
            val h3 = sin(t * 21.0f + seed * 0.07f)
            val combined = (abs(h1 * 0.45f + h2 * 0.35f + h3 * 0.20f)).coerceIn(0f, 1f)
            val amplitude = 0.18f + (combined * 0.82f)
            points.add(amplitude.coerceIn(0.15f, 1.0f))
        }

        return points
    }

    fun synthesizeSignature(song: Song): List<Float> = synthesizeSignature(song.id, song.title)

    private fun parsePoints(str: String): List<Float> {
        return str.split(",")
            .mapNotNull { it.trim().replace(',', '.').toFloatOrNull() }
            .map { it.coerceIn(0.15f, 1.0f) }
    }
}
