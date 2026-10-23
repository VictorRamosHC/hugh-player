package com.hughdevelopment.player.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log
import com.hughdevelopment.player.data.settings.EqualizerPreset
import com.hughdevelopment.player.data.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages native Android hardware audio effects (Equalizer and BassBoost)
 * coupled to the active playback audioSessionId.
 *
 * Implements R1 requirements:
 * - 5 parametric frequency bands (~60Hz, ~230Hz, ~910Hz, ~3.6kHz, ~14kHz)
 * - Safe millibel clamping between [-1500, 1500] and hardware bandLevelRange
 * - BassBoost strength 0 to 1000
 * - Anti-pop smoothing: applies settings before enabling, caches states, avoids redundant HAL calls
 * - Seamless integration with PlayerSettings persistence and MusicPlayerController flows
 */
class AudioEffectManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val settingsManager = SettingsManager.getInstance(appContext)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var activeSessionId: Int = 0

    // Reactive StateFlows
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _currentPreset = MutableStateFlow(EqualizerPreset.FLAT)
    val currentPreset = _currentPreset.asStateFlow()

    private val _bandLevels = MutableStateFlow(EqualizerPreset.FLAT.bandLevels)
    val bandLevels = _bandLevels.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(0)
    val bassBoostStrength = _bassBoostStrength.asStateFlow()

    private val _centerFrequencies = MutableStateFlow(DEFAULT_CENTER_FREQS)
    val centerFrequencies = _centerFrequencies.asStateFlow()

    private val _bandLevelRange = MutableStateFlow(DEFAULT_MIN_MB..DEFAULT_MAX_MB)
    val bandLevelRange = _bandLevelRange.asStateFlow()

    private val _isSupported = MutableStateFlow(true)
    val isSupported = _isSupported.asStateFlow()

    init {
        // Initialize state from persisted settings
        val initialSettings = settingsManager.settings.value
        _isEnabled.value = initialSettings.equalizerEnabled
        _currentPreset.value = initialSettings.equalizerPreset
        _bandLevels.value = initialSettings.equalizerBandLevels
        _bassBoostStrength.value = initialSettings.bassBoostStrength
    }

    /**
     * Binds native AudioFx instances to the player's active audioSessionId.
     * Prevents audio pops by configuring band levels and bass boost prior to enabling.
     */
    @Synchronized
    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) {
            Log.d(TAG, "Skipping invalid audioSessionId: $audioSessionId")
            return
        }

        if (audioSessionId == activeSessionId && equalizer != null && bassBoost != null) {
            Log.d(TAG, "Audio session $audioSessionId already bound.")
            return
        }

        Log.i(TAG, "Binding audio effects to audioSessionId: $audioSessionId")
        releaseEffects()
        activeSessionId = audioSessionId

        try {
            // Instantiate Equalizer with priority 0 (standard app priority)
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq

            // Query hardware range and center frequencies
            try {
                val range = eq.bandLevelRange
                if (range != null && range.size >= 2) {
                    val minMb = range[0].toInt().coerceAtLeast(DEFAULT_MIN_MB)
                    val maxMb = range[1].toInt().coerceAtMost(DEFAULT_MAX_MB)
                    _bandLevelRange.value = minMb..maxMb
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not query bandLevelRange, using defaults", e)
            }

            try {
                val numBands = eq.numberOfBands.toInt()
                val freqs = mutableListOf<Int>()
                for (b in 0 until minOf(5, numBands)) {
                    val centerFreqHz = eq.getCenterFreq(b.toShort()) / 1000
                    freqs.add(centerFreqHz)
                }
                while (freqs.size < 5) {
                    freqs.add(DEFAULT_CENTER_FREQS[freqs.size])
                }
                _centerFrequencies.value = freqs
            } catch (e: Exception) {
                Log.w(TAG, "Could not query center frequencies, using defaults", e)
                _centerFrequencies.value = DEFAULT_CENTER_FREQS
            }

            // Apply current band levels smoothly before enabling to prevent acoustic pops
            val currentLevels = _bandLevels.value
            applyBandLevelsInternal(eq, currentLevels)

            // Instantiate BassBoost
            val bb = BassBoost(0, audioSessionId)
            bassBoost = bb
            val currentBass = _bassBoostStrength.value
            applyBassBoostInternal(bb, currentBass)

            // Enable effects according to active state
            val shouldEnable = _isEnabled.value
            eq.enabled = shouldEnable
            bb.enabled = shouldEnable && currentBass > 0

            _isSupported.value = true
            Log.i(TAG, "Audio effects bound successfully to session $audioSessionId (enabled=$shouldEnable)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize native audiofx for session $audioSessionId", e)
            _isSupported.value = false
            releaseEffects()
        }
    }

    /**
     * Unbinds active audio session without clearing user configuration state.
     */
    @Synchronized
    fun unbindAudioSession() {
        Log.i(TAG, "Unbinding audio session $activeSessionId")
        releaseEffects()
        activeSessionId = 0
    }

    /**
     * Enables or disables the DSP equalizer and bass boost.
     */
    @Synchronized
    fun setEnabled(enabled: Boolean) {
        if (_isEnabled.value == enabled) return
        _isEnabled.value = enabled
        settingsManager.setEqualizerEnabled(enabled)

        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled && _bassBoostStrength.value > 0
        } catch (e: Throwable) {
            Log.e(TAG, "Error setting effect enabled: $enabled", e)
        }
    }

    /**
     * Applies a tactile preset (Flat, Vocal Clarity, Sub-Bass Brutalist, Acoustic Worship, Podcast Enhancer, Custom).
     */
    @Synchronized
    fun setPreset(preset: EqualizerPreset) {
        _currentPreset.value = preset
        settingsManager.setEqualizerPreset(preset)

        if (preset != EqualizerPreset.CUSTOM) {
            _bandLevels.value = preset.bandLevels
            _bassBoostStrength.value = preset.bassBoost

            equalizer?.let { eq ->
                applyBandLevelsInternal(eq, preset.bandLevels)
            }

            bassBoost?.let { bb ->
                applyBassBoostInternal(bb, preset.bassBoost)
                if (_isEnabled.value) {
                    try {
                        bb.enabled = preset.bassBoost > 0
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error enabling bass boost for preset ${preset.id}", e)
                    }
                }
            }
        }
    }

    /**
     * Updates an individual band level (index 0..4) in millibels [-1500, 1500].
     * Marks the active preset as CUSTOM.
     */
    @Synchronized
    fun setBandLevel(bandIndex: Int, levelMillibels: Int) {
        if (bandIndex !in 0..4) return
        val current = _bandLevels.value.toMutableList()
        val clamped = clampMillibels(levelMillibels)
        if (current[bandIndex] == clamped && _currentPreset.value == EqualizerPreset.CUSTOM) return

        current[bandIndex] = clamped
        _bandLevels.value = current
        _currentPreset.value = EqualizerPreset.CUSTOM
        settingsManager.setEqualizerBandLevel(bandIndex, clamped)

        equalizer?.let { eq ->
            try {
                val numBands = eq.numberOfBands.toInt()
                if (bandIndex < numBands) {
                    eq.setBandLevel(bandIndex.toShort(), clamped.toShort())
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error setting band $bandIndex to $clamped mB", e)
            }
        }
    }

    /**
     * Updates all 5 band levels simultaneously in millibels.
     * Marks preset as CUSTOM.
     */
    @Synchronized
    fun setBandLevels(levels: List<Int>) {
        if (levels.size != 5) return
        val clamped = levels.map { clampMillibels(it) }
        _bandLevels.value = clamped
        _currentPreset.value = EqualizerPreset.CUSTOM
        settingsManager.setEqualizerBandLevels(clamped)

        equalizer?.let { eq ->
            applyBandLevelsInternal(eq, clamped)
        }
    }

    /**
     * Updates BassBoost strength (0 to 1000).
     */
    @Synchronized
    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        if (_bassBoostStrength.value == clamped) return
        _bassBoostStrength.value = clamped
        settingsManager.setBassBoostStrength(clamped)

        bassBoost?.let { bb ->
            applyBassBoostInternal(bb, clamped)
            if (_isEnabled.value) {
                try {
                    bb.enabled = clamped > 0
                } catch (e: Throwable) {
                    Log.e(TAG, "Error updating bass boost enabled state", e)
                }
            }
        }
    }

    /**
     * Internal helper to apply band levels to an Equalizer instance safely.
     */
    private fun applyBandLevelsInternal(eq: Equalizer, levels: List<Int>) {
        try {
            val numBands = eq.numberOfBands.toInt()
            for (i in 0 until minOf(levels.size, numBands)) {
                val clamped = clampMillibels(levels[i])
                eq.setBandLevel(i.toShort(), clamped.toShort())
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error applying band levels internally", e)
        }
    }

    /**
     * Internal helper to apply BassBoost strength safely.
     */
    private fun applyBassBoostInternal(bb: BassBoost, strength: Int) {
        try {
            if (bb.strengthSupported) {
                val clamped = strength.coerceIn(0, 1000).toShort()
                bb.setStrength(clamped)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error applying bass boost internally", e)
        }
    }

    /**
     * Safely clamps millibel values within hardware capability and [-1500, 1500].
     */
    private fun clampMillibels(value: Int): Int {
        val range = _bandLevelRange.value
        return value.coerceIn(range.first, range.last).coerceIn(DEFAULT_MIN_MB, DEFAULT_MAX_MB)
    }

    /**
     * Releases active hardware AudioFx instances cleanly.
     */
    private fun releaseEffects() {
        try {
            equalizer?.enabled = false
            equalizer?.release()
        } catch (e: Throwable) {
            Log.w(TAG, "Error releasing equalizer", e)
        } finally {
            equalizer = null
        }

        try {
            bassBoost?.enabled = false
            bassBoost?.release()
        } catch (e: Throwable) {
            Log.w(TAG, "Error releasing bassBoost", e)
        } finally {
            bassBoost = null
        }
    }

    /**
     * Full teardown of AudioEffectManager resources.
     */
    @Synchronized
    fun release() {
        releaseEffects()
        activeSessionId = 0
    }

    companion object {
        private const val TAG = "AudioEffectManager"

        const val DEFAULT_MIN_MB = -1500
        const val DEFAULT_MAX_MB = 1500

        // Standard 5-band approximate center frequencies in Hz
        val DEFAULT_CENTER_FREQS = listOf(60, 230, 910, 3600, 14000)

        @Volatile
        private var instance: AudioEffectManager? = null

        fun getInstance(context: Context): AudioEffectManager {
            return instance ?: synchronized(this) {
                instance ?: AudioEffectManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
