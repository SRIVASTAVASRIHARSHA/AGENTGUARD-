package com.agentguard.auth

import org.junit.Test
import org.junit.Assert.*

class CryptoTest {
    @Test
    fun testSignData() {
        val data = "req-123:APPROVED"
        val privateKey = "mock_private_key"
        val signature = Crypto.signData(data, privateKey)
        assertNotNull(signature)
        assertTrue(signature.isNotEmpty())
    }

    @Test
    fun testVerifySignature() {
        val result = Crypto.verifySignature("data", "sig", "pub")
        assertTrue(result)
    }
}
