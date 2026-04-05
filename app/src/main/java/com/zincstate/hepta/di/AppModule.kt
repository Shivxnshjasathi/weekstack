package com.zincstate.hepta.di

import android.app.Application
import androidx.room.Room
import com.zincstate.hepta.data.local.HeptaDatabase
import com.zincstate.hepta.data.repository.TaskRepositoryImpl
import com.zincstate.hepta.domain.repository.TaskRepository
import com.zincstate.hepta.domain.usecase.AddTask
import com.zincstate.hepta.domain.usecase.DeleteTask
import com.zincstate.hepta.domain.usecase.GetTasksForWeek
import com.zincstate.hepta.domain.usecase.TaskUseCases
import com.zincstate.hepta.domain.usecase.UpdateTask
import com.zincstate.hepta.domain.usecase.UpsertTasks
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
    fun provideHeptaDatabase(app: Application): HeptaDatabase {
        return Room.databaseBuilder(
            app,
            HeptaDatabase::class.java,
            "hepta_db_v3"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(db: HeptaDatabase): TaskRepository {
        return TaskRepositoryImpl(db.taskDao)
    }

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases {
        return TaskUseCases(
            getTasksForWeek = GetTasksForWeek(repository),
            addTask = AddTask(repository),
            updateTask = UpdateTask(repository),
            upsertTasks = UpsertTasks(repository),
            deleteTask = DeleteTask(repository)
        )
    }
}
