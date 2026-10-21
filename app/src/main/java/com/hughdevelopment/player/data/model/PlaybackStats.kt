package com.hughdevelopment.player.data.model

data class PlaybackStats(
    val songId: Long,
    val playCount: Int,
    val totalSecondsListened: Long,
    val lastPlayedTimestamp: Long,
    val isFavorite: Boolean = false
)

data class ListeningOverview(
    val totalSecondsListened: Long,
    val totalSongsPlayed: Int,
    val uniqueTracksListened: Int,
    val favoriteCount: Int,
    val topSongs: List<TopSongSummary>
) {
    val totalMinutesListened: Long
        get() = totalSecondsListened / 60

    val formattedTotalTime: String
        get() = when {
            totalSecondsListened <= 0 -> "0 min"
            totalSecondsListened < 60 -> "${totalSecondsListened}s"
            totalSecondsListened < 3600 -> "${totalSecondsListened / 60} min"
            else -> {
                val hours = totalSecondsListened / 3600
                val mins = (totalSecondsListened % 3600) / 60
                "${hours}h ${mins}m"
            }
        }
}

data class TopSongSummary(
    val songId: Long,
    val title: String,
    val artist: String,
    val playCount: Int,
    val totalSecondsListened: Long
) {
    val totalMinutesListened: Long
        get() = totalSecondsListened / 60

    val formattedTime: String
        get() = when {
            totalSecondsListened < 60 -> "${totalSecondsListened}s"
            else -> "${totalSecondsListened / 60}m"
        }
}
