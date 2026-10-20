package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.core.ui.theme.AppDisplayTextStyle
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dot-matrix progress ring — the transfer readout from the design spec
 * (`.rg`, 72 dots on a circle).
 *
 * Dots fill clockwise: completed dots render in the "on" colour, the single
 * head dot glows in the accent, remaining dots sit dim. The centre carries a
 * large Doto percentage so the ring reads as an instrument, not a spinner.
 *
 * @param progress 0f..1f fraction of completion
 * @param size overall diameter
 * @param dotCount number of dots around the ring (72 per the spec)
 */
@Composable
fun DotProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 224.dp,
    dotCount: Int = 72,
    contentDescriptionText: String? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = clamped,
        animationSpec = spring(dampingRatio = 0.9f),
        label = "ringProgress"
    )

    val onColor: Color = MaterialTheme.colorScheme.onSurface
    val headColor: Color = MaterialTheme.colorScheme.primary
    val trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest

    val semanticsModifier = if (contentDescriptionText != null) {
        Modifier.semantics {
            contentDescription = contentDescriptionText
            progressBarRangeInfo = ProgressBarRangeInfo(current = clamped, range = 0f..1f, steps = 0)
        }
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .then(semanticsModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = (this.size.minDimension / 2f) * 0.78f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val dotRadius = (this.size.minDimension / 2f) * 0.045f
            val filled = (animated * dotCount).toInt()

            for (i in 0 until dotCount) {
                val angle = (i.toFloat() / dotCount) * 2f * Math.PI - Math.PI / 2f
                val cx = center.x + radius * cos(angle).toFloat()
                val cy = center.y + radius * sin(angle).toFloat()
                val color = when {
                    i < filled -> onColor
                    i == filled -> headColor
                    else -> trackColor
                }
                drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animated * 100).toInt()}%",
                style = AppDisplayTextStyle.copy(
                    fontSize = (size.value * 0.26f).sp,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
