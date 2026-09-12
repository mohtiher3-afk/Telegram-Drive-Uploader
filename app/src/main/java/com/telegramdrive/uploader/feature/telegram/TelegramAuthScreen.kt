@file:OptIn(ExperimentalMaterial3Api::class)

package com.telegramdrive.uploader.feature.telegram

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.GradientButton
import com.telegramdrive.uploader.core.ui.components.GradientCard
import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.GradientPalette
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import com.telegramdrive.uploader.domain.model.TelegramError

/** Calm, near-slate gradient for low-emphasis utility tiles. Matches Home's CalmSlate. */
private val CalmSlate = GradientPalette(
    top = Color(0xFF23262E),
    mid = Color(0xFF1E2128),
    base = Color(0xFF181B21),
    glow = Color(0xFF2A2E38)
)

@Composable
fun TelegramAuthScreen(
    onBackClick: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TelegramAuthViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val qrLoginLink by viewModel.qrLoginLink.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    val phoneNumber by viewModel.phoneNumberInput.collectAsStateWithLifecycle()
    val code by viewModel.codeInput.collectAsStateWithLifecycle()
    val password by viewModel.passwordInput.collectAsStateWithLifecycle()

    var showPassword by remember { mutableStateOf(false) }
    val motionEnabled = rememberSystemMotionEnabled()

    // When connection is authorized, notify parent to navigate back or to home
    LaunchedEffect(connectionState) {
        if (connectionState == TelegramConnectionState.AUTHORIZED) {
            onAuthSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.telegramdrive.uploader.R.string.connect_telegram)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("auth_back_button")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.phoneEdge, vertical = AppSpacing.phoneSection),
            contentAlignment = Alignment.TopCenter
        ) {
            // Main content based on state
            AnimatedContent(
                targetState = connectionState,
                transitionSpec = {
                    fadeIn(animationSpec = AppMotion.shortTween<Float>(motionEnabled)) togetherWith
                        fadeOut(animationSpec = AppMotion.shortTween<Float>(motionEnabled))
                },
                label = "auth_screen_transitions"
            ) { state ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    when (state) {
                        TelegramConnectionState.DISCONNECTED -> {
                            TelegramLogo()
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.connect_telegram),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.telegram_auth_description),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (!viewModel.isConfigured) {
                                GradientCard(
                                    palette = GradientPalettes.Sunset,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.telegram_api_not_configured),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GradientPalettes.Sunset.content,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            if (accounts.isNotEmpty()) {
                                AccountSwitcher(
                                    accounts = accounts,
                                    onSwitch = { viewModel.switchAccount(it) },
                                    isProcessing = isProcessing
                                )
                            }
                            GradientButton(
                                text = stringResource(com.telegramdrive.uploader.R.string.connect_telegram),
                                onClick = { viewModel.connect() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("connect_telegram_button"),
                                enabled = !isProcessing,
                                loading = isProcessing,
                                palette = GradientPalettes.Neon
                            )
                        }

                        TelegramConnectionState.CONNECTING -> {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.connecting_telegram),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TelegramConnectionState.WAITING_FOR_PHONE -> {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.telegram_phone_number),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.phone_number_help),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { viewModel.phoneNumberInput.value = it },
                                label = { Text(stringResource(com.telegramdrive.uploader.R.string.phone_number)) },
                                placeholder = { Text("+1234567890") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { viewModel.sendPhoneNumber() }
                                ),
                                enabled = !isProcessing
                            )

                            GradientButton(
                                text = stringResource(com.telegramdrive.uploader.R.string.continue_action),
                                onClick = { viewModel.sendPhoneNumber() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("phone_continue_button"),
                                enabled = phoneNumber.isNotBlank() && !isProcessing,
                                loading = isProcessing
                            )

                            OutlinedButton(
                                onClick = { viewModel.requestQrCodeLogin() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("qr_login_button"),
                                enabled = !isProcessing
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.use_qr))
                            }
                        }

                        TelegramConnectionState.WAITING_FOR_CODE -> {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.enter_verification_code),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.verification_code_sent),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = code,
                                onValueChange = { viewModel.codeInput.value = it },
                                label = { Text(stringResource(com.telegramdrive.uploader.R.string.code)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("code_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { viewModel.sendCode() }
                                ),
                                enabled = !isProcessing
                            )

                            GradientButton(
                                text = stringResource(com.telegramdrive.uploader.R.string.continue_action),
                                onClick = { viewModel.sendCode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("code_continue_button"),
                                enabled = code.isNotBlank() && !isProcessing,
                                loading = isProcessing
                            )
                        }

                        TelegramConnectionState.WAITING_FOR_QR -> {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.scan_qr_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.scan_qr_instructions),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            qrLoginLink?.let { link ->
                                GradientCard(
                                    palette = GradientPalettes.Ocean,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = link,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GradientPalettes.Ocean.content.copy(alpha = 0.9f),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                GradientButton(
                                    text = stringResource(com.telegramdrive.uploader.R.string.copy_qr),
                                    onClick = { clipboardManager.setText(AnnotatedString(link)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("copy_qr_link_button"),
                                    palette = GradientPalettes.Neon
                                )
                            }
                        }

                        TelegramConnectionState.WAITING_FOR_PASSWORD -> {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.two_step_verification),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.enter_cloud_password),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            GradientCard(
                                palette = GradientPalettes.Ocean,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.enter_two_step_password),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GradientPalettes.Ocean.content.copy(alpha = 0.9f)
                                )
                            }

                            OutlinedTextField(
                                value = password,
                                onValueChange = { viewModel.passwordInput.value = it },
                                label = { Text(stringResource(com.telegramdrive.uploader.R.string.password)) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showPassword) stringResource(com.telegramdrive.uploader.R.string.hide_password) else stringResource(com.telegramdrive.uploader.R.string.show_password)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { viewModel.sendPassword() }
                                ),
                                enabled = !isProcessing
                            )

                            GradientButton(
                                text = stringResource(com.telegramdrive.uploader.R.string.continue_action),
                                onClick = { viewModel.sendPassword() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("password_continue_button"),
                                enabled = password.isNotBlank() && !isProcessing,
                                loading = isProcessing
                            )
                        }

                        TelegramConnectionState.ERROR, TelegramConnectionState.CLOSING -> {
                            // Handled locally or transitioning
                            if (state == TelegramConnectionState.CLOSING) {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                                Text(stringResource(com.telegramdrive.uploader.R.string.logging_out))
                            } else {
                                GradientCard(
                                    palette = GradientPalettes.Sunset,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = AppSpacing.sm),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = GradientPalettes.Sunset.content
                                        )
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.authentication_error),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = GradientPalettes.Sunset.content,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = error?.let { stringResource(it.messageResId()) } ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GradientPalettes.Sunset.content.copy(alpha = 0.9f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.testTag("error_text")
                                        )
                                    }
                                }

                                if (error is TelegramError.AppUpdateRequired) {
                                    OutlinedButton(
                                        onClick = { viewModel.requestQrCodeLogin() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("qr_login_recovery_button"),
                                        enabled = !isProcessing
                                    ) {
                                        Text(stringResource(com.telegramdrive.uploader.R.string.continue_qr))
                                    }
                                }

                                GradientButton(
                                    text = stringResource(com.telegramdrive.uploader.R.string.retry_connection),
                                    onClick = { viewModel.connect() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(AppSpacing.touchTarget)
                                        .testTag("retry_connect_button")
                                )
                            }
                        }

                        TelegramConnectionState.AUTHORIZED -> {
                            Text(stringResource(com.telegramdrive.uploader.R.string.authorized_success))
                        }
                    }
                }
            }

            // Snackbar or Floating Error message
            if (error != null && connectionState != TelegramConnectionState.ERROR) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.dismiss), color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .testTag("error_snackbar")
                ) {
                    Text(error?.let { stringResource(it.messageResId()) } ?: "")
                }
            }
        }
    }
}

@Composable
fun TelegramLogo() {
    val palette = GradientPalettes.Neon
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .background(
                brush = Brush.verticalGradient(palette.stops),
                shape = shape
            )
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
    ) {
        // Soft top-light sheen matching the bento tile read.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
                    ),
                    shape = shape
                )
        )
        // Ambient glow wash anchored to the palette glow color.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.glow.copy(alpha = 0.42f), Color.Transparent)
                    ),
                    shape = shape
                )
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            tint = palette.content,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun AccountSwitcher(
    accounts: List<TelegramAccountEntry>,
    onSwitch: (String) -> Unit,
    isProcessing: Boolean
) {
    GradientCard(
        palette = CalmSlate,
        modifier = Modifier.fillMaxWidth(),
        testTag = "account_switcher"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(com.telegramdrive.uploader.R.string.switch_account),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CalmSlate.content
            )
            accounts.forEach { account ->
                Surface(
                    onClick = { if (!isProcessing) onSwitch(account.key) },
                    enabled = !account.isActive && !isProcessing,
                    shape = MaterialTheme.shapes.medium,
                    color = if (account.isActive) {
                        CalmSlate.content.copy(alpha = 0.18f)
                    } else {
                        Color.Transparent
                    },
                    border = if (account.isActive) {
                        BorderStroke(1.dp, CalmSlate.content.copy(alpha = 0.5f))
                    } else {
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_${account.key}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (account.isActive) FontWeight.Bold else FontWeight.Normal,
                                color = CalmSlate.content
                            )
                            Text(
                                text = account.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = CalmSlate.content.copy(alpha = 0.8f)
                            )
                        }
                        if (account.isActive) {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.active),
                                style = MaterialTheme.typography.labelMedium,
                                color = CalmSlate.content.copy(alpha = 0.9f)
                            )
                        } else {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.switch_now),
                                style = MaterialTheme.typography.labelMedium,
                                color = CalmSlate.content.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}
