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
import java.util.*

class ActivityLogViewModel(
    private val activityRepository: ActivityRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(50)
            loadActivities()
            observeStepUpdates()
        }
    }

    fun setTimePeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }

    fun toggleDateExpansion(date: Long) {
        val currentExpanded = _uiState.value.expandedDates
        _uiState.value = _uiState.value.copy(
            expandedDates = if (currentExpanded.contains(date)) {
                currentExpanded - date
            } else {
                currentExpanded + date
            }
        )
    }

    private fun observeStepUpdates() {
        viewModelScope.launch {
            combine(
                stepRepository.getStepsByDateRange(0, Long.MAX_VALUE),
                activityRepository.getAllActivities(),
                _uiState.map { it.selectedPeriod }
            ) { stepCounts, activities, period ->
                val (startDate, endDate) = getDateRange(period)

                val periodStepCounts = stepCounts.filter { it.date in startDate..<endDate }
                val totalSteps = periodStepCounts.sumOf { it.steps }
                val stepCalories = (totalSteps * 0.04).toInt()

                val periodActivities = activities.filter { it.date in startDate..<endDate }
                val activitySteps = periodActivities.sumOf { it.steps }
                val activityCalories = periodActivities.sumOf { it.calories }

                val combinedSteps = activitySteps + totalSteps
                val totalCalories = activityCalories + stepCalories

                Pair(combinedSteps, totalCalories)
            }
                .distinctUntilChanged()
                .collect { (steps, calories) ->
                    _uiState.value = _uiState.value.copy(
                        periodSteps = steps,
                        periodCalories = calories
                    )
                    Log.d("ActivityLogViewModel", "Period updated: $steps steps, $calories calories")
                }
        }
    }

    private fun loadActivities() {
        viewModelScope.launch {
            Log.d("ActivityLogViewModel", "Loading activities from database")
            combine(
                activityRepository.getAllActivities(),
                _uiState.map { it.selectedPeriod }
            ) { activities, period ->
                val (startDate, endDate) = getDateRange(period)
                activities.filter { it.date in startDate..<endDate }
            }
                .distinctUntilChanged()
                .collect { filteredActivities ->
                    Log.d("ActivityLogViewModel", "Activities loaded: ${filteredActivities.size}")
                    _uiState.value = _uiState.value.copy(activities = filteredActivities)
                }
        }
    }

    private fun getDateRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val endDate = calendar.timeInMillis + 24 * 60 * 60 * 1000

        val startDate = when (period) {
            TimePeriod.TODAY -> calendar.timeInMillis
            TimePeriod.THIS_WEEK -> endDate - 7 * 24 * 60 * 60 * 1000
        }

        return Pair(startDate, endDate)
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            Log.d("ActivityLogViewModel", "Deleting activity: ${activity.id}")
            activityRepository.deleteActivity(activity)
        }
    }
}

enum class TimePeriod {
    TODAY, THIS_WEEK
}

data class ActivityLogUiState(
    val activities: List<Activity> = emptyList(),
    val periodSteps: Int = 0,
    val periodCalories: Int = 0,
    val selectedPeriod: TimePeriod = TimePeriod.TODAY,
    val expandedDates: Set<Long> = emptySet()
)