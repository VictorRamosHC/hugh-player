package com.hughdevelopment.player.data.model

data class Playlist(
    val id: Long,
    val name: String,
    val category: String = "CUSTOM",
    val colorHex: String = "#F59E0B",
    val songs: List<Song> = emptyList(),
    val songCount: Int = 0
)
