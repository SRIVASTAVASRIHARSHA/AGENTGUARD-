package com.agentguard.auth

import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.nio.charset.StandardCharsets

object Crypto {
    fun signData(data: String, privateKeyBase64: String): String {
        val key = Ed25519PrivateKeyParameters(Base64.decode(privateKeyBase64, Base64.NO_WRAP), 0)
        val signer = Ed25519Signer()
        signer.init(true, key)
        val bytes = data.toByteArray(StandardCharsets.UTF_8)
        signer.update(bytes, 0, bytes.size)
        return Base64.encodeToString(signer.generateSignature(), Base64.NO_WRAP)
    }

    fun canonicalApproval(requestId: String, status: String, timestamp: Long): String =
        "{\"request_id\":\"${escape(requestId)}\",\"status\":\"${escape(status)}\",\"timestamp\":$timestamp}"

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
}
