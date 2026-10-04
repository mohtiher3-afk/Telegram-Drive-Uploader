package com.telegramdrive.uploader.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the startup path so ART can AOT-compile it on install.
 *
 * The output lands in :app/src/main/baseline-prof.txt and is merged into every
 * release build. Run with:
 *   ./gradlew :benchmark:generateBaselineProfile
 *
 * Requires a connected device or a running emulator; it cannot run on the JVM.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(packageName = TARGET_PACKAGE) {
        pressHome()
        startActivityAndWait()
    }

    private companion object {
        const val TARGET_PACKAGE = "com.aistudio.telegramdrive.prmuq"
    }
}
