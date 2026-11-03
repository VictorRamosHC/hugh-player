package com.hughdevelopment.player.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hughdevelopment.player.HughPlayerApplication
import com.hughdevelopment.player.data.backup.BackupImportResult
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.settings.EqualizerPreset
import com.hughdevelopment.player.data.settings.SettingsManager
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.data.settings.SortOrder
import com.hughdevelopment.player.data.settings.TimeDisplayFormat
import com.hughdevelopment.player.data.settings.formatListeningTime
import com.hughdevelopment.player.hermes.HermesVoiceManager
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
    val backupManager = app.backupManager
    val settingsManager = SettingsManager.getInstance(application)

    val settings: StateFlow<SettingsState> = settingsManager.settings

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
                repository.recordSongPlayed(song, secondsPlayed)
            }
        }
    )

    val songs: StateFlow<List<Song>> = repository.songs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val hiddenSongs: StateFlow<List<Song>> = repository.hiddenSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val listeningOverview: StateFlow<ListeningOverview> = repository.getListeningOverview()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
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
                val listeningTimes = overview.topSongs.associate { it.songId to it.totalSecondsListened }
                songList.sortedWith(
                    compareByDescending<Song> { listeningTimes[it.id] ?: 0L }
                        .thenByDescending { it.durationMs }
                )
            }
            SortOrder.FAVORITES_FIRST -> songList.sortedWith(
                compareByDescending<Song> { it.isFavorite }.thenBy { it.title.lowercase() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
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
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val playbackPositionMs: StateFlow<Long> = playerController.playbackPositionMs
    val durationMs: StateFlow<Long> = playerController.durationMs
    val isShuffleEnabled: StateFlow<Boolean> = playerController.isShuffleEnabled
    val repeatMode: StateFlow<Int> = playerController.repeatMode
    val queue: StateFlow<List<Song>> = playerController.queue

    // --- Audio DSP Equalizer & BassBoost Flows ---
    val equalizerEnabled: StateFlow<Boolean> = playerController.equalizerEnabled
    val equalizerPreset: StateFlow<EqualizerPreset> = playerController.equalizerPreset
    val equalizerBandLevels: StateFlow<List<Int>> = playerController.equalizerBandLevels
    val bassBoostStrength: StateFlow<Int> = playerController.bassBoostStrength
    val equalizerCenterFrequencies: StateFlow<List<Int>> = playerController.equalizerCenterFrequencies
    val equalizerBandLevelRange: StateFlow<IntRange> = playerController.equalizerBandLevelRange
    val isAudioEffectSupported: StateFlow<Boolean> = playerController.isAudioEffectSupported

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

    fun playFavorites() {
        val favs = songs.value.filter { it.isFavorite }
        if (favs.isNotEmpty()) {
            val shuffled = favs.shuffled()
            playerController.playSong(shuffled.first(), shuffled)
            if (!isShuffleEnabled.value) {
                playerController.toggleShuffle()
            }
        } else {
            shuffleAll()
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

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
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

    // --- Hierarchical Navigation Stack (BackHandler support) ---
    private val screenStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun canNavigateBack(): Boolean {
        return _isExpandedPlayerOpen.value || screenStack.isNotEmpty()
    }

    fun navigateBack(): Boolean {
        if (_isExpandedPlayerOpen.value) {
            _isExpandedPlayerOpen.value = false
            return true
        }
        if (screenStack.isNotEmpty()) {
            val previous = screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        return false
    }

    // --- Spotify-Style Playback Queue Methods ---
    fun playNext(song: Song) = playerController.playNext(song)
    fun playNext(songs: List<Song>) = playerController.playNext(songs)
    fun addToQueue(song: Song) = playerController.addToQueue(song)
    fun addToQueue(songs: List<Song>) = playerController.addToQueue(songs)
    fun moveQueueItem(fromIdx: Int, toIdx: Int) = playerController.moveQueueItem(fromIdx, toIdx)
    fun removeFromQueue(idx: Int) = playerController.removeFromQueue(idx)
    fun clearQueue() = playerController.clearQueue()

    // --- Hidden Songs Sovereign Management ---
    fun hideSong(song: Song) {
        viewModelScope.launch {
            repository.hideSong(song)
        }
    }

    fun hideSongs(songs: List<Song>) {
        viewModelScope.launch {
            repository.hideSongs(songs)
        }
    }

    fun unhideSong(songId: Long) {
        viewModelScope.launch {
            repository.unhideSong(songId)
        }
    }

    fun unhideAllSongs() {
        viewModelScope.launch {
            repository.unhideAllSongs()
        }
    }

    fun deleteSongs(songIds: List<Long>, onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = repository.deleteSongsFromDevice(songIds)
            onDone(count)
        }
    }

    // --- Audio DSP Equalizer Controls ---
    fun setEqualizerEnabled(enabled: Boolean) {
        playerController.setEqualizerEnabled(enabled)
        settingsManager.setEqualizerEnabled(enabled)
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        playerController.setEqualizerPreset(preset)
        settingsManager.setEqualizerPreset(preset)
    }

    fun setEqualizerBandLevel(bandIndex: Int, levelMillibels: Int) {
        playerController.setEqualizerBandLevel(bandIndex, levelMillibels)
        settingsManager.setEqualizerBandLevel(bandIndex, levelMillibels)
    }

    fun setEqualizerBandLevels(levels: List<Int>) {
        playerController.setEqualizerBandLevels(levels)
        settingsManager.setEqualizerBandLevels(levels)
    }

    fun setBassBoostStrength(strength: Int) {
        playerController.setBassBoostStrength(strength)
        settingsManager.setBassBoostStrength(strength)
    }

    // --- Settings & Telemetry Format ---
    fun setTimeDisplayFormat(format: TimeDisplayFormat) {
        settingsManager.setTimeDisplayFormat(format)
    }

    // --- Hermes Voice Briefing ---
    fun speakHermesDailyBriefing() {
        val ov = listeningOverview.value
        val set = settings.value
        val formattedTime = formatListeningTime(ov.totalSecondsListened, set.timeDisplayFormat)
        HermesVoiceManager.getInstance().speakDailyBriefing(
            formattedTime = formattedTime,
            streakDays = ov.streakInfo.currentStreak,
            rankTitle = ov.rankTier.title,
            totalTracks = ov.uniqueTracksListened
        )
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

    fun setThemeId(themeId: String) {
        settingsManager.setThemeId(themeId)
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

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val file = backupManager.exportBackupToFile()
                onResult(file.absolutePath)
            } catch (e: Exception) {
                onResult("Erro: ${e.message}")
            }
        }
    }

    fun importBackup(jsonString: String, onResult: (BackupImportResult) -> Unit) {
        viewModelScope.launch {
            val res = backupManager.importBackupFromJson(jsonString)
            loadLocalMusic()
            onResult(res)
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
