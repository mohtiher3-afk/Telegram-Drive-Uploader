@file:OptIn(ExperimentalMaterial3Api::class)

package com.telegramdrive.uploader.feature.telegram

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.domain.model.TelegramError

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
    val clipboardManager = LocalClipboardManager.current

    val phoneNumber by viewModel.phoneNumberInput.collectAsStateWithLifecycle()
    val code by viewModel.codeInput.collectAsStateWithLifecycle()
    val password by viewModel.passwordInput.collectAsStateWithLifecycle()

    var showPassword by remember { mutableStateOf(false) }

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
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main content based on state
            AnimatedContent(
                targetState = connectionState,
                transitionSpec = {
                    fadeIn(animationSpec = AppMotion.shortTween()) togetherWith
                        fadeOut(animationSpec = AppMotion.shortTween())
                },
                label = "auth_screen_transitions"
            ) { state ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.telegram_api_not_configured),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Button(
                                onClick = { viewModel.connect() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("connect_telegram_button"),
                                enabled = !isProcessing
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(com.telegramdrive.uploader.R.string.connect_telegram))
                                }
                            }
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

                            Button(
                                onClick = { viewModel.sendPhoneNumber() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("phone_continue_button"),
                                enabled = phoneNumber.isNotBlank() && !isProcessing
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.continue_action))
                                }
                            }

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

                            Button(
                                onClick = { viewModel.sendCode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("code_continue_button"),
                                enabled = code.isNotBlank() && !isProcessing
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.continue_action))
                                }
                            }
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
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = link,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Button(
                                    onClick = { clipboardManager.setText(AnnotatedString(link)) },
                                    modifier = Modifier.fillMaxWidth().testTag("copy_qr_link_button")
                                ) {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.copy_qr))
                                }
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

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.enter_two_step_password),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(12.dp)
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

                            Button(
                                onClick = { viewModel.sendPassword() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("password_continue_button"),
                                enabled = password.isNotBlank() && !isProcessing
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.continue_action))
                                }
                            }
                        }

                        TelegramConnectionState.ERROR, TelegramConnectionState.CLOSING -> {
                            // Handled locally or transitioning
                            if (state == TelegramConnectionState.CLOSING) {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                                Text(stringResource(com.telegramdrive.uploader.R.string.logging_out))
                            } else {
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.authentication_error),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error?.getLocalizedMessage() ?: "An unknown Telegram error occurred. Please try again.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("error_text")
                                )

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

                                Button(
                                    onClick = { viewModel.connect() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("retry_connect_button")
                                ) {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.retry_connection))
                                }
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
                    Text(error?.getLocalizedMessage() ?: "")
                }
            }
        }
    }
}

@Composable
fun TelegramLogo() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(100.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}
