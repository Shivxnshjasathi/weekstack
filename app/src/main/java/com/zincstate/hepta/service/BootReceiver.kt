package com.zincstate.hepta.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.zincstate.hepta.domain.usecase.TaskUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.app.AlarmManager
import android.app.PendingIntent
import com.zincstate.hepta.presentation.home.ReminderReceiver

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var useCases: TaskUseCases

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            scheduleDailyReminder(context)
            rescheduleTaskAlarms(context)
        }
    }

    private fun scheduleDailyReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelayTo8AM(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelayTo8AM(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - now
    }

    private fun rescheduleTaskAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        CoroutineScope(Dispatchers.IO).launch {
            val today = java.time.LocalDate.now()
            val tasks = useCases.getTasksForWeek(today, today.plusDays(7)).first()
            val now = System.currentTimeMillis()
            
            tasks.forEach { task ->
                if (task.reminderTime != null && task.reminderTime > now && !task.isCompleted) {
                    val intent = Intent(context, ReminderReceiver::class.java).apply {
                        putExtra("task_id", task.id)
                        putExtra("task_text", task.text)
                    }
                    
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        task.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                task.reminderTime,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                task.reminderTime,
                                pendingIntent
                            )
                        }
                    } catch (e: Exception) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            task.reminderTime,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }
}
