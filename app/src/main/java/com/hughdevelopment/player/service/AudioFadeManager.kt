package com.hughdevelopment.player.service

import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * AudioFadeManager
 * 
 * Provides native logarithmic & sinusoidal volume ramping for fade-in, fade-out,
 * and track transitions without clipping or popping artifacts.
 */
class AudioFadeManager(
    private val scope: CoroutineScope,
    private val player: Player
) {
    private var fadeJob: Job? = null
    var defaultFadeDurationMs: Long = 400L

    fun fadeIn(durationMs: Long = defaultFadeDurationMs, onComplete: (() -> Unit)? = null) {
        fadeJob?.cancel()
        fadeJob = scope.launch(Dispatchers.Main) {
            val steps = 25
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            player.volume = 0.0f
            if (!player.isPlaying) {
                player.play()
            }

            for (i in 1..steps) {
                if (!isActive) break
                val progress = i.toFloat() / steps
                // Smooth sinusoidal curve: y = sin(progress * PI / 2)
                val curveVolume = sin(progress * (Math.PI / 2.0)).toFloat()
                player.volume = curveVolume.coerceIn(0.0f, 1.0f)
                delay(stepDelay)
            }
            player.volume = 1.0f
            onComplete?.invoke()
        }
    }

    fun fadeOut(durationMs: Long = defaultFadeDurationMs, onComplete: (() -> Unit)? = null) {
        fadeJob?.cancel()
        fadeJob = scope.launch(Dispatchers.Main) {
            val steps = 25
            val stepDelay = (durationMs / steps).coerceAtLeast(10L)
            val startingVolume = player.volume

            for (i in 1..steps) {
                if (!isActive) break
                val progress = i.toFloat() / steps
                // Cosine curve fading from 1.0 down to 0.0
                val curveFactor = cos(progress * (Math.PI / 2.0)).toFloat()
                player.volume = (startingVolume * curveFactor).coerceIn(0.0f, 1.0f)
                delay(stepDelay)
            }
            player.volume = 0.0f
            onComplete?.invoke()
        }
    }

    fun fadeOutAndPause(durationMs: Long = defaultFadeDurationMs) {
        fadeOut(durationMs) {
            player.pause()
            player.volume = 1.0f // Reset for subsequent playback
        }
    }

    fun fadeOutAndAction(durationMs: Long = defaultFadeDurationMs, action: () -> Unit) {
        fadeOut(durationMs) {
            action()
            fadeIn(durationMs)
        }
    }
}
