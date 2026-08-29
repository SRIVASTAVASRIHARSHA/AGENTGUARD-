package com.agentguard.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("agentguard", "AgentGuard Approvals", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showApprovalNotification(request: ApprovalRequest) {
        val notification = NotificationCompat.Builder(context, "agentguard")
            .setContentTitle("CRITICAL ACTION")
            .setContentText(request.command)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        
        notificationManager.notify(request.requestId.hashCode(), notification)
    }
}
