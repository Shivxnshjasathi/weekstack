package com.zincstate.hepta.di

import android.content.Context
import androidx.room.Room
import com.zincstate.hepta.data.local.HeptaDatabase
import com.zincstate.hepta.data.local.MilestoneDao
import com.zincstate.hepta.data.local.TaskDao
import com.zincstate.hepta.data.repository.TaskRepositoryImpl
import com.zincstate.hepta.domain.repository.TaskRepository
import com.zincstate.hepta.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHeptaDatabase(@ApplicationContext context: Context): HeptaDatabase {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN notes TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN subtasks TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS notes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "dateEpochDays INTEGER NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "content TEXT NOT NULL DEFAULT '', " +
                    "lastUpdated INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }
        
        return Room.databaseBuilder(
            context,
            HeptaDatabase::class.java,
            "hepta_db"
        )
        .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(db: HeptaDatabase): TaskRepository {
        return TaskRepositoryImpl(db.taskDao)
    }

    @Provides
    @Singleton
    fun provideMilestoneDao(db: HeptaDatabase): MilestoneDao {
        return db.milestoneDao
    }

    @Provides
    @Singleton
    fun provideNoteDao(db: HeptaDatabase): com.zincstate.hepta.data.local.NoteDao {
        return db.noteDao
    }

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases {
        return TaskUseCases(
            getTasksForWeek = GetTasksForWeek(repository),
            getTasksForWeekSync = GetTasksForWeekSync(repository),
            addTask = AddTask(repository),
            updateTask = UpdateTask(repository),
            upsertTasks = UpsertTasks(repository),
            deleteTask = DeleteTask(repository)
        )
    }

    @Provides
    @Singleton
    fun provideShiftTasksUseCase(repository: TaskRepository): ShiftTasksUseCase {
        return ShiftTasksUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCalendarEventsUseCase(@ApplicationContext context: Context): GetCalendarEventsUseCase {
        return GetCalendarEventsUseCase(context)
    }
}
