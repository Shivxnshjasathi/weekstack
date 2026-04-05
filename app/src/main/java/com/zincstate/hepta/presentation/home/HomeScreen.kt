package com.zincstate.hepta.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.zincstate.hepta.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.zincstate.hepta.presentation.home.components.AddTaskInput
import com.zincstate.hepta.presentation.home.components.DayHeader
import com.zincstate.hepta.presentation.home.components.TaskItem
import com.zincstate.hepta.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    HeptaTheme(darkTheme = state.isDarkMode) {
        val headerShades = getHeaderShades(state.isDarkMode)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Theme Toggle Button - Bottom Center
            IconButton(
                onClick = { viewModel.toggleTheme() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = if (state.isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
                    contentDescription = stringResource(R.string.toggle_theme),
                    tint = if (state.isDarkMode) Color.White else Color.Black
                )
            }

            if (!state.isLoading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Status Bar Spacer (Matches Monday's color)
                    item {
                        val mondayColor = headerShades.firstOrNull() ?: MaterialTheme.colorScheme.surface
                        Spacer(
                            Modifier
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .fillMaxWidth()
                                .background(mondayColor)
                        )
                    }

                    // 2. The 7 Days
                    itemsIndexed(
                        items = state.datesOfWeek,
                        key = { _, date -> date.toEpochDay() } // Stable keys for performance
                    ) { index, date ->
                        val isExpanded = state.expandedDate == date
                        val tasksForDay = state.tasksMap[date] ?: emptyList()
                        val lastUpdated = state.lastUpdatedMap[date]
                        val bgColor = headerShades.getOrElse(index) { MaterialTheme.colorScheme.surface }

                        DayHeader(
                            date = date,
                            isExpanded = isExpanded,
                            backgroundColor = bgColor,
                            lastUpdated = lastUpdated,
                            onHeaderClick = { viewModel.toggleDayExpansion(date) },
                            isFirstItem = index == 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isExpanded) {
                                        Modifier.heightIn(min = 250.dp)
                                    } else {
                                        // Precise 1/7th height for all remaining screen portions
                                        Modifier.fillParentMaxHeight(1f / 7f)
                                    }
                                )
                        ) {
                            // Expandable content
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                tasksForDay.forEach { task ->
                                    key(task.id) { // Precise task tracking
                                        TaskItem(
                                            task = task,
                                            onToggle = { viewModel.toggleTask(task) },
                                            onUpdate = { newText -> viewModel.updateTaskText(task, newText) },
                                            onDelete = { viewModel.deleteTask(task) }
                                        )
                                    }
                                }
                                
                                key("add_task_$date") {
                                    AddTaskInput(
                                        onAddTask = { text -> viewModel.addTask(text, date) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


