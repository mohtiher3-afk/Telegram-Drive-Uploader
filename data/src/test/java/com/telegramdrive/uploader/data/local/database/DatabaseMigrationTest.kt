package com.telegramdrive.uploader.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * v5 → v6 → v7 must preserve every existing row and expose the idempotency
 * columns (provisionalMessageId, sendDispatched, finalMessageId). Without this,
 * a retry after SendMessage could resend an already-delivered video, and any
 * upgrade path without a real migration would silently wipe upload history.
 *
 * No exported schemas exist in this project, so the source databases are built
 * by hand with raw SQL, then opened through Room with the real migration chain.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    private val v5Columns = listOf(
        "id TEXT NOT NULL PRIMARY KEY",
        "sourceUri TEXT NOT NULL",
        "fileName TEXT NOT NULL",
        "fileSize INTEGER NOT NULL",
        "mimeType TEXT NOT NULL",
        "destinationId INTEGER NOT NULL",
        "status TEXT NOT NULL",
        "progress REAL NOT NULL",
        "uploadedBytes INTEGER NOT NULL",
        "totalBytes INTEGER NOT NULL",
        "speed INTEGER NOT NULL",
        "averageSpeed INTEGER NOT NULL",
        "eta INTEGER NOT NULL",
        "createdAt INTEGER NOT NULL",
        "startedAt INTEGER",
        "completedAt INTEGER",
        "lastError TEXT",
        "retryCount INTEGER NOT NULL",
        "thumbnailPath TEXT",
        "duration INTEGER NOT NULL",
        "width INTEGER NOT NULL",
        "height INTEGER NOT NULL",
        "scheduledAt INTEGER",
        "uploadDurationMs INTEGER NOT NULL",
        "messageLink TEXT"
    )

    /**
     * v3 matches the schema released in app v1.0.1 (versionCode 2): the entity
     * ended at `scheduledAt`; uploadDurationMs and messageLink did not exist yet.
     */
    private val v3Columns = v5Columns.filterNot { it.startsWith("uploadDurationMs") || it.startsWith("messageLink") }

    private val v3RequiredInsertColumns =
        "id, sourceUri, fileName, fileSize, mimeType, destinationId, " +
            "status, progress, uploadedBytes, totalBytes, speed, averageSpeed, eta, createdAt, " +
            "retryCount, duration, width, height"

    private val v3InsertValues = "('row-1', 'file:///a.mp4', 'a.mp4', 10, 'video/mp4', 5, " +
        "'UPLOADING', 0.5, 5, 10, 1, 1, 9, 100, 0, 1000, 640, 480)"

    private fun seedDatabase(dbName: String, version: Int, columns: List<String>, insertSql: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val factory = FrameworkSQLiteOpenHelperFactory()
        val raw: SupportSQLiteOpenHelper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE uploads (${columns.joinToString(", ")})")
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                    }
                })
                .build()
        )
        val writable = raw.writableDatabase
        writable.execSQL("INSERT INTO uploads ($insertSql) VALUES ${v3InsertValues}")
        writable.close()
        raw.close()
    }

    private fun openWithFullMigrationChain(dbName: String, context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun migrate3To7_preservesRowsAndEndsOnCurrentSchema() {
        val dbName = "migration-3-7-test"
        val context = ApplicationProvider.getApplicationContext<Context>()
        seedDatabase(dbName, 3, v3Columns, v3RequiredInsertColumns)

        val db = openWithFullMigrationChain(dbName, context)
        try {
            val row = runBlocking { db.uploadDao().getUploadById("row-1") }
            assertEquals("Row survives v3 → v7 upgrade", "a.mp4", row?.fileName)
            assertEquals(10L, row?.fileSize)
            assertEquals("UPLOADING", row?.status)
            assertEquals("uploadDurationMs must default to 0", 0L, row?.uploadDurationMs)
            assertNull("messageLink must default to null", row?.messageLink)
            assertNull("provisionalMessageId must default to null", row?.provisionalMessageId)
            assertEquals(false, row?.sendDispatched)
            assertNull(row?.finalMessageId)
        } finally {
            db.close()
        }
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate3To7_canStillPersistNewColumns() {
        val dbName = "migration-3-7-test"
        val context = ApplicationProvider.getApplicationContext<Context>()
        seedDatabase(dbName, 3, v3Columns, v3RequiredInsertColumns)

        val db = openWithFullMigrationChain(dbName, context)
        try {
            runBlocking { db.uploadDao().updateUploadDuration("row-1", 1234L) }
            runBlocking { db.uploadDao().updateMessageLink("row-1", "https://t.me/c/5/99") }
            runBlocking { db.uploadDao().markSendConfirmed("row-1", 99L, "https://t.me/c/5/99") }
            val row = runBlocking { db.uploadDao().getUploadById("row-1") }
            assertEquals(1234L, row?.uploadDurationMs)
            assertEquals("https://t.me/c/5/99", row?.messageLink)
            assertEquals(99L, row?.finalMessageId)
            assertEquals(false, row?.sendDispatched)
        } finally {
            db.close()
        }
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate5To7_preservesRowsAndAddsIdempotencyColumns() {
        val dbName = "migration-5-7-test"
        val context = ApplicationProvider.getApplicationContext<Context>()

        val factory = FrameworkSQLiteOpenHelperFactory()
        val raw: SupportSQLiteOpenHelper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE uploads (${v5Columns.joinToString(", ")})")
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                    }
                })
                .build()
        )
        val writable = raw.writableDatabase
        writable.execSQL(
            "INSERT INTO uploads (id, sourceUri, fileName, fileSize, mimeType, destinationId, " +
                "status, progress, uploadedBytes, totalBytes, speed, averageSpeed, eta, createdAt, " +
                "retryCount, duration, width, height, uploadDurationMs) " +
                "VALUES ('row-1', 'file:///a.mp4', 'a.mp4', 10, 'video/mp4', 5, " +
                "'UPLOADING', 0.5, 5, 10, 1, 1, 9, 100, 0, 1000, 640, 480, 0)"
        )
        writable.close()
        raw.close()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .allowMainThreadQueries()
            .build()
        try {
            val row = runBlocking { db.uploadDao().getUploadById("row-1") }
            assertEquals("a.mp4", row?.fileName)
            assertEquals("UPLOADING", row?.status)
            assertNull("New column must default to null", row?.provisionalMessageId)
            assertEquals(false, row?.sendDispatched)
            assertNull(row?.finalMessageId)

            runBlocking { db.uploadDao().updateProvisionalMessageId("row-1", 777L) }
            assertEquals(777L, runBlocking { db.uploadDao().getUploadById("row-1") }?.provisionalMessageId)
        } finally {
            db.close()
        }
        context.deleteDatabase(dbName)
    }

    @Test
    fun migrate6To7_preservesProvisionalIdAndAddsSendBookkeepingColumns() {
        val dbName = "migration-6-7-test"
        val context = ApplicationProvider.getApplicationContext<Context>()

        val factory = FrameworkSQLiteOpenHelperFactory()
        val raw: SupportSQLiteOpenHelper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(6) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE uploads (${v5Columns.joinToString(", ")}, provisionalMessageId INTEGER)")
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                    }
                })
                .build()
        )
        val writable = raw.writableDatabase
        writable.execSQL(
            "INSERT INTO uploads (id, sourceUri, fileName, fileSize, mimeType, destinationId, " +
                "status, progress, uploadedBytes, totalBytes, speed, averageSpeed, eta, createdAt, " +
                "retryCount, duration, width, height, uploadDurationMs, provisionalMessageId) " +
                "VALUES ('row-1', 'file:///a.mp4', 'a.mp4', 10, 'video/mp4', 5, " +
                "'RETRYING', 0.5, 5, 10, 1, 1, 9, 100, 0, 1000, 640, 480, 0, 4242)"
        )
        writable.close()
        raw.close()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
            .allowMainThreadQueries()
            .build()
        try {
            val row = runBlocking { db.uploadDao().getUploadById("row-1") }
            assertEquals("Existing provisional id must survive the migration", 4242L, row?.provisionalMessageId)
            assertEquals(false, row?.sendDispatched)
            assertNull(row?.finalMessageId)

            runBlocking {
                db.uploadDao().markSendDispatched("row-1")
                db.uploadDao().markSendConfirmed("row-1", 9999L, "https://t.me/c/5/9999")
            }
            val confirmed = runBlocking { db.uploadDao().getUploadById("row-1") }
            assertEquals(9999L, confirmed?.finalMessageId)
            assertEquals("https://t.me/c/5/9999", confirmed?.messageLink)
            assertNull(confirmed?.provisionalMessageId)
            assertEquals(false, confirmed?.sendDispatched)
        } finally {
            db.close()
        }
        context.deleteDatabase(dbName)
    }
}
