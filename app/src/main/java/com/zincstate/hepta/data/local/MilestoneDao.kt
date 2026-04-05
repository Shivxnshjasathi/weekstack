package com.zincstate.hepta.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE monthKey = :monthKey ORDER BY lastUpdated DESC")
    fun getMilestonesForMonth(monthKey: String): Flow<List<MilestoneEntity>>

    @Upsert
    suspend fun upsertMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)
}
