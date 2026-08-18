package com.telegramdrive.uploader

import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiagnosticsManagerTest {

    @Before
    fun setUp() {
        DiagnosticsManager.clearDiagnostics()
    }

    @Test
    fun testPhoneRedaction() {
        val rawInput = "Sent SMS code to phone +14155552671"
        val output = DiagnosticsManager.sanitizeText(rawInput)
        assertTrue(output.contains("[REDACTED_PHONE]"))
        assertFalse(output.contains("+14155552671"))
    }

    @Test
    fun testVideoFileRedaction() {
        val rawInput = "Started processing /storage/emulated/0/Download/trip_2026.mp4 for upload"
        val output = DiagnosticsManager.sanitizeText(rawInput)
        assertTrue(output.contains("[REDACTED_VIDEO_FILE]"))
        assertFalse(output.contains("trip_2026.mp4"))
    }

    @Test
    fun testCredentialRedaction() {
        val passwordInput = "login attempt password=secret123"
        val passOutput = DiagnosticsManager.sanitizeText(passwordInput)
        assertTrue(passOutput.contains("password=[REDACTED_CREDENTIAL]"))
        assertFalse(passOutput.contains("secret123"))

        val tokenInput = "api_hash=abc123xyz7890123"
        val tokenOutput = DiagnosticsManager.sanitizeText(tokenInput)
        assertTrue(tokenOutput.contains("api_hash=[REDACTED_CREDENTIAL]"))
        assertFalse(tokenOutput.contains("abc123xyz7890123"))
    }

    @Test
    fun testVerificationCodeRedaction() {
        val rawInput = "User supplied verification code 123456"
        val output = DiagnosticsManager.sanitizeText(rawInput)
        assertTrue(output.contains("[REDACTED_CODE]"))
        assertFalse(output.contains("123456"))
    }

    @Test
    fun testHexTokenRedaction() {
        // Hexadecimal string of 32 characters
        val rawInput = "Connecting using hash 1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d"
        val output = DiagnosticsManager.sanitizeText(rawInput)
        assertTrue(output.contains("[REDACTED_HASH_KEY]"))
        assertFalse(output.contains("1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d"))
    }

    @Test
    fun testDiagnosticsMaxBoundLimit() {
        // Log 250 items, but diagnostics should maintain a max bound of 200 items
        for (i in 1..250) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_PROGRESS,
                severity = DiagnosticSeverity.INFO,
                message = "Progress ticked at $i%"
            )
        }

        val eventList = DiagnosticsManager.events.value
        assertEquals(200, eventList.size)
        // Check that only the most recent events are preserved (last items logged)
        assertTrue(eventList.first().message.contains("Progress ticked at 51%"))
        assertTrue(eventList.last().message.contains("Progress ticked at 250%"))
    }

    @Test
    fun testIncidentIdGenerationOnErrors() {
        val eventIdOrIncidentId = DiagnosticsManager.log(
            category = DiagnosticCategory.DATABASE_ERROR,
            severity = DiagnosticSeverity.ERROR,
            message = "Database connection timed out"
        )
        assertTrue("Error must generate an INC- incident code", eventIdOrIncidentId.startsWith("INC-"))
    }

    @Test
    fun testExportFormatStructure() {
        DiagnosticsManager.log(
            category = DiagnosticCategory.APP_START,
            severity = DiagnosticSeverity.INFO,
            message = "App started"
        )
        val export = DiagnosticsManager.exportDiagnostics()
        assertTrue(export.contains("TELEGRAM DRIVE DIAGNOSTICS EXPORT"))
        assertTrue(export.contains("Total Diagnostic Events: 1"))
        assertTrue(export.contains("App started"))
    }
}
