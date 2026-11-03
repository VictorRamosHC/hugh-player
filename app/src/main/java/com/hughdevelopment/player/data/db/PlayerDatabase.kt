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
        WaveformEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class PlayerDatabase : RoomDatabase() {

    abstract fun statsDao(): StatsDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun dailyListeningDao(): DailyListeningDao
    abstract fun waveformDao(): WaveformDao

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

        fun getInstance(context: Context): PlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayerDatabase::class.java,
                    "hugh_player_stats.db"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
