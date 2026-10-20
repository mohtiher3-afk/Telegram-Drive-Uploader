@file:OptIn(ExperimentalMaterial3Api::class)

package com.telegramdrive.uploader.feature.telegram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.AppButton
import com.telegramdrive.uploader.core.ui.components.AppCard
import com.telegramdrive.uploader.core.ui.components.AppSearchField
import com.telegramdrive.uploader.core.ui.components.SectionHeader
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.feature.R

/**
 * Destination picker — design screen (04) "Send to".
 *
 * Same screen, new look: numbered eyebrow, Doto title, search field,
 * Dusk cards. All routes, callbacks and ViewModel contracts are unchanged.
 */
@Composable
fun TelegramDestinationScreen(
    onBackClick: () -> Unit,
    onConnectClick: () -> Unit,
    onDestinationSelected: (TelegramDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TelegramDestinationViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val destinations by viewModel.destinations.collectAsStateWithLifecycle()
    val pinnedDestinationIds by viewModel.pinnedDestinationIds.collectAsStateWithLifecycle()
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        DestinationHeader(onBackClick = onBackClick)

        if (connectionState != TelegramConnectionState.AUTHORIZED) {
            DisconnectedState(
                onConnectClick = onConnectClick,
                modifier = Modifier.weight(1f)
            )
        } else {
            DestinationContent(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChanged,
                onClearSearch = { viewModel.onSearchQueryChanged("") },
                destinations = destinations,
                pinnedDestinationIds = pinnedDestinationIds,
                selectedDestination = selectedDestination,
                onSelect = viewModel::selectDestination,
                onPinChange = { id, pinned -> viewModel.setDestinationPinned(id, pinned) },
                onClearSelection = viewModel::clearSelection,
                onConfirm = { selectedDestination?.let(onDestinationSelected) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Numbered eyebrow + Doto title, with the back affordance. */
@Composable
private fun DestinationHeader(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("destination_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }
        SectionHeader(
            number = "04",
            label = "DESTINATIONS",
            title = stringResource(R.string.select_destination)
        )
    }
}

/** Shown when Telegram is not authorized yet. */
@Composable
private fun DisconnectedState(
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.telegram_disconnected_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.telegram_disconnected_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        AppButton(
            text = stringResource(R.string.connect_telegram),
            onClick = onConnectClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("destination_connect_telegram_button")
        )
    }
}

/** Authorized state: search, info, selected banner, destination list, confirm. */
@Composable
private fun DestinationContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    destinations: List<TelegramDestination>,
    pinnedDestinationIds: Set<Long>,
    selectedDestination: TelegramDestination?,
    onSelect: (TelegramDestination) -> Unit,
    onPinChange: (Long, Boolean) -> Unit,
    onClearSelection: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = stringResource(R.string.search_destinations),
                modifier = Modifier.weight(1f),
                textFieldModifier = Modifier.testTag("destination_search_input")
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = onClearSearch,
                    modifier = Modifier.testTag("clear_destination_search")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_search)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.small))

        // Direct-upload info card.
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.direct_upload_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Selected destination banner.
        selectedDestination?.let { dest ->
            Spacer(modifier = Modifier.height(AppSpacing.small))
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_destination_banner"),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.target_destination),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = dest.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        dest.username?.let {
                            Text(
                                text = "@$it",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceMonoFamily),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier.testTag("clear_destination_selection")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.remove_selection),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.small))

        Text(
            text = stringResource(R.string.eligible_targets),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (destinations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        if (searchQuery.isBlank()) {
                            R.string.no_destinations_found
                        } else {
                            R.string.no_destinations_found_for_query
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("destination_list"),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                contentPadding = PaddingValues(vertical = AppSpacing.small)
            ) {
                items(destinations, key = { it.id }) { dest ->
                    DestinationRow(
                        destination = dest,
                        isSelected = selectedDestination?.id == dest.id,
                        isPinned = dest.id in pinnedDestinationIds,
                        onClick = { onSelect(dest) },
                        onPinClick = { onPinChange(dest.id, dest.id !in pinnedDestinationIds) }
                    )
                }
            }
        }

        // Action confirm button, pinned at the bottom.
        Spacer(modifier = Modifier.height(AppSpacing.small))
        AppButton(
            text = stringResource(R.string.confirm_destination),
            onClick = onConfirm,
            enabled = selectedDestination != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.medium)
                .testTag("confirm_destination_button")
        )
    }
}

/** A single destination row with avatar initials, meta and pin affordance. */
@Composable
fun DestinationRow(
    destination: TelegramDestination,
    isSelected: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onPinClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("destination_item_${destination.id}"),
        containerColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(
            modifier = Modifier
                .padding(AppSpacing.small)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = AppSpacing.touchTarget)
                    .selectable(
                        selected = isSelected,
                        onClick = onClick,
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = destination.title.take(2).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceMonoFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(AppSpacing.small + AppSpacing.xSmall))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    destination.username?.let {
                        Text(
                            text = "@$it",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceMonoFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            IconButton(
                onClick = onPinClick,
                modifier = Modifier.testTag("pin_destination_${destination.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = stringResource(
                        if (isPinned) {
                            R.string.unpin_destination
                        } else {
                            R.string.pin_destination
                        }
                    ),
                    tint = if (isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.selection_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}