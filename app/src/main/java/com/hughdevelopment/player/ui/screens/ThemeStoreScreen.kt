package com.hughdevelopment.player.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.FormatShapes
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.ui.components.SovereignVoidBackdrop
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.components.quartzGlassCapsule
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VaultFontCategory
import com.hughdevelopment.player.ui.theme.VaultLayoutShape
import com.hughdevelopment.player.ui.theme.VaultThemeConfig
import com.hughdevelopment.player.ui.theme.VaultThemes

@Composable
fun ThemeStoreScreen(
    settings: SettingsState,
    onSelectTheme: (String) -> Unit,
    onChangeLibraryAlpha: (Float) -> Unit,
    onChangePlayerAlpha: (Float) -> Unit,
    onToggleFade: (Boolean) -> Unit,
    onChangeFadeDuration: (Float) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = LocalVaultTheme.current
    val allThemes = VaultThemes.AllThemes

    var selectedSection by remember { mutableStateOf("CATALOGO") } // "CATALOGO" ou "CUSTOMIZACAO"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Fundo Divino Obsidian
        SovereignVoidBackdrop(
            showCybersigil = true,
            auraScale = 1.0f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .quartzGlassCapsule(
                                cornerRadius = currentTheme.cardCornerRadius.coerceAtLeast(8.dp),
                                accentColor = currentTheme.primaryAccent
                            )
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SigilWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "LOJA DE TEMAS // PANTHEON",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = currentTheme.primaryAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "CATÁLOGO VISUAL & CUSTOMIZAÇÃO SOBERANA",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = currentTheme.textMuted
                        )
                    }
                }

                // Ícone da paleta
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            accentColor = currentTheme.secondaryAccent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = currentTheme.secondaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seletor de Seções (Catálogo / Customização Manual)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isCatalog = selectedSection == "CATALOGO"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = if (isCatalog) currentTheme.primaryAccent.copy(alpha = 0.22f) else Color(0x10FFFFFF),
                            accentColor = if (isCatalog) currentTheme.primaryAccent else Color.Transparent
                        )
                        .clickable { selectedSection = "CATALOGO" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ColorLens,
                            contentDescription = null,
                            tint = if (isCatalog) SigilWhite else SigilWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEMAS PRÉ-DEFINIDOS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isCatalog) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isCatalog) SigilWhite else SigilWhite.copy(alpha = 0.6f)
                        )
                    }
                }

                val isCustom = selectedSection == "CUSTOMIZACAO"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = if (isCustom) currentTheme.secondaryAccent.copy(alpha = 0.22f) else Color(0x10FFFFFF),
                            accentColor = if (isCustom) currentTheme.secondaryAccent else Color.Transparent
                        )
                        .clickable { selectedSection = "CUSTOMIZACAO" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = if (isCustom) SigilWhite else SigilWhite.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CUSTOMIZAR MANUAL",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isCustom) SigilWhite else SigilWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedSection) {
                "CATALOGO" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
                    ) {
                        items(allThemes, key = { it.id.name }) { themeItem ->
                            val isCurrent = themeItem.id.name.equals(settings.activeThemeId, ignoreCase = true)
                            ThemeCatalogCard(
                                themeConfig = themeItem,
                                isCurrent = isCurrent,
                                onApply = {
                                    onSelectTheme(themeItem.id.name)
                                    Toast.makeText(context, "Tema ${themeItem.displayName} aplicado!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                "CUSTOMIZACAO" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
                    ) {
                        item {
                            CustomizationWallpapersCard(
                                libraryAlpha = settings.libraryWallpaperAlpha,
                                playerAlpha = settings.playerWallpaperAlpha,
                                onLibraryAlphaChange = onChangeLibraryAlpha,
                                onPlayerAlphaChange = onChangePlayerAlpha
                            )
                        }

                        item {
                            CustomizationFadeCard(
                                isFadeEnabled = settings.isFadeEnabled,
                                fadeDuration = settings.fadeDurationSeconds,
                                onToggleFade = onToggleFade,
                                onChangeFadeDuration = onChangeFadeDuration
                            )
                        }

                        item {
                            CustomizationTypographyCard(
                                currentFont = currentTheme.fontCategory
                            )
                        }

                        item {
                            CustomizationGeometryCard(
                                currentShape = currentTheme.layoutShape,
                                cornerRadius = currentTheme.cardCornerRadius
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card vitrine para cada tema da loja
 */
@Composable
fun ThemeCatalogCard(
    themeConfig: VaultThemeConfig,
    isCurrent: Boolean,
    onApply: () -> Unit
) {
    val activeBorderColor by animateColorAsState(
        targetValue = if (isCurrent) themeConfig.primaryAccent else themeConfig.cardBorderColor.copy(alpha = 0.5f),
        label = "BorderAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = themeConfig.cardCornerRadius.coerceAtLeast(10.dp),
                backgroundColor = themeConfig.surfaceColor.copy(alpha = 0.90f),
                borderWidth = if (isCurrent) 1.8.dp else 1.dp,
                topRimColor = if (isCurrent) themeConfig.primaryAccent.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f),
                bottomRimColor = if (isCurrent) themeConfig.secondaryAccent.copy(alpha = 0.4f) else Color.Transparent
            )
            .padding(16.dp)
    ) {
        Column {
            // Header do tema
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = themeConfig.displayName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = themeConfig.primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = themeConfig.badgeTag,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.secondaryAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                // Amostras de cores
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(themeConfig.primaryAccent))
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(themeConfig.secondaryAccent))
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(themeConfig.backgroundColor).border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descrição da alma visual
            Text(
                text = themeConfig.description,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = themeConfig.textPrimary.copy(alpha = 0.75f),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra Mockup de Visualizer / Acento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(themeConfig.backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isCurrent) 1f else 0.65f)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(themeConfig.primaryAccent, themeConfig.secondaryAccent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botão Aplicar ou Badge Ativo
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = themeConfig.primaryAccent.copy(alpha = 0.25f),
                            accentColor = themeConfig.primaryAccent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = SigilWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEMA EM EXECUÇÃO // ATIVO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SigilWhite,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .quartzGlassCapsule(
                            cornerRadius = 10.dp,
                            backgroundColor = Color(0x1AFFFFFF),
                            accentColor = themeConfig.primaryAccent
                        )
                        .clickable(onClick = onApply),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "APLICAR ESTE TEMA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SigilWhite,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * Customização de Papéis de Parede e Auras
 */
@Composable
fun CustomizationWallpapersCard(
    libraryAlpha: Float,
    playerAlpha: Float,
    onLibraryAlphaChange: (Float) -> Unit,
    onPlayerAlphaChange: (Float) -> Unit
) {
    val theme = LocalVaultTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = theme.cardCornerRadius.coerceAtLeast(10.dp),
                backgroundColor = theme.surfaceColor.copy(alpha = 0.85f),
                borderWidth = 1.dp,
                topRimColor = theme.primaryAccent.copy(alpha = 0.4f)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Wallpaper,
                    contentDescription = null,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PAPÉIS DE PAREDE & AURAS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SigilWhite,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider Opacidade Biblioteca
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AURA DA BIBLIOTECA",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SigilWhite.copy(alpha = 0.8f)
                )
                Text(
                    text = "${(libraryAlpha * 100).toInt()}%",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = theme.primaryAccent
                )
            }
            Slider(
                value = libraryAlpha,
                onValueChange = onLibraryAlphaChange,
                valueRange = 0.05f..0.80f,
                colors = SliderDefaults.colors(
                    thumbColor = theme.primaryAccent,
                    activeTrackColor = theme.primaryAccent,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slider Opacidade Player
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AURA DO PLAYER ALTAR",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SigilWhite.copy(alpha = 0.8f)
                )
                Text(
                    text = "${(playerAlpha * 100).toInt()}%",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = theme.secondaryAccent
                )
            }
            Slider(
                value = playerAlpha,
                onValueChange = onPlayerAlphaChange,
                valueRange = 0.05f..0.85f,
                colors = SliderDefaults.colors(
                    thumbColor = theme.secondaryAccent,
                    activeTrackColor = theme.secondaryAccent,
                    inactiveTrackColor = Color(0x33FFFFFF)
                )
            )
        }
    }
}

/**
 * Customização de Fade / Transições
 */
@Composable
fun CustomizationFadeCard(
    isFadeEnabled: Boolean,
    fadeDuration: Float,
    onToggleFade: (Boolean) -> Unit,
    onChangeFadeDuration: (Float) -> Unit
) {
    val theme = LocalVaultTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = theme.cardCornerRadius.coerceAtLeast(10.dp),
                backgroundColor = theme.surfaceColor.copy(alpha = 0.85f),
                borderWidth = 1.dp,
                topRimColor = theme.secondaryAccent.copy(alpha = 0.4f)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FADE SUAVE // CROSSFADE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SigilWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Transição acústica suave entre faixas",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = theme.textMuted
                    )
                }

                Switch(
                    checked = isFadeEnabled,
                    onCheckedChange = onToggleFade,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SigilWhite,
                        checkedTrackColor = theme.primaryAccent,
                        uncheckedThumbColor = Color(0xFF6B7280),
                        uncheckedTrackColor = Color(0x33FFFFFF)
                    )
                )
            }

            if (isFadeEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DURAÇÃO DO FADE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SigilWhite.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f s", fadeDuration),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = theme.primaryAccent
                    )
                }
                Slider(
                    value = fadeDuration,
                    onValueChange = onChangeFadeDuration,
                    valueRange = 0.5f..5.0f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primaryAccent,
                        activeTrackColor = theme.primaryAccent,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }
        }
    }
}

/**
 * Customização de Tipografia & Letras
 */
@Composable
fun CustomizationTypographyCard(
    currentFont: VaultFontCategory
) {
    val theme = LocalVaultTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = theme.cardCornerRadius.coerceAtLeast(10.dp),
                backgroundColor = theme.surfaceColor.copy(alpha = 0.85f),
                borderWidth = 1.dp,
                topRimColor = theme.primaryAccent.copy(alpha = 0.35f)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.FormatShapes,
                    contentDescription = null,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ESTILO DE LETRAS // TIPOGRAFIA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SigilWhite,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A tipografia é otimizada por tema, refletindo o caráter neoclássico, militar Arasaka ou cockpit IA Delamain.",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SigilWhite.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fonts = listOf(
                    "MONOSPACE" to VaultFontCategory.MONOSPACE,
                    "SANS-SERIF" to VaultFontCategory.SANS_SERIF,
                    "SERIF CLÁSSICO" to VaultFontCategory.SERIF
                )
                fonts.forEach { (label, cat) ->
                    val isSelected = currentFont == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .quartzGlassCapsule(
                                cornerRadius = 8.dp,
                                backgroundColor = if (isSelected) theme.primaryAccent.copy(alpha = 0.25f) else Color(0x10FFFFFF),
                                accentColor = if (isSelected) theme.primaryAccent else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = when (cat) {
                                VaultFontCategory.MONOSPACE -> FontFamily.Monospace
                                VaultFontCategory.SERIF -> FontFamily.Serif
                                else -> FontFamily.SansSerif
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 9.sp,
                            color = if (isSelected) SigilWhite else SigilWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Customização de Geometria de Layout
 */
@Composable
fun CustomizationGeometryCard(
    currentShape: VaultLayoutShape,
    cornerRadius: androidx.compose.ui.unit.Dp
) {
    val theme = LocalVaultTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = theme.cardCornerRadius.coerceAtLeast(10.dp),
                backgroundColor = theme.surfaceColor.copy(alpha = 0.85f),
                borderWidth = 1.dp,
                topRimColor = theme.secondaryAccent.copy(alpha = 0.35f)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = null,
                    tint = theme.secondaryAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GEOMETRIA DE LAYOUT // BORDAS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SigilWhite,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Geometria ativa: ${theme.layoutShape.name} (Curvatura de $cornerRadius). Segue a regra áurea do ACDS.",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SigilWhite.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shapes = listOf(
                    "DIVINO (14dp)" to VaultLayoutShape.DIVINE_CURVES,
                    "BRUTAL (4dp)" to VaultLayoutShape.BRUTALIST_SHARP,
                    "MINIMAL (8dp)" to VaultLayoutShape.NEO_MINIMAL
                )
                shapes.forEach { (label, shape) ->
                    val isSelected = currentShape == shape
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .quartzGlassCapsule(
                                cornerRadius = 8.dp,
                                backgroundColor = if (isSelected) theme.secondaryAccent.copy(alpha = 0.25f) else Color(0x10FFFFFF),
                                accentColor = if (isSelected) theme.secondaryAccent else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 9.sp,
                            color = if (isSelected) SigilWhite else SigilWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
