package com.hughdevelopment.player.data.model

data class PlaybackStats(
    val songId: Long,
    val playCount: Int,
    val totalSecondsListened: Long,
    val lastPlayedTimestamp: Long
)

data class ListeningOverview(
    val totalMinutesListened: Long,
    val totalSongsPlayed: Int,
    val uniqueTracksListened: Int,
    val topSongs: List<TopSongSummary>
)

data class TopSongSummary(
    val songId: Long,
    val title: String,
    val artist: String,
    val playCount: Int,
    val totalMinutesListened: Long
)
