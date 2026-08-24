package com.telegramdrive.uploader.core.navigation

import com.telegramdrive.uploader.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationLabelsTest {
    @Test
    fun navigationItemsKeepStableRoutesAndLocalizedLabels() {
        assertEquals(
            listOf(AppRoutes.HOME, AppRoutes.QUEUE, AppRoutes.HISTORY, AppRoutes.SETTINGS),
            bottomNavItems.map { it.route }
        )
        assertEquals(
            listOf(R.string.nav_home, R.string.nav_queue, R.string.nav_history, R.string.nav_settings),
            bottomNavItems.map { it.titleRes }
        )
        assertTrue(bottomNavItems.all { it.titleRes != 0 })
    }
}
