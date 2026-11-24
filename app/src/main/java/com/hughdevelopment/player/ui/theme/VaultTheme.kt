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
    CYBER_MINT_MATRIX,

    // 10 Curated Model Themes (Pantheon Theme Store v1.9.0)
    DOGE_SOVEREIGN,
    WIN95_VAPORWAVE,
    TERRA_BRASILIS,
    MONOLITH_NULL,
    ACID_PROTOCOL,
    ELDRITCH_CATHEDRAL,
    APOLLO_1970,
    NEO_KYOTO_ZERO,
    WEYLAND_DEEP_SPACE,
    SOVEREIGN_ARCHON
}

enum class VaultWallpaperStyle {
    SACRED_CYBERSIGIL,
    DOGE_MEME_ALTAR,
    WIN95_VAPORWAVE_CRT,
    TERRA_BRASILIS_CONCRETE,
    MONOLITH_NULL_ECLIPSE,
    ACID_HAZARD_RADIATION,
    ELDRITCH_GOTHIC_CATHEDRAL,
    APOLLO_ANALOG_COCKPIT,
    NEO_KYOTO_CARBON_KATANA,
    WEYLAND_DEEP_SPACE_HUD,
    SOVEREIGN_ARCHON_CELESTIAL
}

enum class VaultVisualizerStyle {
    HARMONIC_SPECTRUM,
    DOGE_PAWS_BUBBLE,
    WIN95_PIXEL_BLOCKS,
    ACID_GEIGER_SPIKES,
    APOLLO_VU_METERS,
    KATANA_SLICES,
    NULL_HAIRLINE,
    ARCHON_GOLDEN_HALO
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
    val hermesStatus: String = "HERMES // STATUS",
    val greetingPhrase: String = "Soberania acústica 100% offline ativada."
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
    val wallpaperStyle: VaultWallpaperStyle = VaultWallpaperStyle.SACRED_CYBERSIGIL,
    val visualizerStyle: VaultVisualizerStyle = VaultVisualizerStyle.HARMONIC_SPECTRUM,
    val iconEmoji: String = "🏛️",
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

    // =========================================================================
    // 10 MODEL THEMES (AETHER VAULT v1.9.0 PANTHEON THEME STORE)
    // =========================================================================

    // 1. TIER MEME: Doge Soberano // Much Audio, Very Vault
    val DogeSovereign = VaultThemeConfig(
        id = VaultThemeId.DOGE_SOVEREIGN,
        displayName = "DOGE SOBERANO",
        badgeTag = "TIER 1 // MEME SOBERANO",
        description = "Estátua clássica romana com rosto do Shiba Inu, coroa de louros neon e hieróglifos dithered. Much bitrate, very lossless, so sovereign.",
        primaryAccent = DogeYellow,
        accentGlow = DogeYellow.copy(alpha = 0.40f),
        secondaryAccent = DogeGold,
        tertiaryAccent = CyberAmber,
        backgroundColor = DogeDark,
        surfaceColor = DogeSurface,
        cardBorderColor = DogeBorder,
        textPrimary = SigilWhite,
        textMuted = DogeGold.copy(alpha = 0.70f),
        cardCornerRadius = 22.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.SANS_SERIF,
        layoutShape = VaultLayoutShape.DIVINE_CURVES,
        wallpaperStyle = VaultWallpaperStyle.DOGE_MEME_ALTAR,
        visualizerStyle = VaultVisualizerStyle.DOGE_PAWS_BUBBLE,
        iconEmoji = "🐕",
        lexicon = VaultLexicon(
            brandHeader = "DOGE // VAULT SOBERANO",
            brandSub = "MUCH BITRATE • VERY LOSSLESS",
            libraryTitle = "COFRE DOS BONS GAROTOS",
            foldersTitle = "ALBUNS & LATIDOS",
            favoritesTitle = "PETISCOS DE OURO (FAVORITAS)",
            streakTitle = "OFENSIVA CANINA 100% FIEL",
            offlineBadge = "SUCH OFFLINE // ZERO CLOUD",
            petName = "DOGE // CÉSAR SOBERANO",
            petSubtitle = "WOW • VERY AUDIO • MUCH FAITHFUL",
            nowPlaying = "TOCANDO MUITO // WOW",
            queue = "FILA DOS BONS SONS",
            settings = "CONFIGURAÇÕES MUCHO BOAS",
            hermesStatus = "DOGE ONLINE",
            greetingPhrase = "Wow. Such lossless. Very 96kHz. So sovereign. Much audio!"
        )
    )

    // 2. TIER NOSTALGIA GLITCH: Win95 Vaporwave // BIOS Altar
    val Win95Vaporwave = VaultThemeConfig(
        id = VaultThemeId.WIN95_VAPORWAVE,
        displayName = "WIN95 VAPORWAVE",
        badgeTag = "TIER 2 // NOSTALGIA GLITCH",
        description = "Vênus de Milo em fita VHS degradada, grelha quadriculada infinita em magenta/ciano, scanlines CRT e janelas beveled 3D.",
        primaryAccent = VaporMagenta,
        accentGlow = VaporMagenta.copy(alpha = 0.45f),
        secondaryAccent = VaporCyan,
        tertiaryAccent = Win95Gray,
        backgroundColor = Win95Void,
        surfaceColor = Win95Surface,
        cardBorderColor = Win95Border,
        textPrimary = SigilWhite,
        textMuted = VaporCyan.copy(alpha = 0.75f),
        cardCornerRadius = 2.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        wallpaperStyle = VaultWallpaperStyle.WIN95_VAPORWAVE_CRT,
        visualizerStyle = VaultVisualizerStyle.WIN95_PIXEL_BLOCKS,
        iconEmoji = "📺",
        lexicon = VaultLexicon(
            brandHeader = "SYSTEM95 // VAPOR_ALTAR",
            brandSub = "C:\\AETHER\\AUDIO.EXE",
            libraryTitle = "EXPLORADOR DE ARQUIVOS (A:)",
            foldersTitle = "DIRETÓRIOS DO SISTEMA",
            favoritesTitle = "ATALHOS DA ÁREA DE TRABALHO",
            streakTitle = "UPTIME DO WINDOWS 95",
            offlineBadge = "DISQUETE CARREGADO",
            petName = "HERMES.EXE // WIZARD 95",
            petSubtitle = "SISTEMA OPERACIONAL RETRO • 16-BIT AUDIO",
            nowPlaying = "EXEC_MEDIA_PLAYER.DLL",
            queue = "BUFFER DE COMANDOS FIFO",
            settings = "PAINEL DE CONTROLE",
            hermesStatus = "WIN95 PRONTO",
            greetingPhrase = "SISTEMA INICIALIZADO. DISQUETE DE ÁUDIO INSERIDO NO DRIVE A:. EXECUTANDO TRACK.SYS COM SUCESSO."
        )
    )

    // 3. TIER TROPICAL BRUTALIST: Terra Brasilis // Concreto & Floresta
    val TerraBrasilis = VaultThemeConfig(
        id = VaultThemeId.TERRA_BRASILIS,
        displayName = "TERRA BRASILIS",
        badgeTag = "TIER 3 // TROPICAL BRUTALIST",
        description = "Arquitetura brutalista modernista (Niemeyer/Lina Bo Bardi) fundida com xilogravura de raízes de figueira e samambaias tropicais.",
        primaryAccent = TerraEmerald,
        accentGlow = TerraEmerald.copy(alpha = 0.40f),
        secondaryAccent = TerraCotta,
        tertiaryAccent = Color(0xFFFBBF24),
        backgroundColor = ConcreteDark,
        surfaceColor = ConcreteSurface,
        cardBorderColor = ConcreteBorder,
        textPrimary = Color(0xFFF0FDF4),
        textMuted = Color(0xFFA7F3D0).copy(alpha = 0.70f),
        cardCornerRadius = 8.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.SANS_SERIF,
        layoutShape = VaultLayoutShape.NEO_MINIMAL,
        wallpaperStyle = VaultWallpaperStyle.TERRA_BRASILIS_CONCRETE,
        visualizerStyle = VaultVisualizerStyle.HARMONIC_SPECTRUM,
        iconEmoji = "🇧🇷",
        lexicon = VaultLexicon(
            brandHeader = "TERRA BRASILIS // CONCRETO & MATA",
            brandSub = "BRUTALISMO TROPICAL AUDIÓFILO",
            libraryTitle = "ACERVO NACIONAL SOBERANO",
            foldersTitle = "RESERVAS & BIOMAS SONOROS",
            favoritesTitle = "RELÍQUIAS DA TERRA",
            streakTitle = "RESISTÊNCIA & DISCIPLINA",
            offlineBadge = "100% NACIONAL // INDEPENDENTE",
            petName = "HERMES // VOZ DO CONCRETO",
            petSubtitle = "ACÚSTICA ORGÂNICA NACIONAL • BRUTALISMO PURO",
            nowPlaying = "RESSONÂNCIA TROPICAL ATIVA",
            queue = "CORTEJO DOS RITMOS",
            settings = "PARÂMETROS DA OBRA",
            hermesStatus = "FLORESTA VIVA",
            greetingPhrase = "Saudações brasileiras. Concreto armado e raízes profundas protegem seu acervo. Acústica orgânica nacional ativa."
        )
    )

    // 4. TIER ABYSSAL MINIMAL: Monolith Null // Vantablack Zero
    val MonolithNull = VaultThemeConfig(
        id = VaultThemeId.MONOLITH_NULL,
        displayName = "MONOLITH NULL",
        badgeTag = "TIER 4 // ABYSSAL MINIMAL",
        description = "Silêncio absoluto em 100% preto OLED, com aro solar ultravioleta ultrafino pulsante. Zero bordas, faixas flutuando no vácuo.",
        primaryAccent = NullWhite,
        accentGlow = NullUltraviolet.copy(alpha = 0.35f),
        secondaryAccent = NullUltraviolet,
        tertiaryAccent = NullTitanium,
        backgroundColor = NullOled,
        surfaceColor = NullSurface,
        cardBorderColor = NullBorder,
        textPrimary = NullWhite,
        textMuted = NullTitanium,
        cardCornerRadius = 0.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        wallpaperStyle = VaultWallpaperStyle.MONOLITH_NULL_ECLIPSE,
        visualizerStyle = VaultVisualizerStyle.NULL_HAIRLINE,
        iconEmoji = "🕳️",
        lexicon = VaultLexicon(
            brandHeader = "NULL // 000000",
            brandSub = "VANTABLACK OBSIDIAN VOID",
            libraryTitle = "VÁCUO SONORO [ZERO]",
            foldersTitle = "PARTIÇÕES ABISSAIS",
            favoritesTitle = "SINGULARIDADES PURAS",
            streakTitle = "DISCIPLINA DO SILÊNCIO",
            offlineBadge = "AIR-GAPPED NULL",
            petName = "HERMES // SINGULARIDADE 0",
            petSubtitle = "SILÊNCIO RADICAL • ZERO DISTRACTION",
            nowPlaying = "FREQ // TRANSMISSÃO VÁCUO",
            queue = "SEQUÊNCIA ESSENCIAL",
            settings = "PARÂMETROS ZERO",
            hermesStatus = "PONTO ZERO",
            greetingPhrase = "Silêncio puro. Vácuo absoluto. Apenas a frequência essencial do som."
        )
    )

    // 5. TIER ACID PROTOCOL: Biohazard Containment // Radiação Gamma
    val AcidProtocol = VaultThemeConfig(
        id = VaultThemeId.ACID_PROTOCOL,
        displayName = "ACID PROTOCOL",
        badgeTag = "TIER 5 // RADIAÇÃO GAMMA",
        description = "Sarcófago industrial de contenção nuclear, listras de quarentena militar, cantos chanfrados a 45º e medidor Geiger verde tóxico.",
        primaryAccent = AcidGreen,
        accentGlow = AcidGreen.copy(alpha = 0.45f),
        secondaryAccent = HazardYellow,
        tertiaryAccent = AcidGreen,
        backgroundColor = RadiationVoid,
        surfaceColor = RadiationSurface,
        cardBorderColor = RadiationBorder,
        textPrimary = Color(0xFFF0FDF4),
        textMuted = AcidGreen.copy(alpha = 0.65f),
        cardCornerRadius = 4.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        wallpaperStyle = VaultWallpaperStyle.ACID_HAZARD_RADIATION,
        visualizerStyle = VaultVisualizerStyle.ACID_GEIGER_SPIKES,
        iconEmoji = "☣️",
        lexicon = VaultLexicon(
            brandHeader = "BIOHAZARD // RAD-09",
            brandSub = "CONTENÇÃO GAMMA ATIVA",
            libraryTitle = "SARCÓFAGO DE ÁUDIO BLINDADO",
            foldersTitle = "ZONAS DE QUARENTENA",
            favoritesTitle = "AMOSTRAS ISOTÓPICAS PURAS",
            streakTitle = "ESTABILIDADE DO NÚCLEO 100%",
            offlineBadge = "ISOLAMENTO NÍVEL 4",
            petName = "HERMES-RAD // MEDIDOR GEIGER",
            petSubtitle = "ISÓTOPO SONORO ESTÁVEL • PROTOCOLO BIOHAZARD",
            nowPlaying = "RADIAÇÃO ACÚSTICA // EMISSÃO",
            queue = "CÂMARA DE REAÇÃO",
            settings = "PROTOCOLO DE DESCONTAMINAÇÃO",
            hermesStatus = "NÚCLEO ESTÁVEL",
            greetingPhrase = "ALERTA: NÍVEL DE RADIAÇÃO SONORA ESTÁVEL. CONTENÇÃO BIOLÓGICA OPERACIONAL NO COFRE."
        )
    )

    // 6. TIER SOULS-LIKE GÓTICO: Eldritch Cathedral // Blood & Ash
    val EldritchCathedral = VaultThemeConfig(
        id = VaultThemeId.ELDRITCH_CATHEDRAL,
        displayName = "ELDRITCH CATHEDRAL",
        badgeTag = "TIER 6 // SOULS-LIKE GÓTICO",
        description = "Catedral gótica monumental com vitrais trincados, lua sangrenta banhando gárgulas cibernéticas, ferro forjado e runas escuras.",
        primaryAccent = EldritchBlood,
        accentGlow = EldritchBlood.copy(alpha = 0.45f),
        secondaryAccent = EldritchGold,
        tertiaryAccent = Color(0xFF991B1B),
        backgroundColor = CathedralVoid,
        surfaceColor = CathedralSurface,
        cardBorderColor = CathedralBorder,
        textPrimary = Color(0xFFFFF1F2),
        textMuted = Color(0xFFFDA4AF).copy(alpha = 0.70f),
        cardCornerRadius = 6.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.SERIF,
        layoutShape = VaultLayoutShape.DIVINE_CURVES,
        wallpaperStyle = VaultWallpaperStyle.ELDRITCH_GOTHIC_CATHEDRAL,
        visualizerStyle = VaultVisualizerStyle.KATANA_SLICES,
        iconEmoji = "⚔️",
        lexicon = VaultLexicon(
            brandHeader = "CATHEDRAL // BLOOD & ASH",
            brandSub = "ALTAR DAS RUNAS PROIBIDAS",
            libraryTitle = "GRIMÓRIO DE SANGUE SONORO",
            foldersTitle = "CRIPTAS & MAUSOLÉUS",
            favoritesTitle = "OFERENDAS DE SANGUE",
            streakTitle = "CHAMA DA FOGUEIRA ACESA",
            offlineBadge = "SANTUÁRIO LACRADO",
            petName = "HERMES // GUARDIÃO DAS CINZAS",
            petSubtitle = "CATEDRAL DO SANGUE • RUNAS ACÚSTICAS",
            nowPlaying = "CANTO SAGRADO // EM CURSO",
            queue = "LITURGIA OBSCURA",
            settings = "RITUAIS & RUNAS",
            hermesStatus = "FOGUEIRA VIVA",
            greetingPhrase = "As cinzas despertam do esquecimento. Que os sinos desta catedral gótica ressoem através da noite eterna."
        )
    )

    // 7. TIER HARDWARE RETRO-FUTURIST: Apollo-NASA 1970 // Cockpit Analógico
    val Apollo1970 = VaultThemeConfig(
        id = VaultThemeId.APOLLO_1970,
        displayName = "APOLLO-NASA 1970",
        badgeTag = "TIER 7 // COCKPIT ANALÓGICO",
        description = "Painel de relés analógicos, papel milimetrado âmbar, lâmpadas de tungstênio, botões industriais com parafusos e display VFD.",
        primaryAccent = ApolloAmber,
        accentGlow = ApolloAmber.copy(alpha = 0.40f),
        secondaryAccent = ApolloVfdGreen,
        tertiaryAccent = ApolloAmber,
        backgroundColor = CockpitVoid,
        surfaceColor = CockpitSurface,
        cardBorderColor = CockpitBorder,
        textPrimary = Color(0xFFFFFBEB),
        textMuted = ApolloAmber.copy(alpha = 0.75f),
        cardCornerRadius = 3.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        wallpaperStyle = VaultWallpaperStyle.APOLLO_ANALOG_COCKPIT,
        visualizerStyle = VaultVisualizerStyle.APOLLO_VU_METERS,
        iconEmoji = "🚀",
        lexicon = VaultLexicon(
            brandHeader = "NASA // APOLLO GUIDANCE COMPUTER",
            brandSub = "SATURN-V AUDIO TELEMETRY",
            libraryTitle = "BANCO DE DADOS DE VOO (AGC)",
            foldersTitle = "MÓDULOS DE COMANDO",
            favoritesTitle = "TRAJETÓRIAS PRIORITÁRIAS",
            streakTitle = "MISSÃO CUMPRIDA // T+ DIAS",
            offlineBadge = "TELEMETRIA AIR-GAPPED",
            petName = "CAPCOM // HERMES NASA",
            petSubtitle = "TELEMETRIA DE CABINE • SATURN V AUDIO KERNEL",
            nowPlaying = "TRANSMISSÃO DE CABINE // ATIVA",
            queue = "REGISTRO DE COMANDOS DSKY",
            settings = "PAINEL DE DISJUNTORES",
            hermesStatus = "TELEMETRIA OK",
            greetingPhrase = "Houston, temos áudio. Telemetria da cabine confirmada em 100%. Todos os sistemas analógicos prontos para ignição."
        )
    )

    // 8. TIER CYBER-SHOGUNATE: Neo-Kyoto Zero // Lâmina de Carbono
    val NeoKyotoZero = VaultThemeConfig(
        id = VaultThemeId.NEO_KYOTO_ZERO,
        displayName = "NEO-KYOTO ZERO",
        badgeTag = "TIER 8 // LÂMINA DE CARBONO",
        description = "Samurai cibernético em armadura de fibra de carbono polida, cortes de luz neon carmesim, máscara Hannya e visualizer lâmina térmica.",
        primaryAccent = KatanaCrimson,
        accentGlow = KatanaCrimson.copy(alpha = 0.45f),
        secondaryAccent = KyotoGold,
        tertiaryAccent = KatanaCrimson,
        backgroundColor = CarbonVoid,
        surfaceColor = CarbonSurface,
        cardBorderColor = CarbonBorder,
        textPrimary = Color(0xFFFFF1F2),
        textMuted = Color(0xFFFDA4AF).copy(alpha = 0.70f),
        cardCornerRadius = 4.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        wallpaperStyle = VaultWallpaperStyle.NEO_KYOTO_CARBON_KATANA,
        visualizerStyle = VaultVisualizerStyle.KATANA_SLICES,
        iconEmoji = "⛩️",
        lexicon = VaultLexicon(
            brandHeader = "京都 // NEO-KYOTO ZERO",
            brandSub = "LÂMINA DE CARBONO // BUSHIDO",
            libraryTitle = "ARSENAL DE FREQUÊNCIAS (刀)",
            foldersTitle = "DOJÔS & CLÃS",
            favoritesTitle = "LÂMINAS DE HONRA (名刀)",
            streakTitle = "DISCIPLINA SAMURAI 100%",
            offlineBadge = "SHOGUNATO FECHADO (鎖国)",
            petName = "HERMES // ESPÍRITO HANNYA",
            petSubtitle = "HONRA & FIBRA DE CARBONO • SILÊNCIO LETAL",
            nowPlaying = "CORTE ACÚSTICO // ATIVO",
            queue = "SEQUÊNCIA DE KATAS",
            settings = "CÓDIGO BUSHIDO",
            hermesStatus = "LÂMINA AFIADA",
            greetingPhrase = "A lâmina desembainhada e a frequência sonora são uma só. Precisão cirúrgica sem hesitação."
        )
    )

    // 9. TIER SCI-FI CORP: Weyland-Yutani // Deep Space Vessel
    val WeylandDeepSpace = VaultThemeConfig(
        id = VaultThemeId.WEYLAND_DEEP_SPACE,
        displayName = "WEYLAND-YUTANI",
        badgeTag = "TIER 9 // DEEP SPACE VESSEL",
        description = "Corredores herméticos de nave cargueira espacial profunda, comportas industriais pressurizadas, aço escovado e dados militares HUD.",
        primaryAccent = WeylandCyan,
        accentGlow = WeylandCyan.copy(alpha = 0.40f),
        secondaryAccent = AirlockOrange,
        tertiaryAccent = WeylandCyan,
        backgroundColor = HullVoid,
        surfaceColor = HullSurface,
        cardBorderColor = HullBorder,
        textPrimary = Color(0xFFF0F9FF),
        textMuted = WeylandCyan.copy(alpha = 0.70f),
        cardCornerRadius = 4.dp,
        isBrutalistSharp = true,
        fontCategory = VaultFontCategory.MONOSPACE,
        layoutShape = VaultLayoutShape.BRUTALIST_SHARP,
        wallpaperStyle = VaultWallpaperStyle.WEYLAND_DEEP_SPACE_HUD,
        visualizerStyle = VaultVisualizerStyle.HARMONIC_SPECTRUM,
        iconEmoji = "🪐",
        lexicon = VaultLexicon(
            brandHeader = "WEYLAND-YUTANI // CORP",
            brandSub = "BUILDING BETTER WORLDS",
            libraryTitle = "MANIFIESTO DE CARGA // AUDIO",
            foldersTitle = "COMPARTIMENTOS DO CASCO",
            favoritesTitle = "CARGAS VALIOSAS // CLASSE A",
            streakTitle = "DIAS DE HIPERSONO // ESTÁVEL",
            offlineBadge = "AIR-GAPPED STARSHIP",
            petName = "MU-TH-UR 6000 // IA DE BORDO",
            petSubtitle = "BUILDING BETTER WORLDS • ATMOSPHERE 99.8%",
            nowPlaying = "TRANSMISSÃO DE CABINE // USCSS",
            queue = "ROTA DE VOO INTERESTELAR",
            settings = "TERMINAL CENTRAL WEYLAND",
            hermesStatus = "CASCO SEGURO",
            greetingPhrase = "DIRETRIZ ESPECIAL 937: Integridade do casco em 100%. Suporte à vida e buffer de reprodução nominal."
        )
    )

    // 10. TIER SUPREMO / O MAIS PICA: Sovereign Archon // Olympus Ascended
    val SovereignArchon = VaultThemeConfig(
        id = VaultThemeId.SOVEREIGN_ARCHON,
        displayName = "SOVEREIGN ARCHON",
        badgeTag = "TIER 10 // O MAIS PICA SUPREMO",
        description = "Busto colossal de mármore de Carrara partido ao meio por cristal de quartzo emanando luz líquida dourada e astrolábio celestial 3D na Proporção Áurea.",
        primaryAccent = ArchonGold,
        accentGlow = ArchonGold.copy(alpha = 0.50f),
        secondaryAccent = ArchonCyan,
        tertiaryAccent = ArchonGold,
        backgroundColor = ArchonVoid,
        surfaceColor = ArchonSurface,
        cardBorderColor = ArchonBorder,
        textPrimary = Color(0xFFFFFBEB),
        textMuted = Color(0xFFD4AF37).copy(alpha = 0.75f),
        cardCornerRadius = 18.dp,
        isBrutalistSharp = false,
        fontCategory = VaultFontCategory.SERIF,
        layoutShape = VaultLayoutShape.DIVINE_CURVES,
        wallpaperStyle = VaultWallpaperStyle.SOVEREIGN_ARCHON_CELESTIAL,
        visualizerStyle = VaultVisualizerStyle.ARCHON_GOLDEN_HALO,
        iconEmoji = "👑",
        lexicon = VaultLexicon(
            brandHeader = "ARCHON // OLYMPUS ASCENDED",
            brandSub = "ARQUITETURA DA SOBERANIA MÁXIMA",
            libraryTitle = "SANTUÁRIO DO ARCONTE SUPREMO",
            foldersTitle = "PANTHEONS & DOMÍNIOS CÓSMICOS",
            favoritesTitle = "INSÍGNIAS DA ALTA CORTE",
            streakTitle = "SOBERANIA IMPERIAL INCALCULÁVEL",
            offlineBadge = "TRONO SOBERANO // INVIOLÁVEL",
            petName = "ARCONTE // HERMES SUPREMO",
            petSubtitle = "ARQUITETO DA SOBERANIA • CÚPULA DE QUARTZO",
            nowPlaying = "RITUAL SONORO SAGRADO // ATIVO",
            queue = "ORDEM DAS ESFERAS CELESTES",
            settings = "TABUAS DO ARCONTE",
            hermesStatus = "TRONO OCUPADO",
            greetingPhrase = "Saudações solenes, Arconte Supremo. O ritual sonoro está consagrado em ouro 24k e cristal puro de quartzo. Seu domínio acústico é inquestionável."
        )
    )

    val AllThemes = listOf(
        // Featured Pantheon Store 10 Model Themes
        SovereignArchon,
        DogeSovereign,
        Win95Vaporwave,
        TerraBrasilis,
        MonolithNull,
        AcidProtocol,
        EldritchCathedral,
        Apollo1970,
        NeoKyotoZero,
        WeylandDeepSpace,

        // Core Classics
        Classic,
        ArasakaPantheon,
        DelamainAiCore,
        NeoOlympusGold,
        NeuralViolet,
        CyberMintMatrix
    )

    fun fromId(id: String): VaultThemeConfig {
        return when (id.uppercase()) {
            VaultThemeId.SOVEREIGN_ARCHON.name, "ARCHON", "SOVEREIGN_ARCHON", "SUPREMO", "PICA" -> SovereignArchon
            VaultThemeId.DOGE_SOVEREIGN.name, "DOGE", "DOGE_SOVEREIGN", "SHIBA" -> DogeSovereign
            VaultThemeId.WIN95_VAPORWAVE.name, "WIN95", "WIN95_VAPORWAVE", "VAPORWAVE", "SYSTEM95" -> Win95Vaporwave
            VaultThemeId.TERRA_BRASILIS.name, "TERRA", "TERRA_BRASILIS", "BRASILIS", "TROPICAL" -> TerraBrasilis
            VaultThemeId.MONOLITH_NULL.name, "MONOLITH", "MONOLITH_NULL", "NULL", "VANTABLACK", "ZERO" -> MonolithNull
            VaultThemeId.ACID_PROTOCOL.name, "ACID", "ACID_PROTOCOL", "BIOHAZARD", "GAMMA" -> AcidProtocol
            VaultThemeId.ELDRITCH_CATHEDRAL.name, "ELDRITCH", "ELDRITCH_CATHEDRAL", "GOTHIC", "BLOOD_ASH" -> EldritchCathedral
            VaultThemeId.APOLLO_1970.name, "APOLLO", "APOLLO_1970", "NASA", "COCKPIT" -> Apollo1970
            VaultThemeId.NEO_KYOTO_ZERO.name, "KYOTO", "NEO_KYOTO", "NEO_KYOTO_ZERO", "KATANA", "SAMURAI" -> NeoKyotoZero
            VaultThemeId.WEYLAND_DEEP_SPACE.name, "WEYLAND", "WEYLAND_DEEP_SPACE", "ALIEN", "NOSTROMO" -> WeylandDeepSpace

            VaultThemeId.ARASAKA_PANTHEON.name, "ARASAKA" -> ArasakaPantheon
            VaultThemeId.DELAMAIN_AI_CORE.name, "DELAMAIN", "DELAMAIN_AI" -> DelamainAiCore
            VaultThemeId.NEO_OLYMPUS_GOLD.name, "OLYMPUS", "GOLD" -> NeoOlympusGold
            VaultThemeId.NEURAL_VIOLET.name, "VIOLET", "NEURAL" -> NeuralViolet
            VaultThemeId.CYBER_MINT_MATRIX.name, "MATRIX", "MINT" -> CyberMintMatrix
            VaultThemeId.AETHER_CLASSIC.name, "CLASSIC", "AETHER" -> Classic
            else -> Classic
        }
    }
}

val LocalVaultTheme = staticCompositionLocalOf { VaultThemes.Classic }
