package com.example.driverrating.vision

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DriverMonitoringService : Service() {
    // TODO: Implement CameraX to capture driver's face/body
    // TODO: Use TensorFlow Lite (TFLite) with a model trained for:
    // 1. Phone usage detection
    // 2. Drowsiness detection (EAR - Eye Aspect Ratio)
    // 3. Single-hand driving (Pose estimation)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
