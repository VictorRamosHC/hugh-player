package com.hughdevelopment.player.data.download

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * High-performance OkHttp Downloader implementation for NewPipeExtractor.
 * Optimized with connection pooling, keep-alive, and modern browser headers to eliminate latency.
 */
class HughDownloader private constructor(private val client: OkHttpClient) : Downloader() {

    override fun execute(request: org.schabi.newpipe.extractor.downloader.Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            )

        headers.forEach { (name, values) ->
            values.forEach { value ->
                requestBuilder.addHeader(name, value)
            }
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
        @Volatile
        private var INSTANCE: HughDownloader? = null

        val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }

        fun getInstance(): HughDownloader {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HughDownloader(okHttpClient).also { INSTANCE = it }
            }
        }
    }
}
