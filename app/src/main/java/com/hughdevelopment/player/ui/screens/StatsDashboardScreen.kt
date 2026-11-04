package com.hughdevelopment.player.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hughdevelopment.player.R
import com.hughdevelopment.player.data.model.DayListeningSummary
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.TopSongSummary
import com.hughdevelopment.player.data.settings.TimeDisplayFormat
import com.hughdevelopment.player.data.settings.formatListeningTime
import com.hughdevelopment.player.export.SovereignCardGenerator
import com.hughdevelopment.player.ui.components.CybersigilBackground
import com.hughdevelopment.player.ui.components.SovereignVoidBackdrop
import com.hughdevelopment.player.ui.components.neoGlassCard
import com.hughdevelopment.player.ui.theme.LocalVaultTheme
import com.hughdevelopment.player.ui.theme.CharcoalSurface
import com.hughdevelopment.player.ui.theme.CyberAmber
import com.hughdevelopment.player.ui.theme.HermeticCyan
import com.hughdevelopment.player.ui.theme.MutedStone
import com.hughdevelopment.player.ui.theme.ObsidianGlass
import com.hughdevelopment.player.ui.theme.SigilSilver
import com.hughdevelopment.player.ui.theme.SigilWhite
import com.hughdevelopment.player.ui.theme.SpecularGlassRimTop
import com.hughdevelopment.player.ui.theme.VoidBlack
import com.hughdevelopment.player.ui.theme.WireframeBorder
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsDashboardScreen(
    overview: ListeningOverview,
    totalTracks: Int = 0,
    timeDisplayFormat: TimeDisplayFormat = TimeDisplayFormat.MINUTES,
    onSpeakBriefing: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalVaultTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        // Pure Obsidian Void & Centralized Radiant Aura
        SovereignVoidBackdrop(
            showCybersigil = true,
            auraScale = 1.0f,
            centralizeAura = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "AETHER // VAULT TELEMETRY",
                        color = SigilWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "OFENSIVAS, DISCIPLINAS E ESTATÍSTICAS SOBERANAS",
                        color = CyberAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. DUOLINGO-STYLE STREAK BANNER
                item {
                    StreakBannerCard(overview = overview)
                }

                // 1.5 HERMES SPOKEN BRIEFING ACTION (R8)
                item {
                    HermesBriefingActionCard(
                        onSpeakBriefing = onSpeakBriefing
                    )
                }

                // 1.8 SOVEREIGN AUDIO CARD GENERATOR (Opção 2)
                item {
                    SovereignCardExportActionCard(
                        overview = overview,
                        totalTracks = totalTracks,
                        timeDisplayFormat = timeDisplayFormat
                    )
                }

                // 2. SOVEREIGN LEVEL & XP CARD (ANTI-SLEEPER BALANCED)
                item {
                    SovereignLevelCard(overview = overview)
                }

                // 3. KEY METRICS GRID
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = if (timeDisplayFormat == TimeDisplayFormat.DAYS_AND_HOURS) "TEMPO OUVIDO" else "MINUTOS",
                            value = formatListeningTime(overview.totalSecondsListened, timeDisplayFormat),
                            icon = Icons.Default.Timelapse,
                            accentColor = currentTheme.primaryAccent,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "PLAYS",
                            value = "${overview.totalSongsPlayed}",
                            icon = Icons.Default.Headphones,
                            accentColor = currentTheme.secondaryAccent,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "MÉDIA/DIA",
                            value = "${overview.averageDailyMinutes}m",
                            icon = Icons.Default.CalendarMonth,
                            accentColor = currentTheme.tertiaryAccent,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "DIAS ATIVOS",
                            value = "${overview.streakInfo.totalActiveDays}",
                            icon = Icons.Default.LocalFireDepartment,
                            accentColor = currentTheme.primaryAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. RECENT 7-DAYS BAR CHART
                item {
                    WeeklyBarChartCard(overview = overview)
                }

                // 5. MONTHLY CALENDAR GRID
                item {
                    MonthlyCalendarCard(overview = overview)
                }

                // 6. SIGIL ACHIEVEMENTS BADGES (BALANCED MASTERY)
                item {
                    Column {
                        Text(
                            text = "SIGILOS DE MAESTRIA SOBERANA",
                            color = SigilWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        BalancedSigilsBadgesGrid(overview = overview)
                    }
                }

                // 7. TOP TRACKS RANKING
                item {
                    Text(
                        text = "TOP FAIXAS MAIS TOCADAS",
                        color = SigilWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                if (overview.topSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CharcoalSurface)
                                .border(1.dp, WireframeBorder, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NENHUMA FAIXA REGISTRADA AINDA",
                                color = MutedStone,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                } else {
                    val maxPlays = overview.topSongs.maxOfOrNull { it.playCount }?.coerceAtLeast(1) ?: 1
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
private fun StreakBannerCard(overview: ListeningOverview) {
    val streak = overview.streakInfo.currentStreak
    val isTodayDone = overview.streakInfo.isTodayCompleted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.2.dp,
                topRimColor = CyberAmber,
                bottomRimColor = SpecularGlassRimTop
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isTodayDone) CyberAmber else CharcoalSurface)
                    .border(1.5.dp, if (isTodayDone) SigilWhite else WireframeBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (isTodayDone) VoidBlack else CyberAmber,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$streak DIAS DE OFENSIVA",
                        color = SigilWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (streak > 0) Icons.Default.LocalFireDepartment else Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = if (isTodayDone) {
                        "Meta de hoje concluída! Chama protegida até amanhã."
                    } else {
                        "Ouça pelo menos 5 min hoje para manter sua ofensiva viva!"
                    },
                    color = if (isTodayDone) HermeticCyan else CyberAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SovereignLevelCard(overview: ListeningOverview) {
    val theme = LocalVaultTheme.current
    val rank = overview.rankTier
    val xp = overview.xp
    val progress = (xp.toFloat() / rank.nextThreshold.toFloat()).coerceIn(0.05f, 1.0f)
    val rankColor = try { Color(android.graphics.Color.parseColor(rank.colorHex)) } catch (e: Exception) { CyberAmber }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = rankColor.copy(alpha = 0.4f)
            )
            .padding(14.dp)
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
                        tint = rankColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LVL ${rank.level} • ${rank.title}",
                        color = rankColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "$xp / ${rank.nextThreshold} XP",
                    color = SigilWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = rankColor,
                trackColor = WireframeBorder
            )
        }
    }
}

@Composable
private fun WeeklyBarChartCard(overview: ListeningOverview) {
    val theme = LocalVaultTheme.current
    val days = overview.recent7Days
    val maxMins = days.maxOfOrNull { it.minutes }?.coerceAtLeast(15L) ?: 15L

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = theme.primaryAccent.copy(alpha = 0.25f)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MINUTOS NOS ÚLTIMOS 7 DIAS",
                    color = SigilWhite,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "COLUNAS SAGRADAS",
                    color = theme.primaryAccent,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    val barFrac = (day.minutes.toFloat() / maxMins.toFloat()).coerceIn(0.10f, 1.0f)
                    val hasMinutes = day.minutes > 0
                    val accent = if (day.hasValidStreak) theme.secondaryAccent else if (hasMinutes) theme.primaryAccent else WireframeBorder

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${day.minutes}m",
                            color = if (hasMinutes) SigilWhite else MutedStone.copy(alpha = 0.45f),
                            fontSize = 9.sp,
                            fontWeight = if (hasMinutes) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Futuristic column with radiant vertical gradient and glowing capitel
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((58 * barFrac).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (hasMinutes) {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                accent,
                                                accent.copy(alpha = 0.40f),
                                                accent.copy(alpha = 0.12f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                WireframeBorder.copy(alpha = 0.5f),
                                                WireframeBorder.copy(alpha = 0.2f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            if (hasMinutes) SpecularGlassRimTop else WireframeBorder,
                                            accent.copy(alpha = 0.3f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (hasMinutes) {
                                // Glowing capitel (column crown / capitello divino)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    accent.copy(alpha = 0.6f),
                                                    SigilWhite,
                                                    accent.copy(alpha = 0.6f)
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day.dayOfWeekLabel,
                            color = if (day.hasValidStreak) theme.primaryAccent else MutedStone,
                            fontSize = 9.sp,
                            fontWeight = if (day.hasValidStreak) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyCalendarCard(overview: ListeningOverview) {
    val theme = LocalVaultTheme.current
    val monthName = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(Date()).uppercase()
    val days = overview.monthlyCalendar
    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = theme.primaryAccent.copy(alpha = 0.25f)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALENDÁRIO // $monthName",
                    color = SigilWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${days.count { it.hasValidStreak }} DIAS ATIVOS",
                    color = CyberAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Days Grid (Chunks of 7 columns)
            val rows = days.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { day ->
                            val isToday = day.dateKey == todayKey
                            val dayBoxColor = when {
                                day.hasValidStreak -> CyberAmber.copy(alpha = 0.25f)
                                day.minutes > 0 -> theme.primaryAccent.copy(alpha = 0.22f)
                                else -> CharcoalSurface.copy(alpha = 0.40f)
                            }
                            val dayBorderColor = when {
                                isToday -> HermeticCyan
                                day.hasValidStreak -> CyberAmber
                                else -> WireframeBorder.copy(alpha = 0.35f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(dayBoxColor)
                                    .border(if (isToday) 1.5.dp else 0.8.dp, dayBorderColor, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${day.dayOfMonth}",
                                        color = if (isToday) SigilWhite else if (day.hasValidStreak) CyberAmber else MutedStone,
                                        fontSize = 10.sp,
                                        fontWeight = if (isToday || day.hasValidStreak) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (day.hasValidStreak) {
                                        Text(text = "•", color = CyberAmber, fontSize = 8.sp)
                                    }
                                }
                            }
                        }

                        // Fill trailing spaces if last row has less than 7 days
                        if (week.size < 7) {
                            for (i in 0 until (7 - week.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalancedSigilsBadgesGrid(overview: ListeningOverview) {
    val streak = overview.streakInfo.currentStreak
    val totalActiveDays = overview.streakInfo.totalActiveDays
    val uniqueTracks = overview.uniqueTracksListened
    val favorites = overview.favoriteCount
    val totalMins = overview.totalMinutesListened

    // Anti-Sleeper balanced thresholds:
    val hasStreak3 = streak >= 3
    val hasStreak7 = streak >= 7
    val hasExplorer = uniqueTracks >= 25
    val hasCollector = favorites >= 5
    val hasPurist = totalActiveDays >= 10
    val hasOverclock = totalMins >= 120

    val badges = listOf(
        Triple("OFENSIVA 3D", hasStreak3, Icons.Default.LocalFireDepartment),
        Triple("SEMANA SOBERANA", hasStreak7, Icons.Default.MilitaryTech),
        Triple("EXPLORADOR", hasExplorer, Icons.Default.ElectricBolt),
        Triple("COLECIONADOR", hasCollector, Icons.Default.Favorite),
        Triple("DISCIPLINADO", hasPurist, Icons.Default.Security),
        Triple("IMERSÃO 2H", hasOverclock, Icons.Default.Headphones)
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        badges.chunked(3).forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowBadges.forEach { (title, isUnlocked, icon) ->
                    SigilBadgeItem(
                        title = title,
                        isUnlocked = isUnlocked,
                        icon = icon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
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
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) CyberAmber else MutedStone.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isUnlocked) SigilWhite else MutedStone.copy(alpha = 0.35f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = accentColor.copy(alpha = 0.35f)
            )
            .padding(10.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = SigilWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = title,
                color = MutedStone,
                fontSize = 9.sp,
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
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalSurface)
            .border(1.dp, WireframeBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$index",
                    color = rankColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(30.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = SigilWhite,
                        fontSize = 13.sp,
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
                        fontSize = 13.sp,
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

            Spacer(modifier = Modifier.height(6.dp))

            val playRatio = (item.playCount.toFloat() / maxPlays.toFloat()).coerceIn(0.05f, 1.0f)
            LinearProgressIndicator(
                progress = { playRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = rankColor,
                trackColor = WireframeBorder.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun HermesBriefingActionCard(
    onSpeakBriefing: () -> Unit
) {
    val theme = LocalVaultTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = theme.primaryAccent.copy(alpha = 0.35f)
            )
            .clickable { onSpeakBriefing() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(theme.primaryAccent.copy(alpha = 0.15f))
                .border(1.dp, theme.primaryAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "OUVIR BRIEFING DO HERMES",
                color = theme.primaryAccent,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "Relatório falado offline: minutos, ofensiva, rank e diretriz estratégica",
                color = MutedStone,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SovereignCardExportActionCard(
    overview: ListeningOverview,
    totalTracks: Int,
    timeDisplayFormat: TimeDisplayFormat
) {
    val context = LocalContext.current
    val theme = LocalVaultTheme.current
    val coroutineScope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var exportSuccessUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neoGlassCard(
                cornerRadius = 14.dp,
                backgroundColor = ObsidianGlass,
                borderWidth = 1.dp,
                topRimColor = SpecularGlassRimTop,
                bottomRimColor = theme.secondaryAccent.copy(alpha = 0.35f)
            )
            .clickable(enabled = !isGenerating) {
                isGenerating = true
                coroutineScope.launch {
                    val bitmap = SovereignCardGenerator.generateCardBitmap(
                        overview = overview,
                        totalTracks = totalTracks,
                        timeDisplayFormat = timeDisplayFormat,
                        theme = theme
                    )
                    val uri = SovereignCardGenerator.saveCardToGallery(context, bitmap)
                    exportSuccessUri = uri
                    isGenerating = false
                    showSuccessDialog = true
                }
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(theme.secondaryAccent.copy(alpha = 0.15f))
                .border(1.dp, theme.secondaryAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = theme.secondaryAccent,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = theme.secondaryAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isGenerating) "GERANDO CARTÃO SOBERANO..." else "EXPORTAR CARTÃO DE SOBERANIA",
                color = theme.secondaryAccent,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "Gera um card em alta resolução (1080x1920) 100% offline para galeria e redes",
                color = MutedStone,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "CARTÃO DE SOBERANIA GERADO",
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Text(
                    text = "O relatório visual de telemetria foi salvo com sucesso em Pictures/AetherVault no seu aparelho. Zero dados foram enviados para nuvem.",
                    color = theme.textMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportSuccessUri?.let { uri ->
                            SovereignCardGenerator.shareCard(context, uri)
                        }
                        showSuccessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("COMPARTILHAR", color = theme.background, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("CONCLUIR", color = theme.textPrimary)
                }
            },
            containerColor = theme.cardBackground,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
