package com.hughdevelopment.player.data.model

data class PlaybackStats(
    val songId: Long,
    val playCount: Int,
    val totalSecondsListened: Long,
    val lastPlayedTimestamp: Long,
    val isFavorite: Boolean = false
)

data class StreakInfo(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isTodayCompleted: Boolean = false,
    val totalActiveDays: Int = 0
)

data class DayListeningSummary(
    val dateKey: String, // "YYYY-MM-DD"
    val dayOfMonth: Int,
    val dayOfWeekLabel: String, // "SEG", "TER", "QUA", etc.
    val minutes: Long,
    val hasValidStreak: Boolean
)

data class SovereignRank(
    val level: Int,
    val title: String,
    val nextThreshold: Int,
    val colorHex: String
)

data class ListeningOverview(
    val totalSecondsListened: Long = 0L,
    val totalSongsPlayed: Int = 0,
    val uniqueTracksListened: Int = 0,
    val favoriteCount: Int = 0,
    val topSongs: List<TopSongSummary> = emptyList(),
    val streakInfo: StreakInfo = StreakInfo(),
    val monthlyCalendar: List<DayListeningSummary> = emptyList(),
    val recent7Days: List<DayListeningSummary> = emptyList(),
    val averageDailyMinutes: Long = 0L
) {
    val totalMinutesListened: Long
        get() = totalSecondsListened / 60

    // Anti-Sleeper XP Formula:
    // Heavy rewards for daily consistency, streak maintenance and collection diversity.
    // Passive overnight continuous listening is capped to prevent sleeping exploits.
    val xp: Int
        get() {
            val streakXp = streakInfo.currentStreak * 150
            val activeDaysXp = streakInfo.totalActiveDays * 60
            val tracksXp = (uniqueTracksListened * 15).coerceAtMost(1500)
            val favXp = (favoriteCount * 30).coerceAtMost(600)
            val playXp = (totalSongsPlayed * 4).coerceAtMost(1000)
            val timeXp = (totalMinutesListened * 1).coerceAtMost(600).toInt()

            return streakXp + activeDaysXp + tracksXp + favXp + playXp + timeXp
        }

    val rankTier: SovereignRank
        get() = when {
            xp < 500 -> SovereignRank(1, "INICIADO DO AETHER", 500, "#06B6D4")
            xp < 1500 -> SovereignRank(2, "GUARDIÃO DO VAULT", 1500, "#F59E0B")
            xp < 3500 -> SovereignRank(3, "ALQUIMISTA DO SOM", 3500, "#0022FF")
            else -> SovereignRank(4, "ARCONTE SOBERANO", 6000, "#A855F7")
        }

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
