package com.agentguard.services

import com.agentguard.auth.KeyManager
import com.google.gson.Gson
import okhttp3.*
import java.util.concurrent.TimeUnit

data class ApprovalRequest(
    val requestId: String,
    val command: String,
    val riskScore: Int,
    val level: String = "HIGH",
    val context: Map<String, String> = emptyMap(),
    val reason: String = ""
)

class CloudService(
    private val relayUrl: String,
    private val token: String,
    private val keyManager: KeyManager
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var webSocket: WebSocket? = null

    fun connectWebSocket(phoneId: String, onRequest: (ApprovalRequest) -> Unit, onConnection: (Boolean) -> Unit) {
        val wsUrl = relayUrl.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://")
        val request = Request.Builder()
            .url("$wsUrl/ws/phone/${java.net.URLEncoder.encode(phoneId, "UTF-8")}")
            .header("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onConnection(true)
                webSocket.send(Gson().toJson(mapOf(
                    "type" to "REGISTER_DEVICE",
                    "public_key" to keyManager.publicKeyBase64()
                )))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = Gson().fromJson(text, Map::class.java)
                    if (root["type"] == "APPROVAL_REQUEST") {
                        val msg = Gson().fromJson(text, ApprovalRequest::class.java)
                        onRequest(msg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onConnection(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onConnection(false)
            }
        })
    }

    fun sendApproval(requestId: String, status: String) {
        val timestamp = System.currentTimeMillis()
        val signature = keyManager.signApproval(requestId, status, timestamp)
        val payload = mapOf(
            "type" to "APPROVAL_RESPONSE",
            "request_id" to requestId,
            "status" to status,
            "signature" to signature,
            "timestamp" to timestamp
        )
        webSocket?.send(Gson().toJson(payload))
    }

    fun close() {
        webSocket?.close(1000, "closed")
        webSocket = null
    }
}
