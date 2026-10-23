package com.hughdevelopment.player.data.repository

import android.content.Context
import com.hughdevelopment.player.data.db.DailyListeningDao
import com.hughdevelopment.player.data.db.DailyListeningEntity
import com.hughdevelopment.player.data.db.PlaylistDao
import com.hughdevelopment.player.data.db.PlaylistEntity
import com.hughdevelopment.player.data.db.PlaylistSongCrossRef
import com.hughdevelopment.player.data.db.StatsDao
import com.hughdevelopment.player.data.db.StatsEntity
import com.hughdevelopment.player.data.model.DayListeningSummary
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.model.StreakInfo
import com.hughdevelopment.player.data.model.TopSongSummary
import com.hughdevelopment.player.data.source.MediaStoreScanner
import com.hughdevelopment.player.hermes.HermesVoiceManager
import com.hughdevelopment.player.notifications.VaultNotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MusicRepository(
    val scanner: MediaStoreScanner,
    val statsDao: StatsDao,
    val playlistDao: PlaylistDao,
    val dailyListeningDao: DailyListeningDao,
    val context: Context
) {
    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _favoriteOverrides = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    fun getCurrentSongs(): List<Song> = _rawSongs.value

    // Combine scanned songs with favorites from Room DB and instantaneous memory overrides
    val songs: Flow<List<Song>> = combine(
        _rawSongs,
        statsDao.getFavoriteStatsFlow(),
        _favoriteOverrides
    ) { songList, favEntities, overrides ->
        val favIdSet = favEntities.map { it.songId }.toSet()
        songList.map { song ->
            val isFav = overrides[song.id] ?: favIdSet.contains(song.id)
            song.copy(isFavorite = isFav)
        }
    }

    // Reactive list of playlists with full member Song objects
    val playlists: Flow<List<Playlist>> = combine(
        playlistDao.getAllPlaylistsFlow(),
        playlistDao.getAllPlaylistSongsFlow(),
        songs
    ) { playlistEntities, crossRefs, songList ->
        val songMap = songList.associateBy { it.id }
        val refsByPlaylist = crossRefs.groupBy { it.playlistId }

        playlistEntities.map { entity ->
            val memberSongIds = refsByPlaylist[entity.id]?.map { it.songId } ?: emptyList()
            val memberSongs = memberSongIds.mapNotNull { songMap[it] }
            Playlist(
                id = entity.id,
                name = entity.name,
                category = entity.category,
                colorHex = entity.colorHex,
                songs = memberSongs,
                songCount = memberSongs.size
            )
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    suspend fun refreshLibrary() {
        _isLoading.value = true
        try {
            val scanned = scanner.scanLocalSongs()
            _rawSongs.value = scanned
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun recordSongStarted(song: Song) {
        statsDao.recordTrackStarted(
            songId = song.id,
            title = song.title,
            artist = song.artist
        )
    }

    suspend fun recordSongPlayed(song: Song, secondsPlayed: Long) {
        recordSongProgress(song, secondsPlayed)
    }

    suspend fun recordSongProgress(song: Song, secondsPlayed: Long) {
        if (secondsPlayed > 0) {
            statsDao.addSecondsListened(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                seconds = secondsPlayed
            )

            // Record to Daily Listening for Duolingo Streak Tracking
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val prevRecord = dailyListeningDao.getRecordForDate(todayKey)
            val wasAlreadyValid = prevRecord?.hasValidStreakDay == true

            dailyListeningDao.recordDailyListening(todayKey, secondsPlayed)

            val updatedRecord = dailyListeningDao.getRecordForDate(todayKey)
            if (!wasAlreadyValid && updatedRecord?.hasValidStreakDay == true) {
                // Streak day validated! Trigger celebratory notification and Hermes voice quip
                val currentStreak = calculateCurrentStreak()
                VaultNotificationHelper.notifyStreakDayValidated(context, currentStreak)
                try {
                    HermesVoiceManager.getInstance(context).speakStreakCelebration(currentStreak)
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun toggleFavorite(song: Song): Boolean {
        val targetFav = !song.isFavorite
        _favoriteOverrides.value = _favoriteOverrides.value + (song.id to targetFav)
        val resultingFav = statsDao.toggleFavorite(song.id, song.title, song.artist)
        _favoriteOverrides.value = _favoriteOverrides.value + (song.id to resultingFav)
        return resultingFav
    }

    suspend fun calculateCurrentStreak(): Int {
        val allDays = dailyListeningDao.getRecentDays(120)
        return computeStreakFromRecords(allDays)
    }

    private fun computeStreakFromRecords(records: List<DailyListeningEntity>): Int {
        val validDateSet = records.filter { it.hasValidStreakDay }.map { it.dateKey }.toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val todayKey = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = sdf.format(cal.time)

        val startCal = Calendar.getInstance()
        if (validDateSet.contains(todayKey)) {
            // Count backwards starting from today
        } else if (validDateSet.contains(yesterdayKey)) {
            // Count backwards starting from yesterday
            startCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            return 0
        }

        var streak = 0
        while (true) {
            val dateKey = sdf.format(startCal.time)
            if (validDateSet.contains(dateKey)) {
                streak++
                startCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun getListeningOverview(): Flow<ListeningOverview> {
        return combine(
            statsDao.getAllStatsFlow(),
            statsDao.getTotalSecondsListenedFlow(),
            statsDao.getUniquePlayedCountFlow(),
            statsDao.getFavoriteCountFlow(),
            dailyListeningDao.getAllDailyRecordsFlow()
        ) { statsList, totalSeconds, uniqueCount, favCount, dailyRecords ->
            val totalSec = totalSeconds ?: 0L
            val topSongs = statsList.filter { it.playCount > 0 }.take(10).map { entity ->
                TopSongSummary(
                    songId = entity.songId,
                    title = entity.title,
                    artist = entity.artist,
                    playCount = entity.playCount,
                    totalSecondsListened = entity.totalSecondsListened
                )
            }

            val streak = computeStreakFromRecords(dailyRecords)
            val activeDaysCount = dailyRecords.count { it.hasValidStreakDay }
            val dailyMap = dailyRecords.associateBy { it.dateKey }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayKey = sdf.format(Date())
            val isTodayCompleted = dailyMap[todayKey]?.hasValidStreakDay == true

            // Build Monthly Calendar (current month days 1..maxDays)
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) // 0-based
            val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))

            val monthlyDays = (1..maxDaysInMonth).map { day ->
                val dayCal = Calendar.getInstance().apply {
                    set(year, month, day, 12, 0, 0)
                }
                val key = "%04d-%02d-%02d".format(year, month + 1, day)
                val record = dailyMap[key]
                DayListeningSummary(
                    dateKey = key,
                    dayOfMonth = day,
                    dayOfWeekLabel = dayOfWeekFormat.format(dayCal.time).uppercase().take(3),
                    minutes = record?.totalMinutes ?: 0L,
                    hasValidStreak = record?.hasValidStreakDay == true
                )
            }

            // Build Recent 7 Days (for the mini bar-chart)
            val recent7 = (6 downTo 0).map { daysAgo ->
                val dayCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -daysAgo)
                }
                val key = sdf.format(dayCal.time)
                val record = dailyMap[key]
                DayListeningSummary(
                    dateKey = key,
                    dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH),
                    dayOfWeekLabel = dayOfWeekFormat.format(dayCal.time).uppercase().take(3),
                    minutes = record?.totalMinutes ?: 0L,
                    hasValidStreak = record?.hasValidStreakDay == true
                )
            }

            val totalMonthMins = monthlyDays.sumOf { it.minutes }
            val activeDaysThisMonth = monthlyDays.count { it.minutes > 0 }.coerceAtLeast(1)
            val averageDaily = totalMonthMins / activeDaysThisMonth

            ListeningOverview(
                totalSecondsListened = totalSec,
                totalSongsPlayed = statsList.sumOf { it.playCount },
                uniqueTracksListened = uniqueCount,
                favoriteCount = favCount,
                topSongs = topSongs,
                streakInfo = StreakInfo(
                    currentStreak = streak,
                    bestStreak = streak.coerceAtLeast(activeDaysCount),
                    isTodayCompleted = isTodayCompleted,
                    totalActiveDays = activeDaysCount
                ),
                monthlyCalendar = monthlyDays,
                recent7Days = recent7,
                averageDailyMinutes = averageDaily
            )
        }
    }

    fun searchSongs(query: String): List<Song> {
        val current = _rawSongs.value
        if (query.isBlank()) return current
        val q = query.trim().lowercase()
        return current.filter { song ->
            song.title.lowercase().contains(q) ||
            song.artist.lowercase().contains(q) ||
            song.album.lowercase().contains(q)
        }
    }

    // --- Playlist & Folder Management ---

    suspend fun createPlaylist(name: String, category: String = "CUSTOM", colorHex: String = "#F59E0B"): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name.trim(),
                category = category,
                colorHex = colorHex
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistCompletely(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.insertSongCrossRef(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val refs = songIds.map { PlaylistSongCrossRef(playlistId, it) }
        playlistDao.insertSongCrossRefs(refs)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    // Pre-create the 4 Sovereign Categories automatically
    suspend fun autoOrganizeWithAiPresets(allSongs: List<Song>): Map<String, Int> {
        val presetDefinitions = listOf(
            Triple("Músicas Cristãs (Cantado)", "CRISTÃS (CANTADO)", "#0022FF"),
            Triple("Músicas Cristãs (Playback)", "CRISTÃS (PLAYBACK)", "#F59E0B"),
            Triple("Músicas Dia a Dia", "DIA A DIA", "#06B6D4"),
            Triple("Áudios de Reuniões", "REUNIÕES", "#E4E4E7")
        )

        val playlistIdMap = mutableMapOf<String, Long>()
        for ((name, cat, color) in presetDefinitions) {
            val existing = playlistDao.getPlaylistByName(name)
            val id = existing?.id ?: playlistDao.insertPlaylist(
                PlaylistEntity(name = name, category = cat, colorHex = color)
            )
            playlistIdMap[name] = id
        }

        val christianArtists = listOf(
            "mara lima", "eyshila", "damares", "aline barros", "diante do trono", "nani azevedo",
            "anderson freire", "gabriel guedes", "elaine de jesus", "trazendo a arca", "giselli cristina",
            "cancao & louvor", "canção & louvor", "cassiane", "leia mendonca", "léia mendonça",
            "lea mendonça", "lea mendonca", "renascer praise", "michelle nascimento", "luiz de carvalho",
            "giselma barbosa", "vanderley santos", "ministerio", "ministério", "verdadeiro evangelho",
            "niel nascimento", "edgars2paula", "shirley carvalhaes", "bruna karla", "fernandinho",
            "midian lima", "isadora pompeo", "gabriela rocha", "thalles roberto", "rose nascimento",
            "mattos nascimento", "gerson rufino", "lauriete", "fabiana anastacio", "davi sacer",
            "ludmila ferber", "heloisa rosa", "harpa", "cantor cristao", "cantor cristão", "asaph borba",
            "adhemar de campos", "voz da verdade", "novo som", "oficina g3", "fhop", "alexsander lucio",
            "julia vitória", "julia vitoria", "rute maia", "felipe rodrigues", "elizeu rodrigues",
            "thiago brito", "todah music", "coral canto jovem", "rayssa andreoli", "leandro borges",
            "vanilda bordieri", "gabi sampaio", "nivea soares", "nívea soares", "jose wellington"
        )

        val christianKeywords = listOf(
            "hino", "harpa", "louvor", "gospel", "adorador", "adoração", "adoracao", "oracao", "oração",
            "deus", "jesus", "aleluia", "evangelho", "igreja", "bíblia", "biblia", "cruz", "calvário",
            "calvario", "ressuscitou", "redentor", "santo dos santos", "ungido", "unção", "milagre",
            "árvore e a cruz", "arvore e a cruz", "santidade", "adorai", "adorar", "bendito é o rei",
            "buscar-me-eis", "mais perto quero estar", "nome mais doce", "nada mais", "teu toque",
            "ezequiel 47", "não quero ser o mesmo", "o amor tem um nome", "chora que a vitória vem",
            "imperfeito", "comum e imperfeito"
        )

        val addedCounts = mutableMapOf<String, Int>()

        for (song in allSongs) {
            val lowerTitle = song.title.lowercase()
            val lowerPath = song.dataPath.lowercase()
            val lowerArtist = song.artist.lowercase()

            val isChristian = christianArtists.any { lowerArtist.contains(it) || lowerTitle.contains(it) } ||
                    christianKeywords.any { lowerTitle.contains(it) }

            val targetCategory = when {
                lowerPath.contains("4 - audios de reunioes") || lowerPath.contains("audio_reunioes") || lowerTitle.contains("reunião") || lowerTitle.contains("reuniao") || lowerPath.contains("meeting") || lowerPath.contains("ata") || lowerPath.contains("transcrever") -> {
                    "Áudios de Reuniões"
                }
                lowerPath.contains("2 - cristas (playback)") || lowerTitle.contains("playback") || lowerTitle.contains("instrumental") || lowerPath.contains("playback") || lowerTitle.contains("sem voz") -> {
                    "Músicas Cristãs (Playback)"
                }
                lowerPath.contains("1 - cristas (cantado)") || isChristian -> {
                    "Músicas Cristãs (Cantado)"
                }
                else -> {
                    "Músicas Dia a Dia"
                }
            }

            val targetPlaylistId = playlistIdMap[targetCategory]
            if (targetPlaylistId != null) {
                playlistDao.insertSongCrossRef(PlaylistSongCrossRef(targetPlaylistId, song.id))
                addedCounts[targetCategory] = (addedCounts[targetCategory] ?: 0) + 1
            }
        }

        return addedCounts
    }
}
