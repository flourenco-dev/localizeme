package com.fabiolourenco.localizeme.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.fabiolourenco.localizeme.R
import com.fabiolourenco.localizeme.database.entity.Journey
import com.fabiolourenco.localizeme.model.JourneyLocation
import com.fabiolourenco.localizeme.ui.adapter.JourneyAdapter
import com.fabiolourenco.localizeme.ui.adapter.JourneySelectedCallback
import com.fabiolourenco.localizeme.viewmodel.JourneysViewModel
import com.fabiolourenco.localizeme.viewmodel.LocationsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * This activity displays a map showing the place of the device's current location. It also allows
 * to track and save the device's journey
 */
class MainActivity : AppCompatActivity() {

    // Save the google map reference to operate it
    private var map: GoogleMap? = null
    // Save the current polyline being show to be able to delete it
    private var polyline: Polyline? = null

    // Keep a list of the current tracking locations
    private var currentTracking: ArrayList<JourneyLocation> = ArrayList()
    // Record the start time of a new tracking operation
    private var currentStartTime: Long = 0

    // ViewModel to track Journey database changes
    private lateinit var journeysViewModel: JourneysViewModel
    // ViewModel to get location updates
    private lateinit var locationsViewModel: LocationsViewModel

    // Callback to deal with list (in this case it is a Spinner view) items selected
    private val journeySelectedCallback = JourneySelectedCallback { journey ->
        // Update variable with locations to show
        currentTracking = journey?.getLocations() ?: ArrayList()
        // Draw the selected locations
        addPolyline()
        // Update extra info fields
        altitudeText.text = journey?.getAverageHigh() ?: ""
        velocityText.text = journey?.getAverageSpeed() ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize JourneysViewModel tying it to this Activity
        journeysViewModel = ViewModelProviders.of(this).get(JourneysViewModel::class.java)
        // Get Journey list and get updated when the change
        journeysViewModel.journeys.observe(this, Observer {
            // Create a new adapter instance with the new Journey list
            val adapter = JourneyAdapter(this, it ?: ArrayList(), journeySelectedCallback)
            journeysSpinner.adapter = adapter
            journeysSpinner.onItemSelectedListener = adapter
            // If a tracking is not ongoing, select the last Journey entry
            if (!trackingSwitch.isChecked && adapter.count > 0) {
                journeysSpinner.setSelection(adapter.count - 1)
            }
        })

        // Initialize LocationsViewModel tying it to this Activity
        locationsViewModel = ViewModelProviders.of(this).get(LocationsViewModel::class.java)
        // Build the map
        val mapFragment: MapFragment? = fragmentManager.findFragmentById(R.id.mapFragment) as? MapFragment

        mapFragment?.getMapAsync {
            map = it
            if (requestLocationPermissions()) {
                try {
                    // Enable user location only if permissions are accepted
                    map?.isMyLocationEnabled = true
                    // Apply the default zoom
                    map?.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM))
                } catch (e: SecurityException) {
                    // Don't worry about logging the exception, permissions are guaranteed by requestLocationPermissions()
                    // This try catch block is just used to remove IDE warnings
                }
            }
        }

        // Use switch to start and stop tracking and recording user location updates
        trackingSwitch.setOnClickListener {
            if (trackingSwitch.isChecked) {
                startTracking()
            } else {
                stopTracking()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestLocationPermissions()) {
            try {
                // Permissions may have changed so enable user location only if permissions are accepted
                map?.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                // Don't worry about logging the exception, permissions are guaranteed by requestLocationPermissions()
                // This try catch block is just used to remove IDE warnings
            }
        }
        // If the there was a tracking ongoing get the locations
        if (trackingSwitch.isChecked) {
            currentTracking = locationsViewModel.locations.value ?: ArrayList()
            addPolyline()
        }

        locationsViewModel.toForeground()
    }

    override fun onPause() {
        super.onPause()
        locationsViewModel.toBackground()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermissions()
                } else {
                    // This call requires permission checking, which by now should be accepted
                    if (requestLocationPermissions()) {
                        try {
                            map?.isMyLocationEnabled = true
                            // Apply the default zoom
                            map?.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM))
                        } catch (e: SecurityException) {
                            // Don't worry about logging the exception, permissions are guaranteed by requestLocationPermissions()
                            // This try catch block is just used to remove IDE warnings
                        }
                    }
                }
            }
        }
    }

    private fun requestLocationPermissions() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // In case it has lost permissions and was tracking, stop tracking
            if (trackingSwitch.isChecked) {
                stopTracking()
                trackingSwitch.isChecked = false
            }
            map?.isMyLocationEnabled = false
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_CODE)
            false
        } else {
            true
        }

    private fun startTracking() {
        Toast.makeText(this, "Started tracking...", Toast.LENGTH_SHORT).show()
        if (requestLocationPermissions()) {
            try {
                map?.isMyLocationEnabled = true
                // Apply the default zoom
                map?.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM))
            } catch (e: SecurityException) {
                // Don't worry about logging the exception, permissions are guaranteed by requestLocationPermissions()
                // This try catch block is just used to remove IDE warnings
            }

            // Erase current polyline
            polyline?.remove()
            locationsViewModel.startTracking()
            // Observe updates to last location to update user Journey
            locationsViewModel.location.observe(this, Observer {
                updateLocations(it)
            })

            // Reset start time to the current time of starting a tracking
            currentStartTime = Calendar.getInstance().timeInMillis
            // Hide Journey list while recording a new Journey
            journeysSpinner.visibility = View.INVISIBLE
            extraInfoContainer.visibility = View.INVISIBLE
            // Start over the tracking location list to save the new Journey
            currentTracking = ArrayList()
        }
    }

    private fun stopTracking() {
        Toast.makeText(this, "Stopped tracking!", Toast.LENGTH_SHORT).show()
        locationsViewModel.stopTracking()
        // No more need of observe last location updates
        locationsViewModel.location.removeObservers(this)

        // Just add a new Journey if it has at least two locations, to be able to form a path
        if(currentTracking.size > 1) {
            // Insert the latest recorded Journey in the database
            journeysViewModel.insertJourney(
                    Journey(currentStartTime, Calendar.getInstance().timeInMillis, currentTracking))
        }
        // Show Journey list that will in time contain the lastest entry
        journeysSpinner.visibility = View.VISIBLE
        extraInfoContainer.visibility = View.VISIBLE
    }

    private fun updateLocations(location: JourneyLocation?) {
        location?.let { myLocation ->
            // Add last location to the location list
            currentTracking.add(myLocation)
            // Replace the current polyline with the one that includes the latest location
            addPolyline()
        }
    }

    private fun addPolyline() {
        // Remove old polyline
        polyline?.remove()
        val size = currentTracking.size
        if (size > 1) {
            val polylineOptions = PolylineOptions()
                    .startCap(RoundCap())
                    .endCap(RoundCap())
                    .jointType(JointType.ROUND)
                    .color(DEFAULT_POLYLINE_COLOR)
                    .width(DEFAULT_POLYLINE_WIDTH)
                    .apply {
                        // Add coordinates of all locations in the current/selected Journey
                        currentTracking.forEach {
                            add(LatLng(it.latitude, it.longitude))
                        }
                    }
            // Draw polyline on the map and save its instance
            polyline = map?.addPolyline(polylineOptions)
        }
        // Center the map in latest location
        if (size > 0) {
            val latLng = LatLng(currentTracking[size - 1].latitude, currentTracking[size - 1].longitude)
            val centerMyLocation = CameraUpdateFactory.newLatLng(latLng)
            map?.moveCamera(centerMyLocation)
            // Reapply the default zoom -> Consider removing
            map?.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM))
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_CODE = 0
        private const val DEFAULT_ZOOM = 20f
        private const val DEFAULT_POLYLINE_WIDTH = 12f
        private const val DEFAULT_POLYLINE_COLOR = 0xffF57F17.toInt()
    }
}
