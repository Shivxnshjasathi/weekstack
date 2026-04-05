package com.zincstate.hepta

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.zincstate.hepta.presentation.home.HomeScreen
import com.zincstate.hepta.ui.theme.HeptaTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zincstate.hepta.service.ReminderWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar

import com.zincstate.hepta.presentation.about.AboutScreen
import com.zincstate.hepta.presentation.calendar.CalendarScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.Crossfade
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.zincstate.hepta.presentation.calendar.CalendarScreen
import com.zincstate.hepta.util.BiometricHelper

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var needsAuth = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        installSplashScreen()
        enableEdgeToEdge()
        scheduleDailyReminder()
        
        setContent {
            val viewModel: com.zincstate.hepta.presentation.home.HomeViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            HeptaTheme(
                zenTheme = state.currentZenTheme,
                customColor = state.customThemeColor
            ) {
                // Vault Gate: If enabled & not authenticated, show lock screen
                if (state.isVaultEnabled && !state.isVaultAuthenticated) {
                    VaultScreen(
                        onUnlockRequest = {
                            BiometricHelper.authenticate(
                                activity = this@MainActivity,
                                onSuccess = { viewModel.setVaultAuthenticated(true) },
                                onError = { /* Stay locked */ }
                            )
                        }
                    )
                } else {
                    var currentScreen by remember { mutableStateOf("home") }

                    Crossfade(targetState = currentScreen, label = "screen_nav") { screen ->
                        when (screen) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAbout = { currentScreen = "about" },
                                onNavigateToCalendar = { currentScreen = "calendar" }
                            )
                            "about" -> AboutScreen(
                                onBack = { currentScreen = "home" },
                                currentTheme = state.currentZenTheme,
                                onThemeChange = { viewModel.onThemeChange(it) },
                                onCustomThemeChange = { viewModel.onCustomThemeChange(it) },
                                onApplyPreset = { viewModel.applyPreset(it) },
                                isVaultEnabled = state.isVaultEnabled,
                                onVaultToggle = { viewModel.toggleVault(it) }
                            )
                            "calendar" -> CalendarScreen(
                                onBack = { currentScreen = "home" },
                                dates = state.datesOfWeek,
                                tasksMap = state.tasksMap,
                                milestones = state.milestones,
                                selectedMonth = state.selectedCalendarMonth,
                                onMonthChange = { viewModel.onCalendarMonthChange(it) },
                                onAddMilestone = { viewModel.addMilestone(it) },
                                onToggleMilestone = { viewModel.toggleMilestone(it) },
                                onDeleteMilestone = { viewModel.deleteMilestone(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (needsAuth) {
            // Re-lock when returning from background
            needsAuth = false
        }
    }

    override fun onStop() {
        super.onStop()
        needsAuth = true
    }

    private fun scheduleDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelayTo8AM(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
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
}

@Composable
fun VaultScreen(onUnlockRequest: () -> Unit) {
    // Auto-trigger auth on first composition
    LaunchedEffect(Unit) {
        onUnlockRequest()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onUnlockRequest() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branding Node
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "H",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "HEPTA VAULT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to unlock your workspace",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}