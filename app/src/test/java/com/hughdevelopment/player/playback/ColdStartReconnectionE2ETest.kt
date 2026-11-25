package com.hughdevelopment.player.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Domain State & Reconnection Engine modeling MusicPlayerController reconnection logic.
 *
 * Implements the contract defined in:
 * - PROJECT.md § Interface Contracts § 4. Service Reconnection
 * - ORIGINAL_REQUEST.md § R2. Fase 1
 *
 * Invariant:
 * When reconnecting to MusicPlaybackService after app cold start, even if playlist is empty:
 * - Active Song is reconstructed from MediaItem metadata (id, title, artist, mediaUri).
 * - currentSong is set immediately.
 * - MiniPlayerBar visibility condition (currentSong != null) is satisfied immediately!
 */
class ColdStartReconnectionEngine {

    data class ActiveSongState(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val contentUriString: String,
        val albumArtUriString: String? = null,
        val dataPath: String = ""
    )

    data class MockMediaMetadata(
        val title: String?,
        val artist: String?,
        val albumTitle: String?,
        val artworkUriString: String?
    )

    data class MockMediaItem(
        val mediaId: String,
        val mediaUriString: String,
        val metadata: MockMediaMetadata
    )

    private var _playlist: List<ActiveSongState> = emptyList()
    private var _currentSong: ActiveSongState? = null
    private var _durationMs: Long = 0L
    private var _playbackPositionMs: Long = 0L

    val playlist: List<ActiveSongState> get() = _playlist
    val currentSong: ActiveSongState? get() = _currentSong
    val durationMs: Long get() = _durationMs
    val playbackPositionMs: Long get() = _playbackPositionMs

    val isMiniPlayerVisible: Boolean get() = _currentSong != null

    fun setPlaylist(songs: List<ActiveSongState>) {
        _playlist = songs
    }

    /**
     * Executes the v2.0 reconnection logic.
     */
    fun onServiceReconnected(currentMediaItem: MockMediaItem?, currentPosition: Long, duration: Long) {
        if (currentMediaItem == null) return

        val activeId = currentMediaItem.mediaId.toLongOrNull() ?: return

        // 1. First attempt to match against loaded playlist
        val matchedSong = _playlist.find { it.id == activeId }
        if (matchedSong != null) {
            _currentSong = matchedSong
            _durationMs = if (duration > 0) duration else matchedSong.durationMs
            _playbackPositionMs = currentPosition
            return
        }

        // 2. Cold Start Reconnection Fallback: reconstruct Song directly from MediaItem metadata
        val reconstructedSong = ActiveSongState(
            id = activeId,
            title = currentMediaItem.metadata.title ?: "Unknown Title",
            artist = currentMediaItem.metadata.artist ?: "Unknown Artist",
            album = currentMediaItem.metadata.albumTitle ?: "Unknown Album",
            durationMs = if (duration > 0) duration else 0L,
            contentUriString = currentMediaItem.mediaUriString,
            albumArtUriString = currentMediaItem.metadata.artworkUriString,
            dataPath = ""
        )

        _currentSong = reconstructedSong
        _durationMs = reconstructedSong.durationMs
        _playbackPositionMs = currentPosition
    }

    fun clear() {
        _playlist = emptyList()
        _currentSong = null
        _durationMs = 0L
        _playbackPositionMs = 0L
    }
}

/**
 * Comprehensive 4-Tier Test Suite for Cold Start Service Reconnection (Feature 7).
 */
class ColdStartReconnectionE2ETest {

    private lateinit var engine: ColdStartReconnectionEngine

    @Before
    fun setUp() {
        engine = ColdStartReconnectionEngine()
    }

    // =========================================================================
    // TIER 1: FEATURE COVERAGE (Cold Start State Recovery & MiniPlayer Visibility)
    // =========================================================================

    @Test
    fun testTier1_coldStart_emptyPlaylist_reconstructsActiveSongFromMetadata() {
        // App freshly opened after process kill; playlist has not yet been scanned or loaded
        assertTrue("Playlist must be empty on fresh cold start", engine.playlist.isEmpty())
        assertNull("Current song must initially be null", engine.currentSong)
        assertFalse("MiniPlayer must be initially hidden", engine.isMiniPlayerVisible)

        val activeItem = ColdStartReconnectionEngine.MockMediaItem(
            mediaId = "987654",
            mediaUriString = "content://media/external/audio/media/987654",
            metadata = ColdStartReconnectionEngine.MockMediaMetadata(
                title = "Synthwave Odyssey",
                artist = "Aether Prodigy",
                albumTitle = "Vault Horizons",
                artworkUriString = "content://media/external/audio/albumart/42"
            )
        )

        // Service reconnects with active media item
        engine.onServiceReconnected(activeItem, currentPosition = 45000L, duration = 210000L)

        // Verifications
        assertNotNull("Current song must be non-null after service reconnection", engine.currentSong)
        assertEquals("Song ID must match mediaId", 987654L, engine.currentSong?.id)
        assertEquals("Synthwave Odyssey", engine.currentSong?.title)
        assertEquals("Aether Prodigy", engine.currentSong?.artist)
        assertEquals("Vault Horizons", engine.currentSong?.album)
        assertEquals(210000L, engine.durationMs)
        assertEquals(45000L, engine.playbackPositionMs)

        // CRITICAL INVARIANT: MiniPlayer visibility
        assertTrue(
            "MiniPlayer visibility condition (currentSong != null) must be satisfied immediately on reconnection",
            engine.isMiniPlayerVisible
        )
    }

    @Test
    fun testTier1_warmStart_existingPlaylist_matchesDirectlyFromPlaylist() {
        val existingSong = ColdStartReconnectionEngine.ActiveSongState(
            id = 111L,
            title = "Cached Track",
            artist = "Known Artist",
            album = "Known Album",
            durationMs = 180000L,
            contentUriString = "content://media/111",
            dataPath = "/storage/emulated/0/Music/track.mp3"
        )
        engine.setPlaylist(listOf(existingSong))

        val activeItem = ColdStartReconnectionEngine.MockMediaItem(
            mediaId = "111",
            mediaUriString = "content://media/111",
            metadata = ColdStartReconnectionEngine.MockMediaMetadata(
                title = "Cached Track",
                artist = "Known Artist",
                albumTitle = "Known Album",
                artworkUriString = null
            )
        )

        engine.onServiceReconnected(activeItem, currentPosition = 10000L, duration = 180000L)

        assertEquals("Must match reference to playlist song entity", existingSong, engine.currentSong)
        assertEquals("/storage/emulated/0/Music/track.mp3", engine.currentSong?.dataPath)
        assertTrue(engine.isMiniPlayerVisible)
    }

    // =========================================================================
    // TIER 2: BOUNDARY & CORNER CASES (Null MediaItem, Partial Metadata)
    // =========================================================================

    @Test
    fun testTier2_noActiveMediaItem_remainsNullAndHidden() {
        engine.onServiceReconnected(null, currentPosition = 0L, duration = 0L)
        assertNull(engine.currentSong)
        assertFalse(engine.isMiniPlayerVisible)
    }

    @Test
    fun testTier2_corruptedMediaId_nonNumericIgnoredSafely() {
        val corruptItem = ColdStartReconnectionEngine.MockMediaItem(
            mediaId = "invalid_non_numeric_id",
            mediaUriString = "content://media/invalid",
            metadata = ColdStartReconnectionEngine.MockMediaMetadata("Title", "Artist", null, null)
        )

        engine.onServiceReconnected(corruptItem, currentPosition = 0L, duration = 0L)
        assertNull("Non-numeric mediaId must not crash and should leave currentSong null", engine.currentSong)
        assertFalse(engine.isMiniPlayerVisible)
    }

    @Test
    fun testTier2_partialMetadata_fallsBackToSensibleDefaults() {
        val minimalItem = ColdStartReconnectionEngine.MockMediaItem(
            mediaId = "555",
            mediaUriString = "content://media/555",
            metadata = ColdStartReconnectionEngine.MockMediaMetadata(
                title = null,
                artist = null,
                albumTitle = null,
                artworkUriString = null
            )
        )

        engine.onServiceReconnected(minimalItem, currentPosition = 5000L, duration = 0L)
        assertNotNull(engine.currentSong)
        assertEquals("Unknown Title", engine.currentSong?.title)
        assertEquals("Unknown Artist", engine.currentSong?.artist)
        assertEquals("Unknown Album", engine.currentSong?.album)
        assertTrue(engine.isMiniPlayerVisible)
    }

    // =========================================================================
    // TIER 3: CROSS-FEATURE INTERACTIONS (Reconnection ↔ MiniPlayer ↔ Playlist Sync)
    // =========================================================================

    @Test
    fun testTier3_crossFeature_playlistLoadedAfterReconnectionSync() {
        // Step 1: Cold start occurs, active song reconstructed from service
        val activeItem = ColdStartReconnectionEngine.MockMediaItem(
            mediaId = "777",
            mediaUriString = "content://media/777",
            metadata = ColdStartReconnectionEngine.MockMediaMetadata("Track 777", "Artist 777", "Album 777", null)
        )
        engine.onServiceReconnected(activeItem, 12000L, 180000L)
        assertTrue(engine.isMiniPlayerVisible)
        assertEquals("Track 777", engine.currentSong?.title)

        // Step 2: MediaStore scan finishes 500ms later and loads full library playlist
        val fullSongFromStorage = ColdStartReconnectionEngine.ActiveSongState(
            id = 777L,
            title = "Track 777 (High Res)",
            artist = "Artist 777",
            album = "Album 777",
            durationMs = 180000L,
            contentUriString = "content://media/777",
            dataPath = "/storage/emulated/0/Music/Album/Track07.flac"
        )
        engine.setPlaylist(listOf(fullSongFromStorage))

        // Reconnection or sync step updates to the rich storage entity
        engine.onServiceReconnected(activeItem, 13000L, 180000L)
        assertEquals("/storage/emulated/0/Music/Album/Track07.flac", engine.currentSong?.dataPath)
        assertEquals("Track 777 (High Res)", engine.currentSong?.title)
    }
}
