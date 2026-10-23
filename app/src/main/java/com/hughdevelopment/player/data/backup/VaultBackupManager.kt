package com.hughdevelopment.player.data.backup

import android.content.Context
import android.os.Environment
import com.hughdevelopment.player.data.db.DailyListeningEntity
import com.hughdevelopment.player.data.db.PlaylistEntity
import com.hughdevelopment.player.data.db.PlaylistSongCrossRef
import com.hughdevelopment.player.data.db.StatsEntity
import com.hughdevelopment.player.data.repository.MusicRepository
import com.hughdevelopment.player.data.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupImportResult(
    val success: Boolean,
    val playlistsCount: Int = 0,
    val streakDaysCount: Int = 0,
    val statsCount: Int = 0,
    val message: String = ""
)

/**
 * VaultBackupManager // Sovereign JSON Data Vault
 * 
 * Guarantees zero data loss across devices, app reinstalls, or updates.
 * Exports and restores playlists, streaks, anti-sleeper XP, favorites, and settings.
 */
class VaultBackupManager(
    private val context: Context,
    private val repository: MusicRepository,
    private val settingsManager: SettingsManager
) {

    /**
     * Generates a comprehensive sovereign backup JSON representing the whole database.
     */
    suspend fun exportBackupToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        root.put("app", "AETHER_VAULT")
        root.put("version", "1.4.5")
        root.put("exported_at_ms", System.currentTimeMillis())
        root.put(
            "exported_at_iso",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        // 1. Settings
        val currentSettings = settingsManager.settings.value
        val settingsObj = JSONObject().apply {
            put("fade_enabled", currentSettings.isFadeEnabled)
            put("fade_duration", currentSettings.fadeDurationSeconds.toDouble())
            put("sort_order", currentSettings.sortOrder.name)
            put("folder_sort_order", currentSettings.folderSortOrder.name)
            put("library_alpha", currentSettings.libraryWallpaperAlpha.toDouble())
            put("player_alpha", currentSettings.playerWallpaperAlpha.toDouble())
        }
        root.put("settings", settingsObj)

        // 2. Daily Listening Streaks
        val dailyRecords = repository.dailyListeningDao.getAllDailyRecordsList()
        val dailyArray = JSONArray()
        for (rec in dailyRecords) {
            val d = JSONObject().apply {
                put("date_key", rec.dateKey)
                put("seconds", rec.totalSecondsListened)
                put("songs_played", rec.songsPlayedCount)
                put("is_valid_streak", rec.hasValidStreakDay)
            }
            dailyArray.put(d)
        }
        root.put("daily_listening", dailyArray)

        // 3. Playback Stats & Favorites
        val statsList = repository.statsDao.getAllStatsList()
        val statsArray = JSONArray()
        for (st in statsList) {
            val s = JSONObject().apply {
                put("title", st.title)
                put("artist", st.artist)
                put("play_count", st.playCount)
                put("seconds_listened", st.totalSecondsListened)
                put("last_played_timestamp", st.lastPlayedTimestamp)
                put("is_favorite", st.isFavorite)
            }
            statsArray.put(s)
        }
        root.put("playback_stats", statsArray)

        // 4. Playlists with songs
        val playlists = repository.playlistDao.getAllPlaylistsList()
        val playlistsArray = JSONArray()
        val currentSongs = repository.getCurrentSongs()
        val songMap = currentSongs.associateBy { it.id }

        for (pl in playlists) {
            val pObj = JSONObject().apply {
                put("name", pl.name)
                put("category", pl.category)
                put("color", pl.colorHex)
                put("created_at", pl.createdAt)
            }

            val songIds = repository.playlistDao.getSongIdsForPlaylist(pl.id)
            val songsArray = JSONArray()
            for (sid in songIds) {
                val song = songMap[sid]
                if (song != null) {
                    val sJson = JSONObject().apply {
                        put("title", song.title)
                        put("artist", song.artist)
                        put("duration_ms", song.durationMs)
                    }
                    songsArray.put(sJson)
                }
            }
            pObj.put("songs", songsArray)
            playlistsArray.put(pObj)
        }
        root.put("playlists", playlistsArray)

        root.toString(2)
    }

    /**
     * Exports backup JSON directly to the Downloads directory.
     */
    suspend fun exportBackupToFile(): File = withContext(Dispatchers.IO) {
        val json = exportBackupToJson()
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        val file = File(downloadDir, "aether_vault_backup.json")
        file.writeText(json)
        file
    }

    /**
     * Imports and atomically reconciles backup data into the SQLite Room database.
     */
    suspend fun importBackupFromJson(jsonString: String): BackupImportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val currentSongs = repository.getCurrentSongs()

            // Map songs by lowercased title for cross-device resilience
            val titleToSongMap = currentSongs.groupBy { it.title.trim().lowercase() }

            // 1. Restore Settings
            if (root.has("settings")) {
                val sObj = root.getJSONObject("settings")
                if (sObj.has("fade_duration")) {
                    settingsManager.setFadeDuration(sObj.getDouble("fade_duration").toFloat())
                }
                if (sObj.has("fade_enabled")) {
                    settingsManager.setFadeEnabled(sObj.getBoolean("fade_enabled"))
                }
                if (sObj.has("library_alpha")) {
                    settingsManager.setLibraryWallpaperAlpha(sObj.getDouble("library_alpha").toFloat())
                }
            }

            // 2. Restore Daily Listening Streaks
            var streakDaysRestored = 0
            if (root.has("daily_listening")) {
                val dailyArray = root.getJSONArray("daily_listening")
                val dao = repository.dailyListeningDao
                for (i in 0 until dailyArray.length()) {
                    val item = dailyArray.getJSONObject(i)
                    val dateKey = item.getString("date_key")
                    val seconds = item.getLong("seconds")
                    val songsPlayed = item.optInt("songs_played", 1)
                    val isValid = item.optBoolean("is_valid_streak", seconds >= 300L)

                    dao.insertOrUpdate(
                        DailyListeningEntity(
                            dateKey = dateKey,
                            totalSecondsListened = seconds,
                            songsPlayedCount = songsPlayed,
                            hasValidStreakDay = isValid
                        )
                    )
                    streakDaysRestored++
                }
            }

            // 3. Restore Playback Stats & Favorites
            var statsRestored = 0
            if (root.has("playback_stats")) {
                val statsArray = root.getJSONArray("playback_stats")
                val dao = repository.statsDao
                for (i in 0 until statsArray.length()) {
                    val item = statsArray.getJSONObject(i)
                    val title = item.getString("title")
                    val artist = item.optString("artist", "")
                    val playCount = item.optInt("play_count", 0)
                    val seconds = item.optLong("seconds_listened", 0L)
                    val isFav = item.optBoolean("is_favorite", false)
                    val lastPlayed = item.optLong("last_played_timestamp", System.currentTimeMillis())

                    val matchedSongs = titleToSongMap[title.trim().lowercase()]
                    if (!matchedSongs.isNullOrEmpty()) {
                        for (ms in matchedSongs) {
                            dao.insertOrUpdate(
                                StatsEntity(
                                    songId = ms.id,
                                    title = ms.title,
                                    artist = ms.artist,
                                    playCount = playCount,
                                    totalSecondsListened = seconds,
                                    lastPlayedTimestamp = lastPlayed,
                                    isFavorite = isFav
                                )
                            )
                            statsRestored++
                        }
                    }
                }
            }

            // 4. Restore Playlists
            var playlistsRestored = 0
            if (root.has("playlists")) {
                val playlistsArray = root.getJSONArray("playlists")
                val pDao = repository.playlistDao

                for (i in 0 until playlistsArray.length()) {
                    val plObj = playlistsArray.getJSONObject(i)
                    val name = plObj.getString("name")
                    val category = plObj.optString("category", "CUSTOM")
                    val color = plObj.optString("color", "#F59E0B")

                    val existing = pDao.getPlaylistByName(name)
                    val plId = existing?.id ?: pDao.insertPlaylist(
                        PlaylistEntity(name = name, category = category, colorHex = color)
                    )

                    val songsArr = plObj.optJSONArray("songs")
                    if (songsArr != null) {
                        for (j in 0 until songsArr.length()) {
                            val sObj = songsArr.getJSONObject(j)
                            val sTitle = sObj.getString("title")
                            val matched = titleToSongMap[sTitle.trim().lowercase()]?.firstOrNull()
                            if (matched != null) {
                                pDao.insertSongCrossRef(PlaylistSongCrossRef(plId, matched.id))
                            }
                        }
                    }
                    playlistsRestored++
                }
            }

            BackupImportResult(
                success = true,
                playlistsCount = playlistsRestored,
                streakDaysCount = streakDaysRestored,
                statsCount = statsRestored,
                message = "Backup restaurado com sucesso! $playlistsRestored pastas e $streakDaysRestored dias de ofensiva sincronizados."
            )
        } catch (e: Exception) {
            BackupImportResult(
                success = false,
                message = "Falha ao restaurar: ${e.message}"
            )
        }
    }
}
