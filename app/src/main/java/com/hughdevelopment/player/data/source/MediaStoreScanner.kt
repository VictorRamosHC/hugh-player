package com.hughdevelopment.player.data.source

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.hughdevelopment.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(private val context: Context) {

    private val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Exclude system sounds/ringtones/alarms and files shorter than 5 seconds
        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.DATA} LIKE '%.mp3' OR ${MediaStore.Audio.Media.DATA} LIKE '%.m4a' OR ${MediaStore.Audio.Media.DATA} LIKE '%.wav' OR ${MediaStore.Audio.Media.DATA} LIKE '%.aac') AND ${MediaStore.Audio.Media.DURATION} >= 5000 AND ${MediaStore.Audio.Media.IS_RINGTONE} == 0 AND ${MediaStore.Audio.Media.IS_NOTIFICATION} == 0 AND ${MediaStore.Audio.Media.IS_ALARM} == 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn) ?: ""
                val rawTitle = cursor.getString(titleColumn) ?: "Desconhecido"

                // Intelligent filter: ignore typical voice notes, WhatsApp audios and phone recordings
                if (isNoiseOrRecording(path, rawTitle)) {
                    continue
                }

                val id = cursor.getLong(idColumn)
                val rawArtist = cursor.getString(artistColumn) ?: "Artista Desconhecido"
                val rawAlbum = cursor.getString(albumColumn) ?: "Álbum Desconhecido"
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val albumArtUri = if (albumId > 0) {
                    ContentUris.withAppendedId(albumArtBaseUri, albumId)
                } else null

                songList.add(
                    Song(
                        id = id,
                        title = cleanMetadata(rawTitle),
                        artist = cleanMetadata(rawArtist),
                        album = cleanMetadata(rawAlbum),
                        durationMs = duration,
                        contentUri = contentUri,
                        albumArtUri = albumArtUri,
                        dataPath = path,
                        sizeBytes = size,
                        dateAdded = dateAdded
                    )
                )
            }
        }

        songList
    }

    private fun isNoiseOrRecording(path: String, title: String): Boolean {
        val lowerPath = path.lowercase()

        // Exclude system notification sounds, alarms and ringtones
        if (lowerPath.contains("/ringtones/") ||
            lowerPath.contains("/notifications/") ||
            lowerPath.contains("/alarms/") ||
            lowerPath.contains("/system/media/") ||
            lowerPath.contains("/.cache/") ||
            lowerPath.contains("temp_audio")
        ) {
            return true
        }

        return false
    }

    private fun cleanMetadata(text: String): String {
        return if (text.isBlank() || text.equals("<unknown>", ignoreCase = true)) {
            "Desconhecido"
        } else {
            text.trim()
        }
    }
}
