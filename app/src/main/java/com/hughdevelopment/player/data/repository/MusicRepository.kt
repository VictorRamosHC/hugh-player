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
    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())

    fun getCurrentSongs(): List<Song> = _rawSongs.value

    // Combine scanned songs with favorites from Room DB
    val songs: Flow<List<Song>> = combine(_rawSongs, statsDao.getFavoriteStatsFlow()) { songList, favEntities ->
        val favIdSet = favEntities.map { it.songId }.toSet()
        songList.map { song ->
            if (favIdSet.contains(song.id)) song.copy(isFavorite = true) else song.copy(isFavorite = false)
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    suspend fun refreshLibrary() {
        _isLoading.value = true
        try {
            val scanned = scanner.scanLocalSongs()
            _rawSongs.value = scanned
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun recordSongStarted(song: Song) {
        statsDao.recordTrackStarted(
            songId = song.id,
            title = song.title,
            artist = song.artist
        )
    }

    suspend fun recordSongProgress(song: Song, secondsPlayed: Long) {
        if (secondsPlayed > 0) {
            statsDao.addSecondsListened(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                seconds = secondsPlayed
            )
        }
    }

    suspend fun toggleFavorite(song: Song): Boolean {
        return statsDao.toggleFavorite(song.id, song.title, song.artist)
    }

    fun getListeningOverview(): Flow<ListeningOverview> {
        return combine(
            statsDao.getAllStatsFlow(),
            statsDao.getTotalSecondsListenedFlow(),
            statsDao.getUniquePlayedCountFlow(),
            statsDao.getFavoriteCountFlow()
        ) { statsList, totalSeconds, uniqueCount, favCount ->
            val totalSec = totalSeconds ?: 0L
            val topSongs = statsList.filter { it.playCount > 0 }.take(10).map { entity ->
                TopSongSummary(
                    songId = entity.songId,
                    title = entity.title,
                    artist = entity.artist,
                    playCount = entity.playCount,
                    totalSecondsListened = entity.totalSecondsListened
                )
            }
            ListeningOverview(
                totalSecondsListened = totalSec,
                totalSongsPlayed = statsList.sumOf { it.playCount },
                uniqueTracksListened = uniqueCount,
                favoriteCount = favCount,
                topSongs = topSongs
            )
        }
    }

    fun searchSongs(query: String): List<Song> {
        val current = _rawSongs.value
        if (query.isBlank()) return current
        val q = query.trim().lowercase()
        return current.filter { song ->
            song.title.lowercase().contains(q) ||
            song.artist.lowercase().contains(q) ||
            song.album.lowercase().contains(q)
        }
    }
}
