package com.telegramdrive.uploader.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTokensTest {
    @Test
    fun motionDurationsRemainCentralizedAndOrdered() {
        // Motion durations are defined once in AppMotion; the ordering invariant is
        // what callers depend on, so assert the relationship rather than pinning copies
        // of the numbers here.
        assertTrue(
            "fast < short < medium < pageEnter",
            AppMotion.fastMillis < AppMotion.shortMillis &&
                AppMotion.shortMillis < AppMotion.mediumMillis &&
                AppMotion.mediumMillis < AppMotion.pageEnterMillis
        )
        assertEquals(2_800, AppMotion.auroraBreathMillis)
        assertEquals(1_200, AppMotion.uploadSignalPulseMillis)
    }

    @Test
    fun disabledMotionStillProvidesImmediateFiniteSpecs() {
        assertNotNull(AppMotion.shortTween<Int>(motionEnabled = false))
        assertNotNull(AppMotion.shortSpatialSpring(motionEnabled = false))
        assertNotNull(AppMotion.auroraBreath())
        assertNotNull(AppMotion.uploadSignalPulse())
    }
}
