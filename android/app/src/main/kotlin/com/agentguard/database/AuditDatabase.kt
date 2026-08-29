package com.agentguard.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agentguard.database.models.ApprovalEntity

@Database(entities = [ApprovalEntity::class], version = 1, exportSchema = false)
abstract class AuditDatabase : RoomDatabase() {
    abstract fun approvalDao(): ApprovalDao
}
