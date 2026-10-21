package com.hughdevelopment.player.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hughdevelopment.player.HughPlayerApplication
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.service.MusicPlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HughPlayerApplication
    val repository = app.repository
    val agentManager = app.agentDataManager

    val playerController = MusicPlayerController(
        context = application.applicationContext,
        scope = viewModelScope,
        onSongStarted = { song ->
            viewModelScope.launch {
                repository.recordSongStarted(song)
            }
        },
        onSongProgressLogged = { song, secondsPlayed ->
            viewModelScope.launch {
                repository.recordSongProgress(song, secondsPlayed)
            }
        }
    )

    val songs: StateFlow<List<Song>> = repository.songs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isLoading: StateFlow<Boolean> = repository.isLoading

    val currentPlayingSong: StateFlow<Song?> = playerController.currentSong
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val playbackPositionMs: StateFlow<Long> = playerController.playbackPositionMs
    val durationMs: StateFlow<Long> = playerController.durationMs
    val isShuffleEnabled: StateFlow<Boolean> = playerController.isShuffleEnabled
    val repeatMode: StateFlow<Int> = playerController.repeatMode

    val listeningOverview: StateFlow<ListeningOverview> = repository.getListeningOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListeningOverview(0, 0, 0, 0, emptyList())
        )

    private val _isExpandedPlayerOpen = MutableStateFlow(false)
    val isExpandedPlayerOpen = _isExpandedPlayerOpen.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.LIBRARY)
    val currentScreen = _currentScreen.asStateFlow()

    init {
        playerController.initialize()
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
            repository.toggleFavorite(song)
        }
    }

    fun setExpandedPlayerOpen(open: Boolean) {
        _isExpandedPlayerOpen.value = open
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}

enum class AppScreen {
    LIBRARY,
    STATS_DASHBOARD
}
