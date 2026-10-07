package com.telegramdrive.uploader.core.ai

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the AI response parser that converts Gemini Nano model output into a
 * structured [SmartFileSuggestion].
 *
 * These run against [SmartFileResponseParser] directly. An earlier revision kept a private
 * copy of the parsing logic inside this test file, which is why a real defect survived: the
 * copy and the production code drifted, and the copy's own assertions were never satisfied.
 * Testing the shared parser removes the possibility of that drift.
 */
class SmartFileResponseParserTest {

    @Test
    fun parse_returnsSuggestionWithFilenameAndKeywords() {
        val response = """
            FILENAME: my_photo_sunset
            KEYWORDS: sunset, beach, vacation, evening
        """.trimIndent()

        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-1",
            response = response,
            originalFileName = "IMG_2024.jpg"
        )

        assertEquals("task-1", suggestion.taskId)
        assertTrue(suggestion.suggestedName.contains("my_photo_sunset"))
        // The extension comes from the original file, never from the model's guess.
        assertTrue(suggestion.suggestedName.endsWith(".jpg"))
        assertTrue(suggestion.keywords.contains("sunset"))
        assertTrue(suggestion.keywords.contains("beach"))
        assertEquals(4, suggestion.keywords.size)
    }

    @Test
    fun parse_dropsExtensionTheModelInvented() {
        val response = """
            FILENAME: holiday_clip.png
            KEYWORDS: travel
        """.trimIndent()

        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-ext",
            response = response,
            originalFileName = "recording.mp4"
        )

        // Model said .png, the real container is .mp4.
        assertTrue(suggestion.suggestedName.endsWith(".mp4"))
        assertFalse(suggestion.suggestedName.contains(".png"))
    }

    @Test
    fun parse_fallsBackWhenFilenameMissing() {
        val response = """
            KEYWORDS: nature, landscape
        """.trimIndent()

        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-2",
            response = response,
            originalFileName = "photo.png"
        )

        assertEquals("task-2", suggestion.taskId)
        assertTrue(suggestion.suggestedName.endsWith(".png"))
        assertTrue(suggestion.keywords.contains("nature"))
    }

    @Test
    fun parse_sanitizesForbiddenChars() {
        val response = """
            FILENAME: file:name*with?bad<chars>
            KEYWORDS: test, image
        """.trimIndent()

        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-3",
            response = response,
            originalFileName = "test.jpg"
        )

        assertFalse(suggestion.suggestedName.contains(":"))
        assertFalse(suggestion.suggestedName.contains("*"))
        assertFalse(suggestion.suggestedName.contains("?"))
    }

    @Test
    fun parse_limitsKeywordsToFour() {
        val response = """
            FILENAME: test
            KEYWORDS: one, two, three, four, five, six
        """.trimIndent()

        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-4",
            response = response,
            originalFileName = "test.jpg"
        )

        assertEquals(4, suggestion.keywords.size)
    }

    @Test
    fun parse_preservesArabic() {
        val response = """
            FILENAME: صورة_شمس
            KEYWORDS: شمس, غروب, جميل
        """.trimIndent()

        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-5",
            response = response,
            originalFileName = "test.jpg"
        )

        assertTrue(suggestion.suggestedName.contains("صورة_شمس"))
        assertTrue(suggestion.keywords.contains("شمس"))
    }

    @Test
    fun parse_emptyResponseFallsBackToOriginalExtension() {
        val suggestion = SmartFileResponseParser.parse(
            taskId = "task-6",
            response = "",
            originalFileName = "clip.mov"
        )

        assertEquals("image.mov", suggestion.suggestedName)
        assertTrue(suggestion.keywords.isEmpty())
    }
}
