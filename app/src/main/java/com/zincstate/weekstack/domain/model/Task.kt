package com.zincstate.weekstack.domain.model

import java.time.LocalDate

data class Task(
    val id: Int = 0,
    val text: String,
    val isCompleted: Boolean,
    val targetDate: LocalDate,
    val lastUpdated: Long
)
