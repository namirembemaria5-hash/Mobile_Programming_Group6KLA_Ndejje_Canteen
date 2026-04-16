package com.ndejje.ndejjecanteen.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        return try {
            // Try to get the current location with high accuracy
            val cancellationTokenSource = CancellationTokenSource()
            val result = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
            result
        } catch (e: Exception) {
            // Fall back to last known location
            try {
                fusedLocationClient.lastLocation.await()
            } catch (e2: Exception) {
                null
            }
        }
    }
}
