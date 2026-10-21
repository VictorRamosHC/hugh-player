package com.hughdevelopment.player.data.repository

import com.hughdevelopment.player.data.db.PlaylistDao
import com.hughdevelopment.player.data.db.PlaylistEntity
import com.hughdevelopment.player.data.db.PlaylistSongCrossRef
import com.hughdevelopment.player.data.db.StatsDao
import com.hughdevelopment.player.data.db.StatsEntity
import com.hughdevelopment.player.data.model.ListeningOverview
import com.hughdevelopment.player.data.model.Playlist
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.model.TopSongSummary
import com.hughdevelopment.player.data.source.MediaStoreScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.Locale

class MusicRepository(
    private val scanner: MediaStoreScanner,
    private val statsDao: StatsDao,
    private val playlistDao: PlaylistDao
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

    suspend fun recordSongProgress(song: Song, secondsPlayed: Long) {
        if (secondsPlayed > 0) {
            statsDao.addSecondsListened(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                seconds = secondsPlayed
            )
        }
    }

    suspend fun toggleFavorite(song: Song): Boolean {
        val targetFav = !song.isFavorite
        _favoriteOverrides.value = _favoriteOverrides.value + (song.id to targetFav)
        val resultingFav = statsDao.toggleFavorite(song.id, song.title, song.artist)
        _favoriteOverrides.value = _favoriteOverrides.value + (song.id to resultingFav)
        return resultingFav
    }

    fun getListeningOverview(): Flow<ListeningOverview> {
        return combine(
            statsDao.getAllStatsFlow(),
            statsDao.getTotalSecondsListenedFlow(),
            statsDao.getUniquePlayedCountFlow(),
            statsDao.getFavoriteCountFlow()
        ) { statsList, totalSeconds, uniqueCount, favCount ->
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
            ListeningOverview(
                totalSecondsListened = totalSec,
                totalSongsPlayed = statsList.sumOf { it.playCount },
                uniqueTracksListened = uniqueCount,
                favoriteCount = favCount,
                topSongs = topSongs
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

    /**
     * Auto-classifies the user's entire local music library into Hugh's 4 designated folders:
     * 1. Músicas Cristãs (Cantado)
     * 2. Músicas Cristãs (Playback)
     * 3. Músicas Dia a Dia
     * 4. Áudios de Reuniões
     */
    suspend fun autoOrganizeWithAiPresets(allSongs: List<Song>): Map<String, Int> {
        // Ensure default 4 folders exist without duplicates
        val defaultFolders = listOf(
            Triple("Músicas Cristãs (Cantado)", "CRISTÃS (CANTADO)", "#F59E0B"),
            Triple("Músicas Cristãs (Playback)", "CRISTÃS (PLAYBACK)", "#06B6D4"),
            Triple("Músicas Dia a Dia", "DIA A DIA", "#10B981"),
            Triple("Áudios de Reuniões", "REUNIÕES", "#8B5CF6")
        )

        // Find existing folder by name or create
        val folderIdMap = mutableMapOf<String, Long>()
        for ((name, cat, color) in defaultFolders) {
            val existing = playlistDao.getPlaylistByName(name)
            val id = existing?.id ?: playlistDao.insertPlaylist(
                PlaylistEntity(
                    name = name,
                    category = cat,
                    colorHex = color
                )
            )
            folderIdMap[name] = id
        }

        val christianArtistsAndKeywords = listOf(
            "diante do trono", "niel nascimento", "nani azevedo", "mara lima", "gabriel guedes",
            "fhop", "anderson freire", "bruna karla", "julia vitória", "julia vitoria",
            "eliã oliveira", "elia oliveira", "canção & louvor", "cancao & louvor", "canção e louvor",
            "rute maia", "damares", "jorge araújo", "jorge araujo", "odete", "sintia correia",
            "verdadeiro evangelho", "alexsander lucio", "vaso perfeito", "cicatrizes",
            "coração igual ao teu", "coracao igual ao teu", "bendito é o rei", "bendito e o rei",
            "como na primeira vez", "consolador", "depois da cruz", "daniel", "a árvore e a cruz",
            "a arvore e a cruz", "atos 2", "agnus dei", "adorador", "senhor", "jesus", "deus",
            "cristo", "evangelho", "harpa", "louvor", "gospel", "calvário", "calvario", "eterno presente",
            "gloria", "glória", "hino", "adoração", "adoracao", "eyshila", "midian lima",
            "léa mendonça", "lea mendonca", "aline barros", "cassiane", "luiz de carvalho",
            "vanilda bordieri", "noemi nonato", "trazendo a arca", "najara nogue", "zelly lima",
            "cléia souza", "cleia souza", "rafaelli cristina", "pregação", "pregacao", "orações", "oracoes"
        )

        val playbackKeywords = listOf(
            "playback", "play back", "play-back", "p/b", "#cover tom", "instrumental",
            "sem voz", "play_back", "karaoke", "karaokê", "tom contralto", "tom feminino", "tom masculino"
        )

        val meetingFolderNames = listOf(
            "audio_reunioes", "reunioes", "reuniões", "transcrever", "gravador", "recordings", "call_recorder"
        )

        val meetingKeywords = listOf(
            "reunião", "reuniao", "meeting", "ata ", "conferência", "conferencia",
            "processo ", "problema de acesso", "boletim papo", "bot-boletim", "brainstorm", "briefing"
        )

        val cantadoId = folderIdMap["Músicas Cristãs (Cantado)"]!!
        val playbackId = folderIdMap["Músicas Cristãs (Playback)"]!!
        val diaADiaId = folderIdMap["Músicas Dia a Dia"]!!
        val reunioesId = folderIdMap["Áudios de Reuniões"]!!

        val cantadoRefs = mutableListOf<PlaylistSongCrossRef>()
        val playbackRefs = mutableListOf<PlaylistSongCrossRef>()
        val diaADiaRefs = mutableListOf<PlaylistSongCrossRef>()
        val reunioesRefs = mutableListOf<PlaylistSongCrossRef>()

        for (song in allSongs) {
            val titleLower = song.title.lowercase(Locale.ROOT)
            val artistLower = song.artist.lowercase(Locale.ROOT)
            val albumLower = song.album.lowercase(Locale.ROOT)
            val pathLower = song.dataPath.lowercase(Locale.ROOT)
            val parentDir = java.io.File(song.dataPath).parentFile?.name?.lowercase(Locale.ROOT) ?: ""
            val fullContext = "$titleLower $artistLower $albumLower $pathLower $parentDir"

            val isExplicitMeeting = meetingFolderNames.contains(parentDir) ||
                    meetingKeywords.any { fullContext.contains(it) } ||
                    ((artistLower in listOf("desconhecido", "unknown", "")) &&
                            (titleLower.startsWith("track-") || titleLower.startsWith("rec-") || pathLower.contains("voice")))

            val isPlayback = playbackKeywords.any { fullContext.contains(it) } || parentDir == "playbacks"
            val isChristian = christianArtistsAndKeywords.any { fullContext.contains(it) }

            when {
                isExplicitMeeting && !isPlayback && !isChristian -> {
                    reunioesRefs.add(PlaylistSongCrossRef(reunioesId, song.id))
                }
                isPlayback -> {
                    playbackRefs.add(PlaylistSongCrossRef(playbackId, song.id))
                }
                isChristian -> {
                    cantadoRefs.add(PlaylistSongCrossRef(cantadoId, song.id))
                }
                else -> {
                    diaADiaRefs.add(PlaylistSongCrossRef(diaADiaId, song.id))
                }
            }
        }

        playlistDao.insertSongCrossRefs(cantadoRefs)
        playlistDao.insertSongCrossRefs(playbackRefs)
        playlistDao.insertSongCrossRefs(diaADiaRefs)
        playlistDao.insertSongCrossRefs(reunioesRefs)

        return mapOf(
            "Músicas Cristãs (Cantado)" to cantadoRefs.size,
            "Músicas Cristãs (Playback)" to playbackRefs.size,
            "Músicas Dia a Dia" to diaADiaRefs.size,
            "Áudios de Reuniões" to reunioesRefs.size
        )
    }
}
