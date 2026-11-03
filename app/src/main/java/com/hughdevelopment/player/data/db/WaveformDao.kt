package com.hughdevelopment.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WaveformDao {

    @Query("SELECT waveform_points FROM song_waveforms WHERE song_id = :songId")
    suspend fun getWaveformPoints(songId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaveform(waveform: WaveformEntity)
}
