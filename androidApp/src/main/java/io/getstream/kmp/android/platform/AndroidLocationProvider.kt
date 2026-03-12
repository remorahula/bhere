package io.getstream.kmp.android.platform

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LatLng(val lat: Double, val lng: Double, val accuracy: Double?)

class AndroidLocationProvider(private val context: Context) {
    @SuppressLint("MissingPermission")
    suspend fun current(): LatLng = suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc == null) cont.resumeWithException(IllegalStateException("No location"))
                else cont.resume(LatLng(loc.latitude, loc.longitude, loc.accuracy.toDouble()))
            }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
}