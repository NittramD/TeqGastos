package com.example.teqgastos.data.remote

import android.content.Context
import com.example.teqgastos.data.remote.api.AuthApiService
import com.example.teqgastos.data.remote.interceptor.AuthInterceptor
import com.example.teqgastos.data.remote.interceptor.TokenAuthenticator
import com.example.teqgastos.data.security.TokenManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de red centralizado para TEQMED SpA.
 * Proporciona instancias seguras de Retrofit y OkHttpClient con interceptores de autenticación JWT,
 * renovación automática de tokens y logging estructurado.
 */
object RetrofitClient {

    const val BASE_URL = "https://api.teqmed.cl/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Cliente base para endpoints de autenticación y refresco (sin Authenticator para evitar recursión)
    private val authOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Servicio de autenticación Retrofit para login y refresh tokens.
     */
    val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApiService::class.java)
    }

    /**
     * Crea un cliente OkHttpClient con autenticación Bearer y TokenAuthenticator para peticiones protegidas.
     */
    fun createAuthenticatedOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(tokenManager, authApiService))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Crea una instancia de Retrofit configurada para invocar los endpoints de la API de TEQMED SpA.
     */
    fun createAuthenticatedRetrofit(tokenManager: TokenManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createAuthenticatedOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
