package com.example.teqgastos.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.teqgastos.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

enum class ThemeMode(val label: String) {
    SYSTEM("Seguir Sistema"),
    LIGHT("Modo Claro"),
    DARK("Modo Oscuro")
}

data class UserSession(
    val nombre: String,
    val rut: String,
    val rol: UserRole
)

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_RUT = stringPreferencesKey("user_rut")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_OFFLINE_SIMULATION = booleanPreferencesKey("offline_simulation")
        val KEY_BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
    }

    val userSessionFlow: Flow<UserSession> = context.dataStore.data.map { preferences ->
        val nombre = preferences[KEY_USER_NAME] ?: "Gerardo Galán"
        val rut = preferences[KEY_USER_RUT] ?: "17.482.913-K"
        val rolString = preferences[KEY_USER_ROLE] ?: UserRole.TECNICO.name
        val rol = try {
            UserRole.valueOf(rolString)
        } catch (e: Exception) {
            UserRole.TECNICO
        }
        UserSession(nombre, rut, rol)
    }

    suspend fun saveUserSession(nombre: String, rut: String, rol: UserRole) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = nombre
            preferences[KEY_USER_RUT] = rut
            preferences[KEY_USER_ROLE] = rol.name
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    val offlineSimulationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_OFFLINE_SIMULATION] ?: false
    }

    suspend fun setOfflineSimulation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_OFFLINE_SIMULATION] = enabled
        }
    }

    val biometricsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRICS_ENABLED] ?: true
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRICS_ENABLED] = enabled
        }
    }
}
