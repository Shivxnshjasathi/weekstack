package com.zincstate.hepta.domain.model

import java.time.LocalDate

data class Task(
    val id: Int = 0,
    val text: String,
    val isCompleted: Boolean,
    val targetDate: LocalDate,
    val lastUpdated: Long,
    val position: Int = 0,
    val recurringType: Int = 0,
    val isFocusCompleted: Boolean = false,
    val isMorningIntention: Boolean = false
)
