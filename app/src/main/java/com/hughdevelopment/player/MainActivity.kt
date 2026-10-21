package com.hughdevelopment.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.hughdevelopment.player.ui.components.ExpandedPlayerSheet
import com.hughdevelopment.player.ui.components.MiniPlayerBar
import com.hughdevelopment.player.ui.components.SplashScreenView
import com.hughdevelopment.player.ui.screens.FoldersScreen
import com.hughdevelopment.player.ui.screens.LibraryScreen
import com.hughdevelopment.player.ui.screens.SettingsScreen
import com.hughdevelopment.player.ui.screens.StatsDashboardScreen
import com.hughdevelopment.player.ui.theme.HughPlayerTheme
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.viewmodel.AppScreen
import com.hughdevelopment.player.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()
    private val isPermissionsSettled = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        isPermissionsSettled.value = true

        if (audioGranted) {
            viewModel.loadLocalMusic()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            HughPlayerTheme {
                MainScreen(
                    viewModel = viewModel,
                    isPermissionsSettled = isPermissionsSettled.value,
                    onExportAiManifest = { callback ->
                        viewModel.exportAiManifest(this@MainActivity, callback)
                    }
                )
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
            isPermissionsSettled.value = true
            viewModel.loadLocalMusic()
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    isPermissionsSettled: Boolean,
    onExportAiManifest: (((String) -> Unit)) -> Unit
) {
    val sortedSongs by viewModel.sortedSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
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
    val settings by viewModel.settings.collectAsState()

    var showSplash by remember { mutableStateOf(true) }

    // Start splash timer only after permissions are settled so user always sees the boot screen
    LaunchedEffect(isPermissionsSettled) {
        if (isPermissionsSettled) {
            delay(2800)
            showSplash = false
        }
    }

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
                        songs = sortedSongs,
                        currentPlayingSong = currentSong,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        sortOrder = settings.sortOrder,
                        wallpaperAlpha = settings.libraryWallpaperAlpha,
                        playlists = playlists,
                        onSongSelected = { song, playlist ->
                            viewModel.playSong(song, playlist)
                        },
                        onRefresh = { viewModel.loadLocalMusic() },
                        onOpenFolders = { viewModel.navigateTo(AppScreen.FOLDERS) },
                        onOpenDashboard = { viewModel.navigateTo(AppScreen.STATS_DASHBOARD) },
                        onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        onToggleFavorite = { song -> viewModel.toggleFavorite(song) },
                        onAddSongToPlaylist = { playlistId, songId ->
                            viewModel.addSongToPlaylist(playlistId, songId)
                        },
                        onShuffleAll = {
                            if (sortedSongs.isNotEmpty()) {
                                val shuffled = sortedSongs.shuffled()
                                viewModel.playSong(shuffled.first(), shuffled)
                            }
                        },
                        onSelectSortOrder = { order ->
                            viewModel.setSortOrder(order)
                        }
                    )
                }
                AppScreen.FOLDERS -> {
                    FoldersScreen(
                        playlists = playlists,
                        allSongs = sortedSongs,
                        wallpaperAlpha = settings.libraryWallpaperAlpha,
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) },
                        onPlayPlaylist = { playlist ->
                            if (playlist.songs.isNotEmpty()) {
                                viewModel.playSong(playlist.songs.first(), playlist.songs)
                            }
                        },
                        onSongSelectedFromPlaylist = { song, playlistSongs ->
                            viewModel.playSong(song, playlistSongs)
                        },
                        onCreatePlaylist = { name, category, colorHex ->
                            viewModel.createPlaylist(name, category, colorHex)
                        },
                        onDeletePlaylist = { playlistId ->
                            viewModel.deletePlaylist(playlistId)
                        },
                        onAddSongsToPlaylist = { playlistId, songIds ->
                            viewModel.addSongsToPlaylist(playlistId, songIds)
                        },
                        onRemoveSongFromPlaylist = { playlistId, songId ->
                            viewModel.removeSongFromPlaylist(playlistId, songId)
                        },
                        onAutoOrganizeAi = { callback ->
                            viewModel.autoOrganizeWithAiPresets(callback)
                        }
                    )
                }
                AppScreen.STATS_DASHBOARD -> {
                    StatsDashboardScreen(
                        overview = overview,
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) },
                        onToggleFade = { viewModel.setFadeEnabled(it) },
                        onChangeFadeDuration = { viewModel.setFadeDuration(it) },
                        onChangeLibraryAlpha = { viewModel.setLibraryWallpaperAlpha(it) },
                        onChangePlayerAlpha = { viewModel.setPlayerWallpaperAlpha(it) },
                        onExportAiManifest = onExportAiManifest
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
                wallpaperAlpha = settings.playerWallpaperAlpha,
                isFadeActive = settings.isFadeEnabled,
                onDismiss = { viewModel.setExpandedPlayerOpen(false) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onSeekTo = { pos -> viewModel.seekTo(pos) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleFavorite = { viewModel.toggleFavorite(currentSong!!) },
                onAddToPlaylist = {
                    viewModel.setExpandedPlayerOpen(false)
                    viewModel.navigateTo(AppScreen.FOLDERS)
                }
            )
        }

        // Cinematic Splash Screen overlay with interactive tap and smooth fade out
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(500))
        ) {
            SplashScreenView(onDismiss = { showSplash = false })
        }
    }
}
