package com.zincstate.weekstack.data.mapper

import com.zincstate.weekstack.data.local.TaskEntity
import com.zincstate.weekstack.domain.model.Task
import java.time.LocalDate

fun TaskEntity.toDomainTask(): Task {
    return Task(
        id = id,
        text = text,
        isCompleted = isCompleted,
        targetDate = LocalDate.ofEpochDay(targetDateEpochDays),
        lastUpdated = lastUpdated
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        text = text,
        isCompleted = isCompleted,
        targetDateEpochDays = targetDate.toEpochDay(),
        lastUpdated = lastUpdated
    )
}
