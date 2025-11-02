// FILE: app/src/main/java/com/smartfit/data/sensors/StepCounterService.kt

package com.smartfit.data.sensors

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.smartfit.MainActivity
import com.smartfit.R
import com.smartfit.data.local.database.SmartFitDatabase
import com.smartfit.domain.model.StepCount
import kotlinx.coroutines.*
import androidx.core.content.edit

class StepCounterService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var initialStepCount = 0
    private var sessionSteps = 0
    private var lastSavedSteps = 0

    companion object {
        const val CHANNEL_ID = "step_counter_channel"
        const val NOTIFICATION_ID = 1
        private const val PREFS_NAME = "step_counter_prefs"
        private const val KEY_INITIAL_STEPS = "initial_steps"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"

        fun startService(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, StepCounterService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("StepCounterService", "Service created")

        createNotificationChannel()
        startForeground()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepCounterService", "Step sensor registered")
        } else {
            Log.e("StepCounterService", "Step sensor not available")
            stopSelf()
        }

        checkAndResetDaily()
    }

    private fun startForeground() {
        val notification = createNotification(0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your daily steps"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(steps: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText("Today's steps: $steps")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun checkAndResetDaily() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastResetDate = prefs.getLong(KEY_LAST_RESET_DATE, 0)
        val today = getTodayStartTimestamp()

        if (lastResetDate < today) {
            // New day, reset counter
            prefs.edit {
                putInt(KEY_INITIAL_STEPS, 0)
                putLong(KEY_LAST_RESET_DATE, today)
            }
            initialStepCount = 0
            sessionSteps = 0
            lastSavedSteps = 0
            Log.d("StepCounterService", "Daily reset performed")
        } else {
            initialStepCount = prefs.getInt(KEY_INITIAL_STEPS, 0)
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalStepsSinceBoot = it.values[0].toInt()

                if (initialStepCount == 0) {
                    // First reading of the day
                    initialStepCount = totalStepsSinceBoot
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit {
                        putInt(KEY_INITIAL_STEPS, initialStepCount)
                    }
                }

                sessionSteps = totalStepsSinceBoot - initialStepCount

                // Update notification every 1 step
                if (sessionSteps - lastSavedSteps >= 1) {
                    updateNotification(sessionSteps)
                    saveStepsToDatabaseAsync(sessionSteps)
                    lastSavedSteps = sessionSteps
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    private fun updateNotification(steps: Int) {
        val notification = createNotification(steps)
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Check for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun saveStepsToDatabaseAsync(steps: Int) {
        scope.launch {
            try {
                val database = SmartFitDatabase.getDatabase(this@StepCounterService)
                val stepRepository = com.smartfit.data.repository.StepRepository(database.stepCountDao())

                // Check if we already have today's sensor entry
                val existingSteps = stepRepository.getTodaysSensorSteps()

                if (existingSteps != null) {
                    // Update existing entry
                    stepRepository.insertStepCount(
                        StepCount(
                            id = existingSteps.id,
                            steps = steps,
                            date = System.currentTimeMillis(),
                            source = "sensor"
                        )
                    )
                } else {
                    // Create new entry
                    stepRepository.insertStepCount(
                        StepCount(
                            steps = steps,
                            date = System.currentTimeMillis(),
                            source = "sensor"
                        )
                    )
                }

                Log.d("StepCounterService", "Steps saved to database: $steps")
            } catch (e: Exception) {
                Log.e("StepCounterService", "Error saving steps to database", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        scope.cancel()
        Log.d("StepCounterService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}