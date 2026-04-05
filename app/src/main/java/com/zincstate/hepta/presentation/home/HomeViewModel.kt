package com.zincstate.hepta.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HomeUiState(
    val datesOfWeek: List<LocalDate> = emptyList(),
    val tasksMap: Map<LocalDate, List<Task>> = emptyMap(),
    val lastUpdatedMap: Map<LocalDate, Long> = emptyMap(),
    val expandedDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isDarkMode: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCases: TaskUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun toggleTheme() {
        _state.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    init {
        val today = LocalDate.now()
        // Determine the start of the week (Monday) and end (Sunday)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        val datesRow = mutableListOf<LocalDate>()
        var current = startOfWeek
        while (!current.isAfter(endOfWeek)) {
            datesRow.add(current)
            current = current.plusDays(1)
        }

        _state.update {
            it.copy(
                datesOfWeek = datesRow,
                expandedDate = today
            )
        }

        observeTasks(startOfWeek, endOfWeek)
    }

    private fun observeTasks(start: LocalDate, end: LocalDate) {
        useCases.getTasksForWeek(start, end).onEach { tasks ->
            val grouped = tasks.groupBy { it.targetDate }
            val lastUpdated = tasks.groupBy { it.targetDate }.mapValues { entry ->
                entry.value.maxOfOrNull { it.lastUpdated } ?: 0L
            }
            _state.update {
                it.copy(
                    tasksMap = grouped,
                    lastUpdatedMap = lastUpdated,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun toggleDayExpansion(date: LocalDate) {
        _state.update {
            it.copy(
                expandedDate = if (it.expandedDate == date) null else date
            )
        }
    }

    fun addTask(text: String, date: LocalDate) {
        viewModelScope.launch {
            useCases.addTask(text, date)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            useCases.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun updateTaskText(task: Task, newText: String) {
        viewModelScope.launch {
            useCases.updateTask(task.copy(text = newText))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            useCases.deleteTask(task)
        }
    }
}
