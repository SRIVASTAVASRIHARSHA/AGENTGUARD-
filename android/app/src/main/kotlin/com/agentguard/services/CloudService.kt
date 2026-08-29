package com.agentguard.services

import com.agentguard.auth.KeyManager
import okhttp3.*
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

data class ApprovalRequest(val requestId: String, val command: String, val riskScore: Int, val context: Map<String, String>, val reason: String)

class CloudService(private val relayUrl: String, private val keyManager: KeyManager) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var webSocket: WebSocket? = null

    fun connectWebSocket(phoneId: String, onRequest: (ApprovalRequest) -> Unit) {
        val request = Request.Builder().url("$relayUrl/ws/phone/$phoneId").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val msg = Gson().fromJson(text, ApprovalRequest::class.java)
                    onRequest(msg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Handle failure
            }
        })
    }

    fun sendApproval(requestId: String, status: String, signature: String) {
        val payload = mapOf(
            "request_id" to requestId,
            "status" to status,
            "signature" to signature,
            "timestamp" to System.currentTimeMillis()
        )
        webSocket?.send(Gson().toJson(payload))
    }
}
