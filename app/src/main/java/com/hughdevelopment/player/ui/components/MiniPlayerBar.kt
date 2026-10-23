package com.hughdevelopment.player.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SpecularGlassRimBottom
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
import com.hughdevelopment.player.ui.theme.WireframeBorder
import kotlinx.coroutines.launch

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
    val theme = LocalVaultTheme.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 72.dp.toPx() }
    val cueActivationPx = with(density) { 12.dp.toPx() }
    val maxAlphaFadePx = with(density) { 400.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    var dragTarget by remember { mutableFloatStateOf(0f) }
    var barWidthPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentSong.id) {
        dragTarget = 0f
        if (offsetX.value != 0f) {
            offsetX.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .neoGlassCard(
                cornerRadius = theme.cardCornerRadius,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.2.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = if (isPlaying) theme.primaryAccent else SpecularGlassRimBottom
            )
            .clickable { onBarClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .onSizeChanged { barWidthPx = it.width.toFloat() }
            ) {
                // Background Directional HUD Cue: Previous Song (Revealed on Right Drag)
                if (offsetX.value > cueActivationPx) {
                    val cueAlpha = (offsetX.value / thresholdPx).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .graphicsLayer { alpha = cueAlpha },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ANTERIOR",
                            color = theme.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Background Directional HUD Cue: Next Song (Revealed on Left Drag)
                if (offsetX.value < -cueActivationPx) {
                    val cueAlpha = (-offsetX.value / thresholdPx).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .graphicsLayer { alpha = cueAlpha },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRÓXIMA",
                            color = theme.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Sliding Song Info & Action Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = offsetX.value
                            val rawAlpha = 1.0f - (kotlin.math.abs(offsetX.value) / maxAlphaFadePx)
                            alpha = rawAlpha.coerceIn(0.35f, 1.0f)
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragTarget = offsetX.value
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val currentOffset = dragTarget
                                        val offscreenDistance = if (barWidthPx > 0f) barWidthPx else with(density) { 400.dp.toPx() }
                                        if (currentOffset <= -thresholdPx) {
                                            // Drag to the left past threshold (-72.dp): animate offscreen, call onSkipNext(), spring back from positive offset
                                            offsetX.animateTo(
                                                targetValue = -offscreenDistance,
                                                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                                            )
                                            onSkipNext()
                                            offsetX.snapTo(offscreenDistance * 0.75f)
                                            dragTarget = 0f
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        } else if (currentOffset >= thresholdPx) {
                                            // Drag to the right past threshold (+72.dp): animate offscreen, call onSkipPrevious(), spring back from negative offset
                                            offsetX.animateTo(
                                                targetValue = offscreenDistance,
                                                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                                            )
                                            onSkipPrevious()
                                            offsetX.snapTo(-offscreenDistance * 0.75f)
                                            dragTarget = 0f
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        } else {
                                            // Released below threshold: snap back smoothly to 0f with a spring bounce animation
                                            dragTarget = 0f
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        dragTarget = 0f
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragTarget += dragAmount
                                    val target = dragTarget
                                    coroutineScope.launch {
                                        offsetX.snapTo(target)
                                    }
                                }
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reliable Artwork with Neoclassical Statue fallback
                    ArtworkImage(
                        artworkUri = currentSong.albumArtUri,
                        modifier = Modifier.size(46.dp),
                        cornerRadius = if (theme.cardCornerRadius < 8.dp) theme.cardCornerRadius else 8.dp,
                        accentColor = theme.secondaryAccent
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Track Title and Artist
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            color = theme.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "TRACK // ${currentSong.artist}",
                            color = theme.textMuted,
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
                            tint = if (currentSong.isFavorite) theme.secondaryAccent else theme.textMuted,
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
                            tint = theme.textPrimary,
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
            }

            // High-precision Cyber Progress Line
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = theme.primaryAccent,
                trackColor = WireframeBorder.copy(alpha = 0.5f)
            )
        }
    }
}
