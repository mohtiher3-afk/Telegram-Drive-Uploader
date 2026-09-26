package com.telegramdrive.uploader.core.ui.components

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.telegramdrive.uploader.core.ui.RefreshRateHelper

/**
 * Composable that monitors display refresh rate changes and provides
 * the current refresh rate as a state value.
 *
 * Registers a [DisplayManager.DisplayListener] to detect when the display
 * configuration changes (e.g., user switches between 60Hz and 120Hz modes).
 * Automatically unregisters the listener when the composable leaves the composition.
 *
 * @param context application context for accessing system services
 * @return the current refresh rate in Hz
 */
@Composable
fun rememberRefreshRateState(context: Context = LocalContext.current): Float {
    var refreshRate by remember { mutableStateOf(RefreshRateHelper.getRefreshRate(context)) }

    DisposableEffect(Unit) {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val listener = DisplayManager.DisplayListener { display ->
            if (display.state == Display.STATE_ON) {
                refreshRate = RefreshRateHelper.getRefreshRate(context)
            }
        }
        displayManager.registerDisplayListener(listener, null)
        onDispose { displayManager.unregisterDisplayListener(listener) }
    }

    return refreshRate
}

/**
 * Returns a human-readable description of the current refresh rate.
 *
 * @param refreshRate the refresh rate in Hz
 * @return formatted string describing the refresh rate (e.g., "120Hz", "90Hz", "60Hz")
 */
fun formatRefreshRate(refreshRate: Float): String {
    return when {
        refreshRate >= 120 -> "120Hz"
        refreshRate >= 90 -> "90Hz"
        refreshRate >= 75 -> "75Hz"
        else -> "60Hz"
    }
}

/**
 * Checks if the device supports high refresh rate (90Hz or above).
 *
 * @param context application context
 * @return true if the device supports high refresh rate display
 */
fun isHighRefreshRateDevice(context: Context): Boolean {
    return RefreshRateHelper.getRefreshRate(context) >= 90f
}

/**
 * Returns the maximum refresh rate the device supports.
 *
 * @param context application context
 * @return maximum refresh rate in Hz
 */
fun getMaxRefreshRate(context: Context): Float {
    return RefreshRateHelper.getMaxRefreshRate(context)
}
