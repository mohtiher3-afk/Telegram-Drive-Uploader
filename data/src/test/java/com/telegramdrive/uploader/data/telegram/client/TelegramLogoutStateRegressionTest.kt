package com.telegramdrive.uploader.data.telegram.client

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.local.database.AppDatabase
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import org.drinkless.tdlib.TdApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression: a NORMAL TDLib logout emits `AuthorizationStateLoggingOut`
 * between `Ready` and `Closing`. The pre-fix handler had no branch for it, so
 * the state fell into `else -> fail(...)` and the app reported `ERROR`
 * ("Unsupported Telegram authorization state") in the middle of a routine
 * logout — a false auth failure on the session lifecycle.
 *
 * Fail leg (old code): `connectionState` becomes `ERROR` and `error` is set.
 * Pass leg (fixed code): `LoggingOut` maps to `CLOSING` with no error.
 *
 * Direct-construction pattern (no Hilt, no live TDLib): see
 * TdLibRuntimeSmokeTest / SettingsDataStorePersistenceTest honesty contract.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TelegramLogoutStateRegressionTest {

    private lateinit var database: AppDatabase
    private lateinit var client: TelegramClientImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        client = TelegramClientImpl(SettingsDataStore(context), database.uploadDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun loggingOutDuringLogout_isClosing_notError() {
        client.handleAuthorizationState(TdApi.AuthorizationStateLoggingOut())

        assertEquals(
            "AuthorizationStateLoggingOut is a normal logout state and must not map to ERROR",
            TelegramConnectionState.CLOSING,
            client.connectionState.value
        )
        assertNull(
            "A routine logout must not surface a Telegram error",
            client.error.value
        )
    }

    @Test
    fun logoutLifecycle_loggingOutThroughClosed_endsDisconnected() {
        client.handleAuthorizationState(TdApi.AuthorizationStateLoggingOut())
        assertEquals(
            TelegramConnectionState.CLOSING,
            client.connectionState.value
        )

        client.handleAuthorizationState(TdApi.AuthorizationStateClosing())
        assertEquals(
            TelegramConnectionState.CLOSING,
            client.connectionState.value
        )

        client.handleAuthorizationState(TdApi.AuthorizationStateClosed())
        assertEquals(
            TelegramConnectionState.DISCONNECTED,
            client.connectionState.value
        )
        assertNull(client.error.value)
    }
}