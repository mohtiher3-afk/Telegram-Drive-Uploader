package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A screen-level container that applies consistent insets and padding across all destinations.
 * <p>
 * Provides:
 * - Top: status bar padding + 8dp (so content doesn't draw under the status bar)
 * - Horizontal: 20.dp screen-edge padding (from design spec)
 * - Bottom: room for the floating nav bar when shown on bottom-nav destinations
 * </p>
 * <p>
 * Keeps edge-to-edge drawing so the status bar / gesture area remain transparent.
 * </p>
 */
@Composable
fun AppScreenContainer(
    modifier: Modifier = Modifier,
    hasFloatingNav: Boolean = false,
    content: @Composable () -> Unit
) {
    // Floating nav bar height ~56dp + gesture bar
    val floatingNavBottomPadding = if (hasFloatingNav) 72.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            // Use system inset padding values via WindowInsets
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            // Apply horizontal padding + bottom floating nav + extra 8dp top to content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = floatingNavBottomPadding
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                content()
            }
        }
    }
}