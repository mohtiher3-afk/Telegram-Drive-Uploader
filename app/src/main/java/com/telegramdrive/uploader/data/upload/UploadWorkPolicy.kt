package com.telegramdrive.uploader.data.upload

import androidx.work.Constraints
import androidx.work.NetworkType

internal object UploadWorkPolicy {
    fun constraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
