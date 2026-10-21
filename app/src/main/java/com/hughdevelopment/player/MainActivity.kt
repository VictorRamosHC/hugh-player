package com.hughdevelopment.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.hughdevelopment.player.ui.components.ExpandedPlayerSheet
import com.hughdevelopment.player.ui.components.MiniPlayerBar
import com.hughdevelopment.player.ui.screens.LibraryScreen
import com.hughdevelopment.player.ui.screens.StatsDashboardScreen
import com.hughdevelopment.player.ui.theme.HughPlayerTheme
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.viewmodel.AppScreen
import com.hughdevelopment.player.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        if (audioGranted) {
            viewModel.loadLocalMusic()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            HughPlayerTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.loadLocalMusic()
        }
    }
}

@Composable
fun MainScreen(viewModel: MusicViewModel) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playbackPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val overview by viewModel.listeningOverview.collectAsState()
    val isExpandedOpen by viewModel.isExpandedPlayerOpen.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        songs = songs,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        onSongSelected = { song, playlist ->
                            viewModel.playSong(song, playlist)
                        },
                        onRefresh = { viewModel.loadLocalMusic() },
                        onOpenDashboard = { viewModel.navigateTo(AppScreen.STATS_DASHBOARD) },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                        onShuffleAll = {
                            if (songs.isNotEmpty()) {
                                val shuffled = songs.shuffled()
                                viewModel.playSong(shuffled.first(), shuffled)
                            }
                        }
                    )
                }
                AppScreen.STATS_DASHBOARD -> {
                    StatsDashboardScreen(
                        overview = overview,
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                    )
                }
            }
        }

        // Anchored Spotify-like Mini Player
        if (currentSong != null) {
            MiniPlayerBar(
                currentSong = currentSong!!,
                isPlaying = isPlaying,
                progress = progress,
                onBarClick = { viewModel.setExpandedPlayerOpen(true) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onToggleFavorite = { viewModel.toggleFavorite(currentSong!!) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Fullscreen Expanded Player Sheet with Visualizer, Scrubber & Cybernetic Controls
        if (isExpandedOpen && currentSong != null) {
            ExpandedPlayerSheet(
                song = currentSong!!,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onDismiss = { viewModel.setExpandedPlayerOpen(false) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onSeekTo = { pos -> viewModel.seekTo(pos) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleFavorite = { viewModel.toggleFavorite(currentSong!!) }
            )
        }
    }
}
