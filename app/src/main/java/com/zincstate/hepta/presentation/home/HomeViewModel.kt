package com.zincstate.hepta.presentation.home

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
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
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HomeUiState(
    val datesOfWeek: List<LocalDate> = emptyList(),
    val tasksMap: Map<LocalDate, List<Task>> = emptyMap(),
    val morningIntentionsMap: Map<LocalDate, Task?> = emptyMap(),
    val inboxTasks: List<Task> = emptyList(),
    val calendarEventsMap: Map<LocalDate, List<com.zincstate.hepta.domain.model.CalendarEvent>> = emptyMap(),
    val dayTagsMap: Map<LocalDate, String> = emptyMap(),
    val dayLoadMap: Map<LocalDate, Float> = emptyMap(), // Load factor 0.0 - 1.0
    val lastUpdatedMap: Map<LocalDate, Long> = emptyMap(),
    val expandedDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isDarkMode: Boolean = true,
    val showStats: Boolean = false,
    val completionStats: Map<LocalDate, Float> = emptyMap(),
    val hasCalendarPermission: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    private val shiftTasks: com.zincstate.hepta.domain.usecase.ShiftTasksUseCase,
    private val getCalendarEvents: com.zincstate.hepta.domain.usecase.GetCalendarEventsUseCase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val INBOX_DATE = LocalDate.MAX

    private fun calculateDayLoad() {
        val tasksMap = _state.value.tasksMap
        val calendarEventsMap = _state.value.calendarEventsMap
        val dates = _state.value.datesOfWeek
        
        val counts = dates.associateWith { date ->
            (tasksMap[date]?.size ?: 0) + (calendarEventsMap[date]?.size ?: 0)
        }
        
        val maxCount = (counts.values.maxOfOrNull { it } ?: 1).toFloat().coerceAtLeast(1f)
        
        val loads = counts.mapValues { entry ->
            entry.value.toFloat() / maxCount
        }
        
        _state.update { it.copy(dayLoadMap = loads) }
    }
    
    fun shiftUnfinishedTasks(fromDate: LocalDate, toDate: LocalDate) {
        viewModelScope.launch {
            val unfinished = _state.value.tasksMap[fromDate]?.filter { !it.isCompleted && !it.isMorningIntention } ?: return@launch
            shiftTasks(fromDate, toDate, unfinished)
        }
    }

    fun shiftToInbox(task: Task) {
        viewModelScope.launch {
            useCases.updateTask(task.copy(targetDate = INBOX_DATE, isCompleted = false))
        }
    }

    private fun checkCalendarPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun updatePermissionStatus() {
        val hasPermission = checkCalendarPermission()
        _state.update { it.copy(hasCalendarPermission = hasPermission) }
        if (hasPermission) {
            fetchCalendarEvents()
        }
    }

    private fun fetchCalendarEvents() {
        if (!checkCalendarPermission()) return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val start = _state.value.datesOfWeek.firstOrNull() ?: return@launch
            val end = _state.value.datesOfWeek.lastOrNull() ?: return@launch
            
            val events = getCalendarEvents(start, end)
            val grouped = events.groupBy { it.date }
            
            // Extract All-Day events for Day Tags (e.g. Holidays/Birthdays)
            val tags = grouped.mapValues { entry ->
                entry.value.filter { it.isAllDay }
                    .joinToString(" • ") { it.title }
                    .takeIf { it.isNotBlank() }
            }.filterValues { it != null } as Map<LocalDate, String>

            _state.update {
                it.copy(
                    calendarEventsMap = grouped,
                    dayTagsMap = tags
                )
            }
            calculateDayLoad()
        }
    }

    fun toggleTheme() {
        _state.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleStats() {
        _state.update { it.copy(showStats = !it.showStats) }
    }

    fun exportTasksToCsv(context: Context) {
        viewModelScope.launch {
            val allTasks = _state.value.tasksMap.values.flatten().sortedBy { it.targetDate }
            val csvHeader = "Date,Task,Status,Recurring\n"
            val csvContent = allTasks.joinToString("\n") { task ->
                "${task.targetDate},\"${task.text}\",${if (task.isCompleted) "Done" else "Pending"},${if (task.recurringType > 0) "Daily" else "None"}"
            }
            val fullCsv = csvHeader + csvContent
            
            val file = File(context.cacheDir, "HEPTA_tasks_export.csv")
            file.writeText(fullCsv)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "HEPTA Tasks Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Tasks"))
        }
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

        // Observe the current week + the "Inbox" (MAX date)
        observeTasks(startOfWeek, endOfWeek)
    }

    private fun observeTasks(start: LocalDate, end: LocalDate) {
        // Fetch tasks for the week + Inbox tasks
        // Since getTasksForWeek is a range, we can fetch from start to MAX to include Inbox
        useCases.getTasksForWeek(start, INBOX_DATE).onEach { allTasks ->
            val inbox = allTasks.filter { it.targetDate == INBOX_DATE }
            val weekTasks = allTasks.filter { it.targetDate != INBOX_DATE }
            val grouped = weekTasks.filter { !it.isMorningIntention }.groupBy { it.targetDate }
            val intentions = weekTasks.filter { it.isMorningIntention }.associateBy { it.targetDate }
            
            val lastUpdated = weekTasks.groupBy { it.targetDate }.mapValues { entry ->
                entry.value.maxOfOrNull { it.lastUpdated } ?: 0L
            }
            val stats = grouped.mapValues { entry ->
                val dayTasks = entry.value
                if (dayTasks.isEmpty()) 0f else {
                    dayTasks.count { it.isCompleted }.toFloat() / dayTasks.size
                }
            }
            _state.update {
                it.copy(
                    tasksMap = grouped,
                    morningIntentionsMap = intentions,
                    inboxTasks = inbox,
                    lastUpdatedMap = lastUpdated,
                    completionStats = stats,
                    isLoading = false
                )
            }
            calculateDayLoad()
        }.launchIn(viewModelScope)
    }

    fun updateMorningIntention(date: LocalDate, text: String) {
        viewModelScope.launch {
            val existing = _state.value.morningIntentionsMap[date]
            if (existing != null) {
                useCases.updateTask(existing.copy(text = text))
            } else {
                // Add new special task with position -1 and isMorningIntention = true
                val task = Task(
                    text = text,
                    isCompleted = false,
                    targetDate = date,
                    lastUpdated = System.currentTimeMillis(),
                    position = -1,
                    isMorningIntention = true
                )
                useCases.upsertTasks(listOf(task))
            }
        }
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
            val currentTasks = _state.value.tasksMap[date] ?: emptyList()
            val maxPosition = currentTasks.maxOfOrNull { it.position } ?: -1
            useCases.addTask(text, date, maxPosition + 1)
        }
    }

    fun onMoveTask(date: LocalDate, fromIndex: Int, toIndex: Int) {
        onMoveTaskToDate(date, fromIndex, date, toIndex)
    }

    fun onMoveTaskToDate(fromDate: LocalDate, fromIndex: Int, toDate: LocalDate, toIndex: Int) {
        val fromTasks = _state.value.tasksMap[fromDate]?.toMutableList() ?: return
        if (fromIndex !in fromTasks.indices) return

        val movedTask = fromTasks.removeAt(fromIndex)
        val updatedFromTasks = fromTasks.mapIndexed { index, task ->
            task.copy(position = index)
        }

        val updatedToTasks = if (fromDate == toDate) {
            val toTasks = updatedFromTasks.toMutableList()
            val targetIndex = toIndex.coerceIn(0, toTasks.size)
            toTasks.add(targetIndex, movedTask)
            toTasks.mapIndexed { index, task ->
                task.copy(position = index, targetDate = toDate)
            }
        } else {
            val toTasks = _state.value.tasksMap[toDate]?.toMutableList() ?: mutableListOf()
            val targetIndex = toIndex.coerceIn(0, toTasks.size)
            toTasks.add(targetIndex, movedTask)
            toTasks.mapIndexed { index, task ->
                task.copy(position = index, targetDate = toDate)
            }
        }

        viewModelScope.launch {
            if (fromDate == toDate) {
                useCases.upsertTasks(updatedToTasks)
            } else {
                useCases.upsertTasks(updatedFromTasks + updatedToTasks)
            }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            useCases.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun toggleTaskRecurrence(task: Task) {
        val newType = if (task.recurringType == 0) 1 else 0
        viewModelScope.launch {
            useCases.updateTask(task.copy(recurringType = newType))
        }
    }

    fun startFocusSession(context: android.content.Context, task: Task) {
        com.zincstate.hepta.service.FocusService.start(context, task.text)
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
