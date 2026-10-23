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
 * Provides smooth volume ramping for fade-in, fade-out, and soft pause transitions.
 * Concurrency-safe: rapid clicks cancel prior fades cleanly and guarantee
 * correct volume states without deadlocks, missing clicks, or Binder IPC overload.
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
        player.volume = 1.0f
    }

    /**
     * Smoothly fades in volume from 0.0 to 1.0 using a sinusoidal curve.
     * Supports user durations up to 15.0 seconds.
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

                val safeDuration = durationMs.coerceIn(100L, 15000L)
                val steps = (safeDuration / 45L).toInt().coerceIn(8, 45)
                val stepDelay = (safeDuration / steps).coerceAtLeast(25L)

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
     * Smoothly fades out volume from current volume to 0.0 without pausing.
     * Useful for track-to-track transitions.
     */
    fun fadeOut(durationMs: Long = defaultFadeDurationMs, onComplete: (() -> Unit)? = null) {
        fadeJob?.cancel()

        if (durationMs <= 50L) {
            player.volume = 0.0f
            onComplete?.invoke()
            return
        }

        fadeJob = scope.launch(Dispatchers.Main) {
            try {
                val startingVolume = player.volume.coerceAtLeast(0.1f)
                val safeDuration = durationMs.coerceIn(100L, 5000L)
                val steps = (safeDuration / 45L).toInt().coerceIn(6, 35)
                val stepDelay = (safeDuration / steps).coerceAtLeast(25L)

                for (i in 1..steps) {
                    if (!isActive) break
                    val progress = i.toFloat() / steps
                    val curveFactor = cos(progress * (Math.PI / 2.0)).toFloat()
                    player.volume = (startingVolume * curveFactor).coerceIn(0.0f, 1.0f)
                    delay(stepDelay)
                }
                player.volume = 0.0f
                onComplete?.invoke()
            } catch (e: Exception) {
                // Cancelled or interrupted
            }
        }
    }

    /**
     * Smooth anti-pop fade out for Pause.
     * Smoothly scales volume down, pauses playback, then resets volume to 1.0f for subsequent resume.
     */
    fun fadeOutAndPause(durationMs: Long = 600L, onPaused: (() -> Unit)? = null) {
        fadeJob?.cancel()

        val safeDuration = durationMs.coerceIn(80L, 4000L)
        if (safeDuration <= 80L) {
            player.pause()
            player.volume = 1.0f
            onPaused?.invoke()
            return
        }

        fadeJob = scope.launch(Dispatchers.Main) {
            try {
                val startingVolume = player.volume.coerceAtLeast(0.1f)
                val steps = (safeDuration / 45L).toInt().coerceIn(6, 35)
                val stepDelay = (safeDuration / steps).coerceAtLeast(25L)

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
