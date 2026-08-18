package com.telegramdrive.uploader.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    private val TELEGRAM_STATE_KEY = stringPreferencesKey("telegram_connection_state")
    private val TELEGRAM_USER_ID_KEY = stringPreferencesKey("telegram_user_id")
    private val TELEGRAM_USER_FIRST_NAME_KEY = stringPreferencesKey("telegram_user_first_name")
    private val TELEGRAM_USER_LAST_NAME_KEY = stringPreferencesKey("telegram_user_last_name")
    private val TELEGRAM_USER_USERNAME_KEY = stringPreferencesKey("telegram_user_username")
    private val TELEGRAM_USER_PHONE_KEY = stringPreferencesKey("telegram_user_phone")

    val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System"
    }

    val telegramConnectionState: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TELEGRAM_STATE_KEY] ?: "DISCONNECTED"
    }

    val telegramUser: Flow<String?> = context.dataStore.data.map { preferences ->
        val id = preferences[TELEGRAM_USER_ID_KEY] ?: return@map null
        val firstName = preferences[TELEGRAM_USER_FIRST_NAME_KEY] ?: ""
        val lastName = preferences[TELEGRAM_USER_LAST_NAME_KEY] ?: ""
        val username = preferences[TELEGRAM_USER_USERNAME_KEY] ?: ""
        val phone = preferences[TELEGRAM_USER_PHONE_KEY] ?: ""
        "$id|$firstName|$lastName|$username|$phone"
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
