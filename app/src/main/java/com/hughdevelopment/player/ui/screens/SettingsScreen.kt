package com.hughdevelopment.player.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
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
import com.hughdevelopment.player.data.backup.BackupImportResult
import com.hughdevelopment.player.data.settings.SettingsManager
import com.hughdevelopment.player.data.settings.SettingsState
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.KleinBlue
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.VaultThemes
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
    onSelectTheme: (String) -> Unit = {},
    onExportBackup: ((String) -> Unit) -> Unit = {},
    onImportBackup: (String, (BackupImportResult) -> Unit) -> Unit = { _, _ -> },
    onToggleHermesVoice: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var exportStatusText by remember { mutableStateOf<String?>(null) }
    var backupStatusText by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (jsonString.isNotBlank()) {
                    onImportBackup(jsonString) { result ->
                        backupStatusText = result.message
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

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
                    .neoGlassCard(
                        cornerRadius = 10.dp,
                        backgroundColor = ObsidianGlass,
                        borderWidth = 1.dp,
                        topRimColor = SpecularGlassRimTop
                    )
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
                            valueRange = 1.0f..15.0f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberAmber,
                                activeTrackColor = CyberAmber,
                                inactiveTrackColor = WireframeBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Dica: Valores entre 8s e 10s criam a fusão perfeita entre faixas sem corte abrupto.",
                            color = MutedStone,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: HERMES // VOZ SOBERANA (TTS OFFLINE)
            SectionHeader(
                title = "HERMES // VOZ SOBERANA (TTS OFFLINE)",
                icon = Icons.Default.AutoAwesome
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoGlassCard(
                        cornerRadius = 10.dp,
                        backgroundColor = ObsidianGlass,
                        borderWidth = 1.dp,
                        topRimColor = SpecularGlassRimTop
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voz Nativa Hermes: Verbalizar saudações e ofensivas offline",
                            color = SigilWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (settings.isHermesVoiceEnabled)
                                "Ativada (Síntese local sem internet • Fala saudações, ofensivas e promoções de rank)"
                            else
                                "Desativada (Modo silencioso)",
                            color = if (settings.isHermesVoiceEnabled) HermeticCyan else MutedStone,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Switch(
                        checked = settings.isHermesVoiceEnabled,
                        onCheckedChange = { isEnabled ->
                            onToggleHermesVoice?.invoke(isEnabled)
                                ?: SettingsManager.getInstance(context).setHermesVoiceEnabled(isEnabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidBlack,
                            checkedTrackColor = CyberAmber,
                            uncheckedThumbColor = MutedStone,
                            uncheckedTrackColor = CharcoalSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // THEME MARKETPLACE // COFRE DE IDENTIDADES VISUAIS
            SectionHeader(
                title = "COFRE DE TEMAS // MERCADO DE IDENTIDADES",
                icon = Icons.Default.AutoAwesome
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VaultThemes.AllThemes.forEach { theme ->
                    val isSelected = settings.activeThemeId.equals(theme.id.name, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) theme.primaryAccent else WireframeBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTheme(theme.id.name) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(theme.secondaryAccent)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = theme.displayName,
                                        color = if (isSelected) theme.primaryAccent else SigilWhite,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = theme.badgeTag,
                                        color = MutedStone,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = theme.description,
                                    color = MutedStone,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) theme.primaryAccent else CharcoalSurface)
                                    .border(1.dp, if (isSelected) theme.primaryAccent else WireframeBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isSelected) "ATIVO" else "ATIVAR",
                                    color = if (isSelected) VoidBlack else SigilSilver,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
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
                    .neoGlassCard(
                        cornerRadius = 10.dp,
                        backgroundColor = ObsidianGlass,
                        borderWidth = 1.dp,
                        topRimColor = SpecularGlassRimTop
                    )
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

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION: SOVEREIGN BACKUP & RESTORE
            SectionHeader(
                title = "COFRE DE DADOS // BACKUP & RESTAURAÇÃO JSON",
                icon = Icons.Default.Backup
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
                    Text(
                        text = "Soberania Total: Zero Perda de Dados",
                        color = SigilWhite,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Exporte todo o seu histórico (ofensivas, pastas, favoritos e estatísticas) para um arquivo JSON portável. Restaure a qualquer momento, mesmo ao trocar de celular.",
                        color = MutedStone,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onExportBackup { path ->
                                    backupStatusText = "Backup gerado em:\n$path"
                                    Toast.makeText(context, "Backup salvo na pasta Downloads!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KleinBlue,
                                contentColor = SigilWhite
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "EXPORTAR JSON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberAmber,
                                contentColor = VoidBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "RESTAURAR JSON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (backupStatusText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = backupStatusText!!,
                            color = HermeticCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: OFFLINE INTEGRITY & OBSERVABILITY
            SectionHeader(
                title = "DIAGNÓSTICO // OFFLINE INTEGRITY",
                icon = Icons.Default.Security
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoGlassCard(
                        cornerRadius = 10.dp,
                        backgroundColor = ObsidianGlass,
                        borderWidth = 1.dp,
                        topRimColor = SpecularGlassRimTop
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagnosticRow("ISOLAMENTO DE REDE", "100% OFFLINE (ZERO PERMISSÕES INTERNET)", HermeticCyan)
                    DiagnosticRow("BANCO DE DADOS", "ROOM 2.0 (WAL ATIVO • ÍNDICES B-TREE)", CyberAmber)
                    DiagnosticRow("CACHE DE ARTE", "BOUNDED LRU (1.000 SLOTS • 120PX HARDWARE)", SigilWhite)
                    DiagnosticRow("MOTOR DE ÁUDIO", "MEDIA3 EXOPLAYER (0ms INTENT TRACKER)", SigilSilver)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Brand
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AETHER VAULT // VERSION 1.4.1 CORE\nSOVEREIGN OFFLINE AUDIO • CO-FOUNDERS: HUGH & ATOM",
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
private fun DiagnosticRow(
    label: String,
    status: String,
    statusColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SigilSilver,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = status,
            color = statusColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
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
