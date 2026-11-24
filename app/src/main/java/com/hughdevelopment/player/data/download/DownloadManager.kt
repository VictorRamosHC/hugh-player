package com.hughdevelopment.player.data.download

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import com.hughdevelopment.player.data.db.DownloadDao
import com.hughdevelopment.player.data.db.DownloadEntity
import com.hughdevelopment.player.data.repository.MusicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class DownloadLiveState(
    val videoId: String,
    val title: String,
    val artist: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val progressPercent: Int,
    val speedBytesPerSec: Long,
    val speedFormatted: String,
    val etaFormatted: String,
    val status: String,
    val errorMessage: String? = null
)

class DownloadManager private constructor(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val repository: MusicRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    // Telemetria em memória atualizada em tempo real (60fps) para a UI
    private val _liveStates = MutableStateFlow<Map<String, DownloadLiveState>>(emptyMap())
    val liveStates: StateFlow<Map<String, DownloadLiveState>> = _liveStates.asStateFlow()

    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    val activeDownloads: Flow<List<DownloadEntity>> = downloadDao.getActiveDownloads()
    val completedDownloads: Flow<List<DownloadEntity>> = downloadDao.getCompletedDownloads()

    init {
        // Inicializa NewPipe extractor
        try {
            org.schabi.newpipe.extractor.NewPipe.init(HughDownloader.getInstance())
        } catch (_: Exception) {}
    }

    /**
     * Inicia o fluxo completo de download para uma URL do YouTube
     */
    fun enqueueDownload(url: String, targetPlaylistId: Long? = null, onStarted: ((String) -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        scope.launch {
            try {
                val videoId = YouTubeExtractorHelper.extractVideoId(url)
                    ?: throw IllegalArgumentException("Link do YouTube inválido: $url")

                withContext(Dispatchers.Main) {
                    onStarted?.invoke(videoId)
                }

                // 1. Extração rápida de metadados
                val audioInfo = YouTubeExtractorHelper.extractAudio(url)

                // 2. Registra no banco de dados como PENDING
                val entity = DownloadEntity(
                    videoId = audioInfo.videoId,
                    title = audioInfo.title,
                    artist = audioInfo.artist,
                    durationSeconds = audioInfo.durationSeconds,
                    streamUrl = audioInfo.streamUrl,
                    originalUrl = audioInfo.originalUrl,
                    thumbnailUrl = audioInfo.thumbnailUrl,
                    targetPlaylistId = targetPlaylistId,
                    format = audioInfo.format,
                    itag = audioInfo.itag,
                    bytesDownloaded = 0L,
                    totalBytes = 0L,
                    status = DownloadEntity.STATUS_PENDING
                )
                downloadDao.insertDownload(entity)

                // 3. Dispara o download de alta velocidade
                startDownloadJob(entity)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError?.invoke(e.message ?: "Falha ao processar link")
                }
            }
        }
    }

    /**
     * Executa o download streaming otimizado com cálculo contínuo de velocidade e ETA
     */
    fun startDownloadJob(entity: DownloadEntity) {
        // Evita jobs duplicados para o mesmo vídeo
        if (activeJobs[entity.videoId]?.isActive == true) return

        val job = scope.launch {
            var lastDbUpdateTime = 0L
            var lastSpeedCheckTime = System.currentTimeMillis()
            var bytesAtLastSpeedCheck = 0L
            var currentSpeedBps = 0L

            try {
                downloadDao.updateStatus(entity.videoId, DownloadEntity.STATUS_DOWNLOADING)

                val targetFolder = getDownloadDestinationFolder()
                if (!targetFolder.exists()) targetFolder.mkdirs()

                val sanitizedTitle = entity.title.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
                val sanitizedArtist = entity.artist.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
                val baseFileName = if (sanitizedArtist.isNotBlank() && sanitizedArtist != "Unknown") {
                    "$sanitizedArtist - $sanitizedTitle"
                } else sanitizedTitle

                val tempFile = File(targetFolder, ".tmp_${entity.videoId}.${entity.format}")
                val finalFile = File(targetFolder, "$baseFileName.${entity.format}")

                // Requisição OkHttp otimizada para download rápido de áudio
                val request = Request.Builder()
                    .url(entity.streamUrl)
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                    )
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "identity") // Áudio já é compactado
                    .header("Connection", "keep-alive")
                    .build()

                val response = HughDownloader.okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Erro no servidor de áudio (HTTP ${response.code})")
                }

                val body = response.body ?: throw IllegalStateException("Corpo da resposta vazio")
                val totalBytes = body.contentLength().let { if (it > 0) it else entity.totalBytes }

                var downloadedBytes = 0L
                val bufferSize = 64 * 1024 // Buffer de 64 KB para máxima taxa de transferência
                val buffer = ByteArray(bufferSize)

                body.byteStream().use { inputStream: InputStream ->
                    BufferedOutputStream(FileOutputStream(tempFile), bufferSize).use { outputStream ->
                        var readBytes: Int
                        while (inputStream.read(buffer).also { readBytes = it } != -1) {
                            outputStream.write(buffer, 0, readBytes)
                            downloadedBytes += readBytes

                            val now = System.currentTimeMillis()
                            val speedDeltaTime = now - lastSpeedCheckTime

                            // Atualiza a velocidade calculada e ETA a cada 400ms
                            if (speedDeltaTime >= 400) {
                                val bytesDelta = downloadedBytes - bytesAtLastSpeedCheck
                                currentSpeedBps = if (speedDeltaTime > 0) (bytesDelta * 1000) / speedDeltaTime else 0L

                                lastSpeedCheckTime = now
                                bytesAtLastSpeedCheck = downloadedBytes

                                val speedFormatted = formatSpeed(currentSpeedBps)
                                val etaFormatted = formatEta(downloadedBytes, totalBytes, currentSpeedBps)
                                val percent = if (totalBytes > 0) ((downloadedBytes * 100f) / totalBytes).toInt().coerceIn(0, 100) else 0

                                // Atualiza stateflow em memória (sem bloquear I/O)
                                updateLiveState(
                                    DownloadLiveState(
                                        videoId = entity.videoId,
                                        title = entity.title,
                                        artist = entity.artist,
                                        bytesDownloaded = downloadedBytes,
                                        totalBytes = totalBytes,
                                        progressPercent = percent,
                                        speedBytesPerSec = currentSpeedBps,
                                        speedFormatted = speedFormatted,
                                        etaFormatted = etaFormatted,
                                        status = DownloadEntity.STATUS_DOWNLOADING
                                    )
                                )
                            }

                            // Grava no banco a cada 1.2 segundos para persistência
                            if (now - lastDbUpdateTime >= 1200) {
                                downloadDao.updateProgress(
                                    videoId = entity.videoId,
                                    bytesDownloaded = downloadedBytes,
                                    totalBytes = totalBytes,
                                    status = DownloadEntity.STATUS_DOWNLOADING
                                )
                                lastDbUpdateTime = now
                            }
                        }
                        outputStream.flush()
                    }
                }

                // Renomeia o arquivo temporário para o destino final
                if (finalFile.exists()) finalFile.delete()
                tempFile.renameTo(finalFile)

                // Atualiza banco com STATUS_COMPLETED
                downloadDao.updateProgress(
                    videoId = entity.videoId,
                    bytesDownloaded = downloadedBytes,
                    totalBytes = downloadedBytes,
                    status = DownloadEntity.STATUS_COMPLETED
                )

                // Remove do liveState ou marca como completo
                updateLiveState(
                    DownloadLiveState(
                        videoId = entity.videoId,
                        title = entity.title,
                        artist = entity.artist,
                        bytesDownloaded = downloadedBytes,
                        totalBytes = downloadedBytes,
                        progressPercent = 100,
                        speedBytesPerSec = 0,
                        speedFormatted = "CONCLUÍDO",
                        etaFormatted = "00:00",
                        status = DownloadEntity.STATUS_COMPLETED
                    )
                )

                // 1. Direct MediaStore registration with audio tags & metadata
                val mimeType = if (entity.format.equals("opus", ignoreCase = true)) "audio/ogg" else "audio/mp4"
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Audio.Media.TITLE, entity.title)
                        put(MediaStore.Audio.Media.ARTIST, if (entity.artist.isNotBlank() && entity.artist != "Unknown") entity.artist else "Artista Desconhecido")
                        put(MediaStore.Audio.Media.ALBUM, "Aether Vault")
                        put(MediaStore.Audio.Media.IS_MUSIC, 1)
                        put(MediaStore.Audio.Media.DATA, finalFile.absolutePath)
                        put(MediaStore.Audio.Media.DISPLAY_NAME, finalFile.name)
                        put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                        put(MediaStore.Audio.Media.SIZE, finalFile.length())
                        if (entity.durationSeconds > 0) {
                            put(MediaStore.Audio.Media.DURATION, entity.durationSeconds * 1000L)
                        }
                    }
                    context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                } catch (_: Exception) {}

                // 2. Notifica o MediaScannerConnection do Android para indexação imediata
                withContext(Dispatchers.Main) {
                    val scanMimeTypes = if (entity.format.equals("opus", ignoreCase = true)) {
                        arrayOf("audio/ogg", "audio/opus")
                    } else {
                        arrayOf("audio/mp4", "audio/m4a", "audio/aac")
                    }
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(finalFile.absolutePath),
                        scanMimeTypes
                    ) { _, _ ->
                        scope.launch {
                            // Se havia playlist de destino, adiciona após recarregar
                            repository.refreshLibrary()
                        }
                    }
                }

            } catch (e: CancellationException) {
                downloadDao.updateStatus(entity.videoId, DownloadEntity.STATUS_CANCELLED)
                removeLiveState(entity.videoId)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Erro desconhecido durante o download"
                downloadDao.updateStatus(entity.videoId, DownloadEntity.STATUS_FAILED, errorMsg)
                updateLiveState(
                    DownloadLiveState(
                        videoId = entity.videoId,
                        title = entity.title,
                        artist = entity.artist,
                        bytesDownloaded = 0,
                        totalBytes = 0,
                        progressPercent = 0,
                        speedBytesPerSec = 0,
                        speedFormatted = "FALHOU",
                        etaFormatted = "--:--",
                        status = DownloadEntity.STATUS_FAILED,
                        errorMessage = errorMsg
                    )
                )
            } finally {
                activeJobs.remove(entity.videoId)
            }
        }

        activeJobs[entity.videoId] = job
    }

    fun cancelDownload(videoId: String) {
        activeJobs[videoId]?.cancel()
        activeJobs.remove(videoId)
        scope.launch {
            downloadDao.updateStatus(videoId, DownloadEntity.STATUS_CANCELLED)
            removeLiveState(videoId)
        }
    }

    fun deleteDownload(videoId: String) {
        cancelDownload(videoId)
        scope.launch {
            downloadDao.deleteDownload(videoId)
            removeLiveState(videoId)
        }
    }

    fun clearCompletedDownloads() {
        scope.launch {
            downloadDao.clearCompleted()
        }
    }

    private fun updateLiveState(state: DownloadLiveState) {
        val current = _liveStates.value.toMutableMap()
        current[state.videoId] = state
        _liveStates.value = current
    }

    private fun removeLiveState(videoId: String) {
        val current = _liveStates.value.toMutableMap()
        current.remove(videoId)
        _liveStates.value = current
    }

    private fun getDownloadDestinationFolder(): File {
        // Tenta salvar na pasta pública de Música para que apareça em qualquer app e seja persistente
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val vaultDir = File(musicDir, "AetherVault")
        if (vaultDir.exists() || vaultDir.mkdirs()) {
            return vaultDir
        }
        return context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadManager? = null

        fun getInstance(context: Context, downloadDao: DownloadDao, repository: MusicRepository): DownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DownloadManager(context.applicationContext, downloadDao, repository).also {
                    INSTANCE = it
                }
            }
        }

        fun formatSpeed(bytesPerSec: Long): String {
            return when {
                bytesPerSec >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
                bytesPerSec >= 1024 -> String.format(Locale.US, "%d KB/s", bytesPerSec / 1024)
                bytesPerSec > 0 -> "$bytesPerSec B/s"
                else -> "0 KB/s"
            }
        }

        fun formatEta(bytesDownloaded: Long, totalBytes: Long, speedBytesPerSec: Long): String {
            if (totalBytes <= 0 || speedBytesPerSec <= 0 || bytesDownloaded >= totalBytes) {
                return "--:--"
            }
            val remainingBytes = totalBytes - bytesDownloaded
            val secondsRemaining = remainingBytes / speedBytesPerSec
            val minutes = secondsRemaining / 60
            val seconds = secondsRemaining % 60
            return if (minutes >= 60) {
                val hours = minutes / 60
                val remainingMins = minutes % 60
                String.format(Locale.US, "%dh %02dm", hours, remainingMins)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
                bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                bytes >= 1024 -> String.format(Locale.US, "%d KB", bytes / 1024)
                bytes > 0 -> "$bytes B"
                else -> "0 B"
            }
        }
    }
}
