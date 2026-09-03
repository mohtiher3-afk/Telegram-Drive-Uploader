package com.telegramdrive.uploader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UploadEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uploadDao(): UploadDao
}
