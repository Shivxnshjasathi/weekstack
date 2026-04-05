package com.zincstate.hepta.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 3, exportSchema = false)
abstract class HeptaDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
}
