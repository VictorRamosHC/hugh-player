package com.hughdevelopment.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.components.StatueGlyphView
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
    onSongSelected: (Song, List<Song>) -> Unit,
    onRefresh: () -> Unit,
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

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

    Box(modifier = modifier.fillMaxSize().background(VoidBlack)) {
        CybersigilBackground()

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

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

                // Stats Dashboard Action
                IconButton(onClick = onOpenDashboard) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Métricas",
                        tint = CyberAmber
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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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
                    contentPadding = PaddingValues(bottom = 96.dp) // Room for mini-player
                ) {
                    itemsIndexed(
                        items = filteredSongs,
                        key = { _, song -> song.id }
                    ) { _, song ->
                        val isSelected = song.id == currentPlayingSong?.id

                        SongRowItem(
                            song = song,
                            isSelected = isSelected,
                            isPlaying = isSelected && isPlaying,
                            onClick = { onSongSelected(song, filteredSongs) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRowItem(
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CharcoalSurface else VoidBlack)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberAmber else WireframeBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or Statue Glyph
        if (song.albumArtUri != null) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            StatueGlyphView(
                modifier = Modifier.size(44.dp),
                accentColor = if (isSelected) CyberAmber else HermeticCyan
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Titles
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isSelected) CyberAmber else SigilWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.formattedDuration}",
                color = MutedStone,
                fontSize = 12.sp,
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
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
