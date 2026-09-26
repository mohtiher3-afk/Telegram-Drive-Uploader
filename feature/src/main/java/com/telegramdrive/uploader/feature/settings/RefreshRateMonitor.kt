package com.telegramdrive.uploader.feature.settings

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Composable that monitors display refresh rate changes.
 */
@Composable
fun rememberRefreshRateState(context: Context = LocalContext.current): Float {
    var refreshRate by remember { mutableStateOf(RefreshRateHelper.getRefreshRate(context)) }

    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val listener = object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {}
                override fun onDisplayRemoved(displayId: Int) {}
                override fun onDisplayChanged(displayId: Int) {
                    if (displayId != Display.DEFAULT_DISPLAY) return
                    refreshRate = RefreshRateHelper.getRefreshRate(context)
                }
            }
            displayManager.registerDisplayListener(listener, null)
            onDispose { displayManager.unregisterDisplayListener(listener) }
        }
    }

    return refreshRate
}

fun formatRefreshRate(refreshRate: Float): String {
    return when {
        refreshRate >= 120 -> "120Hz"
        refreshRate >= 90 -> "90Hz"
        refreshRate >= 75 -> "75Hz"
        else -> "60Hz"
    }
}

fun isHighRefreshRateDevice(context: Context): Boolean {
    return RefreshRateHelper.getRefreshRate(context) >= 90f
}

fun getMaxRefreshRate(context: Context): Float {
    return RefreshRateHelper.getMaxRefreshRate(context)
}
