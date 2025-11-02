// FILE: app/src/main/java/com/smartfit/di/AppContainer.kt

package com.smartfit.di

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.smartfit.data.local.database.SmartFitDatabase
import com.smartfit.data.local.datastore.PreferencesManager
import com.smartfit.data.remote.ApiService
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.MealRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.data.repository.SuggestionRepository
import com.smartfit.data.sensors.StepCounterService
import com.smartfit.domain.model.Suggestion
import com.smartfit.ui.screens.home.HomeViewModel
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

    // PERSONAL API KEY, KINDLY DO NOT MISUSE.
    private val apiKey = "5a4ab41f86msh4ed11a8aa8941adp1f5fecjsne5e3ea08664d"
    private val apiService = ApiService.create(apiKey)

    val activityRepository = ActivityRepository(activityDao)
    val mealRepository = MealRepository(mealDao)
    val stepRepository = StepRepository(stepCountDao)
    val suggestionRepository = SuggestionRepository(apiService, useMockData = apiKey.isEmpty())
    val preferencesManager = PreferencesManager(context)

    // Singleton HomeViewModel to prevent reloading
    var homeViewModel: HomeViewModel? = null
        get() {
            if (field == null) {
                field = HomeViewModel(
                    activityRepository,
                    suggestionRepository,
                    mealRepository,
                    stepRepository,
                    this
                )
            }
            return field
        }

    // Cache for loaded suggestions
    var cachedSuggestions: List<Suggestion> = emptyList()

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