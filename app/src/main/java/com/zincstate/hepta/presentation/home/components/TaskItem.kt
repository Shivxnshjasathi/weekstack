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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
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

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        modifier = modifier.testTag("task_item_${task.id}"),
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 
                    Color(0xFF8B0000) else Color.Transparent,
                label = "swipeColor"
            )
            val scale by animateFloatAsState(
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
                    modifier = Modifier.scale(scale)
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val checkboxColor = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
            if (!isEditing || textValue.isNotBlank()) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("task_checkbox_${task.id}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.tertiary,
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
                Text(
                    text = task.text,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEditing = true }
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = textDecoration
                    ),
                    color = textColor
                )
            }
        }
    }
}
