package com.zincstate.hepta.presentation.home.components

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.zincstate.hepta.R
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
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
            .testTag("day_header_${date.dayOfWeek.name}")
            .background(backgroundColor)
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onHeaderClick() 
            }
    ) {
        // Expandable Header (Taller & Bolder)
        val textColor = if (backgroundColor.red > 0.5f && backgroundColor.green > 0.5f && backgroundColor.blue > 0.5f) Color.Black else Color.White
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(
                    top = if (isFirstItem) 48.dp else (if (isExpanded) 32.dp else 24.dp),
                    bottom = if (isExpanded) 32.dp else 24.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = date.format(dayOfWeekFormatter).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = textColor
                )
                if (isExpanded) {
                    Text(
                        text = date.format(fullDateFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
