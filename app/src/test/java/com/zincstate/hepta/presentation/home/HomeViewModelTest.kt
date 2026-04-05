package com.zincstate.hepta.presentation.home

import com.google.common.truth.Truth.assertThat
import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.usecase.TaskUseCases
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val useCases = mockk<TaskUseCases>(relaxed = true)
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behavior for state initialization
        val today = LocalDate.now()
        every { useCases.getTasksForWeek(any(), any()) } returns flowOf(emptyList())

        viewModel = HomeViewModel(useCases)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoadingState() = runTest {
        assertThat(viewModel.state.value.isLoading).isTrue()
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.datesOfWeek).hasSize(7)
    }

    @Test
    fun toggleThemeUpdatesState() = runTest {
        val initialTheme = viewModel.state.value.isDarkMode
        viewModel.toggleTheme()
        assertThat(viewModel.state.value.isDarkMode).isNotEqualTo(initialTheme)
    }

    @Test
    fun toggleDayExpansionCorrectly() = runTest {
        val date = LocalDate.now()
        viewModel.toggleDayExpansion(date)
        assertThat(viewModel.state.value.expandedDate).isEqualTo(date)
        
        viewModel.toggleDayExpansion(date)
        assertThat(viewModel.state.value.expandedDate).isNull()
    }

    @Test
    fun addTaskCallsUseCase() = runTest {
        val text = "Task 1"
        val date = LocalDate.now()
        
        viewModel.addTask(text, date)
        
        coVerify { useCases.addTask(text, date) }
    }

    @Test
    fun completeTaskCallsUseCase() = runTest {
        val task = Task(id = 1, text = "Task", isCompleted = false, targetDate = LocalDate.now(), lastUpdated = 0L)
        
        viewModel.toggleTask(task)
        
        coVerify { useCases.updateTask(match { it.isCompleted }) }
    }
}
