package com.hughdevelopment.player.data.ai

import android.content.Context
import com.hughdevelopment.player.data.model.Song
import com.hughdevelopment.player.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * AgentDataManager (AI Bridge for Hugh Development)
 * 
 * Provides structured tools and data representations designed for on-device LLM
 * or local agents to inspect, classify, manage folders, and generate smart playlists.
 */
class AgentDataManager(
    private val context: Context,
    private val repository: MusicRepository
) {

    /**
     * Tool: Exports full library metadata as a structured JSON string.
     * Ideal for feeding an AI agent's context or system prompt.
     */
    suspend fun getLibraryContextForAgent(): String = withContext(Dispatchers.Default) {
        val songs: List<Song> = repository.getCurrentSongs()
        val root = JSONObject()
        val songsArray = JSONArray()

        for (song in songs) {
            val item = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("album", song.album)
                put("duration_sec", song.durationMs / 1000)
                put("path", song.dataPath)
                put("parent_dir", File(song.dataPath).parentFile?.name ?: "Unknown")
            }
            songsArray.put(item)
        }

        root.put("total_songs", songs.size)
        root.put("library", songsArray)
        root.toString(2)
    }

    /**
     * Tool: Scans and maps the local disk folder hierarchy where music files reside.
     * Enables an AI agent to see how the user's storage is partitioned.
     */
    suspend fun inspectFolderTree(): Map<String, List<String>> = withContext(Dispatchers.IO) {
        val songs: List<Song> = repository.getCurrentSongs()
        val folderMap = mutableMapOf<String, MutableList<String>>()

        for (song in songs) {
            val file = File(song.dataPath)
            val parentFolder = file.parentFile?.name ?: "Root"
            val list = folderMap.getOrPut(parentFolder) { mutableListOf() }
            list.add(song.title)
        }

        folderMap
    }

    /**
     * Tool: Prepares a proposal for organizing messy filenames into clean "Artist/Album/Song.mp3"
     * Ready for an agent to evaluate and suggest renames or moves.
     */
    fun proposeStandardizedPaths(songs: List<Song>): List<FileRenameProposal> {
        return songs.map { song ->
            val safeArtist = song.artist.replace(Regex("[^a-zA-Z0-9 _-]"), "").trim()
            val safeAlbum = song.album.replace(Regex("[^a-zA-Z0-9 _-]"), "").trim()
            val safeTitle = song.title.replace(Regex("[^a-zA-Z0-9 _-]"), "").trim()
            val ext = File(song.dataPath).extension.ifBlank { "mp3" }

            val targetPath = "Music/$safeArtist/$safeAlbum/$safeTitle.$ext"
            FileRenameProposal(
                songId = song.id,
                currentPath = song.dataPath,
                proposedRelativePath = targetPath,
                currentArtist = song.artist,
                currentTitle = song.title
            )
        }
    }
}

data class FileRenameProposal(
    val songId: Long,
    val currentPath: String,
    val proposedRelativePath: String,
    val currentArtist: String,
    val currentTitle: String
)
