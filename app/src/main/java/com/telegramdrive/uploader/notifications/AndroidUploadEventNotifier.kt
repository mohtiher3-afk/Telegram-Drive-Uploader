package com.telegramdrive.uploader.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.telegramdrive.uploader.data.R
import com.telegramdrive.uploader.data.upload.notifications.UploadEventNotifier
import com.telegramdrive.uploader.domain.upload.UploadEventNotificationEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidUploadEventNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : UploadEventNotifier {

    // POST_NOTIFICATIONS is declared in the app manifest and gated by canPostNotifications()
    // at runtime; notify() is additionally wrapped in a SecurityException guard.
    @SuppressLint("MissingPermission")
    override fun notify(event: UploadEventNotificationEvent, uploadId: String) {
        if (!canPostNotifications()) return

        createChannelIfNeeded()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val notificationId = uploadId.hashCode()
        val titleRes = when (event) {
            UploadEventNotificationEvent.COMPLETED -> R.string.upload_notification_completed_title
            UploadEventNotificationEvent.FAILED -> R.string.upload_notification_failed_title
        }
        val textRes = when (event) {
            UploadEventNotificationEvent.COMPLETED -> R.string.upload_notification_completed_text
            UploadEventNotificationEvent.FAILED -> R.string.upload_notification_failed_text
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload_notification)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(textRes))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(textRes)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        // Set progress to 100 for completion, indeterminate for failure
        if (event == UploadEventNotificationEvent.COMPLETED) {
            builder.setProgress(100, 100, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        try {
            manager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // The permission can change between the check and notify call; the upload state remains authoritative.
        }
    }

    @SuppressLint("MissingPermission")
    override fun showProgressNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long) {
        if (!canPostNotifications()) return

        createChannelIfNeeded()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val notificationId = uploadId.hashCode()
        try {
            manager.notify(notificationId, buildProgressNotification(uploadId, fileName, progress, uploadedBytes, totalBytes))
        } catch (_: SecurityException) {
            // The permission can change between the check and notify call; the upload state remains authoritative.
        }
    }

    override fun buildForegroundNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long): Notification =
        buildProgressNotification(uploadId, fileName, progress, uploadedBytes, totalBytes)

    private fun buildProgressNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long): Notification {
        createChannelIfNeeded()
        val notificationId = uploadId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percent = progress.coerceIn(0, 100)
        val text = "$fileName — $percent%"

        val controlIntent = PendingIntent.getService(
            context,
            notificationId,
            Intent(context, com.telegramdrive.uploader.service.ForegroundUploadControlService::class.java).apply {
                // progress < 0 is the paused sentinel (see showPausedProgressNotification):
                // the single control button toggles between Pause and Resume.
                if (progress < 0) {
                    action = com.telegramdrive.uploader.service.ForegroundUploadControlService.ACTION_RESUME
                } else {
                    action = com.telegramdrive.uploader.service.ForegroundUploadControlService.ACTION_PAUSE
                }
                putExtra(com.telegramdrive.uploader.service.ForegroundUploadControlService.EXTRA_UPLOAD_ID, uploadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getService(
            context,
            notificationId + 1,
            Intent(context, com.telegramdrive.uploader.service.ForegroundUploadControlService::class.java).apply {
                action = com.telegramdrive.uploader.service.ForegroundUploadControlService.ACTION_CANCEL
                putExtra(com.telegramdrive.uploader.service.ForegroundUploadControlService.EXTRA_UPLOAD_ID, uploadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val detailsIntent = PendingIntent.getService(
            context,
            notificationId + 2,
            Intent(context, com.telegramdrive.uploader.service.ForegroundUploadControlService::class.java).apply {
                action = com.telegramdrive.uploader.service.ForegroundUploadControlService.ACTION_DETAILS
                putExtra(com.telegramdrive.uploader.service.ForegroundUploadControlService.EXTRA_UPLOAD_ID, uploadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload_notification)
            .setContentTitle(context.getString(R.string.upload_notification_in_progress_title))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(progress >= 0 && progress < 100)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setProgress(100, percent, false)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .addAction(
                if (progress < 0) R.drawable.ic_pause else R.drawable.ic_pause,
                context.getString(
                    if (progress < 0) R.string.upload_notification_action_resume
                    else R.string.upload_notification_action_pause
                ),
                controlIntent
            )
            .addAction(R.drawable.ic_cancel, context.getString(R.string.upload_notification_action_cancel), cancelIntent)
            .addAction(R.drawable.ic_info, context.getString(R.string.upload_notification_action_details), detailsIntent)
            .build()
    }

    /**
     * Re-renders the stable progress notification in its paused state: same
     * notification id, indeterminate progress, control button flipped to Resume.
     * Called when the control service persists PAUSED so the user can resume
     * from the same notification instead of a second one.
     */
    @SuppressLint("MissingPermission")
    fun showPausedProgressNotification(uploadId: String, fileName: String) {
        if (!canPostNotifications()) return

        createChannelIfNeeded()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        // Sentinel progress < 0 flips the control action to Resume inside
        // buildProgressNotification; percent/text are clamped for display.
        val notification = buildProgressNotification(
            uploadId = uploadId,
            fileName = fileName,
            progress = -1,
            uploadedBytes = 0L,
            totalBytes = 0L
        )
        try {
            manager.notify(uploadId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission revoked between check and notify; DB remains authoritative.
        }
    }

    override fun dismissProgressNotification(uploadId: String) {
        NotificationManagerCompat.from(context).cancel(uploadId.hashCode())
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.upload_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.upload_notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun launchIntent(): Intent =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        } ?: Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }

    private companion object {
        const val CHANNEL_ID = "upload_status"
    }
}
