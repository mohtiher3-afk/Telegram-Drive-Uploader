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
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.animateFloatAsState

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
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val width = modifier.width?.calculate(density)?.toFloat() ?? 100.dp.toPx()
    val shimmerWidth = width * 0.6f
    val startX = (width + shimmerWidth) * shimmerProgress - shimmerWidth
    
    when (variant) {
        SkeletonVariant.ListItem -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    .height(92.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail skeleton
                    SkeletonPlaceholder(
                        modifier = Modifier.size(68.dp).clip(MaterialTheme.shapes.small),
                        shimmerProgress = shimmerProgress,
                        shimmerWidth = shimmerWidth,
                        startX = startX,
                        baseColor = MaterialTheme.colorScheme.surfaceVariant,
                        highlightColor = MaterialTheme.colorScheme.surface,
                        motionEnabled = motionEnabled
                    )
                    
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    
                    // Content skeleton
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .width(0.6f * width)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.surfaceVariant,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                        
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .width(0.4f * width)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.surfaceVariant,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                        
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .width(0.3f * width)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    .height(92.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Video thumbnail skeleton
                    SkeletonPlaceholder(
                        modifier = Modifier.size(68.dp).clip(MaterialTheme.shapes.small),
                        shimmerProgress = shimmerProgress,
                        shimmerWidth = shimmerWidth,
                        startX = startX,
                        baseColor = MaterialTheme.colorScheme.surfaceVariant,
                        highlightColor = MaterialTheme.colorScheme.surface,
                        motionEnabled = motionEnabled
                    )
                    
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        SkeletonPlaceholder(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .width(0.7f * width)
                                .clip(RoundedCornerShape(4.dp)),
                            shimmerProgress = shimmerProgress,
                            shimmerWidth = shimmerWidth,
                            startX = startX,
                            baseColor = MaterialTheme.colorScheme.surfaceVariant,
                            highlightColor = MaterialTheme.colorScheme.surface,
                            motionEnabled = motionEnabled
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            SkeletonPlaceholder(
                                modifier = Modifier
                                    .height(14.dp)
                                    .width(0.3f * width)
                                    .clip(RoundedCornerShape(4.dp)),
                                shimmerProgress = shimmerProgress,
                                shimmerWidth = shimmerWidth,
                                startX = startX,
                                baseColor = MaterialTheme.colorScheme.surfaceVariant,
                                highlightColor = MaterialTheme.colorScheme.surface,
                                motionEnabled = motionEnabled
                            )
                            
                            SkeletonPlaceholder(
                                modifier = Modifier
                                    .height(14.dp)
                                    .width(0.2f * width)
                                    .clip(RoundedCornerShape(4.dp)),
                                shimmerProgress = shimmerProgress,
                                shimmerWidth = shimmerWidth,
                                startX = startX,
                                baseColor = MaterialTheme.colorScheme.surfaceVariant,
                                highlightColor = MaterialTheme.colorScheme.surface,
                                motionEnabled = motionEnabled
                            )
                        }
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
    baseColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
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
                        tileMode = androidx.compose.ui.graphics.Shader.TileMode.Clamp
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