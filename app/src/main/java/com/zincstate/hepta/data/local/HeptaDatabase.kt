package com.zincstate.hepta.data.local

import androidx.room.*
import com.zincstate.hepta.data.local.TaskEntity
import com.zincstate.hepta.data.local.MilestoneEntity
import com.zincstate.hepta.data.local.TaskDao
import com.zincstate.hepta.data.local.MilestoneDao

@Database(
    entities = [
        TaskEntity::class, 
        MilestoneEntity::class
    ], 
    version = 6, 
    exportSchema = false
)
abstract class HeptaDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val milestoneDao: MilestoneDao
}
