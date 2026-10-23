package com.hughdevelopment.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM playback_stats ORDER BY play_count DESC")
    fun getAllStatsFlow(): Flow<List<StatsEntity>>

    @Query("SELECT * FROM playback_stats")
    suspend fun getAllStatsList(): List<StatsEntity>

    @Query("SELECT * FROM playback_stats WHERE is_favorite = 1")
    fun getFavoriteStatsFlow(): Flow<List<StatsEntity>>

    @Query("SELECT * FROM playback_stats ORDER BY play_count DESC LIMIT :limit")
    fun getTopPlayedTracks(limit: Int): Flow<List<StatsEntity>>

    @Query("SELECT * FROM playback_stats WHERE song_id = :songId LIMIT 1")
    suspend fun getStatsForSong(songId: Long): StatsEntity?

    @Query("SELECT SUM(total_seconds_listened) FROM playback_stats")
    fun getTotalSecondsListenedFlow(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM playback_stats WHERE play_count > 0")
    fun getUniquePlayedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM playback_stats WHERE is_favorite = 1")
    fun getFavoriteCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: StatsEntity)

    suspend fun recordTrackStarted(songId: Long, title: String, artist: String) {
        val existing = getStatsForSong(songId)
        if (existing == null) {
            insertOrUpdate(
                StatsEntity(
                    songId = songId,
                    title = title,
                    artist = artist,
                    playCount = 1,
                    totalSecondsListened = 0L,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            insertOrUpdate(
                existing.copy(
                    playCount = existing.playCount + 1,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun addSecondsListened(songId: Long, title: String, artist: String, seconds: Long) {
        if (seconds <= 0) return
        val existing = getStatsForSong(songId)
        if (existing == null) {
            insertOrUpdate(
                StatsEntity(
                    songId = songId,
                    title = title,
                    artist = artist,
                    playCount = 1,
                    totalSecondsListened = seconds,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            insertOrUpdate(
                existing.copy(
                    totalSecondsListened = existing.totalSecondsListened + seconds
                )
            )
        }
    }

    suspend fun toggleFavorite(songId: Long, title: String, artist: String): Boolean {
        val existing = getStatsForSong(songId)
        return if (existing == null) {
            insertOrUpdate(
                StatsEntity(
                    songId = songId,
                    title = title,
                    artist = artist,
                    playCount = 0,
                    totalSecondsListened = 0,
                    isFavorite = true
                )
            )
            true
        } else {
            val newFav = !existing.isFavorite
            insertOrUpdate(existing.copy(isFavorite = newFav))
            newFav
        }
    }
}
