package com.hughdevelopment.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hughdevelopment.player.HughPlayerApplication
import com.hughdevelopment.player.MainActivity
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.download.DownloadLiveState
import com.hughdevelopment.player.data.download.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DownloadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null
    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        val app = application as HughPlayerApplication
        downloadManager = DownloadManager.getInstance(this, app.database.downloadDao(), app.repository)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Observa os estados dos downloads para atualizar a notificação em tempo real
        observeJob = serviceScope.launch {
            downloadManager.liveStates.collect { states ->
                val activeDownloads = states.values.filter { it.status == "DOWNLOADING" }
                if (activeDownloads.isNotEmpty()) {
                    val current = activeDownloads.first()
                    val notification = buildDownloadNotification(current)
                    startForeground(NOTIFICATION_ID, notification)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val videoId = intent?.getStringExtra(EXTRA_VIDEO_ID)
        val url = intent?.getStringExtra(EXTRA_URL)

        when (action) {
            ACTION_START_DOWNLOAD -> {
                if (!url.isNullOrBlank()) {
                    val initialNotification = buildInitialNotification()
                    startForeground(NOTIFICATION_ID, initialNotification)
                    downloadManager.enqueueDownload(url)
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                if (!videoId.isNullOrBlank()) {
                    downloadManager.cancelDownload(videoId)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aether Ingestion // Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progresso de download de faixas e telemetria"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildInitialNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AETHER // INGESTÃO DE ÁUDIO")
            .setContentText("Conectando aos fluxos do YouTube...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun buildDownloadNotification(state: DownloadLiveState): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_CANCEL_DOWNLOAD
            putExtra(EXTRA_VIDEO_ID, state.videoId)
        }
        val cancelPendingIntent = PendingIntent.getService(
            this, state.videoId.hashCode(), cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val infoText = "${state.progressPercent}% • ${state.speedFormatted} • ETA ${state.etaFormatted}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BAIXANDO: ${state.title}")
            .setContentText(infoText)
            .setSubText(state.artist)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setProgress(100, state.progressPercent, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "CANCELAR", cancelPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        observeJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "hugh_download_channel"
        const val NOTIFICATION_ID = 2024
        const val ACTION_START_DOWNLOAD = "com.hughdevelopment.player.action.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.hughdevelopment.player.action.CANCEL_DOWNLOAD"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_VIDEO_ID = "extra_video_id"

        fun startDownload(context: Context, url: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_URL, url)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
