package com.agentguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgentGuardTheme {
                AppMainScreen()
            }
        }
    }
}

@Composable
fun AgentGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF09090B),
            surface = Color(0xFF18181B),
            primary = Color(0xFF38BDF8),
            error = Color(0xFFF43F5E),
            onBackground = Color(0xFFFAFAFA)
        ),
        content = content
    )
}

data class PendingCommand(
    val id: String,
    val command: String,
    val riskScore: Int,
    val reason: String
)

@Composable
fun AppMainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var pendingCommand by remember { 
        mutableStateOf<PendingCommand?>(
            PendingCommand("req-1", "git push --force origin main", 96, "Destructive remote push operation")
        ) 
    }
    var approvedCount by remember { mutableStateOf(0) }
    var deniedCount by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF09090B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ AgentGuard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF18181B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isConnected) Color(0xFF34D399) else Color(0xFFF43F5E),
                                    shape = RoundedCornerShape(50.dp)
                                )
                        )
                        Text(
                            text = if (isConnected) "Relay Active" else "Disconnected",
                            fontSize = 12.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Approval Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x26F43F5E)
                            ) {
                                Text(
                                    text = pendingCommand?.let { "CRITICAL (${it.riskScore})" } ?: "IDLE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color(0xFFF43F5E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "30s timeout",
                                fontSize = 12.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }

                        // Command display box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF09090B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                        ) {
                            Text(
                                text = pendingCommand?.command ?: "Awaiting agent command stream...",
                                modifier = Modifier.padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFFFAFAFA)
                            )
                        }

                        Text(
                            text = pendingCommand?.reason ?: "Hardware Ed25519 authentication",
                            fontSize = 12.sp,
                            color = Color(0xFFA1A1AA)
                        )

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (pendingCommand != null) {
                                        deniedCount++
                                        pendingCommand = null
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A))
                            ) {
                                Text("Reject", color = Color(0xFFFAFAFA), fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (pendingCommand != null) {
                                        approvedCount++
                                        pendingCommand = null
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399))
                            ) {
                                Text("Approve 🔑", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                pendingCommand = PendingCommand(
                                    "req-${System.currentTimeMillis()}",
                                    "git push --force origin main",
                                    96,
                                    "Destructive remote push operation"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text("+ Simulate `git push --force` Command", color = Color(0xFF38BDF8), fontSize = 13.sp)
                        }
                    }
                }

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Approved", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text("$approvedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Rejected", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text("$deniedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                        }
                    }
                }
            }

            // Panic Stop Button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33F43F5E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E))
            ) {
                Text("🚨 Emergency Stop All Agents", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
            }
        }
    }
}
