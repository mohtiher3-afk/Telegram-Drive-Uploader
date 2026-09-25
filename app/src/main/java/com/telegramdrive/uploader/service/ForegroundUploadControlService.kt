package com.telegramdrive.uploader.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.telegramdrive.uploader.data.R
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import com.telegramdrive.uploader.core.util.OwnedStagedFileStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundUploadControlService : android.app.Service() {

    companion object {
        const val ACTION_PAUSE = "com.telegramdrive.uploader.action.PAUSE"
        const val ACTION_RESUME = "com.telegramdrive.uploader.action.RESUME"
        const val ACTION_CANCEL = "com.telegramdrive.uploader.action.CANCEL"
        const val ACTION_DETAILS = "com.telegramdrive.uploader.action.DETAILS"
        const val EXTRA_UPLOAD_ID = "upload_id"
        const val CHANNEL_ID = "upload_control"
    }

    @Inject
    lateinit var uploadManager: UploadManager

    @Inject
    lateinit var uploadRepository: UploadRepository

    @Inject
    lateinit var ownedFileStore: OwnedStagedFileStore

    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val actionMutex = Mutex()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> handlePause(intent, startId)
            ACTION_RESUME -> handleResume(intent, startId)
            ACTION_CANCEL -> handleCancel(intent, startId)
            ACTION_DETAILS -> handleDetails(intent)
            else -> {
                stopSelf(startId)
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun handlePause(intent: Intent, startId: Int) {
        val uploadId = intent.getStringExtra(EXTRA_UPLOAD_ID) ?: return
        backgroundScope.launch {
            try {
                actionMutex.withLock {
                    val changed = uploadRepository.updateStatusIf(
                        id = uploadId,
                        status = UploadStatus.PAUSED,
                        allowedStatuses = listOf(
                            UploadStatus.QUEUED,
                            UploadStatus.PREPARING,
                            UploadStatus.UPLOADING,
                            UploadStatus.RETRYING
                        )
                    )
                    if (changed) {
                        uploadRepository.bumpExecutionGeneration(
                            uploadId,
                            listOf(UploadStatus.PAUSED)
                        )
                        uploadManager.pauseUpload(uploadId)
                        // Re-render the SAME stable notification (same id) in paused
                        // state: control button flips to Resume, progress held.
                        // Falls back to the legacy standalone paused card only via
                        // showPausedNotification if the unified path throws.
                        runCatching {
                            val task = uploadRepository.getUploadById(uploadId)
                            com.telegramdrive.uploader.notifications.AndroidUploadEventNotifier(
                                applicationContext
                            ).showPausedProgressNotification(
                                uploadId,
                                task?.fileName ?: uploadId,
                                task?.progress?.toInt() ?: 0
                            )
                        }.onFailure { showPausedNotification(uploadId) }
                    }
                }
            } finally {
                stopSelf(startId)
            }
        }
    }

    private fun handleResume(intent: Intent, startId: Int) {
        val uploadId = intent.getStringExtra(EXTRA_UPLOAD_ID) ?: return
        backgroundScope.launch {
            try {
                actionMutex.withLock {
                    uploadRepository.getUploadById(uploadId)?.let { task ->
                        val changed = uploadRepository.updateStatusIf(
                            id = uploadId,
                            status = UploadStatus.QUEUED,
                            allowedStatuses = listOf(UploadStatus.PAUSED)
                        )
                        if (changed) {
                            uploadRepository.bumpExecutionGeneration(
                                uploadId,
                                listOf(UploadStatus.QUEUED)
                            )
                            uploadManager.resumeUpload(task)
                            notificationManager.cancel(uploadId.hashCode())
                        }
                    }
                }
            } finally {
                stopSelf(startId)
            }
        }
    }

    private fun handleCancel(intent: Intent, startId: Int) {
        val uploadId = intent.getStringExtra(EXTRA_UPLOAD_ID) ?: return
        backgroundScope.launch {
            try {
                actionMutex.withLock {
                    val task = uploadRepository.getUploadById(uploadId)
                    val changed = uploadRepository.updateStatusIf(
                        id = uploadId,
                        status = UploadStatus.CANCELLED,
                        allowedStatuses = listOf(
                            UploadStatus.QUEUED,
                            UploadStatus.PREPARING,
                            UploadStatus.UPLOADING,
                            UploadStatus.PAUSED,
                            UploadStatus.RETRYING,
                            UploadStatus.FAILED
                        )
                    )
                    if (changed) {
                        uploadManager.cancelUpload(uploadId)
                        task?.let { ownedFileStore.deleteOwnedFilesFor(it) }
                        notificationManager.cancel(uploadId.hashCode())
                    }
                }
            } finally {
                stopSelf(startId)
            }
        }
    }

    private fun handleDetails(intent: Intent) {
        val uploadId = intent.getStringExtra(EXTRA_UPLOAD_ID) ?: return
        startForegroundActivity(uploadId)
    }

    private fun startForegroundActivity(uploadId: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_UPLOAD_ID, uploadId)
        } ?: Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
            putExtra(EXTRA_UPLOAD_ID, uploadId)
        }
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun showPausedNotification(uploadId: String) {
        createChannelIfNeeded()

        val resumeIntent = PendingIntent.getService(
            this,
            uploadId.hashCode(),
            Intent(this, ForegroundUploadControlService::class.java).apply {
                action = ACTION_RESUME
                putExtra(EXTRA_UPLOAD_ID, uploadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getService(
            this,
            uploadId.hashCode() + 1,
            Intent(this, ForegroundUploadControlService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_UPLOAD_ID, uploadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val detailsIntent = PendingIntent.getService(
            this,
            uploadId.hashCode() + 2,
            Intent(this, ForegroundUploadControlService::class.java).apply {
                action = ACTION_DETAILS
                putExtra(EXTRA_UPLOAD_ID, uploadId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingIntent = PendingIntent.getActivity(
            this,
            uploadId.hashCode() + 3,
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            } ?: Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload_notification)
            .setContentTitle("Upload Paused")
            .setContentText("Upload $uploadId has been paused")
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .addAction(R.drawable.ic_pause, getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_resume), resumeIntent)
            .addAction(R.drawable.ic_cancel, getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_cancel), cancelIntent)
            .addAction(R.drawable.ic_info, getString(com.telegramdrive.uploader.data.R.string.upload_notification_action_details), detailsIntent)
            .build()

        notificationManager.notify(uploadId.hashCode(), notification)
    }

    private val context: Context get() = this

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = "Upload Control"
        val description = "Controls for upload notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            this.description = description
        }

        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        backgroundScope.cancel()
        super.onDestroy()
    }
}