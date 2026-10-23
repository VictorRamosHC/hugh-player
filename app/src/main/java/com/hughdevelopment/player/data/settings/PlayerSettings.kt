package com.hughdevelopment.player.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SortOrder(val label: String) {
    DEFAULT("A-Z"),
    DATE_ADDED_DESC("RECENTES"),
    SIZE_DESC("MAIORES (MB)"),
    MOST_PLAYED("MAIS OUVIDAS"),
    FAVORITES_FIRST("FAVORITAS")
}

data class SettingsState(
    val isFadeEnabled: Boolean = true,
    val fadeDurationSeconds: Float = 0.8f,
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val folderSortOrder: SortOrder = SortOrder.DATE_ADDED_DESC,
    val libraryWallpaperAlpha: Float = 0.30f,
    val playerWallpaperAlpha: Float = 0.35f,
    val isHapticEnabled: Boolean = true,
    val activeThemeId: String = "AETHER_CLASSIC"
)

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    private fun loadSettings(): SettingsState {
        val fadeEnabled = prefs.getBoolean(KEY_FADE_ENABLED, true)
        val fadeDuration = prefs.getFloat(KEY_FADE_DURATION, 1.5f)
        val sortName = prefs.getString(KEY_SORT_ORDER, SortOrder.DEFAULT.name) ?: SortOrder.DEFAULT.name
        val sortOrder = try {
            SortOrder.valueOf(sortName)
        } catch (e: Exception) {
            SortOrder.DEFAULT
        }
        val folderSortName = prefs.getString(KEY_FOLDER_SORT_ORDER, SortOrder.DATE_ADDED_DESC.name) ?: SortOrder.DATE_ADDED_DESC.name
        val folderSortOrder = try {
            SortOrder.valueOf(folderSortName)
        } catch (e: Exception) {
            SortOrder.DATE_ADDED_DESC
        }
        val libAlpha = prefs.getFloat(KEY_LIB_ALPHA, 0.30f)
        val playerAlpha = prefs.getFloat(KEY_PLAYER_ALPHA, 0.35f)
        val haptic = prefs.getBoolean(KEY_HAPTIC, true)
        val themeId = prefs.getString(KEY_THEME_ID, "AETHER_CLASSIC") ?: "AETHER_CLASSIC"

        return SettingsState(
            isFadeEnabled = fadeEnabled,
            fadeDurationSeconds = fadeDuration,
            sortOrder = sortOrder,
            folderSortOrder = folderSortOrder,
            libraryWallpaperAlpha = libAlpha,
            playerWallpaperAlpha = playerAlpha,
            isHapticEnabled = haptic,
            activeThemeId = themeId
        )
    }

    fun setThemeId(themeId: String) {
        prefs.edit().putString(KEY_THEME_ID, themeId).apply()
        _settings.value = _settings.value.copy(activeThemeId = themeId)
    }

    fun setFadeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FADE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isFadeEnabled = enabled)
    }

    fun setFadeDuration(seconds: Float) {
        val clamped = seconds.coerceIn(0.5f, 15.0f)
        prefs.edit().putFloat(KEY_FADE_DURATION, clamped).apply()
        _settings.value = _settings.value.copy(fadeDurationSeconds = clamped)
    }

    fun setSortOrder(order: SortOrder) {
        prefs.edit().putString(KEY_SORT_ORDER, order.name).apply()
        _settings.value = _settings.value.copy(sortOrder = order)
    }

    fun setFolderSortOrder(order: SortOrder) {
        prefs.edit().putString(KEY_FOLDER_SORT_ORDER, order.name).apply()
        _settings.value = _settings.value.copy(folderSortOrder = order)
    }

    fun setLibraryWallpaperAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.05f, 0.90f)
        prefs.edit().putFloat(KEY_LIB_ALPHA, clamped).apply()
        _settings.value = _settings.value.copy(libraryWallpaperAlpha = clamped)
    }

    fun setPlayerWallpaperAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.05f, 0.90f)
        prefs.edit().putFloat(KEY_PLAYER_ALPHA, clamped).apply()
        _settings.value = _settings.value.copy(playerWallpaperAlpha = clamped)
    }

    // --- Session Persistence: Remember Last Played Song & Position ---
    fun saveLastPlaybackSession(songId: Long, positionMs: Long, playlistId: Long? = null) {
        prefs.edit()
            .putLong(KEY_LAST_SONG_ID, songId)
            .putLong(KEY_LAST_POSITION_MS, positionMs.coerceAtLeast(0L))
            .apply {
                if (playlistId != null) {
                    putLong(KEY_LAST_PLAYLIST_ID, playlistId)
                } else {
                    remove(KEY_LAST_PLAYLIST_ID)
                }
            }
            .apply()
    }

    fun getLastPlayedSongId(): Long? {
        val id = prefs.getLong(KEY_LAST_SONG_ID, -1L)
        return if (id > 0) id else null
    }

    fun getLastPlayedPositionMs(): Long {
        return prefs.getLong(KEY_LAST_POSITION_MS, 0L).coerceAtLeast(0L)
    }

    fun getLastPlayedPlaylistId(): Long? {
        val id = prefs.getLong(KEY_LAST_PLAYLIST_ID, -1L)
        return if (id > 0) id else null
    }

    companion object {
        private const val PREFS_NAME = "hugh_player_vault_prefs"
        private const val KEY_FADE_ENABLED = "key_fade_enabled"
        private const val KEY_FADE_DURATION = "key_fade_duration"
        private const val KEY_SORT_ORDER = "key_sort_order"
        private const val KEY_FOLDER_SORT_ORDER = "key_folder_sort_order"
        private const val KEY_LIB_ALPHA = "key_lib_alpha"
        private const val KEY_PLAYER_ALPHA = "key_player_alpha"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_THEME_ID = "key_theme_id"

        private const val KEY_LAST_SONG_ID = "key_last_song_id"
        private const val KEY_LAST_POSITION_MS = "key_last_position_ms"
        private const val KEY_LAST_PLAYLIST_ID = "key_last_playlist_id"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
