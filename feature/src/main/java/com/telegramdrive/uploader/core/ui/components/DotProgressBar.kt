package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/**
 * The 26-dot progress bar from the design spec (`.db`).
 *
 * A compact dot-matrix bar used in list rows (Queue, History) and the
 * notification where the full 72-dot ring is too large. Dots fill left→right:
 * lit = onSurface, the single head dot = primary (glowing), unlit = surfaceHigh.
 *
 * @param progress 0f..1f
 * @param dotCount 26 per the spec
 */
@Composable
fun DotProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    dotCount: Int = 26,
    contentDescriptionText: String? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = spring(dampingRatio = 0.9f),
        label = "dotBarProgress"
    )

    val litColor: Color = MaterialTheme.colorScheme.onSurface
    val headColor: Color = MaterialTheme.colorScheme.primary
    val idleColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest

    val semanticsModifier = if (contentDescriptionText != null) {
        Modifier.semantics {
            contentDescription = contentDescriptionText
            progressBarRangeInfo = ProgressBarRangeInfo(current = clamped, range = 0f..1f, steps = 0)
        }
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .then(semanticsModifier),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val filled = (animated * dotCount).toInt()
        for (i in 0 until dotCount) {
            val color = when {
                i < filled -> litColor
                i == filled -> headColor
                else -> idleColor
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
