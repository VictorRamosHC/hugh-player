package com.hughdevelopment.player.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.lyrics.LrcLine
import com.hughdevelopment.player.lyrics.LrcParser
import com.hughdevelopment.player.lyrics.LyricsRepository
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
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
    wallpaperAlpha: Float = 0.35f,
    isFadeActive: Boolean = true,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
    waveformPoints: List<Float> = emptyList()
) {
    val theme = LocalVaultTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local drag state to eliminate seeking stutter/lag completely
    var draggingProgress by remember { mutableStateOf<Float?>(null) }

    val effectiveProgress = draggingProgress ?: if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayedPositionMs = draggingProgress?.let { (it * durationMs).toLong() } ?: currentPositionMs

    // Lyrics State & Discovery
    val lyricsRepository = remember { LyricsRepository.default }
    var showLyrics by remember { mutableStateOf(false) }
    var lyrics by remember(song.id, song.dataPath) { mutableStateOf<List<LrcLine>?>(null) }
    var hasLoadedLyrics by remember(song.id, song.dataPath) { mutableStateOf(false) }

    val auraTransition = rememberInfiniteTransition(label = "AltarAuraTransition")
    val auraScale by auraTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )
    val auraAlpha by auraTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )
    val haloOrbitAngle by auraTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HaloOrbitAngle"
    )

    LaunchedEffect(song.id, song.dataPath) {
        hasLoadedLyrics = false
        lyrics = lyricsRepository.getLyricsForSong(song)
        hasLoadedLyrics = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.background)
        ) {
            // 1. Luminous Ambient Aura (Top-Left primary radiant cloud)
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .offset(x = (-80).dp, y = (-80).dp)
                    .scale(auraScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                theme.primaryAccent.copy(alpha = auraAlpha * 0.40f),
                                theme.accentGlow.copy(alpha = auraAlpha * 0.20f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // 2. Secondary Luminous Ambient Aura (Mid-Right secondary radiant cloud)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 60.dp, y = 70.dp)
                    .scale(auraScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                theme.secondaryAccent.copy(alpha = auraAlpha * 0.30f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // 3. Cybernetic Precision Corner & Astrolabe Sigils
            CybersigilBackground(showHalo = false)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
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
                            .border(1.dp, theme.secondaryAccent.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .background(CharcoalSurface)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isFadeActive) "AETHER // PANTHEON SACRED • FADE" else "AETHER // PANTHEON SACRED",
                            color = theme.secondaryAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Open DSP Equalizer
                        IconButton(onClick = onOpenEqualizer) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Equalizador DSP",
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Open Spotify Queue
                        IconButton(onClick = onOpenQueue) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Fila de Reprodução",
                                tint = theme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Toggle between Artwork view and Synced Lyrics view
                        IconButton(onClick = { showLyrics = !showLyrics }) {
                            Icon(
                                imageVector = if (showLyrics) Icons.Default.MusicNote else Icons.AutoMirrored.Filled.Article,
                                contentDescription = if (showLyrics) "Ver Capa" else "Ver Letras",
                                tint = if (showLyrics) CyberAmber else if (lyrics != null) HermeticCyan else SigilSilver,
                                modifier = Modifier.size(26.dp)
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

                        IconButton(onClick = onAddToPlaylist) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "Adicionar à pasta",
                                tint = SigilSilver,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Central Visual Container: Seamless transition between Artwork & Synced Lyrics
                AnimatedContent(
                    targetState = showLyrics,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(280))
                    },
                    label = "PlayerArtworkLyricsSwitch"
                ) { isLyricsView ->
                    if (!isLyricsView) {
                        // Divine Celestial Altar Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. Ambient Pulsating Halo (Radial Gradient Aura)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(auraScale)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                theme.accentGlow.copy(alpha = auraAlpha),
                                                theme.primaryAccent.copy(alpha = auraAlpha * 0.45f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )

                            // 2. Rotating Celestial Astrolabe Ring Behind Cover
                            Canvas(modifier = Modifier.fillMaxSize(1.08f)) {
                                val w = size.width
                                val h = size.height
                                val center = Offset(w / 2f, h / 2f)
                                val outerR = w * 0.47f

                                rotate(degrees = if (isPlaying) haloOrbitAngle else 0f, pivot = center) {
                                    drawCircle(
                                        color = theme.primaryAccent.copy(alpha = 0.35f),
                                        radius = outerR,
                                        center = center,
                                        style = Stroke(
                                            width = 1.2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f), 0f)
                                        )
                                    )
                                }
                            }

                            // 3. Central Artwork Card with Titanium Chamfered Bezel
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.86f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                theme.primaryAccent,
                                                SpecularGlassRimTop,
                                                WireframeBorder.copy(alpha = 0.5f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .background(CharcoalSurface)
                                    .clickable { showLyrics = true }
                            ) {
                                ArtworkImage(
                                    artworkUri = song.albumArtUri,
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 20.dp,
                                    accentColor = theme.secondaryAccent,
                                    targetDimensionPx = 480
                                )

                                // Overlay Micro-HUD Badges
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VoidBlack.copy(alpha = 0.78f))
                                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                ) {
                                    Text(
                                        text = "FLAC 96kHz / 24-BIT",
                                        color = SigilSilver,
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VoidBlack.copy(alpha = 0.78f))
                                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                ) {
                                    Text(
                                        text = "SOVEREIGN SACRED",
                                        color = theme.secondaryAccent,
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Synced Lyrics Available Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            1.dp,
                                            if (lyrics != null) theme.primaryAccent.copy(alpha = 0.7f) else WireframeBorder,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .background(VoidBlack.copy(alpha = 0.85f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (lyrics != null) "LRC • SYNC" else "LRC",
                                        color = if (lyrics != null) theme.primaryAccent else MutedStone,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Synchronized Lyrics Container with Neoclassical Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, CyberAmber.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                                .background(CharcoalSurface)
                        ) {
                            val currentLyrics = lyrics
                            if (hasLoadedLyrics && currentLyrics.isNullOrEmpty()) {
                                // Neoclassical Empty State (R2)
                                EmptyLyricsState(onDismiss = { showLyrics = false })
                            } else if (!currentLyrics.isNullOrEmpty()) {
                                // Real-Time Synchronized Lyrics View (R2)
                                SynchronizedLyricsList(
                                    lines = currentLyrics,
                                    currentPositionMs = currentPositionMs,
                                    onSeekTo = onSeekTo,
                                    onDismiss = { showLyrics = false }
                                )
                            } else {
                                // Subtle loading glyph
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    StatueGlyphView(
                                        modifier = Modifier.size(90.dp),
                                        accentColor = CyberAmber
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Song Title & Artist
                Text(
                    text = song.title,
                    color = SigilWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
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

                // Real-time Procedural Audio Visualizer Spectrum Bars (Theme-Adaptive)
                AudioVisualizerBars(
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(horizontal = 8.dp),
                    barCount = 30,
                    height = 32.dp,
                    activeColor = theme.primary,
                    secondaryColor = theme.primaryAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time 42-Band Waveform Scrubber with interactive tactile Seeking
                WaveformScrubber(
                    points = waveformPoints,
                    currentPositionMs = displayedPositionMs,
                    durationMs = durationMs,
                    onSeekTo = onSeekTo,
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .padding(horizontal = 4.dp)
                )

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
                            tint = if (isShuffleEnabled) theme.primary else MutedStone,
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

                    // Play/Pause Core Button with Radiant Cyber Rim
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(theme.primary)
                            .border(2.dp, SpecularGlassRimTop, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                                tint = SigilWhite,
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
                        val tint = if (repeatMode != Player.REPEAT_MODE_OFF) theme.primary else MutedStone
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

/**
 * Real-time synchronized scrolling lyrics list with active line highlight and tap-to-seek.
 */
@Composable
private fun SynchronizedLyricsList(
    lines: List<LrcLine>,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()

    // Real-time active line index
    val activeLineIndex = remember(lines, currentPositionMs) {
        LrcParser.getActiveLineIndex(lines, currentPositionMs)
    }

    // Auto-scroll effect: smoothly scroll to active item
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex >= 0 && !listState.isScrollInProgress) {
            listState.animateScrollToItem(
                index = activeLineIndex,
                scrollOffset = 0
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 110.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(lines) { index, line ->
                val isActive = (index == activeLineIndex)
                SynchronizedLyricItem(
                    line = line,
                    isActive = isActive,
                    onClick = { onSeekTo(line.timeMs) }
                )
            }
        }

        // Top soft edge gradient
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CharcoalSurface, Color.Transparent)
                    )
                )
        )

        // Bottom soft edge gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(32.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, CharcoalSurface)
                    )
                )
        )

        // Bottom HUD Coordinate / Return Tag
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(VoidBlack.copy(alpha = 0.85f))
                .border(1.dp, CyberAmber.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .clickable { onDismiss() }
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LRC SYNC // TOQUE PARA SALTAR • VER CAPA",
                color = CyberAmber,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Individual lyric stanza item with high-contrast active highlight and animated alpha/scale.
 */
@Composable
private fun SynchronizedLyricItem(
    line: LrcLine,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1.0f else 0.40f,
        animationSpec = tween(durationMillis = 200),
        label = "LyricAlpha"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "LyricScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isActive) {
                    Modifier
                        .background(VoidBlack.copy(alpha = 0.70f))
                        .border(1.dp, CyberAmber.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = line.text.ifEmpty { "♪" },
            color = if (isActive) CyberAmber else SigilWhite,
            fontSize = if (isActive) 17.sp else 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            lineHeight = if (isActive) 22.sp else 18.sp,
            modifier = Modifier
                .alpha(animatedAlpha)
                .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
        )
    }
}

/**
 * Neoclassical art empty state when no local .LRC file is found.
 */
@Composable
private fun EmptyLyricsState(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StatueGlyphView(
            modifier = Modifier.size(115.dp),
            accentColor = CyberAmber
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Letras locais não encontradas",
            color = SigilWhite,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Adicione um arquivo .lrc na mesma pasta do áudio ou no diretório Music/Lyrics.",
            color = MutedStone,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, WireframeBorder, RoundedCornerShape(4.dp))
                .background(VoidBlack.copy(alpha = 0.7f))
                .clickable { onDismiss() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "VOLTAR PARA A CAPA",
                color = HermeticCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
