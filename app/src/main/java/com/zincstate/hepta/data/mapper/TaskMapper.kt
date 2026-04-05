package com.zincstate.hepta.data.mapper

import com.zincstate.hepta.data.local.TaskEntity
import com.zincstate.hepta.domain.model.Task
import java.time.LocalDate

fun TaskEntity.toDomainTask(): Task {
    return Task(
        id = id,
        text = text,
        isCompleted = isCompleted,
        targetDate = LocalDate.ofEpochDay(targetDateEpochDays),
        lastUpdated = lastUpdated,
        position = position,
        recurringType = recurringType,
        isFocusCompleted = isFocusCompleted,
        isMorningIntention = isMorningIntention,
        reminderTime = reminderTime
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        text = text,
        isCompleted = isCompleted,
        targetDateEpochDays = targetDate.toEpochDay(),
        lastUpdated = lastUpdated,
        position = position,
        recurringType = recurringType,
        isFocusCompleted = isFocusCompleted,
        isMorningIntention = isMorningIntention,
        reminderTime = reminderTime
    )
}
