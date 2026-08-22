package com.telegramdrive.uploader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UploadEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uploadDao(): UploadDao
}
