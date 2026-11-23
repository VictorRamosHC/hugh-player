package com.hughdevelopment.player.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY created_at DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY created_at DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING', 'DOWNLOADING') ORDER BY created_at ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY created_at DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE video_id = :videoId LIMIT 1")
    suspend fun getDownload(videoId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE video_id = :videoId LIMIT 1")
    fun observeDownload(videoId: String): Flow<DownloadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadEntity)

    @Query("UPDATE downloads SET bytes_downloaded = :bytesDownloaded, total_bytes = :totalBytes, status = :status WHERE video_id = :videoId")
    suspend fun updateProgress(videoId: String, bytesDownloaded: Long, totalBytes: Long, status: String)

    @Query("UPDATE downloads SET status = :status, error_message = :errorMessage WHERE video_id = :videoId")
    suspend fun updateStatus(videoId: String, status: String, errorMessage: String? = null)

    @Query("DELETE FROM downloads WHERE video_id = :videoId")
    suspend fun deleteDownload(videoId: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}
