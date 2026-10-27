package com.example.teqgastos.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class IndicadoresValores(
    val valorUf: Double = 38250.0,
    val valorDolar: Double = 945.0,
    val fecha: String = "Hoy",
    val isOnline: Boolean = false
)

object IndicadoresService {
    private const val TAG = "IndicadoresService"
    private const val API_URL = "https://mindicador.cl/api"

    suspend fun obtenerIndicadores(): IndicadoresValores = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val ufValor = json.optJSONObject("uf")?.optDouble("valor", 38250.0) ?: 38250.0
                val dolarValor = json.optJSONObject("dolar")?.optDouble("valor", 945.0) ?: 945.0
                val fecha = json.optJSONObject("uf")?.optString("fecha", "Hoy") ?: "Hoy"

                Log.d(TAG, "Indicadores obtenidos en vivo: UF=$ufValor, USD=$dolarValor")
                return@withContext IndicadoresValores(
                    valorUf = ufValor,
                    valorDolar = dolarValor,
                    fecha = fecha.take(10),
                    isOnline = true
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Modo offline o error al conectar con API mindicador.cl: ${e.message}. Usando valores en caché.")
        } finally {
            connection?.disconnect()
        }

        // Fallback offline resiliente
        return@withContext IndicadoresValores(
            valorUf = 38250.0,
            valorDolar = 945.0,
            fecha = "Caché Local Offline",
            isOnline = false
        )
    }
}
