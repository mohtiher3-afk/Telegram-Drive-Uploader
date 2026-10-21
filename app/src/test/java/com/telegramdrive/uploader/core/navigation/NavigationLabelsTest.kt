package com.telegramdrive.uploader.core.navigation

import com.telegramdrive.uploader.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationLabelsTest {
    @Test
    fun navigationItemsKeepStableRoutesAndLocalizedLabels() {
        val screens = listOf(Screen.Home, Screen.Send, Screen.Add, Screen.Queue, Screen.Setup)
        assertEquals(
            listOf(
                AppRoutes.HOME,
                AppRoutes.TELEGRAM_DESTINATION,
                AppRoutes.UPLOAD_PREPARATION,
                AppRoutes.QUEUE,
                AppRoutes.SETTINGS
            ),
            screens.map { it.route }
        )
        assertEquals(
            listOf(
                R.string.nav_home,
                R.string.nav_send,
                R.string.nav_add,
                R.string.nav_queue,
                R.string.nav_setup
            ),
            screens.map { it.titleRes }
        )
        assertTrue(screens.all { it.titleRes != 0 })
    }
}
