package com.agentguard.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("agentguard", "AgentGuard Approvals", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 3000)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showApprovalNotification(request: ApprovalRequest) {
        // Trigger 3-second haptic vibration immediately
        triggerThreeSecondVibration()

        val notification = NotificationCompat.Builder(context, "agentguard")
            .setContentTitle("🚨 CRITICAL ACTION REQUIRED (${request.riskScore})")
            .setContentText(request.command)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 3000))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        
        notificationManager.notify(request.requestId.hashCode(), notification)
    }

    private fun triggerThreeSecondVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Vibrator::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(3000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
