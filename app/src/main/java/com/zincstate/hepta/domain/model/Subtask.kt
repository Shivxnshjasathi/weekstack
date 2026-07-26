package com.zincstate.hepta.domain.model

import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false
)
