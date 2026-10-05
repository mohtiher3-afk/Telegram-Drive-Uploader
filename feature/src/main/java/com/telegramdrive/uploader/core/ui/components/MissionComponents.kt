package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DesignTokens

/**
 * Mission Control primitives shared by every screen.
 *
 * These exist so a screen composes structure and never colour: each role below reads
 * from [DesignTokens.AppColors], and the platform rule is that no screen hardcodes a hex
 * value or composes its own translucency. AppColorsContrastTest gates every
 * foreground/background pairing these produce.
 *
 * Glow is drawn with `Brush.radialGradient` inside `drawBehind`, not with `Modifier.blur`,
 * because blur is a runtime effect that cannot be reasoned about statically and is
 * expensive on the low-end devices this app targets.
 */

/** The app canvas: near-black with a single soft violet glow in the top-right corner. */
@Composable
fun MissionBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DesignTokens.AppColors.background)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DesignTokens.AppColors.purple.copy(alpha = 0.18f),
                        Color.Transparent,
                    ),
                    center = Offset(0.85f, 0f),
                    radius = 900f,
                ),
            ),
    ) {
        content()
    }
}

/**
 * The Mission Control page container: background plus a scrollable column with the
 * standard horizontal inset. Replaces the old transparent `MissionControlPage`.
 */
@Composable
fun MissionScreen(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    MissionBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}

/**
 * Raised panel: the card surface from the design (`#1a1a1e`) with a hairline border.
 *
 * Border rather than shadow, because on a near-black canvas a shadow is invisible and
 * the edge has to read some other way.
 */
@Composable
fun MissionCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadius.card.value),
    borderColor: Color = DesignTokens.AppColors.glassBorder,
    containerColor: Color = DesignTokens.AppColors.surfaceCard,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(content = content)
    }
}

/** Small uppercase label above a heading. Violet, because it is a caption, not body text. */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = DesignTokens.AppColors.purpleHot,
        modifier = modifier,
    )
}

/** Pill badge for a status or a counter. */
@Composable
fun MissionBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = DesignTokens.AppColors.glassFill,
    contentColor: Color = DesignTokens.AppColors.contentMuted,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, DesignTokens.AppColors.glassBorder, CircleShape)
            .padding(horizontal = AppSpacing.small + AppSpacing.xSmall, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
        )
    }
}

/**
 * The hero block from the design: a violet-glow gradient panel with a hairline violet
 * border, used for the active transfer on the dashboard.
 */
@Composable
fun MissionHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val violet = DesignTokens.AppColors.purpleHot
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.feature.value),
        color = Color.Transparent,
        border = BorderStroke(1.dp, violet.copy(alpha = 0.30f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF251633),
                            Color(0xFF171220),
                            Color(0xFF271638),
                        ),
                    ),
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DesignTokens.AppColors.purple.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        center = Offset(0.81f, 0.53f),
                        radius = 700f,
                    ),
                ),
        ) {
            Column(content = content)
        }
    }
}

/**
 * A labelled metric: a caption, a large value, and an optional icon. Used for the four
 * dashboard statistics.
 */
@Composable
fun MissionStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = DesignTokens.AppColors.contentPrimary,
    supporting: String? = null,
) {
    MissionCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = DesignTokens.AppColors.contentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accent,
            modifier = Modifier.padding(
                start = AppSpacing.medium,
                end = AppSpacing.medium,
                bottom = supporting?.let { AppSpacing.small } ?: AppSpacing.medium,
            ),
        )
        if (supporting != null) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = DesignTokens.AppColors.contentMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    start = AppSpacing.medium,
                    end = AppSpacing.medium,
                    bottom = AppSpacing.medium,
                ),
            )
        }
    }
}

/**
 * Horizontal progress track with a lime fill. Lime is the app's only action colour, so a
 * progress fill uses it and nothing else.
 */
@Composable
fun MissionProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    trackColor: Color = DesignTokens.AppColors.glassFill,
    fillColor: Color = DesignTokens.AppColors.lime,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .clip(CircleShape)
                .background(fillColor),
        )
    }
}

/** Vertical spacing helper so screens do not each invent their own section gap. */
@Composable
fun MissionSectionGap(height: Dp = AppSpacing.extraLarge) {
    Box(modifier = Modifier.height(height))
}