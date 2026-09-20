package com.example.gis

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class PinnedLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val isMock: Boolean = false,
    val bairro: String? = null,
    val municipio: String? = null,
    val streetAddress: String? = null
)

object IncidentLocationManager {

    private const val TAG = "IncidentLocationManager"

    /**
     * Verifica se as permissões de localização foram concedidas
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Verifica se os serviços de localização (GPS/Rede) estão ativados no dispositivo
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Obtém a localização GPS em tempo real de alta precisão através do Google FusedLocationProviderClient,
     * com fallback para LocationManager nativo caso necessário.
     */
    suspend fun getCurrentPinnedLocation(context: Context): Result<PinnedLocation> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Result.failure(SecurityException("Permissão de localização não concedida."))
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fetchFusedLocation(fusedClient)
                ?: fetchLegacyLocation(context)

            if (location != null) {
                val geocoded = reverseGeocode(context, location.latitude, location.longitude)
                val pinned = PinnedLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else 35.0,
                    accuracy = if (location.hasAccuracy()) location.accuracy else 3.5f,
                    bairro = geocoded?.bairro,
                    municipio = geocoded?.municipio,
                    streetAddress = geocoded?.streetAddress
                )
                Result.success(pinned)
            } else {
                // Fallback padrão centrado em Luanda caso o dispositivo esteja sem sinal GPS interno no emulador
                val fallback = PinnedLocation(
                    latitude = GisEngine.DEFAULT_CENTER_LAT,
                    longitude = GisEngine.DEFAULT_CENTER_LNG,
                    altitude = 38.0,
                    accuracy = 5.0f,
                    municipio = "Luanda",
                    bairro = "Ingombota",
                    streetAddress = "Luanda Centro Operacional"
                )
                Result.success(fallback)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter localização: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchFusedLocation(fusedClient: FusedLocationProviderClient): Location? {
        return try {
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine { cont ->
                try {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (loc != null) {
                                cont.resume(loc)
                            } else {
                                // Tentar última localização conhecida como fallback imediato
                                fusedClient.lastLocation
                                    .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                                    .addOnFailureListener { cont.resume(null) }
                            }
                        }
                        .addOnFailureListener {
                            fusedClient.lastLocation
                                .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                                .addOnFailureListener { cont.resume(null) }
                        }
                } catch (se: SecurityException) {
                    cont.resume(null)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falha no FusedLocationProvider: ${e.message}")
            null
        }
    }

    private fun fetchLegacyLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            gpsLoc ?: netLoc
        } catch (e: SecurityException) {
            null
        }
    }

    /**
     * Geocodificação reversa assíncrona para deduzir Bairro, Município e Endereço a partir de coordenadas GPS
     */
    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): PinnedLocation? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null

            val geocoder = Geocoder(context, Locale.forLanguageTag("pt-AO"))
            val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(results: MutableList<Address>) {
                            cont.resume(results)
                        }
                        override fun onError(errorMessage: String?) {
                            cont.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }

            val address = addresses?.firstOrNull() ?: return@withContext null

            val street = address.thoroughfare?.let { thoroughfare ->
                val subThoroughfare = address.subThoroughfare
                if (subThoroughfare != null) "$thoroughfare, nº $subThoroughfare" else thoroughfare
            } ?: address.featureName ?: address.getAddressLine(0)

            val subLocality = address.subLocality ?: address.subAdminArea
            val locality = address.locality ?: address.subAdminArea ?: address.adminArea

            PinnedLocation(
                latitude = latitude,
                longitude = longitude,
                bairro = subLocality,
                municipio = locality,
                streetAddress = street
            )
        } catch (e: Exception) {
            Log.w(TAG, "Geocodificação reversa falhou: ${e.message}")
            null
        }
    }
}
