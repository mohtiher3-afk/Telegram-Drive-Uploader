package com.telegramdrive.uploader.core.ui.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppSpacing

enum class GlowBentoVariant { Subtle, Tinted, Hero }
data class GlowBentoTile(val title: String, val value: String, val icon: ImageVector, val testTag: String, val variant: GlowBentoVariant = GlowBentoVariant.Tinted, val accent: Color? = null, val contentDescription: String? = null)
val GlowBentoCornerRadius: Dp = 28.dp
const val GlowBentoLayerLimit = 2
const val GlowBentoMaxTiles = 4
fun glowBentoVariantForStatus(isCompleted: Boolean, pendingCount: Int): GlowBentoVariant = when { isCompleted -> GlowBentoVariant.Hero; pendingCount > 0 -> GlowBentoVariant.Tinted; else -> GlowBentoVariant.Subtle }
fun <T> chunkGlowBentoTiles(tiles: List<T>): List<List<T>> = tiles.take(GlowBentoMaxTiles).chunked(2)
fun glowBentoOutlineAlpha(variant: GlowBentoVariant): Float = when (variant) { GlowBentoVariant.Hero -> .34f; GlowBentoVariant.Tinted -> .22f; GlowBentoVariant.Subtle -> .14f }
@FloatRange(from = 0.0, to = 1.0) fun glowBentoHighlightStrength(@FloatRange(from = 0.0, to = 1.0) intensity: Float): Float = intensity.coerceIn(0f, 1f)
@Composable internal fun glowBentoHighlight(variant: GlowBentoVariant, accent: Color?, dark: Boolean): Brush {
    val primary = accent ?: MaterialTheme.colorScheme.primary; val secondary = if (dark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer; val transparent = Color.Transparent
    return remember(variant, primary, secondary, dark) { when (variant) { GlowBentoVariant.Hero -> Brush.linearGradient(0f to primary.copy(alpha = .38f), .35f to secondary.copy(alpha = .25f), .72f to transparent, 1f to transparent); GlowBentoVariant.Tinted -> Brush.linearGradient(0f to primary.copy(alpha = .22f), .42f to transparent); GlowBentoVariant.Subtle -> Brush.linearGradient(0f to primary.copy(alpha = .10f), .48f to transparent) } }
}
@Composable fun GlowBentoGrid(tiles: List<GlowBentoTile>, modifier: Modifier = Modifier, heroContent: (@Composable BoxScope.() -> Unit)? = null) {
    Column(modifier.fillMaxWidth().testTag("glow_bento_grid"), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) { heroContent?.let { Surface(Modifier.fillMaxWidth().testTag("glow_bento_hero"), RoundedCornerShape(GlowBentoCornerRadius), color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) { Box(Modifier.padding(AppSpacing.medium), contentAlignment = Alignment.CenterStart) { it() } } }; chunkGlowBentoTiles(tiles).forEachIndexed { index, row -> Row(Modifier.fillMaxWidth().testTag("glow_bento_row_$index"), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) { row.forEach { GlowBentoTileCard(it, Modifier.weight(1f)) } } } }
}
@Composable private fun GlowBentoTileCard(tile: GlowBentoTile, modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f; val shape = RoundedCornerShape(GlowBentoCornerRadius)
    Surface(modifier.height(128.dp).testTag(tile.testTag).border(glowBentoOutlineAlpha(tile.variant).dp, MaterialTheme.colorScheme.outlineVariant, shape).semantics { contentDescription = tile.contentDescription ?: "${tile.title}: ${tile.value}" }, shape = shape, color = MaterialTheme.colorScheme.surfaceContainer) { Column(Modifier.background(glowBentoHighlight(tile.variant, tile.accent, dark)).padding(AppSpacing.medium), verticalArrangement = Arrangement.SpaceBetween) { Icon(tile.icon, null, tint = tile.accent ?: MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)); Column { Text(tile.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(tile.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }
}
