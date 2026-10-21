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
    var defaultFadeDurationMs: Long = 800L

    /**
     * Smoothly fades in volume from 0.0 to 1.0.
     * Guaranteed: player.volume will always be 1.0f upon completion or cancellation.
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
                val steps = (safeDuration / 40L).toInt().coerceIn(8, 40)
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
                // Ensure volume is never left at zero or partial
                player.volume = 1.0f
            }
        }
    }

    /**
     * Snappy anti-pop fade out for Pause (max 250ms).
     * Immediately pauses player and resets volume to 1.0f.
     */
    fun fadeOutAndPause(durationMs: Long = 250L) {
        fadeJob?.cancel()

        val safeDuration = durationMs.coerceIn(50L, 300L)
        if (safeDuration <= 50L) {
            player.pause()
            player.volume = 1.0f
            return
        }

        fadeJob = scope.launch(Dispatchers.Main) {
            try {
                val startingVolume = player.volume.coerceAtLeast(0.1f)
                val steps = (safeDuration / 30L).toInt().coerceIn(6, 15)
                val stepDelay = (safeDuration / steps).coerceAtLeast(10L)

                for (i in 1..steps) {
                    if (!isActive) break
                    val progress = i.toFloat() / steps
                    val curveFactor = cos(progress * (Math.PI / 2.0)).toFloat()
                    player.volume = (startingVolume * curveFactor).coerceIn(0.0f, 1.0f)
                    delay(stepDelay)
                }
                player.pause()
                player.volume = 1.0f
            } finally {
                player.pause()
                player.volume = 1.0f
            }
        }
    }

    /**
     * Smooth track transition: quick 100ms fade-out dip, executes track switch action, then fades in.
     * Guaranteed: action() will ALWAYS execute even if cancelled or rapid clicks occur!
     */
    fun fadeOutAndAction(durationMs: Long = defaultFadeDurationMs, action: () -> Unit) {
        fadeJob?.cancel()

        if (durationMs <= 50L) {
            action()
            player.volume = 1.0f
            fadeIn(0L)
            return
        }

        val fadeInDuration = durationMs.coerceIn(150L, 1000L)

        fadeJob = scope.launch(Dispatchers.Main) {
            var actionExecuted = false
            try {
                val startingVolume = player.volume.coerceAtLeast(0.1f)
                val steps = 5
                val stepDelay = 20L // Total 100ms dip

                for (i in 1..steps) {
                    if (!isActive) break
                    val progress = i.toFloat() / steps
                    val curveFactor = cos(progress * (Math.PI / 2.0)).toFloat()
                    player.volume = (startingVolume * curveFactor).coerceIn(0.0f, 1.0f)
                    delay(stepDelay)
                }
                action()
                actionExecuted = true
                fadeIn(fadeInDuration)
            } finally {
                if (!actionExecuted) {
                    action()
                }
                if (!isActive) {
                    player.volume = 1.0f
                }
            }
        }
    }
}
