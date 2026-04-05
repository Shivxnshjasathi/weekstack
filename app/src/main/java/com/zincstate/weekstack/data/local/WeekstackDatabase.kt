package com.zincstate.weekstack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class WeekstackDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
}
