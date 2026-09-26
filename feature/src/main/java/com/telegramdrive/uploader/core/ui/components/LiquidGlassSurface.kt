package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import com.telegramdrive.uploader.core.ui.theme.LiquidGlassTokens
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import kotlin.math.max
import kotlin.math.min

/** Visual intensity for bounded glass surfaces; it never carries product state. */
enum class LiquidGlassEmphasis(
    val borderAlpha: Float,
    val reflectionAlpha: Float,
    val ambientAlpha: Float,
    val elevation: Int = 0
) {
    Subtle(borderAlpha = 0.12f, reflectionAlpha = 0.02f, ambientAlpha = 0.015f, elevation = 1),
    Operational(borderAlpha = 0.22f, reflectionAlpha = 0.04f, ambientAlpha = 0.03f, elevation = 2),
    FeatureLens(borderAlpha = 0.35f, reflectionAlpha = 0.08f, ambientAlpha = 0.06f, elevation = 4),
    Hero(borderAlpha = 0.5f, reflectionAlpha = 0.12f, ambientAlpha = 0.1f, elevation = 8)
}

/**
 * Adds a subtle reflection sheen that moves on interaction
 * Perfect for cards and buttons that need premium feel
 */
@Composable
fun Modifier.liquidGlassReflection(
    shape: Shape = RoundedCornerShape(16.dp),
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    intensity: Float = 1.0f
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "liquidGlassReflection" }
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val progress by remember { mutableStateOf(0f) }
    val isPressed = remember { mutableStateOf(false) }
    
    Modifier
        .fillMaxSize()
        .clip(shape)
        .drawWithContent {
            drawContent()
            
            // Animated sheen effect
            val sheenWidth = (size.width * 0.6f).coerceAtMost(200.dp.toPx())
            val sheenX = (size.width + sheenWidth) * progress - sheenWidth
            
            if (progress > 0f && progress < 1f) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.08f * intensity),
                            Color.White.copy(alpha = 0.12f * intensity),
                            accent.copy(alpha = 0.08f * intensity),
                            Color.Transparent
                        ),
                        start = Offset(sheenX - sheenWidth, -size.height),
                        end = Offset(sheenX, size.height * 2),
                        tileMode = androidx.compose.ui.graphics.Shader.TileMode.Clamp
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height * 2),
                    style = Fill
                )
            }
        }
        .onGloballyPositioned { layoutCoordinates ->
            // Could add hover/press listeners here for interactive sheen
        }
}

/**
 * Creates a realistic glassmorphism surface with:
 * - Frosted glass background (semi-transparent with blur simulation)
 * - Subtle border highlight (top/left brighter, bottom/right darker)
 * - Optional reflection sheen
 * - Ambient glow for elevated surfaces
 * - Elevation-based shadow
 */
@Composable
fun Modifier.liquidGlassOverlay(
    shape: Shape = RoundedCornerShape(16.dp),
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    emphasis: LiquidGlassEmphasis = LiquidGlassEmphasis.Operational,
    animate: Boolean = true
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "liquidGlassOverlay" }
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val emphasisValue = emphasis
    val accentColor = accent
    
    // Animate emphasis changes for smooth transitions
    val animatedBorderAlpha by animateFloatAsState(
        targetValue = emphasisValue.borderAlpha,
        animationSpec = if (animate) AppMotion.mediumTween() else androidx.compose.animation.core.snap,
        label = "glassBorderAlpha"
    )
    val animatedReflectionAlpha by animateFloatAsState(
        targetValue = emphasisValue.reflectionAlpha,
        animationSpec = if (animate) AppMotion.shortTween() else androidx.compose.animation.core.snap,
        label = "glassReflectionAlpha"
    )
    val animatedAmbientAlpha by animateFloatAsState(
        targetValue = emphasisValue.ambientAlpha,
        animationSpec = if (animate) AppMotion.shortTween() else androidx.compose.animation.core.snap,
        label = "glassAmbientAlpha"
    )
    
    // Dark theme detection for adaptive colors
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    this.then(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        // Top highlight - subtle white/accent glow
                        if (isDark) 
                            Color.White.copy(alpha = animatedReflectionAlpha * 0.3f)
                            else Color.White.copy(alpha = animatedReflectionAlpha * 0.15f),
                        // Main surface - frosted glass
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        // Bottom shadow
                        if (isDark)
                            Color.Black.copy(alpha = animatedAmbientAlpha * 0.4f)
                            else Color.Black.copy(alpha = animatedAmbientAlpha * 0.15f)
                    ),
                    start = Offset.Zero,
                    end = Offset(0f, 1f)
                )
            )
            // Border highlight (top-left brighter)
            .border(
                width = (1.5f * density.density).dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        if (isDark)
                            Color.White.copy(alpha = animatedBorderAlpha * 0.6f)
                            else accentColor.copy(alpha = animatedBorderAlpha * 0.4f),
                        Color.Transparent,
                        if (isDark)
                            Color.Black.copy(alpha = animatedBorderAlpha * 0.3f)
                            else Color.Black.copy(alpha = animatedBorderAlpha * 0.1f)
                    ),
                    start = Offset.Zero,
                    end = Offset(1f, 1f)
                ),
                shape = shape
            )
            // Ambient glow for elevated surfaces
            .then(if (emphasisValue.elevation > 2) {
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        Brush.radialGradient(
                            center = Offset(0.5f, 0.5f),
                            radius = max(1f, emphasisValue.elevation.toFloat() / 10f),
                            colors = listOf(
                                accentColor.copy(alpha = animatedAmbientAlpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                    .graphicsLayer {
                        shadowElevation = emphasisValue.elevation.toFloat() * density.density
                    }
            } else Modifier)
    )
}

/**
 * Adds a signal rim - animated border glow for active states
 * Used for upload in progress, recording, etc.
 */
@Composable
fun Modifier.glowSignalRim(
    shape: Shape = RoundedCornerShape(28.dp),
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    pulseSpeed: Float = 1.0f
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "glowSignalRim" }
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowSignalRim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = (1500 / pulseSpeed).toInt(),
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "glowPulseAlpha"
    )
    
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    if (!enabled) return@composed this
    
    this.then(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .border(
                width = (2f * androidx.compose.ui.platform.LocalDensity.current.density).dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = (pulseAlpha * 0.6f).coerceIn(0.2f, 0.8f)),
                        if (isDark)
                            MaterialTheme.colorScheme.secondary.copy(alpha = (pulseAlpha * 0.4f).coerceIn(0.1f, 0.5f))
                            else MaterialTheme.colorScheme.tertiary.copy(alpha = (pulseAlpha * 0.3f).coerceIn(0.1f, 0.4f)),
                        accent.copy(alpha = (pulseAlpha * 0.6f).coerceIn(0.2f, 0.8f))
                    ),
                    start = Offset.Zero,
                    end = Offset(1f, 1f),
                    tileMode = androidx.compose.ui.graphics.Shader.TileMode.Mirror
                ),
                shape = shape
            )
            .graphicsLayer {
                // Subtle outer glow
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    // Shadow handled by elevation
                }
            }
    )
}

/**
 * Real animated progress glow for upload indicators
 */
@Composable
fun RealUploadProgressGlow(
    progressFraction: Float,
    statusColor: androidx.compose.ui.graphics.Color,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progressGlow")
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2000,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "progressRotation"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = AppMotion.springTween(),
        label = "progressFraction"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background track
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = (4f * density.density).toFloat()
            val radius = min(size.width, size.height) / 2 - strokeWidth / 2
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Track
            drawCircle(
                color = statusColor.copy(alpha = 0.15f),
                center = center,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc with glow
            val sweepAngle = 360f * animatedProgress
            drawArc(
                brush = Brush.sweepGradient(
                    center = center,
                    colors = listOf(
                        statusColor.copy(alpha = 0.3f),
                        statusColor.copy(alpha = 0.8f),
                        statusColor.copy(alpha = 1f),
                        statusColor.copy(alpha = 0.8f),
                        statusColor.copy(alpha = 0.3f)
                    ),
                    startAngle = -90f + rotateAngle,
                    endAngle = 270f + rotateAngle,
                    tileMode = androidx.compose.ui.graphics.Shader.TileMode.Mirror
                ),
                center = center,
                radius = radius,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Glow effect behind progress
            if (animatedProgress > 0f) {
                drawArc(
                    color = statusColor.copy(alpha = pulseAlpha * 0.3f),
                    center = center,
                    radius = radius + strokeWidth,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth * 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Premium glass card with multiple visual layers
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    emphasis: LiquidGlassEmphasis = LiquidGlassEmphasis.Operational,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassOverlay(shape = shape, emphasis = emphasis)
            .shadow(emphasis.elevation.dp, shape = shape),
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        ) {
            content()
        }
    }
}

/**
 * Animated shimmer placeholder for loading states
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    baseColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    animationDuration: Int = 1500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = animationDuration,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val width = modifier.width?.calculate(density)?.toFloat() ?? 100.dp.toPx()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(baseColor)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val shimmerWidth = width * 0.6f
            val startX = (width + shimmerWidth) * translateX - shimmerWidth
            
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        highlightColor.copy(alpha = 0.4f),
                        baseColor
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + shimmerWidth, size.height),
                    tileMode = androidx.compose.ui.graphics.Shader.TileMode.Clamp
                ),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }
    }
}

/**
 * Micro-interaction Modifiers
 */

@Composable
fun Modifier.pressAnimation(
    scale: Float = 0.96f,
    motionEnabled: Boolean = rememberSystemMotionEnabled()
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "pressAnimation" }
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState()
    
    this
        .interactionSource(interactionSource)
        .graphicsLayer {
            val targetScale = if (pressed.value) scale else 1f
            scaleX = animateFloatAsState(
                targetValue = targetScale,
                animationSpec = if (motionEnabled) AppMotion.fastTween() else androidx.compose.animation.core.snap(),
                label = "pressScale"
            ).value
            scaleY = animateFloatAsState(
                targetValue = targetScale,
                animationSpec = if (motionEnabled) AppMotion.fastTween() else androidx.compose.animation.core.snap(),
                label = "pressScale"
            ).value
        }
        .combinedClickable(
            onClick = {},
            onLongClick = {},
            onDoubleClick = {}
        )
}

@Composable
fun Modifier.hoverScale(
    scale: Float = 1.02f,
    motionEnabled: Boolean = rememberSystemMotionEnabled()
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "hoverScale" }
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val hovered = interactionSource.collectIsHoveredAsState()
    
    this
        .interactionSource(interactionSource)
        .graphicsLayer {
            val targetScale = if (hovered.value) scale else 1f
            scaleX = animateFloatAsState(
                targetValue = targetScale,
                animationSpec = if (motionEnabled) AppMotion.shortTween() else androidx.compose.animation.core.snap(),
                label = "hoverScale"
            ).value
            scaleY = animateFloatAsState(
                targetValue = targetScale,
                animationSpec = if (motionEnabled) AppMotion.shortTween() else androidx.compose.animation.core.snap(),
                label = "hoverScale"
            ).value
        }
}
