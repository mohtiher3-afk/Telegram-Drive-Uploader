package com.telegramdrive.uploader

import com.telegramdrive.uploader.core.navigation.Screen
import com.telegramdrive.uploader.core.navigation.bottomNavItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiNavigationTest {
    
    @Test
    fun testNavigationScreensAreCorrect() {
        assertEquals("home", Screen.Home.route)
        assertEquals("Home", Screen.Home.title)

        assertEquals("queue", Screen.Queue.route)
        assertEquals("Queue", Screen.Queue.title)

        assertEquals("history", Screen.History.route)
        assertEquals("History", Screen.History.title)

        assertEquals("settings", Screen.Settings.route)
        assertEquals("Settings", Screen.Settings.title)
    }

    @Test
    fun testBottomNavItemsIncludeAllScreens() {
        assertEquals(4, bottomNavItems.size)
        assertTrue(bottomNavItems.contains(Screen.Home))
        assertTrue(bottomNavItems.contains(Screen.Queue))
        assertTrue(bottomNavItems.contains(Screen.History))
        assertTrue(bottomNavItems.contains(Screen.Settings))
    }
}
