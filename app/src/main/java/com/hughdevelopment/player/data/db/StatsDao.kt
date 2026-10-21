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

    @Query("SELECT * FROM playback_stats ORDER BY play_count DESC LIMIT :limit")
    fun getTopPlayedTracks(limit: Int): Flow<List<StatsEntity>>

    @Query("SELECT * FROM playback_stats WHERE song_id = :songId LIMIT 1")
    suspend fun getStatsForSong(songId: Long): StatsEntity?

    @Query("SELECT SUM(total_seconds_listened) FROM playback_stats")
    fun getTotalSecondsListenedFlow(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM playback_stats WHERE play_count > 0")
    fun getUniquePlayedCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: StatsEntity)

    suspend fun recordPlayProgress(songId: Long, title: String, artist: String, addedSeconds: Long) {
        val existing = getStatsForSong(songId)
        if (existing == null) {
            insertOrUpdate(
                StatsEntity(
                    songId = songId,
                    title = title,
                    artist = artist,
                    playCount = 1,
                    totalSecondsListened = addedSeconds,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            insertOrUpdate(
                existing.copy(
                    playCount = existing.playCount + 1,
                    totalSecondsListened = existing.totalSecondsListened + addedSeconds,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
