@file:OptIn(ExperimentalMaterial3Api::class)

package com.telegramdrive.uploader.feature.telegram

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.GradientButton
import com.telegramdrive.uploader.core.ui.components.GradientCard
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import com.telegramdrive.uploader.core.ui.theme.GradientPalette
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes

/** Calm, near-slate gradient for low-emphasis utility tiles. Matches Home's CalmSlate. */
private val CalmSlate = GradientPalette(
    top = Color(0xFF23262E),
    mid = Color(0xFF1E2128),
    base = Color(0xFF181B21),
    glow = Color(0xFF2A2E38)
)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.telegramdrive.uploader.R.string.select_destination)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("destination_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(com.telegramdrive.uploader.R.string.back)
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (connectionState != TelegramConnectionState.AUTHORIZED) {
                // Not authenticated fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(com.telegramdrive.uploader.R.string.telegram_disconnected_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(com.telegramdrive.uploader.R.string.telegram_disconnected_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GradientButton(
                        text = stringResource(com.telegramdrive.uploader.R.string.connect_telegram),
                        onClick = onConnectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_connect_telegram_button")
                    )
                }
            } else {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text(stringResource(com.telegramdrive.uploader.R.string.search_destinations)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(com.telegramdrive.uploader.R.string.clear_search))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("destination_search_input"),
                    singleLine = true
                )

                GradientCard(
                    palette = CalmSlate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CalmSlate.content
                        )
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.direct_upload_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = CalmSlate.content.copy(alpha = 0.9f)
                        )
                    }
                }

                // Selected Destination Banner
                selectedDestination?.let { dest ->
                    GradientCard(
                        palette = GradientPalettes.Ocean,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "selected_destination_banner"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.target_destination),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GradientPalettes.Ocean.content.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = dest.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GradientPalettes.Ocean.content
                                )
                                dest.username?.let {
                                    Text(
                                        text = "@$it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GradientPalettes.Ocean.content.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.clearSelection() },
                                modifier = Modifier.testTag("clear_destination_selection")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(com.telegramdrive.uploader.R.string.remove_selection),
                                    tint = GradientPalettes.Ocean.content
                                )
                            }
                        }
                    }
                }

                // Destination List
                Text(
                    text = stringResource(com.telegramdrive.uploader.R.string.eligible_targets),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
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
                                    com.telegramdrive.uploader.R.string.no_destinations_found
                                } else {
                                    com.telegramdrive.uploader.R.string.no_destinations_found_for_query
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(destinations, key = { it.id }) { dest ->
                            DestinationRow(
                                destination = dest,
                                isSelected = selectedDestination?.id == dest.id,
                                isPinned = dest.id in pinnedDestinationIds,
                                onClick = { viewModel.selectDestination(dest) },
                                onPinClick = { viewModel.setDestinationPinned(dest.id, dest.id !in pinnedDestinationIds) }
                            )
                        }
                    }
                }

                // Action Confirm Button
                GradientButton(
                    text = stringResource(com.telegramdrive.uploader.R.string.confirm_destination),
                    onClick = {
                        selectedDestination?.let { onDestinationSelected(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_destination_button"),
                    palette = GradientPalettes.Neon,
                    enabled = selectedDestination != null
                )
            }
        }
    }
}

@Composable
fun DestinationRow(
    destination: TelegramDestination,
    isSelected: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onPinClick: () -> Unit
) {
    val icon = when (destination.type) {
        TelegramDestinationType.USER -> Icons.Default.Person
        TelegramDestinationType.CHANNEL -> Icons.Default.Campaign
        TelegramDestinationType.GROUP -> Icons.Default.Group
        TelegramDestinationType.SUPERGROUP -> Icons.Default.Groups
        TelegramDestinationType.OTHER -> Icons.Default.Folder
    }

    val palette = if (isSelected) GradientPalettes.Ocean else CalmSlate

    GradientCard(
        palette = palette,
        modifier = Modifier.fillMaxWidth(),
        testTag = "destination_item_${destination.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = onClick,
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = palette.content.copy(alpha = 0.16f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = palette.content
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.content
                    )
                    destination.username?.let {
                        Text(
                            text = "@$it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.content.copy(alpha = 0.8f)
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
                            com.telegramdrive.uploader.R.string.unpin_destination
                        } else {
                            com.telegramdrive.uploader.R.string.pin_destination
                        }
                    ),
                    tint = if (isPinned) {
                        palette.content
                    } else {
                        palette.content.copy(alpha = 0.6f)
                    }
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(com.telegramdrive.uploader.R.string.selected),
                    tint = palette.content,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
