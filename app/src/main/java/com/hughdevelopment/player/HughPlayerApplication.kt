package com.hughdevelopment.player

import android.app.Application
import com.hughdevelopment.player.data.db.PlayerDatabase
import com.hughdevelopment.player.data.repository.MusicRepository
import com.hughdevelopment.player.data.source.MediaStoreScanner
import com.hughdevelopment.player.data.ai.AgentDataManager

class HughPlayerApplication : Application() {

    lateinit var database: PlayerDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var agentDataManager: AgentDataManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = PlayerDatabase.getInstance(this)
        val scanner = MediaStoreScanner(this)
        repository = MusicRepository(scanner, database.statsDao())
        agentDataManager = AgentDataManager(this, repository)
    }
}
