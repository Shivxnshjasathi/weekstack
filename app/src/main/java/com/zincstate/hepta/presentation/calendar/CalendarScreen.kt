package com.zincstate.hepta.presentation.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.zincstate.hepta.domain.model.Milestone
import com.zincstate.hepta.domain.model.Task
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    dates: List<LocalDate>,
    tasksMap: Map<LocalDate, List<Task>>,
    milestones: List<Milestone> = emptyList(),
    selectedMonth: YearMonth = YearMonth.now(),
    onMonthChange: (YearMonth) -> Unit = {},
    onAddMilestone: (String) -> Unit = {},
    onToggleMilestone: (Milestone) -> Unit = {},
    onDeleteMilestone: (Milestone) -> Unit = {}
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val daysInMonth = remember(selectedMonth) {
        val firstDay = selectedMonth.atDay(1)
        val lastDay = selectedMonth.atEndOfMonth()
        val days = mutableListOf<LocalDate?>()
        
        // Add padding for the start of the week (assuming Monday start like the rest of the app)
        val firstDayOfWeek = firstDay.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        repeat(firstDayOfWeek - 1) {
            days.add(null)
        }
        
        for (i in 1..selectedMonth.lengthOfMonth()) {
            days.add(selectedMonth.atDay(i))
        }
        days
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedMonth) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (dragAmount > 50) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onMonthChange(selectedMonth.minusMonths(1))
                        change.consume()
                    } else if (dragAmount < -50) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onMonthChange(selectedMonth.plusMonths(1))
                        change.consume()
                    }
                }
            },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "STELLAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} ${selectedMonth.year}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
                    }
                    IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysInMonth) { date ->
                    if (date != null) {
                        val tasks = tasksMap[date] ?: emptyList()
                        val isToday = date == LocalDate.now()
                        val isSelected = date == selectedDate
                        
                        CalendarDayNode(
                            date = date,
                            taskCount = tasks.size,
                            isToday = isToday,
                            isSelected = isSelected,
                            onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                selectedDate = if (selectedDate == date) null else date 
                            }
                        )
                    } else {
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Dynamic Bottom Content: Either Selected Day Tasks OR Monthly Milestones
            if (selectedDate != null) {
                // DAY DETAILS VIEW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAY DETAILS: ${selectedDate?.dayOfMonth} ${selectedDate?.month?.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedDate = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val selectedTasks = tasksMap[selectedDate] ?: emptyList()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selectedTasks.forEach { task ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f) 
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                           Row(
                               modifier = Modifier.padding(16.dp),
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               Box(
                                   modifier = Modifier
                                       .size(6.dp)
                                       .background(
                                           color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                                   else MaterialTheme.colorScheme.primary,
                                           shape = CircleShape
                                       )
                               )
                               Spacer(modifier = Modifier.width(16.dp))
                               Text(
                                   text = task.text,
                                   style = MaterialTheme.typography.bodySmall,
                                   color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                           else MaterialTheme.colorScheme.onSurface,
                                   textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                               )
                           }
                        }
                    }
                    
                    if (selectedTasks.isEmpty()) {
                        Text(
                            text = "NO INTENTIONS SET FOR THIS DAY.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            } else {
                // MONTHLY MILESTONES VIEW (Default)
                Text(
                    text = "MONTHLY MILESTONES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    milestones.forEach { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            onToggle = { onToggleMilestone(milestone) },
                            onDelete = { onDeleteMilestone(milestone) }
                        )
                    }
                    
                    if (milestones.isEmpty()) {
                        Text(
                            text = "No milestones set for this month. Focus on big themes here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
            
            // Milestone Input field
            var newMilestoneText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newMilestoneText,
                onValueChange = { newMilestoneText = it },
                placeholder = { 
                    Text(
                        "ADD STELLAR GOAL...", 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (newMilestoneText.isNotEmpty()) {
                        IconButton(onClick = {
                            onAddMilestone(newMilestoneText)
                            newMilestoneText = ""
                        }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Modern Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACTIVITY GLOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDayNode(
    date: LocalDate,
    taskCount: Int,
    isToday: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val alpha = when {
        taskCount == 0 -> 0.05f
        taskCount < 3 -> 0.2f
        taskCount < 6 -> 0.4f
        else -> 0.6f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = if (isToday) MaterialTheme.colorScheme.primary 
                            else if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Task Density Glow
            if (taskCount > 0 && !isToday) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                            shape = CircleShape
                        )
                        .blur(8.dp)
                )
            }
            
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.background 
                        else if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MilestoneItem(
    milestone: Milestone,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    color = if (milestone.isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = milestone.text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (milestone.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
