package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Determinate progress indicator for transfer rows.
 *
 * @param value progress fraction in `0f..1f`
 * @param color indicator color
 * @param modifier modifier for the indicator
 * @param wifiStatus optional Wi-Fi status ("connected", "disconnected", or "unknown")
 */
@Composable
fun ModernProgressIndicator(
    value: Float,
    color: Color = Color(0xFF636E72),
    modifier: Modifier = Modifier,
    wifiStatus: String? = null
) {
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        color = color,
        modifier = modifier.fillMaxWidth()
    )
}