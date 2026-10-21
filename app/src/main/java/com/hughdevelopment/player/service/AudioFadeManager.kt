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
 * Provides smooth volume ramping for fade-in and soft fade-out.
 * Concurrency-safe: rapid clicks cancel prior fades cleanly and guarantee
 * correct volume states without deadlocks or missing clicks.
 */
class AudioFadeManager(
    private val scope: CoroutineScope,
    private val player: Player
) {
    private var fadeJob: Job? = null
    var defaultFadeDurationMs: Long = 800L

    fun cancel() {
        fadeJob?.cancel()
        fadeJob = null
    }

    /**
     * Smoothly fades in volume from 0.0 to 1.0.
     */
    fun fadeIn(durationMs: Long = defaultFadeDurationMs, onComplete: (() -> Unit)? = null) {
        fadeJob?.cancel()

        if (durationMs <= 50L) {
            player.volume = 1.0f
            if (!player.isPlaying) {
                player.play()
            }
            onComplete?.invoke()
            return
        }

        fadeJob = scope.launch(Dispatchers.Main) {
            try {
                player.volume = 0.0f
                if (!player.isPlaying) {
                    player.play()
                }

                val safeDuration = durationMs.coerceIn(100L, 2000L)
                val steps = (safeDuration / 30L).toInt().coerceIn(6, 30)
                val stepDelay = (safeDuration / steps).coerceAtLeast(10L)

                for (i in 1..steps) {
                    if (!isActive) break
                    val progress = i.toFloat() / steps
                    val curveVolume = sin(progress * (Math.PI / 2.0)).toFloat()
                    player.volume = curveVolume.coerceIn(0.0f, 1.0f)
                    delay(stepDelay)
                }
                player.volume = 1.0f
                onComplete?.invoke()
            } finally {
                if (isActive) {
                    player.volume = 1.0f
                }
            }
        }
    }

    /**
     * Soft anti-pop fade out for Pause (max 200ms).
     * Pauses the player when done, then restores volume to 1.0f for subsequent play.
     */
    fun fadeOutAndPause(durationMs: Long = 200L, onPaused: (() -> Unit)? = null) {
        fadeJob?.cancel()

        val safeDuration = durationMs.coerceIn(50L, 250L)
        if (safeDuration <= 50L) {
            player.pause()
            player.volume = 1.0f
            onPaused?.invoke()
            return
        }

        fadeJob = scope.launch(Dispatchers.Main) {
            try {
                val startingVolume = player.volume.coerceAtLeast(0.1f)
                val steps = (safeDuration / 25L).toInt().coerceIn(4, 10)
                val stepDelay = (safeDuration / steps).coerceAtLeast(10L)

                for (i in 1..steps) {
                    if (!isActive) break
                    val progress = i.toFloat() / steps
                    val curveFactor = cos(progress * (Math.PI / 2.0)).toFloat()
                    player.volume = (startingVolume * curveFactor).coerceIn(0.0f, 1.0f)
                    delay(stepDelay)
                }
            } finally {
                player.pause()
                player.volume = 1.0f
                onPaused?.invoke()
            }
        }
    }
}
