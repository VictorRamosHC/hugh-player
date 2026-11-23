package com.hughdevelopment.player.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        StatsEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        DailyListeningEntity::class,
        WaveformEntity::class,
        DownloadEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class PlayerDatabase : RoomDatabase() {

    abstract fun statsDao(): StatsDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun dailyListeningDao(): DailyListeningDao
    abstract fun waveformDao(): WaveformDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: PlayerDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playback_stats ADD COLUMN is_hidden INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS song_waveforms (song_id INTEGER PRIMARY KEY NOT NULL, waveform_points TEXT NOT NULL)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `downloads` (
                        `video_id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `duration_seconds` INTEGER NOT NULL,
                        `stream_url` TEXT NOT NULL,
                        `original_url` TEXT NOT NULL,
                        `thumbnail_url` TEXT,
                        `target_playlist_id` INTEGER,
                        `format` TEXT NOT NULL,
                        `itag` INTEGER NOT NULL,
                        `bytes_downloaded` INTEGER NOT NULL,
                        `total_bytes` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `error_message` TEXT,
                        `created_at` INTEGER NOT NULL,
                        PRIMARY KEY(`video_id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_downloads_status` ON `downloads` (`status`)")
            }
        }

        fun getInstance(context: Context): PlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayerDatabase::class.java,
                    "hugh_player_stats.db"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
