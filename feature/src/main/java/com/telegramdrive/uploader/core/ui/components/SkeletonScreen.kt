package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TileMode
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled




/**
 * Individual shimmer placeholder for custom skeleton layouts
 */
@Composable
fun SkeletonPlaceholder(
    modifier: Modifier = Modifier,
    shimmerProgress: Float,
    shimmerWidth: Float,
    startX: Float,
    baseColor: androidx.compose.ui.graphics.Color = DesignTokens.AppColors.surfaceCard,
    highlightColor: androidx.compose.ui.graphics.Color = DesignTokens.AppColors.surface,
    motionEnabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .background(baseColor)
    ) {
        if (motionEnabled) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            highlightColor.copy(alpha = 0.4f),
                            baseColor
                        ),
                        start = androidx.compose.ui.geometry.Offset(startX, 0f),
                        end = androidx.compose.ui.geometry.Offset(startX + shimmerWidth, size.height),
                        tileMode = androidx.compose.ui.graphics.TileMode.Clamp
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, size.height)
                )
            }
        }
    }
}

/**
 * Skeleton variant types for different UI patterns
 */
enum class SkeletonVariant {
    ListItem,      // Standard list item with thumbnail
    VideoItem,     // Video-specific with resolution/duration
    EmptyState,    // Full empty state with icon, text, button
}
