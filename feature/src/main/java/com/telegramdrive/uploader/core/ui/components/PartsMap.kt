package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/**
 * Chunk map — the 36-cell transfer grid from docs/maintenance/DESIGN_SPEC.md §5 (`.map`).
 *
 * Each cell is one uploaded part: done (solid "on" colour), in-flight (accent,
 * gently pulsing), or failed (danger, pulsing, tappable to retry). A one-line
 * summary sits above the grid ("18 of 36 parts sent", or the failure hint when a
 * part has failed).
 *
 * @param totalParts number of parts (36 per the spec)
 * @param sentParts parts completed
 * @param failedPartIndex 1-based index of a failed part, or null
 * @param onFailedPartClick retry handler for the failed cell
 */
@Composable
fun PartsMap(
    totalParts: Int,
    sentParts: Int,
    modifier: Modifier = Modifier,
    failedPartIndex: Int? = null,
    onFailedPartClick: (() -> Unit)? = null,
    columns: Int = 6
) {
    val motionEnabled = rememberSystemMotionEnabled()
    val sent = sentParts.coerceIn(0, totalParts)

    val doneColor = MaterialTheme.colorScheme.onSurface
    val activeColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val idleColor = MaterialTheme.colorScheme.surfaceContainer

    // One shared pulse for the in-flight + failed cells.
    val pulse = if (motionEnabled) {
        val transition = rememberInfiniteTransition(label = "partsPulse")
        val a by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), repeatMode = RepeatMode.Reverse),
            label = "partsPulseAlpha"
        )
        a
    } else 1f

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val summary = if (failedPartIndex != null) {
            "Part $failedPartIndex failed. Tap to retry."
        } else {
            "$sent of $totalParts parts sent"
        }
        Text(
            text = summary,
            style = MaterialTheme.typography.labelMedium,
            color = if (failedPartIndex != null) failColor else MaterialTheme.colorScheme.onSurfaceVariant
        )

        val rows = (totalParts + columns - 1) / columns
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (c in 0 until columns) {
                    val index = r * columns + c
                    if (index >= totalParts) {
                        Box(modifier = Modifier.weight(1f))
                        continue
                    }
                    val isDone = index < sent
                    val isActive = index == sent
                    val isFailed = failedPartIndex != null && (index + 1) == failedPartIndex
                    val cellColor: Color = when {
                        isFailed -> failColor.copy(alpha = pulse)
                        isActive -> activeColor.copy(alpha = pulse)
                        isDone -> doneColor
                        else -> idleColor
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cellColor)
                            .then(
                                if (isFailed && onFailedPartClick != null) {
                                    Modifier.clickable { onFailedPartClick() }
                                } else Modifier
                            )
                            .semantics {
                                contentDescription = when {
                                    isFailed -> "Part ${index + 1} failed"
                                    isActive -> "Part ${index + 1} sending"
                                    isDone -> "Part ${index + 1} sent"
                                    else -> "Part ${index + 1} pending"
                                }
                            }
                    )
                }
            }
        }
    }
}
