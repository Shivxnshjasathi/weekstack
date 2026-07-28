package com.zincstate.hepta.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false
)
