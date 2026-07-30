package com.zincstate.hepta.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class Task(
    val id: Int = 0,
    val text: String,
    val isCompleted: Boolean,
    val targetDate: LocalDate,
    val lastUpdated: Long,
    val position: Int = 0,
    val recurringType: Int = 0,
    val isFocusCompleted: Boolean = false,
    val isMorningIntention: Boolean = false,
    val reminderTime: Long? = null,
    val notes: String? = null,
    val subtasks: List<Subtask> = emptyList()
)
