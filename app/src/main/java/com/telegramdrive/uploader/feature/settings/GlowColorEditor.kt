package com.telegramdrive.uploader.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.core.ui.components.glowFocusIndicator
import com.telegramdrive.uploader.core.ui.theme.GlowColorCodec
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/** Editor-local preview. The surrounding app changes only after the explicit save action. */
@Composable
fun GlowColorEditor(
    savedHex: String,
    onSave: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHsv = remember(savedHex) { GlowColorCodec.hsvFromColor(GlowColorCodec.colorFromHex(savedHex)) }
    var hue by remember(savedHex) { mutableFloatStateOf(initialHsv.hue) }
    var saturation by remember(savedHex) { mutableFloatStateOf(initialHsv.saturation) }
    var brightness by remember(savedHex) { mutableFloatStateOf(initialHsv.value.coerceIn(0.40f, 1f)) }
    var wheelSize by remember { mutableStateOf(IntSize.Zero) }
    val pendingColor = GlowColorCodec.colorFromHsv(hue, saturation, brightness)
    val pendingHex = GlowColorCodec.hexFromColor(pendingColor)
    val previewTextColor = if (pendingColor.luminance() > 0.52f) Color(0xFF0B101B) else Color.White
    val wheelDescription = stringResource(R.string.custom_glow_color_wheel)
    val wheelState = stringResource(R.string.custom_glow_color_wheel_state, pendingHex)
    val updateWheelColor: (Offset) -> Unit = { point ->
        val center = Offset(wheelSize.width / 2f, wheelSize.height / 2f)
        val dx = point.x - center.x
        val dy = point.y - center.y
        val radius = (minOf(wheelSize.width, wheelSize.height) / 2f).coerceAtLeast(1f)
        saturation = (hypot(dx, dy) / radius).coerceIn(0f, 1f)
        hue = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 360f) % 360f)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.custom_glow_editor),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.custom_glow_editor_summary),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(184.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .onSizeChanged { wheelSize = it }
                    .pointerInput(wheelSize) {
                        detectTapGestures(onTap = updateWheelColor)
                    }
                    .pointerInput(wheelSize) {
                        detectDragGestures(
                            onDragStart = updateWheelColor,
                            onDrag = { change, _ -> updateWheelColor(change.position) }
                        )
                    }
                    .semantics {
                        contentDescription = wheelDescription
                        stateDescription = wheelState
                    }
                    .testTag("custom_glow_color_wheel")
            ) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red),
                        center = center
                    ),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, Color.Transparent), center = center, radius = radius),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 1f - brightness),
                    radius = radius,
                    center = center
                )
                val angle = Math.toRadians(hue.toDouble())
                val marker = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * radius * saturation,
                    y = center.y + kotlin.math.sin(angle).toFloat() * radius * saturation
                )
                drawCircle(Color.White, radius = 8.dp.toPx(), center = marker)
                drawCircle(Color(0xFF0B101B), radius = 5.dp.toPx(), center = marker)
            }
        }

        Text(
            text = "#$pendingHex",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        GlowEditorSlider(
            label = stringResource(R.string.custom_glow_hue),
            value = hue,
            valueRange = 0f..360f,
            onValueChange = { hue = it },
            tag = "custom_glow_hue_slider"
        )
        GlowEditorSlider(
            label = stringResource(R.string.custom_glow_saturation),
            value = saturation,
            valueRange = 0f..1f,
            onValueChange = { saturation = it },
            tag = "custom_glow_saturation_slider"
        )
        GlowEditorSlider(
            label = stringResource(R.string.custom_glow_brightness),
            value = brightness,
            valueRange = 0.40f..1f,
            onValueChange = { brightness = it },
            tag = "custom_glow_brightness_slider"
        )

        Text(
            text = stringResource(R.string.custom_glow_live_preview),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = pendingColor.copy(alpha = 0.20f),
            border = androidx.compose.foundation.BorderStroke(1.dp, pendingColor.copy(alpha = 0.82f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.custom_glow_selected_control), color = MaterialTheme.colorScheme.onSurface)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = pendingColor,
                    modifier = Modifier.testTag("custom_glow_live_preview")
                ) {
                    Text(
                        text = stringResource(R.string.custom_glow_preview_action),
                        color = previewTextColor,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onSave(pendingHex) },
                colors = ButtonDefaults.buttonColors(containerColor = pendingColor, contentColor = previewTextColor),
                modifier = Modifier
                    .weight(1f)
                    .glowFocusableGlowRing()
                    .testTag("save_custom_glow_button")
            ) {
                Text(stringResource(R.string.save_custom_glow))
            }
            TextButton(
                onClick = onReset,
                modifier = Modifier
                    .glowFocusableGlowRing()
                    .testTag("reset_glow_colors_button")
            ) {
                Text(stringResource(R.string.reset_glow_colors))
            }
        }
    }
}

@Composable
private fun GlowEditorSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    tag: String
) {
    val percentage = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start) * 100f)
        .roundToInt()
        .coerceIn(0, 100)
    val sliderState = stringResource(R.string.custom_glow_slider_value, percentage)
    var focused by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .glowFocusIndicator(focused)
                .semantics {
                    contentDescription = label
                    stateDescription = sliderState
                }
                .testTag(tag)
        )
    }
}

/** Keeps focus state local to each existing Material button and leaves its callback untouched. */
@Composable
private fun Modifier.glowFocusableGlowRing(): Modifier {
    var focused by remember { mutableStateOf(false) }
    return onFocusChanged { focused = it.isFocused }
        .glowFocusIndicator(focused)
}
