package com.example.teqgastos.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.teqgastos.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

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
}
