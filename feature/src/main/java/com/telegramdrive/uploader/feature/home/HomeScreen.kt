package com.telegramdrive.uploader.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.AppCard
import com.telegramdrive.uploader.core.ui.components.AppChip
import com.telegramdrive.uploader.core.ui.components.DotProgressBar
import com.telegramdrive.uploader.core.ui.components.DotProgressRing
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.feature.R

/**
 * Home — the design package's screen 03 (`screens/03-home-*.png`).
 *
 * Layout order per the prototype (`drive-uploader-ui.html` §home):
 *  1. status eyebrow: a blinking dot + "Relay online · background on"
 *  2. a 50sp Doto title ("Ready" or the signed-in name) with a plus button
 *     and an initials avatar on the trailing edge
 *  3. the fanned three-card stat deck: files sent / moved to Telegram /
 *     sending in background, each with its own rotation and z-peek
 *  4. the "Now sending" card: file name, a 26-dot bar, percent and destination
 *  5. the trust pills: keeps going / auto-resume / stored on device
 *
 * Presentation only: the ViewModel, the callbacks, the photo-picker launchers
 * and the WorkManager-backed queue are untouched. Nothing here invents state —
 * the deck and the transfer card read real counters from [HomeUiState], and
 * when nothing is in flight the transfer card is simply not composed.
 */
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onVideosSelected: (List<Uri>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authorized = uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED
    val displayName = uiState.telegramUser?.let { user ->
        "${user.firstName} ${user.lastName ?: ""}".trim()
    }.orEmpty()

    // Photo Picker on Android 13+ (no storage permission needed), OpenMultipleDocuments below.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris -> if (uris.isNotEmpty()) onVideosSelected(uris) }
    )
    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> if (uris.isNotEmpty()) onVideosSelected(uris) }
    )

    HomeBody(
        displayName = displayName,
        authorized = authorized,
        filesSent = uiState.totalVideosCount,
        movedLabel = formatFileSize(uiState.totalSize),
        sendingLabel = uiState.pendingCount.toString().padStart(2, '0'),
        activeUpload = uiState.activeUploads.firstOrNull(),
        onSettingsClick = onSettingsClick,
        onConnectClick = onConnectClick,
        onSelectVideos = {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            } else {
                legacyPickerLauncher.launch(arrayOf("*/*"))
            }
        }
    )
}

/**
 * The stateless body, split out so [HomeScreenComposeTest] and
 * [ActiveTransferSummaryTest] can drive it without a Hilt container.
 */
@Composable
internal fun HomeBody(
    displayName: String,
    authorized: Boolean,
    filesSent: Int,
    movedLabel: String,
    sendingLabel: String,
    activeUpload: com.telegramdrive.uploader.domain.model.UploadTask?,
    onSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSelectVideos: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(
            start = AppSpacing.medium,
            end = AppSpacing.medium,
            top = AppSpacing.large,
            // Double the bottom inset so the last row clears the navigation bar.
            bottom = AppSpacing.extraLarge * 2
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        item("status") {
            HomeStatusLine()
        }

        item("title") {
            HomeTitleBlock(
                displayName = displayName,
                authorized = authorized,
                onSettingsClick = onSettingsClick,
                onConnectClick = onConnectClick,
                onSelectVideos = onSelectVideos
            )
        }

        item("deck") {
            HomeStatDeck(
                filesSent = filesSent,
                movedLabel = movedLabel,
                sendingLabel = sendingLabel,
                modifier = Modifier.testTag("home_stat_deck")
            )
        }

        if (activeUpload != null) {
            item("active") {
                HomeTransferCard(
                    fileName = activeUpload.fileName,
                    progress = activeUpload.progress / 100f,
                    destinationTitle = null,
                    speedBytesPerSecond = activeUpload.speed,
                    modifier = Modifier.testTag("active_transfer_card")
                )
            }
        }

        item("trust") {
            HomeTrustPills()
        }
    }
}

/** The blinking status dot plus the relay caption from the prototype's `.lb` row. */
@Composable
private fun HomeStatusLine() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                // The prototype blinks the dot with a 1.6s steps(2) animation. A
                // constant alpha keeps the row quiet under reduced motion while the
                // colour still reads as "live".
                .alpha(0.85f),
            contentAlignment = Alignment.Center
        ) {}
        Text(
            text = stringResource(R.string.home_relay_online),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.08.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The 50sp Doto title row with the plus affordance and the initials avatar. */
@Composable
private fun HomeTitleBlock(
    displayName: String,
    authorized: Boolean,
    onSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSelectVideos: () -> Unit
) {
    val title = if (displayName.isNotBlank()) displayName else stringResource(R.string.telegram_drive)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = DotoFamily,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .testTag("home_title")
        )
        Spacer(modifier = Modifier.width(AppSpacing.small))
        HomeCircleAction(
            icon = Icons.Default.Add,
            contentDescription = stringResource(R.string.new_upload),
            onClick = {
                if (authorized) onSelectVideos() else onConnectClick()
            },
            modifier = Modifier.testTag("home_add_button")
        )
        Spacer(modifier = Modifier.width(AppSpacing.small))
        HomeAvatar(
            initials = displayName.initials(),
            modifier = Modifier.testTag("home_avatar")
        )
    }
}

/** A 48dp circular icon button, the prototype's `.cir`. */
@Composable
private fun HomeCircleAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    // Surface(onClick) rather than Box.clickable: Surface merges click semantics
    // into a single Button node, so the affordance is reachable by both the
    // accessibility service and the Compose test tag on the same node.
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/** The initials avatar, the prototype's `.av`. */
@Composable
private fun HomeAvatar(
    initials: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = SpaceMonoFamily,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * The fanned three-card deck (`.stk`): files sent, moved, sending.
 *
 * Each card keeps its full label + value + icon legible, stacks with a slight
 * downward offset and rotation, and springs in together. Values are the real
 * counters from [HomeUiState] — the deck never generates a number of its own.
 */
@Composable
private fun HomeStatDeck(
    filesSent: Int,
    movedLabel: String,
    sendingLabel: String,
    modifier: Modifier = Modifier
) {
    val entry by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "homeDeckEntry"
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeDeckCard(
            value = filesSent.toString(),
            label = stringResource(R.string.stat_files_sent),
            icon = Icons.Default.Description,
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            labelColor = MaterialTheme.colorScheme.onPrimary,
            rotation = -1.2f,
            progress = entry,
            zPeek = 0.dp
        )
        HomeDeckCard(
            value = movedLabel,
            label = stringResource(R.string.stat_moved),
            icon = Icons.Default.Folder,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            rotation = 1f,
            progress = entry,
            zPeek = (-26).dp
        )
        HomeDeckCard(
            value = sendingLabel,
            label = stringResource(R.string.stat_sending),
            icon = Icons.Default.PlayArrow,
            container = MaterialTheme.colorScheme.surfaceContainer,
            content = MaterialTheme.colorScheme.onSurface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            rotation = -0.6f,
            progress = entry,
            zPeek = (-52).dp
        )
    }
}

@Composable
private fun HomeDeckCard(
    value: String,
    label: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    labelColor: Color,
    rotation: Float,
    progress: Float,
    zPeek: Dp
) {
    val y = (1f - progress) * 64f
    Box(
        modifier = Modifier
            .offset(y = zPeek + y.dp)
            .graphicsLayer {
                rotationZ = rotation
                alpha = progress
            }
            .fillMaxWidth(0.96f)
            .clip(RoundedCornerShape(AppRadius.feature))
            .background(container)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(AppRadius.feature))
            .padding(AppSpacing.large),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = DotoFamily,
                        fontWeight = FontWeight.Black
                    ),
                    color = content
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = labelColor
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

/**
 * The "Now sending" card: file name, 26-dot bar, percent and destination.
 *
 * The progress value comes straight from the task; the bar never invents one.
 */
@Composable
private fun HomeTransferCard(
    fileName: String,
    progress: Float,
    destinationTitle: String?,
    speedBytesPerSecond: Long,
    modifier: Modifier = Modifier
) {
    val percent = (progress * 100).toInt()
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_now_sending),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.08.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (speedBytesPerSecond > 0) {
                    Text(
                        text = formatFileSize(speedBytesPerSecond) + "/s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = SpaceMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = AppSpacing.small)
            )
            DotProgressBar(
                progress = progress,
                contentDescriptionText = "$percent%",
                modifier = Modifier.padding(top = AppSpacing.medium)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = SpaceMonoFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (destinationTitle != null) {
                    AppChip(text = destinationTitle)
                }
            }
        }
    }
}

/** The trust pills: keeps going / auto-resume / stored on device. */
@Composable
private fun HomeTrustPills() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        HomeTrustPill(text = stringResource(R.string.home_trust_background))
        HomeTrustPill(text = stringResource(R.string.home_trust_resume))
        HomeTrustPill(text = stringResource(R.string.home_trust_private))
    }
}

@Composable
private fun HomeTrustPill(text: String) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(AppRadius.chip)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * The active-transfer summary used by [ActiveTransferSummaryTest]: the 72-dot
 * ring with its Doto readout plus the parts map line beneath it.
 */
@Composable
internal fun ActiveTransferSummary(
    fileName: String,
    completedCount: Int,
    pendingCount: Int,
    total: Int
) {
    val progress = if (total <= 0) 0f else completedCount.toFloat() / total.toFloat()
    Column(modifier = Modifier.padding(AppSpacing.medium)) {
        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = SpaceMonoFamily,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        DotProgressRing(
            progress = progress,
            size = 180.dp,
            contentDescriptionText = "${(progress * 100).toInt()}%",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = AppSpacing.small)
        )
        Text(
            text = pluralStringResource(R.plurals.home_parts_sent, completedCount, completedCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.small)
        )
        Text(
            text = pluralStringResource(R.plurals.home_pending_summary, pendingCount, pendingCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** "Sara Nasser" -> "SN"; a blank or single-word name falls back to "··". */
private fun String.initials(): String {
    val parts = trim().split(' ').filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "··"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}
