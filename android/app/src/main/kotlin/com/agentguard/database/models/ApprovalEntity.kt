package com.agentguard.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approvals")
data class ApprovalEntity(
    @PrimaryKey val requestId: String,
    val command: String,
    val status: String,
    val timestamp: Long
)
