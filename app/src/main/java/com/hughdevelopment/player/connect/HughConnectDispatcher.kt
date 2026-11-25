package com.hughdevelopment.player.connect

import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONObject

object HughConnectDispatcher {
    private const val TAG = "HughConnectDispatcher"

    interface PlaybackListener {
        fun onPlaybackRequested(command: String, trackId: Long?)
    }

    private var playbackListener: PlaybackListener? = null

    fun registerPlaybackListener(listener: PlaybackListener) {
        playbackListener = listener
    }

    fun unregisterPlaybackListener() {
        playbackListener = null
    }

    fun notifyPlaybackRequested(context: Context, command: String, trackId: Long?) {
        Log.i(TAG, "Playback requested via Hugh Connect: command=$command, trackId=$trackId")
        playbackListener?.onPlaybackRequested(command, trackId)
    }

    /**
     * Broadcasts a Hugh Connect playback event to NIGHT//CITY using explicit package targeting.
     */
    fun broadcastPlaybackEvent(
        context: Context,
        action: String,
        isPlaying: Boolean,
        trackId: Long?,
        title: String?,
        artist: String?,
        durationMs: Long?,
        positionMs: Long?
    ) {
        try {
            val payload = JSONObject().apply {
                put("is_playing", isPlaying)
                if (trackId != null) put("track_id", trackId)
                if (title != null) put("title", title)
                if (artist != null) put("artist", artist)
                if (durationMs != null) put("duration_ms", durationMs)
                if (positionMs != null) put("position_ms", positionMs)
            }

            val msg = HughConnectContract.Message(
                sender = context.packageName,
                recipient = HughConnectContract.PKG_NIGHT_CITY,
                type = HughConnectContract.TYPE_EVENT,
                action = action,
                payload = payload
            )

            val intent = Intent(HughConnectContract.ACTION_EVENT).apply {
                `package` = HughConnectContract.PKG_NIGHT_CITY
                putExtra(HughConnectContract.EXTRA_PAYLOAD_JSON, msg.toJsonString())
            }

            context.sendBroadcast(intent)
            Log.i(TAG, "Broadcast Hugh Connect event $action to ${HughConnectContract.PKG_NIGHT_CITY}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast Hugh Connect event: ${e.message}")
        }
    }
}
