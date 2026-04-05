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
        
        val today = LocalDate.now()
        val tasks = useCases.getTasksForWeek(today, today).first()
            .filter { !it.isCompleted }
            .take(5)

        provideContent {
            HeptaWidgetContent(tasks)
        }
    }

    @Composable
    private fun HeptaWidgetContent(tasks: List<com.zincstate.hepta.domain.model.Task>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(12.dp)
        ) {
            Text(
                text = "HEPTA",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks for today.",
                    style = TextStyle(color = ColorProvider(Color.Gray))
                )
            } else {
                tasks.forEach { task ->
                    Text(
                        text = "• ${task.text}",
                        style = TextStyle(color = ColorProvider(Color.White)),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }
            }
        }
    }
}
