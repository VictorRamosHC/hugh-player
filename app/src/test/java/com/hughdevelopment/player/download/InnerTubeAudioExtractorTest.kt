package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.download.AetherJson
import com.hughdevelopment.player.data.download.InnerTubeAudioExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class InnerTubeAudioExtractorTest {

    @Test
    fun testParsePlayerResponse_extractsM4aAudioSuccessfully() {
        val sampleJson = """
        {
          "playabilityStatus": {
            "status": "OK"
          },
          "videoDetails": {
            "videoId": "testVideoId1",
            "title": "Quantum Resonance / Track 01",
            "author": "Delamain Audio",
            "lengthSeconds": "240",
            "thumbnail": {
              "thumbnails": [
                { "url": "https://i.ytimg.com/vi/testVideoId1/default.jpg", "width": 120, "height": 90 },
                { "url": "https://i.ytimg.com/vi/testVideoId1/hqdefault.jpg", "width": 480, "height": 360 },
                { "url": "https://i.ytimg.com/vi/testVideoId1/maxresdefault.jpg", "width": 1280, "height": 720 }
              ]
            }
          },
          "streamingData": {
            "adaptiveFormats": [
              {
                "itag": 251,
                "mimeType": "audio/webm; codecs=\"opus\"",
                "bitrate": 160000,
                "url": "https://rr1---sn-test.googlevideo.com/videoplayback?itag=251"
              },
              {
                "itag": 140,
                "mimeType": "audio/mp4; codecs=\"mp4a.40.2\"",
                "bitrate": 128000,
                "url": "https://rr1---sn-test.googlevideo.com/videoplayback?itag=140"
              }
            ]
          }
        }
        """.trimIndent()

        val json = AetherJson.parse(sampleJson) as AetherJson.Obj
        val info = InnerTubeAudioExtractor.parsePlayerResponse("testVideoId1", json, "INNERTUBE_ANDROID")

        assertEquals("testVideoId1", info.videoId)
        assertEquals("Quantum Resonance  Track 01", info.title)
        assertEquals("Delamain Audio", info.artist)
        assertEquals(240L, info.durationSeconds)
        assertEquals("m4a", info.format)
        assertEquals(140, info.itag)
        assertEquals(128, info.averageBitrate)
        assertEquals("https://rr1---sn-test.googlevideo.com/videoplayback?itag=140", info.streamUrl)
        assertEquals("https://i.ytimg.com/vi/testVideoId1/maxresdefault.jpg", info.thumbnailUrl)
        assertEquals("INNERTUBE_ANDROID", info.extractorTier)
    }

    @Test
    fun testParsePlayerResponse_selectsOpusWhenM4aAbsent() {
        val sampleJson = """
        {
          "playabilityStatus": {
            "status": "OK"
          },
          "videoDetails": {
            "videoId": "opusVideoId2",
            "title": "Neon Grid Echoes",
            "author": "Synth Collective",
            "lengthSeconds": "180",
            "thumbnail": {
              "thumbnails": [
                { "url": "https://i.ytimg.com/vi/opusVideoId2/hqdefault.jpg", "width": 480, "height": 360 }
              ]
            }
          },
          "streamingData": {
            "adaptiveFormats": [
              {
                "itag": 251,
                "mimeType": "audio/webm; codecs=\"opus\"",
                "bitrate": 160000,
                "url": "https://rr1---sn-test.googlevideo.com/videoplayback?itag=251"
              }
            ]
          }
        }
        """.trimIndent()

        val json = AetherJson.parse(sampleJson) as AetherJson.Obj
        val info = InnerTubeAudioExtractor.parsePlayerResponse("opusVideoId2", json, "INNERTUBE_ANDROID")

        assertEquals("opusVideoId2", info.videoId)
        assertEquals("opus", info.format)
        assertEquals(251, info.itag)
        assertEquals(160, info.averageBitrate)
        assertTrue(info.streamUrl.contains("itag=251"))
    }

    @Test
    fun testParsePlayerResponse_extractsFromSignatureCipher() {
        val cipherUrl = "https%3A%2F%2Frr1---sn-test.googlevideo.com%2Fvideoplayback%3Fitag%3D140%26source%3Dyoutube"
        val sampleJson = """
        {
          "playabilityStatus": {
            "status": "OK"
          },
          "videoDetails": {
            "videoId": "cipherVideo3",
            "title": "Encrypted Stream Track",
            "author": "Cyber Cipher",
            "lengthSeconds": "300"
          },
          "streamingData": {
            "adaptiveFormats": [
              {
                "itag": 140,
                "mimeType": "audio/mp4; codecs=\"mp4a.40.2\"",
                "bitrate": 128000,
                "signatureCipher": "s=mockSignature123&sp=sig&url=$cipherUrl"
              }
            ]
          }
        }
        """.trimIndent()

        val json = AetherJson.parse(sampleJson) as AetherJson.Obj
        val info = InnerTubeAudioExtractor.parsePlayerResponse("cipherVideo3", json, "INNERTUBE_ANDROID")

        assertEquals("cipherVideo3", info.videoId)
        assertEquals("https://rr1---sn-test.googlevideo.com/videoplayback?itag=140&source=youtube", info.streamUrl)
    }

    @Test
    fun testParsePlayerResponse_throwsWhenUnplayable() {
        val sampleJson = """
        {
          "playabilityStatus": {
            "status": "UNPLAYABLE",
            "reason": "The page needs to be reloaded."
          },
          "videoDetails": {
            "videoId": "blockedVideo4"
          }
        }
        """.trimIndent()

        val json = AetherJson.parse(sampleJson) as AetherJson.Obj
        try {
            InnerTubeAudioExtractor.parsePlayerResponse("blockedVideo4", json, "INNERTUBE_ANDROID")
            fail("Should throw IllegalStateException when video status is UNPLAYABLE")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("The page needs to be reloaded.") == true)
        }
    }
}
