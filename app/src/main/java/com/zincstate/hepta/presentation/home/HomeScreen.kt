package com.zincstate.hepta.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.zincstate.hepta.ui.theme.getZenColors
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.zincstate.hepta.R
import com.zincstate.hepta.presentation.home.components.AddTaskInput
import com.zincstate.hepta.presentation.home.components.CalendarEventItem
import com.zincstate.hepta.presentation.home.components.DayHeader
import com.zincstate.hepta.presentation.home.components.TaskItem
import com.zincstate.hepta.ui.theme.*
import java.time.LocalDate
import com.zincstate.hepta.service.FocusService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAbout: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var draggingTaskId by remember { mutableStateOf<Int?>(null) }
    
    // Calendar Permission handling
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.updatePermissionStatus()
    }
    
    // Check permission on start
    LaunchedEffect(Unit) {
        if (!state.hasCalendarPermission) {
            permissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
        } else {
            viewModel.updatePermissionStatus()
        }
    }

    // Focus Timer State
    val isTimerRunning by FocusService.isRunning.collectAsState()
    val remainingSeconds by FocusService.remainingTime.collectAsState()
    val currentTaskName by FocusService.currentTaskName.collectAsState()
    
    val zenColors = getZenColors(state.currentZenTheme)
    
    // Dynamic Fading for Custom Themes: Create 7 alpha-shaded colors from the base custom color
    val headerShades = remember(state.currentZenTheme, state.customThemeColor) {
        if (state.currentZenTheme == com.zincstate.hepta.ui.theme.ZenTheme.CUSTOM && state.customThemeColor != null) {
            List(7) { i -> state.customThemeColor!!.copy(alpha = 0.03f + (i * 0.08f)) }
        } else {
            zenColors.headerShades
        }
    }
    
    HeptaTheme(zenTheme = state.currentZenTheme) {

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    val edgeThreshold = 30.dp.toPx()
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            // Only trigger edge swipe if the interaction started near the edge
                            if (change.position.x < edgeThreshold || change.previousPosition.x < edgeThreshold) {
                                if (dragAmount > 20f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToAbout()
                                    change.consume()
                                }
                            }
                        }
                    )
                }
        ) {
            val totalHeight = maxHeight
            // Calculate a base height that fills 1/7th of the screen (adjusting for spacers)
            val baseHeaderHeight = (totalHeight - 80.dp) / 7.2f 

            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Persistent Focus Header (Sticky)
                AnimatedVisibility(
                    visible = isTimerRunning,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    FocusTimerOverlay(
                        taskName = currentTaskName,
                        remainingSeconds = remainingSeconds,
                        totalDurationMinutes = state.selectedFocusDuration,
                        onStop = { FocusService.stop(context) }
                    )
                }

                // 1.5 Header with Identity Signature
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEPTA",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 4.sp
                    )
                    
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 2.sp,
                        modifier = Modifier.clickable { onNavigateToAbout() }
                    )
                }

                // 2. Task List (Hybrid LazyColumn for Scrolling + Grid look)
                if (!state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 800.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                        state.datesOfWeek.forEachIndexed { index, date ->
                            val isExpanded = state.expandedDate == date
                            val tasksForDay = state.tasksMap[date] ?: emptyList()
                            val lastUpdated = state.lastUpdatedMap[date]
                            val eventTag = state.dayTagsMap[date]
                            val calendarEvents = (state.calendarEventsMap[date] ?: emptyList()).filter { !it.isAllDay }.sortedBy { it.startTime }
                            val loadFactor = state.dayLoadMap[date] ?: 0f
                            val tomorrow = state.datesOfWeek.getOrNull(index + 1)
                            val uncompletedTasks = tasksForDay.filter { !it.isCompleted }

                            // 1. Day Header (always visible)
                            item(key = "day_${date.toEpochDay()}") {
                                DayHeader(
                                    date = date,
                                    isExpanded = isExpanded,
                                    backgroundColor = headerShades.getOrElse(index) { zenColors.colorScheme.surface },
                                    lastUpdated = lastUpdated,
                                    eventTag = eventTag,
                                    loadFactor = loadFactor,
                                    onHeaderClick = { viewModel.toggleDayExpansion(date) },
                                    modifier = Modifier.heightIn(min = if (!isExpanded) baseHeaderHeight else 0.dp)
                                ) { }
                            }

                            // 2. Expanded Content (Individual Items for performance)
                            if (isExpanded) {
                                // 2a. Quick Actions
                                if (tomorrow != null && uncompletedTasks.isNotEmpty()) {
                                    item(key = "shift_${date.toEpochDay()}") {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                                .clickable { 
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.shiftUnfinishedTasks(date, tomorrow) 
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "SHIFT ${uncompletedTasks.size} TO ${tomorrow.dayOfWeek.name}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }

                                // 2b. Calendar Events
                                items(
                                    items = calendarEvents,
                                    key = { "event_${date.toEpochDay()}_${it.id}" }
                                ) { event ->
                                    CalendarEventItem(event = event)
                                }

                                // 2c. Tasks
                                itemsIndexed(
                                    items = tasksForDay.sortedBy { it.position },
                                    key = { _, task -> task.id }
                                ) { taskIndex, task ->
                                    val isDragging = draggingTaskId == task.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .pointerInput(task.id, tasksForDay.size) {
                                                var accumulatedOffset = 0f
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = { 
                                                         haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                         draggingTaskId = task.id
                                                         accumulatedOffset = 0f
                                                    },
                                                    onDragEnd = { draggingTaskId = null },
                                                    onDragCancel = { draggingTaskId = null },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        accumulatedOffset += dragAmount.y
                                                        
                                                        val currentItemInfo = listState.layoutInfo.visibleItemsInfo
                                                            .find { it.key == "day_${date.toEpochDay()}" } ?: return@detectDragGesturesAfterLongPress
                                                        val globalY = currentItemInfo.offset + accumulatedOffset
                                                        
                                                        val targetItem = listState.layoutInfo.visibleItemsInfo
                                                            .find { globalY > it.offset && globalY < (it.offset + it.size) }

                                                        if (targetItem != null && targetItem.key != "day_${date.toEpochDay()}") {
                                                            val targetKey = targetItem.key.toString()
                                                            if (targetKey.startsWith("day_")) {
                                                                val targetDateEpoch = targetKey.removePrefix("day_").toLongOrNull()
                                                                if (targetDateEpoch != null) {
                                                                    val targetDate = LocalDate.ofEpochDay(targetDateEpoch)
                                                                    viewModel.onMoveTaskToDate(date, taskIndex, targetDate, 0)
                                                                    accumulatedOffset = 0f
                                                                }
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                    ) {
                                        TaskItem(
                                            task = task,
                                            onToggle = { viewModel.toggleTask(task) },
                                            onUpdate = { newText -> viewModel.updateTaskText(task, newText) },
                                            onDelete = { viewModel.deleteTask(task) },
                                            onFocus = { viewModel.startFocusSession(context, task) },
                                            onToggleRecurring = { viewModel.toggleTaskRecurrence(task) },
                                            onShiftToInbox = { viewModel.shiftToInbox(task) },
                                            onSetReminder = { time -> viewModel.setTaskReminder(task, time) },
                                            selectedFocusDuration = state.selectedFocusDuration,
                                            onCycleFocusDuration = { viewModel.cycleFocusDuration() },
                                            isDragging = isDragging
                                        )
                                    }
                                }

                                // 2d. Add Input
                                item(key = "add_${date.toEpochDay()}") {
                                    AddTaskInput(
                                        onAddTask = { text -> viewModel.addTask(text, date) }
                                    )
                                }
                            }
                        }

                        // 4. THE INFINITE SHELF (Analytics + Future Log)
                        item(key = "infinite_shelf") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Spacer(modifier = Modifier.height(64.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(32.dp))

                                // 4a. Zen Weekly Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "WEEK PROGRESS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${(state.weekProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "DEEP WORK",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${state.totalDeepWorkCount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "VELOCITY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${state.totalCompletedTasks}/${state.totalTasks}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(64.dp))

                            }
                        }

                        // 4c. Inbox Tasks within the Shelf
                        if (state.inboxTasks.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                    Text(
                                        text = "∞ THE INFINITY INBOX",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            items(
                                items = state.inboxTasks,
                                key = { "inbox_shelf_${it.id}" }
                            ) { task ->
                                TaskItem(
                                    task = task,
                                    onToggle = { viewModel.toggleTask(task) },
                                    onUpdate = { viewModel.updateTaskText(task, it) },
                                    onDelete = { viewModel.deleteTask(task) },
                                    onFocus = { viewModel.startFocusSession(context, task) },
                                    onToggleRecurring = { viewModel.toggleTaskRecurrence(task) },
                                    onShiftToInbox = { viewModel.shiftToInbox(task) },
                                    onSetReminder = { time -> viewModel.setTaskReminder(task, time) },
                                    selectedFocusDuration = state.selectedFocusDuration,
                                    onCycleFocusDuration = { viewModel.cycleFocusDuration() },
                                    isDragging = false
                                )
                            }
                        }

                        item(key = "nav_spacer") {
                            Spacer(modifier = Modifier.navigationBarsPadding().height(120.dp))
                        }
                    }
                }
            }
            } // End of Column (line 122)

            // 3. The Identity Nexus Dock (Glassmorphic)
            val infiniteTransition = rememberInfiniteTransition(label = "nexus_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .zIndex(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The Lexus Nexus Node (Identity Shortcut)
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary, // The accent color of the theme
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onNavigateToCalendar() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.background,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleStats() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        ),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Zen Analytics",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 4. Weekly Stats sheet
            if (state.showStats) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleStats() },
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    WeeklyStatsSheet(
                        dates = state.datesOfWeek,
                        stats = state.completionStats,
                        totalCompleted = state.totalCompletedTasks,
                        totalTasks = state.totalTasks,
                        deepWorkCount = state.totalDeepWorkCount,
                        weekProgress = state.weekProgress,
                        onExport = { viewModel.exportTasksToCsv(context) }
                    )
                }
            }
        }
    }
}

@Composable
fun FocusTimerOverlay(
    taskName: String,
    remainingSeconds: Int,
    totalDurationMinutes: Int,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = totalDurationMinutes * 60f
    val progress = (remainingSeconds / totalSeconds)
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeStr = "%02d:%02d".format(minutes, seconds)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOCUSING ON",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = taskName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = onStop,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Butt
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyStatsSheet(
    dates: List<LocalDate>,
    stats: Map<LocalDate, Float>,
    totalCompleted: Int,
    totalTasks: Int,
    deepWorkCount: Int,
    weekProgress: Float,
    onExport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ZEN ANALYTICS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Advanced Metric Nodes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoNode(
                label = "EFFICIENCY",
                value = "${(weekProgress * 100).toInt()}%",
                modifier = Modifier.weight(1f)
            )
            InfoNode(
                label = "VELOCITY",
                value = "$totalCompleted/$totalTasks",
                modifier = Modifier.weight(1f)
            )
            InfoNode(
                label = "DEEP WORK",
                value = "$deepWorkCount",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Enhanced Bar Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dates.forEach { date ->
                val completion = stats[date] ?: 0f
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(completion.coerceAtLeast(0.01f))
                            .background(
                                color = if (completion >= 1f) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f + (completion * 0.5f)),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = date.dayOfWeek.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT WEEKLY ZEN LOG (CSV)", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun InfoNode(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
