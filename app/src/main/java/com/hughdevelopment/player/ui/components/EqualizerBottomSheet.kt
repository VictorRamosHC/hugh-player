package com.hughdevelopment.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.data.settings.EqualizerPreset
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.WireframeBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    isEnabled: Boolean,
    currentPreset: EqualizerPreset,
    bandLevels: List<Int>,
    bassBoostStrength: Int,
    centerFrequencies: List<Int>,
    onDismiss: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onBandLevelChange: (bandIndex: Int, levelMillibels: Int) -> Unit,
    onBassBoostChange: (strength: Int) -> Unit
) {
    val theme = LocalVaultTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presetScrollState = rememberScrollState()

    val bandLabels = listOf(
        "60Hz\nSUB-GRAVE",
        "230Hz\nGRAVE",
        "910Hz\nMÉDIO",
        "3.6kHz\nPRESENÇA",
        "14kHz\nAGUDO"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.background,
        scrimColor = theme.background.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header with Master On/Off Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = theme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EQUALIZADOR DSP 5-BANDAS",
                            color = theme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (isEnabled) "DSP ATIVO • PROCESSAMENTO NATIVO" else "DSP BYPASS (DESATIVADO)",
                            color = if (isEnabled) theme.primaryAccent else MutedStone,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = theme.background,
                        checkedTrackColor = theme.primary,
                        uncheckedThumbColor = MutedStone,
                        uncheckedTrackColor = theme.cardBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Horizontal Chips
            Text(
                text = "PRESETS ACÚSTICOS",
                color = theme.primaryAccent,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(presetScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EqualizerPreset.entries.forEach { preset ->
                    val isSelected = currentPreset == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) theme.primary else theme.cardBackground)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) theme.primary else WireframeBorder.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(enabled = isEnabled) { onSelectPreset(preset) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset.displayName.uppercase(),
                            color = if (isSelected) theme.background else if (isEnabled) theme.textPrimary else MutedStone,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5 Band Controls
            Text(
                text = "CURVA DE FREQUÊNCIA (-15dB A +15dB)",
                color = theme.primaryAccent,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.cardBackground)
                    .border(1.dp, WireframeBorder.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (bandIdx in 0 until 5) {
                    val rawLevelMb = bandLevels.getOrElse(bandIdx) { 0 }
                    val levelDb = rawLevelMb / 100f
                    val label = bandLabels.getOrElse(bandIdx) { "BANDA ${bandIdx + 1}" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = if (isEnabled) theme.textPrimary else MutedStone,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(80.dp),
                            lineHeight = 12.sp
                        )

                        Slider(
                            value = rawLevelMb.toFloat(),
                            onValueChange = { newVal ->
                                onBandLevelChange(bandIdx, newVal.toInt())
                            },
                            valueRange = -1500f..1500f,
                            enabled = isEnabled,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primary,
                                activeTrackColor = theme.primary,
                                inactiveTrackColor = WireframeBorder.copy(alpha = 0.4f),
                                disabledThumbColor = MutedStone,
                                disabledActiveTrackColor = WireframeBorder.copy(alpha = 0.2f)
                            )
                        )

                        Text(
                            text = if (levelDb >= 0) "+%.1fdB".format(levelDb) else "%.1fdB".format(levelDb),
                            color = if (isEnabled) theme.primaryAccent else MutedStone,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(55.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bass Boost Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.cardBackground)
                    .border(1.dp, WireframeBorder.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BASS BOOST (SUB-WOOFER)",
                        color = if (isEnabled) theme.textPrimary else MutedStone,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    val bassPercent = (bassBoostStrength / 10f).toInt()
                    Text(
                        text = "$bassPercent%",
                        color = if (isEnabled) theme.primary else MutedStone,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = bassBoostStrength.toFloat(),
                    onValueChange = { onBassBoostChange(it.toInt()) },
                    valueRange = 0f..1000f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primary,
                        activeTrackColor = theme.primary,
                        inactiveTrackColor = WireframeBorder.copy(alpha = 0.4f),
                        disabledThumbColor = MutedStone,
                        disabledActiveTrackColor = WireframeBorder.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
