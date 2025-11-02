// FILE: app/src/main/java/com/smartfit/ui/screens/home/HomeViewModel.kt

package com.smartfit.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.MealRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.data.repository.SuggestionRepository
import com.smartfit.di.AppContainer
import com.smartfit.domain.model.Suggestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val suggestionRepository: SuggestionRepository,
    private val mealRepository: MealRepository,
    private val stepRepository: StepRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var isInitialized = false

    init {
        // Delay initialization to prevent blocking app startup
        viewModelScope.launch {
            delay(100) // Let UI render first
            observeRealTimeData()
            isInitialized = true
        }
    }

    private fun observeRealTimeData() {
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

            // Combine all data sources into one flow with debounce to prevent excessive updates
            combine(
                stepRepository.getStepsByDateRange(startOfDay, endOfDay),
                activityRepository.getAllActivities(),
                mealRepository.getAllMeals()
            ) { stepCounts, _, _ ->
                // Recalculate everything when any data changes
                val totalSteps = stepCounts.sumOf { it.steps }
                val stepCalories = (totalSteps * 0.04).toInt()

                val (_, activityCalories) = activityRepository.getDailySummary(today)
                val totalBurned = activityCalories + stepCalories

                val caloriesConsumed = mealRepository.getDailyCalorieIntake(today)
                val netCalories = caloriesConsumed - totalBurned

                Triple(totalSteps, totalBurned, Pair(caloriesConsumed, netCalories))
            }
                .distinctUntilChanged() // Only emit when values actually change
                .collect { (steps, burned, consumedAndNet) ->
                    _uiState.value = _uiState.value.copy(
                        dailySteps = steps,
                        dailyCaloriesBurned = burned,
                        dailyCaloriesConsumed = consumedAndNet.first,
                        netCalories = consumedAndNet.second
                    )
                    Log.d("HomeViewModel", "Real-time update - Steps: $steps, Burned: $burned")
                }
        }
    }

    fun loadSuggestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingSuggestions = true,
                shouldAnimateCards = true
            )
            Log.d("HomeViewModel", "Loading suggestions from network")

            suggestionRepository.getSuggestions(10).fold(
                onSuccess = { suggestions ->
                    Log.d("HomeViewModel", "Suggestions loaded successfully: ${suggestions.size}")
                    _uiState.value = _uiState.value.copy(
                        suggestions = suggestions,
                        isLoadingSuggestions = false,
                        suggestionsError = null,
                        suggestionsLoaded = true
                    )
                    appContainer.cachedSuggestions = suggestions
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Error loading suggestions: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoadingSuggestions = false,
                        suggestionsError = error.message ?: "Failed to load suggestions",
                        shouldAnimateCards = false
                    )
                }
            )
        }
    }

    fun toggleSuggestionsExpanded() {
        _uiState.value = _uiState.value.copy(
            isSuggestionsExpanded = !_uiState.value.isSuggestionsExpanded,
            shouldAnimateCards = true
        )
    }
}

data class HomeUiState(
    val dailySteps: Int = 0,
    val dailyCaloriesBurned: Int = 0,
    val dailyCaloriesConsumed: Int = 0,
    val netCalories: Int = 0,
    val suggestions: List<Suggestion> = emptyList(),
    val isLoadingSuggestions: Boolean = false,
    val suggestionsError: String? = null,
    val suggestionsLoaded: Boolean = false,
    val isSuggestionsExpanded: Boolean = true,
    val shouldAnimateCards: Boolean = false
)