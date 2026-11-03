package com.hughdevelopment.player.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.settings.SortOrder
import com.hughdevelopment.player.ui.components.ArtworkImage
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.components.HermesDialogueSheet
import com.hughdevelopment.player.ui.components.HermesPetBanner
import com.hughdevelopment.player.ui.components.activePlayingGlassCard
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.WireframeBorder

@Composable
fun LibraryScreen(
    songs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    isLoading: Boolean,
    sortOrder: SortOrder,
    wallpaperAlpha: Float,
    playlists: List<Playlist> = emptyList(),
    overview: ListeningOverview = ListeningOverview(0, 0, 0, 0, emptyList()),
    onSongSelected: (Song, List<Song>) -> Unit,
    onRefresh: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddSongToPlaylist: (Long, Long) -> Unit = { _, _ -> },
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit = { _, _ -> },
    onShuffleAll: () -> Unit,
    onSelectSortOrder: (SortOrder) -> Unit,
    onAutoOrganize: () -> Unit = {},
    onPlayFavorites: () -> Unit = {},
    onPlayNext: (List<Song>) -> Unit = {},
    onAddToQueue: (List<Song>) -> Unit = {},
    onHideSong: (Song) -> Unit = {},
    onHideSongs: (List<Song>) -> Unit = {},
    onDeleteSongs: (List<Long>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme = LocalVaultTheme.current

    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var songsToAddToPlaylistMulti by remember { mutableStateOf<List<Song>?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<List<Long>?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var showHermesSheet by remember { mutableStateOf(false) }
    val sortScrollState = rememberScrollState()

    // Multi-Selection Mode State (R3)
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedSongIds by remember { mutableStateOf(setOf<Long>()) }

    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) songs
        else {
            val q = searchQuery.trim().lowercase()
            songs.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        // High-contrast atmospheric backdrop
        Image(
            painter = painterResource(id = R.drawable.bg_library),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(wallpaperAlpha)
        )

        // Procedural wireframe overlay
        CybersigilBackground(showHalo = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (isMultiSelectMode) {
                // Multi-Selection Header Command Bar (R3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentTheme.cardBackground)
                        .border(1.dp, currentTheme.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isMultiSelectMode = false
                            selectedSongIds = emptySet()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Seleção",
                            tint = currentTheme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "${selectedSongIds.size} SELEC.",
                        color = currentTheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            selectedSongIds = if (selectedSongIds.size == filteredSongs.size) emptySet()
                            else filteredSongs.map { it.id }.toSet()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (selectedSongIds.size == filteredSongs.size) "LIMPAR" else "TODAS",
                            color = currentTheme.primaryAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val selectedSongsList = filteredSongs.filter { selectedSongIds.contains(it.id) }

                    // Play Next (Tocar a seguir)
                    IconButton(
                        onClick = {
                            if (selectedSongsList.isNotEmpty()) {
                                onPlayNext(selectedSongsList)
                                Toast.makeText(context, "${selectedSongsList.size} faixas na vez para tocar a seguir", Toast.LENGTH_SHORT).show()
                                isMultiSelectMode = false
                                selectedSongIds = emptySet()
                            }
                        },
                        enabled = selectedSongsList.isNotEmpty(),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = "Tocar a Seguir",
                            tint = if (selectedSongsList.isNotEmpty()) currentTheme.primary else MutedStone,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Add to Queue (Adicionar à fila)
                    IconButton(
                        onClick = {
                            if (selectedSongsList.isNotEmpty()) {
                                onAddToQueue(selectedSongsList)
                                Toast.makeText(context, "${selectedSongsList.size} faixas adicionadas à fila", Toast.LENGTH_SHORT).show()
                                isMultiSelectMode = false
                                selectedSongIds = emptySet()
                            }
                        },
                        enabled = selectedSongsList.isNotEmpty(),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Adicionar à Fila",
                            tint = if (selectedSongsList.isNotEmpty()) currentTheme.primary else MutedStone,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Add to Folder / Playlist
                    IconButton(
                        onClick = {
                            if (selectedSongsList.isNotEmpty()) {
                                songsToAddToPlaylistMulti = selectedSongsList
                            }
                        },
                        enabled = selectedSongsList.isNotEmpty(),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Adicionar à Pasta",
                            tint = if (selectedSongsList.isNotEmpty()) currentTheme.primaryAccent else MutedStone,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Hide tracks (Esconder faixas)
                    IconButton(
                        onClick = {
                            if (selectedSongsList.isNotEmpty()) {
                                onHideSongs(selectedSongsList)
                                Toast.makeText(context, "${selectedSongsList.size} faixas ocultadas da biblioteca", Toast.LENGTH_SHORT).show()
                                isMultiSelectMode = false
                                selectedSongIds = emptySet()
                            }
                        },
                        enabled = selectedSongsList.isNotEmpty(),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Esconder faixas",
                            tint = if (selectedSongsList.isNotEmpty()) currentTheme.primary else MutedStone,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Delete tracks (Excluir faixas)
                    IconButton(
                        onClick = {
                            if (selectedSongsList.isNotEmpty()) {
                                showDeleteConfirmDialog = selectedSongsList.map { it.id }
                            }
                        },
                        enabled = selectedSongsList.isNotEmpty(),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = if (selectedSongsList.isNotEmpty()) currentTheme.destructiveAlert else MutedStone,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // App Brand Standard Header in Cybersigilism
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTheme.lexicon.brandHeader,
                            color = currentTheme.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "${currentTheme.lexicon.brandSub} • ${songs.size} TRACKS",
                            color = currentTheme.primaryAccent,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Multi-select toggle
                    IconButton(onClick = { isMultiSelectMode = true }) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Modo Multi-Seleção",
                            tint = currentTheme.primaryAccent
                        )
                    }

                    // Shuffle All action
                    IconButton(onClick = onShuffleAll) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Tocar Todas no Shuffle",
                            tint = SigilSilver
                        )
                    }

                    // Folders & Playlists Action
                    IconButton(onClick = onOpenFolders) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Pastas & Playlists",
                            tint = currentTheme.primaryAccent
                        )
                    }

                    // Stats Dashboard Action
                    IconButton(onClick = onOpenDashboard) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Métricas",
                            tint = currentTheme.primary
                        )
                    }

                    // Settings Action
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = SigilWhite
                        )
                    }

                    // Rescan Storage Action
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar",
                            tint = SigilSilver
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hermes Pet AI Companion Banner
            HermesPetBanner(
                overview = overview,
                songsCount = songs.size,
                playlistsCount = playlists.size,
                onOpenChat = { showHermesSheet = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Cyber Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "BUSCAR NA BIBLIOTECA...",
                        color = MutedStone,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MutedStone
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentTheme.primary,
                    unfocusedBorderColor = WireframeBorder,
                    focusedTextColor = SigilWhite,
                    unfocusedTextColor = SigilSilver,
                    focusedContainerColor = currentTheme.cardBackground,
                    unfocusedContainerColor = currentTheme.cardBackground
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Filter Chips Row (Recent, Size, Most Played, Favorites, A-Z)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(sortScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SortChip(
                    label = "A-Z",
                    isSelected = sortOrder == SortOrder.DEFAULT,
                    onClick = { onSelectSortOrder(SortOrder.DEFAULT) }
                )
                SortChip(
                    label = "⚡ RECENTES",
                    isSelected = sortOrder == SortOrder.DATE_ADDED_DESC,
                    onClick = { onSelectSortOrder(SortOrder.DATE_ADDED_DESC) }
                )
                SortChip(
                    label = "💾 MAIORES (MB)",
                    isSelected = sortOrder == SortOrder.SIZE_DESC,
                    onClick = { onSelectSortOrder(SortOrder.SIZE_DESC) }
                )
                SortChip(
                    label = "🎧 MAIS OUVIDAS",
                    isSelected = sortOrder == SortOrder.MOST_PLAYED,
                    onClick = { onSelectSortOrder(SortOrder.MOST_PLAYED) }
                )
                SortChip(
                    label = "⭐ FAVORITAS",
                    isSelected = sortOrder == SortOrder.FAVORITES_FIRST,
                    onClick = { onSelectSortOrder(SortOrder.FAVORITES_FIRST) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Songs List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = currentTheme.primary)
                }
            } else if (filteredSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "NENHUMA FAIXA ENCONTRADA" else "NENHUM ÁUDIO LOCAL NO DISCO",
                        color = MutedStone,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp) // Space for mini-player
                ) {
                    itemsIndexed(
                        items = filteredSongs,
                        key = { _, song -> song.id },
                        contentType = { _, _ -> "song_row" }
                    ) { _, song ->
                        val isPlayingSong = song.id == currentPlayingSong?.id
                        val isSelectedInMulti = selectedSongIds.contains(song.id)

                        SongRowItem(
                            song = song,
                            isSelected = isPlayingSong,
                            isPlaying = isPlayingSong && isPlaying,
                            isMultiSelectMode = isMultiSelectMode,
                            isSelectedInMulti = isSelectedInMulti,
                            onClick = {
                                if (isMultiSelectMode) {
                                    selectedSongIds = if (isSelectedInMulti) {
                                        selectedSongIds - song.id
                                    } else {
                                        selectedSongIds + song.id
                                    }
                                } else {
                                    onSongSelected(song, filteredSongs)
                                }
                            },
                            onLongClick = {
                                if (!isMultiSelectMode) {
                                    isMultiSelectMode = true
                                    selectedSongIds = setOf(song.id)
                                }
                            },
                            onToggleFavorite = { onToggleFavorite(song) },
                            onAddToPlaylist = { songToAddToPlaylist = song },
                            onPlayNext = {
                                onPlayNext(listOf(song))
                                Toast.makeText(context, "'${song.title}' será tocada a seguir", Toast.LENGTH_SHORT).show()
                            },
                            onAddToQueue = {
                                onAddToQueue(listOf(song))
                                Toast.makeText(context, "'${song.title}' adicionada à fila", Toast.LENGTH_SHORT).show()
                            },
                            onHide = {
                                onHideSong(song)
                                Toast.makeText(context, "'${song.title}' ocultada da biblioteca", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                showDeleteConfirmDialog = listOf(song.id)
                            }
                        )
                    }
                }
            }
        }

        // Dialog: Quick Add Single Song to Folder
        if (songToAddToPlaylist != null) {
            val targetSong = songToAddToPlaylist!!
            AddToPlaylistDialog(
                playlists = playlists,
                title = "ADICIONAR À PASTA",
                subtitle = targetSong.title,
                onDismiss = { songToAddToPlaylist = null },
                onSelectPlaylist = { pl ->
                    onAddSongToPlaylist(pl.id, targetSong.id)
                    songToAddToPlaylist = null
                    Toast.makeText(context, "Música adicionada à pasta '${pl.name}'", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Dialog: Add Multi-Selected Songs to Folder
        if (songsToAddToPlaylistMulti != null) {
            val targetSongs = songsToAddToPlaylistMulti!!
            AddToPlaylistDialog(
                playlists = playlists,
                title = "ADICIONAR ${targetSongs.size} FAIXAS À PASTA",
                subtitle = "${targetSongs.size} faixas selecionadas",
                onDismiss = { songsToAddToPlaylistMulti = null },
                onSelectPlaylist = { pl ->
                    onAddSongsToPlaylist(pl.id, targetSongs.map { it.id })
                    songsToAddToPlaylistMulti = null
                    isMultiSelectMode = false
                    selectedSongIds = emptySet()
                    Toast.makeText(context, "${targetSongs.size} músicas adicionadas à pasta '${pl.name}'", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Dialog: Confirm Deletion
        if (showDeleteConfirmDialog != null) {
            val idsToDelete = showDeleteConfirmDialog!!
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                containerColor = currentTheme.cardBackground,
                title = {
                    Text(
                        text = "CONFIRMAR EXCLUSÃO",
                        color = currentTheme.destructiveAlert,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                text = {
                    Text(
                        text = "Deseja excluir permanentemente ${idsToDelete.size} faixa(s) do dispositivo?",
                        color = currentTheme.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteSongs(idsToDelete)
                            showDeleteConfirmDialog = null
                            isMultiSelectMode = false
                            selectedSongIds = emptySet()
                            Toast.makeText(context, "Excluindo faixas...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "EXCLUIR", color = currentTheme.destructiveAlert, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text(text = "CANCELAR", color = MutedStone, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // Hermes Pet Dialogue Bottom Sheet
        if (showHermesSheet) {
            HermesDialogueSheet(
                overview = overview,
                songsCount = songs.size,
                playlists = playlists,
                onDismiss = { showHermesSheet = false },
                onAutoOrganize = {
                    onAutoOrganize()
                    showHermesSheet = false
                },
                onPlayFavorites = {
                    onPlayFavorites()
                    showHermesSheet = false
                }
            )
        }
    }
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit
) {
    val theme = LocalVaultTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.cardBackground,
        title = {
            Text(
                text = title,
                color = theme.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column {
                Text(
                    text = subtitle,
                    color = theme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (playlists.isEmpty()) {
                    Text(
                        text = "Nenhuma pasta criada ainda. Abra a aba de Pastas para criar sua primeira pasta.",
                        color = MutedStone,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(playlists.size) { index ->
                            val pl = playlists[index]
                            val plColor = remember(pl.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(pl.colorHex))
                                } catch (_: Exception) {
                                    theme.primary
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.background)
                                    .border(1.dp, plColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { onSelectPlaylist(pl) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = plColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pl.name,
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${pl.category} • ${pl.songCount} faixas",
                                        color = MutedStone,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCELAR", color = MutedStone, fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
private fun SortChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalVaultTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) theme.primary else theme.cardBackground)
            .border(
                width = 1.dp,
                color = if (isSelected) theme.primary else WireframeBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) theme.background else theme.textPrimary,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongRowItem(
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    isMultiSelectMode: Boolean,
    isSelectedInMulti: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit
) {
    val theme = LocalVaultTheme.current
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
                if (isSelected) Modifier.activePlayingGlassCard(cornerRadius = 8.dp)
                else Modifier.neoGlassCard(cornerRadius = 8.dp, backgroundColor = theme.cardBackground, borderWidth = 0.8.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Multi-select Checkbox (R3)
        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelectedInMulti,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = theme.primary,
                    uncheckedColor = MutedStone,
                    checkmarkColor = theme.background
                ),
                modifier = Modifier.padding(end = 6.dp)
            )
        }

        // Thumbnail with Neoclassical statue fallback
        ArtworkImage(
            artworkUri = song.albumArtUri,
            modifier = Modifier.size(44.dp),
            cornerRadius = 6.dp,
            accentColor = if (isSelected) theme.primary else theme.primaryAccent,
            targetDimensionPx = 120
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Titles
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isSelected) theme.primary else theme.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.formattedDuration} • ${song.formattedSize}",
                color = MutedStone,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Animated Equalizer Indicator if Playing
        if (isSelected && isPlaying) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Tocando",
                tint = theme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 4.dp)
            )
        }

        if (!isMultiSelectMode) {
            // Quick Favorite Icon
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoritar",
                    tint = if (song.isFavorite) theme.primary else WireframeBorder,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quick Add to Playlist Icon
            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Adicionar à pasta",
                    tint = SigilSilver,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Context 3-dot Dropdown Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções",
                        tint = SigilSilver,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(theme.cardBackground)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Tocar a Seguir", color = theme.textPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = theme.primary)
                        },
                        onClick = {
                            menuExpanded = false
                            onPlayNext()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Adicionar à Fila", color = theme.textPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = theme.primaryAccent)
                        },
                        onClick = {
                            menuExpanded = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Adicionar à Pasta", color = theme.textPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = theme.primaryAccent)
                        },
                        onClick = {
                            menuExpanded = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Esconder Faixa", color = theme.primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = theme.primary)
                        },
                        onClick = {
                            menuExpanded = false
                            onHide()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Excluir", color = theme.destructiveAlert, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = theme.destructiveAlert)
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
