package com.telegramdrive.uploader.data.telegram.client

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.domain.model.TelegramAccountEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression: the TDLib session directory must be STABLE across process
 * restarts, or the saved login is orphaned and the user is forced to re-login.
 *
 * Pre-fix bug (reproduced by [preFixResolver_picksDifferentDirectoryThanLoginUsed]):
 * 1. First ever connect → no accounts stored → `activeAccountKey = null` →
 *    session saved in `filesDir/tdlib-database-default`.
 * 2. After login, `addAccount()` stores the account (key = normalized phone).
 * 3. Next app start → resolver derived the key FROM the account → TDLib opened
 *    `filesDir/tdlib-database-<phone>` (empty) → `WaitPhoneNumber` → forced
 *    re-login. The session in `tdlib-database-default` was orphaned.
 *
 * The fix records the directory actually used at login (`dirKey`) and resolves
 * it back on the next connect. Fail leg (old code): the verbatim pre-fix
 * resolver diverges from the login directory. Pass leg (fixed code):
 * [resolveTdlibDirectoryKey] returns the recorded directory, legacy rows fall
 * back to the account key (no behavior change for existing installs), and
 * [SettingsDataStore.addAccount] persists the recorded directory.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TelegramSessionDirectoryRegressionTest {

    private val firstLoginDir = "default"
    private val accountKey = "15551234567"

    /** The account row exactly as `addAccount("+1 555 123 4567", …, "default")` stores it. */
    private val accountsAfterFirstLogin = listOf(
        TelegramAccountEntry(
            key = accountKey,
            phone = "+1 555 123 4567",
            displayName = "Alice",
            isActive = true,
            dirKey = firstLoginDir
        )
    )

    @Test
    fun preFixResolver_picksDifferentDirectoryThanLoginUsed() {
        // Verbatim pre-fix connect() logic — this IS the bug being reproduced.
        val legacyRestartDir = accountsAfterFirstLogin.firstOrNull { it.isActive }?.key
            ?: accountsAfterFirstLogin.firstOrNull()?.key

        assertNotEquals(
            "Pre-fix resolver opened a different directory on restart than the " +
                "login used, orphaning the saved session (forced re-login)",
            firstLoginDir,
            legacyRestartDir
        )
    }

    @Test
    fun fixedResolver_returnsDirectoryRecordedAtLogin() {
        assertEquals(
            "The next connect must reopen the directory the session was saved in",
            firstLoginDir,
            resolveTdlibDirectoryKey(accountsAfterFirstLogin)
        )
    }

    @Test
    fun fixedResolver_nullWithoutAccounts_fallsBackToDefaultAtParameterSend() {
        // Fresh install: no accounts yet → sendTdlibParameters() uses "default",
        // which is then recorded by addAccount — same directory on every run.
        assertNull(resolveTdlibDirectoryKey(emptyList()))
    }

    @Test
    fun legacyStoredRow_withoutDirKey_fallsBackToAccountKey() {
        // Rows written before this fix have exactly 3 fields; they must decode
        // to the SAME directory the pre-fix code used (account key), so existing
        // installs do not change behavior.
        val decoded = SettingsDataStore.decodeAccounts(
            "$accountKey~+1 555 123 4567~Alice",
            accountKey
        )

        assertEquals(1, decoded.size)
        assertEquals(accountKey, decoded[0].dirKey)
        assertTrue(decoded[0].isActive)
        assertEquals(accountKey, resolveTdlibDirectoryKey(decoded))
    }

    @Test
    fun encodeDecode_roundTripsDirectoryKey() {
        val encoded = SettingsDataStore.encodeAccounts(accountsAfterFirstLogin)
        val decoded = SettingsDataStore.decodeAccounts(encoded, accountKey)

        assertEquals(1, decoded.size)
        assertEquals(
            "The recorded directory must survive the persistence round trip",
            firstLoginDir,
            decoded[0].dirKey
        )
    }

    @Test
    fun addAccount_persistsDirectoryUsedAtLogin() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SettingsDataStore(context)

        store.addAccount("+1 555 123 4567", "Alice", firstLoginDir)

        val persisted = store.accounts.first().single { it.phone == "+1 555 123 4567" }
        assertEquals(
            "addAccount must record the directory the session was created under",
            firstLoginDir,
            persisted.dirKey
        )
    }
}