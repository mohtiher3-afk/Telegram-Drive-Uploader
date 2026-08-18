#!/bin/bash
BASE="app/src/main/java/com/telegramdrive/uploader"

mkdir -p $BASE/core/common
mkdir -p $BASE/core/datastore
mkdir -p $BASE/core/di
mkdir -p $BASE/core/navigation
mkdir -p $BASE/core/ui/theme
mkdir -p $BASE/core/ui/components
mkdir -p $BASE/core/util

mkdir -p $BASE/data/local
mkdir -p $BASE/data/repository
mkdir -p $BASE/data/model

mkdir -p $BASE/domain/model
mkdir -p $BASE/domain/repository
mkdir -p $BASE/domain/usecase

mkdir -p $BASE/feature/home
mkdir -p $BASE/feature/queue
mkdir -p $BASE/feature/history
mkdir -p $BASE/feature/settings
mkdir -p $BASE/feature/upload

# Move DB
mv $BASE/core/database/* $BASE/data/local/ 2>/dev/null
rm -rf $BASE/core/database

# Move Models
mv $BASE/core/model/* $BASE/domain/model/ 2>/dev/null
rm -rf $BASE/core/model

# Move Repositories
mv $BASE/core/repository/* $BASE/data/repository/ 2>/dev/null
rm -rf $BASE/core/repository

# Remove fake engine
rm -rf $BASE/core/engine

# Move UI Theme
mv $BASE/ui/theme/* $BASE/core/ui/theme/ 2>/dev/null

# Move UI Navigation
mv $BASE/ui/navigation/* $BASE/core/navigation/ 2>/dev/null

# Move Components
mv $BASE/ui/components/* $BASE/core/ui/components/ 2>/dev/null

# Move Screens
mv $BASE/ui/screens/HomeScreen.kt $BASE/feature/home/ 2>/dev/null
mv $BASE/ui/screens/QueueScreen.kt $BASE/feature/queue/ 2>/dev/null
mv $BASE/ui/screens/HistoryScreen.kt $BASE/feature/history/ 2>/dev/null
mv $BASE/ui/screens/SettingsScreen.kt $BASE/feature/settings/ 2>/dev/null
mv $BASE/ui/screens/UploadDetailsScreen.kt $BASE/feature/upload/ 2>/dev/null
mv $BASE/ui/screens/DestinationScreen.kt $BASE/feature/settings/ 2>/dev/null

# Remove old UI folder
rm -rf $BASE/ui

# Create DataStore
cat << 'DS' > $BASE/core/datastore/SettingsDataStore.kt
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

    val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System"
    }

    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}
DS

# Move MainActivity ViewModel and fix packages (I will run a separate regex pass for this)
