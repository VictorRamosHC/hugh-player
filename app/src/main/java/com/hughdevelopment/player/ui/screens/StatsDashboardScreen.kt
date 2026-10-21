package com.hughdevelopment.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.TopSongSummary
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
fun StatsDashboardScreen(
    overview: ListeningOverview,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        CybersigilBackground(showHalo = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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
                        text = "ANALYTICS // VAULT",
                        color = SigilWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "OFFLINE WRAPPED • 100% DISCO LOCAL",
                        color = CyberAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Grid Metric Cards (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "TEMPO OUVIDO",
                    value = overview.formattedTotalTime,
                    icon = Icons.Default.Timelapse,
                    accentColor = CyberAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "PLAYS TOTAIS",
                    value = "${overview.totalSongsPlayed}",
                    icon = Icons.Default.Headphones,
                    accentColor = HermeticCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "FAIXAS ÚNICAS",
                    value = "${overview.uniqueTracksListened}",
                    icon = Icons.Default.ElectricBolt,
                    accentColor = SigilSilver,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "FAVORITAS",
                    value = "${overview.favoriteCount}",
                    icon = Icons.Default.Favorite,
                    accentColor = CyberAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TOP FAIXAS MAIS TOCADAS",
                color = SigilWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (overview.topSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalSurface)
                        .border(1.dp, WireframeBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[ VAULT PRONTO // AGUARDANDO REPRODUÇÕES ]",
                            color = CyberAmber,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toque suas faixas na biblioteca. O banco Room grava contagens e segundos de forma 100% offline.",
                            color = MutedStone,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val maxPlays = overview.topSongs.maxOfOrNull { it.playCount }?.coerceAtLeast(1) ?: 1

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    itemsIndexed(overview.topSongs) { index, item ->
                        TopSongRow(
                            index = index + 1,
                            item = item,
                            maxPlays = maxPlays
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalSurface)
            .border(1.dp, WireframeBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = SigilWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = title,
                color = MutedStone,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun TopSongRow(
    index: Int,
    item: TopSongSummary,
    maxPlays: Int
) {
    val rankColor = when (index) {
        1 -> CyberAmber
        2 -> HermeticCyan
        3 -> SigilSilver
        else -> MutedStone
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurface)
            .border(1.dp, WireframeBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Indicator
                Text(
                    text = "#$index",
                    color = rankColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(34.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = SigilWhite,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.artist,
                        color = MutedStone,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.playCount}x",
                        color = HermeticCyan,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = item.formattedTime,
                        color = MutedStone,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Proportional play bar
            LinearProgressIndicator(
                progress = { (item.playCount.toFloat() / maxPlays.toFloat()).coerceIn(0.05f, 1.0f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = rankColor,
                trackColor = WireframeBorder.copy(alpha = 0.3f)
            )
        }
    }
}
