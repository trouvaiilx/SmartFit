// FILE: app/src/main/java/com/smartfit/ui/screens/exercisedetail/ExerciseDetailViewModel.kt

package com.smartfit.ui.screens.exercisedetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.di.AppContainer
import com.smartfit.domain.model.Suggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Exercise Detail screen.
 * Uses cached suggestions from AppContainer to avoid reloading.
 */
class ExerciseDetailViewModel(
    private val appContainer: AppContainer,
    exerciseId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    init {
        loadExercise(exerciseId)
    }

    private fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            Log.d("ExerciseDetailViewModel", "Loading exercise with id: $exerciseId")

            // Try to get from cache first
            val exercise = appContainer.getSuggestionById(exerciseId)

            if (exercise != null) {
                Log.d("ExerciseDetailViewModel", "Exercise found in cache: ${exercise.title}")
                _uiState.value = _uiState.value.copy(
                    exercise = exercise,
                    isLoading = false
                )
            } else {
                // Fallback: load from repository if not in cache
                Log.d("ExerciseDetailViewModel", "Exercise not in cache, loading from repository")
                appContainer.suggestionRepository.getSuggestions(20).fold(
                    onSuccess = { suggestions ->
                        Log.d("ExerciseDetailViewModel", "Total suggestions loaded: ${suggestions.size}")
                        val foundExercise = suggestions.find { it.id == exerciseId }

                        if (foundExercise != null) {
                            Log.d("ExerciseDetailViewModel", "Exercise found: ${foundExercise.title}")
                            // Update cache
                            if (appContainer.cachedSuggestions.isEmpty()) {
                                appContainer.cachedSuggestions = suggestions
                            }
                        } else {
                            Log.e("ExerciseDetailViewModel", "Exercise with id $exerciseId not found")
                        }

                        _uiState.value = _uiState.value.copy(
                            exercise = foundExercise,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        Log.e("ExerciseDetailViewModel", "Error loading exercise: ${error.message}", error)
                        _uiState.value = _uiState.value.copy(
                            exercise = null,
                            isLoading = false
                        )
                    }
                )
            }
        }
    }
}

data class ExerciseDetailUiState(
    val exercise: Suggestion? = null,
    val isLoading: Boolean = false
)