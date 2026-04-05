package com.zincstate.hepta.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val location: String?,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val calendarName: String?,
    val date: LocalDate
)
