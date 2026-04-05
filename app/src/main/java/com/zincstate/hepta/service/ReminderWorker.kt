package com.zincstate.hepta.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zincstate.hepta.domain.usecase.TaskUseCases
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.first

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.annotation.SuppressLint
import com.zincstate.hepta.R

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val useCases: TaskUseCases
) : CoroutineWorker(context, workerParams) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        
        // 1. Handle Recurring Tasks Reset
        val allTasks = useCases.getTasksForWeek(today.minusDays(7), today.plusDays(1)).first()
        val tasksToReset = allTasks.filter { task ->
            task.recurringType > 0 && task.targetDate.isBefore(today)
        }

        if (tasksToReset.isNotEmpty()) {
            val updatedTasks = tasksToReset.map { task ->
                task.copy(
                    isCompleted = false,
                    targetDate = today,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            useCases.upsertTasks(updatedTasks)
        }

        // 2. Post Daily Summary Notification
        val todayTasks = useCases.getTasksForWeek(today, today).first()
        val pendingCount = todayTasks.count { !it.isCompleted }

        if (pendingCount > 0) {
            val notification = NotificationCompat.Builder(context, "daily_reminders")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Your Daily Plan")
                .setContentText("You have $pendingCount tasks to focus on today.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(context)) {
                notify(1, notification)
            }
        }

        return Result.success()
    }
}
