package com.hughdevelopment.player

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.hughdevelopment.player.data.ai.AgentDataManager
import com.hughdevelopment.player.data.db.PlayerDatabase
import com.hughdevelopment.player.data.repository.MusicRepository
import com.hughdevelopment.player.data.settings.SettingsManager
import com.hughdevelopment.player.data.source.MediaStoreScanner
import com.hughdevelopment.player.notifications.VaultNotificationHelper

class HughPlayerApplication : Application(), ImageLoaderFactory {

    lateinit var database: PlayerDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var agentDataManager: AgentDataManager
        private set

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        VaultNotificationHelper.initNotificationChannel(this)
        database = PlayerDatabase.getInstance(this)
        settingsManager = SettingsManager.getInstance(this)
        val scanner = MediaStoreScanner(this)
        repository = MusicRepository(
            scanner = scanner,
            statsDao = database.statsDao(),
            playlistDao = database.playlistDao(),
            dailyListeningDao = database.dailyListeningDao(),
            context = this
        )
        agentDataManager = AgentDataManager(this, repository)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("hugh_artwork_cache"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}
