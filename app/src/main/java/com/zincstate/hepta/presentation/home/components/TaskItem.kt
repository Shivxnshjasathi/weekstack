package com.zincstate.hepta.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Reorder
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.ui.zIndex

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
    modifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(task.text) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        }
    )

    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        label = "dragScale"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = modifier
            .testTag("task_item_${task.id}")
            .scale(dragScale)
            .zIndex(if (isDragging) 1f else 0f),
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 
                    Color(0xFF8B0000) else Color.Transparent,
                label = "swipeColor"
            )
            val iconScale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 1f,
                label = "iconScale"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.scale(iconScale)
                )
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
                }

                // Action Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onFocus) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Focus",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (task.targetDate != java.time.LocalDate.MAX) {
                        IconButton(onClick = onShiftToInbox) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "To Inbox",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onToggleRecurring) {
                        Icon(
                            imageVector = if (task.recurringType > 0) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            contentDescription = "Recurring",
                            tint = if (task.recurringType > 0) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
