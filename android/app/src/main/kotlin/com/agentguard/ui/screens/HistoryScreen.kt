package com.agentguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class ApprovalLog(val command: String, val status: String, val formattedTime: String)

@Composable
fun HistoryScreen(logs: List<ApprovalLog>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ACTION HISTORY", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(logs) { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(log.command, modifier = Modifier.weight(1f))
                        Text(log.status, color = if (log.status == "APPROVED") Color.Green else Color.Red)
                    }
                }
            }
        }
    }
}
