package com.zincstate.hepta.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateEpochDays: Long,
    val category: String, // "work" or "personal"
    val content: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
