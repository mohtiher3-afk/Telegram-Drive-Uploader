package com.telegramdrive.uploader.core.ui.components

import com.telegramdrive.uploader.domain.model.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadStatusLabelTest {
    @Test
    fun everyUploadStateHasItsOwnLocalizedResource() {
        val resources = UploadStatus.values().map(::uploadStatusLabelRes)

        assertEquals(UploadStatus.values().size, resources.distinct().size)
        assertTrue(resources.all { it != 0 })
    }
}
