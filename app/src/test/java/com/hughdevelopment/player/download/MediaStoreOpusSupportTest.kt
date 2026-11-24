package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.source.MediaStoreScanner
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStoreOpusSupportTest {

    @Test
    fun testMediaStoreScanner_selectionFilterIncludesOpusExtension() {
        val selection = MediaStoreScanner.DEFAULT_SELECTION
        assertTrue(
            "MediaStore query selection must include '%.opus' filter to support WebM Opus downloads",
            selection.contains("LIKE '%.opus'")
        )
    }

    @Test
    fun testMediaStoreScanner_selectionFilterIncludesOtherKeyFormats() {
        val selection = MediaStoreScanner.DEFAULT_SELECTION
        assertTrue(selection.contains("LIKE '%.mp3'"))
        assertTrue(selection.contains("LIKE '%.m4a'"))
        assertTrue(selection.contains("LIKE '%.wav'"))
        assertTrue(selection.contains("LIKE '%.aac'"))
    }
}
