package com.example.teqgastos.data.remote.interceptor

import com.example.teqgastos.data.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que adjunta automáticamente el token JWT Bearer en las cabeceras
 * HTTP para todas las peticiones salientes autenticadas de TEQMED SpA.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "TeqGastos-Android/1.0 (TEQMED SpA)")

        // Si ya contiene cabecera Authorization (ej. custom), no sobreescribir
        if (originalRequest.header("Authorization") == null) {
            val token = tokenManager.getAccessToken()
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
