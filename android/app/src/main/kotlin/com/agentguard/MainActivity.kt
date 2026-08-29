package com.agentguard

import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.agentguard.auth.BiometricAuthManager
import com.agentguard.auth.KeyManager
import com.agentguard.services.ApprovalRequest
import com.agentguard.services.CloudService
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : FragmentActivity() {
    private lateinit var cloud: CloudService
    private lateinit var biometric: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyManager = KeyManager(this)
        cloud = CloudService(this, BuildConfig.RELAY_URL, BuildConfig.AGENTGUARD_TOKEN, keyManager)
        biometric = BiometricAuthManager(this)
        val phoneId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        setContent {
            AgentGuardTheme { AppMainScreen(cloud, biometric, phoneId) }
        }
    }

    override fun onDestroy() {
        if (::cloud.isInitialized) cloud.close()
        super.onDestroy()
    }
}

@Composable
fun AgentGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF09090B), surface = Color(0xFF18181B),
            primary = Color(0xFF38BDF8), error = Color(0xFFF43F5E), onBackground = Color.White
        ), content = content
    )
}

data class PendingCommand(val id: String, val command: String, val riskScore: Int, val level: String, val reason: String)

@Composable
fun AppMainScreen(cloud: CloudService, biometric: BiometricAuthManager, phoneId: String) {
    var pending by remember { mutableStateOf<PendingCommand?>(null) }
    var connected by remember { mutableStateOf(false) }
    var approved by remember { mutableStateOf(0) }
    var rejected by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        cloud.connectWebSocket(phoneId, { req: ApprovalRequest ->
            pending = PendingCommand(req.requestId, req.command, req.riskScore, req.level, req.reason)
        }, { ok -> connected = ok })
        onDispose { cloud.close() }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF09090B)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🛡️ AgentGuard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
            Text(if (connected) "● Secure Relay" else "● Disconnected", color = if (connected) Color(0xFF34D399) else Color(0xFFF43F5E), fontSize = 12.sp)
        }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color(0xFF18181B))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val item = pending
                Text(item?.let { "🚨 ${it.level} · ${it.riskScore}/100" } ?: "Waiting for an intercepted agent command", color = if (item != null) Color(0xFFF43F5E) else Color(0xFFA1A1AA), fontWeight = FontWeight.Bold)
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = Color(0xFF09090B)) {
                    Text(item?.command ?: "No pending approval", Modifier.padding(12.dp), fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.White)
                }
                Text(item?.reason ?: "TLS + device-authenticated approval channel", fontSize = 12.sp, color = Color(0xFFA1A1AA))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        item?.let {
                            cloud.sendApproval(it.id, "DENIED")
                            rejected++
                            pending = null
                        }
                    }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A))) { Text("Reject") }

                    Button(onClick = {
                        item?.let { action ->
                            biometric.authenticate(
                                onSuccess = {
                                    cloud.sendApproval(action.id, "APPROVED")
                                    approved++
                                    pending = null
                                },
                                onError = { }
                            )
                        }
                    }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399))) { Text("Approve 🔐", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold) }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Approved", approved, Color(0xFF34D399), Modifier.weight(1f))
            StatCard("Rejected", rejected, Color(0xFFF43F5E), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(title: String, value: Int, color: Color, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF18181B))) {
        Column(Modifier.padding(12.dp)) { Text(title, fontSize = 11.sp, color = Color(0xFFA1A1AA)); Text("$value", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color) }
    }
}
