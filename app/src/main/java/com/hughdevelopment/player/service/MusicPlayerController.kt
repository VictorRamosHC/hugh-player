package com.hughdevelopment.player.service

import android.content.ComponentName
import android.content.Context
import android.net.Uri
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
    private val onSongProgressLogged: (Song, Long) -> Unit
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var fadeManager: AudioFadeManager? = null

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

    private var progressTrackingJob: Job? = null
    private var secondsAccumulatedForCurrentSong = 0L

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
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
                }
            }
        }
    }

    fun playSong(song: Song, newPlaylist: List<Song> = _playlist.value) {
        val controller = mediaController ?: return
        _playlist.value = newPlaylist
        _currentSong.value = song
        _durationMs.value = song.durationMs
        secondsAccumulatedForCurrentSong = 0L

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
        fadeManager?.fadeIn(400L) ?: controller.play()
    }

    fun togglePlayPauseWithFade() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            fadeManager?.fadeOutAndPause(350L) ?: controller.pause()
        } else {
            fadeManager?.fadeIn(350L) ?: controller.play()
        }
    }

    fun skipNextWithFade() {
        val controller = mediaController ?: return
        fadeManager?.fadeOutAndAction(250L) {
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
            } else {
                // Loop back to start if at end
                controller.seekTo(0, 0L)
            }
        }
    }

    fun skipPreviousWithFade() {
        val controller = mediaController ?: return
        fadeManager?.fadeOutAndAction(250L) {
            if (controller.currentPosition > 3000L || !controller.hasPreviousMediaItem()) {
                controller.seekTo(0L)
            } else {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackPositionMs.value = positionMs
    }

    private fun startPositionTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { c ->
                    _playbackPositionMs.value = c.currentPosition
                    _durationMs.value = if (c.duration > 0) c.duration else _currentSong.value?.durationMs ?: 0L
                    secondsAccumulatedForCurrentSong += 1
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
            secondsAccumulatedForCurrentSong = 0
        }
    }

    fun release() {
        flushAccumulatedListenStats()
        progressTrackingJob?.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
