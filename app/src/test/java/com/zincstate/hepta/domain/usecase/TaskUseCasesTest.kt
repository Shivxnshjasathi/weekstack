package com.zincstate.hepta.domain.usecase

import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.repository.TaskRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class TaskUseCasesTest {

    private lateinit var repository: TaskRepository
    private lateinit var useCases: TaskUseCases

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCases = TaskUseCases(
            getTasksForWeek = GetTasksForWeek(repository),
            addTask = AddTask(repository),
            updateTask = UpdateTask(repository),
            upsertTasks = UpsertTasks(repository),
            deleteTask = DeleteTask(repository),
            getTasksForWeekSync = GetTasksForWeekSync(repository)
        )
    }

    @Test
    fun addTaskValidatesEmptyText() = runBlocking {
        useCases.addTask("", LocalDate.now())
        coVerify(exactly = 0) { repository.insertTask(any()) }
    }

    @Test
    fun addTaskTrimsAndInserts() = runBlocking {
        val testText = "  Finish Play Store  "
        useCases.addTask(testText, LocalDate.now())
        
        coVerify { 
            repository.insertTask(match { it.text == "Finish Play Store" }) 
        }
    }

    @Test
    fun updateTaskUpdatesTimestampAndCallsRepository() = runBlocking {
        val originalTime = 1000L
        val task = Task(
            id = 1,
            text = "Test", 
            lastUpdated = originalTime, 
            targetDate = LocalDate.now(),
            isCompleted = false,
            position = 0
        )
        
        useCases.updateTask(task)
        
        coVerify { 
            repository.updateTask(match { it.id == 1 && it.lastUpdated > originalTime })
        }
    }

    @Test
    fun deleteTaskCallsRepository() = runBlocking {
        val task = Task(
            id = 1,
            text = "Remove me", 
            lastUpdated = 0, 
            targetDate = LocalDate.now(),
            isCompleted = false,
            position = 0
        )
        useCases.deleteTask(task)
        coVerify { repository.deleteTask(task) }
    }
    
    @Test
    fun getTasksForWeekCallsRepository() = runBlocking {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)
        val mockTasks = listOf(
            Task(id = 1, text = "T1", lastUpdated = 0, targetDate = startDate, isCompleted = false, position = 0)
        )
        
        every { repository.getTasksForDateRange(startDate, endDate) } returns flowOf(mockTasks)
        
        val result = useCases.getTasksForWeek(startDate, endDate).first()
        
        assert(result.size == 1)
        assert(result[0].text == "T1")
        coVerify { repository.getTasksForDateRange(startDate, endDate) }
    }
}
