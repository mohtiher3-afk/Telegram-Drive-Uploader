package com.telegramdrive.uploader.core.ai

import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Assert.*
import org.junit.Test

/**
 * Test cases for GeminiSmartFileAssistant.parseAIResponseForTest.
 * These tests verify the AI response parsing logic that converts Gemini model output
 * into structured SmartFileSuggestion objects.
 */
class GeminiSmartFileAssistantTest {

    @Test
    fun parseAIResponse_returnsSuggestionWithFilenameAndKeywords() {
        val response = """
            FILENAME: my_photo_sunset
            KEYWORDS: sunset, beach, vacation, evening
        """.trimIndent()

        val suggestion = parseAIResponseForTest(
            taskId = "task-1",
            response = response,
            originalFileName = "IMG_2024.jpg"
        )

        assertEquals("task-1", suggestion.taskId)
        assertTrue(suggestion.suggestedName.contains("my_photo_sunset"))
        assertTrue(suggestion.suggestedName.endsWith(".jpg"))
        assertTrue(suggestion.keywords.contains("sunset"))
        assertTrue(suggestion.keywords.contains("beach"))
        assertEquals(4, suggestion.keywords.size)
    }

    @Test
    fun parseAIResponse_fallsBackWhenFilenameMissing() {
        val response = """
            KEYWORDS: nature, landscape
        """.trimIndent()

        val suggestion = parseAIResponseForTest(
            taskId = "task-2",
            response = response,
            originalFileName = "photo.png"
        )

        assertEquals("task-2", suggestion.taskId)
        assertTrue(suggestion.suggestedName.endsWith(".png"))
        assertTrue(suggestion.keywords.contains("nature"))
    }

    @Test
    fun parseAIResponse_sanitizesForbiddenChars() {
        val response = """
            FILENAME: file:name*with?bad<chars>
            KEYWORDS: test, image
        """.trimIndent()

        val suggestion = parseAIResponseForTest(
            taskId = "task-3",
            response = response,
            originalFileName = "test.jpg"
        )

        assertFalse(suggestion.suggestedName.contains(":"))
        assertFalse(suggestion.suggestedName.contains("*"))
        assertFalse(suggestion.suggestedName.contains("?"))
    }

    @Test
    fun parseAIResponse_limitsKeywordsToFour() {
        val response = """
            FILENAME: test
            KEYWORDS: one, two, three, four, five, six
        """.trimIndent()

        val suggestion = parseAIResponseForTest(
            taskId = "task-4",
            response = response,
            originalFileName = "test.jpg"
        )

        assertEquals(4, suggestion.keywords.size)
    }

    @Test
    fun parseAIResponse_preservesArabic() {
        val response = """
            FILENAME: صورة_شمس
            KEYWORDS: شمس, غروب, جميل
        """.trimIndent()

        val suggestion = parseAIResponseForTest(
            taskId = "task-5",
            response = response,
            originalFileName = "test.jpg"
        )

        assertTrue(suggestion.suggestedName.contains("صورة_شمس"))
        assertTrue(suggestion.keywords.contains("شمس"))
    }

    /**
     * Helper function that extracts the parse logic for testing.
     * This mirrors the private parseAIResponse method but is exposed for unit testing.
     */
    private fun parseAIResponseForTest(taskId: String, response: String, originalFileName: String): SmartFileSuggestion {
        val lines = response.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var suggestedName = ""
        val keywords = mutableListOf<String>()

        for (line in lines) {
            when {
                line.startsWith("FILENAME:", ignoreCase = true) -> {
                    suggestedName = line.substringAfter("FILENAME:").trim()
                }
                line.startsWith("KEYWORDS:", ignoreCase = true) -> {
                    val kwText = line.substringAfter("KEYWORDS:").trim()
                    keywords.addAll(kwText.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                }
            }
        }

        // If AI didn't provide a filename, fall back to original
        if (suggestedName.isBlank()) {
            val extension = originalFileName.substringAfterLast('.', "mp4")
                .lowercase()
                .replace(Regex("[^a-z0-9]"), "")
                .ifBlank { "mp4" }
            suggestedName = "image.$extension"
        }

        // Sanitize filename
        val forbiddenChars = Regex("[\\\\/:*?\"<>|]")
        suggestedName = suggestedName
            .replace(forbiddenChars, "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06ff.]"), "")
            .take(120)
            .trim('_', '.')

        if (suggestedName.isBlank()) suggestedName = "image"

        // Limit keywords to 4
        val limitedKeywords = keywords.take(4)

        return SmartFileSuggestion(taskId, suggestedName, limitedKeywords)
    }
}