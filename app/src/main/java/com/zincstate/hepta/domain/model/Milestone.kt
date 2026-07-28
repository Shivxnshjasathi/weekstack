package com.zincstate.hepta.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Milestone(
    val id: Int = 0,
    val text: String,
    val monthKey: String,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
