package com.zincstate.hepta.domain.repository

import com.zincstate.hepta.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun getTasksForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun upsertTasks(tasks: List<Task>)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
}
