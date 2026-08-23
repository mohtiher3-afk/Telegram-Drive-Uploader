package com.telegramdrive.uploader.core.di

import android.content.Context
import androidx.room.Room
import com.telegramdrive.uploader.data.local.database.AppDatabase
import com.telegramdrive.uploader.data.local.database.UploadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "telegram_drive_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUploadDao(database: AppDatabase): UploadDao {
        return database.uploadDao()
    }
}
