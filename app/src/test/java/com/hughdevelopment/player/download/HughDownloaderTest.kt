package com.hughdevelopment.player.download

import com.hughdevelopment.player.data.download.HughCookieJar
import com.hughdevelopment.player.data.download.HughDownloader
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper

class HughDownloaderTest {

    @Test
    fun testHughCookieJar_preSeededWithConsentCookies() {
        val jar = HughCookieJar()
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ".toHttpUrl()
        val cookies = jar.loadForRequest(url)

        val socs = cookies.find { it.name == "SOCS" }
        val pref = cookies.find { it.name == "PREF" }

        assertNotNull("SOCS cookie must be present", socs)
        assertNotNull("PREF cookie must be present", pref)
        assertEquals("CAESEwgDEgk0ODEzNzg3MjEaAmVuIAEaBgiA_LyaBg", socs?.value)
        assertTrue(pref?.value?.contains("hl=en") == true)
    }

    @Test
    fun testHughCookieJar_preservesResponseCookies() {
        val jar = HughCookieJar()
        val url = "https://www.youtube.com/api/test".toHttpUrl()

        val visitorCookie = Cookie.Builder()
            .domain("youtube.com")
            .path("/")
            .name("VISITOR_INFO1_LIVE")
            .value("AbCdEf12345")
            .build()

        jar.saveFromResponse(url, listOf(visitorCookie))

        val loaded = jar.loadForRequest("https://www.youtube.com/watch?v=12345678901".toHttpUrl())
        val foundVisitor = loaded.find { it.name == "VISITOR_INFO1_LIVE" }

        assertNotNull("Visitor cookie should persist across requests", foundVisitor)
        assertEquals("AbCdEf12345", foundVisitor?.value)
    }

    @Test
    fun testHughCookieJar_domainMatching() {
        val jar = HughCookieJar()

        // Non-youtube URL should not have youtube consent cookies
        val googleUrl = "https://example.com/api".toHttpUrl()
        val cookies = jar.loadForRequest(googleUrl)
        assertFalse(cookies.any { it.name == "SOCS" })

        // Subdomain of youtube.com should receive consent cookies
        val subUrl = "https://music.youtube.com/watch?v=123".toHttpUrl()
        val subCookies = jar.loadForRequest(subUrl)
        assertTrue(subCookies.any { it.name == "SOCS" })
    }

    @Test
    fun testInitConsent_setsYoutubeParsingHelperConsentAccepted() {
        HughDownloader.initConsent()
        assertTrue("Consent must be accepted in NewPipe YoutubeParsingHelper", YoutubeParsingHelper.isConsentAccepted())
    }

    @Test
    fun testDefaultUserAgent_isDefinedAndModern() {
        val ua = HughDownloader.DEFAULT_USER_AGENT
        assertTrue("UA should be Chrome based", ua.contains("Chrome"))
        assertTrue("UA should contain Mozilla prefix", ua.startsWith("Mozilla/5.0"))
    }
}
