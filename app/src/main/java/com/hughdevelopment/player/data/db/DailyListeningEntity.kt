package com.hughdevelopment.player.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_listening")
data class DailyListeningEntity(
    @PrimaryKey
    @ColumnInfo(name = "date_key")
    val dateKey: String, // Format: "YYYY-MM-DD", e.g. "2026-10-23"

    @ColumnInfo(name = "total_seconds_listened")
    val totalSecondsListened: Long = 0L,

    @ColumnInfo(name = "songs_played_count")
    val songsPlayedCount: Int = 0,

    @ColumnInfo(name = "has_valid_streak_day")
    val hasValidStreakDay: Boolean = false // Validated if user listened to >= 300 seconds (5 min)
) {
    val totalMinutes: Long
        get() = totalSecondsListened / 60
}
