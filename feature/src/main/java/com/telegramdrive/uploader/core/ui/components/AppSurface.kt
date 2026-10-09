package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppElevation
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.DesignTokens

/**
 * The single surface every card in the app is built from.
 *
 * The old UI mixed three competing treatments on one screen (a near-black glass
 * card, a tinted primaryContainer card and a gradient tile), which is what made
 * it read as assembled rather than designed. [AppSurface] is the replacement:
 * one background step, one hairline, one radius, no shadow. Hierarchy comes
 * from size and spacing, not from a second visual language.
 *
 * @param accent optional accent. When present it is rendered as a very low
 *   alpha corner wash rather than a filled background, so a user's Glow colour
 *   stays an accent instead of turning a whole card muddy.
 */
@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(AppRadius.card),
    accent: Color? = null,
    accentAlpha: Float = 0.14f,
    container: Color = DesignTokens.AppColors.surfaceCard,
    content: @Composable BoxScope.() -> Unit
) {
    val hairline = DesignTokens.AppColors.glassBorder
    Box(
        modifier = modifier
            .clip(shape)
            .background(container)
            .border(BorderStroke(1.dp, hairline.copy(alpha = AppElevation.HairlineAlpha)), shape)
    ) {
        if (accent != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accent.copy(alpha = accentAlpha),
                                Color.Transparent
                            )
                        ),
                        shape
                    )
            )
        }
        content()
    }
}

