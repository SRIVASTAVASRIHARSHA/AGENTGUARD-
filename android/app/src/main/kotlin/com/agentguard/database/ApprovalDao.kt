package com.agentguard.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agentguard.database.models.ApprovalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalEntity)

    @Query("SELECT * FROM approvals ORDER BY timestamp DESC")
    fun getAllApprovals(): Flow<List<ApprovalEntity>>
}
