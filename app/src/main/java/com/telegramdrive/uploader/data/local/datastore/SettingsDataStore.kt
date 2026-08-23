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
    private val ONBOARDING_COMPLETED_KEY = stringPreferencesKey("onboarding_completed")
    private val TELEGRAM_STATE_KEY = stringPreferencesKey("telegram_connection_state")
    private val TELEGRAM_USER_ID_KEY = stringPreferencesKey("telegram_user_id")
    private val TELEGRAM_USER_FIRST_NAME_KEY = stringPreferencesKey("telegram_user_first_name")
    private val TELEGRAM_USER_LAST_NAME_KEY = stringPreferencesKey("telegram_user_last_name")
    private val TELEGRAM_USER_USERNAME_KEY = stringPreferencesKey("telegram_user_username")
    private val TELEGRAM_USER_PHONE_KEY = stringPreferencesKey("telegram_user_phone")
    private val PINNED_DESTINATION_IDS_KEY = stringPreferencesKey("pinned_destination_ids")

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] == "true"
    }

    val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System"
    }

    val telegramConnectionState: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TELEGRAM_STATE_KEY] ?: "DISCONNECTED"
    }

    val pinnedDestinationIds: Flow<Set<Long>> = context.dataStore.data.map { preferences ->
        PinnedDestinationIds.parse(preferences[PINNED_DESTINATION_IDS_KEY])
    }

    val telegramUser: Flow<String?> = context.dataStore.data.map { preferences ->
        val id = preferences[TELEGRAM_USER_ID_KEY] ?: return@map null
        val firstName = preferences[TELEGRAM_USER_FIRST_NAME_KEY] ?: ""
        val lastName = preferences[TELEGRAM_USER_LAST_NAME_KEY] ?: ""
        val username = preferences[TELEGRAM_USER_USERNAME_KEY] ?: ""
        val phone = preferences[TELEGRAM_USER_PHONE_KEY] ?: ""
        "$id|$firstName|$lastName|$username|$phone"
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed.toString()
        }
    }

    suspend fun setPinnedDestination(destinationId: Long, pinned: Boolean) {
        context.dataStore.edit { preferences ->
            val current = PinnedDestinationIds.parse(preferences[PINNED_DESTINATION_IDS_KEY])
            val updated = if (pinned) current + destinationId else current - destinationId
            preferences[PINNED_DESTINATION_IDS_KEY] = PinnedDestinationIds.encode(updated)
        }
    }

    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
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
}
