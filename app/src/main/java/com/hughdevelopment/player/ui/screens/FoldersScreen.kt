package com.hughdevelopment.player.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.ui.components.ArtworkImage
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.KleinBlue
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder

@Composable
fun FoldersScreen(
    playlists: List<Playlist>,
    allSongs: List<Song>,
    wallpaperAlpha: Float,
    onBack: () -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onSongSelectedFromPlaylist: (Song, List<Song>) -> Unit,
    onCreatePlaylist: (String, String, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onRemoveSongFromPlaylist: (Long, Long) -> Unit,
    onAutoOrganizeAi: ((Map<String, Int>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlaylistForDetail by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddSongsDialog by remember { mutableStateOf(false) }

    // Synchronize active selected playlist when list updates, clearing stale deleted playlists
    val activePlaylist = remember(playlists, selectedPlaylistForDetail) {
        if (selectedPlaylistForDetail == null) null
        else playlists.find { it.id == selectedPlaylistForDetail?.id }
    }

    androidx.compose.runtime.LaunchedEffect(playlists, selectedPlaylistForDetail) {
        if (selectedPlaylistForDetail != null && activePlaylist == null) {
            selectedPlaylistForDetail = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_folders),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(wallpaperAlpha)
        )

        CybersigilBackground(showHalo = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (activePlaylist != null) {
                        selectedPlaylistForDetail = null
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = SigilWhite
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (activePlaylist != null) activePlaylist.name else "AETHER // PASTAS & PLAYLISTS",
                        color = SigilWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (activePlaylist != null) "${activePlaylist.songCount} FAIXAS NESTA PASTA" else "ORGANIZAÇÃO & CURADORIA OFFLINE",
                        color = CyberAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (activePlaylist == null) {
                    // Auto-Organize with AI Button
                    IconButton(onClick = {
                        onAutoOrganizeAi { counts ->
                            val msg = "Classificação IA Concluída!\n" +
                                    "Cristãs (Cantado): ${counts["Músicas Cristãs (Cantado)"] ?: 0}\n" +
                                    "Cristãs (Playback): ${counts["Músicas Cristãs (Playback)"] ?: 0}\n" +
                                    "Dia a Dia: ${counts["Músicas Dia a Dia"] ?: 0}\n" +
                                    "Reuniões: ${counts["Áudios de Reuniões"] ?: 0}"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Organizar com IA",
                            tint = HermeticCyan
                        )
                    }

                    // Create New Folder Button
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Nova Pasta",
                            tint = CyberAmber
                        )
                    }
                } else {
                    // Add Songs to current opened folder
                    IconButton(onClick = { showAddSongsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar Músicas",
                            tint = CyberAmber
                        )
                    }

                    // Play this entire folder
                    IconButton(onClick = { onPlayPlaylist(activePlaylist) }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Tocar Pasta",
                            tint = SigilWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activePlaylist == null) {
                // --- Top Banner for AI Presets ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalSurface)
                        .border(1.dp, WireframeBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESTRUTURA DE PASTAS DA DISCOGRAFIA",
                                color = SigilWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Text(
                                text = "4 CATEGORIAS",
                                color = CyberAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Toque em ⚡ para classificar suas músicas automaticamente em: Cristãs (Cantado), Cristãs (Playback), Músicas Dia a Dia e Áudios de Reuniões.",
                            color = MutedStone,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Playlists List ---
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = WireframeBorder,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "NENHUMA PASTA CRIADA AINDA",
                                color = MutedStone,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    onAutoOrganizeAi {
                                        Toast.makeText(context, "Pastas padrão criadas e organizadas!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberAmber,
                                    contentColor = VoidBlack
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ORGANIZAR EM 4 PASTAS COM IA",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = playlists, key = { it.id }) { playlist ->
                            FolderCardItem(
                                playlist = playlist,
                                onOpen = { selectedPlaylistForDetail = playlist },
                                onPlay = { onPlayPlaylist(playlist) },
                                onDelete = { onDeletePlaylist(playlist.id) }
                            )
                        }
                    }
                }
            } else {
                // --- Folder Detail View (Songs inside this folder) ---
                val bannerCoverRes = remember(activePlaylist.name, activePlaylist.category) {
                    getPlaylistCoverResource(activePlaylist.name, activePlaylist.category)
                }

                if (bannerCoverRes != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, WireframeBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = bannerCoverRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, VoidBlack.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = "${activePlaylist.name.uppercase()} • ${activePlaylist.songs.size} FAIXAS",
                                color = SigilWhite,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (activePlaylist.songs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ESTA PASTA ESTÁ VAZIA",
                                color = MutedStone,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddSongsDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberAmber,
                                    contentColor = VoidBlack
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "INSERIR MÚSICAS NESTA PASTA",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(items = activePlaylist.songs, key = { it.id }) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CharcoalSurface)
                                    .border(1.dp, WireframeBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { onSongSelectedFromPlaylist(song, activePlaylist.songs) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ArtworkImage(
                                    artworkUri = song.albumArtUri,
                                    modifier = Modifier.size(42.dp),
                                    cornerRadius = 6.dp,
                                    accentColor = CyberAmber
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        color = SigilWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${song.artist} • ${song.formattedDuration}",
                                        color = MutedStone,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveSongFromPlaylist(activePlaylist.id, song.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remover da pasta",
                                        tint = MutedStone,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Dialog: Create New Folder ---
        if (showCreateDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, category, colorHex ->
                    onCreatePlaylist(name, category, colorHex)
                    showCreateDialog = false
                    Toast.makeText(context, "Pasta '$name' criada!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // --- Dialog: Add Songs to Active Folder ---
        if (showAddSongsDialog && activePlaylist != null) {
            AddSongsToFolderDialog(
                allSongs = allSongs,
                existingSongIds = activePlaylist.songs.map { it.id }.toSet(),
                onDismiss = { showAddSongsDialog = false },
                onConfirm = { selectedIds ->
                    onAddSongsToPlaylist(activePlaylist.id, selectedIds)
                    showAddSongsDialog = false
                    Toast.makeText(context, "${selectedIds.size} músicas adicionadas!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun FolderCardItem(
    playlist: Playlist,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = remember(playlist.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(playlist.colorHex))
        } catch (e: Exception) {
            CyberAmber
        }
    }

    val coverRes = remember(playlist.name, playlist.category) {
        getPlaylistCoverResource(playlist.name, playlist.category)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 10.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.2.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = accentColor.copy(alpha = 0.5f)
            )
            .clickable { onOpen() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (coverRes != null) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                color = SigilWhite,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = playlist.category,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${playlist.songCount} FAIXAS",
                    color = MutedStone,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Play entire folder
        if (playlist.songCount > 0) {
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Tocar",
                    tint = SigilWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Delete folder
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Excluir Pasta",
                tint = WireframeBorder,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, colorHex: String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("GERAL") }
    var selectedColor by remember { mutableStateOf("#F59E0B") }

    val categories = listOf("CRISTÃS (CANTADO)", "CRISTÃS (PLAYBACK)", "DIA A DIA", "REUNIÕES", "GERAL")
    val colors = listOf(
        "#F59E0B" to "Amber",
        "#06B6D4" to "Cyan",
        "#10B981" to "Emerald",
        "#8B5CF6" to "Purple",
        "#0022FF" to "Blue"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        title = {
            Text(
                text = "NOVA PASTA // PLAYLIST",
                color = SigilWhite,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    placeholder = {
                        Text(
                            text = "Ex: Músicas Cristãs, Playbacks...",
                            color = MutedStone,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberAmber,
                        unfocusedBorderColor = WireframeBorder,
                        focusedTextColor = SigilWhite,
                        unfocusedTextColor = SigilSilver
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CATEGORIA:",
                    color = SigilSilver,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        CategoryChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        CategoryChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "COR DO ACENTO:",
                    color = SigilSilver,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (hex, _) ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == hex) 2.5.dp else 1.dp,
                                    color = if (selectedColor == hex) SigilWhite else WireframeBorder,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VoidBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName.trim(), selectedCategory, selectedColor)
                    }
                },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberAmber,
                    contentColor = VoidBlack
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "CRIAR PASTA",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCELAR",
                    color = MutedStone,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) CyberAmber else VoidBlack)
            .border(1.dp, if (isSelected) CyberAmber else WireframeBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) VoidBlack else SigilSilver,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun AddSongsToFolderDialog(
    allSongs: List<Song>,
    existingSongIds: Set<Long>,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<Long>() }

    val availableSongs = remember(allSongs, existingSongIds) {
        allSongs.filter { !existingSongIds.contains(it.id) }
    }

    val filtered = remember(availableSongs, searchQuery) {
        if (searchQuery.isBlank()) availableSongs
        else {
            val q = searchQuery.trim().lowercase()
            availableSongs.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        title = {
            Text(
                text = "INSERIR MÚSICAS NA PASTA",
                color = SigilWhite,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(modifier = Modifier.height(380.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Filtrar por nome ou artista...",
                            color = MutedStone,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MutedStone
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberAmber,
                        unfocusedBorderColor = WireframeBorder,
                        focusedTextColor = SigilWhite,
                        unfocusedTextColor = SigilSilver
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${filtered.size} disponíveis",
                        color = MutedStone,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${selectedIds.size} selecionadas",
                        color = CyberAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items = filtered, key = { it.id }) { song ->
                        val isChecked = selectedIds.contains(song.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChecked) CyberAmber.copy(alpha = 0.12f) else VoidBlack)
                                .clickable {
                                    if (isChecked) selectedIds.remove(song.id)
                                    else selectedIds.add(song.id)
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { check ->
                                    if (check) selectedIds.add(song.id)
                                    else selectedIds.remove(song.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyberAmber,
                                    uncheckedColor = WireframeBorder,
                                    checkmarkColor = VoidBlack
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = if (isChecked) CyberAmber else SigilWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    color = MutedStone,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberAmber,
                    contentColor = VoidBlack
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "ADICIONAR (${selectedIds.size})",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCELAR",
                    color = MutedStone,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}

/**
 * Resolves the appropriate Brutalist Neoclassical cover for a playlist or folder
 * using normalized accent-insensitive keyword matching. Name takes priority over category,
 * preventing default generic categories (e.g. 'GERAL') from shadowing specific names.
 */
fun getPlaylistCoverResource(name: String, category: String): Int? {
    fun normalize(text: String): String {
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase(java.util.Locale.ROOT)
    }

    val normName = normalize(name)
    val normCat = normalize(category)

    fun match(target: String): Int? {
        return when {
            target.contains("playback") || target.contains("instrumental") ||
                target.contains("play back") || target.contains("play-back") ||
                target.contains("karaoke") || target.contains("sem voz") ||
                target.contains("backing") -> R.drawable.cover_christian_playback

            target.contains("reunia") || target.contains("reunio") ||
                target.contains("meeting") || target.contains("ata") ||
                target.contains("gravad") || target.contains("gravac") ||
                target.contains("voice") || target.contains("voz") ||
                target.contains("conferenc") || target.contains("briefing") -> R.drawable.cover_meetings_vault

            target.contains("crista") || target.contains("cantad") ||
                target.contains("gospel") || target.contains("louvor") ||
                target.contains("hino") || target.contains("adoracao") ||
                target.contains("worship") || target.contains("sacr") ||
                target.contains("evangel") -> R.drawable.cover_christian_vocal

            target.contains("dia a dia") || target.contains("dia-a-dia") ||
                target.contains("cotidian") || target.contains("rotina") ||
                target.contains("diari") || target.contains("daily") ||
                target.contains("favorit") -> R.drawable.cover_daily_music

            else -> null
        }
    }

    // 1. Priority: check explicit playlist name
    val fromName = match(normName)
    if (fromName != null) return fromName

    // 2. Secondary: check category if not generic default ('geral' or 'custom')
    if (normCat != "geral" && normCat != "custom" && normCat.isNotBlank()) {
        val fromCat = match(normCat)
        if (fromCat != null) return fromCat
    }

    // 3. Fallback: if name explicitly contains 'geral' (e.g. "Músicas em Geral")
    if (normName.contains("geral")) {
        return R.drawable.cover_daily_music
    }

    return null
}
