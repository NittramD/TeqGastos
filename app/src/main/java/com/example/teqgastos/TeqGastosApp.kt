package com.example.teqgastos

import android.app.Application
import com.example.teqgastos.data.datastore.UserPreferences
import com.example.teqgastos.data.repository.TeqGastosRepository
import com.example.teqgastos.sync.SyncWorker
import com.example.teqgastos.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TeqGastosApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: TeqGastosRepository by lazy {
        TeqGastosRepository(this)
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(this)
    }

    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Inicializar datos iniciales de TEQMED SpA en segundo plano
        applicationScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        // Programar sincronización automática con WorkManager sujeta a red
        SyncWorker.schedulePeriodicSync(this)
    }
}
