package com.zincstate.hepta.domain.usecase

import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.repository.TaskRepository

class UpsertTasks(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(tasks: List<Task>) {
        repository.upsertTasks(tasks.map { it.copy(lastUpdated = System.currentTimeMillis()) })
    }
}
