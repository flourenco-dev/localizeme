package com.fabiolourenco.localizeme.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.support.annotation.RequiresPermission
import com.fabiolourenco.localizeme.model.JourneyLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import timber.log.Timber

/**
 * ViewModel to handle all logic of receiving location updates
 */
class LocationsViewModel(application: Application) : AndroidViewModel(application) {

    // MediatorLiveData can observe other LiveData objects and react on their emissions
    private var observableLocations: MediatorLiveData<ArrayList<JourneyLocation>> = MediatorLiveData()
    private var observableLocation: MediatorLiveData<JourneyLocation> = MediatorLiveData()

    // Location provider to get location updates
    private val provider = LocationServices.getFusedLocationProviderClient(application)
    // Request defining the location updates priority and its desired interval in ms
    private val request = LocationRequest().apply {
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        interval = 1000
    }

    val locations: LiveData<ArrayList<JourneyLocation>>
        get() = observableLocations

    val location: LiveData<JourneyLocation>
        get() = observableLocation

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            // Update current journey list of locations and last location, by creating a new LatLng
            // object with the latitude and longitude from the location
            locationResult?.locations?.forEach {
                val journeyLocation = JourneyLocation(it.latitude, it.longitude, it.altitude, it.speed)
                observableLocations.value?.add(journeyLocation)
                observableLocation.value = journeyLocation
            }
        }
    }

    /**
     * Start receiving location updates
     */
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_FINE_LOCATION"])
    fun startTracking() {
        // Reset values for stored locations and last location, in order to start a new Journey
        observableLocations.value = ArrayList()
        observableLocation.value = null
        try {
            provider.requestLocationUpdates(request, locationCallback, null)
        } catch (e: SecurityException) {
            Timber.e(e, "Location permissions were not accepted")
        }
    }

    /**
     * Stop receiving location updates
     */
    fun stopTracking() {
        provider.removeLocationUpdates(locationCallback)
    }

    /**
     * Method used to lower provider request priority if the app is sent to background, with the
     * objective of lowering battery usage
     */
    fun toBackground() {
        // Consider switching to LocationRequest.PRIORITY_LOW_POWER
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    /**
     * Method used to raise provider request priority if the app comes to foreground, with the
     * objective of improving location accuracy
     */
    fun toForeground() {
        // Consider switching to LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCleared() {
        super.onCleared()
        // Make sure that there are no more location updates when ViewModel is cleared
        stopTracking()
    }
}