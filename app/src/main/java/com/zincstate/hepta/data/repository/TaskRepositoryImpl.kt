package com.zincstate.hepta.data.repository

import com.zincstate.hepta.data.local.TaskDao
import com.zincstate.hepta.data.mapper.toDomainTask
import com.zincstate.hepta.data.mapper.toEntity
import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TaskRepositoryImpl(
    private val dao: TaskDao
) : TaskRepository {

    override fun getTasksForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>> {
        return dao.getTasksForDateRange(startDate.toEpochDay(), endDate.toEpochDay())
            .map { entities -> 
                entities.map { it.toDomainTask() } 
            }
    }

    override suspend fun insertTask(task: Task) {
        dao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task.toEntity())
    }
}
