package com.telegramdrive.uploader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 3 → 4: track real upload time. Adds `uploadDurationMs` (long millis, default 0)
 * so previously released builds (app v1.0.1, versionCode 2) can upgrade without
 * wiping user upload history.
 */
val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE uploads ADD COLUMN uploadDurationMs INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * 4 → 5: real t.me message liks. Adds nullable `messageLink` so builds released as
 * app v1.0.8/v1.0.18 (versionCode 8/18) can upgrade in place.
 */
val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE uploads ADD COLUMN messageLink TEXT")
    }
}

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
