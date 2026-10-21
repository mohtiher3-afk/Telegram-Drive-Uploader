package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.core.ui.theme.DotoFamily

/**
 * Numbered section header shared by every screen so the numbered title system stays
 * consistent: an eyebrow line ("(06) QUEUE") with an accent dot, over a large Doto
 * headline, with an optional muted subtitle and optional counter at the end of title.
 *
 * Reads MaterialTheme colorScheme roles only, so the header follows the active theme
 * instead of painting a fixed colour. The previous version lived in MissionComponents
 * and used a fixed white foreground token, which rendered the headline invisible
 * against a light background.
 *
 * @param number two-digit section label, e.g. "06"
 * @param label upper-case caption, e.g. "QUEUE"
 * @param title the large headline
 * @param counter optional counter shown at end of title line (e.g. "03")
 * @param subtitle optional muted line under the title
 */
@Composable
fun SectionHeader(
    number: String,
    label: String,
    title: String,
    modifier: Modifier = Modifier,
    counter: String? = null,
    subtitle: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "($number) $label",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = DotoFamily,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            if (counter != null) {
                Text(
                    text = counter,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = DotoFamily,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
