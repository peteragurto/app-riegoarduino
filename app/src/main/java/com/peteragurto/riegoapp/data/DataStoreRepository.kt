package com.peteragurto.riegoapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val ipAddressKey = stringPreferencesKey("ip_address")

    suspend fun saveIpAddress(ipAddress: String) {
        dataStore.edit { preferences ->
            preferences[ipAddressKey] = ipAddress
        }
    }

    fun getIpAddressFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ipAddressKey]
        }
    }
}