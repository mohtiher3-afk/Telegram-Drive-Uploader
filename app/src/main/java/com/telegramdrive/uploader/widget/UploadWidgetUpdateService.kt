package com.telegramdrive.uploader.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.telegramdrive.uploader.data.upload.notifications.UploadEventNotifier
import com.telegramdrive.uploader.domain.upload.UploadEventNotificationEvent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

/**
 * EntryPoint for accessing UploadEventNotifier from non-Android components.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun uploadEventNotifier(): UploadEventNotifier
}

/**
 * Service responsible for updating the home screen widget.
 *
 * Updates are triggered by:
 * 1. Upload state changes (via UploadEventNotifier)
 * 2. Periodic updates (every 30 minutes via WorkManager)
 * 3. Manual refresh (via widget button)
 */
class UploadWidgetUpdateService(private val context: Context) {

    private val entryPoint = EntryPointAccessors.fromApplication(
        context.applicationContext,
        WidgetEntryPoint::class.java
    )

    private val uploadEventNotifier = entryPoint.uploadEventNotifier()

    fun notify(event: UploadEventNotificationEvent, uploadId: String) {
        uploadEventNotifier.notify(event, uploadId)
        updateWidget()
    }

    fun showProgressNotification(
        uploadId: String,
        fileName: String,
        progress: Int,
        uploadedBytes: Long,
        totalBytes: Long
    ) {
        uploadEventNotifier.showProgressNotification(uploadId, fileName, progress, uploadedBytes, totalBytes)
        updateWidget()
    }

    fun buildForegroundNotification(
        uploadId: String,
        fileName: String,
        progress: Int,
        uploadedBytes: Long,
        totalBytes: Long
    ) = uploadEventNotifier.buildForegroundNotification(uploadId, fileName, progress, uploadedBytes, totalBytes)

    fun buildPausedNotification(
        uploadId: String,
        fileName: String,
        progress: Int
    ) = uploadEventNotifier.buildPausedNotification(uploadId, fileName, progress)

    fun dismissProgressNotification(uploadId: String) {
        uploadEventNotifier.dismissProgressNotification(uploadId)
        updateWidget()
    }

    private fun updateWidget() {
        val intent = Intent(context, UploadWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, UploadWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        context.sendBroadcast(intent)
    }

    companion object {
        private const val WORK_NAME = "widget_update_work"
        private const val UPDATE_INTERVAL_MINUTES = 30L

        fun schedulePeriodicUpdates(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelPeriodicUpdates(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, UploadWidgetProvider::class.java).apply {
                action = UploadWidgetProvider.ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Worker for periodic widget updates.
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        UploadWidgetUpdateService.triggerUpdate(applicationContext)
        return Result.success()
    }
}
