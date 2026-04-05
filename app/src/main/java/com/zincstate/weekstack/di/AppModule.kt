package com.zincstate.weekstack.di

import android.app.Application
import androidx.room.Room
import com.zincstate.weekstack.data.local.WeekstackDatabase
import com.zincstate.weekstack.data.repository.TaskRepositoryImpl
import com.zincstate.weekstack.domain.repository.TaskRepository
import com.zincstate.weekstack.domain.usecase.AddTask
import com.zincstate.weekstack.domain.usecase.DeleteTask
import com.zincstate.weekstack.domain.usecase.GetTasksForWeek
import com.zincstate.weekstack.domain.usecase.TaskUseCases
import com.zincstate.weekstack.domain.usecase.UpdateTask
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeekstackDatabase(app: Application): WeekstackDatabase {
        return Room.databaseBuilder(
            app,
            WeekstackDatabase::class.java,
            "weekstack_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(db: WeekstackDatabase): TaskRepository {
        return TaskRepositoryImpl(db.taskDao)
    }

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases {
        return TaskUseCases(
            getTasksForWeek = GetTasksForWeek(repository),
            addTask = AddTask(repository),
            updateTask = UpdateTask(repository),
            deleteTask = DeleteTask(repository)
        )
    }
}
