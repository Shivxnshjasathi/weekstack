package com.zincstate.hepta.data.local

import androidx.room.*
import com.zincstate.hepta.data.local.TaskEntity
import com.zincstate.hepta.data.local.MilestoneEntity
import com.zincstate.hepta.data.local.NoteEntity
import com.zincstate.hepta.data.local.TaskDao
import com.zincstate.hepta.data.local.MilestoneDao
import com.zincstate.hepta.data.local.NoteDao

@Database(
    entities = [
        TaskEntity::class, 
        MilestoneEntity::class,
        NoteEntity::class
    ], 
    version = 7, 
    exportSchema = false
)
abstract class HeptaDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao
    abstract val milestoneDao: MilestoneDao
    abstract val noteDao: NoteDao
}
