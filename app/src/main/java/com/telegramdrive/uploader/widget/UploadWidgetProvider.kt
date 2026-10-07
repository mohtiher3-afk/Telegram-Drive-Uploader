package com.telegramdrive.uploader.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.telegramdrive.uploader.MainActivity
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.UploadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home Screen Widget for Telegram Drive Uploader.
 *
 * Displays:
 * - Active upload progress (if any)
 * - Queue status (pending count)
 * - Quick action to open app
 *
 * Widget updates are triggered by:
 * - Upload state changes (via UploadWidgetUpdateService)
 * - Periodic updates (every 30 minutes)
 * - Manual refresh button
 */
@AndroidEntryPoint
class UploadWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var uploadRepository: UploadRepository

    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget added - schedule periodic updates
        UploadWidgetUpdateService.schedulePeriodicUpdates(context)
    }

    override fun onDisabled(context: Context) {
        // Last widget removed - cancel periodic updates
        UploadWidgetUpdateService.cancelPeriodicUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, UploadWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        updateScope.launch {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_upload)

                // Get upload stats
                val allUploads = uploadRepository.getAllUploads().first()
                val activeUploads = allUploads.filter {
                    it.status == UploadStatus.UPLOADING ||
                    it.status == UploadStatus.QUEUED ||
                    it.status == UploadStatus.PREPARING ||
                    it.status == UploadStatus.RETRYING
                }
                val pendingCount = allUploads.count {
                    it.status == UploadStatus.QUEUED || it.status == UploadStatus.PREPARING
                }
                val completedCount = allUploads.count { it.status == UploadStatus.COMPLETED }

                // Set header
                views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))

                // Active upload section
                if (activeUploads.isNotEmpty()) {
                    val upload = activeUploads.first()
                    val progress = (upload.progress * 100).toInt()
                    views.setTextViewText(R.id.widget_file_name, upload.fileName)
                    views.setTextViewText(R.id.widget_progress_text, "$progress%")
                    views.setProgressBar(R.id.widget_progress_bar, 100, progress, false)
                    views.setViewVisibility(R.id.widget_active_section, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_empty_state, android.view.View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_active_section, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_empty_state, android.view.View.VISIBLE)
                }

                // Stats
                views.setTextViewText(R.id.widget_pending_count, pendingCount.toString())
                views.setTextViewText(R.id.widget_completed_count, completedCount.toString())

                // Open app action
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

                // Refresh action
                val refreshIntent = Intent(context, UploadWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Show error state
                val views = RemoteViews(context.packageName, R.layout.widget_upload)
                views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
                views.setTextViewText(R.id.widget_error_text, "Tap to refresh")
                views.setViewVisibility(R.id.widget_error_text, android.view.View.VISIBLE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.telegramdrive.uploader.widget.ACTION_REFRESH"
    }
}
