package com.telegramdrive.uploader.data.local.datastore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device/emulator verification for the dynamic color preference boundary.
 */
@RunWith(AndroidJUnit4::class)
class SettingsDataStorePersistenceTest {

    @Test
    fun dynamicColorEnabledIsReadByANewSettingsReader() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val writer = SettingsDataStore(context)

        try {
            writer.setDynamicColorEnabled(false)

            val readerAfterActivityRecreation = SettingsDataStore(context)
            assertEquals(false, readerAfterActivityRecreation.dynamicColorEnabled.first())

            writer.setDynamicColorEnabled(true)
            assertEquals(true, readerAfterActivityRecreation.dynamicColorEnabled.first())
        } finally {
            writer.setDynamicColorEnabled(true)
        }
    }
}
