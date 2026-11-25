package com.hughdevelopment.player.connect

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import com.hughdevelopment.player.HughPlayerApplication
import com.hughdevelopment.player.service.MusicPlaybackService
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bound Service providing low-latency request-response IPC for Hugh Connect.
 * Also handles standard explicit intents.
 */
class HughConnectService : Service() {

    private val binder = HughConnectBinder()

    inner class HughConnectBinder : Binder() {
        fun getService(): HughConnectService = this@HughConnectService

        /**
         * Transaction-based IPC for bound clients.
         * Code 1: executeCommand(payloadJson: String) -> responseJson: String
         */
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code == TRANSACTION_EXECUTE_COMMAND) {
                data.enforceInterface(DESCRIPTOR)
                val callingUid = Binder.getCallingUid()
                val callingPkg = HughSecurityManager.validateCallingPackage(this@HughConnectService, callingUid)

                val requestJson = data.readString()
                val responseJson = if (callingPkg == null) {
                    val err = HughConnectContract.Message.createErrorResponse(
                        null,
                        packageName,
                        HughConnectContract.PKG_NIGHT_CITY,
                        HughConnectContract.STATUS_PERMISSION_DENIED,
                        "SECURITY_EXCEPTION",
                        "Caller UID $callingUid is not an authorized ecosystem package"
                    )
                    err.toJsonString()
                } else {
                    handleIncomingJson(requestJson, callingPkg)
                }

                reply?.writeNoException()
                reply?.writeString(responseJson)
                return true
            }
            return super.onTransact(code, data, reply, flags)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "Client bound to HughConnectService: ${intent?.component}")
        return binder
    }

    fun handleIncomingJson(jsonStr: String?, verifiedSenderPkg: String): String {
        val parsedResult = HughConnectContract.Message.parseAndValidate(jsonStr, packageName)
        if (parsedResult.isFailure) {
            val err = parsedResult.exceptionOrNull()
            Log.e(TAG, "Failed to parse/validate message: ${err?.message}")
            val errorMsg = HughConnectContract.Message.createErrorResponse(
                null,
                packageName,
                verifiedSenderPkg,
                HughConnectContract.STATUS_REJECTED,
                "VALIDATION_ERROR",
                err?.message ?: "Malformed message payload"
            )
            return errorMsg.toJsonString()
        }

        val request = parsedResult.getOrThrow()
        if (request.sender != verifiedSenderPkg && verifiedSenderPkg.isNotEmpty()) {
            val errorMsg = HughConnectContract.Message.createErrorResponse(
                request,
                packageName,
                verifiedSenderPkg,
                HughConnectContract.STATUS_PERMISSION_DENIED,
                "SENDER_SPOOFING",
                "Declared sender (${request.sender}) does not match authenticated package ($verifiedSenderPkg)"
            )
            return errorMsg.toJsonString()
        }

        return executeRequest(request).toJsonString()
    }

    private fun executeRequest(msg: HughConnectContract.Message): HughConnectContract.Message {
        return when (msg.action) {
            HughConnectContract.ACTION_PING -> {
                val caps = JSONArray().apply {
                    put(HughConnectContract.ACTION_PING)
                    put(HughConnectContract.ACTION_PLAYBACK_CONTROL)
                    put(HughConnectContract.ACTION_GET_PLAYBACK_STATE)
                    put(HughConnectContract.ACTION_INSPECT_LOCAL_CONTEXT)
                }
                val payload = JSONObject().apply {
                    put("app_id", packageName)
                    put("app_name", "Aether Vault")
                    put("app_version", "1.9.0")
                    put("protocol_version", HughConnectContract.PROTOCOL_VERSION)
                    put("capabilities", caps)
                    put("connection_status", "AVAILABLE")
                }
                HughConnectContract.Message(
                    messageId = msg.messageId,
                    sender = packageName,
                    recipient = msg.sender,
                    type = HughConnectContract.TYPE_RESPONSE,
                    action = msg.action,
                    status = HughConnectContract.STATUS_SUCCESS,
                    payload = payload
                )
            }

            HughConnectContract.ACTION_PLAYBACK_CONTROL -> {
                val command = msg.payload.optString("command")
                val trackId = if (msg.payload.has("track_id")) msg.payload.optLong("track_id") else null

                val player = MusicPlaybackService.activePlayer
                var isPlaying = false
                var trackTitle: String? = null
                var trackArtist: String? = null
                var trackDuration = 0L

                if (player != null) {
                    when (command) {
                        "PLAY" -> {
                            player.play()
                            isPlaying = true
                        }
                        "PAUSE" -> {
                            player.pause()
                            isPlaying = false
                        }
                        "PLAY_PAUSE" -> {
                            if (player.isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.play()
                                isPlaying = true
                            }
                        }
                        "NEXT" -> {
                            if (player.hasNextMediaItem()) {
                                player.seekToNextMediaItem()
                            }
                            player.play()
                            isPlaying = true
                        }
                        "PREVIOUS" -> {
                            if (player.hasPreviousMediaItem()) {
                                player.seekToPreviousMediaItem()
                            } else {
                                player.seekTo(0L)
                            }
                            player.play()
                            isPlaying = true
                        }
                        "STOP" -> {
                            player.stop()
                            isPlaying = false
                        }
                        else -> {
                            return HughConnectContract.Message.createErrorResponse(
                                msg,
                                packageName,
                                msg.sender,
                                HughConnectContract.STATUS_ERROR,
                                "UNSUPPORTED_COMMAND",
                                "Unknown playback control command: $command"
                            )
                        }
                    }

                    val currentItem = player.currentMediaItem
                    trackTitle = currentItem?.mediaMetadata?.title?.toString()
                    trackArtist = currentItem?.mediaMetadata?.artist?.toString()
                    trackDuration = if (player.duration > 0) player.duration else 0L
                } else {
                    // Fallback to notify listener if UI is hosting player controller
                    when (command) {
                        "PLAY" -> isPlaying = true
                        "PAUSE" -> isPlaying = false
                        "PLAY_PAUSE" -> isPlaying = !MusicPlaybackService.activeIsPlaying
                        "NEXT" -> isPlaying = true
                        "PREVIOUS" -> isPlaying = true
                        "STOP" -> isPlaying = false
                        else -> {
                            return HughConnectContract.Message.createErrorResponse(
                                msg,
                                packageName,
                                msg.sender,
                                HughConnectContract.STATUS_ERROR,
                                "UNSUPPORTED_COMMAND",
                                "Unknown playback control command: $command"
                            )
                        }
                    }
                    HughConnectDispatcher.notifyPlaybackRequested(this, command, trackId)
                }

                val currentTrackObj = JSONObject().apply {
                    put("id", trackId ?: (player?.currentMediaItem?.mediaId?.toLongOrNull() ?: 0L))
                    put("title", trackTitle ?: "Aether Music Stream")
                    put("artist", trackArtist ?: "Aether Vault")
                    put("duration_ms", trackDuration)
                }

                val payload = JSONObject().apply {
                    put("command_executed", command)
                    put("is_playing", isPlaying)
                    put("current_track", currentTrackObj)
                }

                HughConnectContract.Message(
                    messageId = msg.messageId,
                    sender = packageName,
                    recipient = msg.sender,
                    type = HughConnectContract.TYPE_RESPONSE,
                    action = msg.action,
                    status = HughConnectContract.STATUS_SUCCESS,
                    payload = payload
                )
            }

            HughConnectContract.ACTION_GET_PLAYBACK_STATE -> {
                val player = MusicPlaybackService.activePlayer
                val isPlaying = player?.isPlaying ?: (MusicPlaybackService.activeAudioSessionId > 0 && MusicPlaybackService.activeIsPlaying)
                val currentItem = player?.currentMediaItem
                val trackObj = if (currentItem != null) {
                    JSONObject().apply {
                        put("id", currentItem.mediaId.toLongOrNull() ?: 0L)
                        put("title", currentItem.mediaMetadata.title?.toString() ?: "Unknown Title")
                        put("artist", currentItem.mediaMetadata.artist?.toString() ?: "Unknown Artist")
                        put("duration_ms", if (player.duration > 0) player.duration else 0L)
                        put("position_ms", player.currentPosition)
                    }
                } else null

                val payload = JSONObject().apply {
                    put("is_playing", isPlaying)
                    put("has_active_session", MusicPlaybackService.activeAudioSessionId > 0)
                    if (trackObj != null) {
                        put("current_track", trackObj)
                    }
                    put("protocol_version", HughConnectContract.PROTOCOL_VERSION)
                }
                HughConnectContract.Message(
                    messageId = msg.messageId,
                    sender = packageName,
                    recipient = msg.sender,
                    type = HughConnectContract.TYPE_RESPONSE,
                    action = msg.action,
                    status = HughConnectContract.STATUS_SUCCESS,
                    payload = payload
                )
            }

            HughConnectContract.ACTION_INSPECT_LOCAL_CONTEXT -> {
                // Phase 5: Atom Local Context Foundation
                // Securely exposes connected app identity, capabilities, and active playback context
                // Strictly preserves privacy: NO GPS history, NO full listening history.
                val player = MusicPlaybackService.activePlayer
                val isPlaying = player?.isPlaying ?: MusicPlaybackService.activeIsPlaying
                val currentItem = player?.currentMediaItem

                val capabilities = JSONArray().apply {
                    put("AUDIO_PLAYBACK_CONTROL")
                    put("AUDIO_METADATA_STREAM")
                    put("OFFLINE_MEDIA_DSP")
                    put("INTER_APP_BROADCAST")
                    put("ATOM_LOCAL_INTELLIGENCE_READY")
                }

                val currentTrackObj = JSONObject().apply {
                    put("title", currentItem?.mediaMetadata?.title?.toString() ?: "IDLE")
                    put("artist", currentItem?.mediaMetadata?.artist?.toString() ?: "Hugh Development")
                    put("is_playing", isPlaying)
                    put("duration_ms", player?.duration ?: 0L)
                }

                val localContext = JSONObject().apply {
                    put("node_id", "aether-vault-local")
                    put("application_id", packageName)
                    put("version_name", "1.9.0")
                    put("architecture", "Native Kotlin / Jetpack Compose / Media3")
                    put("capabilities", capabilities)
                    put("status", if (isPlaying) "STREAMING" else "STANDBY")
                    put("current_playback", currentTrackObj)
                    put("privacy_level", "STRICT_LOCAL_NON_TRACKING")
                }

                HughConnectContract.Message(
                    messageId = msg.messageId,
                    sender = packageName,
                    recipient = msg.sender,
                    type = HughConnectContract.TYPE_RESPONSE,
                    action = msg.action,
                    status = HughConnectContract.STATUS_SUCCESS,
                    payload = localContext
                )
            }

            else -> {
                HughConnectContract.Message.createErrorResponse(
                    msg,
                    packageName,
                    msg.sender,
                    HughConnectContract.STATUS_ERROR,
                    "UNSUPPORTED_ACTION",
                    "Action ${msg.action} is not supported"
                )
            }
        }
    }

    companion object {
        const val TAG = "HughConnectService"
        const val DESCRIPTOR = "com.hughdevelopment.player.connect.IHughConnect"
        const val TRANSACTION_EXECUTE_COMMAND = 1
    }
}
