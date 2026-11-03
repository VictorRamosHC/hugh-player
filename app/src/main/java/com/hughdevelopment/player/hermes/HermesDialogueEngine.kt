package com.hughdevelopment.player.hermes

import kotlin.random.Random

/**
 * HermesDialogueEngine // Sovereign Voice Persona & Cyber-Narrator Dialogue Synthesis.
 *
 * Implements Item 8:
 * Offline pre-programmed dialogues with ElevenLabs-style cadence,
 * structured spoken briefings covering daily listening time, streak discipline,
 * rank ascension, and strategic audio philosophy tips.
 */
object HermesDialogueEngine {

    private val sovereignTips = listOf(
        "Dica do Hermes: O equalizador DSP de cinco bandas opera diretamente no chip de áudio do seu dispositivo. Ajuste o sub-grave para máxima fidelidade acústica.",
        "Dica do Hermes: O Aether Vault não depende de servidores externos. Suas estatísticas e arquivos pertencem exclusivamente a você.",
        "Diretriz Estratégica: Mantenha sua ofensiva diária ouvindo pelo menos cinco minutos. Consistência é o verdadeiro luxo frio corporativo.",
        "Dica do Hermes: Utilize o gesto de deslizar no mini player para alternar entre faixas rapidamente durante suas jornadas.",
        "Dica do Hermes: O modo de faixas ocultas permite purificar sua biblioteca sem nunca apagar arquivos do armazenamento do telefone.",
        "Diretriz Arasaka: Precisão, foco e autonomia sonora. Cada segundo de reprodução no santuário alimenta seu rank de Arconte."
    )

    private val greetingIntros = listOf(
        "Saudações, Fundador Victor. Relatório de telemetria do Aether Vault pronto.",
        "Iniciando briefing do sistema. Status do santuário sonoro sincronizado.",
        "Atenção Soberano. Aqui está o seu relatório de disciplina acústica de hoje."
    )

    /**
     * Synthesizes a structured spoken briefing paragraph.
     */
    fun buildDailyBriefing(
        formattedTime: String,
        streakDays: Int,
        rankTitle: String,
        totalTracks: Int
    ): String {
        val intro = greetingIntros[Random.nextInt(greetingIntros.size)]
        val timePhrase = "Você já acumulou $formattedTime de audição soberana."
        val streakPhrase = when {
            streakDays <= 0 -> "Sua ofensiva de hoje ainda aguarda validação. Ouça mais alguns minutos para garantir o selo diário."
            streakDays == 1 -> "Ofensiva diária ativada com sucesso: um dia de disciplina ininterrupta."
            else -> "Sua ofensiva está em $streakDays dias consecutivos de disciplina inabalável."
        }
        val rankPhrase = "Seu nível de autoridade acústica atual é: $rankTitle, com $totalTracks faixas ativas catalogadas no santuário."
        val tip = sovereignTips[Random.nextInt(sovereignTips.size)]

        return "$intro $timePhrase $streakPhrase $rankPhrase $tip"
    }

    /**
     * Returns a random standalone strategic narrator tip.
     */
    fun getRandomTip(): String {
        return sovereignTips[Random.nextInt(sovereignTips.size)]
    }
}
