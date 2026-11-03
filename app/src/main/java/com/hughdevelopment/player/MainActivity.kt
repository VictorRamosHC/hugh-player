package com.hughdevelopment.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.hughdevelopment.player.ui.components.EqualizerBottomSheet
import com.hughdevelopment.player.ui.components.ExpandedPlayerSheet
import com.hughdevelopment.player.ui.components.HiddenSongsBottomSheet
import com.hughdevelopment.player.ui.components.MiniPlayerBar
import com.hughdevelopment.player.ui.components.QueueBottomSheet
import com.hughdevelopment.player.ui.components.SplashScreenView
import com.hughdevelopment.player.ui.screens.FoldersScreen
import com.hughdevelopment.player.ui.screens.LibraryScreen
import com.hughdevelopment.player.ui.screens.SettingsScreen
import com.hughdevelopment.player.ui.screens.StatsDashboardScreen
import com.hughdevelopment.player.ui.theme.HughPlayerTheme
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
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
            val settings by viewModel.settings.collectAsState()
            val currentTheme = remember(settings.activeThemeId) {
                com.hughdevelopment.player.ui.theme.VaultThemes.fromId(settings.activeThemeId)
            }

            HughPlayerTheme(themeConfig = currentTheme) {
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
    val context = LocalContext.current
    val currentTheme = LocalVaultTheme.current

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
    val queue by viewModel.queue.collectAsState()
    val hiddenSongs by viewModel.hiddenSongs.collectAsState()
    val waveformPoints by viewModel.currentWaveformPoints.collectAsState()

    // DSP Equalizer States
    val eqEnabled by viewModel.equalizerEnabled.collectAsState()
    val eqPreset by viewModel.equalizerPreset.collectAsState()
    val eqBands by viewModel.equalizerBandLevels.collectAsState()
    val eqBass by viewModel.bassBoostStrength.collectAsState()
    val eqFreqs by viewModel.equalizerCenterFrequencies.collectAsState()

    // Sheet visibility states
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showHiddenSongsSheet by remember { mutableStateOf(false) }

    var showSplash by remember { mutableStateOf(true) }

    // Multi-layered Back Navigation Hierarchy (R11)
    BackHandler(enabled = true) {
        if (showQueueSheet) {
            showQueueSheet = false
        } else if (showEqualizerSheet) {
            showEqualizerSheet = false
        } else if (showHiddenSongsSheet) {
            showHiddenSongsSheet = false
        } else if (isExpandedOpen) {
            viewModel.setExpandedPlayerOpen(false)
        } else if (viewModel.canNavigateBack()) {
            viewModel.navigateBack()
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }

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
            .background(currentTheme.background)
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
                        overview = overview,
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
                        onAddSongsToPlaylist = { playlistId, songIds ->
                            viewModel.addSongsToPlaylist(playlistId, songIds)
                        },
                        onShuffleAll = {
                            if (sortedSongs.isNotEmpty()) {
                                val shuffled = sortedSongs.shuffled()
                                viewModel.playSong(shuffled.first(), shuffled)
                            }
                        },
                        onSelectSortOrder = { order ->
                            viewModel.setSortOrder(order)
                        },
                        onAutoOrganize = {
                            viewModel.autoOrganizeWithAiPresets { counts ->
                                Toast.makeText(
                                    context,
                                    "Hermes organizou: ${counts.values.sum()} faixas em 4 pastas!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        onPlayFavorites = {
                            viewModel.playFavorites()
                        },
                        onPlayNext = { songsToPlayNext ->
                            viewModel.playNext(songsToPlayNext)
                        },
                        onAddToQueue = { songsToQueue ->
                            viewModel.addToQueue(songsToQueue)
                        },
                        onHideSong = { songToHide ->
                            viewModel.hideSong(songToHide)
                        },
                        onHideSongs = { songsToHide ->
                            viewModel.hideSongs(songsToHide)
                        },
                        onDeleteSongs = { songIdsToDelete ->
                            viewModel.deleteSongs(songIdsToDelete) { deletedCount ->
                                Toast.makeText(context, "$deletedCount faixa(s) excluída(s)", Toast.LENGTH_SHORT).show()
                            }
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
                        totalTracks = sortedSongs.size,
                        timeDisplayFormat = settings.timeDisplayFormat,
                        onSpeakBriefing = { viewModel.speakHermesDailyBriefing() },
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
                        onExportAiManifest = onExportAiManifest,
                        onSelectTheme = { viewModel.setThemeId(it) },
                        onExportBackup = { viewModel.exportBackup(it) },
                        onImportBackup = { json, cb -> viewModel.importBackup(json, cb) },
                        onOpenEqualizer = { showEqualizerSheet = true },
                        onOpenHiddenSongs = { showHiddenSongsSheet = true },
                        hiddenSongsCount = hiddenSongs.size,
                        onChangeTimeDisplayFormat = { viewModel.setTimeDisplayFormat(it) }
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
                },
                onOpenQueue = { showQueueSheet = true },
                onOpenEqualizer = { showEqualizerSheet = true },
                waveformPoints = waveformPoints
            )
        }

        // Spotify-style Queue Bottom Sheet (R1)
        if (showQueueSheet) {
            QueueBottomSheet(
                currentSong = currentSong,
                isPlaying = isPlaying,
                queue = queue,
                onDismiss = { showQueueSheet = false },
                onSongSelected = { targetSong ->
                    viewModel.playSong(targetSong, queue)
                },
                onMoveItem = { fromIdx, toIdx ->
                    viewModel.moveQueueItem(fromIdx, toIdx)
                },
                onRemoveItem = { queueIdx ->
                    viewModel.removeFromQueue(queueIdx)
                },
                onClearQueue = {
                    viewModel.clearQueue()
                }
            )
        }

        // Interactive 5-Band Hardware DSP Equalizer Sheet (R10)
        if (showEqualizerSheet) {
            EqualizerBottomSheet(
                isEnabled = eqEnabled,
                currentPreset = eqPreset,
                bandLevels = eqBands,
                bassBoostStrength = eqBass,
                centerFrequencies = eqFreqs,
                onDismiss = { showEqualizerSheet = false },
                onToggleEnabled = { viewModel.setEqualizerEnabled(it) },
                onSelectPreset = { viewModel.setEqualizerPreset(it) },
                onBandLevelChange = { bIdx, lvlMb -> viewModel.setEqualizerBandLevel(bIdx, lvlMb) },
                onBassBoostChange = { viewModel.setBassBoostStrength(it) }
            )
        }

        // Sovereign Hidden Songs Management Sheet (R4)
        if (showHiddenSongsSheet) {
            HiddenSongsBottomSheet(
                hiddenSongs = hiddenSongs,
                onDismiss = { showHiddenSongsSheet = false },
                onUnhideSong = { songId ->
                    viewModel.unhideSong(songId)
                },
                onUnhideAll = {
                    viewModel.unhideAllSongs()
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
