package com.quantum_prof.chronotime.ui.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

data class Tilt(val pitch: Float, val roll: Float)

@Composable
fun rememberTilt(): Tilt {
    val context = LocalContext.current
    var tilt by remember { mutableStateOf(Tilt(0f, 0f)) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Simple tilt approximation
                    // x is lateral (rollish)
                    // y is longitudinal (pitchish)
                    // limit values to avoid extreme movement
                    val x = (it.values[0] / 9.8f).coerceIn(-1f, 1f)
                    val y = (it.values[1] / 9.8f).coerceIn(-1f, 1f)
                    tilt = Tilt(y, x)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return tilt
}

