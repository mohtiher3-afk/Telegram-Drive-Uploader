package com.telegramdrive.uploader.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.upload.notifications.UploadEventNotifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AndroidUploadEventNotifierTest {
    private lateinit var context: Context
    private lateinit var notifier: AndroidUploadEventNotifier
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notifier = AndroidUploadEventNotifier(context)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @Test
    fun `foreground notification exposes all upload control actions`() {
        val notification = notifier.buildForegroundNotification(
            uploadId = "upload-1",
            fileName = "clip.mp4",
            progress = 42,
            uploadedBytes = 420L,
            totalBytes = 1_000L
        )

        assertEquals(3, notification.actions?.size)
        val labels = notification.actions.orEmpty().map { it.title.toString() }
        assertTrue(labels.contains(context.getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_pause)))
        assertTrue(labels.contains(context.getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_cancel)))
        assertTrue(labels.contains(context.getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_details)))
    }

    @Test
    fun `paused progress notification flips control action to resume`() {
        notifier.showPausedProgressNotification("upload-paused", "clip.mp4", progress = 42)

        val shown = shadowOf(notificationManager).getNotification("upload-paused".hashCode())
        assertNotNull(shown)
        val labels = shown.actions.orEmpty().map { it.title.toString() }
        assertEquals(3, shown.actions?.size)
        assertTrue(labels.contains(context.getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_resume)))
        assertTrue(labels.contains(context.getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_cancel)))
        assertTrue(labels.contains(context.getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_details)))
        // The paused card holds the real progress instead of resetting to 0%.
        assertEquals(
            "clip.mp4 — 42%",
            shown.extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
        )
    }

    @Test
    fun `dismiss progress notification removes the stable upload notification`() {
        val notification: Notification = notifier.buildForegroundNotification(
            uploadId = "upload-2",
            fileName = "clip.mp4",
            progress = 50,
            uploadedBytes = 500L,
            totalBytes = 1_000L
        )
        notificationManager.notify("upload-2".hashCode(), notification)

        assertNotNull(shadowOf(notificationManager).getNotification("upload-2".hashCode()))
        notifier.dismissProgressNotification("upload-2")
        assertEquals(null, shadowOf(notificationManager).getNotification("upload-2".hashCode()))
    }
}
