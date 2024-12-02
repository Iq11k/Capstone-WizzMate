package com.bangkit.wizzmateapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    private val isLoginKey = booleanPreferencesKey("is_login")
    private val tokenKey = stringPreferencesKey("token")
    private val username = stringPreferencesKey("username")

    fun isLogin(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[isLoginKey] ?: false
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[tokenKey]
        }
    }

    fun getusername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[username]
        }
    }

    suspend fun saveLoginStatus(isLogin: Boolean) {
        dataStore.edit { preferences ->
            preferences[isLoginKey] = isLogin
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun saveusername(username: String) {
        dataStore.edit { preferences ->
            preferences[this.username] = username
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences[isLoginKey] = false
            preferences.remove(username)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SessionPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
