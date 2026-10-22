package com.hughdevelopment.player.ui.screens

import com.hughdevelopment.player.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistCoverResolverTest {

    @Test
    fun testDefaultFourFolders_resolveCorrectCovers() {
        assertEquals(
            R.drawable.cover_christian_vocal,
            getPlaylistCoverResource("Músicas Cristãs (Cantado)", "CRISTÃS (CANTADO)")
        )
        assertEquals(
            R.drawable.cover_christian_playback,
            getPlaylistCoverResource("Músicas Cristãs (Playback)", "CRISTÃS (PLAYBACK)")
        )
        assertEquals(
            R.drawable.cover_daily_music,
            getPlaylistCoverResource("Músicas Dia a Dia", "DIA A DIA")
        )
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Áudios de Reuniões", "REUNIÕES")
        )
    }

    @Test
    fun testEdgeCases_unaccentedVariations() {
        // Without tilde or diacritics
        assertEquals(
            R.drawable.cover_christian_vocal,
            getPlaylistCoverResource("Musicas Cristas (Cantado)", "CRISTAS")
        )
        assertEquals(
            R.drawable.cover_christian_playback,
            getPlaylistCoverResource("Musicas Cristas (Playback)", "PLAYBACK")
        )
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Audios de Reunioes", "REUNIOES")
        )
    }

    @Test
    fun testEdgeCases_singularReuniao() {
        // Singular "Reunião" contains 'ã', NOT 'õ' (which broke the previous contains("Reuniõ"))
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Reunião de Segunda", "GERAL")
        )
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Áudio da Reunião", "")
        )
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Reuniao de Equipe", "")
        )
        assertEquals(
            R.drawable.cover_meetings_vault,
            getPlaylistCoverResource("Ata de Reunião", "TRABALHO")
        )
    }

    @Test
    fun testEdgeCases_synonymsAndKeywords() {
        // Christian vocal synonyms
        assertEquals(
            R.drawable.cover_christian_vocal,
            getPlaylistCoverResource("Louvores da Harpa", "")
        )
        assertEquals(
            R.drawable.cover_christian_vocal,
            getPlaylistCoverResource("Hinos Cristãos", "")
        )
        assertEquals(
            R.drawable.cover_christian_vocal,
            getPlaylistCoverResource("Gospel Acústico", "")
        )

        // Playback synonyms
        assertEquals(
            R.drawable.cover_christian_playback,
            getPlaylistCoverResource("Instrumental Harpa", "")
        )
        assertEquals(
            R.drawable.cover_christian_playback,
            getPlaylistCoverResource("Playback Tom Alto", "")
        )

        // Daily synonyms
        assertEquals(
            R.drawable.cover_daily_music,
            getPlaylistCoverResource("Cotidiano no Trabalho", "")
        )
        assertEquals(
            R.drawable.cover_daily_music,
            getPlaylistCoverResource("Dia-a-dia", "")
        )
        assertEquals(
            R.drawable.cover_daily_music,
            getPlaylistCoverResource("Rotina Matinal", "")
        )
    }

    @Test
    fun testUnmatched_returnsNull() {
        assertNull(getPlaylistCoverResource("Eletrônica Pesada", "TECHNO"))
        assertNull(getPlaylistCoverResource("Jazz & Blues", "JAZZ"))
        assertNull(getPlaylistCoverResource("", ""))
    }
}
