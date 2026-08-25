package com.telegramdrive.uploader.data.local.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device/emulator verification for the custom Glow preference boundary.
 * It does not create a TDLib client, authenticate, select a destination, or enqueue an upload.
 */
@RunWith(AndroidJUnit4::class)
class SettingsDataStorePersistenceTest {

    @Test
    fun customGlowIsReadByANewSettingsReader() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val writer = SettingsDataStore(context)

        try {
            writer.resetGlowColorPreferences()
            writer.saveCustomGlowColorPreferences("12ABEF")

            val readerAfterActivityRecreation = SettingsDataStore(context)
            assertEquals("Custom", readerAfterActivityRecreation.glowColorPreference.first())
            assertEquals("12ABEF", readerAfterActivityRecreation.customGlowHex.first())
        } finally {
            writer.resetGlowColorPreferences()
        }
    }
}
