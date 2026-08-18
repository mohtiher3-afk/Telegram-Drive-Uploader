package com.telegramdrive.uploader.feature.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.telegramdrive.uploader.domain.model.TelegramConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onConnectClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // Trigger update of cache size when screen is opened
    LaunchedEffect(Unit) {
        viewModel.updateCacheSize()
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Log out of Telegram") },
            text = { Text("Are you sure you want to log out? This will clear your Telegram session, but your local upload history and statistics will remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logoutTelegram()
                        showLogoutConfirmation = false
                    },
                    modifier = Modifier.testTag("logout_confirm_ok_button")
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmation = false },
                    modifier = Modifier.testTag("logout_confirm_cancel_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("logout_confirm_dialog")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
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
                title = "Appearance"
            ) {
                val themes = listOf("System", "Light", "Dark")
                themes.forEach { themeOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setTheme(themeOption) }
                            .padding(vertical = 12.dp)
                            .testTag("settings_theme_${themeOption.lowercase()}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = themeOption,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        RadioButton(
                            selected = uiState.theme == themeOption,
                            onClick = { viewModel.setTheme(themeOption) }
                        )
                    }
                }
            }

            // 2. Storage Section
            SettingsSection(
                icon = Icons.Default.Storage,
                title = "Storage & Cache"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Thumbnail Cache Size",
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
                        Text("Clear Cache")
                    }
                }
            }

            // 3. Upload Config (Disabled Placeholders)
            SettingsSection(
                icon = Icons.Default.UploadFile,
                title = "Upload Settings"
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
                                text = "Auto-retry failed uploads",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Attempts upload again if it disconnects",
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
                                text = "Upload only on Wi-Fi",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Preserve cellular data",
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

            // 4. Telegram Account Integration
            SettingsSection(
                icon = Icons.AutoMirrored.Filled.Send,
                title = "Telegram Integration",
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
                            Text("Status", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Connected", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Account Name", style = MaterialTheme.typography.bodyLarge)
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                        user?.username?.let { username ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Username", style = MaterialTheme.typography.bodyLarge)
                                Text("@$username", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        user?.phoneNumber?.let { phone ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Phone Number", style = MaterialTheme.typography.bodyLarge)
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
                            Text("Log Out")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConnectClick() }
                            .padding(vertical = 12.dp)
                            .testTag("telegram_status_disconnected"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Telegram Account",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Disconnected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(onClick = onConnectClick) {
                            Text("Connect")
                        }
                    }
                }
            }

            // 5. Diagnostics Section
            var showDiagnosticLogs by remember { mutableStateOf(false) }
            val diagnosticEvents by DiagnosticsManager.events.collectAsStateWithLifecycle()
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current

            SettingsSection(
                icon = Icons.Default.BugReport,
                title = "Diagnostics & Privacy Logs",
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
                                text = "Developer Logging",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Show sanitized background logs",
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
                                    Toast.makeText(context, "Sanitized logs copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("copy_logs_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Copy Logs")
                            }
                            
                            Button(
                                onClick = {
                                    DiagnosticsManager.clearDiagnostics()
                                    Toast.makeText(context, "Logs cleared!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("clear_logs_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Clear Logs")
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
                                        text = "No logs recorded yet.",
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
                                            "ERROR" -> Color(0xFFD32F2F)
                                            "WARN" -> Color(0xFFFFA000)
                                            else -> Color(0xFF1976D2)
                                        }
                                        val bgColor = when (event.severity) {
                                            "ERROR" -> Color(0xFFFDE8E8)
                                            "WARN" -> Color(0xFFFFF9E6)
                                            else -> Color(0xFFE8F0FE)
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
                                                color = Color.Gray
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
                                                        text = "Incident: ${event.incidentId}",
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
                title = "About"
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
                            text = "Application Version",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "1.0.0",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Built with Jetpack Compose & Material 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

