package com.hughdevelopment.player.data.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val originalUrl: String,
    val thumbnailUrl: String?,
    val format: String,
    val itag: Int,
    val averageBitrate: Int
)

object YouTubeExtractorHelper {

    private val YOUTUBE_URL_PATTERN = Pattern.compile(
        "(?i)(?:https?://)?(?:www\\.|m\\.|music\\.|gaming\\.)?(?:youtube\\.com/(?:watch\\?.*?v=|shorts/|live/|v/|embed/)|youtu\\.be/)([a-zA-Z0-9_-]{11})(?![a-zA-Z0-9_-])"
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

    suspend fun extractAudio(url: String): ExtractedAudioInfo = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(url) ?: throw IllegalArgumentException("Link do YouTube inválido ou ID não encontrado: $url")
        val cleanUrl = "https://www.youtube.com/watch?v=$videoId"

        // Garante que o NewPipe está inicializado
        try {
            NewPipe.init(HughDownloader.getInstance())
        } catch (_: Exception) {}

        val extractor = ServiceList.YouTube.getStreamExtractor(cleanUrl) as YoutubeStreamExtractor
        extractor.fetchPage()

        val audioStreams = extractor.audioStreams
        if (audioStreams.isNullOrEmpty()) {
            throw IllegalStateException("Nenhum fluxo de áudio encontrado para esta faixa.")
        }

        // Escolhe o melhor stream de áudio:
        // 1. M4A / AAC (itag 140) é a mais rápida, não precisa de re-encoding e toca nativamente no Media3 ExoPlayer com metadata perfeita
        // 2. Se não houver 140, seleciona o stream de áudio com maior bitrate disponível
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

        ExtractedAudioInfo(
            videoId = videoId,
            title = sanitizeFileName(title),
            artist = sanitizeFileName(artist),
            durationSeconds = duration,
            streamUrl = selectedStream.url.orEmpty(),
            originalUrl = cleanUrl,
            thumbnailUrl = thumbnail,
            format = formatExt,
            itag = selectedStream.itag,
            averageBitrate = selectedStream.averageBitrate
        )
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
    }
}
