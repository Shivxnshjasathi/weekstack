package com.zincstate.hepta.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.zincstate.hepta.domain.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetCalendarEventsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val contentResolver: ContentResolver = context.contentResolver
        
        // Convert dates to milliseconds
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, startMillis)
        android.content.ContentUris.appendId(builder, endMillis)
        
        val uri = builder.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            CalendarContract.Instances.BEGIN + " ASC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleCol = it.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginCol = it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endCol = it.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayCol = it.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val descCol = it.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            val locCol = it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            val calNameCol = it.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)

            while (it.moveToNext()) {
                val begin = it.getLong(beginCol)
                val eventDate = Instant.ofEpochMilli(begin).atZone(ZoneId.systemDefault()).toLocalDate()
                
                events.add(
                    CalendarEvent(
                        id = it.getLong(idCol),
                        title = it.getString(titleCol) ?: "Untitled",
                        startTime = begin,
                        endTime = it.getLong(endCol),
                        isAllDay = it.getInt(allDayCol) == 1,
                        description = it.getString(descCol),
                        location = it.getString(locCol),
                        calendarName = it.getString(calNameCol),
                        date = eventDate
                    )
                )
            }
        }
        
        return events
    }
}
