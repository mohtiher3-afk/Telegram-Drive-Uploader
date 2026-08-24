package com.telegramdrive.uploader.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MotionTokensTest {
    @Test
    fun motionDurationsRemainCentralizedAndOrdered() {
        assertEquals(160, AppMotion.fastMillis)
        assertEquals(220, AppMotion.shortMillis)
        assertEquals(280, AppMotion.mediumMillis)
    }

    @Test
    fun disabledMotionStillProvidesImmediateFiniteSpecs() {
        assertNotNull(AppMotion.shortTween<Int>(motionEnabled = false))
        assertNotNull(AppMotion.shortSpatialSpring(motionEnabled = false))
    }
}
