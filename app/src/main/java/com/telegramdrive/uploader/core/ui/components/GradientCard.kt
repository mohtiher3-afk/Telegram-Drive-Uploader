package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.GradientPalette
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes

/**
 * Gradient bento card. New component introduced by the Gradient Bento Dark
 * redesign; the legacy liquid-glass modifiers stay untouched no-ops.
 * Content inherits [GradientPalette.content] via LocalContentColor.
 */
@Composable
fun GradientCard(
    palette: GradientPalette,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    glowAlpha: Float = 0.35f,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tagged = if (testTag != null) modifier.testTag(testTag) else modifier
    Box(
        modifier = tagged
            .clip(shape)
            .background(Brush.verticalGradient(palette.stops))
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    ) {
        // Soft top-light sheen for the molten-tile read.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape
                )
        )
        // Ambient glow wash anchored to the palette glow color.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.glow.copy(alpha = glowAlpha * 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape
                )
        )
        CompositionLocalProvider(LocalContentColor provides palette.content) {
            Column(modifier = Modifier.padding(AppSpacing.md)) {
                content()
            }
        }
    }
}

/**
 * Primary call-to-action rendered as a molten gradient pill with ripple.
 * [enabled] dims the tile; [testTag] is preserved for UI tests.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    palette: GradientPalette = GradientPalettes.Neon,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(28.dp),
    testTag: String? = null,
    loading: Boolean = false
) {
    val tagged = if (testTag != null) modifier.testTag(testTag) else modifier
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = Color.Transparent,
        modifier = tagged.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(palette.mid, palette.top, palette.mid)
                    ),
                    shape
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = if (enabled) 0.22f else 0.08f),
                    shape
                )
                .padding(vertical = AppSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            if (loading && enabled) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = palette.content
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) palette.content
                    else palette.content.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
