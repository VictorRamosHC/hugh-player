package com.hughdevelopment.player.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hughdevelopment.player.data.db.DownloadEntity
import com.hughdevelopment.player.data.download.DownloadLiveState
import com.hughdevelopment.player.data.download.DownloadManager
import com.hughdevelopment.player.data.download.YouTubeExtractorHelper
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.ui.components.SovereignVoidBackdrop
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.components.quartzGlassCapsule
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.DangerRed
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.SigilWhite

enum class DownloadTab {
    IN_PROGRESS,
    COMPLETED
}

@Composable
fun DownloadsScreen(
    downloadManager: DownloadManager,
    allSongs: List<Song>,
    onPlaySong: (Song) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val theme = LocalVaultTheme.current

    val allDownloads by downloadManager.allDownloads.collectAsState(initial = emptyList())
    val liveStates by downloadManager.liveStates.collectAsState()

    var activeTab by remember { mutableStateOf(DownloadTab.IN_PROGRESS) }
    var inputUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Separação em andamento vs concluídas
    val inProgressDownloads = remember(allDownloads, liveStates) {
        allDownloads.filter { it.status == DownloadEntity.STATUS_DOWNLOADING || it.status == DownloadEntity.STATUS_PENDING }
    }
    val completedDownloads = remember(allDownloads) {
        allDownloads.filter { it.status == DownloadEntity.STATUS_COMPLETED }
    }

    // Auto-detecta link do YouTube na área de transferência ao entrar na tela
    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val pastedText = clipData.getItemAt(0)?.text?.toString().orEmpty()
            if (YouTubeExtractorHelper.isYouTubeUrl(pastedText) && inputUrl.isBlank()) {
                inputUrl = pastedText
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Fundo Divino Obsidian
        SovereignVoidBackdrop(
            showCybersigil = true,
            auraScale = 0.9f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Top Bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .quartzGlassCapsule(cornerRadius = 12.dp)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SigilWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "AETHER // INGESTÃO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = HermeticCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "DOWNLOAD DIRETO DO YOUTUBE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = SigilWhite.copy(alpha = 0.6f)
                        )
                    }
                }

                // Ícone de status
                Box(
                    modifier = Modifier
                        .quartzGlassCapsule(cornerRadius = 10.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (inProgressDownloads.isNotEmpty()) HermeticCyan else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (inProgressDownloads.isNotEmpty()) "${inProgressDownloads.size} ATIVO(S)" else "ONLINE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SigilWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card de Inserção de Link / Colar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoGlassCard(
                        cornerRadius = 14.dp,
                        backgroundColor = Color(0xD90B0B14),
                        borderWidth = 1.dp,
                        topRimColor = HermeticCyan.copy(alpha = 0.35f)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            textStyle = TextStyle(
                                color = SigilWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(HermeticCyan),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (inputUrl.isNotBlank()) {
                                        isSubmitting = true
                                        downloadManager.enqueueDownload(
                                            url = inputUrl,
                                            onStarted = {
                                                isSubmitting = false
                                                inputUrl = ""
                                                activeTab = DownloadTab.IN_PROGRESS
                                                Toast.makeText(context, "Download iniciado com sucesso!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isSubmitting = false
                                                Toast.makeText(context, "Erro: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (inputUrl.isBlank()) {
                                    Text(
                                        text = "Cole o link ou ID do YouTube...",
                                        color = SigilWhite.copy(alpha = 0.35f),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (inputUrl.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Limpar",
                                tint = SigilWhite.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { inputUrl = "" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Botão Colar se o campo estiver vazio
                        if (inputUrl.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .quartzGlassCapsule(cornerRadius = 10.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipData = clipboard.primaryClip
                                        if (clipData != null && clipData.itemCount > 0) {
                                            val text = clipData.getItemAt(0)?.text?.toString().orEmpty()
                                            inputUrl = text
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = "Colar",
                                        tint = HermeticCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COLAR",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = HermeticCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botão Iniciar Download
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .quartzGlassCapsule(
                                cornerRadius = 10.dp,
                                backgroundColor = if (inputUrl.isNotBlank()) HermeticCyan.copy(alpha = 0.25f) else Color(0x1AFFFFFF),
                                accentColor = if (inputUrl.isNotBlank()) HermeticCyan else Color.White.copy(alpha = 0.2f)
                            )
                            .clickable(enabled = inputUrl.isNotBlank() && !isSubmitting) {
                                focusManager.clearFocus()
                                isSubmitting = true
                                downloadManager.enqueueDownload(
                                    url = inputUrl,
                                    onStarted = {
                                        isSubmitting = false
                                        inputUrl = ""
                                        activeTab = DownloadTab.IN_PROGRESS
                                        Toast.makeText(context, "Download iniciado no cofre!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isSubmitting = false
                                        Toast.makeText(context, "Erro: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = HermeticCyan,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudDownload,
                                    contentDescription = null,
                                    tint = if (inputUrl.isNotBlank()) HermeticCyan else SigilWhite.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "INICIAR INGESTÃO // BAIXAR",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (inputUrl.isNotBlank()) SigilWhite else SigilWhite.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Abas de Navegação (EM ANDAMENTO / CONCLUÍDAS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Aba Em Andamento
                val isSelectedProgress = activeTab == DownloadTab.IN_PROGRESS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = if (isSelectedProgress) HermeticCyan.copy(alpha = 0.20f) else Color(0x10FFFFFF),
                            accentColor = if (isSelectedProgress) HermeticCyan else Color.Transparent
                        )
                        .clickable { activeTab = DownloadTab.IN_PROGRESS },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EM ANDAMENTO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelectedProgress) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelectedProgress) SigilWhite else SigilWhite.copy(alpha = 0.6f)
                        )
                        if (inProgressDownloads.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(HermeticCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = inProgressDownloads.size.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // Aba Concluídas
                val isSelectedCompleted = activeTab == DownloadTab.COMPLETED
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = if (isSelectedCompleted) CyberAmber.copy(alpha = 0.20f) else Color(0x10FFFFFF),
                            accentColor = if (isSelectedCompleted) CyberAmber else Color.Transparent
                        )
                        .clickable { activeTab = DownloadTab.COMPLETED },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CONCLUÍDAS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelectedCompleted) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelectedCompleted) SigilWhite else SigilWhite.copy(alpha = 0.6f)
                        )
                        if (completedDownloads.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(CyberAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = completedDownloads.size.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Conteúdo da Aba Selecionada
            when (activeTab) {
                DownloadTab.IN_PROGRESS -> {
                    if (inProgressDownloads.isEmpty()) {
                        EmptyDownloadView(
                            title = "NENHUM DOWNLOAD ATIVO",
                            subtitle = "Cole um link do YouTube acima ou compartilhe direto do app do YouTube."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
                        ) {
                            items(inProgressDownloads, key = { it.videoId }) { entity ->
                                val liveState = liveStates[entity.videoId]
                                DownloadProgressCard(
                                    entity = entity,
                                    liveState = liveState,
                                    onCancel = { downloadManager.cancelDownload(entity.videoId) }
                                )
                            }
                        }
                    }
                }

                DownloadTab.COMPLETED -> {
                    if (completedDownloads.isEmpty()) {
                        EmptyDownloadView(
                            title = "COFRE DE DOWNLOADS VAZIO",
                            subtitle = "As faixas concluídas aparecerão aqui e na sua Biblioteca principal automaticamente."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
                        ) {
                            items(completedDownloads, key = { it.videoId }) { entity ->
                                DownloadCompletedCard(
                                    entity = entity,
                                    allSongs = allSongs,
                                    onPlay = onPlaySong,
                                    onDelete = { downloadManager.deleteDownload(entity.videoId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card vivo de download com telemetria: %, velocidade e ETA
 */
@Composable
fun DownloadProgressCard(
    entity: DownloadEntity,
    liveState: DownloadLiveState?,
    onCancel: () -> Unit
) {
    val percent = liveState?.progressPercent ?: entity.progressPercent
    val speed = liveState?.speedFormatted ?: "CONECTANDO..."
    val eta = liveState?.etaFormatted ?: "--:--"
    val bytesDownloaded = liveState?.bytesDownloaded ?: entity.bytesDownloaded
    val totalBytes = liveState?.totalBytes ?: entity.totalBytes

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = Color(0xCC0E0E18),
                borderWidth = 1.dp,
                topRimColor = HermeticCyan.copy(alpha = 0.5f)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                if (!entity.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = entity.thumbnailUrl,
                        contentDescription = entity.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF181828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = HermeticCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entity.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SigilWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${entity.artist} • ${entity.format.uppercase()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SigilWhite.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botão Cancelar
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "Cancelar Download",
                    tint = SigilWhite.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onCancel)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de Progresso Neon
            val progressFraction = (percent / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF161622))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(HermeticCyan, CyberAmber)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linha de Telemetria Detalhada: Porcentagem, Velocidade e ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // % e Bytes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$percent%",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = HermeticCyan
                    )
                    if (totalBytes > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${DownloadManager.formatBytes(bytesDownloaded)} / ${DownloadManager.formatBytes(totalBytes)})",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = SigilWhite.copy(alpha = 0.5f)
                        )
                    }
                }

                // Velocidade
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = speed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = CyberAmber
                    )
                }

                // ETA Restante
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = SigilWhite.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ETA $eta",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = SigilWhite.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

/**
 * Card de música já concluída com botão de play imediato
 */
@Composable
fun DownloadCompletedCard(
    entity: DownloadEntity,
    allSongs: List<Song>,
    onPlay: (Song) -> Unit,
    onDelete: () -> Unit
) {
    // Procura a música na biblioteca carregada para tocar
    val matchingSong = remember(allSongs, entity) {
        allSongs.find { song ->
            song.title.contains(entity.title, ignoreCase = true) ||
            entity.title.contains(song.title, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = Color(0xCC0E0E18),
                borderWidth = 1.dp,
                topRimColor = CyberAmber.copy(alpha = 0.35f)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!entity.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = entity.thumbnailUrl,
                    contentDescription = entity.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF181828)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = SigilWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entity.artist} • ${DownloadManager.formatBytes(entity.totalBytes)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SigilWhite.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botão Play se encontrar a música no MediaStore
            if (matchingSong != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = CyberAmber.copy(alpha = 0.2f),
                            accentColor = CyberAmber
                        )
                        .clickable { onPlay(matchingSong) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "Tocar",
                        tint = CyberAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Botão Excluir
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Excluir",
                tint = SigilWhite.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDelete)
            )
        }
    }
}

@Composable
fun EmptyDownloadView(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .quartzGlassCapsule(cornerRadius = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = null,
                    tint = HermeticCyan.copy(alpha = 0.8f),
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SigilWhite,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SigilWhite.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
