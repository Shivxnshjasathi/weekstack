package com.zincstate.hepta.presentation.notes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zincstate.hepta.data.local.NoteDao
import com.zincstate.hepta.data.local.NoteEntity
import com.zincstate.hepta.domain.model.Task
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(FlowPreview::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    noteDao: NoteDao,
    todayTasks: List<Task> = emptyList(),
    onToggleTask: (Task) -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    val dateFormatted = remember(today) {
        today.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
    }

    var selectedCategory by remember { mutableStateOf("work") }

    // Load existing note for today + selected category
    val noteEntity by noteDao.getNoteForDateAndCategory(
        today.toEpochDay(), selectedCategory
    ).collectAsState(initial = null)

    var noteText by remember(noteEntity?.id, selectedCategory) {
        mutableStateOf(noteEntity?.content ?: "")
    }

    // Sync when noteEntity loads from DB (first load)
    LaunchedEffect(noteEntity) {
        if (noteEntity != null && noteText.isEmpty()) {
            noteText = noteEntity?.content ?: ""
        }
    }

    // Auto-save with debounce
    val textState = remember { mutableStateOf("") }
    LaunchedEffect(noteText) {
        textState.value = noteText
    }
    LaunchedEffect(textState) {
        snapshotFlow { textState.value }
            .debounce(500)
            .collect { text ->
                val existing = noteEntity
                if (existing != null) {
                    noteDao.upsertNote(existing.copy(content = text, lastUpdated = System.currentTimeMillis()))
                } else if (text.isNotBlank()) {
                    noteDao.upsertNote(
                        NoteEntity(
                            dateEpochDays = today.toEpochDay(),
                            category = selectedCategory,
                            content = text,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onNavigateToCalendar) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Date Header
            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryPill(
                    label = "Work",
                    isSelected = selectedCategory == "work",
                    onClick = { selectedCategory = "work" }
                )
                CategoryPill(
                    label = "Personal",
                    isSelected = selectedCategory == "personal",
                    onClick = { selectedCategory = "personal" }
                )
            }

            // Notes Text Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Tasks Section
            if (todayTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "TASKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(
                        items = todayTasks,
                        key = { "note_task_${it.id}" }
                    ) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                .clickable { onToggleTask(task) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTask(task) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = task.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (task.isCompleted)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.onSurface
        else
            Color.Transparent,
        border = if (!isSelected)
            ButtonDefaults.outlinedButtonBorder(enabled = true)
        else
            null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected)
                MaterialTheme.colorScheme.background
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
