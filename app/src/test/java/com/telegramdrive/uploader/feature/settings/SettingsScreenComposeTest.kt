package com.telegramdrive.uploader.feature.settings

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.BuildConfig
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class SettingsScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `system theme is selected by default`() {
        setContent()

        composeRule.onNodeWithTag("settings_theme_system").assertIsSelected()
        composeRule.onNodeWithTag("settings_theme_dark").assertIsNotSelected()
    }

    @Test
    fun `cache size renders initial value`() {
        setContent()

        composeRule.onNodeWithTag("cache_size_text").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("cache_size_text").assertTextEquals("0 B")
    }

    @Test
    fun `telegram section shows disconnected state with connect button`() {
        setContent()

        composeRule.onNodeWithTag("telegram_status_disconnected").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("telegram_status_connected").assertDoesNotExist()
    }

    @Test
    fun `notification enable button is visible when permission is missing`() {
        setContent()

        composeRule.onNodeWithTag("enable_upload_notifications_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("open_notification_settings_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `diagnostics switch reveals log actions`() {
        setContent()

        composeRule.onNodeWithTag("diagnostic_logs_switch").performScrollTo().performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("copy_logs_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("clear_logs_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("share_logs_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `about section shows current app version`() {
        setContent()

        composeRule.onNode(hasText(BuildConfig.VERSION_NAME)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `disabled upload config switches are present`() {
        setContent()

        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.onNode(hasText(context.getString(com.telegramdrive.uploader.R.string.auto_retry_failed_uploads)))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNode(hasText(context.getString(com.telegramdrive.uploader.R.string.upload_only_wifi)))
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun setContent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = SettingsViewModel(
            settingsDataStore = SettingsDataStore(context),
            telegramRepository = FakeTelegramRepository(),
            context = context
        )
        composeRule.setContent {
            SettingsScreen(onConnectClick = {}, viewModel = viewModel)
        }
        composeRule.waitForIdle()
    }

    private class FakeTelegramRepository : TelegramRepository {
        override val connectionState = MutableStateFlow(TelegramConnectionState.DISCONNECTED)
        override val currentUser = MutableStateFlow<TelegramUser?>(null)
        override val error = MutableStateFlow<TelegramError?>(null)
        override val qrLoginLink = MutableStateFlow<String?>(null)
        override val accounts = flowOf(emptyList<TelegramAccountEntry>())
        override val isConfigured = true

        override suspend fun connect() {}
        override suspend fun sendPhoneNumber(phoneNumber: String) {}
        override suspend fun sendCode(code: String) {}
        override suspend fun sendPassword(password: String) {}
        override suspend fun requestQrCodeLogin() {}
        override suspend fun logout() {}
        override suspend fun switchAccount(accountKey: String) {}
        override fun clearError() {}
        override fun getDestinations(query: String): Flow<List<TelegramDestination>> =
            flowOf(emptyList())
    }
}