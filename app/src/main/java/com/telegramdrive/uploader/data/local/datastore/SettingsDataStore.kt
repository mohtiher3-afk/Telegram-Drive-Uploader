package com.telegramdrive.uploader.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.telegramdrive.uploader.core.util.PinnedDestinationIds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("theme_preference")
    private val GLOW_COLOR_KEY = stringPreferencesKey("glow_color_preference")
    private val CUSTOM_GLOW_HEX_KEY = stringPreferencesKey("custom_glow_hex")
    private val ONBOARDING_COMPLETED_KEY = stringPreferencesKey("onboarding_completed")
    private val OPENING_COMPLETED_KEY = stringPreferencesKey("opening_completed")
    private val TELEGRAM_STATE_KEY = stringPreferencesKey("telegram_connection_state")
    private val TELEGRAM_USER_ID_KEY = stringPreferencesKey("telegram_user_id")
    private val TELEGRAM_USER_FIRST_NAME_KEY = stringPreferencesKey("telegram_user_first_name")
    private val TELEGRAM_USER_LAST_NAME_KEY = stringPreferencesKey("telegram_user_last_name")
    private val TELEGRAM_USER_USERNAME_KEY = stringPreferencesKey("telegram_user_username")
    private val TELEGRAM_USER_PHONE_KEY = stringPreferencesKey("telegram_user_phone")
    private val PINNED_DESTINATION_IDS_KEY = stringPreferencesKey("pinned_destination_ids")
    private val SELECTED_DESTINATION_ID_KEY = androidx.datastore.preferences.core.longPreferencesKey("selected_destination_id")
    private val SELECTED_DESTINATION_TITLE_KEY = stringPreferencesKey("selected_destination_title")
    private val ACCOUNTS_KEY = stringPreferencesKey("telegram_accounts")
    private val ACTIVE_ACCOUNT_KEY = stringPreferencesKey("active_telegram_account")

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] == "true"
    }

    val openingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[OPENING_COMPLETED_KEY] == "true"
    }

    val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System"
    }

    val glowColorPreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GLOW_COLOR_KEY] ?: "Cobalt"
    }

    val customGlowHex: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_GLOW_HEX_KEY] ?: "B8C4FF"
    }

    val telegramConnectionState: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TELEGRAM_STATE_KEY] ?: "DISCONNECTED"
    }

    val pinnedDestinationIds: Flow<Set<Long>> = context.dataStore.data.map { preferences ->
        PinnedDestinationIds.parse(preferences[PINNED_DESTINATION_IDS_KEY])
    }

    val selectedDestinationId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_DESTINATION_ID_KEY]
    }

    val selectedDestinationTitle: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_DESTINATION_TITLE_KEY]
    }

    val telegramUser: Flow<String?> = context.dataStore.data.map { preferences ->
        val id = preferences[TELEGRAM_USER_ID_KEY] ?: return@map null
        val firstName = preferences[TELEGRAM_USER_FIRST_NAME_KEY] ?: ""
        val lastName = preferences[TELEGRAM_USER_LAST_NAME_KEY] ?: ""
        val username = preferences[TELEGRAM_USER_USERNAME_KEY] ?: ""
        val phone = preferences[TELEGRAM_USER_PHONE_KEY] ?: ""
        "$id|$firstName|$lastName|$username|$phone"
    }

    val accounts: Flow<List<TelegramAccountEntry>> = context.dataStore.data.map { preferences ->
        val accounts = preferences[ACCOUNTS_KEY].orEmpty()
        val active = preferences[ACTIVE_ACCOUNT_KEY]
        decodeAccounts(accounts, active)
    }

    suspend fun addAccount(phone: String, displayName: String) {
        context.dataStore.edit { preferences ->
            val current = decodeAccounts(preferences[ACCOUNTS_KEY], null)
            val key = normalizeAccountKey(phone)
            val existing = current.any { it.key == key }
            if (!existing) {
                val updated = current + TelegramAccountEntry(key, phone, displayName)
                preferences[ACCOUNTS_KEY] = encodeAccounts(updated)
                if (preferences[ACTIVE_ACCOUNT_KEY] == null) {
                    preferences[ACTIVE_ACCOUNT_KEY] = key
                }
            }
        }
    }

    suspend fun setActiveAccount(key: String) {
        context.dataStore.edit { preferences ->
            val current = decodeAccounts(preferences[ACCOUNTS_KEY], null)
            if (current.any { it.key == key }) {
                preferences[ACTIVE_ACCOUNT_KEY] = key
            }
        }
    }

    suspend fun removeAccount(key: String) {
        context.dataStore.edit { preferences ->
            val current = decodeAccounts(preferences[ACCOUNTS_KEY], null)
            val updated = current.filterNot { it.key == key }
            preferences[ACCOUNTS_KEY] = encodeAccounts(updated)
            val active = preferences[ACTIVE_ACCOUNT_KEY]
            if (active == key) {
                val newActive = updated.firstOrNull()?.key
                if (newActive != null) {
                    preferences[ACTIVE_ACCOUNT_KEY] = newActive
                } else {
                    preferences.remove(ACTIVE_ACCOUNT_KEY)
                }
            }
        }
    }

    suspend fun clearActiveAccount() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_ACCOUNT_KEY)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed.toString()
        }
    }

    suspend fun setOpeningCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[OPENING_COMPLETED_KEY] = completed.toString()
        }
    }

    suspend fun setPinnedDestination(destinationId: Long, pinned: Boolean) {
        context.dataStore.edit { preferences ->
            val current = PinnedDestinationIds.parse(preferences[PINNED_DESTINATION_IDS_KEY])
            val updated = if (pinned) current + destinationId else current - destinationId
            preferences[PINNED_DESTINATION_IDS_KEY] = PinnedDestinationIds.encode(updated)
        }
    }

    suspend fun setSelectedDestination(destinationId: Long, title: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_DESTINATION_ID_KEY] = destinationId
            preferences[SELECTED_DESTINATION_TITLE_KEY] = title
        }
    }

    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setGlowColorPreference(glowColor: String) {
        context.dataStore.edit { preferences ->
            preferences[GLOW_COLOR_KEY] = glowColor
        }
    }

    suspend fun setCustomGlowHex(hex: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_GLOW_HEX_KEY] = hex
        }
    }

    suspend fun saveCustomGlowColorPreferences(hex: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_GLOW_HEX_KEY] = hex
            preferences[GLOW_COLOR_KEY] = "Custom"
        }
    }

    suspend fun resetGlowColorPreferences() {
        context.dataStore.edit { preferences ->
            preferences[GLOW_COLOR_KEY] = "Cobalt"
            preferences[CUSTOM_GLOW_HEX_KEY] = "B8C4FF"
        }
    }

    suspend fun setTelegramConnectionState(state: String) {
        context.dataStore.edit { preferences ->
            preferences[TELEGRAM_STATE_KEY] = state
        }
    }

    suspend fun saveTelegramUser(id: Long, firstName: String, lastName: String?, username: String?, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[TELEGRAM_USER_ID_KEY] = id.toString()
            preferences[TELEGRAM_USER_FIRST_NAME_KEY] = firstName
            preferences[TELEGRAM_USER_LAST_NAME_KEY] = lastName ?: ""
            preferences[TELEGRAM_USER_USERNAME_KEY] = username ?: ""
            preferences[TELEGRAM_USER_PHONE_KEY] = phone
        }
    }

    suspend fun clearTelegramUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(TELEGRAM_USER_ID_KEY)
            preferences.remove(TELEGRAM_USER_FIRST_NAME_KEY)
            preferences.remove(TELEGRAM_USER_LAST_NAME_KEY)
            preferences.remove(TELEGRAM_USER_USERNAME_KEY)
            preferences.remove(TELEGRAM_USER_PHONE_KEY)
        }
    }

    suspend fun clearTelegramSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TELEGRAM_USER_ID_KEY)
            preferences.remove(TELEGRAM_USER_FIRST_NAME_KEY)
            preferences.remove(TELEGRAM_USER_LAST_NAME_KEY)
            preferences.remove(TELEGRAM_USER_USERNAME_KEY)
            preferences.remove(TELEGRAM_USER_PHONE_KEY)
            preferences.remove(PINNED_DESTINATION_IDS_KEY)
            preferences.remove(SELECTED_DESTINATION_ID_KEY)
            preferences.remove(SELECTED_DESTINATION_TITLE_KEY)
            preferences[TELEGRAM_STATE_KEY] = "DISCONNECTED"
        }
    }

    companion object {
        fun normalizeAccountKey(value: String): String = value.trim().replace("+", "").replace(" ", "")

        private fun encodeAccounts(accounts: List<TelegramAccountEntry>): String =
            accounts.joinToString("|") { "${it.key}~${it.phone}~${it.displayName}" }

        private fun decodeAccounts(raw: String?, activeKey: String?): List<TelegramAccountEntry> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split("|").mapNotNull { row ->
                val parts = row.split("~")
                if (parts.size < 3) return@mapNotNull null
                TelegramAccountEntry(parts[0], parts[1], parts[2], activeKey == parts[0])
            }
        }
    }

}

data class TelegramAccountEntry(
    val key: String,
    val phone: String,
    val displayName: String,
    val isActive: Boolean = false
)
