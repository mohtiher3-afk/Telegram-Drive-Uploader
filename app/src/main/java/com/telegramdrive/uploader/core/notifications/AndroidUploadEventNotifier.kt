package com.telegramdrive.uploader.core.notifications

import android.Manifest
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
import com.telegramdrive.uploader.MainActivity
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.domain.upload.UploadEventNotificationEvent
import com.telegramdrive.uploader.domain.upload.UploadEventNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidUploadEventNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : UploadEventNotifier {

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
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
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

    override fun showProgressNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long) {
        if (!canPostNotifications()) return

        createChannelIfNeeded()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val notificationId = uploadId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percent = progress.coerceIn(0, 100)
        val text = "$fileName — $percent%"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload_notification)
            .setContentTitle(context.getString(R.string.upload_notification_in_progress_title))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(progress < 100)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setProgress(100, percent, false)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .build()

        try {
            manager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // The permission can change between the check and notify call; the upload state remains authoritative.
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

    private companion object {
        const val CHANNEL_ID = "upload_status"
    }
}
