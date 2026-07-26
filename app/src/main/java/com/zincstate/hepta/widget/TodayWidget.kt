package com.zincstate.hepta.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.action.clickable
import androidx.glance.action.actionParametersOf
import androidx.glance.Image
import androidx.glance.ImageProvider
import com.zincstate.hepta.R
import com.zincstate.hepta.domain.usecase.TaskUseCases
import com.zincstate.hepta.ui.theme.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class TodayWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun taskUseCases(): TaskUseCases
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val useCases = entryPoint.taskUseCases()

        provideContent {
            val today = LocalDate.now()
            // Fetch data inside provideContent so it runs on every widget update.
            // runBlocking is safe here because Glance composes in the background.
            val tasks = kotlinx.coroutines.runBlocking {
                useCases.getTasksForWeekSync(today, today)
            }.filter { !it.isCompleted }.take(5)
            
            HeptaWidgetContent(tasks)
        }
    }

    @android.annotation.SuppressLint("RestrictedApi")
    @Composable
    private fun HeptaWidgetContent(tasks: List<com.zincstate.hepta.domain.model.Task>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E)) // Dark Obsidian surface
                .cornerRadius(16.dp)
                .padding(16.dp)
                .clickable(actionStartActivity(android.content.Intent(androidx.glance.LocalContext.current, com.zincstate.hepta.MainActivity::class.java).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }))
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HEPTA",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF6366F1)), // Indigo accent
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                
                Text(
                    text = "+ ADD",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier
                        .background(Color(0xFF333333))
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable(actionStartActivity(android.content.Intent(androidx.glance.LocalContext.current, QuickAddActivity::class.java).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }))
                )
            }
            
            Spacer(modifier = GlanceModifier.height(16.dp))
            
            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All clear for today. Great job!",
                        style = TextStyle(color = ColorProvider(Color.Gray))
                    )
                }
            } else {
                tasks.forEach { task ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Static Bullet Point
                        Box(
                            modifier = GlanceModifier
                                .size(8.dp)
                                .background(ColorProvider(Color(0xFF6366F1))) // Indigo accent
                                .cornerRadius(4.dp)
                        ) {}
                        
                        Spacer(modifier = GlanceModifier.width(12.dp))
                        
                        Text(
                            text = task.text,
                            style = TextStyle(color = ColorProvider(Color.White)),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }
            }
        }
    }

    companion object {
        fun forceUpdateAll(context: Context) {
            try {
                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, TodayWidgetReceiver::class.java))
                if (ids.isNotEmpty()) {
                    val intent = android.content.Intent(context, TodayWidgetReceiver::class.java).apply {
                        action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
