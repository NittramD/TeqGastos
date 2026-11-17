package com.example.teqgastos.data.remote.interceptor

import android.util.Log
import com.example.teqgastos.data.remote.api.AuthApiService
import com.example.teqgastos.data.remote.api.RefreshTokenRequest
import com.example.teqgastos.data.security.TokenManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Autenticador de OkHttp que intercepta respuestas HTTP 401 (Unauthorized),
 * ejecuta de forma sincronizada la renovación del JWT mediante el Refresh Token
 * y reintenta la petición original con el nuevo Access Token emitido por TEQMED SpA.
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY_COUNT = 2
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Evitar bucles infinitos de reintentos
        if (responseCount(response) >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Límite máximo de reintentos 401 alcanzado ($MAX_RETRY_COUNT). Abortando reautenticación.")
            return null
        }

        synchronized(this) {
            val currentToken = tokenManager.getAccessToken()
            val requestHeaderToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

            // Si otro hilo ya refrescó el token en paralelo, reintentar de inmediato con el nuevo token
            if (currentToken != null && currentToken != requestHeaderToken) {
                Log.d(TAG, "El token fue refrescado previamente por otra petición concurrente. Reintentando con nuevo token.")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                Log.w(TAG, "No hay refresh token disponible en almacenamiento seguro. Cerrando sesión.")
                tokenManager.clearTokens()
                return null
            }

            Log.i(TAG, "Iniciando renovación sincronizada de token JWT con Refresh Token...")
            try {
                val refreshCall = authApiService.refreshTokenSync(RefreshTokenRequest(refreshToken))
                val refreshResponse = refreshCall.execute()

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val authBody = refreshResponse.body()!!
                    Log.i(TAG, "Renovación de token JWT exitosa. Guardando nuevas credenciales.")
                    tokenManager.saveTokens(
                        accessToken = authBody.accessToken,
                        refreshToken = authBody.refreshToken,
                        expiresInSeconds = authBody.expiresIn
                    )

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${authBody.accessToken}")
                        .build()
                } else {
                    Log.e(TAG, "Fallo al refrescar token (HTTP ${refreshResponse.code()}). Sesión inválida o expirada.")
                    tokenManager.clearTokens()
                    return null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción durante la renovación sincronizada del token: ${e.message}", e)
                return null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
