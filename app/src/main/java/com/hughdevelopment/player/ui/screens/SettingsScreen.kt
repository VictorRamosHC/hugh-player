package com.hughdevelopment.player.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder

@Composable
fun SettingsScreen(
    settings: SettingsState,
    onBack: () -> Unit,
    onToggleFade: (Boolean) -> Unit,
    onChangeFadeDuration: (Float) -> Unit,
    onChangeLibraryAlpha: (Float) -> Unit,
    onChangePlayerAlpha: (Float) -> Unit,
    onExportAiManifest: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var exportStatusText by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Subtle background texture
        Image(
            painter = painterResource(id = R.drawable.bg_dashboard),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
        )

        CybersigilBackground(showHalo = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = SigilWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "SISTEMA // CONFIGURAÇÕES",
                        color = SigilWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "AJUSTES DE ÁUDIO, ESTÉTICA E PONTE IA",
                        color = CyberAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: AUDIO ENGINE & FADE
            SectionHeader(
                title = "MOTOR DE ÁUDIO & FADE IN / OUT",
                icon = Icons.Default.GraphicEq
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, WireframeBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    // Toggle Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fade Suave na Troca de Músicas",
                                color = SigilWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (settings.isFadeEnabled) "Ativado (Rampa senoidal contínua)" else "Desativado (Corte direto)",
                                color = if (settings.isFadeEnabled) HermeticCyan else MutedStone,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Switch(
                            checked = settings.isFadeEnabled,
                            onCheckedChange = onToggleFade,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VoidBlack,
                                checkedTrackColor = CyberAmber,
                                uncheckedThumbColor = MutedStone,
                                uncheckedTrackColor = CharcoalSurface
                            )
                        )
                    }

                    if (settings.isFadeEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Duração da Transição:",
                                color = SigilSilver,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "%.1f segundos".format(settings.fadeDurationSeconds),
                                color = CyberAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = settings.fadeDurationSeconds,
                            onValueChange = onChangeFadeDuration,
                            valueRange = 0.2f..3.0f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberAmber,
                                activeTrackColor = CyberAmber,
                                inactiveTrackColor = WireframeBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Dica: O pause possui corte anti-pop suave instantâneo (250ms). Valores entre 0.4s e 1.2s criam a transição perfeita e fluida entre faixas.",
                            color = MutedStone,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: VISUAL WALLPAPER OPACITY
            SectionHeader(
                title = "PERSONALIZAÇÃO VISUAL & CONTRASTE",
                icon = Icons.Default.Palette
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, WireframeBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    // Library Wallpaper Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Opacidade Papel de Parede (Biblioteca)",
                            color = SigilWhite,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${(settings.libraryWallpaperAlpha * 100).toInt()}%",
                            color = HermeticCyan,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = settings.libraryWallpaperAlpha,
                        onValueChange = onChangeLibraryAlpha,
                        valueRange = 0.05f..0.70f,
                        colors = SliderDefaults.colors(
                            thumbColor = HermeticCyan,
                            activeTrackColor = HermeticCyan,
                            inactiveTrackColor = WireframeBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Expanded Player Wallpaper Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Opacidade Papel de Parede (Player Expandido)",
                            color = SigilWhite,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${(settings.playerWallpaperAlpha * 100).toInt()}%",
                            color = CyberAmber,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = settings.playerWallpaperAlpha,
                        onValueChange = onChangePlayerAlpha,
                        valueRange = 0.05f..0.70f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberAmber,
                            activeTrackColor = CyberAmber,
                            inactiveTrackColor = WireframeBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: AI BRIDGE (USB / DESKTOP AGENTS)
            SectionHeader(
                title = "PONTE DE IA USB // ATOM & AGENTS",
                icon = Icons.Default.Hub
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, CyberAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Exportação de Metadados para Agente Local",
                        color = SigilWhite,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Conecte o USB no computador para permitir que Atom, Hermes Agent ou scripts locais leiam suas pastas, categorizem faixas e reorganizem sua discografia automaticamente.",
                        color = MutedStone,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onExportAiManifest { filePath ->
                                exportStatusText = "Exportado com sucesso para:\n$filePath"
                                Toast.makeText(context, "Manifesto gerado em Downloads!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberAmber,
                            contentColor = VoidBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ GERAR MANIFESTO JSON NA PASTA DOWNLOADS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (exportStatusText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exportStatusText!!,
                            color = HermeticCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Brand
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HUGH PLAYER // VERSION 1.3.0 CORE\nHUGH DEVELOPMENT • CO-FOUNDERS: HUGH & ATOM",
                    color = MutedStone.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyberAmber,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = SigilWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}
