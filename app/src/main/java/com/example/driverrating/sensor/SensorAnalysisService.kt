package com.example.driverrating.sensor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

class SensorAnalysisService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    // Thresholds (Calibration needed for real-world usage)
    private val HARD_BRAKING_THRESHOLD = 12.0f // m/s^2 negative Y
    private val RASH_ACCEL_THRESHOLD = 10.0f // m/s^2 variance
    private val ZIG_ZAG_THRESHOLD = 2.0f // rad/s Z-axis rotation

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> analyzeAccelerometer(event.values)
            Sensor.TYPE_GYROSCOPE -> analyzeGyroscope(event.values)
        }
    }

    private fun analyzeAccelerometer(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // Detect Sudden Braking (assuming phone is mounted vertically facing forward)
        // Y-axis is longitudinal. Negative Y is braking.
        // Note: Orientation logic needs to be robust in production (using rotation vector).
        if (y < -HARD_BRAKING_THRESHOLD) {
            logEvent("SUDDEN_BRAKING", "Hard braking detected: $y")
        }

        // Detect Rash Acceleration/Cornering
        val totalForce = sqrt(x*x + y*y + z*z)
        if (abs(totalForce - 9.8f) > RASH_ACCEL_THRESHOLD) {
             logEvent("RASH_DRIVING", "High G-force detected: $totalForce")
        }
    }

    private fun analyzeGyroscope(values: FloatArray) {
        val zRotation = values[2] // Yaw

        // Detect Zig-Zag (Rapid changes in yaw)
        if (abs(zRotation) > ZIG_ZAG_THRESHOLD) {
            logEvent("ZIG_ZAG", "Zig-zag driving detected: $zRotation")
        }
    }

    private fun logEvent(type: String, message: String) {
        Log.w("DriverRating", "[$type] $message")
        // TODO: Broadcast this event to UI or save to DB
        val intent = Intent("com.example.driverrating.EVENT_UPDATE")
        intent.putExtra("type", type)
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
