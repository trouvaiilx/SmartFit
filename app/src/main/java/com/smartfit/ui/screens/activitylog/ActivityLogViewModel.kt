// FILE: app/src/main/java/com/smartfit/ui/screens/activitylog/ActivityLogViewModel.kt

package com.smartfit.ui.screens.activitylog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.domain.model.Activity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Activity Log screen managing list of activities.
 */
class ActivityLogViewModel(
    private val activityRepository: ActivityRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    init {
        // Delay initialization to prevent blocking
        viewModelScope.launch {
            delay(50)
            loadActivities()
            loadWeeklySummary()
            observeStepUpdates()
        }
    }

    private fun observeStepUpdates() {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = today
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val endOfDay = calendar.timeInMillis + 24 * 60 * 60 * 1000
            val startOfWeek = endOfDay - 7 * 24 * 60 * 60 * 1000

            combine(
                stepRepository.getStepsByDateRange(startOfWeek, endOfDay),
                activityRepository.getAllActivities()
            ) { stepCounts, _ ->
                val totalSteps = stepCounts.sumOf { it.steps }
                val (activitySteps, activityCalories) = activityRepository.getWeeklySummary(today)

                val combinedSteps = activitySteps + totalSteps
                val stepCalories = (totalSteps * 0.04).toInt()
                val totalCalories = activityCalories + stepCalories

                Pair(combinedSteps, totalCalories)
            }
                .distinctUntilChanged()
                .collect { (steps, calories) ->
                    _uiState.value = _uiState.value.copy(
                        weeklySteps = steps,
                        weeklyCalories = calories
                    )
                    Log.d("ActivityLogViewModel", "Weekly updated: $steps steps, $calories calories")
                }
        }
    }

    private fun loadActivities() {
        viewModelScope.launch {
            Log.d("ActivityLogViewModel", "Loading activities from database")
            activityRepository.getAllActivities()
                .distinctUntilChanged()
                .collect { activities ->
                    Log.d("ActivityLogViewModel", "Activities loaded: ${activities.size}")
                    _uiState.value = _uiState.value.copy(activities = activities)
                }
        }
    }

    private fun loadWeeklySummary() {
        viewModelScope.launch {
            try {
                Log.d("ActivityLogViewModel", "Loading weekly summary")
                val today = System.currentTimeMillis()
                val (steps, calories) = activityRepository.getWeeklySummary(today)

                if (_uiState.value.weeklySteps != steps || _uiState.value.weeklyCalories != calories) {
                    _uiState.value = _uiState.value.copy(
                        weeklySteps = steps,
                        weeklyCalories = calories
                    )
                    Log.d("ActivityLogViewModel", "Weekly summary updated: $steps steps, $calories calories")
                }
            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error loading weekly summary", e)
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            Log.d("ActivityLogViewModel", "Deleting activity: ${activity.id}")
            activityRepository.deleteActivity(activity)
            loadWeeklySummary()
        }
    }
}

data class ActivityLogUiState(
    val activities: List<Activity> = emptyList(),
    val weeklySteps: Int = 0,
    val weeklyCalories: Int = 0
)