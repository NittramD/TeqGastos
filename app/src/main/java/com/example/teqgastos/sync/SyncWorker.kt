package com.example.teqgastos.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Worker en segundo plano resiliente de WorkManager.
 * Emplea política exponencial de reintento para superar zonas sin señal (subterráneos y pabellones de hospitales).
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "SyncWorker ejecutando sincronización mediante SyncEngine (Intento #$runAttemptCount)...")
        val syncEngine = SyncEngine.getInstance(applicationContext)

        val result = syncEngine.sincronizarTodo()

        return if (result.isSuccess) {
            Log.d(TAG, "SyncWorker completado exitosamente: ${result.getOrNull()}")
            Result.success()
        } else {
            val error = result.exceptionOrNull()
            Log.w(TAG, "SyncWorker falló. Reintentando con Backoff exponencial... Motivo: ${error?.message}")
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Log.e(TAG, "Límite de reintentos alcanzado en SyncWorker.")
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "teqgastos_sync_work"
        const val ONE_TIME_WORK_NAME = "teqgastos_one_time_sync"

        /**
         * Programa sincronización periódica cada 15 minutos con reintentos exponenciales y validación de red.
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Sincronización periódica programada con Backoff exponencial.")
        }

        /**
         * Encola una sincronización puntual inmediata cuando se recupera conectividad.
         */
        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            Log.d(TAG, "Sincronización One-Time encolada.")
        }
    }
}
