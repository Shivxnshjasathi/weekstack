package com.zincstate.hepta.domain.model

data class Milestone(
    val id: Int = 0,
    val text: String,
    val monthKey: String,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
