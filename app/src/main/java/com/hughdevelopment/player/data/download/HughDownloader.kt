package com.hughdevelopment.player.data.download

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Persistent thread-safe CookieJar that preserves session cookies across requests
 * and injects default YouTube consent (SOCS) and preferences (PREF) cookies.
 */
class HughCookieJar : CookieJar {
    private val cookieStore = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

    init {
        // Pre-populate default YouTube consent and preferences cookies
        val consentCookies = listOf(
            Cookie.Builder()
                .domain("youtube.com")
                .path("/")
                .name("SOCS")
                .value("CAESEwgDEgk0ODEzNzg3MjEaAmVuIAEaBgiA_LyaBg")
                .build(),
            Cookie.Builder()
                .domain("youtube.com")
                .path("/")
                .name("PREF")
                .value("f4=4000000&hl=en")
                .build(),
            Cookie.Builder()
                .domain("youtube.com")
                .path("/")
                .name("CONSENT")
                .value("PENDING+999")
                .build()
        )
        val youtubeCookies = ConcurrentHashMap<String, Cookie>()
        consentCookies.forEach { youtubeCookies[it.name] = it }
        cookieStore["youtube.com"] = youtubeCookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val hostKey = getHostKey(url.host)
        val domainMap = cookieStore.getOrPut(hostKey) { ConcurrentHashMap() }
        cookies.forEach { cookie ->
            domainMap[cookie.name] = cookie
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val result = mutableListOf<Cookie>()
        val seenNames = mutableSetOf<String>()

        // Check exact host and parent domains (e.g. www.youtube.com, youtube.com)
        cookieStore.forEach { (domain, cookieMap) ->
            if (host == domain || host.endsWith(".$domain")) {
                cookieMap.values.forEach { cookie ->
                    if (cookie.matches(url) && seenNames.add(cookie.name)) {
                        result.add(cookie)
                    }
                }
            }
        }

        // For any YouTube host, ensure SOCS and PREF are always present
        if (host.contains("youtube.com")) {
            if (seenNames.add("SOCS")) {
                result.add(
                    Cookie.Builder()
                        .domain(host)
                        .path("/")
                        .name("SOCS")
                        .value("CAESEwgDEgk0ODEzNzg3MjEaAmVuIAEaBgiA_LyaBg")
                        .build()
                )
            }
            if (seenNames.add("PREF")) {
                result.add(
                    Cookie.Builder()
                        .domain(host)
                        .path("/")
                        .name("PREF")
                        .value("f4=4000000&hl=en")
                        .build()
                )
            }
        }

        return result
    }

    private fun getHostKey(host: String): String {
        return if (host.contains("youtube.com")) "youtube.com" else host
    }
}

/**
 * High-performance OkHttp Downloader implementation for NewPipeExtractor.
 * Optimized with persistent CookieJar, connection pooling, keep-alive,
 * and clean User-Agent handling.
 */
class HughDownloader private constructor(private val client: OkHttpClient) : Downloader() {

    override fun execute(request: org.schabi.newpipe.extractor.downloader.Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = Request.Builder().url(url)

        // Clean User-Agent handling: do not add duplicate or conflicting User-Agent headers
        var hasUserAgent = false
        headers.forEach { (name, values) ->
            if (name.equals("User-Agent", ignoreCase = true)) {
                hasUserAgent = true
                if (values.isNotEmpty()) {
                    requestBuilder.header("User-Agent", values.first())
                }
            } else {
                values.forEach { value ->
                    requestBuilder.addHeader(name, value)
                }
            }
        }

        if (!hasUserAgent) {
            requestBuilder.header(
                "User-Agent",
                DEFAULT_USER_AGENT
            )
        }

        val requestBody = dataToSend?.toRequestBody(null)
        requestBuilder.method(httpMethod, requestBody)

        val okHttpResponse = client.newCall(requestBuilder.build()).execute()

        if (okHttpResponse.code == 429) {
            okHttpResponse.close()
            throw ReCaptchaException("reCAPTCHA challenge detected by YouTube", url)
        }

        val responseBody = okHttpResponse.body?.string().orEmpty()
        val responseHeaders = okHttpResponse.headers.toMultimap()

        return Response(
            okHttpResponse.code,
            okHttpResponse.message,
            responseHeaders,
            responseBody,
            okHttpResponse.request.url.toString()
        )
    }

    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

        @Volatile
        private var INSTANCE: HughDownloader? = null

        val cookieJar: HughCookieJar by lazy {
            HughCookieJar()
        }

        val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }

        fun initConsent() {
            try {
                YoutubeParsingHelper.setConsentAccepted(true)
            } catch (_: Throwable) {}
        }

        fun getInstance(): HughDownloader {
            initConsent()
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HughDownloader(okHttpClient).also { INSTANCE = it }
            }
        }
    }
}
