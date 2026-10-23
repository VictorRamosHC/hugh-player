package com.hughdevelopment.player.data.ai

import android.content.Context
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.model.SovereignRank
import com.hughdevelopment.player.hermes.HermesVoiceManager
import java.util.Calendar

/**
 * HermesPetEngine // Sovereign On-Device Conversational Core
 * 
 * Technical Co-CEO Decision:
 * 100% Offline, Zero Cloud Dependencies, Zero API Keys, Zero Cost.
 * A context-aware stateful heuristic conversational agent with procedural personality,
 * telemetric observation, native offline TTS voice synthesis and direct vault action dispatching.
 */
object HermesPetEngine {

    var isVoiceEnabled: Boolean
        get() = HermesVoiceManager.getInstance().isHermesVoiceEnabled
        set(value) {
            HermesVoiceManager.getInstance().isHermesVoiceEnabled = value
        }

    /**
     * Initializes the native offline Android TTS engine via HermesVoiceManager.
     */
    fun initializeVoice(context: Context, enabled: Boolean = true) {
        HermesVoiceManager.init(context, enabled)
    }

    /**
     * Speaks the provided text offline if voice is enabled.
     */
    fun speak(text: String, utteranceId: String = "hermes_${System.currentTimeMillis()}") {
        HermesVoiceManager.getInstance().speak(text, utteranceId)
    }

    /**
     * Speaks a short offline greeting quip based on current listening telemetry.
     */
    fun speakGreeting(overview: ListeningOverview, songsCount: Int, playlistsCount: Int) {
        val quip = getDynamicGreeting(overview, songsCount, playlistsCount)
        speak(quip, "greeting_quip")
    }

    /**
     * Speaks a direct greeting to the user.
     */
    fun speakGreeting(userName: String = "Soberano") {
        HermesVoiceManager.getInstance().speakGreeting(userName)
    }

    /**
     * Celebrates streak milestone or daily completion.
     */
    fun speakStreakCelebration(streakDays: Int) {
        HermesVoiceManager.getInstance().speakStreakCelebration(streakDays)
    }

    /**
     * Celebrates rank ascension.
     */
    fun speakRankPromotion(rank: SovereignRank) {
        HermesVoiceManager.getInstance().speakRankPromotion(rank)
    }

    /**
     * Celebrates rank ascension by rank title string.
     */
    fun speakRankPromotion(newRank: String) {
        HermesVoiceManager.getInstance().speakRankPromotion(newRank)
    }

    /**
     * Strips emojis, hashtags, and markdown cyber-sigils for natural voice pronunciation.
     */
    fun sanitizeForSpeech(input: String): String {
        return HermesVoiceManager.sanitize(input)
    }

    /**
     * Releases native TTS resources.
     */
    fun shutdown() {
        HermesVoiceManager.getInstance().shutdown()
    }

    data class HermesMessage(
        val sender: String, // "Hermes" or "User"
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        val actionType: HermesActionType? = null
    )

    enum class HermesActionType {
        AUTO_ORGANIZE_FOLDERS,
        PLAY_FAVORITES,
        SHOW_STREAK,
        FOCUS_MODE
    }

    /**
     * Generates a context-aware greeting & observational quip based on real-time app telemetry.
     */
    fun getDynamicGreeting(
        overview: ListeningOverview,
        songsCount: Int,
        playlistsCount: Int
    ): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val streak = overview.streakInfo.currentStreak
        val isTodayDone = overview.streakInfo.isTodayCompleted
        val topSong = overview.topSongs.firstOrNull()

        return when {
            // Late night banter (00:00 - 05:00)
            hour in 0..5 -> {
                if (topSong != null && topSong.playCount >= 4) {
                    "Madrugada adentro, Hugh? Detectei '${topSong.title}' em loop. A música é boa mesmo ou o café venceu a insônia?"
                } else {
                    "Vigília no Vault... O silêncio da madrugada é a melhor frequência para o Aether. Boa audição, chefe."
                }
            }

            // Streak at risk in the evening (20:00 - 23:59)
            hour >= 20 && !isTodayDone && streak > 0 -> {
                "Alerta estóico: Sua ofensiva de $streak dias está em risco antes da meia-noite! Ouça 5 minutos agora para manter a chama acesa."
            }

            // Streak validated celebration
            isTodayDone && streak >= 3 -> {
                "Ofensiva de $streak dias impecável 🔥. A disciplina no Vault separa os amadores dos verdadeiros mestres do áudio."
            }

            // Unorganized library observation
            playlistsCount <= 1 && songsCount >= 10 -> {
                "Detectei $songsCount faixas soltas no disco. Que tal deixar que eu organize tudo nas 4 pastas com o motor de IA?"
            }

            // Morning stoic motivation
            hour in 6..11 -> {
                "Bom dia, Fundador. $songsCount faixas prontas e blindadas 100% offline no Vault. O que vamos conquistar hoje?"
            }

            // Afternoon focus
            hour in 12..18 -> {
                "Tarde produtiva. Mais de ${overview.totalMinutesListened} minutos acumulados na sua jornada musical. Foco total!"
            }

            else -> {
                "Hermes a postos. Seu santuário sonoro está 100% offline, sem algoritmos invasivos te espionando."
            }
        }
    }

    /**
     * Process conversational inputs from the user and respond with sovereign wit & actions.
     */
    fun respondToUserQuery(
        query: String,
        overview: ListeningOverview,
        songsCount: Int,
        playlists: List<Playlist>
    ): HermesMessage {
        val q = query.trim().lowercase()

        val response = when {
            q.contains("ofensiva") || q.contains("streak") || q.contains("dias") -> {
                val s = overview.streakInfo.currentStreak
                val msg = if (overview.streakInfo.isTodayCompleted) {
                    "Sua ofensiva está em $s dias seguidos 🔥! A meta de hoje já foi batida. Continue firme amanhã!"
                } else {
                    "Você está com $s dias de ofensiva. Ainda faltam alguns minutos para bater a meta de hoje e proteger a chama!"
                }
                HermesMessage("Hermes", msg, actionType = HermesActionType.SHOW_STREAK)
            }

            q.contains("organiz") || q.contains("pasta") || q.contains("arrum") -> {
                HermesMessage(
                    "Hermes",
                    "Posso separar sua biblioteca inteira nas 4 categorias de elite (Cristãs Cantadas, Playback, Dia a Dia e Reuniões) agora mesmo. Basta autorizar!",
                    actionType = HermesActionType.AUTO_ORGANIZE_FOLDERS
                )
            }

            q.contains("o que ouvir") || q.contains("recomenda") || q.contains("tocar") || q.contains("sugestão") -> {
                val top = overview.topSongs.firstOrNull()
                if (top != null) {
                    HermesMessage(
                        "Hermes",
                        "Sua faixa mais tocada no momento é '${top.title}' de ${top.artist} (${top.playCount}x). Quer que eu dispare suas favoritas no shuffle?",
                        actionType = HermesActionType.PLAY_FAVORITES
                    )
                } else {
                    HermesMessage(
                        "Hermes",
                        "Que tal abrir a pasta 'Dia a Dia' e deixar o Aether Vault guiar sua tarde no shuffle?",
                        actionType = HermesActionType.PLAY_FAVORITES
                    )
                }
            }

            q.contains("xp") || q.contains("rank") || q.contains("nível") || q.contains("nivel") -> {
                val rank = overview.rankTier
                HermesMessage(
                    "Hermes",
                    "Você está no Rank LVL ${rank.level} • ${rank.title} com ${overview.xp} XP acumulados. A consistência diária é o segredo para o próximo nível!"
                )
            }

            q.contains("filosof") || q.contains("frase") || q.contains("estóic") || q.contains("estoic") || q.contains("conselho") -> {
                val quotes = listOf(
                    "“Não nos perturbam as coisas, mas a opinião que temos delas.” — Epicteto. Deixe a música acalmar sua mente.",
                    "“A arte é longa, a vida é curta.” — Mantenha seu Vault sagrado e ouça o que verdadeiramente ressoa com você.",
                    "“Aquele que tem um porquê para viver pode suportar quase qualquer como.” — Nietzsche. Foco na missão, Hugh!"
                )
                HermesMessage("Hermes", quotes.random())
            }

            else -> {
                HermesMessage(
                    "Hermes",
                    "Entendido, Fundador. Sou seu assistente de bolso soberano: posso organizar suas pastas, relatar sua ofensiva ou indicar o que ouvir. Como posso ajudar?"
                )
            }
        }
        speak(response.text)
        return response
    }
}
