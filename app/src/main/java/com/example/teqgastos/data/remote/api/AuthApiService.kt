package com.example.teqgastos.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    @SerializedName("rut") val rut: String,
    @SerializedName("clave") val clave: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class UsuarioResponse(
    @SerializedName("rut") val rut: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("rol") val rol: String
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("tokenType") val tokenType: String = "Bearer",
    @SerializedName("usuario") val usuario: UsuarioResponse? = null
)

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    @POST("api/v1/auth/refresh")
    fun refreshTokenSync(
        @Body request: RefreshTokenRequest
    ): Call<AuthResponse>
}
