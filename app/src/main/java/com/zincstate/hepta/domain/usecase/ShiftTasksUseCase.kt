package com.zincstate.hepta.domain.usecase

import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.repository.TaskRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject

class ShiftTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(from: LocalDate, to: LocalDate, tasks: List<Task>) {
        if (tasks.isEmpty()) return
        
        // Fetch current max position for target day
        val targetTasks = repository.getTasksForDateRange(to, to).firstOrNull() ?: emptyList()
        val nextPosition = (targetTasks.maxOfOrNull { it.position } ?: -1) + 1
        
        val updatedTasks = tasks.mapIndexed { index, task ->
            task.copy(
                targetDate = to,
                isCompleted = false,
                position = nextPosition + index,
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        repository.upsertTasks(updatedTasks)
    }
}

// Wait, getting tasks as a Flow inside a suspend function isn't ideal for a one-shot fix.
// I'll rewrite this to be a cleaner room-based batch move.
