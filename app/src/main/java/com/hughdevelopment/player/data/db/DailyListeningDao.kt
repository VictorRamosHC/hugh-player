package com.hughdevelopment.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyListeningDao {

    @Query("SELECT * FROM daily_listening ORDER BY date_key DESC")
    fun getAllDailyRecordsFlow(): Flow<List<DailyListeningEntity>>

    @Query("SELECT * FROM daily_listening WHERE date_key LIKE :monthPrefix ORDER BY date_key ASC")
    fun getRecordsForMonthFlow(monthPrefix: String): Flow<List<DailyListeningEntity>>

    @Query("SELECT * FROM daily_listening ORDER BY date_key DESC LIMIT :limit")
    suspend fun getRecentDays(limit: Int): List<DailyListeningEntity>

    @Query("SELECT * FROM daily_listening WHERE date_key = :dateKey LIMIT 1")
    suspend fun getRecordForDate(dateKey: String): DailyListeningEntity?

    @Query("SELECT COUNT(*) FROM daily_listening WHERE has_valid_streak_day = 1")
    fun getTotalActiveDaysFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyListeningEntity)

    suspend fun recordDailyListening(dateKey: String, addedSeconds: Long) {
        if (addedSeconds <= 0) return
        val existing = getRecordForDate(dateKey)
        if (existing == null) {
            val total = addedSeconds
            insertOrUpdate(
                DailyListeningEntity(
                    dateKey = dateKey,
                    totalSecondsListened = total,
                    songsPlayedCount = 1,
                    hasValidStreakDay = total >= 300L
                )
            )
        } else {
            val newTotal = existing.totalSecondsListened + addedSeconds
            insertOrUpdate(
                existing.copy(
                    totalSecondsListened = newTotal,
                    songsPlayedCount = existing.songsPlayedCount + 1,
                    hasValidStreakDay = newTotal >= 300L
                )
            )
        }
    }
}
