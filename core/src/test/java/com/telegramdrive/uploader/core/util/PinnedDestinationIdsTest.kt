package com.telegramdrive.uploader.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinnedDestinationIdsTest {
    @Test
    fun parseIgnoresMalformedValuesAndDuplicates() {
        assertEquals(setOf(42L, 7L), PinnedDestinationIds.parse("42,invalid,7,42"))
    }

    @Test
    fun encodeIsDeterministic() {
        assertEquals("7,42", PinnedDestinationIds.encode(setOf(42L, 7L)))
    }

    @Test
    fun toggleAddsAndRemovesDestination() {
        val pinned = PinnedDestinationIds.toggle(emptySet(), 42L)
        assertTrue(42L in pinned)
        assertEquals(emptySet<Long>(), PinnedDestinationIds.toggle(pinned, 42L))
    }
}
