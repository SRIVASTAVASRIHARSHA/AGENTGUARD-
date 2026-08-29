package com.agentguard.auth

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

class KeyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "agentguard_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun ensureKeys() {
        if (!sharedPrefs.contains(PRIVATE)) {
            val privateKey = Ed25519PrivateKeyParameters(org.bouncycastle.crypto.prng.FixedSecureRandom())
            sharedPrefs.edit()
                .putString(PRIVATE, Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP))
                .putString(PUBLIC, Base64.encodeToString(privateKey.generatePublicKey().encoded, Base64.NO_WRAP))
                .apply()
        }
    }

    fun publicKeyBase64(): String {
        ensureKeys()
        return sharedPrefs.getString(PUBLIC, null)!!
    }

    fun signApproval(requestId: String, status: String, timestamp: Long): String {
        ensureKeys()
        return Crypto.signData(Crypto.canonicalApproval(requestId, status, timestamp), sharedPrefs.getString(PRIVATE, null)!!)
    }

    companion object {
        private const val PRIVATE = "ed25519_private"
        private const val PUBLIC = "ed25519_public"
    }
}
