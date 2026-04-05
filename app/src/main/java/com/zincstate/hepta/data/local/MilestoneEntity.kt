package com.zincstate.hepta.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val monthKey: String, // format "YYYY-MM"
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
