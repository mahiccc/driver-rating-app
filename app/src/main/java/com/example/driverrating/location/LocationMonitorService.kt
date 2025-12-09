package com.example.driverrating.location

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

class LocationMonitorService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Thresholds
    private val SPEED_LIMIT_HIGHWAY = 80.0f / 3.6f // 80 km/h in m/s
    private val SPEED_LIMIT_NARROW = 40.0f / 3.6f // 40 km/h in m/s

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    analyzeLocation(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        return START_STICKY
    }

    private fun analyzeLocation(location: Location) {
        val speed = location.speed // m/s
        val speedKmh = speed * 3.6f

        // TODO: Integrate OpenStreetMap (OSM) to get actual road speed limit and width
        // For MVP, we assume a generic limit or check against a mock "narrow lane" geofence
        
        val isNarrowLane = checkIfNarrowLane(location.latitude, location.longitude)

        if (isNarrowLane && speed > SPEED_LIMIT_NARROW) {
            logEvent("OVERSPEEDING_NARROW", "Speeding in narrow lane: $speedKmh km/h")
        } else if (speed > SPEED_LIMIT_HIGHWAY) {
            logEvent("OVERSPEEDING", "High speed detected: $speedKmh km/h")
        }

        // Wrong way detection requires previous bearing and map data (road direction)
        // This is a placeholder for that logic
    }

    private fun checkIfNarrowLane(lat: Double, lon: Double): Boolean {
        // Placeholder: In a real app, query OSM Overpass API or local graph
        // Return true if road width < 5 meters
        return false 
    }

    private fun logEvent(type: String, message: String) {
        Log.w("DriverRating", "[$type] $message")
        val intent = Intent("com.example.driverrating.EVENT_UPDATE")
        intent.putExtra("type", type)
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
