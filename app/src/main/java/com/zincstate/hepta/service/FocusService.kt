package com.zincstate.hepta.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zincstate.hepta.MainActivity
import com.zincstate.hepta.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FocusService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    companion object {
        private val _remainingTime = MutableStateFlow(0)
        val remainingTime = _remainingTime.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning = _isRunning.asStateFlow()

        private val _currentTaskName = MutableStateFlow("")
        val currentTaskName = _currentTaskName.asStateFlow()

        const val CHANNEL_ID = "focus_session"
        const val NOTIFICATION_ID = 2

        fun start(context: Context, taskName: String, durationMillis: Long = 25 * 60 * 1000L) {
            val intent = Intent(context, FocusService::class.java).apply {
                putExtra("task_name", taskName)
                putExtra("duration", durationMillis)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusService::class.java)
            context.stopService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskName = intent?.getStringExtra("task_name") ?: "Focus Session"
        val duration = intent?.getLongExtra("duration", 25 * 60 * 1000L) ?: (25 * 60 * 1000L)

        _currentTaskName.value = taskName
        _isRunning.value = true

        val notification = createNotification(taskName, formatTime(duration / 1000))
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        startTimer(duration)

        return START_NOT_STICKY
    }

    private fun startTimer(duration: Long) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var timeLeft = duration / 1000
            while (timeLeft >= 0) {
                _remainingTime.value = timeLeft.toInt()
                updateNotification(_currentTaskName.value, formatTime(timeLeft))
                delay(1000)
                timeLeft--
            }
            _isRunning.value = false
            stopSelf()
        }
    }

    private fun updateNotification(taskName: String, timeStr: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(taskName, timeStr))
    }

    private fun createNotification(taskName: String, timeStr: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focusing: $taskName")
            .setContentText("Remaining: $timeStr")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        _isRunning.value = false
        serviceScope.cancel()
    }
}
