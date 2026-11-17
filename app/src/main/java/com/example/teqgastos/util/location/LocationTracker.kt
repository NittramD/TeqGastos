package com.example.teqgastos.util.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val ciudad: String? = null,
    val direccion: String? = null,
    val formattedDescription: String
)

object LocationTracker {
    private const val TAG = "LocationTracker"

    /**
     * Verifica si se cuenta con al menos un permiso de ubicación.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Obtiene la ubicación actual del dispositivo usando FusedLocationProviderClient.
     * Retorna [LocationResult] con latitud, longitud, ciudad y formato descriptivo.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationResult? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Permisos de ubicación no otorgados.")
            return@withContext null
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        val location: Location? = withTimeoutOrNull(7000L) {
            suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            continuation.resume(loc)
                        } else {
                            // Fallback a última ubicación conocida si getCurrentLocation retorna null
                            fusedClient.lastLocation
                                .addOnSuccessListener { lastLoc -> continuation.resume(lastLoc) }
                                .addOnFailureListener { continuation.resume(null) }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al consultar FusedLocation: ${e.message}")
                        // Intentar fallback a lastLocation
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc -> continuation.resume(lastLoc) }
                            .addOnFailureListener { continuation.resume(null) }
                    }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        }

        if (location == null) {
            Log.w(TAG, "No fue posible determinar la ubicación GPS.")
            return@withContext null
        }

        val lat = location.latitude
        val lon = location.longitude
        val ciudadYDireccion = resolveGeocode(context, lat, lon)

        val ciudad = ciudadYDireccion.first ?: inferChileanCity(lat, lon)
        val direccion = ciudadYDireccion.second

        val formattedDesc = if (!ciudad.isNullOrBlank()) {
            String.format(Locale.US, "%.4f, %.4f (%s)", lat, lon, ciudad)
        } else {
            String.format(Locale.US, "%.4f, %.4f", lat, lon)
        }

        LocationResult(
            latitude = lat,
            longitude = lon,
            timestamp = location.time.takeIf { it > 0 } ?: System.currentTimeMillis(),
            ciudad = ciudad,
            direccion = direccion,
            formattedDescription = formattedDesc
        )
    }

    /**
     * Resuelve nombre de ciudad y dirección mediante Android Geocoder con tolerancia a fallos offline.
     */
    @Suppress("DEPRECATION")
    private fun resolveGeocode(context: Context, lat: Double, lon: Double): Pair<String?, String?> {
        if (!Geocoder.isPresent()) return Pair(null, null)

        return try {
            val geocoder = Geocoder(context, Locale("es", "CL"))
            val results: List<Address>? = geocoder.getFromLocation(lat, lon, 1)
            if (!results.isNullOrEmpty()) {
                val address = results[0]
                val ciudad = address.locality ?: address.subAdminArea ?: address.adminArea
                val lineaDireccion = address.getAddressLine(0)
                Pair(ciudad, lineaDireccion)
            } else {
                Pair(null, null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder no disponible o error de red: ${e.message}")
            Pair(null, null)
        }
    }

    /**
     * Infiere la ciudad o centro médico de la red TEQMED en caso de estar sin conexión a Internet.
     */
    private fun inferChileanCity(lat: Double, lon: Double): String? {
        val knownCenters = listOf(
            Triple("Concepción", -36.8269, -73.0497),
            Triple("Talcahuano", -36.7249, -73.1168),
            Triple("Los Ángeles", -37.4697, -72.3537),
            Triple("Chillán", -36.6066, -72.1034),
            Triple("Temuco", -38.7359, -72.5904),
            Triple("Santiago", -33.4489, -70.6693),
            Triple("Rancagua", -34.1701, -70.7407)
        )

        for ((name, kLat, kLon) in knownCenters) {
            if (abs(lat - kLat) < 0.25 && abs(lon - kLon) < 0.25) {
                return name
            }
        }
        return null
    }
}
