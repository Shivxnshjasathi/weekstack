package com.zincstate.hepta.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
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
    
    HeptaTheme(darkTheme = state.isDarkMode, dynamicColor = true) {
        val headerShades = getHeaderShades(state.isDarkMode)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        onStop = { FocusService.stop(context) }
                    )
                }

                // 2. Task List (Hybrid LazyColumn for Scrolling + Grid look)
                if (!state.isLoading) {
                    val mondayColor = headerShades.firstOrNull() ?: MaterialTheme.colorScheme.surface
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Top Safe Area Spacer (Matches Monday's Color)
                        item {
                            Spacer(
                                Modifier
                                    .windowInsetsTopHeight(WindowInsets.statusBars)
                                    .fillMaxWidth()
                                    .background(mondayColor)
                            )
                        }

                        state.datesOfWeek.forEachIndexed { index, date ->
                            val isExpanded = state.expandedDate == date
                            val tasksForDay = state.tasksMap[date] ?: emptyList()
                            val lastUpdated = state.lastUpdatedMap[date]
                            
                            item(key = "day_${date.toEpochDay()}") {
                                val eventTag = state.dayTagsMap[date]
                                val calendarEvents = state.calendarEventsMap[date] ?: emptyList()

                                DayHeader(
                                    date = date,
                                    isExpanded = isExpanded,
                                    backgroundColor = headerShades.getOrElse(index) { MaterialTheme.colorScheme.surface },
                                    lastUpdated = lastUpdated,
                                    eventTag = eventTag,
                                    onHeaderClick = { viewModel.toggleDayExpansion(date) },
                                    modifier = Modifier.heightIn(min = if (!isExpanded) baseHeaderHeight else 0.dp)
                                ) {
                                    // Tasks and Events within this day
                                    Column {
                                        // 1. Official Calendar Events
                                        calendarEvents.filter { !it.isAllDay }.sortedBy { it.startTime }.forEach { event ->
                                            CalendarEventItem(event = event)
                                        }

                                        // 2. HEPTA Manual Tasks
                                        tasksForDay.sortedBy { it.position }.forEachIndexed { taskIndex, task ->
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
                                                    isDragging = isDragging
                                                )
                                            }
                                        }

                                        AddTaskInput(
                                            onAddTask = { text -> viewModel.addTask(text, date) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        item(key = "nav_spacer") {
                            Spacer(modifier = Modifier.statusBarsPadding().navigationBarsPadding().height(80.dp))
                        }
                    }
                }
            }

            // 3. Bottom Actions Dock (Fixed)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .zIndex(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        imageVector = if (state.isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = stringResource(R.string.toggle_theme),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleStats() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Stats",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 4. Weekly Stats sheet
            if (state.showStats) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleStats() },
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) },
                    containerColor = if (state.isDarkMode) Color.Black else Color.White
                ) {
                    WeeklyStatsSheet(
                        dates = state.datesOfWeek,
                        stats = state.completionStats,
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
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = 25 * 60f
    val progress = 1f - (remainingSeconds / totalSeconds)
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeStr = "%02d:%02d".format(minutes, seconds)

    Surface(
        color = Color.Black,
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
                        color = Color.Gray
                    )
                    Text(
                        text = taskName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = onStop,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White
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
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.1f),
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
            text = "PRODUCTIVITY",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Bar Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
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
                            .width(12.dp)
                            .fillMaxHeight(completion.coerceAtLeast(0.01f))
                            .background(
                                color = if (completion >= 1f) MaterialTheme.colorScheme.onBackground 
                                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = date.dayOfWeek.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT WEEK LOG (CSV)")
        }
    }
}
