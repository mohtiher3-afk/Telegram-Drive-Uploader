package com.telegramdrive.uploader.data.upload

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType

internal object UploadWorkPolicy {
    fun constraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun existingWorkPolicy(isRetryOrResume: Boolean): ExistingWorkPolicy =
        if (isRetryOrResume) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
}
