package com.agentguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RiskBadge(riskScore: Int) {
    val color = when {
        riskScore >= 91 -> Color.Red
        riskScore >= 61 -> Color.Yellow
        riskScore >= 31 -> Color.Cyan
        else -> Color.Green
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RISK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$riskScore/100", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ApprovalScreen(command: String, riskScore: Int, reason: String, onApprove: () -> Unit, onDeny: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("CRITICAL ALERT", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
            Text(command, color = Color.LightGray, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp))
        }
        
        RiskBadge(riskScore)
        
        Text(reason, modifier = Modifier.padding(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onDeny, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("DENY")
            }
            Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
                Text("APPROVE")
            }
        }
    }
}
