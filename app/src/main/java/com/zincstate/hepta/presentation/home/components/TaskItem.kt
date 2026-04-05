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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val checkboxColor = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
            if (!isEditing || textValue.isNotBlank()) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle() 
                    },
                    modifier = Modifier.testTag("task_checkbox_${task.id}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (task.recurringType > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        uncheckedColor = checkboxColor,
                        checkmarkColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            if (isEditing) {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            isEditing = false
                            if (textValue.isBlank()) {
                                onDelete()
                            } else {
                                onUpdate(textValue)
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                val textColor = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEditing = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.text,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = textDecoration
                        ),
                        color = textColor
                    )

                    if (!task.isCompleted) {
                        IconButton(
                            onClick = onFocus,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Focus",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onToggleRecurring,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (task.recurringType > 0) Icons.Default.Sync else Icons.Default.SyncDisabled,
                                contentDescription = "Recurring",
                                tint = if (task.recurringType > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
