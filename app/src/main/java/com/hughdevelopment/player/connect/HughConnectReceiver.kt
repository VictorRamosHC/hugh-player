package com.hughdevelopment.player.connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver for receiving asynchronous events or commands from NIGHT//CITY.
 */
class HughConnectReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        Log.i(TAG, "HughConnectReceiver received action: $action")

        val payloadJson = intent.getStringExtra(HughConnectContract.EXTRA_PAYLOAD_JSON) ?: return
        val callingPkg = HughSecurityManager.validateCallingPackage(context, android.os.Binder.getCallingUid())
            ?: intent.getStringExtra("SENDER_PACKAGE") // Fallback for broadcast intent origin check

        val parsed = HughConnectContract.Message.parseAndValidate(payloadJson, context.packageName)
        if (parsed.isFailure) {
            Log.e(TAG, "Rejected broadcast payload: ${parsed.exceptionOrNull()?.message}")
            return
        }

        val message = parsed.getOrThrow()
        Log.i(TAG, "Received message from ${message.sender}: action=${message.action}, type=${message.type}")

        when (message.action) {
            HughConnectContract.ACTION_PLAYBACK_CONTROL -> {
                val cmd = message.payload.optString("command")
                val trackId = if (message.payload.has("track_id")) message.payload.optLong("track_id") else null
                HughConnectDispatcher.notifyPlaybackRequested(context, cmd, trackId)
            }
        }
    }

    companion object {
        const val TAG = "HughConnectReceiver"
    }
}
