package com.hughdevelopment.player.connect

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object HughConnectContract {
    const val PROTOCOL_NAME = "HUGH_CONNECT"
    const val PROTOCOL_VERSION = 1
    const val MAX_PAYLOAD_SIZE = 65536 // 64 KB

    // Packages
    const val PKG_NIGHT_CITY = "com.anonymous.nightcity"
    const val PKG_PLAYER = "com.hughdevelopment.player"
    const val PKG_PLAYER_DEBUG = "com.hughdevelopment.player.debug"

    // Permission & Actions
    const val PERMISSION_ECOSYSTEM_BRIDGE = "com.hughdevelopment.permission.ECOSYSTEM_BRIDGE"
    const val ACTION_COMMAND = "com.hughdevelopment.action.HUGH_CONNECT_COMMAND"
    const val ACTION_EVENT = "com.hughdevelopment.action.HUGH_CONNECT_EVENT"

    // Intent Extras
    const val EXTRA_PAYLOAD_JSON = "HUGH_CONNECT_PAYLOAD"

    // Message Types
    const val TYPE_COMMAND = "COMMAND"
    const val TYPE_RESPONSE = "RESPONSE"
    const val TYPE_EVENT = "EVENT"

    // Actions
    const val ACTION_PING = "PING"
    const val ACTION_PLAYBACK_CONTROL = "PLAYBACK_CONTROL"
    const val ACTION_GET_PLAYBACK_STATE = "GET_PLAYBACK_STATE"
    const val ACTION_INSPECT_LOCAL_CONTEXT = "INSPECT_LOCAL_CONTEXT"

    // Events
    const val EVENT_PLAYBACK_STATE_CHANGED = "PLAYBACK_STATE_CHANGED"
    const val EVENT_TRACK_CHANGED = "TRACK_CHANGED"
    const val EVENT_PLAYBACK_STARTED = "PLAYBACK_STARTED"
    const val EVENT_PLAYBACK_PAUSED = "PLAYBACK_PAUSED"
    const val EVENT_PLAYBACK_STOPPED = "PLAYBACK_STOPPED"

    // Status
    const val STATUS_SUCCESS = "SUCCESS"
    const val STATUS_ERROR = "ERROR"
    const val STATUS_UNSUPPORTED_VERSION = "UNSUPPORTED_VERSION"
    const val STATUS_PERMISSION_DENIED = "PERMISSION_DENIED"
    const val STATUS_REJECTED = "REJECTED"

    val ALLOWED_PACKAGES = setOf(PKG_NIGHT_CITY, PKG_PLAYER, PKG_PLAYER_DEBUG)

    data class Message(
        val protocol: String = PROTOCOL_NAME,
        val version: Int = PROTOCOL_VERSION,
        val messageId: String = UUID.randomUUID().toString(),
        val sender: String,
        val recipient: String,
        val type: String,
        val action: String,
        val status: String? = null,
        val timestamp: Long = System.currentTimeMillis(),
        val payload: JSONObject = JSONObject(),
        val error: JSONObject? = null
    ) {
        fun toJsonString(): String {
            val json = JSONObject()
            json.put("protocol", protocol)
            json.put("version", version)
            json.put("message_id", messageId)
            json.put("sender", sender)
            json.put("recipient", recipient)
            json.put("type", type)
            json.put("action", action)
            if (status != null) json.put("status", status)
            json.put("timestamp", timestamp)
            json.put("payload", payload)
            if (error != null) json.put("error", error)
            return json.toString()
        }

        companion object {
            fun parseAndValidate(jsonStr: String?, expectedRecipient: String? = null): Result<Message> {
                if (jsonStr.isNullOrBlank()) {
                    return Result.failure(IllegalArgumentException("Message is empty or null"))
                }
                if (jsonStr.length > MAX_PAYLOAD_SIZE) {
                    return Result.failure(IllegalArgumentException("Payload exceeds maximum size limit of $MAX_PAYLOAD_SIZE bytes"))
                }

                return try {
                    val root = JSONObject(jsonStr)
                    val protocol = root.optString("protocol")
                    if (protocol != PROTOCOL_NAME) {
                        return Result.failure(IllegalArgumentException("Invalid protocol: $protocol"))
                    }

                    val version = root.optInt("version", -1)
                    if (version != PROTOCOL_VERSION) {
                        return Result.failure(IllegalArgumentException("Unsupported protocol version: $version"))
                    }

                    val messageId = root.optString("message_id")
                    if (messageId.isBlank()) {
                        return Result.failure(IllegalArgumentException("Missing message_id"))
                    }

                    val sender = root.optString("sender")
                    if (!ALLOWED_PACKAGES.contains(sender)) {
                        return Result.failure(SecurityException("Unauthorized sender package: $sender"))
                    }

                    val recipient = root.optString("recipient")
                    if (!ALLOWED_PACKAGES.contains(recipient)) {
                        return Result.failure(IllegalArgumentException("Invalid recipient package: $recipient"))
                    }

                    if (expectedRecipient != null && recipient != expectedRecipient && !recipient.startsWith("com.hughdevelopment.player")) {
                        return Result.failure(IllegalArgumentException("Message recipient mismatch: got $recipient, expected $expectedRecipient"))
                    }

                    val type = root.optString("type")
                    if (type != TYPE_COMMAND && type != TYPE_RESPONSE && type != TYPE_EVENT) {
                        return Result.failure(IllegalArgumentException("Invalid message type: $type"))
                    }

                    val action = root.optString("action")
                    if (action.isBlank()) {
                        return Result.failure(IllegalArgumentException("Missing action"))
                    }

                    val status = if (root.has("status")) root.optString("status") else null
                    val timestamp = root.optLong("timestamp", System.currentTimeMillis())
                    val payload = root.optJSONObject("payload") ?: JSONObject()
                    val error = root.optJSONObject("error")

                    Result.success(
                        Message(
                            protocol = protocol,
                            version = version,
                            messageId = messageId,
                            sender = sender,
                            recipient = recipient,
                            type = type,
                            action = action,
                            status = status,
                            timestamp = timestamp,
                            payload = payload,
                            error = error
                        )
                    )
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            fun createErrorResponse(
                originalMessage: Message?,
                sender: String,
                recipient: String,
                status: String,
                errorCode: String,
                errorMessage: String
            ): Message {
                val errObj = JSONObject().apply {
                    put("code", errorCode)
                    put("message", errorMessage)
                }
                return Message(
                    messageId = originalMessage?.messageId ?: UUID.randomUUID().toString(),
                    sender = sender,
                    recipient = recipient,
                    type = TYPE_RESPONSE,
                    action = originalMessage?.action ?: "ERROR",
                    status = status,
                    error = errObj
                )
            }
        }
    }
}
