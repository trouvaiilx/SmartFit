// FILE: app/src/main/java/com/smartfit/ui/screens/meals/MealLogViewModel.kt

package com.smartfit.ui.screens.meals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.MealRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.domain.model.Meal
import com.smartfit.ui.screens.activitylog.TimePeriod
import com.smartfit.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MealLogViewModel(
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository,
    private val stepRepository: StepRepository,
    private val calorieGoal: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealLogUiState(calorieGoal = calorieGoal))
    val uiState: StateFlow<MealLogUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            delay(Constants.UI_SHORT_DELAY_MS)
            loadMeals()
            observeRealTimeCalories()
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

    private fun loadMeals() {
        viewModelScope.launch {
            Log.d("MealLogViewModel", "Loading meals from database")
            combine(
                mealRepository.getAllMeals(),
                _uiState.map { it.selectedPeriod },
                _searchQuery
            ) { meals, period, query ->
                val (startDate, endDate) = getDateRange(period)
                val filtered = meals.filter { it.date in startDate..<endDate }
                if (query.isBlank()) {
                    filtered
                } else {
                    filtered.filter { meal ->
                        meal.name.contains(query, ignoreCase = true) ||
                                meal.notes.contains(query, ignoreCase = true)
                    }
                }
            }
                .distinctUntilChanged()
                .collect { filteredMeals ->
                    Log.d("MealLogViewModel", "Meals loaded: ${filteredMeals.size}")
                    _uiState.value = _uiState.value.copy(meals = filteredMeals)
                }
        }
    }

    private fun observeRealTimeCalories() {
        viewModelScope.launch {
            combine(
                stepRepository.getStepsByDateRange(0, Long.MAX_VALUE),
                activityRepository.getAllActivities(),
                mealRepository.getAllMeals(),
                _uiState.map { it.selectedPeriod }
            ) { stepCounts, activities, meals, period ->
                val (startDate, endDate) = getDateRange(period)

                val periodStepCounts = stepCounts.filter { it.date in startDate..<endDate }
                val totalSteps = periodStepCounts.sumOf { it.steps }
                val stepCalories = (totalSteps * 0.04).toInt()

                val periodActivities = activities.filter { it.date in startDate..<endDate }
                val activityCalories = periodActivities.sumOf { it.calories }
                val totalBurned = activityCalories + stepCalories

                val periodMeals = meals.filter { it.date in startDate..<endDate }
                val consumed = periodMeals.sumOf { it.calories }

                Triple(consumed, totalBurned, calorieGoal)
            }
                .distinctUntilChanged()
                .collect { (consumed, burned, goal) ->
                    _uiState.value = _uiState.value.copy(
                        dailyCaloriesConsumed = consumed,
                        dailyCaloriesBurned = burned,
                        calorieGoal = goal
                    )
                    Log.d("MealLogViewModel", "Calories updated: Consumed $consumed, Burned $burned")
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

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            Log.d("MealLogViewModel", "Deleting meal: ${meal.id}")
            mealRepository.deleteMeal(meal)
        }
    }
}

data class MealLogUiState(
    val meals: List<Meal> = emptyList(),
    val dailyCaloriesConsumed: Int = 0,
    val dailyCaloriesBurned: Int = 0,
    val calorieGoal: Int = 2000,
    val selectedPeriod: TimePeriod = TimePeriod.TODAY,
    val expandedDates: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)