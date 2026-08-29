package com.telegramdrive.uploader.feature.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.telegramdrive.uploader.BuildConfig

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay
import com.telegramdrive.uploader.core.ui.theme.GlowColorPreset
import com.telegramdrive.uploader.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onConnectClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(canPostUploadNotifications(context)) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // Trigger update of cache size when screen is opened
    LaunchedEffect(Unit) {
        viewModel.updateCacheSize()
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text(stringResource(com.telegramdrive.uploader.R.string.logout_dialog_title)) },
            text = { Text(stringResource(com.telegramdrive.uploader.R.string.logout_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logoutTelegram()
                        showLogoutConfirmation = false
                    },
                    modifier = Modifier.testTag("logout_confirm_ok_button")
                ) {
                    Text(stringResource(com.telegramdrive.uploader.R.string.log_out))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmation = false },
                    modifier = Modifier.testTag("logout_confirm_cancel_button")
                ) {
                    Text(stringResource(com.telegramdrive.uploader.R.string.cancel))
                }
            },
            modifier = Modifier.testTag("logout_confirm_dialog")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.telegramdrive.uploader.R.string.settings)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Appearance Section
            SettingsSection(
                icon = Icons.Default.Palette,
                title = stringResource(com.telegramdrive.uploader.R.string.appearance)
            ) {
                val themes = listOf(
                    "System" to com.telegramdrive.uploader.R.string.theme_system,
                    "Light" to com.telegramdrive.uploader.R.string.theme_light,
                    "Dark" to com.telegramdrive.uploader.R.string.theme_dark
                )
                themes.forEach { (themeKey, themeLabelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.theme == themeKey,
                                onClick = { viewModel.setTheme(themeKey) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp)
                            .testTag("settings_theme_${themeKey.lowercase()}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(themeLabelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        RadioButton(
                            selected = uiState.theme == themeKey,
                            onClick = null
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(com.telegramdrive.uploader.R.string.glow_colors),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(com.telegramdrive.uploader.R.string.glow_colors_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                GlowColorPreset.entries.forEach { preset ->
                    val selected = uiState.glowColor == preset.storageValue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { viewModel.setGlowColor(preset) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 10.dp)
                            .testTag("settings_glow_${preset.storageValue.lowercase()}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(20.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = preset.swatchColor(uiState.customGlowHex)
                            ) {}
                            Text(
                                text = stringResource(glowColorLabelRes(preset)),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        RadioButton(selected = selected, onClick = null)
                    }
                }
                if (uiState.glowColor == GlowColorPreset.CUSTOM.storageValue) {
                    Spacer(modifier = Modifier.height(6.dp))
                    GlowColorEditor(
                        savedHex = uiState.customGlowHex,
                        onSave = viewModel::saveCustomGlowColor,
                        onReset = viewModel::resetGlowColors
                    )
                } else {
                    TextButton(
                        onClick = viewModel::resetGlowColors,
                        modifier = Modifier.testTag("reset_glow_colors_button")
                    ) {
                        Text(stringResource(com.telegramdrive.uploader.R.string.reset_glow_colors))
                    }
                }
            }

            // 2. Storage Section
            SettingsSection(
                icon = Icons.Default.Storage,
                title = stringResource(com.telegramdrive.uploader.R.string.storage_cache)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.thumbnail_cache_size),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.cacheSize,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("cache_size_text")
                        )
                    }
                    Button(
                        onClick = { viewModel.clearCache() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.testTag("clear_cache_button")
                    ) {
                        Text(stringResource(com.telegramdrive.uploader.R.string.clear_cache))
                    }
                }
            }

            // 3. Upload Config (Disabled Placeholders)
            SettingsSection(
                icon = Icons.Default.UploadFile,
                title = stringResource(com.telegramdrive.uploader.R.string.upload_settings)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.auto_retry_failed_uploads),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.auto_retry_failed_uploads_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = null,
                            enabled = false
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.upload_only_wifi),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.preserve_cellular_data),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = false,
                            onCheckedChange = null,
                            enabled = false
                        )
                    }
                }
            }

            SettingsSection(
                icon = Icons.Default.Notifications,
                title = stringResource(com.telegramdrive.uploader.R.string.upload_notifications)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(com.telegramdrive.uploader.R.string.upload_notifications_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            if (notificationsEnabled) {
                                com.telegramdrive.uploader.R.string.upload_notifications_enabled
                            } else {
                                com.telegramdrive.uploader.R.string.upload_notifications_disabled
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (notificationsEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Button(
                                onClick = {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                modifier = Modifier.testTag("enable_upload_notifications_button")
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.allow_upload_notifications))
                            }
                        }
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                )
                            },
                            modifier = Modifier.testTag("open_notification_settings_button")
                        ) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.open_notification_settings))
                        }
                    }
                }
            }

            // 4. Telegram Account Integration
            SettingsSection(
                icon = Icons.AutoMirrored.Filled.Send,
                title = stringResource(com.telegramdrive.uploader.R.string.telegram_integration),
                modifier = Modifier.testTag("telegram_integration_section")
            ) {
                if (uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED) {
                    val user = uiState.telegramUser
                    val name = if (user != null) "${user.firstName} ${user.lastName ?: ""}".trim() else "User"
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("telegram_status_connected"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.status), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(stringResource(com.telegramdrive.uploader.R.string.connected), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.account_name), style = MaterialTheme.typography.bodyLarge)
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                        user?.username?.let { username ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.username), style = MaterialTheme.typography.bodyLarge)
                                Text("@$username", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        user?.phoneNumber?.let { phone ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.phone_number), style = MaterialTheme.typography.bodyLarge)
                                Text(phone, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showLogoutConfirmation = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("telegram_logout_button")
                        ) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.log_out))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("telegram_status_disconnected"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(com.telegramdrive.uploader.R.string.telegram_account),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(com.telegramdrive.uploader.R.string.disconnected),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(onClick = onConnectClick) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.connect))
                        }
                    }
                }
            }

            // 5. Diagnostics Section
            var showDiagnosticLogs by remember { mutableStateOf(false) }
            val diagnosticEvents by DiagnosticsManager.events.collectAsStateWithLifecycle()
            val clipboardManager = LocalClipboardManager.current

            SettingsSection(
                icon = Icons.Default.BugReport,
                title = stringResource(com.telegramdrive.uploader.R.string.diagnostics_privacy_logs),
                modifier = Modifier.testTag("diagnostics_section")
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.developer_logging),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.show_sanitized_logs),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showDiagnosticLogs,
                            onCheckedChange = { showDiagnosticLogs = it },
                            modifier = Modifier.testTag("diagnostic_logs_switch")
                        )
                    }

                    if (showDiagnosticLogs) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val exportedText = DiagnosticsManager.exportDiagnostics()
                                    clipboardManager.setText(AnnotatedString(exportedText))
                                    Toast.makeText(context, com.telegramdrive.uploader.R.string.sanitized_logs_copied, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("copy_logs_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.copy_logs))
                            }
                            
                            Button(
                                onClick = {
                                    DiagnosticsManager.clearDiagnostics()
                                    Toast.makeText(context, com.telegramdrive.uploader.R.string.logs_cleared, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("clear_logs_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.clear_logs))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            if (diagnosticEvents.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.no_logs_recorded),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                val latestEvents = diagnosticEvents.takeLast(100).reversed()
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(latestEvents) { event ->
                                        val color = when (event.severity) {
                                            "ERROR" -> MaterialTheme.colorScheme.error
                                            "WARN" -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                        val bgColor = when (event.severity) {
                                            "ERROR" -> MaterialTheme.colorScheme.errorContainer
                                            "WARN" -> MaterialTheme.colorScheme.tertiaryContainer
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        }
                                        val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(bgColor, shape = RoundedCornerShape(4.dp))
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = formattedTime,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "[${event.severity}]",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                                color = color
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = event.message,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (event.incidentId != null) {
                                                    Text(
                                                        text = stringResource(com.telegramdrive.uploader.R.string.incident_id, event.incidentId),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. About
            SettingsSection(
                icon = Icons.Default.Info,
                title = stringResource(com.telegramdrive.uploader.R.string.about)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.application_version),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(com.telegramdrive.uploader.R.string.built_with_compose),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun canPostUploadNotifications(context: android.content.Context): Boolean {
    val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    return runtimePermissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private fun glowColorLabelRes(preset: GlowColorPreset): Int = when (preset) {
    GlowColorPreset.COBALT -> com.telegramdrive.uploader.R.string.glow_color_cobalt
    GlowColorPreset.LIME -> com.telegramdrive.uploader.R.string.glow_color_lime
    GlowColorPreset.CYAN -> com.telegramdrive.uploader.R.string.glow_color_cyan
    GlowColorPreset.VIOLET -> com.telegramdrive.uploader.R.string.glow_color_violet
    GlowColorPreset.CUSTOM -> com.telegramdrive.uploader.R.string.glow_color_custom
}

@Composable
fun SettingsSection(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = AppSpacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.md)
            ) {
                content()
            }
        }
    }
}
