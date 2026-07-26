package com.zincstate.hepta.domain.usecase

import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.repository.TaskRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class GetTasksForWeek(
    private val repository: TaskRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>> {
        return repository.getTasksForDateRange(startDate, endDate)
    }
}

class GetTasksForWeekSync(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<Task> {
        return repository.getTasksForDateRangeSync(startDate, endDate)
    }
}

class AddTask(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(text: String, date: LocalDate, position: Int = 0) {
        if (text.isBlank()) return
        
        val task = Task(
            text = text.trim(),
            isCompleted = false,
            targetDate = date,
            lastUpdated = System.currentTimeMillis(),
            position = position
        )
        repository.insertTask(task)
    }
}

class UpdateTask(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task.copy(lastUpdated = System.currentTimeMillis()))
    }
}



class DeleteTask(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
}

data class TaskUseCases(
    val getTasksForWeek: GetTasksForWeek,
    val getTasksForWeekSync: GetTasksForWeekSync,
    val addTask: AddTask,
    val updateTask: UpdateTask,
    val upsertTasks: UpsertTasks,
    val deleteTask: DeleteTask
)
