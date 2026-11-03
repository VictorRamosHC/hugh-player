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
        DailyListeningEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class PlayerDatabase : RoomDatabase() {

    abstract fun statsDao(): StatsDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun dailyListeningDao(): DailyListeningDao

    companion object {
        @Volatile
        private var INSTANCE: PlayerDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playback_stats ADD COLUMN is_hidden INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): PlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayerDatabase::class.java,
                    "hugh_player_stats.db"
                )
                .addMigrations(MIGRATION_5_6)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
