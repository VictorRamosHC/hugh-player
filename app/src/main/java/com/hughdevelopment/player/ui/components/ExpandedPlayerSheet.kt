package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayerSheet(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local drag state to eliminate seeking stutter/lag completely
    var draggingProgress by remember { mutableStateOf<Float?>(null) }

    val effectiveProgress = draggingProgress ?: if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayedPositionMs = draggingProgress?.let { (it * durationMs).toLong() } ?: currentPositionMs

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VoidBlack,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            CybersigilBackground(showHalo = true)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Fechar",
                            tint = SigilWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Tech HUD Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, CyberAmber.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .background(CharcoalSurface)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "HUGH // 0x01 CORE • FADE ACTIVE",
                            color = CyberAmber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (song.isFavorite) CyberAmber else SigilSilver,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Album Artwork with Cybernetic Frame & Neoclassical Fallback
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, WireframeBorder, RoundedCornerShape(18.dp))
                        .background(CharcoalSurface)
                ) {
                    ArtworkImage(
                        artworkUri = song.albumArtUri,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 18.dp,
                        accentColor = CyberAmber
                    )

                    // Overlay HUD Coordinate Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(VoidBlack.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFLINE VAULT // LOSSLESS",
                            color = HermeticCyan,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Song Title & Artist
                Text(
                    text = song.title,
                    color = SigilWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${song.artist} • ${song.album}",
                    color = MutedStone,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Procedural Audio Visualizer Spectrum Bars
                AudioVisualizerBars(
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(horizontal = 8.dp),
                    barCount = 30,
                    height = 32.dp,
                    activeColor = CyberAmber,
                    secondaryColor = HermeticCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                // High-precision Cyber Slider (Smooth seeking without playback stutter)
                Slider(
                    value = effectiveProgress,
                    onValueChange = { newFrac ->
                        draggingProgress = newFrac
                    },
                    onValueChangeFinished = {
                        draggingProgress?.let { finalFrac ->
                            onSeekTo((finalFrac * durationMs).toLong())
                            draggingProgress = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = SigilWhite,
                        activeTrackColor = CyberAmber,
                        inactiveTrackColor = WireframeBorder
                    )
                )

                // Time indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(displayedPositionMs),
                        color = HermeticCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatTime(durationMs),
                        color = MutedStone,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Complete Cyber Playback Controls Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatório",
                            tint = if (isShuffleEnabled) CyberAmber else MutedStone,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Skip Previous
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = SigilSilver,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Play/Pause Core Button with Cyber Reactor Ring
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SigilWhite)
                            .border(2.5.dp, CyberAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                                tint = VoidBlack,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Skip Next
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Próxima",
                            tint = SigilSilver,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Repeat Mode Button
                    IconButton(onClick = onToggleRepeat) {
                        val icon = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        val tint = if (repeatMode != Player.REPEAT_MODE_OFF) CyberAmber else MutedStone
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repetir",
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
