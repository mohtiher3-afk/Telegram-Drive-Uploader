package com.telegramdrive.uploader.data.di

import android.content.Context
import androidx.room.Room
import com.telegramdrive.uploader.data.local.database.AppDatabase
import com.telegramdrive.uploader.data.local.database.MIGRATION_3_4
import com.telegramdrive.uploader.data.local.database.MIGRATION_4_5
import com.telegramdrive.uploader.data.local.database.MIGRATION_5_6
import com.telegramdrive.uploader.data.local.database.MIGRATION_6_7
import com.telegramdrive.uploader.data.local.database.MIGRATION_7_8
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
        )
            // Destructive fallback is allowed ONLY on downgrade. A missing forward
            // migration must crash loudly during development instead of silently
            // wiping every upload row on upgrade.
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .build()
    }

    @Provides
    fun provideUploadDao(database: AppDatabase): UploadDao {
        return database.uploadDao()
    }
}
