package com.hughdevelopment.player.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.TopSongSummary
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
        // High-contrast brutalist dashboard ambient backdrop
        Image(
            painter = painterResource(id = R.drawable.bg_dashboard),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.30f)
        )

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

            Spacer(modifier = Modifier.height(12.dp))

            // GAMIFICATION: Vault Rank & XP Card
            VaultLevelCard(overview = overview)

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

            // Sigils / Achievements Badges Row
            SigilsBadgesRow(overview = overview)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TOP FAIXAS MAIS TOCADAS",
                color = SigilWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (overview.topSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
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
private fun VaultLevelCard(overview: ListeningOverview) {
    val xp = (overview.totalMinutesListened * 10) + (overview.totalSongsPlayed * 5) + (overview.favoriteCount * 25)
    val (rankTitle, nextThreshold, currentLevel) = when {
        xp < 300 -> Triple("NEOPHYTE OF THE VAULT", 300, 1)
        xp < 1000 -> Triple("ACOLYTE OF SOUND", 1000, 2)
        xp < 3000 -> Triple("HERMETIC ALCHEMIST", 3000, 3)
        else -> Triple("ARCHON OF THE CORE", 5000, 4)
    }
    val progress = (xp.toFloat() / nextThreshold.toFloat()).coerceIn(0.05f, 1.0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalSurface)
            .border(1.5.dp, CyberAmber.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LVL $currentLevel • $rankTitle",
                        color = CyberAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "$xp / $nextThreshold XP",
                    color = HermeticCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = CyberAmber,
                trackColor = WireframeBorder
            )
        }
    }
}

@Composable
private fun SigilsBadgesRow(overview: ListeningOverview) {
    val hasOverclocker = overview.totalMinutesListened >= 30
    val hasDevotee = overview.favoriteCount >= 3
    val hasLoop = overview.topSongs.any { it.playCount >= 5 }
    val hasPurist = overview.uniqueTracksListened >= 5

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SigilBadgeItem("OVERCLOCK", hasOverclocker, Icons.Default.ElectricBolt, Modifier.weight(1f))
        SigilBadgeItem("DEVOTEE", hasDevotee, Icons.Default.Favorite, Modifier.weight(1f))
        SigilBadgeItem("LOOP", hasLoop, Icons.Default.Sync, Modifier.weight(1f))
        SigilBadgeItem("PURIST", hasPurist, Icons.Default.Security, Modifier.weight(1f))
    }
}

@Composable
private fun SigilBadgeItem(
    title: String,
    isUnlocked: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isUnlocked) CharcoalSurface else VoidBlack.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (isUnlocked) CyberAmber else WireframeBorder.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp)
            )
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) CyberAmber else MutedStone.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = if (isUnlocked) SigilWhite else MutedStone.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
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
            .neoGlassCard(
                cornerRadius = 10.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = accentColor.copy(alpha = 0.35f)
            )
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
