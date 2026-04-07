package com.zincstate.hepta.presentation.home

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.zincstate.hepta.domain.usecase.GetCalendarEventsUseCase
import com.zincstate.hepta.domain.usecase.ShiftTasksUseCase
import com.zincstate.hepta.domain.usecase.TaskUseCases
import com.zincstate.hepta.ui.theme.ZenTheme
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

    private lateinit var viewModel: HomeViewModel
    private lateinit var useCases: TaskUseCases
    private lateinit var milestoneDao: com.zincstate.hepta.data.local.MilestoneDao
    private lateinit var shiftTasks: ShiftTasksUseCase
    private lateinit var getCalendarEvents: GetCalendarEventsUseCase
    private lateinit var context: Context

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        useCases = mockk(relaxed = true)
        milestoneDao = mockk(relaxed = true)
        shiftTasks = mockk(relaxed = true)
        getCalendarEvents = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock the flow of tasks
        every { useCases.getTasksForWeek(any(), any()) } returns flowOf(emptyList())
        // Mock the flow of milestones
        every { milestoneDao.getMilestonesForMonth(any()) } returns flowOf(emptyList())

        viewModel = HomeViewModel(
            useCases = useCases,
            milestoneDao = milestoneDao,
            shiftTasks = shiftTasks,
            getCalendarEvents = getCalendarEvents,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasCorrectDates() {
        val state = viewModel.state.value
        assertThat(state.datesOfWeek).hasSize(7)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun onThemeChangeUpdatesState() {
        val newTheme = ZenTheme.NORD
        viewModel.onThemeChange(newTheme)
        
        val state = viewModel.state.value
        assertThat(state.currentZenTheme).isEqualTo(newTheme)
        assertThat(state.isDarkMode).isTrue()
    }

    @Test
    fun statsCalculationForEmptyTasks() {
        val state = viewModel.state.value
        assertThat(state.weekProgress).isEqualTo(0f)
        assertThat(state.totalCompletedTasks).isEqualTo(0)
    }
}
