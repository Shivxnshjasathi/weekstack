package com.zincstate.hepta.data.mapper

import com.zincstate.hepta.data.local.TaskEntity
import com.zincstate.hepta.domain.model.Task
import com.zincstate.hepta.domain.model.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

private val gson = Gson()

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
        reminderTime = reminderTime,
        notes = notes,
        subtasks = try {
            val listType = object : TypeToken<List<Subtask>>() {}.type
            gson.fromJson(subtasks, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
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
        reminderTime = reminderTime,
        notes = notes,
        subtasks = gson.toJson(subtasks)
    )
}
