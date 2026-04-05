package com.zincstate.weekstack.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: WeekstackDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeekstackDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.taskDao
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetTasks() = runTest {
        val today = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            text = "Test Task",
            isCompleted = false,
            targetDateEpochDays = today,
            lastUpdated = System.currentTimeMillis()
        )

        dao.insertTask(task)

        val tasks = dao.getTasksForDateRange(today, today).first()
        assertThat(tasks).hasSize(1)
        assertThat(tasks[0].text).isEqualTo("Test Task")
    }

    @Test
    fun updateTask() = runTest {
        val today = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            id = 1,
            text = "Old Task",
            isCompleted = false,
            targetDateEpochDays = today,
            lastUpdated = 1000L
        )
        dao.insertTask(task)

        val updatedTask = task.copy(text = "Updated Task", isCompleted = true)
        dao.updateTask(updatedTask)

        val tasks = dao.getTasksForDateRange(today, today).first()
        assertThat(tasks[0].text).isEqualTo("Updated Task")
        assertThat(tasks[0].isCompleted).isTrue()
    }

    @Test
    fun deleteTask() = runTest {
        val today = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            id = 1,
            text = "Delete Me",
            isCompleted = false,
            targetDateEpochDays = today,
            lastUpdated = 1000L
        )
        dao.insertTask(task)
        dao.deleteTask(task)

        val tasks = dao.getTasksForDateRange(today, today).first()
        assertThat(tasks).isEmpty()
    }
}
