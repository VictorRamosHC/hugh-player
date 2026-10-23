package com.hughdevelopment.player.lyrics

import android.os.Environment
import com.hughdevelopment.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Multi-tier local lyrics discovery and caching repository (Requirement R2).
 *
 * Scans local storage tiers in order:
 * 1. Tier 1: Same directory as the song file (Song.dataPath)
 * 2. Tier 2: Adjacent "Lyrics" or "lyrics" subfolder relative to song file
 * 3. Tier 3: System "Music/Lyrics" folder on external storage
 */
class LyricsRepository {

    private val cache = ConcurrentHashMap<Long, List<LrcLine>>()

    /**
     * Resolves, parses, and caches lyrics for the given [song].
     * Returns null if no valid .lrc file was discovered or if parsed lines are empty.
     */
    suspend fun getLyricsForSong(song: Song): List<LrcLine>? = withContext(Dispatchers.IO) {
        cache[song.id]?.let { return@withContext it }

        val file = findLyricsFile(song) ?: return@withContext null
        val lines = LrcParser.parseFile(file)
        if (lines.isNotEmpty()) {
            cache[song.id] = lines
            lines
        } else {
            null
        }
    }

    /**
     * Multi-tier discovery looking for an .lrc file matching [song].
     */
    fun findLyricsFile(song: Song): File? {
        val audioPath = song.dataPath
        if (audioPath.isBlank()) return null

        val audioFile = try {
            File(audioPath)
        } catch (e: Exception) {
            null
        }

        val parentDir = audioFile?.parentFile
        val audioBaseName = audioFile?.nameWithoutExtension?.trim().orEmpty()
        val title = song.title.trim()
        val artist = song.artist.trim()
        val artistTitle = if (artist.isNotEmpty() && title.isNotEmpty()) "$artist - $title" else ""
        val titleArtist = if (title.isNotEmpty() && artist.isNotEmpty()) "$title - $artist" else ""

        // Tier 1: Same directory as song file (Song.dataPath)
        if (parentDir != null && parentDir.exists() && parentDir.isDirectory) {
            searchInDirectory(parentDir, audioBaseName, title, artistTitle, titleArtist)?.let { return it }
        }

        // Tier 2: Adjacent "Lyrics" or "lyrics" subfolder
        if (parentDir != null && parentDir.exists() && parentDir.isDirectory) {
            val subfolderCandidates = listOf("Lyrics", "lyrics", "LYRICS")
            for (subName in subfolderCandidates) {
                val subDir = File(parentDir, subName)
                if (subDir.exists() && subDir.isDirectory) {
                    searchInDirectory(subDir, audioBaseName, title, artistTitle, titleArtist)?.let { return it }
                }
            }
        }

        // Tier 3: System "Music/Lyrics" folder on external storage
        val systemMusicDirs = mutableListOf<File>()
        try {
            val publicMusicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            if (publicMusicDir != null) {
                systemMusicDirs.add(File(publicMusicDir, "Lyrics"))
                systemMusicDirs.add(File(publicMusicDir, "lyrics"))
            }
        } catch (_: Exception) {}

        // Fallback common Android paths
        systemMusicDirs.add(File("/storage/emulated/0/Music/Lyrics"))
        systemMusicDirs.add(File("/storage/emulated/0/Music/lyrics"))
        systemMusicDirs.add(File("/sdcard/Music/Lyrics"))
        systemMusicDirs.add(File("/sdcard/Music/lyrics"))

        for (dir in systemMusicDirs) {
            if (dir.exists() && dir.isDirectory) {
                searchInDirectory(dir, audioBaseName, title, artistTitle, titleArtist)?.let { return it }
            }
        }

        return null
    }

    /**
     * Searches a directory for LRC files matching audioBaseName, title, artistTitle, or titleArtist.
     * Evaluates exact names first, then case-insensitive matching.
     */
    private fun searchInDirectory(
        dir: File,
        audioBaseName: String,
        title: String,
        artistTitle: String,
        titleArtist: String = ""
    ): File? {
        val candidateNames = mutableListOf<String>()

        if (audioBaseName.isNotEmpty()) {
            candidateNames.add("$audioBaseName.lrc")
            candidateNames.add("$audioBaseName.LRC")
            val stripped = audioBaseName.replaceFirst(Regex("""^\d+[\s.-]+"""), "").trim()
            if (stripped.isNotEmpty() && stripped != audioBaseName) {
                candidateNames.add("$stripped.lrc")
                candidateNames.add("$stripped.LRC")
            }
        }
        if (title.isNotEmpty()) {
            candidateNames.add("$title.lrc")
            candidateNames.add("$title.LRC")
        }
        if (artistTitle.isNotEmpty()) {
            candidateNames.add("$artistTitle.lrc")
            candidateNames.add("$artistTitle.LRC")
        }
        if (titleArtist.isNotEmpty()) {
            candidateNames.add("$titleArtist.lrc")
            candidateNames.add("$titleArtist.LRC")
        }

        // Direct existence check
        for (name in candidateNames) {
            val candidate = File(dir, name)
            if (candidate.exists() && candidate.isFile && candidate.canRead() && candidate.length() > 0) {
                return candidate
            }
        }

        // Case-insensitive listing search
        val lrcFiles = dir.listFiles { file ->
            file.isFile && file.extension.equals("lrc", ignoreCase = true)
        } ?: return null

        for (name in candidateNames) {
            val matched = lrcFiles.firstOrNull { it.name.equals(name, ignoreCase = true) }
            if (matched != null && matched.canRead() && matched.length() > 0) {
                return matched
            }
        }

        return null
    }

    /**
     * Clears in-memory cached lyrics.
     */
    fun clearCache() {
        cache.clear()
    }

    companion object {
        val default = LyricsRepository()
    }
}
