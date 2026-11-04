package com.oceanwalls.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "oceanwalls_settings")

class SettingsStore(private val context: Context) {

    private val keyWifiOnly = booleanPreferencesKey("wifi_only_downloads")
    private val keyLastRefresh = longPreferencesKey("last_home_refresh")

    val wifiOnlyDownloads: Flow<Boolean> = context.dataStore.data.map { it[keyWifiOnly] ?: false }
    val lastHomeRefresh: Flow<Long> = context.dataStore.data.map { it[keyLastRefresh] ?: 0L }

    suspend fun setWifiOnlyDownloads(value: Boolean) {
        context.dataStore.edit { it[keyWifiOnly] = value }
    }

    suspend fun markHomeRefreshed(epochMillis: Long) {
        context.dataStore.edit { it[keyLastRefresh] = epochMillis }
    }
}
