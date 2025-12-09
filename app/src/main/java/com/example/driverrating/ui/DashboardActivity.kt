package com.example.driverrating.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.driversafetysdk.location.LocationMonitorService
import com.example.driversafetysdk.sensor.SensorAnalysisService

class DashboardActivity : ComponentActivity() {

    private val eventList = mutableStateListOf<String>()
    private var driverScore by mutableIntStateOf(100)

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent?.getStringExtra("type") ?: return
            val message = intent.getStringExtra("message") ?: return
            
            eventList.add(0, "[$type] $message")
            
            // Simple scoring logic: deduct points for violations
            driverScore = (driverScore - 5).coerceAtLeast(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start Services
        startForegroundService(Intent(this, SensorAnalysisService::class.java))
        startForegroundService(Intent(this, LocationMonitorService::class.java))

        // Register Receiver
        ContextCompat.registerReceiver(
            this,
            eventReceiver,
            IntentFilter("com.example.driverrating.EVENT_UPDATE"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setContent {
            MaterialTheme {
                DashboardScreen(driverScore, eventList)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(eventReceiver)
    }
}

@Composable
fun DashboardScreen(score: Int, events: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Driver Rating",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (score > 80) Color(0xFFE8F5E9) else if (score > 50) Color(0xFFFFF3E0) else Color(0xFFFFEBEE)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Text(
                    text = "$score",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (score > 80) Color(0xFF2E7D32) else if (score > 50) Color(0xFFEF6C00) else Color(0xFFC62828)
                )
                Text(text = "Safety Score")
            }
        }

        Text(
            text = "Recent Alerts",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                AlertItem(event)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val context = androidx.compose.ui.platform.LocalContext.current
        Button(
            onClick = {
                val intent = Intent("com.example.driverrating.EVENT_UPDATE")
                intent.putExtra("type", "SIMULATION")
                intent.putExtra("message", "Simulated Rash Driving Alert!")
                context.sendBroadcast(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Simulate Alert (Demo Mode)")
        }
    }
}

@Composable
fun AlertItem(message: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}
