// FILE: app/src/main/java/com/smartfit/di/AppContainer.kt

package com.smartfit.di

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smartfit.BuildConfig
import com.smartfit.data.local.database.SmartFitDatabase
import com.smartfit.data.local.datastore.PreferencesManager
import com.smartfit.data.remote.ApiService
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.MealRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.data.repository.SuggestionRepository
import com.smartfit.data.sensors.StepCounterService
import com.smartfit.domain.model.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manual dependency injection container.
 * Provides singleton instances of repositories and data sources.
 */
class AppContainer(private val context: Context) {
    private val database = SmartFitDatabase.getDatabase(context)
    private val activityDao = database.activityDao()
    private val mealDao = database.mealDao()
    private val stepCountDao = database.stepCountDao()

    // Use BuildConfig instead of hardcoded key
    private val apiKey = BuildConfig.EXERCISEDB_API_KEY
    private val apiService = ApiService.create(apiKey)

    val activityRepository = ActivityRepository(activityDao)
    val mealRepository = MealRepository(mealDao)
    val stepRepository = StepRepository(stepCountDao)
    val suggestionRepository = SuggestionRepository(apiService, useMockData = apiKey.isEmpty())
    val preferencesManager = PreferencesManager(context)

    // Cache for loaded suggestions with timestamp
    var cachedSuggestions: List<Suggestion> = emptyList()
    var lastSuggestionLoadTime: Long = 0

    companion object {
        const val SUGGESTION_CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 hours
    }

    init {
        // Check if step tracking should be started on app launch
        CoroutineScope(Dispatchers.IO).launch {
            val isEnabled = preferencesManager.stepTrackingEnabledFlow.first()
            if (isEnabled) {
                Log.d("AppContainer", "Step tracking enabled, starting service")
                startStepCounterService()
            }
        }
    }

    fun getSuggestionById(id: String): Suggestion? {
        return cachedSuggestions.find { it.id == id }
    }

    fun isSuggestionCacheValid(): Boolean {
        return System.currentTimeMillis() - lastSuggestionLoadTime < SUGGESTION_CACHE_DURATION
    }

    fun startStepCounterService() {
        try {
            Log.d("AppContainer", "Starting step counter service")
            val intent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e("AppContainer", "Error starting step counter service", e)
        }
    }

    fun stopStepCounterService() {
        try {
            Log.d("AppContainer", "Stopping step counter service")
            context.stopService(Intent(context, StepCounterService::class.java))
        } catch (e: Exception) {
            Log.e("AppContainer", "Error stopping step counter service", e)
        }
    }
}