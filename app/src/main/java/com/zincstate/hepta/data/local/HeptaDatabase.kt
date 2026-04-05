package com.zincstate.hepta.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class HeptaDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
}
