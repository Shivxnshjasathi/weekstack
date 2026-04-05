package com.zincstate.hepta.domain.usecase

import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.repository.TaskRepository
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

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
            deleteTask = DeleteTask(repository)
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
    fun deleteTaskCallsRepository() = runBlocking {
        val task = Task(text = "Remove me", lastUpdated = 0, targetDate = LocalDate.now())
        useCases.deleteTask(task)
        coVerify { repository.deleteTask(task) }
    }
}
