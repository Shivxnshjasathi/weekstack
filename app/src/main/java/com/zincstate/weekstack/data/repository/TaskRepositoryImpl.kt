package com.zincstate.weekstack.data.repository

import com.zincstate.weekstack.data.local.TaskDao
import com.zincstate.weekstack.data.mapper.toDomainTask
import com.zincstate.weekstack.data.mapper.toEntity
import com.zincstate.weekstack.domain.model.Task
import com.zincstate.weekstack.domain.repository.TaskRepository
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
