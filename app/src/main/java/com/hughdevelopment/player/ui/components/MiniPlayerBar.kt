package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.WireframeBorder

@Composable
fun MiniPlayerBar(
    currentSong: Song,
    isPlaying: Boolean,
    progress: Float,
    onBarClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalSurface)
            .border(1.2.dp, WireframeBorder, RoundedCornerShape(12.dp))
            .clickable { onBarClick() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reliable Artwork with Neoclassical Statue fallback
                ArtworkImage(
                    artworkUri = currentSong.albumArtUri,
                    modifier = Modifier.size(46.dp),
                    cornerRadius = 8.dp,
                    accentColor = CyberAmber
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Track Title and Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        color = SigilWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        color = MutedStone,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Toggle Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (currentSong.isFavorite) CyberAmber else MutedStone,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Skip Previous
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = SigilSilver,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play / Pause with Fade
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        tint = SigilWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Skip Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima Música",
                        tint = SigilSilver,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // High-precision Cyber Progress Line
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = CyberAmber,
                trackColor = WireframeBorder.copy(alpha = 0.5f)
            )
        }
    }
}
