package com.hughdevelopment.player.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class VaultThemeId {
    AETHER_CLASSIC,
    ARASAKA_PANTHEON,
    DELAMAIN_AI_CORE,
    NEO_OLYMPUS_GOLD,
    NEURAL_VIOLET,
    CYBER_MINT_MATRIX
}

enum class VaultFontCategory {
    MONOSPACE,
    SANS_SERIF,
    SERIF
}

enum class VaultLayoutShape {
    DIVINE_CURVES,    // 14-18dp
    BRUTALIST_SHARP,  // 0-4dp
    NEO_MINIMAL       // 8-10dp
}

data class VaultLexicon(
    val brandHeader: String,
    val brandSub: String,
    val libraryTitle: String,
    val foldersTitle: String,
    val favoritesTitle: String,
    val streakTitle: String,
    val offlineBadge: String,
    val petName: String,
    val petSubtitle: String,
    val nowPlaying: String = "EM REPRODUÇÃO // ATIVO",
    val queue: String = "FILA DE REPRODUÇÃO",
    val settings: String = "CONFIGURAÇÕES",
    val hermesStatus: String = "HERMES // STATUS"
)

data class VaultThemeConfig(
    val id: VaultThemeId,
    val displayName: String,
    val badgeTag: String,
    val description: String,
    val primaryAccent: Color,
    val accentGlow: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardBorderColor: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val cardCornerRadius: Dp,
    val isBrutalistSharp: Boolean,
    val fontCategory: VaultFontCategory = VaultFontCategory.MONOSPACE,
    val layoutShape: VaultLayoutShape = VaultLayoutShape.DIVINE_CURVES,
    val lexicon: VaultLexicon
) {
    val primary: Color get() = primaryAccent
    val background: Color get() = backgroundColor
    val cardBackground: Color get() = surfaceColor
    val destructiveAlert: Color get() = DangerRed
}

object VaultThemes {

    val Classic = VaultThemeConfig(
        id = VaultThemeId.AETHER_CLASSIC,
        displayName = "AETHER CLASSIC",
        badgeTag = "NEOCLASSICAL GLASS",
        description = "Vidro fosco estilo Apple, anéis ciano e âmbar com estatuária clássica dither.",
        primaryAccent = HermeticCyan,
        accentGlow = HermeticCyan.copy(alpha = 0.35f),
        secondaryAccent = CyberAmber,
        tertiaryAccent = KleinBlue,
        backgroundColor = VoidBlack,
        surfaceColor = CharcoalSurface,
        cardBorderColor = WireframeBorder,
        textPrimary = SigilWhite,
        textMuted = MutedStone,
        cardCornerRadius = 14.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.DIVINE_CURVES,
        lexicon = VaultLexicon(
            brandHeader = "AETHER // VAULT",
            brandSub = "SOVEREIGN OFFLINE AUDIO",
            libraryTitle = "BIBLIOTECA",
            foldersTitle = "PASTAS & PLAYLISTS",
            favoritesTitle = "FAVORITAS",
            streakTitle = "OFENSIVA SOBERANA",
            offlineBadge = "100% OFFLINE",
            petName = "HERMES // COMPANHEIRO IA",
            petSubtitle = "IA SOBERANA 100% OFFLINE • ZERO CLOUD",
            nowPlaying = "TOCANDO AGORA // ATIVO",
            queue = "FILA DE REPRODUÇÃO",
            settings = "CONFIGURAÇÕES",
            hermesStatus = "HERMES ONLINE"
        )
    )

    val ArasakaPantheon = VaultThemeConfig(
        id = VaultThemeId.ARASAKA_PANTHEON,
        displayName = "ARASAKA PANTHEON",
        badgeTag = "荒坂神話 // BRUTALISMO",
        description = "Olimpo corporativo cyberpunk: neon vermelho laser, mármore puro e retículas HUD militares.",
        primaryAccent = ArasakaCrimson,
        accentGlow = ArasakaCrimsonGlow,
        secondaryAccent = ImperialBrass,
        tertiaryAccent = ArasakaReticle,
        backgroundColor = MonolithVoid,
        surfaceColor = CorporateSteel,
        cardBorderColor = ArasakaBorder,
        textPrimary = MarbleWhite,
        textMuted = Color(0xFF8B8B9E),
        cardCornerRadius = 4.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        lexicon = VaultLexicon(
            brandHeader = "荒坂 // ARASAKA PANTHEON",
            brandSub = "數據庫 // DATA VAULT SECURE",
            libraryTitle = "COFRE DE DADOS (數據庫)",
            foldersTitle = "SETORES CORPORATIVOS",
            favoritesTitle = "PROTOCOLOS DIVINOS // TIER 0",
            streakTitle = "SINCRONIA DE REDE 99.8%",
            offlineBadge = "未接続 // AIR-GAPPED",
            petName = "HERMES-01 // IA DE CONFORMIDADE ARASAKA",
            petSubtitle = "DIRETRIZ CORPORATIVA • ZERO VAZAMENTO",
            nowPlaying = "荒坂再生 // EXECUÇÃO ATIVA",
            queue = "FILA DE TRANSMISSÃO",
            settings = "CENTRAL DE DIRETRIZES",
            hermesStatus = "ARASAKA COMPLIANCE"
        )
    )

    val DelamainAiCore = VaultThemeConfig(
        id = VaultThemeId.DELAMAIN_AI_CORE,
        displayName = "DELAMAIN AI CORE",
        badgeTag = "CORPORATE // AI EXECUTIVE",
        description = "Minimalismo corporativo de inteligência artificial: azul neon elétrico, platina e concierge executivo Delamain.",
        primaryAccent = DelamainCyan,
        accentGlow = DelamainCyanGlow,
        secondaryAccent = DelamainPlatinum,
        tertiaryAccent = DelamainCyan,
        backgroundColor = DelamainGraphite,
        surfaceColor = DelamainSurface,
        cardBorderColor = DelamainBorder,
        textPrimary = DelamainPlatinum,
        textMuted = DelamainMuted,
        cardCornerRadius = 4.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.SANS_SERIF,
        layoutShape = VaultLayoutShape.NEO_MINIMAL,
        lexicon = VaultLexicon(
            brandHeader = "DELAMAIN // AI CORE",
            brandSub = "REDE DE TRANSPORTE SONORO",
            libraryTitle = "REDE DE ÁUDIO EXECUTIVA",
            foldersTitle = "ROTAS & DIRETÓRIOS",
            favoritesTitle = "PASSAGEIROS VIP // PRIORITÁRIOS",
            streakTitle = "CONCIERGE ON-LINE 100%",
            offlineBadge = "SISTEMA ISOLADO // EXECUTIVO",
            petName = "DELAMAIN // CONCIERGE IA",
            petSubtitle = "SERVIÇO EXECUTIVO DE ÁUDIO • SEGURANÇA MÁXIMA",
            nowPlaying = "TRANSPORTE SONORO // ATIVO",
            queue = "REDE DE DADOS",
            settings = "CENTRAL DE COMANDOS",
            hermesStatus = "DELAMAIN CONCIERGE"
        )
    )

    val NeoOlympusGold = VaultThemeConfig(
        id = VaultThemeId.NEO_OLYMPUS_GOLD,
        displayName = "NEO-OLYMPUS ALCHEMY",
        badgeTag = "SACRED GOLD & BRONZE",
        description = "Alquimia hermética do Sol Negro: ouro puro 24k, bronze forjado e capitéis jônicos radiantes.",
        primaryAccent = OlympusGold,
        accentGlow = OlympusGoldGlow,
        secondaryAccent = AlchemicalBronze,
        tertiaryAccent = OlympusGold,
        backgroundColor = SolarVoid,
        surfaceColor = OlympusSurface,
        cardBorderColor = OlympusBorder,
        textPrimary = Color(0xFFFFFBEB),
        textMuted = Color(0xFFA89F8D),
        cardCornerRadius = 16.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.SERIF,
        layoutShape = VaultLayoutShape.DIVINE_CURVES,
        lexicon = VaultLexicon(
            brandHeader = "OLYMPUS // ALCHEMY",
            brandSub = "SACRED HERMETIC AUDIO VAULT",
            libraryTitle = "COFRE DO SOL NEGRO",
            foldersTitle = "ORDENS & TEMPLOS",
            favoritesTitle = "OFERENDAS DE OURO",
            streakTitle = "CHAMA ETERNA DE PROMETEU",
            offlineBadge = "TEMPLO HERMÉTICO // ISOLADO",
            petName = "ORÁCULO // HERMES TRISMEGISTO",
            petSubtitle = "SABEDORIA OCULTA • ALQUIMIA SONORA",
            nowPlaying = "TRANSMUTAÇÃO EM CURSO // ATIVA",
            queue = "CORTEJO SAGRADO",
            settings = "TABUAS ESMERALDA",
            hermesStatus = "ORÁCULO DESPERTO"
        )
    )

    val NeuralViolet = VaultThemeConfig(
        id = VaultThemeId.NEURAL_VIOLET,
        displayName = "NEURAL VIOLET // OLYMPIA",
        badgeTag = "BIOLUMINESCENT VOID",
        description = "Bioluminescência quântica: violeta ultravioleta, magenta laser e sintetizadores da meia-noite.",
        primaryAccent = NeuralPurple,
        accentGlow = NeuralPurpleGlow,
        secondaryAccent = BiolumMagenta,
        tertiaryAccent = NeuralPurple,
        backgroundColor = NeuralVoid,
        surfaceColor = NeuralSurface,
        cardBorderColor = NeuralBorder,
        textPrimary = Color(0xFFFAF5FF),
        textMuted = Color(0xFF9CA3AF),
        cardCornerRadius = 14.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.SANS_SERIF,
        layoutShape = VaultLayoutShape.DIVINE_CURVES,
        lexicon = VaultLexicon(
            brandHeader = "NEURAL // VIOLET",
            brandSub = "QUANTUM SYNAPSE AUDIO",
            libraryTitle = "REDE NEURAL DE ÁUDIO",
            foldersTitle = "CLUSTERS & NODOS",
            favoritesTitle = "IMPULSOS PRIORITÁRIOS",
            streakTitle = "SINCRONIA SINÁPTICA 100%",
            offlineBadge = "REDE DESCONECTADA // LOCAL",
            petName = "HERMES // NÚCLEO BIOLUMINESCENTE",
            petSubtitle = "FEEDBACK COGNITIVO • PROCESSAMENTO OFFLINE",
            nowPlaying = "RESONÂNCIA ATIVA",
            queue = "BUFFER SINÁPTICO",
            settings = "PARÂMETROS NEURAIS",
            hermesStatus = "SINAPSE ATIVA"
        )
    )

    val CyberMintMatrix = VaultThemeConfig(
        id = VaultThemeId.CYBER_MINT_MATRIX,
        displayName = "CYBER-MINT MATRIX",
        badgeTag = "TERMINAL ZERO // GREEN",
        description = "Terminal hacker militar air-gapped: verde fósforo esmeralda, CRT escuro e precisão cirúrgica.",
        primaryAccent = MatrixMint,
        accentGlow = MatrixMintGlow,
        secondaryAccent = EmeraldTerminal,
        tertiaryAccent = MatrixMint,
        backgroundColor = MatrixVoid,
        surfaceColor = MatrixSurface,
        cardBorderColor = MatrixBorder,
        textPrimary = Color(0xFFECFDF5),
        textMuted = Color(0xFF6EE7B7).copy(alpha = 0.6f),
        cardCornerRadius = 2.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        lexicon = VaultLexicon(
            brandHeader = "MATRIX // ROOT@VAULT",
            brandSub = "AIR-GAPPED AUDIO KERNEL",
            libraryTitle = "ROOT_FS // AUDIO_STORAGE",
            foldersTitle = "DIRETÓRIOS_LOCAIS",
            favoritesTitle = "FAVORITOS_ROOT [CHMOD 777]",
            streakTitle = "UPTIME 99.999% SEM FALHAS",
            offlineBadge = "KERNEL_ISOLATED",
            petName = "DAEMON // HERMES_SYS_v2",
            petSubtitle = "PROCESSO PRIVILEGIADO • ZERO SINK",
            nowPlaying = "EXEC_AUDIO_STREAMING",
            queue = "QUEUE_FIFO_BUFFER",
            settings = "SYSCTL_CONFIG",
            hermesStatus = "DAEMON ACTIVE"
        )
    )

    val AllThemes = listOf(
        Classic,
        ArasakaPantheon,
        DelamainAiCore,
        NeoOlympusGold,
        NeuralViolet,
        CyberMintMatrix
    )

    fun fromId(id: String): VaultThemeConfig {
        return when (id.uppercase()) {
            VaultThemeId.ARASAKA_PANTHEON.name, "ARASAKA" -> ArasakaPantheon
            VaultThemeId.DELAMAIN_AI_CORE.name, "DELAMAIN", "DELAMAIN_AI" -> DelamainAiCore
            VaultThemeId.NEO_OLYMPUS_GOLD.name, "OLYMPUS", "GOLD" -> NeoOlympusGold
            VaultThemeId.NEURAL_VIOLET.name, "VIOLET", "NEURAL" -> NeuralViolet
            VaultThemeId.CYBER_MINT_MATRIX.name, "MATRIX", "MINT" -> CyberMintMatrix
            else -> Classic
        }
    }
}

val LocalVaultTheme = staticCompositionLocalOf { VaultThemes.Classic }
