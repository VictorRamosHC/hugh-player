package com.hughdevelopment.player.data.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.AudioStream
import java.util.regex.Pattern

data class ExtractedAudioInfo(
    val videoId: String,
    val title: String,
    val artist: String,
    val durationSeconds: Long,
    val streamUrl: String,
    val originalUrl: String = "https://www.youtube.com/watch?v=$videoId",
    val thumbnailUrl: String? = null,
    val format: String = "m4a",
    val itag: Int = 140,
    val averageBitrate: Int = 128,
    val extractorTier: String = "PRIMARY"
)

object YouTubeExtractorHelper : AudioStreamExtractor {

    private val YOUTUBE_URL_PATTERN = Pattern.compile(
        "(?i)(?<![-a-zA-Z0-9_./@:])(?:https?://)?(?:(?:www\\.|m\\.|music\\.|gaming\\.)?youtube\\.com/(?:watch\\?(?:[^#\\s]*&)?v=|shorts/|live/|v/|embed/)|(?:www\\.)?youtu\\.be/)([a-zA-Z0-9_-]{11})(?![a-zA-Z0-9_-])"
    )

    fun extractVideoId(url: String): String? {
        val matcher = YOUTUBE_URL_PATTERN.matcher(url.trim())
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            // Se o usuário digitou direto o ID de 11 caracteres
            val trimmed = url.trim()
            if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) trimmed else null
        }
    }

    fun isYouTubeUrl(url: String): Boolean = extractVideoId(url) != null

    override suspend fun extractAudio(url: String): ExtractedAudioInfo = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(url)
            ?: throw IllegalArgumentException("Link do YouTube inválido ou ID não encontrado: $url")
        val cleanUrl = "https://www.youtube.com/watch?v=$videoId"

        var newPipeError: Exception? = null

        // Tier 1: Enhanced NewPipeExtractor (with persistent CookieJar, SOCS/PREF cookies, clean UA)
        try {
            HughDownloader.initConsent()
            NewPipe.init(HughDownloader.getInstance())

            val extractor = ServiceList.YouTube.getStreamExtractor(cleanUrl) as YoutubeStreamExtractor
            extractor.fetchPage()

            val audioStreams = extractor.audioStreams
            if (!audioStreams.isNullOrEmpty()) {
                val selectedStream: AudioStream = audioStreams.find { it.itag == 140 }
                    ?: audioStreams.filter { it.format?.name?.contains("M4A", ignoreCase = true) == true }
                        .maxByOrNull { it.averageBitrate }
                    ?: audioStreams.maxByOrNull { it.averageBitrate }
                    ?: audioStreams.first()

                val title = extractor.name ?: "Faixa Desconhecida"
                val artist = extractor.uploaderName ?: "Artista Desconhecido"
                val duration = extractor.length
                val thumbnail = extractor.thumbnails.maxByOrNull { it.height * it.width }?.url

                val formatName = selectedStream.format?.name.orEmpty()
                val formatExt = when {
                    formatName.contains("M4A", ignoreCase = true) -> "m4a"
                    formatName.contains("OPUS", ignoreCase = true) || formatName.contains("WEBM", ignoreCase = true) -> "opus"
                    else -> "m4a"
                }

                return@withContext ExtractedAudioInfo(
                    videoId = videoId,
                    title = sanitizeFileName(title),
                    artist = sanitizeFileName(artist),
                    durationSeconds = duration,
                    streamUrl = selectedStream.url.orEmpty(),
                    originalUrl = cleanUrl,
                    thumbnailUrl = thumbnail,
                    format = formatExt,
                    itag = selectedStream.itag,
                    averageBitrate = selectedStream.averageBitrate,
                    extractorTier = "NEWPIPE_TIER1"
                )
            }
        } catch (e: Exception) {
            newPipeError = e
        }

        // Tier 2: Native InnerTube Android / iOS Client Extractor
        var innerTubeError: Exception? = null
        try {
            return@withContext InnerTubeAudioExtractor.extractAudio(cleanUrl)
        } catch (e: Exception) {
            innerTubeError = e
        }

        // Tier 3: Resilient Public Proxy API (Piped / Invidious)
        try {
            return@withContext fetchFromProxy(cleanUrl, videoId)
        } catch (proxyError: Exception) {
            val failureSummary = buildString {
                append("Falha em todas as camadas de extração: ")
                if (newPipeError != null) append("[Tier 1 NewPipe: ${newPipeError.message}] ")
                if (innerTubeError != null) append("[Tier 2 InnerTube: ${innerTubeError.message}] ")
                append("[Tier 3 Proxy: ${proxyError.message}]")
            }
            throw IllegalStateException(failureSummary, proxyError)
        }
    }

    private fun fetchFromProxy(cleanUrl: String, videoId: String): ExtractedAudioInfo {
        val pipedInstances = listOf(
            "https://api.piped.private.coffee",
            "https://pipedapi.kavin.rocks",
            "https://piped-api.lunar.icu"
        )
        for (instance in pipedInstances) {
            try {
                val req = Request.Builder()
                    .url("$instance/streams/$videoId")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                val res = HughDownloader.okHttpClient.newCall(req).execute()
                if (res.isSuccessful) {
                    val body = res.body?.string().orEmpty()
                    val json = AetherJson.parse(body) as? AetherJson.Obj
                    if (json != null) {
                        val title = json.getString("title") ?: "Faixa Desconhecida"
                        val author = json.getString("uploader") ?: "Artista Desconhecido"
                        val duration = json.getLong("duration", 0L)
                        val thumb = json.getString("thumbnailUrl")
                        val audioStreams = json.getArr("audioStreams")
                        if (audioStreams != null && audioStreams.isNotEmpty()) {
                            var bestUrl: String? = null
                            var bestFmt = "m4a"
                            var bestBitrate = 128
                            var bestItag = 140
                            for (i in 0 until audioStreams.size) {
                                val s = audioStreams.getObj(i) ?: continue
                                val u = s.getString("url") ?: continue
                                val f = s.getString("format") ?: ""
                                val b = s.getInt("bitrate", 0)
                                if (f.contains("M4A", ignoreCase = true) || u.contains(".m4a")) {
                                    bestUrl = u
                                    bestFmt = "m4a"
                                    bestBitrate = if (b > 0) b / 1000 else 128
                                    bestItag = 140
                                    break
                                } else if (bestUrl == null) {
                                    bestUrl = u
                                    bestFmt = if (f.contains("OPUS", ignoreCase = true)) "opus" else "m4a"
                                    bestBitrate = if (b > 0) b / 1000 else 160
                                    bestItag = 251
                                }
                            }
                            if (!bestUrl.isNullOrBlank()) {
                                return ExtractedAudioInfo(
                                    videoId = videoId,
                                    title = sanitizeFileName(title),
                                    artist = sanitizeFileName(author),
                                    durationSeconds = duration,
                                    streamUrl = bestUrl,
                                    originalUrl = cleanUrl,
                                    thumbnailUrl = thumb,
                                    format = bestFmt,
                                    itag = bestItag,
                                    averageBitrate = bestBitrate,
                                    extractorTier = "PROXY_PIPED"
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val invidiousInstances = listOf(
            "https://inv.tux.pizza",
            "https://yewtu.be",
            "https://vid.puffyan.us"
        )
        for (instance in invidiousInstances) {
            try {
                val req = Request.Builder()
                    .url("$instance/api/v1/videos/$videoId")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                val res = HughDownloader.okHttpClient.newCall(req).execute()
                if (res.isSuccessful) {
                    val body = res.body?.string().orEmpty()
                    val json = AetherJson.parse(body) as? AetherJson.Obj
                    if (json != null) {
                        val title = json.getString("title") ?: "Faixa Desconhecida"
                        val author = json.getString("author") ?: "Artista Desconhecido"
                        val duration = json.getLong("lengthSeconds", 0L)
                        val formats = json.getArr("adaptiveFormats")
                        if (formats != null && formats.isNotEmpty()) {
                            var bestUrl: String? = null
                            var bestFmt = "m4a"
                            var bestBitrate = 128
                            var bestItag = 140
                            for (i in 0 until formats.size) {
                                val f = formats.getObj(i) ?: continue
                                val type = f.getString("type") ?: ""
                                if (!type.contains("audio", ignoreCase = true)) continue
                                val u = f.getString("url") ?: continue
                                val container = f.getString("container") ?: ""
                                val itag = f.getInt("itag", 0)
                                val b = f.getInt("bitrate", 0)
                                if (itag == 140 || container.contains("m4a", ignoreCase = true)) {
                                    bestUrl = u
                                    bestFmt = "m4a"
                                    bestBitrate = if (b > 0) b / 1000 else 128
                                    bestItag = itag
                                    break
                                } else if (bestUrl == null) {
                                    bestUrl = u
                                    bestFmt = if (container.contains("webm", ignoreCase = true) || container.contains("opus", ignoreCase = true)) "opus" else "m4a"
                                    bestBitrate = if (b > 0) b / 1000 else 160
                                    bestItag = itag
                                }
                            }
                            if (!bestUrl.isNullOrBlank()) {
                                return ExtractedAudioInfo(
                                    videoId = videoId,
                                    title = sanitizeFileName(title),
                                    artist = sanitizeFileName(author),
                                    durationSeconds = duration,
                                    streamUrl = bestUrl,
                                    originalUrl = cleanUrl,
                                    thumbnailUrl = null,
                                    format = bestFmt,
                                    itag = bestItag,
                                    averageBitrate = bestBitrate,
                                    extractorTier = "PROXY_INVIDIOUS"
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        throw IllegalStateException("Todos os provedores de proxy (Piped/Invidious) falharam.")
    }

    fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
    }
}
