package com.hughdevelopment.player.hermes

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log
import com.hughdevelopment.player.data.model.SovereignRank
import java.util.Locale

/**
 * HermesVoiceManager // Sovereign Offline Voice Synthesis Engine (F-13, F-14, F-15).
 *
 * Technical Integrity Mandate:
 * 100% Offline, Zero Cloud Dependencies, Zero API Keys, Zero Cost.
 * Integrates Android's native TextToSpeech engine with safe pt-BR/fallback locale configuration,
 * cyber-sigil text sanitization, streak/rank celebration announcements, and user settings guard.
 */
class HermesVoiceManager(
    context: Context? = null,
    enabled: Boolean = true
) {
    private var appContext: Context? = context?.applicationContext ?: context
    private var tts: TextToSpeech? = null

    var isInitialized: Boolean = false
        private set

    var isHermesVoiceEnabled: Boolean = enabled

    private var pendingUtterance: String? = null

    init {
        if (appContext != null) {
            initTts(appContext!!)
        }
    }

    /**
     * Initializes or reconfigures the TextToSpeech engine with context and enabled state.
     */
    fun init(context: Context, enabled: Boolean = isHermesVoiceEnabled) {
        this.appContext = context.applicationContext
        this.isHermesVoiceEnabled = enabled
        if (tts == null && appContext != null) {
            initTts(appContext!!)
        }
    }

    private fun initTts(context: Context) {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let { engine ->
                        val ptBr = Locale("pt", "BR")
                        val langResult = engine.setLanguage(ptBr)
                        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.w(TAG, "pt-BR TTS data not found, falling back to default device locale")
                            engine.setLanguage(Locale.getDefault())
                        }
                        engine.setPitch(0.95f)
                        engine.setSpeechRate(1.0f)

                        val audioAttrs = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        engine.setAudioAttributes(audioAttrs)
                        isInitialized = true
                        Log.i(TAG, "Hermes Voice Engine initialized successfully 100% offline.")

                        // Flush pending utterance if any was queued prior to init
                        pendingUtterance?.let { pending ->
                            pendingUtterance = null
                            if (isHermesVoiceEnabled && pending.isNotBlank()) {
                                engine.speak(pending, TextToSpeech.QUEUE_FLUSH, null, "hermes_pending_${System.currentTimeMillis()}")
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "TTS initialization failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception initializing TextToSpeech", e)
        }
    }

    /**
     * Speaks the provided text using the offline TTS engine.
     * Respects the isHermesVoiceEnabled setting and sanitizes emojis/sigils before speaking.
     */
    fun speak(text: String, utteranceId: String = "hermes_${System.currentTimeMillis()}") {
        if (!isHermesVoiceEnabled) {
            Log.d(TAG, "Hermes Voice is disabled, skipping speech: $text")
            return
        }
        val cleanText = sanitizeForSpeech(text)
        if (cleanText.isBlank()) return

        if (isInitialized && tts != null) {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            pendingUtterance = cleanText
            if (tts == null && appContext != null) {
                initTts(appContext!!)
            }
        }
    }

    /**
     * Speaks an offline greeting to the user.
     */
    fun speakGreeting(userName: String = "Soberano") {
        speak("Saudações, $userName. Santuário sonoro pronto e 100% offline.", "hermes_greeting")
    }

    /**
     * Speaks celebratory phrase for listening streak milestones.
     */
    fun speakStreakCelebration(streakDays: Int) {
        val phrase = getStreakCelebrationPhrase(streakDays)
        speak(phrase, "hermes_streak_$streakDays")
    }

    /**
     * Speaks celebratory phrase for rank promotions (by rank name/title).
     */
    fun speakRankPromotion(newRank: String) {
        val phrase = if (newRank.startsWith("nível", ignoreCase = true) || newRank.startsWith("nivel", ignoreCase = true)) {
            "Atenção Fundador: Promoção de Rank confirmada! Você ascendeu para $newRank."
        } else {
            "Atenção Fundador: Promoção de Rank confirmada! Você ascendeu para nível $newRank."
        }
        speak(phrase, "hermes_rank_promotion")
    }

    /**
     * Speaks celebratory phrase for rank promotions (by SovereignRank model).
     */
    fun speakRankPromotion(rank: SovereignRank) {
        val phrase = getRankPromotionPhrase(rank)
        speak(phrase, "hermes_rank_promotion_${rank.level}")
    }

    /**
     * Sanitizes raw conversational text for clear, natural offline voice pronunciation.
     * Strips emojis, runes, brackets, ascii sigils and collapses multiple whitespace.
     */
    fun sanitizeForSpeech(raw: String): String {
        return sanitize(raw)
    }

    /**
     * Releases native TTS resources and resets initialization state.
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TextToSpeech", e)
        } finally {
            tts = null
            isInitialized = false
            pendingUtterance = null
        }
    }

    companion object {
        private const val TAG = "HermesVoiceManager"

        @Volatile
        private var defaultInstance: HermesVoiceManager? = null

        /**
         * Gets or creates the singleton HermesVoiceManager instance.
         */
        fun getInstance(context: Context? = null): HermesVoiceManager {
            return defaultInstance ?: synchronized(this) {
                defaultInstance ?: HermesVoiceManager(context).also { defaultInstance = it }
            }
        }

        /**
         * Initializes the singleton HermesVoiceManager instance with application context.
         */
        fun init(context: Context, enabled: Boolean = true): HermesVoiceManager {
            return defaultInstance?.apply {
                init(context, enabled)
            } ?: synchronized(this) {
                defaultInstance ?: HermesVoiceManager(context, enabled).also { defaultInstance = it }
            }
        }

        /**
         * Static sanitization helper stripping emojis, runes, brackets, and quotes.
         */
        fun sanitize(raw: String): String {
            return raw
                .replace(Regex("""[🔥🏛️⚡🎧🔴•\[\](){}]"""), "")
                .replace(Regex("""["“”—–_]"""), " ")
                .replace(Regex("""\s+"""), " ")
                .trim()
        }

        /**
         * Generates the celebratory spoken phrase for consecutive daily streaks.
         */
        fun getStreakCelebrationPhrase(streakDays: Int): String {
            return when {
                streakDays <= 1 -> "Meta diária batida no Vault! Disciplina ativada."
                streakDays in 2..6 -> "Ofensiva de $streakDays dias mantida! A consistência constrói o mestre."
                streakDays in 7..29 -> "Impressionante: $streakDays dias de ofensiva ininterrupta no Aether Vault!"
                else -> "Lendário! $streakDays dias de disciplina absoluta. Você é um verdadeiro Arconte!"
            }
        }

        /**
         * Generates celebratory spoken message for sovereign rank ascensions.
         */
        fun getRankPromotionPhrase(rank: SovereignRank): String {
            return "Atenção Fundador: Promoção de Rank confirmada! Você ascendeu para nível ${rank.level}, ${rank.title}."
        }

        /**
         * Evaluates whether speech should be dispatched given enabled setting and input text.
         */
        fun shouldDispatchSpeech(isVoiceEnabled: Boolean, text: String): Boolean {
            if (!isVoiceEnabled) return false
            val clean = sanitize(text)
            return clean.isNotBlank()
        }

        // Static delegate conveniences:
        fun speak(text: String) = defaultInstance?.speak(text)
        fun speakGreeting(userName: String = "Soberano") = defaultInstance?.speakGreeting(userName)
        fun speakStreakCelebration(streakDays: Int) = defaultInstance?.speakStreakCelebration(streakDays)
        fun speakRankPromotion(newRank: String) = defaultInstance?.speakRankPromotion(newRank)
        fun speakRankPromotion(rank: SovereignRank) = defaultInstance?.speakRankPromotion(rank)
        fun shutdown() = defaultInstance?.shutdown()
    }
}
