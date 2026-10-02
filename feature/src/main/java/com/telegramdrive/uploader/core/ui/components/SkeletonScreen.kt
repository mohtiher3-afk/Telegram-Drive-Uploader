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
import androidx.compose.runtime.remember
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
 * Skeleton screen placeholder for loading states
 * Provides animated shimmer placeholders that mimic content structure
 */
@Composable
fun SkeletonScreen(
    modifier: Modifier = Modifier,
    variant: SkeletonVariant = SkeletonVariant.ListItem,
    motionEnabled: Boolean = rememberSystemMotionEnabled()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1500,
                easing = AppMotion.standardEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonShimmer"
    )
    
    val baseWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { 240.dp.toPx() }
    val shimmerWidth = baseWidthPx * 0.6f
    val startX = (baseWidthPx + shimmerWidth) * shimmerProgress - shimmerWidth
    
    when (variant) {
        SkeletonVariant.ListItem -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.AppSpacing.md, vertical = DesignTokens.AppSpacing.sm)
                    .height(92.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(DesignTokens.AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail skeleton
                    SkeletonPlaceholder(
                        modifier = Modifier.size(68.dp).clip(MaterialTheme.shapes.small),
                        shimmerProgress = shimmerProgress,
                        shimmerWidth = shimmerWidth,
                        startX = startX,
                        baseColor = MaterialTheme.colorScheme.secondaryContainer,
                        highlightColor = MaterialTheme.colorScheme.surface,
                        motionEnabled = motionEnabled
                    )
                    
                    Spacer(modifier = Modifier.width(DesignTokens.AppSpacing.md))
                    
                    // Content skeleton
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.xs)
                    ) {
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .fillMaxWidth(0.6f)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.secondaryContainer,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                        
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .fillMaxWidth(0.4f)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.secondaryContainer,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                        
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .fillMaxWidth(0.3f)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.secondaryContainer,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                    }
                }
            }
        }
        SkeletonVariant.VideoItem -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.AppSpacing.md, vertical = DesignTokens.AppSpacing.sm)
                    .height(92.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(DesignTokens.AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Video thumbnail skeleton
                    SkeletonPlaceholder(
                        modifier = Modifier.size(68.dp).clip(MaterialTheme.shapes.small),
                        shimmerProgress = shimmerProgress,
                        shimmerWidth = shimmerWidth,
                        startX = startX,
                        baseColor = MaterialTheme.colorScheme.secondaryContainer,
                        highlightColor = MaterialTheme.colorScheme.surface,
                        motionEnabled = motionEnabled
                    )
                    
                    Spacer(modifier = Modifier.width(DesignTokens.AppSpacing.md))
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.xs)
                    ) {
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .fillMaxWidth(0.7f)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.secondaryContainer,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
                        ) {
                            SkeletonPlaceholder(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(0.3f)
                                    .clip(RoundedCornerShape(4.dp)),
                                shimmerProgress = shimmerProgress,
                                shimmerWidth = shimmerWidth,
                                startX = startX,
                                baseColor = MaterialTheme.colorScheme.secondaryContainer,
                                highlightColor = MaterialTheme.colorScheme.surface,
                                motionEnabled = motionEnabled
                            )
                            
                            SkeletonPlaceholder(
                                modifier = Modifier
                                    .height(14.dp)
                                    .fillMaxWidth(0.2f)
                                    .clip(RoundedCornerShape(4.dp)),
                                shimmerProgress = shimmerProgress,
                                shimmerWidth = shimmerWidth,
                                startX = startX,
                                baseColor = MaterialTheme.colorScheme.secondaryContainer,
                                highlightColor = MaterialTheme.colorScheme.surface,
                                motionEnabled = motionEnabled
                            )
                        }
                    }
                }
            }
        }
        SkeletonVariant.EmptyState -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
            ) {
                SkeletonPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    shimmerProgress = shimmerProgress,
                    shimmerWidth = shimmerWidth,
                    startX = startX,
                    baseColor = MaterialTheme.colorScheme.secondaryContainer,
                    highlightColor = MaterialTheme.colorScheme.surface,
                    motionEnabled = motionEnabled
                )
                SkeletonPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    shimmerProgress = shimmerProgress,
                    shimmerWidth = shimmerWidth,
                    startX = startX,
                    baseColor = MaterialTheme.colorScheme.secondaryContainer,
                    highlightColor = MaterialTheme.colorScheme.surface,
                    motionEnabled = motionEnabled
                )
            }
        }
    }
}


/**
 * Individual shimmer placeholder for custom skeleton layouts
 */
@Composable
fun SkeletonPlaceholder(
    modifier: Modifier = Modifier,
    shimmerProgress: Float,
    shimmerWidth: Float,
    startX: Float,
    baseColor: androidx.compose.ui.graphics.Color = Color.Unspecified,
    highlightColor: androidx.compose.ui.graphics.Color = Color.Unspecified,
    motionEnabled: Boolean = true
) {
    val resolvedBaseColor = if (baseColor == Color.Unspecified) MaterialTheme.colorScheme.surfaceContainer else baseColor
    val resolvedHighlightColor = if (highlightColor == Color.Unspecified) MaterialTheme.colorScheme.surface else highlightColor
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .background(resolvedBaseColor)
    ) {
        if (motionEnabled) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            resolvedBaseColor,
                            resolvedHighlightColor.copy(alpha = 0.4f),
                            resolvedBaseColor
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
