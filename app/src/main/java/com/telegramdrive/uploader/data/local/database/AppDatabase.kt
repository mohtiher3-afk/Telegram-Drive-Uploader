package com.telegramdrive.uploader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 5 → 6: nullable provisional TDLib message id for send idempotency across
 * WorkManager retries. Preserves all existing rows.
 */
val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE uploads ADD COLUMN provisionalMessageId INTEGER")
    }
}

/**
 * 6 → 7: send idempotency bookkeeping. `sendDispatched` marks a SendMessage that was
 * handed to TDLib but whose provisional id may never have been persisted (process
 * death); `finalMessageId` records confirmed delivery so retries can never re-send.
 * Both are added in place; every existing row is preserved.
 */
val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE uploads ADD COLUMN sendDispatched INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE uploads ADD COLUMN finalMessageId INTEGER")
    }
}

@Database(entities = [UploadEntity::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uploadDao(): UploadDao
}
