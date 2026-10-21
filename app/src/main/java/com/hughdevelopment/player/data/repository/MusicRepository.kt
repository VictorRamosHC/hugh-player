package com.hughdevelopment.player.data.repository

import com.hughdevelopment.player.data.db.StatsDao
import com.hughdevelopment.player.data.db.StatsEntity
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.model.TopSongSummary
import com.hughdevelopment.player.data.source.MediaStoreScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class MusicRepository(
    private val scanner: MediaStoreScanner,
    private val statsDao: StatsDao
) {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    suspend fun refreshLibrary() {
        _isLoading.value = true
        try {
            val scanned = scanner.scanLocalSongs()
            _songs.value = scanned
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun recordSongPlayed(song: Song, secondsPlayed: Long) {
        if (secondsPlayed >= 10) { // Count as play only if listened for at least 10s
            statsDao.recordPlayProgress(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                addedSeconds = secondsPlayed
            )
        }
    }

    fun getListeningOverview(): Flow<ListeningOverview> {
        return combine(
            statsDao.getAllStatsFlow(),
            statsDao.getTotalSecondsListenedFlow(),
            statsDao.getUniquePlayedCountFlow()
        ) { statsList, totalSeconds, uniqueCount ->
            val totalMinutes = (totalSeconds ?: 0L) / 60
            val topSongs = statsList.take(10).map { entity ->
                TopSongSummary(
                    songId = entity.songId,
                    title = entity.title,
                    artist = entity.artist,
                    playCount = entity.playCount,
                    totalMinutesListened = entity.totalSecondsListened / 60
                )
            }
            ListeningOverview(
                totalMinutesListened = totalMinutes,
                totalSongsPlayed = statsList.sumOf { it.playCount },
                uniqueTracksListened = uniqueCount,
                topSongs = topSongs
            )
        }
    }

    fun searchSongs(query: String): List<Song> {
        val current = _songs.value
        if (query.isBlank()) return current
        val q = query.trim().lowercase()
        return current.filter { song ->
            song.title.lowercase().contains(q) ||
            song.artist.lowercase().contains(q) ||
            song.album.lowercase().contains(q)
        }
    }
}
