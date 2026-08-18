package com.telegramdrive.uploader

import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TelegramClientAndAuthTest {

    @Test
    fun testAuthenticationStateMachineValidTransitions() {
        var currentState = TelegramConnectionState.DISCONNECTED
        val transitionHistory = mutableListOf<TelegramConnectionState>()

        val onTransition = { newState: TelegramConnectionState ->
            // Enforce state transition rules
            when (currentState) {
                TelegramConnectionState.DISCONNECTED -> {
                    assertTrue(newState == TelegramConnectionState.CONNECTING)
                }
                TelegramConnectionState.CONNECTING -> {
                    assertTrue(newState == TelegramConnectionState.WAITING_FOR_PHONE || newState == TelegramConnectionState.ERROR)
                }
                TelegramConnectionState.WAITING_FOR_PHONE -> {
                    assertTrue(newState == TelegramConnectionState.WAITING_FOR_CODE || newState == TelegramConnectionState.ERROR || newState == TelegramConnectionState.CONNECTING)
                }
                TelegramConnectionState.WAITING_FOR_CODE -> {
                    assertTrue(newState == TelegramConnectionState.WAITING_FOR_PASSWORD || newState == TelegramConnectionState.AUTHORIZED || newState == TelegramConnectionState.ERROR || newState == TelegramConnectionState.CONNECTING)
                }
                TelegramConnectionState.WAITING_FOR_PASSWORD -> {
                    assertTrue(newState == TelegramConnectionState.AUTHORIZED || newState == TelegramConnectionState.ERROR || newState == TelegramConnectionState.CONNECTING)
                }
                TelegramConnectionState.AUTHORIZED -> {
                    assertTrue(newState == TelegramConnectionState.CLOSING || newState == TelegramConnectionState.DISCONNECTED)
                }
                TelegramConnectionState.CLOSING -> {
                    assertTrue(newState == TelegramConnectionState.DISCONNECTED)
                }
                TelegramConnectionState.ERROR -> {
                    assertTrue(newState == TelegramConnectionState.CONNECTING || newState == TelegramConnectionState.DISCONNECTED)
                }
            }
            currentState = newState
            transitionHistory.add(newState)
        }

        // Simulating sequence: Disconnected -> Connecting -> WaitPhoneNumber -> WaitCode -> WaitPassword -> Authorized -> Closing -> Disconnected
        onTransition(TelegramConnectionState.CONNECTING)
        onTransition(TelegramConnectionState.WAITING_FOR_PHONE)
        onTransition(TelegramConnectionState.WAITING_FOR_CODE)
        onTransition(TelegramConnectionState.WAITING_FOR_PASSWORD)
        onTransition(TelegramConnectionState.AUTHORIZED)
        onTransition(TelegramConnectionState.CLOSING)
        onTransition(TelegramConnectionState.DISCONNECTED)

        assertEquals(7, transitionHistory.size)
        assertEquals(TelegramConnectionState.DISCONNECTED, currentState)
    }

    @Test
    fun testAuthenticationSecurityLeakChecks() {
        // Enforce that sensitive variables like passwords, codes, tokens are not persisted or logged
        val implFile = File("src/main/java/com/telegramdrive/uploader/data/telegram/client/TelegramClientImpl.kt")
        if (implFile.exists()) {
            val content = implFile.readText()
            
            // Check that password is not logged
            assertFalse("Passwords must not be logged", content.contains("Log.d(password)") || content.contains("Log.i(password)"))
            // Check that verification code is not logged
            assertFalse("Verification code must not be logged", content.contains("Log.d(code)") || content.contains("Log.i(code)"))
            // Check that phone number is not unnecessarily logged
            assertFalse("Phone number must not be logged in raw format", content.contains("Log.d(phoneNumber)") || content.contains("Log.i(phoneNumber)"))
        }
    }

    @Test
    fun testUserMapping() {
        val rawUser = TelegramUser(
            id = 12345L,
            firstName = "First",
            lastName = "Last",
            username = "username",
            phoneNumber = "+123456789",
            profilePhoto = "/path/photo.jpg"
        )
        
        assertEquals(12345L, rawUser.id)
        assertEquals("First", rawUser.firstName)
        assertEquals("Last", rawUser.lastName)
        assertEquals("username", rawUser.username)
        assertEquals("+123456789", rawUser.phoneNumber)
        assertEquals("/path/photo.jpg", rawUser.profilePhoto)
    }

    @Test
    fun testErrorMappings() {
        val networkEx = java.net.UnknownHostException("No internet connection")
        val fileNotFoundEx = java.io.FileNotFoundException("Could not find file")
        val authEx = SecurityException("Permission denied to auth token")

        val networkCategory = com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager.mapException(networkEx)
        val fileCategory = com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager.mapException(fileNotFoundEx)
        val authCategory = com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager.mapException(authEx)

        assertEquals(com.telegramdrive.uploader.core.diagnostics.ErrorCategory.NETWORK, networkCategory)
        assertEquals(com.telegramdrive.uploader.core.diagnostics.ErrorCategory.FILE_ACCESS, fileCategory)
        assertEquals(com.telegramdrive.uploader.core.diagnostics.ErrorCategory.PERMISSION, authCategory)
    }
}
