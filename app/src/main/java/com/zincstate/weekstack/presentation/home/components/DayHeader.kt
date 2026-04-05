package com.zincstate.weekstack.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement

@Composable
fun DayHeader(
    date: LocalDate,
    isExpanded: Boolean,
    backgroundColor: Color,
    lastUpdated: Long?,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
    val fullDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    val lastUpdatedText = lastUpdated?.let {
        val instant = Instant.ofEpochMilli(it)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        zonedDateTime.format(timeFormatter)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isExpanded) MaterialTheme.colorScheme.surface else backgroundColor)
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onHeaderClick() 
            }
    ) {
        // Expandable Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isFirstItem) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = 24.dp, vertical = if (isExpanded) 20.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = date.format(dayOfWeekFormatter).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isExpanded) MaterialTheme.colorScheme.onBackground else Color.White.copy(alpha = 0.8f)
                )
                if (isExpanded) {
                    Text(
                        text = date.format(fullDateFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (lastUpdatedText != null) {
                        Text(
                            text = "Edited $lastUpdatedText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Expanded Content with Animation
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                content()
            }
        }
    }
}
