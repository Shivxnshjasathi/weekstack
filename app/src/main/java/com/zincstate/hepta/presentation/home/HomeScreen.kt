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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val listState = rememberLazyListState()
    var draggingTaskId by remember { mutableStateOf<Int?>(null) }
    var hackerClickCount by remember { mutableStateOf(0) }
    
    // Permission handling
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.READ_CALENDAR] == true) {
            viewModel.updatePermissionStatus()
        }
    }
    
    // Check permission on start
    LaunchedEffect(Unit) {
        val perms = mutableListOf(android.Manifest.permission.READ_CALENDAR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (!state.hasCalendarPermission) {
            permissionLauncher.launch(perms.toTypedArray())
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
        val configuration = LocalConfiguration.current
        val totalHeight = configuration.screenHeightDp.dp
        // Calculate a base height that fills 1/7th of the screen (adjusting for spacers)
        val baseHeaderHeight = (totalHeight - 80.dp) / 7.2f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    val edgeThreshold = 30.dp.toPx()
                    var startX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { startX = it.x },
                        onHorizontalDrag = { change, dragAmount ->
                            // Only trigger edge swipe if the interaction started near the edge
                            if (startX < edgeThreshold) {
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
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Persistent Focus Header (Sticky)
                AnimatedVisibility(
                    visible = isTimerRunning,
                    enter = slideInVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut()
                ) {
                    FocusTimerOverlay(
                        taskName = currentTaskName,
                        remainingSeconds = remainingSeconds,
                        totalDurationMinutes = state.selectedFocusDuration,
                        onStop = { FocusService.stop(context) }
                    )
                }

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
                                            .zIndex(if (isDragging) 1f else 0f)
                                            .pointerInput(task.id) {
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
                                                            if (targetItem.key is Int) {
                                                                val targetTaskId = targetItem.key as Int
                                                                if (targetTaskId != task.id) {
                                                                    val sortedTasks = tasksForDay.sortedBy { it.position }
                                                                    val targetIndex = sortedTasks.indexOfFirst { it.id == targetTaskId }
                                                                    if (targetIndex != -1) {
                                                                        viewModel.onMoveTask(date, taskIndex, targetIndex)
                                                                        accumulatedOffset = 0f
                                                                    }
                                                                }
                                                            } else {
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
                                                    }
                                                )
                                            }
                                    ) {
                                        TaskItem(
                                            task = task,
                                            lastUpdated = task.lastUpdated,
                                            isCompleted = task.isCompleted,
                                            onToggle = { viewModel.toggleTask(task) },
                                            onUpdate = { newText -> viewModel.updateTaskText(task, newText) },
                                            onDelete = { viewModel.deleteTask(task) },
                                            onFocus = { viewModel.startFocusSession(context, task) },
                                            onToggleRecurring = { viewModel.toggleTaskRecurrence(task) },
                                            onShiftToInbox = { viewModel.shiftToInbox(task) },
                                            onShiftToTomorrow = { viewModel.shiftTaskToTomorrow(task) },
                                            onUpdateNotes = { notes -> viewModel.updateTaskNotes(task, notes) },
                                            onUpdateSubtasks = { subtasks -> viewModel.updateTaskSubtasks(task, subtasks) },
                                            onSetReminder = { time -> viewModel.setTaskReminder(task, time) },
                                            selectedFocusDuration = state.selectedFocusDuration,
                                            onCycleFocusDuration = { viewModel.cycleFocusDuration() },
                                            isDragging = isDragging
                                        )
                                    }
                                }

                                // 2d. Spacer to prevent touch overlap
                                item(key = "spacer_${date.toEpochDay()}") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // 2e. Add Input
                                item(key = "add_${date.toEpochDay()}") {
                                    AddTaskInput(
                                        onAddTask = { text -> viewModel.addTask(text, date) }
                                    )
                                }
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
                                    lastUpdated = task.lastUpdated,
                                    isCompleted = task.isCompleted,
                                    onToggle = { viewModel.toggleTask(task) },
                                    onUpdate = { viewModel.updateTaskText(task, it) },
                                    onDelete = { viewModel.deleteTask(task) },
                                    onFocus = { viewModel.startFocusSession(context, task) },
                                    onToggleRecurring = { viewModel.toggleTaskRecurrence(task) },
                                    onShiftToInbox = { viewModel.shiftToInbox(task) },
                                    onShiftToTomorrow = { viewModel.shiftTaskToTomorrow(task) },
                                    onUpdateNotes = { notes -> viewModel.updateTaskNotes(task, notes) },
                                    onUpdateSubtasks = { subtasks -> viewModel.updateTaskSubtasks(task, subtasks) },
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
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            val clickScale by animateFloatAsState(
                targetValue = if (isPressed) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "nexus_click_scale"
            )

            val infiniteTransition = rememberInfiniteTransition(label = "nexus_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
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
                        .scale(pulseScale * clickScale)
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary, // The accent color of the theme
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                hackerClickCount++
                                if (hackerClickCount >= 7) {
                                    hackerClickCount = 0
                                    viewModel.onThemeChange(com.zincstate.hepta.ui.theme.ZenTheme.HACKER)
                                    android.widget.Toast.makeText(context, "TERMINAL UNLOCKED", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    onNavigateToNotes()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "H",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.background,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .clickable { 
                            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                            viewModel.toggleStats() 
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Zen Analytics",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 4. Weekly Stats sheet
            if (state.showStats) {
                val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleStats() },
                    sheetState = sheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    WeeklyStatsSheet(
                        dates = state.datesOfWeek,
                        stats = state.completionStats,
                        totalCompleted = state.totalCompletedTasks,
                        totalTasks = state.totalTasks,
                        deepWorkCount = state.totalDeepWorkCount,
                        focusDuration = state.selectedFocusDuration,
                        weekProgress = state.weekProgress,
                        onExport = { viewModel.exportTasksToCsv(context) },
                        onClose = { viewModel.toggleStats() }
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

    val noiseGenerator = remember { com.zincstate.hepta.util.PinkNoiseGenerator() }
    var isNoisePlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            noiseGenerator.stop()
        }
    }

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
                    onClick = {
                        noiseGenerator.toggle()
                        isNoisePlaying = noiseGenerator.isPlaying()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isNoisePlaying) 0.3f else 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isNoisePlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Soundscape",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        noiseGenerator.stop()
                        onStop()
                    },
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
    focusDuration: Int,
    weekProgress: Float,
    onExport: () -> Unit,
    onClose: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEEKLY INSIGHTS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = onSurfaceColor
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Close",
                    tint = mutedColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top section (Donut + 2 Stat Cards)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Card (Donut Chart)
            Surface(
                modifier = Modifier.weight(1.1f).fillMaxHeight(),
                color = surfaceColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Donut Chart
                        val progressAnim by animateFloatAsState(
                            targetValue = weekProgress,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "donut_progress"
                        )
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(90.dp)) {
                            val strokeWidth = 10.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color.White,
                                startAngle = -90f,
                                sweepAngle = progressAnim * 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(weekProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "DONE",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontSize = 9.sp),
                                color = mutedColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = onSurfaceColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$totalCompleted Completed", style = MaterialTheme.typography.bodySmall, color = onSurfaceColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = mutedColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${totalTasks - totalCompleted} Left", style = MaterialTheme.typography.bodySmall, color = mutedColor)
                    }
                }
            }
            
            // Right Section (2 Cards)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Focus Time Card
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = onSurfaceColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val hours = (deepWorkCount * focusDuration) / 60
                                val mins = (deepWorkCount * focusDuration) % 60
                                Text(
                                    text = "${hours}h ${mins}m",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "FOCUS TIME",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 10.sp),
                                    color = mutedColor
                                )
                            }
                        }
                    }
                }
                
                // Total Tasks Card
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = onSurfaceColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$totalTasks",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "TOTAL TASKS",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 10.sp),
                                    color = mutedColor
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bottom Bar Chart
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = surfaceColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ACTIVITY THIS WEEK",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = mutedColor
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    var showBars by remember { androidx.compose.runtime.mutableStateOf(false) }
                    LaunchedEffect(Unit) { showBars = true }
                    
                    dates.forEach { date ->
                        val targetCompletion = if (showBars) (stats[date] ?: 0f) else 0f
                        val completion by animateFloatAsState(
                            targetValue = targetCompletion,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "bar_anim"
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .weight(1f)
                                    .background(trackColor, CircleShape),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                val heightFrac = completion.coerceIn(0f, 1f)
                                if (heightFrac > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(heightFrac)
                                            .background(Color(0xFFB0BCC2), CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = date.dayOfWeek.name.take(1),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = onSurfaceColor
                            )
                        }
                    }
                }
            }
        }
    }
}
