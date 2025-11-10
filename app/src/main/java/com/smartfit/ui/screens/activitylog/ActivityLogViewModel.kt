// FILE: app/src/main/java/com/smartfit/ui/screens/activitylog/ActivityLogViewModel.kt

package com.smartfit.ui.screens.activitylog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.domain.model.Activity
import com.smartfit.util.Constants
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            delay(Constants.UI_SHORT_DELAY_MS)
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
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
                val stepCalories = (totalSteps * Constants.CALORIES_PER_STEP).toInt()

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
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                activityRepository.getAllActivities(),
                _uiState.map { it.selectedPeriod },
                _searchQuery
            ) { activities, period, query ->
                val (startDate, endDate) = getDateRange(period)
                val filteredByDate = activities.filter { it.date in startDate..<endDate }

                // Apply search filter
                if (query.isBlank()) {
                    filteredByDate
                } else {
                    filteredByDate.filter { activity ->
                        activity.type.contains(query, ignoreCase = true) ||
                                activity.notes.contains(query, ignoreCase = true)
                    }
                }
            }
                .distinctUntilChanged()
                .collect { filteredActivities ->
                    Log.d("ActivityLogViewModel", "Activities loaded: ${filteredActivities.size}")
                    _uiState.value = _uiState.value.copy(
                        activities = filteredActivities,
                        isLoading = false
                    )
                }
        }
    }

    private fun getDateRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val endDate = calendar.timeInMillis + Constants.DAY_IN_MILLIS

        val startDate = when (period) {
            TimePeriod.TODAY -> calendar.timeInMillis
            TimePeriod.THIS_WEEK -> endDate - Constants.WEEK_IN_MILLIS
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
    val expandedDates: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)