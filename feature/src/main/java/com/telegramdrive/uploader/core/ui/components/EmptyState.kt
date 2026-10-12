package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.DesignTokens

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    animation: com.telegramdrive.uploader.core.ui.components.LottieAnimation = com.telegramdrive.uploader.core.ui.components.LottieAnimation.None
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.AppSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedEmptyStateIcon(
                animationType = animation,
                size = 48.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(DesignTokens.AppSpacing.sm))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                color = DesignTokens.AppColors.contentPrimary
            )

            Spacer(modifier = Modifier.height(DesignTokens.AppSpacing.xs))

            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = DesignTokens.AppColors.contentMuted
            )

            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(DesignTokens.AppSpacing.medium))
                AppButton(
                    text = actionText,
                    onClick = onActionClick,
                )
            }
        }
    }
}
