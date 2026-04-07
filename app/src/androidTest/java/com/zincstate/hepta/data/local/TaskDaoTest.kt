package com.zincstate.hepta.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: HeptaDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        // Use an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HeptaDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.taskDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetTasks() = runBlocking {
        val date = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            text = "Test Task",
            isCompleted = false,
            targetDateEpochDays = date,
            lastUpdated = System.currentTimeMillis(),
            position = 0
        )
        dao.insertTask(task)

        val tasks = dao.getTasksForDateRange(date, date).first()
        assertThat(tasks).hasSize(1)
        assertThat(tasks[0].text).isEqualTo("Test Task")
    }

    @Test
    fun deleteAndVerifyEmpty() = runBlocking {
        val date = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            text = "Delete Me",
            isCompleted = false,
            targetDateEpochDays = date,
            lastUpdated = System.currentTimeMillis(),
            position = 0
        )
        dao.insertTask(task)
        
        val savedTask = dao.getTasksForDateRange(date, date).first()[0]
        dao.deleteTask(savedTask)

        val tasks = dao.getTasksForDateRange(date, date).first()
        assertThat(tasks).isEmpty()
    }

    @Test
    fun updateTaskStatus() = runBlocking {
        val date = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            text = "Update Me",
            isCompleted = false,
            targetDateEpochDays = date,
            lastUpdated = System.currentTimeMillis()
        )
        dao.insertTask(task)

        val savedTask = dao.getTasksForDateRange(date, date).first()[0]
        dao.updateTask(savedTask.copy(isCompleted = true))

        val updatedTask = dao.getTasksForDateRange(date, date).first()[0]
        assertThat(updatedTask.isCompleted).isTrue()
    }
}
