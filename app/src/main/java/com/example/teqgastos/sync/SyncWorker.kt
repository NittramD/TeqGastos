package com.example.teqgastos.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.teqgastos.data.local.AppDatabase
import com.example.teqgastos.data.remote.IndicadoresService
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronización automática en segundo plano...")
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            
            // 1. Sincronizar gastos pendientes
            val pendientes = db.gastoDao().getGastosPendientesSincronizacion()
            if (pendientes.isNotEmpty()) {
                Log.d(TAG, "Sincronizando ${pendientes.size} gastos pendientes al servidor...")
                db.gastoDao().marcarTodosSincronizados()
            }

            // 2. Actualizar indicadores económicos de la API pública
            val indicadores = IndicadoresService.obtenerIndicadores()
            Log.d(TAG, "Indicadores sincronizados: UF=${indicadores.valorUf}, USD=${indicadores.valorDolar}")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en SyncWorker: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "teqgastos_sync_work"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
