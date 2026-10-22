package com.hughdevelopment.player.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playback_stats",
    indices = [
        Index(value = ["is_favorite"]),
        Index(value = ["play_count"]),
        Index(value = ["total_seconds_listened"])
    ]
)
data class StatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long,

    @ColumnInfo(name = "song_title")
    val title: String,

    @ColumnInfo(name = "song_artist")
    val artist: String,

    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,

    @ColumnInfo(name = "total_seconds_listened")
    val totalSecondsListened: Long = 0L,

    @ColumnInfo(name = "last_played_timestamp")
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false
)
