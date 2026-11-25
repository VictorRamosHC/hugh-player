package com.hughdevelopment.player.connect

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class HughConnectContractTest {

    @Test
    fun testValidPingCommandSerializationAndParsing() {
        val ping = HughConnectContract.Message(
            sender = HughConnectContract.PKG_NIGHT_CITY,
            recipient = HughConnectContract.PKG_PLAYER,
            type = HughConnectContract.TYPE_COMMAND,
            action = HughConnectContract.ACTION_PING,
            payload = JSONObject().put("client_timestamp", 123456789L)
        )

        val jsonStr = ping.toJsonString()
        val parseResult = HughConnectContract.Message.parseAndValidate(jsonStr, HughConnectContract.PKG_PLAYER)

        assertTrue(parseResult.isSuccess)
        val parsed = parseResult.getOrThrow()
        assertEquals(HughConnectContract.PROTOCOL_NAME, parsed.protocol)
        assertEquals(HughConnectContract.PROTOCOL_VERSION, parsed.version)
        assertEquals(HughConnectContract.PKG_NIGHT_CITY, parsed.sender)
        assertEquals(HughConnectContract.PKG_PLAYER, parsed.recipient)
        assertEquals(HughConnectContract.ACTION_PING, parsed.action)
        assertEquals(HughConnectContract.TYPE_COMMAND, parsed.type)
        assertEquals(123456789L, parsed.payload.getLong("client_timestamp"))
    }

    @Test
    fun testRejectUnauthorizedSender() {
        val malformed = JSONObject().apply {
            put("protocol", "HUGH_CONNECT")
            put("version", 1)
            put("message_id", "test-id-1234")
            put("sender", "com.malicious.attacker")
            put("recipient", HughConnectContract.PKG_PLAYER)
            put("type", "COMMAND")
            put("action", "PLAYBACK_CONTROL")
        }

        val parseResult = HughConnectContract.Message.parseAndValidate(malformed.toString(), HughConnectContract.PKG_PLAYER)
        assertTrue(parseResult.isFailure)
        assertTrue(parseResult.exceptionOrNull() is SecurityException)
    }

    @Test
    fun testRejectIncompatibleVersion() {
        val outdated = JSONObject().apply {
            put("protocol", "HUGH_CONNECT")
            put("version", 999)
            put("message_id", "test-id-1234")
            put("sender", HughConnectContract.PKG_NIGHT_CITY)
            put("recipient", HughConnectContract.PKG_PLAYER)
            put("type", "COMMAND")
            put("action", "PING")
        }

        val parseResult = HughConnectContract.Message.parseAndValidate(outdated.toString(), HughConnectContract.PKG_PLAYER)
        assertTrue(parseResult.isFailure)
        assertTrue(parseResult.exceptionOrNull()?.message?.contains("Unsupported protocol version") == true)
    }

    @Test
    fun testRejectOversizedPayload() {
        val largeString = "A".repeat(70000)
        val parseResult = HughConnectContract.Message.parseAndValidate(largeString, HughConnectContract.PKG_PLAYER)
        assertTrue(parseResult.isFailure)
        assertTrue(parseResult.exceptionOrNull()?.message?.contains("exceeds maximum size") == true)
    }

    @Test
    fun testPlaybackControlMessage() {
        val playCmd = HughConnectContract.Message(
            sender = HughConnectContract.PKG_NIGHT_CITY,
            recipient = HughConnectContract.PKG_PLAYER,
            type = HughConnectContract.TYPE_COMMAND,
            action = HughConnectContract.ACTION_PLAYBACK_CONTROL,
            payload = JSONObject().apply {
                put("command", "PLAY")
                put("track_id", 42L)
            }
        )

        val jsonStr = playCmd.toJsonString()
        val parseResult = HughConnectContract.Message.parseAndValidate(jsonStr, HughConnectContract.PKG_PLAYER)
        assertTrue(parseResult.isSuccess)
        val parsed = parseResult.getOrThrow()
        assertEquals("PLAY", parsed.payload.getString("command"))
        assertEquals(42L, parsed.payload.getLong("track_id"))
    }

    @Test
    fun testInspectLocalContextMessage() {
        val contextCmd = HughConnectContract.Message(
            sender = HughConnectContract.PKG_NIGHT_CITY,
            recipient = HughConnectContract.PKG_PLAYER,
            type = HughConnectContract.TYPE_COMMAND,
            action = HughConnectContract.ACTION_INSPECT_LOCAL_CONTEXT,
            payload = JSONObject()
        )

        val jsonStr = contextCmd.toJsonString()
        val parseResult = HughConnectContract.Message.parseAndValidate(jsonStr, HughConnectContract.PKG_PLAYER)
        assertTrue(parseResult.isSuccess)
        val parsed = parseResult.getOrThrow()
        assertEquals(HughConnectContract.ACTION_INSPECT_LOCAL_CONTEXT, parsed.action)
    }
}
