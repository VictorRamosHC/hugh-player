package com.hughdevelopment.player.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hughdevelopment.player.HughPlayerApplication
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.settings.SettingsManager
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.data.settings.SortOrder
import com.hughdevelopment.player.service.MusicPlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HughPlayerApplication
    val repository = app.repository
    val agentManager = app.agentDataManager
    val settingsManager = SettingsManager.getInstance(application)

    val settings: StateFlow<SettingsState> = settingsManager.settings

    val playerController = MusicPlayerController(
        context = application.applicationContext,
        scope = viewModelScope,
        onSongStarted = { song ->
            viewModelScope.launch {
                repository.recordSongStart(song)
            }
        },
        onSongProgressLogged = { song, secondsPlayed ->
            viewModelScope.launch {
                repository.recordSongPlayed(song, secondsPlayed)
            }
        }
    )

    val songs: StateFlow<List<Song>> = repository.songs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val listeningOverview: StateFlow<ListeningOverview> = repository.getListeningOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListeningOverview(0, 0, 0, 0, emptyList())
        )

    // Dynamic reactive sorted playlist based on user settings (Recent, Size, Most Played, Favorites, A-Z)
    val sortedSongs: StateFlow<List<Song>> = combine(
        songs,
        listeningOverview,
        settings
    ) { songList, overview, set ->
        when (set.sortOrder) {
            SortOrder.DEFAULT -> songList.sortedBy { it.title.lowercase() }
            SortOrder.DATE_ADDED_DESC -> songList.sortedByDescending { it.dateAdded }
            SortOrder.SIZE_DESC -> songList.sortedByDescending { it.sizeBytes }
            SortOrder.MOST_PLAYED -> {
                val playCounts = overview.topSongs.associate { it.songId to it.playCount }
                songList.sortedByDescending { playCounts[it.id] ?: 0 }
            }
            SortOrder.FAVORITES_FIRST -> songList.sortedWith(
                compareByDescending<Song> { it.isFavorite }.thenBy { it.title.lowercase() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isLoading: StateFlow<Boolean> = repository.isLoading

    // Seamlessly combined so favorite toggling instantly reflects on the active playback bar
    val currentPlayingSong: StateFlow<Song?> = combine(
        playerController.currentSong,
        songs
    ) { controllerSong, songList ->
        if (controllerSong == null) null
        else songList.find { it.id == controllerSong.id } ?: controllerSong
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val playbackPositionMs: StateFlow<Long> = playerController.playbackPositionMs
    val durationMs: StateFlow<Long> = playerController.durationMs
    val isShuffleEnabled: StateFlow<Boolean> = playerController.isShuffleEnabled
    val repeatMode: StateFlow<Int> = playerController.repeatMode

    private val _isExpandedPlayerOpen = MutableStateFlow(false)
    val isExpandedPlayerOpen = _isExpandedPlayerOpen.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.LIBRARY)
    val currentScreen = _currentScreen.asStateFlow()

    init {
        playerController.initialize()
        viewModelScope.launch {
            settingsManager.settings.collect { s ->
                playerController.isFadeEnabled = s.isFadeEnabled
                playerController.fadeDurationMs = (s.fadeDurationSeconds * 1000f).toLong()
            }
        }

        // Auto-restore last playback session seamlessly once library songs are indexed
        viewModelScope.launch {
            songs.collect { songList ->
                if (songList.isNotEmpty() && playerController.currentSong.value == null) {
                    val lastSongId = settingsManager.getLastPlayedSongId()
                    if (lastSongId != null) {
                        val lastSong = songList.find { it.id == lastSongId }
                        if (lastSong != null) {
                            val lastPos = settingsManager.getLastPlayedPositionMs()
                            playerController.restoreLastPlayedSession(lastSong, lastPos, songList)
                        }
                    }
                }
            }
        }
    }

    fun loadLocalMusic() {
        viewModelScope.launch {
            repository.refreshLibrary()
        }
    }

    fun playSong(song: Song, playlist: List<Song>) {
        playerController.playSong(song, playlist)
    }

    fun togglePlayPause() {
        playerController.togglePlayPauseWithFade()
    }

    fun skipNext() {
        playerController.skipNextWithFade()
    }

    fun skipPrevious() {
        playerController.skipPreviousWithFade()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleRepeat() {
        playerController.toggleRepeat()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val newFav = !song.isFavorite
            playerController.updateSongFavorite(song.id, newFav)
            val finalFav = repository.toggleFavorite(song)
            playerController.updateSongFavorite(song.id, finalFav)
        }
    }

    fun playPlaylist(playlist: Playlist) {
        if (playlist.songs.isNotEmpty()) {
            playerController.playSong(playlist.songs.first(), playlist.songs)
        }
    }

    fun playSongFromPlaylist(song: Song, playlistSongs: List<Song>) {
        playerController.playSong(song, playlistSongs)
    }

    fun shuffleAll() {
        val all = sortedSongs.value
        if (all.isNotEmpty()) {
            val shuffled = all.shuffled()
            playerController.playSong(shuffled.first(), shuffled)
            if (!isShuffleEnabled.value) {
                playerController.toggleShuffle()
            }
        }
    }

    fun createPlaylist(name: String, category: String, colorHex: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, category, colorHex)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            repository.addSongsToPlaylist(playlistId, songIds)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun autoOrganizeWithAiPresets(onResult: (Map<String, Int>) -> Unit) {
        viewModelScope.launch {
            val current = repository.getCurrentSongs().ifEmpty { songs.value }
            val counts = repository.autoOrganizeWithAiPresets(current)
            onResult(counts)
        }
    }

    fun setExpandedPlayerOpen(open: Boolean) {
        _isExpandedPlayerOpen.value = open
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSortOrder(order: SortOrder) {
        settingsManager.setSortOrder(order)
    }

    fun setFolderSortOrder(order: SortOrder) {
        settingsManager.setFolderSortOrder(order)
    }

    fun setFadeEnabled(enabled: Boolean) {
        settingsManager.setFadeEnabled(enabled)
    }

    fun setFadeDuration(seconds: Float) {
        settingsManager.setFadeDuration(seconds)
    }

    fun setLibraryWallpaperAlpha(alpha: Float) {
        settingsManager.setLibraryWallpaperAlpha(alpha)
    }

    fun setPlayerWallpaperAlpha(alpha: Float) {
        settingsManager.setPlayerWallpaperAlpha(alpha)
    }

    fun exportAiManifest(context: Context, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val path = agentManager.exportManifestToFile(context)
                onResult(path)
            } catch (e: Exception) {
                onResult("Erro: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}

enum class AppScreen {
    LIBRARY,
    FOLDERS,
    STATS_DASHBOARD,
    SETTINGS
}
