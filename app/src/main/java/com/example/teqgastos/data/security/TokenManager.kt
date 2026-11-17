package com.example.teqgastos.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

data class JwtPayload(
    val rut: String? = null,
    val nombre: String? = null,
    val rol: String? = null,
    val exp: Long? = null
)

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        createEncryptedPrefs(context, masterKey)
    } catch (e: Exception) {
        // En caso de corrupción o cambio de llaves en KeyStore, recrear almacenamiento seguro
        context.deleteSharedPreferences(PREFS_FILENAME)
        createEncryptedPrefs(context, masterKey)
    }

    private fun createEncryptedPrefs(context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val PREFS_FILENAME = "teqgastos_secure_tokens"
        private const val KEY_ACCESS_TOKEN = "jwt_access_token"
        private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
        private const val KEY_EXPIRES_AT = "jwt_expires_at"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Guarda los tokens JWT y calcula el timestamp absoluto de expiración.
     */
    fun saveTokens(accessToken: String, refreshToken: String, expiresInSeconds: Long) {
        val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    /**
     * Obtiene el token de acceso actual en memoria segura.
     */
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    /**
     * Obtiene el token de refresco actual.
     */
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    /**
     * Verifica si el token ha expirado o está próximo a expirar (margen de 30s de gracia).
     */
    fun isTokenExpired(): Boolean {
        val token = getAccessToken() ?: return true
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val now = System.currentTimeMillis()

        if (expiresAt > 0L) {
            return now >= (expiresAt - 30_000L)
        }

        // Si no está registrado KEY_EXPIRES_AT, inspeccionar el claim 'exp' del JWT directamente
        val payload = decodeJwtPayload(token)
        val expSeconds = payload?.exp ?: return true
        val expMillis = expSeconds * 1000L
        return now >= (expMillis - 30_000L)
    }

    /**
     * Elimina todos los tokens de la sesión actual en EncryptedSharedPreferences.
     */
    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    /**
     * Decodifica el payload JWT (Claims en Base64) extrayendo rut, nombre, rol y timestamp exp.
     */
    fun decodeJwtPayload(jwt: String? = getAccessToken()): JwtPayload? {
        if (jwt.isNullOrBlank()) return null
        val parts = jwt.split(".")
        if (parts.size < 2) return null

        return try {
            val payloadBase64 = parts[1]
            val decodedBytes = Base64.decode(
                payloadBase64,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val jsonString = String(decodedBytes, Charsets.UTF_8)
            val json = JSONObject(jsonString)

            JwtPayload(
                rut = json.optString("rut").takeIf { it.isNotBlank() }
                    ?: json.optString("sub").takeIf { it.isNotBlank() },
                nombre = json.optString("nombre").takeIf { it.isNotBlank() }
                    ?: json.optString("name").takeIf { it.isNotBlank() },
                rol = json.optString("rol").takeIf { it.isNotBlank() }
                    ?: json.optString("role").takeIf { it.isNotBlank() },
                exp = if (json.has("exp")) json.optLong("exp") else null
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generador de tokens JWT válidos estructurados para modo offline/demo corporativo TEQMED SpA.
     */
    fun generateDemoJwt(
        rut: String,
        nombre: String,
        rol: String,
        expiresInSeconds: Long = 3600
    ): Pair<String, String> {
        val header = Base64.encodeToString(
            JSONObject(mapOf("alg" to "HS256", "typ" to "JWT")).toString().toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        ).trim()

        val exp = (System.currentTimeMillis() / 1000) + expiresInSeconds
        val payloadMap = mapOf(
            "sub" to rut,
            "rut" to rut,
            "nombre" to nombre,
            "rol" to rol,
            "exp" to exp,
            "iss" to "teqmed-auth-service"
        )
        val payload = Base64.encodeToString(
            JSONObject(payloadMap).toString().toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        ).trim()

        val signature = Base64.encodeToString(
            "teqmed_sp_signature_secret_key_2026".toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        ).trim()

        val accessToken = "$header.$payload.$signature"
        val refreshToken = "refresh_${java.util.UUID.randomUUID()}"
        return Pair(accessToken, refreshToken)
    }
}
