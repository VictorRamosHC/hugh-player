package com.hughdevelopment.player.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.settings.SortOrder
import com.hughdevelopment.player.ui.components.ArtworkImage
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder

@Composable
fun LibraryScreen(
    songs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    isLoading: Boolean,
    sortOrder: SortOrder,
    wallpaperAlpha: Float,
    onSongSelected: (Song, List<Song>) -> Unit,
    onRefresh: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onShuffleAll: () -> Unit,
    onSelectSortOrder: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val sortScrollState = rememberScrollState()

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
            .background(VoidBlack)
    ) {
        // High-contrast neoclassical atmosphere backdrop with dynamic opacity
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

            // App Brand Header in Cybersigilism
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HUGH // PLAYER",
                        color = SigilWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "OFFLINE VAULT • ${songs.size} TRACKS",
                        color = HermeticCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
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

                // Stats Dashboard Action
                IconButton(onClick = onOpenDashboard) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Métricas",
                        tint = CyberAmber
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
                    focusedBorderColor = CyberAmber,
                    unfocusedBorderColor = WireframeBorder,
                    focusedTextColor = SigilWhite,
                    unfocusedTextColor = SigilSilver,
                    focusedContainerColor = CharcoalSurface,
                    unfocusedContainerColor = CharcoalSurface
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
                    CircularProgressIndicator(color = CyberAmber)
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
                        val isSelected = song.id == currentPlayingSong?.id

                        SongRowItem(
                            song = song,
                            isSelected = isSelected,
                            isPlaying = isSelected && isPlaying,
                            onClick = { onSongSelected(song, filteredSongs) },
                            onToggleFavorite = { onToggleFavorite(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) CyberAmber else CharcoalSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberAmber else WireframeBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) VoidBlack else SigilSilver,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SongRowItem(
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CharcoalSurface else VoidBlack)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberAmber else WireframeBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with reliable Neoclassical Statue fallback and cached failure protection
        ArtworkImage(
            artworkUri = song.albumArtUri,
            modifier = Modifier.size(44.dp),
            cornerRadius = 6.dp,
            accentColor = if (isSelected) CyberAmber else HermeticCyan,
            targetDimensionPx = 120
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Titles
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isSelected) CyberAmber else SigilWhite,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val sizeMb = "%.1f MB".format(song.sizeBytes.toFloat() / (1024f * 1024f))
            Text(
                text = "${song.artist} • ${song.formattedDuration} • $sizeMb",
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
                tint = CyberAmber,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 6.dp)
            )
        }

        // Quick Favorite Icon
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favoritar",
                tint = if (song.isFavorite) CyberAmber else WireframeBorder,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
