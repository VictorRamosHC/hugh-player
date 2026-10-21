package com.hughdevelopment.player.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.hughdevelopment.player.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onSongStarted: (Song) -> Unit,
    private val onSongProgressLogged: (Song, Long) -> Unit
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var fadeManager: AudioFadeManager? = null

    var isFadeEnabled: Boolean = true
    var fadeDurationMs: Long = 800L

    private fun getEffectiveFadeDuration(): Long = if (isFadeEnabled) fadeDurationMs else 0L

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs = _durationMs.asStateFlow()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist = _playlist.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private var progressTrackingJob: Job? = null
    private var secondsAccumulatedForCurrentSong = 0L
    private var isPlayIntended = false

    fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()?.apply {
                    fadeManager = AudioFadeManager(scope, this)
                    addListener(playerListener)
                    _isShuffleEnabled.value = shuffleModeEnabled
                    _repeatMode.value = repeatMode
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            isPlayIntended = isPlaying
            if (isPlaying) {
                startPositionTracker()
            } else {
                stopPositionTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                skipNextWithFade()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                val song = _playlist.value.find { it.id == songId }
                if (song != null) {
                    flushAccumulatedListenStats()
                    _currentSong.value = song
                    _durationMs.value = song.durationMs
                    onSongStarted(song)
                }
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(newRepeatMode: Int) {
            _repeatMode.value = newRepeatMode
        }
    }

    fun playSong(song: Song, newPlaylist: List<Song> = _playlist.value) {
        val controller = mediaController ?: return
        flushAccumulatedListenStats()

        _playlist.value = newPlaylist
        _currentSong.value = song
        _durationMs.value = song.durationMs
        secondsAccumulatedForCurrentSong = 0L
        isPlayIntended = true
        _isPlaying.value = true

        // Trigger instantaneous play recording in DB
        onSongStarted(song)

        val mediaItems = newPlaylist.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(s.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.albumArtUri)
                        .build()
                )
                .build()
        }

        val targetIndex = newPlaylist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()

        val fadeDur = getEffectiveFadeDuration()
        if (fadeDur > 50L) {
            fadeManager?.fadeIn(fadeDur)
        } else {
            fadeManager?.cancel()
            controller.volume = 1.0f
            controller.play()
        }
    }

    fun togglePlayPauseWithFade() {
        val controller = mediaController ?: return
        val currentlyPlaying = isPlayIntended || controller.isPlaying

        if (currentlyPlaying) {
            // Intended action: PAUSE
            isPlayIntended = false
            _isPlaying.value = false
            flushAccumulatedListenStats()

            val fadeDur = getEffectiveFadeDuration()
            if (fadeDur > 50L) {
                fadeManager?.fadeOutAndPause(200L)
            } else {
                fadeManager?.cancel()
                controller.pause()
                controller.volume = 1.0f
            }
        } else {
            // Intended action: PLAY
            isPlayIntended = true
            _isPlaying.value = true

            val fadeDur = getEffectiveFadeDuration()
            if (fadeDur > 50L) {
                fadeManager?.fadeIn(fadeDur)
            } else {
                fadeManager?.cancel()
                controller.volume = 1.0f
                controller.play()
            }
        }
    }

    fun skipNextWithFade() {
        val controller = mediaController ?: return
        flushAccumulatedListenStats()
        val shouldPlay = isPlayIntended || controller.isPlaying

        // Immediate track transition
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else {
            controller.seekTo(0, 0L)
        }

        if (shouldPlay) {
            isPlayIntended = true
            _isPlaying.value = true
            val fadeDur = getEffectiveFadeDuration()
            if (fadeDur > 50L) {
                fadeManager?.fadeIn(fadeDur)
            } else {
                fadeManager?.cancel()
                controller.volume = 1.0f
                controller.play()
            }
        } else {
            // Respect paused state
            isPlayIntended = false
            _isPlaying.value = false
            fadeManager?.cancel()
            controller.volume = 1.0f
            controller.pause()
        }
    }

    fun skipPreviousWithFade() {
        val controller = mediaController ?: return
        flushAccumulatedListenStats()
        val shouldPlay = isPlayIntended || controller.isPlaying

        if (controller.currentPosition > 3000L || !controller.hasPreviousMediaItem()) {
            controller.seekTo(0L)
        } else {
            controller.seekToPreviousMediaItem()
        }

        if (shouldPlay) {
            isPlayIntended = true
            _isPlaying.value = true
            val fadeDur = getEffectiveFadeDuration()
            if (fadeDur > 50L) {
                fadeManager?.fadeIn(fadeDur)
            } else {
                fadeManager?.cancel()
                controller.volume = 1.0f
                controller.play()
            }
        } else {
            // Respect paused state
            isPlayIntended = false
            _isPlaying.value = false
            fadeManager?.cancel()
            controller.volume = 1.0f
            controller.pause()
        }
    }

    fun updateSongFavorite(songId: Long, isFavorite: Boolean) {
        if (_currentSong.value?.id == songId) {
            _currentSong.value = _currentSong.value?.copy(isFavorite = isFavorite)
        }
        _playlist.value = _playlist.value.map {
            if (it.id == songId) it.copy(isFavorite = isFavorite) else it
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackPositionMs.value = positionMs
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffle = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newShuffle
        _isShuffleEnabled.value = newShuffle
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
        _repeatMode.value = nextMode
    }

    private fun startPositionTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { c ->
                    _playbackPositionMs.value = c.currentPosition
                    _durationMs.value = if (c.duration > 0) c.duration else _currentSong.value?.durationMs ?: 0L
                    secondsAccumulatedForCurrentSong += 1

                    // Auto-flush every 5 seconds to continuously guarantee statistics persistence
                    if (secondsAccumulatedForCurrentSong >= 5) {
                        flushAccumulatedListenStats()
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun stopPositionTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
        flushAccumulatedListenStats()
    }

    private fun flushAccumulatedListenStats() {
        val song = _currentSong.value
        if (song != null && secondsAccumulatedForCurrentSong > 0) {
            onSongProgressLogged(song, secondsAccumulatedForCurrentSong)
            secondsAccumulatedForCurrentSong = 0L
        }
    }

    fun release() {
        flushAccumulatedListenStats()
        progressTrackingJob?.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
