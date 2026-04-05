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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import com.zincstate.hepta.data.local.MilestoneEntity
import com.zincstate.hepta.domain.model.Milestone
import com.zincstate.hepta.ui.theme.ZenTheme

data class HomeUiState(
    val datesOfWeek: List<LocalDate> = emptyList(),
    val tasksMap: Map<LocalDate, List<Task>> = emptyMap(),
    val morningIntentionsMap: Map<LocalDate, Task?> = emptyMap(),
    val inboxTasks: List<Task> = emptyList(),
    val calendarEventsMap: Map<LocalDate, List<com.zincstate.hepta.domain.model.CalendarEvent>> = emptyMap(),
    val dayTagsMap: Map<LocalDate, String> = emptyMap(),
    val dayLoadMap: Map<LocalDate, Float> = emptyMap(),
    val lastUpdatedMap: Map<LocalDate, Long> = emptyMap(),
    val expandedDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isDarkMode: Boolean = true,
    val currentZenTheme: ZenTheme = ZenTheme.OBSIDIAN,
    val customThemeColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    val milestones: List<com.zincstate.hepta.domain.model.Milestone> = emptyList(),
    val selectedCalendarMonth: java.time.YearMonth = java.time.YearMonth.now(),
    val showStats: Boolean = false,
    val completionStats: Map<LocalDate, Float> = emptyMap(),
    val hasCalendarPermission: Boolean = false,
    val weekProgress: Float = 0f,
    val totalDeepWorkCount: Int = 0,
    val totalCompletedTasks: Int = 0,
    val totalTasks: Int = 0,
    val isVaultEnabled: Boolean = false,
    val isVaultAuthenticated: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    private val database: com.zincstate.hepta.data.local.HeptaDatabase,
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

        // Load settings from AppPreferences
        val savedTheme = com.zincstate.hepta.util.AppPreferences.getTheme(context)
        val customHex = com.zincstate.hepta.util.AppPreferences.getCustomColorHex(context)
        val customColor = customHex?.let { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) } ?: androidx.compose.ui.graphics.Color.Unspecified
        val vaultEnabled = com.zincstate.hepta.util.AppPreferences.isVaultEnabled(context)
        
        _state.update {
            it.copy(
                datesOfWeek = datesRow,
                expandedDate = today,
                currentZenTheme = savedTheme,
                customThemeColor = customColor,
                isDarkMode = savedTheme != com.zincstate.hepta.ui.theme.ZenTheme.ARCTIC && savedTheme != com.zincstate.hepta.ui.theme.ZenTheme.SEPIA,
                isVaultEnabled = vaultEnabled,
                isVaultAuthenticated = !vaultEnabled // If vault is off, consider authenticated
            )
        }

        // Collect Milestones reactively based on selected month
        viewModelScope.launch {
            _state.map { it.selectedCalendarMonth }.distinctUntilChanged().collect { month ->
                val monthKey = "${month.year}-${String.format("%02d", month.monthValue)}"
                database.milestoneDao.getMilestonesForMonth(monthKey).collect { entities: List<MilestoneEntity> ->
                    _state.update { it.copy(milestones = entities.map { e ->
                        com.zincstate.hepta.domain.model.Milestone(
                            id = e.id,
                            text = e.text,
                            monthKey = e.monthKey,
                            isCompleted = e.isCompleted,
                            lastUpdated = e.lastUpdated
                        )
                    }) }
                }
            }
        }

        // Observe the current week + the "Infinity Inbox" (MAX date)
        observeTasks(startOfWeek, endOfWeek)
    }

    private fun observeTasks(start: LocalDate, end: LocalDate) {
        // Query from start of week to MAX date to include the Infinity Inbox
        useCases.getTasksForWeek(start, INBOX_DATE).onEach { tasks ->
            // 1. Separate morning intentions
            val morningIntentions = tasks.filter { it.isMorningIntention }
            
            // 2. Weekly tasks (tasks on actual days this week)
            val weekRange = _state.value.datesOfWeek
            val regularTasks = tasks.filter { !it.isMorningIntention && it.targetDate in weekRange }
            val regularTasksMap = regularTasks.groupBy { it.targetDate }
            
            // 3. Infinity Inbox tasks (targetDate == INBOX_DATE)
            val inboxTasks = tasks.filter { it.targetDate == INBOX_DATE }
            val intentionsMap = morningIntentions.associateBy { it.targetDate }
            
            _state.update { 
                it.copy(
                    tasksMap = regularTasksMap,
                    morningIntentionsMap = intentionsMap,
                    inboxTasks = inboxTasks,
                    isLoading = false
                )
            }
            calculateDayLoad()
            calculateWeeklyStats()
        }.launchIn(viewModelScope)
    }

    private fun calculateWeeklyStats() {
        val tasksMap = _state.value.tasksMap
        val allWeeklyTasks = tasksMap.values.flatten()
        
        val totalTasksCount = allWeeklyTasks.size
        val completedTasksCount = allWeeklyTasks.count { it.isCompleted }
        val deepWorkCount = allWeeklyTasks.count { it.isCompleted && it.isFocusCompleted }
        
        val progress = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount else 0f
        
        // Calculate per-day completion stats for the bar chart
        val statsMap = _state.value.datesOfWeek.associateWith { date ->
            val dayTasks = tasksMap[date] ?: emptyList()
            if (dayTasks.isEmpty()) 0f 
            else dayTasks.count { it.isCompleted }.toFloat() / dayTasks.size
        }
        
        _state.update {
            it.copy(
                weekProgress = progress,
                totalDeepWorkCount = deepWorkCount,
                totalCompletedTasks = completedTasksCount,
                totalTasks = totalTasksCount,
                completionStats = statsMap
            )
        }
    }

    fun onThemeChange(theme: ZenTheme) {
        com.zincstate.hepta.util.AppPreferences.setTheme(context, theme)
        _state.update { 
            it.copy(
                currentZenTheme = theme,
                isDarkMode = theme != ZenTheme.ARCTIC && theme != ZenTheme.SEPIA
            )
        }
    }

    fun onCustomThemeChange(color: androidx.compose.ui.graphics.Color) {
        val hexString = String.format("#%08X", color.value.toLong() and 0xFFFFFFFFL)
        com.zincstate.hepta.util.AppPreferences.setCustomColorHex(context, hexString)
        com.zincstate.hepta.util.AppPreferences.setTheme(context, ZenTheme.CUSTOM)
        _state.update {
            it.copy(
                currentZenTheme = ZenTheme.CUSTOM,
                customThemeColor = color,
                isDarkMode = true
            )
        }
    }

    fun applyPreset(presetType: com.zincstate.hepta.domain.model.PresetType) {
        viewModelScope.launch {
            val dates = _state.value.datesOfWeek
            
            if (presetType == com.zincstate.hepta.domain.model.PresetType.DEFAULT) {
                // Clear all tasks for the current week
                val allTasks = _state.value.tasksMap.values.flatten()
                allTasks.forEach { useCases.deleteTask(it) }
                return@launch
            }

            val templates = com.zincstate.hepta.domain.model.PresetTemplates.presets[presetType] ?: return@launch
            val tasksToInsert = mutableListOf<Task>()
            
            dates.forEach { date ->
                templates.forEachIndexed { index, template ->
                    tasksToInsert.add(
                        Task(
                            text = template.text,
                            isCompleted = false,
                            targetDate = date,
                            lastUpdated = System.currentTimeMillis(),
                            position = 100 + index, // High position to avoid conflicts
                            isFocusCompleted = false
                        )
                    )
                }
            }
            useCases.upsertTasks(tasksToInsert)
        }
    }

    fun onCalendarMonthChange(month: java.time.YearMonth) {
        _state.update { it.copy(selectedCalendarMonth = month) }
        // Fetch milestones for the new month
        viewModelScope.launch {
            val monthKey = "${month.year}-${String.format("%02d", month.monthValue)}"
            // I'll need to observe this from the DAO
            // For now, I'll just trigger a refresh if I had a repository call
            // Since I'm using MVI, I'll update the state directly via collection
        }
    }

    fun addMilestone(text: String) {
        viewModelScope.launch {
            val month = _state.value.selectedCalendarMonth
            val monthKey = "${month.year}-${String.format("%02d", month.monthValue)}"
            val entity = com.zincstate.hepta.data.local.MilestoneEntity(
                text = text,
                monthKey = monthKey
            )
            database.milestoneDao.upsertMilestone(entity)
        }
    }

    fun toggleMilestone(milestone: com.zincstate.hepta.domain.model.Milestone) {
        viewModelScope.launch {
            val entity = com.zincstate.hepta.data.local.MilestoneEntity(
                id = milestone.id,
                text = milestone.text,
                monthKey = milestone.monthKey,
                isCompleted = !milestone.isCompleted,
                lastUpdated = System.currentTimeMillis()
            )
            database.milestoneDao.upsertMilestone(entity)
        }
    }

    fun deleteMilestone(milestone: com.zincstate.hepta.domain.model.Milestone) {
        viewModelScope.launch {
            val entity = com.zincstate.hepta.data.local.MilestoneEntity(
                id = milestone.id,
                text = milestone.text,
                monthKey = milestone.monthKey
            )
            database.milestoneDao.deleteMilestone(entity)
        }
    }

    fun toggleVault(enabled: Boolean) {
        com.zincstate.hepta.util.AppPreferences.setVaultEnabled(context, enabled)
        _state.update { it.copy(isVaultEnabled = enabled) }
    }

    fun setVaultAuthenticated(authenticated: Boolean) {
        _state.update { it.copy(isVaultAuthenticated = authenticated) }
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
