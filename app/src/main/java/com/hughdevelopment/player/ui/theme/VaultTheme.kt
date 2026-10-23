package com.hughdevelopment.player.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class VaultThemeId {
    AETHER_CLASSIC,
    ARASAKA_PANTHEON
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
    val petSubtitle: String
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
    val lexicon: VaultLexicon
)

object VaultThemes {

    val Classic = VaultThemeConfig(
        id = VaultThemeId.AETHER_CLASSIC,
        displayName = "AETHER CLASSIC",
        badgeTag = "NEOCLASSICAL GLASS",
        description = "Vidro fosco estilo Apple, anéis azuis Klein e estatuária clássica dither.",
        primaryAccent = KleinBlue,
        accentGlow = KleinBlueGlow,
        secondaryAccent = CyberAmber,
        tertiaryAccent = HermeticCyan,
        backgroundColor = VoidBlack,
        surfaceColor = CharcoalSurface,
        cardBorderColor = WireframeBorder,
        textPrimary = SigilWhite,
        textMuted = MutedStone,
        cardCornerRadius = 10.dp,
        isBrutalistSharp = false,
        lexicon = VaultLexicon(
            brandHeader = "AETHER // VAULT",
            brandSub = "SOVEREIGN OFFLINE AUDIO",
            libraryTitle = "BIBLIOTECA",
            foldersTitle = "PASTAS & PLAYLISTS",
            favoritesTitle = "FAVORITAS",
            streakTitle = "OFENSIVA SOBERANA",
            offlineBadge = "100% OFFLINE",
            petName = "HERMES // COMPANHEIRO IA",
            petSubtitle = "IA SOBERANA 100% OFFLINE • ZERO CLOUD"
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
        cardCornerRadius = 0.dp,
        isBrutalistSharp = true,
        lexicon = VaultLexicon(
            brandHeader = "荒坂 // ARASAKA PANTHEON",
            brandSub = "數據庫 // DATA VAULT SECURE",
            libraryTitle = "COFRE DE DADOS (數據庫)",
            foldersTitle = "SETORES CORPORATIVOS",
            favoritesTitle = "PROTOCOLOS DIVINOS // TIER 0",
            streakTitle = "SINCRONIA DE REDE 99.8%",
            offlineBadge = "未接続 // AIR-GAPPED",
            petName = "HERMES-01 // IA DE CONFORMIDADE ARASAKA",
            petSubtitle = "DIRETRIZ CORPORATIVA • ZERO VAZAMENTO"
        )
    )

    val AllThemes = listOf(Classic, ArasakaPantheon)

    fun fromId(id: String): VaultThemeConfig {
        return when (id.uppercase()) {
            VaultThemeId.ARASAKA_PANTHEON.name -> ArasakaPantheon
            else -> Classic
        }
    }
}

val LocalVaultTheme = staticCompositionLocalOf { VaultThemes.Classic }
