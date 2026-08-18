package com.telegramdrive.uploader

import com.telegramdrive.uploader.core.di.RepositoryModule
import com.telegramdrive.uploader.core.di.UploadModule
import com.telegramdrive.uploader.data.telegram.client.TelegramClientImpl
import com.telegramdrive.uploader.data.upload.TelegramUploadEngineImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProductionFakeProtectionTest {

    @Test
    fun testProductionDIBindingsAreReal() {
        // Assert that RepositoryModule is binding Real TelegramClientImpl
        val repositoryMethods = RepositoryModule::class.java.declaredMethods
        val bindClientMethod = repositoryMethods.find { it.name == "bindTelegramClient" }
        
        // Assert the method exists and its parameter is the real TelegramClientImpl
        assertTrue("bindTelegramClient should exist in RepositoryModule", bindClientMethod != null)
        val paramType = bindClientMethod!!.parameterTypes[0]
        assertTrue("bindTelegramClient should accept TelegramClientImpl", paramType == TelegramClientImpl::class.java)

        // Assert that UploadModule binds Real TelegramUploadEngineImpl
        val uploadMethods = UploadModule::class.java.declaredMethods
        val bindEngineMethod = uploadMethods.find { it.name == "bindTelegramUploadEngine" }
        assertTrue("bindTelegramUploadEngine should exist in UploadModule", bindEngineMethod != null)
        val engineParamType = bindEngineMethod!!.parameterTypes[0]
        assertTrue("bindTelegramUploadEngine should accept TelegramUploadEngineImpl", engineParamType == TelegramUploadEngineImpl::class.java)
    }

    @Test
    fun testNoFakesOrMocksAreImportedInProductionDI() {
        val diFolder = File("src/main/java/com/telegramdrive/uploader/core/di")
        if (diFolder.exists()) {
            diFolder.listFiles()?.forEach { file ->
                val content = file.readText()
                assertFalse("Production DI module ${file.name} must not import Fake/Mock clients", content.contains("FakeTelegramClient"))
                assertFalse("Production DI module ${file.name} must not import MockTelegramClient", content.contains("MockTelegramClient"))
                assertFalse("Production DI module ${file.name} must not import FakeUploadEngine", content.contains("FakeUploadEngine"))
                assertFalse("Production DI module ${file.name} must not import MockUploadEngine", content.contains("MockUploadEngine"))
                assertFalse("Production DI module ${file.name} must not import DemoTelegramClient", content.contains("DemoTelegramClient"))
                assertFalse("Production DI module ${file.name} must not import DemoUploadEngine", content.contains("DemoUploadEngine"))
            }
        }
    }

    @Test
    fun testNoTimerBasedProgressOrFakeCompletionInProductionEngine() {
        val engineFile = File("src/main/java/com/telegramdrive/uploader/data/upload/TelegramUploadEngineImpl.kt")
        if (engineFile.exists()) {
            val content = engineFile.readText()
            // The production engine should not use simple timer based ticks or fixed intervals for faking
            assertFalse("Production engine must not use fake timer simulation", content.contains("fakeProgress") || content.contains("simulateTimer"))
        }
    }
}
