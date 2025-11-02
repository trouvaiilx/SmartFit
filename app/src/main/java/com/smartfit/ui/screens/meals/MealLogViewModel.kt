package com.smartfit.ui.screens.meals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.MealRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.domain.model.Meal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MealLogViewModel(
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository,
    private val stepRepository: StepRepository,
    private val calorieGoal: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealLogUiState())
    val uiState: StateFlow<MealLogUiState> = _uiState.asStateFlow()

    init {
        // Delay initialization
        viewModelScope.launch {
            delay(50)
            loadMeals()
            observeRealTimeCalories()
        }
    }

    private fun loadMeals() {
        viewModelScope.launch {
            Log.d("MealLogViewModel", "Loading meals from database")
            mealRepository.getAllMeals()
                .distinctUntilChanged()
                .collect { meals ->
                    Log.d("MealLogViewModel", "Meals loaded: ${meals.size}")
                    _uiState.value = _uiState.value.copy(meals = meals)
                }
        }
    }

    private fun observeRealTimeCalories() {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = today
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000

            combine(
                stepRepository.getStepsByDateRange(startOfDay, endOfDay),
                activityRepository.getAllActivities(),
                mealRepository.getAllMeals()
            ) { stepCounts, _, _ ->
                val totalSteps = stepCounts.sumOf { it.steps }
                val stepCalories = (totalSteps * 0.04).toInt()

                val (_, activityCalories) = activityRepository.getDailySummary(today)
                val totalBurned = activityCalories + stepCalories

                val consumed = mealRepository.getDailyCalorieIntake(today)

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
    val calorieGoal: Int = 2000
)