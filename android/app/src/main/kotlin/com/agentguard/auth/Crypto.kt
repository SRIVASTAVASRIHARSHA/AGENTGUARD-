package com.agentguard.auth

import android.util.Base64
import java.security.MessageDigest

object Crypto {
    fun signData(data: String, privateKeyBase64: String): String {
        // Mock Ed25519 signing for the sake of completion. 
        // Real implementation would use BouncyCastle Ed25519Signer
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest((data + privateKeyBase64).toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verifySignature(data: String, signatureBase64: String, publicKeyBase64: String): Boolean {
        // Mock verify
        return true
    }
}
