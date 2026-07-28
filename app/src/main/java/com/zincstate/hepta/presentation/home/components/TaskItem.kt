package com.zincstate.hepta.presentation.home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.ui.theme.*

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit,
    onFocus: () -> Unit,
    onToggleRecurring: () -> Unit,
    onShiftToInbox: () -> Unit,
    onShiftToTomorrow: () -> Unit = {},
    onUpdateNotes: (String?) -> Unit = {},
    onUpdateSubtasks: (List<com.zincstate.hepta.domain.model.Subtask>) -> Unit = {},
    onSetReminder: (Long) -> Unit = {},
    selectedFocusDuration: Int = 25,
    onCycleFocusDuration: () -> Unit = {},
    modifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(task.text) { mutableStateOf(task.text) }
    var notesValue by remember(task.notes) { mutableStateOf(task.notes ?: "") }
    var isExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                    // Reset the swipe state to center after action
                    false 
                }
                else -> false
            }
        }
    )

    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dragScale"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = true,
        modifier = modifier
            .testTag("task_item_${task.id}")
            .scale(dragScale)
            .zIndex(if (isDragging) 1f else 0f),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935) // A more vibrant Material Red
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    else -> Color.Transparent
                },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "swipeColor"
            )
            
            val icon = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                else -> null
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 28.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.scale(animateFloatAsState(
                            targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.3f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "iconScale"
                        ).value)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Focus Indicator / Checkbox
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle() 
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (task.recurringType > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Column(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = textValue,
                        onValueChange = { 
                            textValue = it
                            isEditing = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            fontWeight = if (task.isFocusCompleted && task.isCompleted) FontWeight.Bold else FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onUpdate(textValue)
                            isEditing = false
                            focusManager.clearFocus()
                        }),
                        decorationBox = { innerTextField ->
                            if (textValue.isEmpty()) {
                                Text(
                                    text = "Task name",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            innerTextField()
                        }
                    )
                    
                    if (task.isFocusCompleted && task.isCompleted) {
                        Text(
                            text = "FOCUS SESSION WORK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    }
                    
                    val hasNotes = !task.notes.isNullOrBlank()
                    val hasSubtasks = task.subtasks.isNotEmpty()
                    if ((hasNotes || hasSubtasks) && !isExpanded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            if (hasNotes) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Has Notes",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(12.dp)
                                )
                                if (hasSubtasks) Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (hasSubtasks) {
                                val completedCount = task.subtasks.count { it.isCompleted }
                                Text(
                                    text = "$completedCount/${task.subtasks.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                // Removed expand chevron here
                // Action Controls
                var expanded by remember { mutableStateOf(false) }
                val context = LocalContext.current
                
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isExpanded) "Collapse Notes" else "Expand Notes", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                isExpanded = !isExpanded
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Focus ($selectedFocusDuration m)", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                expanded = false
                                onFocus()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer, 
                                    contentDescription = null,
                                    tint = if (task.isFocusCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Change Duration", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                onCycleFocusDuration()
                                expanded = false 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(if (task.reminderTime != null) "Update Reminder" else "Set Reminder", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                expanded = false
                                val calendar = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(context, { _, hour, minute ->
                                    val reminderCalendar = java.util.Calendar.getInstance().apply {
                                        if (task.targetDate != java.time.LocalDate.MAX) {
                                            set(java.util.Calendar.YEAR, task.targetDate.year)
                                            set(java.util.Calendar.MONTH, task.targetDate.monthValue - 1)
                                            set(java.util.Calendar.DAY_OF_MONTH, task.targetDate.dayOfMonth)
                                        }
                                        set(java.util.Calendar.HOUR_OF_DAY, hour)
                                        set(java.util.Calendar.MINUTE, minute)
                                        set(java.util.Calendar.SECOND, 0)
                                    }
                                    onSetReminder(reminderCalendar.timeInMillis)
                                }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), false).show()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (task.reminderTime != null) Icons.Default.AccessTime else Icons.Default.NotificationsNone, 
                                    contentDescription = null,
                                    tint = if (task.reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        
                        if (task.targetDate != java.time.LocalDate.MAX) {
                            DropdownMenuItem(
                                text = { Text("Move to Inbox", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    expanded = false
                                    onShiftToInbox()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Inbox, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                        
                        if (task.targetDate != java.time.LocalDate.MAX) {
                            DropdownMenuItem(
                                text = { Text("Shift to Tomorrow", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    expanded = false
                                    onShiftToTomorrow()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text(if (task.recurringType > 0) "Disable Recurring" else "Make Recurring", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { 
                                expanded = false
                                onToggleRecurring()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (task.recurringType > 0) Icons.Default.Sync else Icons.Default.SyncDisabled,
                                    contentDescription = null,
                                    tint = if (task.recurringType > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            }

            // Expanded Content (Notes & Subtasks)
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                ) {
                    // Notes Section
                    OutlinedTextField(
                        value = notesValue,
                        onValueChange = { 
                            notesValue = it
                            onUpdateNotes(it.takeIf { s -> s.isNotBlank() })
                        },
                        placeholder = { Text("Add notes...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                        minLines = 2,
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Subtasks Section
                    Text(
                        text = "SUBTASKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    task.subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { isChecked ->
                                    val updatedSubtasks = task.subtasks.map { 
                                        if (it.id == subtask.id) it.copy(isCompleted = isChecked) else it 
                                    }
                                    onUpdateSubtasks(updatedSubtasks)
                                },
                                modifier = Modifier.size(24.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    checkmarkColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = subtask.text,
                                onValueChange = { newText ->
                                    val updatedSubtasks = task.subtasks.map { 
                                        if (it.id == subtask.id) it.copy(text = newText) else it 
                                    }
                                    onUpdateSubtasks(updatedSubtasks)
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                            )
                            IconButton(
                                onClick = { 
                                    val updatedSubtasks = task.subtasks.filter { it.id != subtask.id }
                                    onUpdateSubtasks(updatedSubtasks)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Subtask",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    // Add Subtask Button
                    TextButton(
                        onClick = { 
                            val newSubtasks = task.subtasks + com.zincstate.hepta.domain.model.Subtask(text = "")
                            onUpdateSubtasks(newSubtasks)
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subtask",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add subtask",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
