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
import com.smartfit.util.CalorieCalculator
import com.smartfit.util.Constants
import com.smartfit.util.DateUtils
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
            delay(Constants.UI_INIT_DELAY_MS)
            observeRealTimeData()

            // Auto-load suggestions if cache is valid
            if (appContainer.isSuggestionCacheValid() && appContainer.cachedSuggestions.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    suggestions = appContainer.cachedSuggestions,
                    suggestionsLoaded = true
                )
            }

            isInitialized = true
        }
    }

    private fun observeRealTimeData() {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val startOfDay = DateUtils.getStartOfDay(today)
            val endOfDay = DateUtils.getEndOfDay(today)

            // Combine all data sources with debounce to prevent excessive updates
            combine(
                stepRepository.getStepsByDateRange(startOfDay, endOfDay),
                activityRepository.getActivitiesByDateRange(startOfDay, endOfDay),
                mealRepository.getMealsByDateRange(startOfDay, endOfDay)
            ) { stepCounts, activities, meals ->
                // Calculate step-based calories
                val totalSteps = stepCounts.sumOf { it.steps }
                val stepCalories = CalorieCalculator.calculateStepCalories(totalSteps)

                // Calculate activity calories
                val activityCalories = activities.sumOf { it.calories }
                val totalBurned = activityCalories + stepCalories

                // Calculate consumed calories
                val caloriesConsumed = meals.sumOf { it.calories }
                val netCalories = caloriesConsumed - totalBurned

                Triple(totalSteps, totalBurned, Pair(caloriesConsumed, netCalories))
            }
                .distinctUntilChanged()
                .catch { e ->
                    Log.e("HomeViewModel", "Error observing real-time data", e)
                }
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
        // Check cache first
        if (appContainer.isSuggestionCacheValid() && appContainer.cachedSuggestions.isNotEmpty()) {
            Log.d("HomeViewModel", "Using cached suggestions")
            _uiState.value = _uiState.value.copy(
                suggestions = appContainer.cachedSuggestions,
                isLoadingSuggestions = false,
                suggestionsError = null,
                suggestionsLoaded = true,
                shouldAnimateCards = true
            )
            return
        }

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
                    appContainer.lastSuggestionLoadTime = System.currentTimeMillis()
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