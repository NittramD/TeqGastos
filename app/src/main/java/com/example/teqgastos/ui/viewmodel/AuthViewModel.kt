package com.example.teqgastos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teqgastos.data.datastore.UserPreferences
import com.example.teqgastos.data.datastore.UserSession
import com.example.teqgastos.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    val userSession: StateFlow<UserSession> = userPreferences.userSessionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSession("Gerardo Galán", "17.482.913-K", UserRole.TECNICO)
    )

    fun switchRole(newRole: UserRole) {
        viewModelScope.launch {
            val (name, rut) = when (newRole) {
                UserRole.TECNICO -> Pair("Gerardo Galán", "17.482.913-K")
                UserRole.SUPERVISOR -> Pair("Dra. Marcela Fuentes (Jefatura)", "12.839.210-4")
                UserRole.FINANZAS -> Pair("Roberto Pinochet (Finanzas)", "14.209.811-1")
            }
            userPreferences.saveUserSession(name, rut, newRole)
        }
    }

    class Factory(private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(userPreferences) as T
        }
    }
}
