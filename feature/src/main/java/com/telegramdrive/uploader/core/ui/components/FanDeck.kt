package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.AppSpacing

/**
 * The fanned stat deck - the Home hero from docs/maintenance/DESIGN_SPEC.md section 5 (`.stk`).
 *
 * Three cards stack with a slight downward offset and rotation so each peeks out
 * from behind the one above it, but every card keeps its full content (value +
 * label + circular icon) legible - matching the design, which shows "428 / FILES
 * SENT" in front with "1.2TB / MOVED" and "03 / SENDING" behind. The whole stack
 * springs in with one coordinated fan-in.
 */
@Composable
fun FanDeck(
    headlineLabel: String,
    headlineValue: String,
    secondaryLabel: String,
    secondaryValue: String,
    tertiaryLabel: String,
    tertiaryValue: String,
    modifier: Modifier = Modifier
) {
    val entry by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "fanDeckEntry"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FanDeckCard(
            value = headlineValue,
            label = headlineLabel,
            icon = Icons.Default.CloudUpload,
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            rotation = -1.2f,
            progress = entry,
            zPeek = 0.dp
        )
        FanDeckCard(
            value = secondaryValue,
            label = secondaryLabel,
            icon = Icons.Default.Folder,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            rotation = 1f,
            progress = entry,
            zPeek = (-16).dp
        )
        FanDeckCard(
            value = tertiaryValue,
            label = tertiaryLabel,
            icon = Icons.Default.PlayArrow,
            container = MaterialTheme.colorScheme.surfaceContainerHighest,
            content = MaterialTheme.colorScheme.onSurface,
            rotation = -0.6f,
            progress = entry,
            zPeek = (-32).dp
        )
    }
}

@Composable
private fun FanDeckCard(
    value: String,
    label: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    rotation: Float,
    progress: Float,
    zPeek: Dp
) {
    val y = (1f - progress) * 48f
    Box(
        modifier = Modifier
            .offset(y = zPeek + y.dp)
            .graphicsLayer {
                rotationZ = rotation
                alpha = progress
            }
            .fillMaxWidth(0.96f)
            .height(120.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(container)
            .padding(AppSpacing.large),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    // Space Mono, not Doto: Doto has no reliable digit glyphs (renders
                    // tofu), while Space Mono draws numerals cleanly (DESIGN_SPEC readouts).
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = content
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp
                    ),
                    color = content.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(content.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
