package com.telegramdrive.uploader.feature.settings

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager
import androidx.annotation.VisibleForTesting

/**
 * Provides utility methods to query and monitor the device's screen refresh rate.
 *
 * Uses [WindowManager] to retrieve the current [Display] and reports the
 * refresh rate in Hz. Values are read from the system display on first call
 * and cached for subsequent access.
 */
object RefreshRateHelper {

    /** Cached refresh rate in Hz. Defaults to 60 Hz for devices that cannot report it. */
    @Volatile
    private var cachedRefreshRate: Float = 60f

    /**
     * Returns the current screen refresh rate in Hz.
     *
     * On first invocation, queries the system via [WindowManager] and caches the
     * result. Subsequent calls return the cached value unless explicitly reset via
     * [clearCache].
     *
     * @param context any application or activity context
     * @return refresh rate in Hz (typically 60, 90, or 120 on modern devices)
     */
    fun getRefreshRate(context: Context): Float {
        if (cachedRefreshRate == 60f) {
            synchronized(this) {
                if (cachedRefreshRate == 60f) {
                    cachedRefreshRate = computeRefreshRate(context)
                }
            }
        }
        return cachedRefreshRate
    }

    /**
     * Returns the maximum refresh rate the device supports.
     *
     * Requires API 30+ (Android 11). Falls back to the current refresh rate
     * on older devices.
     *
     * @param context any application or activity context
     * @return maximum refresh rate in Hz
     */
    fun getMaxRefreshRate(context: Context): Float {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return getRefreshRate(context)
        }
        return computeMaxRefreshRate(context)
    }

    /**
     * Resets the cached refresh rate, forcing a re-query on the next call.
     */
    @VisibleForTesting
    fun clearCache() {
        synchronized(this) {
            cachedRefreshRate = 60f
        }
    }

    private fun computeRefreshRate(context: Context): Float {
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = wm.defaultDisplay
            display.refreshRate
        } catch (_: Exception) {
            60f
        }
    }

    private fun computeMaxRefreshRate(context: Context): Float {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return getRefreshRate(context)
        }
        return try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = wm.defaultDisplay
            // maximumRefreshRate is available from API 30+
            display.maximumRefreshRate
        } catch (_: Exception) {
            getRefreshRate(context)
        }
    }
}
