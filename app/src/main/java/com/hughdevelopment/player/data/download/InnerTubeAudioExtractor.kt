package com.hughdevelopment.player.data.download

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLDecoder

/**
 * Interface contract for multi-tier audio stream extraction.
 */
interface AudioStreamExtractor {
    suspend fun extractAudio(url: String): ExtractedAudioInfo
}

/**
 * Lightweight, zero-dependency JSON AST parser for reliable parsing across
 * both Android runtime and pure JVM unit tests.
 */
sealed class AetherJson {
    data class Obj(val map: Map<String, AetherJson>) : AetherJson() {
        fun getString(key: String): String? {
            val v = map[key] ?: return null
            return when (v) {
                is Str -> v.value
                is Num -> v.value
                is Bool -> v.value.toString()
                else -> null
            }
        }

        fun getLong(key: String, default: Long = 0L): Long {
            val v = map[key] ?: return default
            val raw = when (v) {
                is Num -> v.value
                is Str -> v.value
                else -> return default
            }
            return raw.toLongOrNull()
                ?: raw.toDoubleOrNull()?.takeIf { it.isFinite() }?.toLong()
                ?: default
        }

        fun getInt(key: String, default: Int = 0): Int {
            val v = map[key] ?: return default
            val raw = when (v) {
                is Num -> v.value
                is Str -> v.value
                else -> return default
            }
            return raw.toIntOrNull()
                ?: raw.toDoubleOrNull()?.takeIf { it.isFinite() }?.toInt()
                ?: default
        }

        fun getDouble(key: String, default: Double = 0.0): Double {
            val v = map[key] ?: return default
            val raw = when (v) {
                is Num -> v.value
                is Str -> v.value
                else -> return default
            }
            return raw.toDoubleOrNull()?.takeIf { it.isFinite() } ?: default
        }

        fun getObj(key: String): Obj? = map[key] as? Obj
        fun getArr(key: String): Arr? = map[key] as? Arr
    }

    data class Arr(val list: List<AetherJson>) : AetherJson() {
        val size: Int get() = list.size
        fun isNotEmpty(): Boolean = list.isNotEmpty()
        fun isEmpty(): Boolean = list.isEmpty()
        fun getObj(index: Int): Obj? = list.getOrNull(index) as? Obj
        fun getString(index: Int): String? {
            val v = list.getOrNull(index) ?: return null
            return when (v) {
                is Str -> v.value
                is Num -> v.value
                is Bool -> v.value.toString()
                else -> null
            }
        }
        fun getLong(index: Int, default: Long = 0L): Long {
            val v = list.getOrNull(index) ?: return default
            val raw = when (v) {
                is Num -> v.value
                is Str -> v.value
                else -> return default
            }
            return raw.toLongOrNull()
                ?: raw.toDoubleOrNull()?.takeIf { it.isFinite() }?.toLong()
                ?: default
        }
        fun getInt(index: Int, default: Int = 0): Int {
            val v = list.getOrNull(index) ?: return default
            val raw = when (v) {
                is Num -> v.value
                is Str -> v.value
                else -> return default
            }
            return raw.toIntOrNull()
                ?: raw.toDoubleOrNull()?.takeIf { it.isFinite() }?.toInt()
                ?: default
        }
        fun getDouble(index: Int, default: Double = 0.0): Double {
            val v = list.getOrNull(index) ?: return default
            val raw = when (v) {
                is Num -> v.value
                is Str -> v.value
                else -> return default
            }
            return raw.toDoubleOrNull()?.takeIf { it.isFinite() } ?: default
        }
    }

    data class Str(val value: String) : AetherJson()
    data class Num(val value: String) : AetherJson()
    data class Bool(val value: Boolean) : AetherJson()
    object Null : AetherJson()

    companion object {
        fun parse(input: String): AetherJson {
            val parser = Parser(input)
            return parser.parseValue()
        }

        private class Parser(private val src: String) {
            private var pos = 0

            fun parseValue(): AetherJson {
                skipWhitespace()
                if (pos >= src.length) return Null
                return when (src[pos]) {
                    '{' -> parseObject()
                    '[' -> parseArray()
                    '"' -> Str(parseString())
                    't', 'f' -> parseBoolean()
                    'n' -> parseNull()
                    else -> parseNumber()
                }
            }

            private fun parseObject(): Obj {
                consume('{')
                val map = mutableMapOf<String, AetherJson>()
                skipWhitespace()
                if (pos < src.length && src[pos] == '}') {
                    pos++
                    return Obj(map)
                }
                while (pos < src.length) {
                    skipWhitespace()
                    if (pos >= src.length || src[pos] != '"') break
                    val key = parseString()
                    skipWhitespace()
                    consume(':')
                    val value = parseValue()
                    map[key] = value
                    skipWhitespace()
                    if (pos < src.length && src[pos] == ',') {
                        pos++
                    } else if (pos < src.length && src[pos] == '}') {
                        pos++
                        break
                    } else {
                        break
                    }
                }
                return Obj(map)
            }

            private fun parseArray(): Arr {
                consume('[')
                val list = mutableListOf<AetherJson>()
                skipWhitespace()
                if (pos < src.length && src[pos] == ']') {
                    pos++
                    return Arr(list)
                }
                while (pos < src.length) {
                    skipWhitespace()
                    val value = parseValue()
                    list.add(value)
                    skipWhitespace()
                    if (pos < src.length && src[pos] == ',') {
                        pos++
                    } else if (pos < src.length && src[pos] == ']') {
                        pos++
                        break
                    } else {
                        break
                    }
                }
                return Arr(list)
            }

            private fun parseString(): String {
                consume('"')
                val sb = StringBuilder()
                while (pos < src.length) {
                    val c = src[pos++]
                    if (c == '"') return sb.toString()
                    if (c == '\\' && pos < src.length) {
                        when (val esc = src[pos++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000c')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (pos + 4 <= src.length) {
                                    val hex = src.substring(pos, pos + 4)
                                    pos += 4
                                    sb.append(hex.toInt(16).toChar())
                                }
                            }
                            else -> sb.append(esc)
                        }
                    } else {
                        sb.append(c)
                    }
                }
                return sb.toString()
            }

            private fun parseNumber(): Num {
                val start = pos
                if (pos < src.length && (src[pos] == '-' || src[pos] == '+')) pos++
                while (pos < src.length && (src[pos] in '0'..'9' || src[pos] == '.' || src[pos] == 'e' || src[pos] == 'E' || src[pos] == '-' || src[pos] == '+')) {
                    pos++
                }
                return Num(src.substring(start, pos))
            }

            private fun parseBoolean(): Bool {
                return if (src.startsWith("true", pos)) {
                    pos += 4
                    Bool(true)
                } else if (src.startsWith("false", pos)) {
                    pos += 5
                    Bool(false)
                } else {
                    pos++
                    Bool(false)
                }
            }

            private fun parseNull(): Null {
                if (src.startsWith("null", pos)) pos += 4 else pos++
                return Null
            }

            private fun consume(expected: Char) {
                skipWhitespace()
                if (pos < src.length && src[pos] == expected) pos++
            }

            private fun skipWhitespace() {
                while (pos < src.length && src[pos].isWhitespace()) pos++
            }
        }
    }
}

/**
 * Native InnerTube Android / iOS Client Extractor.
 * Directly communicates with YouTube's /youtubei/v1/player endpoint using ANDROID
 * mobile client context (19.29.35, Android SDK 34), completely bypassing the Web
 * botguard "The page needs to be reloaded." verification error.
 */
object InnerTubeAudioExtractor : AudioStreamExtractor {

    private const val PLAYER_URL = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // Android client config (primary Tier 2: completely avoids Web reload challenge)
    const val ANDROID_CLIENT_NAME = "ANDROID"
    const val ANDROID_CLIENT_VERSION = "19.29.35"
    const val ANDROID_SDK_VERSION = 34
    const val ANDROID_USER_AGENT = "com.google.android.youtube/19.29.35 (Linux; U; Android 14; US) gzip"

    // iOS fallback client config
    const val IOS_CLIENT_NAME = "IOS"
    const val IOS_CLIENT_VERSION = "19.45.4"
    const val IOS_USER_AGENT = "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X; US)"

    override suspend fun extractAudio(url: String): ExtractedAudioInfo = withContext(Dispatchers.IO) {
        val videoId = YouTubeExtractorHelper.extractVideoId(url)
            ?: throw IllegalArgumentException("Link do YouTube inválido ou ID não encontrado: $url")

        // Try primary Android mobile client context
        try {
            return@withContext requestPlayerAndExtract(videoId, isIos = false)
        } catch (androidErr: Exception) {
            // Fall back to iOS client context
            try {
                return@withContext requestPlayerAndExtract(videoId, isIos = true)
            } catch (iosErr: Exception) {
                throw IllegalStateException(
                    "InnerTube extraction failed: Android (${androidErr.message}), iOS (${iosErr.message})",
                    androidErr
                )
            }
        }
    }

    private fun requestPlayerAndExtract(videoId: String, isIos: Boolean): ExtractedAudioInfo {
        val clientName = if (isIos) IOS_CLIENT_NAME else ANDROID_CLIENT_NAME
        val clientVersion = if (isIos) IOS_CLIENT_VERSION else ANDROID_CLIENT_VERSION
        val userAgent = if (isIos) IOS_USER_AGENT else ANDROID_USER_AGENT

        val payloadJson = buildPayload(videoId, clientName, clientVersion, isIos)
        val requestBody = payloadJson.toRequestBody(JSON_MEDIA_TYPE)

        val requestBuilder = Request.Builder()
            .url(PLAYER_URL)
            .post(requestBody)
            .header("User-Agent", userAgent)
            .header("X-Goog-Api-Format-Version", "2")
            .header("Content-Type", "application/json")

        if (!isIos) {
            requestBuilder.header("X-YouTube-Client-Name", "3")
            requestBuilder.header("X-YouTube-Client-Version", clientVersion)
        } else {
            requestBuilder.header("X-YouTube-Client-Name", "5")
            requestBuilder.header("X-YouTube-Client-Version", clientVersion)
        }

        val response = HughDownloader.okHttpClient.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("InnerTube HTTP error: ${response.code} ${response.message}")
        }

        val bodyString = response.body?.string().orEmpty()
        if (bodyString.isBlank()) {
            throw IllegalStateException("InnerTube empty response body")
        }

        val json = AetherJson.parse(bodyString) as? AetherJson.Obj
            ?: throw IllegalStateException("Failed to parse InnerTube JSON response")

        return parsePlayerResponse(videoId, json, if (isIos) "INNERTUBE_IOS" else "INNERTUBE_ANDROID")
    }

    private fun buildPayload(videoId: String, clientName: String, clientVersion: String, isIos: Boolean): String {
        return if (!isIos) {
            """
            {
              "context": {
                "client": {
                  "clientName": "$clientName",
                  "clientVersion": "$clientVersion",
                  "platform": "MOBILE",
                  "osName": "Android",
                  "osVersion": "14",
                  "androidSdkVersion": $ANDROID_SDK_VERSION,
                  "hl": "en",
                  "gl": "US",
                  "utcOffsetMinutes": 0
                },
                "request": {
                  "internalExperimentFlags": [],
                  "useSsl": true
                },
                "user": {
                  "lockedSafetyMode": false
                }
              },
              "videoId": "$videoId",
              "playbackContext": {
                "contentPlaybackContext": {
                  "html5Preference": "HTML5_PREF_WANTS"
                }
              }
            }
            """.trimIndent()
        } else {
            """
            {
              "context": {
                "client": {
                  "clientName": "$clientName",
                  "clientVersion": "$clientVersion",
                  "deviceModel": "iPhone16,2",
                  "hl": "en",
                  "gl": "US",
                  "utcOffsetMinutes": 0
                },
                "request": {
                  "internalExperimentFlags": [],
                  "useSsl": true
                },
                "user": {
                  "lockedSafetyMode": false
                }
              },
              "videoId": "$videoId",
              "playbackContext": {
                "contentPlaybackContext": {
                  "html5Preference": "HTML5_PREF_WANTS"
                }
              }
            }
            """.trimIndent()
        }
    }

    fun parsePlayerResponse(videoId: String, json: AetherJson.Obj, tier: String): ExtractedAudioInfo {
        // Validate playability
        val playability = json.getObj("playabilityStatus")
        val status = playability?.getString("status")
        if (status != null && !status.equals("OK", ignoreCase = true)) {
            val reason = playability.getString("reason") ?: "Status: $status"
            throw IllegalStateException("Vídeo não reproduzível via InnerTube: $reason")
        }

        // Video Details
        val videoDetails = json.getObj("videoDetails")
            ?: throw IllegalStateException("InnerTube response missing videoDetails")

        val title = videoDetails.getString("title") ?: "Faixa Desconhecida"
        val author = videoDetails.getString("author") ?: "Artista Desconhecido"
        val lengthSeconds = videoDetails.getLong("lengthSeconds", 0L)

        var bestThumbUrl: String? = null
        val thumbnails = videoDetails.getObj("thumbnail")?.getArr("thumbnails")
        if (thumbnails != null && thumbnails.isNotEmpty()) {
            var maxArea = 0L
            for (i in 0 until thumbnails.size) {
                val thumb = thumbnails.getObj(i) ?: continue
                val w = thumb.getLong("width", 0L)
                val h = thumb.getLong("height", 0L)
                val area = w * h
                if (area >= maxArea) {
                    maxArea = area
                    bestThumbUrl = thumb.getString("url")
                }
            }
        }

        // Streaming Data
        val streamingData = json.getObj("streamingData")
            ?: throw IllegalStateException("InnerTube response missing streamingData")

        val adaptiveFormats = streamingData.getArr("adaptiveFormats")
            ?: throw IllegalStateException("InnerTube response missing adaptiveFormats")

        data class AudioCandidate(
            val itag: Int,
            val mimeType: String,
            val bitrate: Int,
            val url: String,
            val isM4a: Boolean
        )

        val candidates = mutableListOf<AudioCandidate>()

        for (i in 0 until adaptiveFormats.size) {
            val formatObj = adaptiveFormats.getObj(i) ?: continue
            val mimeType = formatObj.getString("mimeType") ?: ""
            if (!mimeType.contains("audio", ignoreCase = true)) continue

            val itag = formatObj.getInt("itag", 0)
            val bitrate = formatObj.getInt("bitrate", 0)
            var streamUrl = formatObj.getString("url")

            // If url is not directly present, check signatureCipher / cipher
            if (streamUrl.isNullOrBlank()) {
                val cipher = formatObj.getString("signatureCipher") ?: formatObj.getString("cipher")
                if (!cipher.isNullOrBlank()) {
                    streamUrl = extractUrlFromCipher(cipher)
                }
            }

            if (!streamUrl.isNullOrBlank()) {
                val isM4a = mimeType.contains("mp4", ignoreCase = true) || mimeType.contains("m4a", ignoreCase = true)
                candidates.add(AudioCandidate(itag, mimeType, bitrate, streamUrl, isM4a))
            }
        }

        if (candidates.isEmpty()) {
            throw IllegalStateException("Nenhum fluxo de áudio válido com URL encontrado na resposta do InnerTube")
        }

        // Priority: 1. itag 140 (M4A 128k) -> 2. any M4A highest bitrate -> 3. itag 251 (Opus) -> 4. highest bitrate
        val chosen = candidates.find { it.itag == 140 }
            ?: candidates.filter { it.isM4a }.maxByOrNull { it.bitrate }
            ?: candidates.find { it.itag == 251 }
            ?: candidates.maxByOrNull { it.bitrate }
            ?: candidates.first()

        val selectedUrl = chosen.url
        val selectedItag = chosen.itag
        val selectedBitrate = chosen.bitrate
        val selectedFormat = if (chosen.isM4a) "m4a" else "opus"

        return ExtractedAudioInfo(
            videoId = videoId,
            title = sanitizeFileName(title),
            artist = sanitizeFileName(author),
            durationSeconds = lengthSeconds,
            streamUrl = selectedUrl,
            originalUrl = "https://www.youtube.com/watch?v=$videoId",
            thumbnailUrl = bestThumbUrl,
            format = selectedFormat,
            itag = selectedItag,
            averageBitrate = if (selectedBitrate > 0) selectedBitrate / 1000 else 128,
            extractorTier = tier
        )
    }

    private fun extractUrlFromCipher(cipher: String): String? {
        return try {
            val params = cipher.split("&").associate {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], "UTF-8") else parts[0] to ""
            }
            params["url"]
        } catch (_: Exception) {
            null
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
    }
}
