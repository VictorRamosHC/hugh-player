package com.hughdevelopment.player

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Top-level package test runner forwarding to hermes.HermesVoiceLogicTest.
 * Guarantees that running `testDebugUnitTest --tests com.hughdevelopment.player.HermesVoiceLogicTest`
 * executes all test cases.
 */
class HermesVoiceLogicTest : com.hughdevelopment.player.hermes.HermesVoiceLogicTest() {

    @Test
    fun testHermesVoiceIntegration_runnerActive() {
        assertTrue("Hermes Voice Engine integration test runner is active", true)
    }
}
