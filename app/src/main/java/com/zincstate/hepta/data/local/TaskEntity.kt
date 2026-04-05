package com.zincstate.hepta.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val isCompleted: Boolean,
    val targetDateEpochDays: Long,
    val lastUpdated: Long,
    val position: Int = 0,
    val recurringType: Int = 0, // 0=None, 1=Daily, 2=Weekly
    val isFocusCompleted: Boolean = false,
    val isMorningIntention: Boolean = false
)
