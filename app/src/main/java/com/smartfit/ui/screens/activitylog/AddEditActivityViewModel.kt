// FILE: app/src/main/java/com/smartfit/ui/screens/activitylog/AddEditActivityViewModel.kt

package com.smartfit.ui.screens.activitylog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.domain.model.Activity
import com.smartfit.util.CalorieCalculator
import com.smartfit.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for adding and editing activities with calorie calculation.
 * Now uses Constants and proper validation.
 */
class AddEditActivityViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditActivityUiState())
    val uiState: StateFlow<AddEditActivityUiState> = _uiState.asStateFlow()

    fun loadActivity(activityId: Int) {
        viewModelScope.launch {
            Log.d("AddEditActivityViewModel", "Loading activity with id: $activityId")
            val activity = activityRepository.getActivityById(activityId)
            if (activity != null) {
                // Check if it's a custom activity type
                val isCustomType = activity.type !in listOf("Walking", "Running", "Cycling", "Gym", "Swimming", "Yoga")

                _uiState.value = _uiState.value.copy(
                    type = if (isCustomType) "Other" else activity.type,
                    customActivityName = if (isCustomType) activity.type else "",
                    duration = activity.duration.toString(),
                    calories = activity.calories.toString(),
                    notes = activity.notes,
                    activityId = activity.id
                )
                Log.d("AddEditActivityViewModel", "Activity loaded: ${activity.type}, id: ${activity.id}")
            } else {
                Log.e("AddEditActivityViewModel", "Activity not found with id: $activityId")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Activity not found"
                )
            }
        }
    }

    fun updateType(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
        // Clear custom name if switching away from Other
        if (type != "Other") {
            _uiState.value = _uiState.value.copy(customActivityName = "")
        }
        // Only auto-calculate if not "Other"
        if (type != "Other") {
            calculateCalories()
        }
    }

    fun updateCustomActivityName(name: String) {
        // Validate length
        if (!ValidationUtils.isValidTextLength(name)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = ValidationUtils.getErrorMessage("text", name)
            )
            return
        }
        _uiState.value = _uiState.value.copy(customActivityName = name)
    }

    fun updateDuration(duration: String) {
        _uiState.value = _uiState.value.copy(duration = duration)

        // Validate duration
        duration.toIntOrNull()?.let { dur ->
            if (!ValidationUtils.isValidDuration(dur)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = ValidationUtils.getErrorMessage("duration", dur)
                )
                return
            }
        }

        // Only auto-calculate if not "Other"
        if (_uiState.value.type != "Other") {
            calculateCalories()
        }
    }

    fun updateCalories(calories: String) {
        _uiState.value = _uiState.value.copy(calories = calories)

        // Validate calories
        calories.toIntOrNull()?.let { cal ->
            if (!ValidationUtils.isValidCalories(cal)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = ValidationUtils.getErrorMessage("calories", cal)
                )
            }
        }
    }

    fun updateNotes(notes: String) {
        // Validate length
        if (!ValidationUtils.isValidTextLength(notes)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = ValidationUtils.getErrorMessage("text", notes)
            )
            return
        }
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    /**
     * Calculate calories based on activity type and duration using CalorieCalculator
     */
    private fun calculateCalories() {
        val duration = _uiState.value.duration.toIntOrNull() ?: 0
        if (duration <= 0) return

        val type = _uiState.value.type
        val calculatedCalories = CalorieCalculator.calculateActivityCalories(duration, type)

        _uiState.value = _uiState.value.copy(calories = calculatedCalories.toString())
        Log.d("AddEditActivityViewModel", "Calculated calories: $calculatedCalories for $type, $duration min")
    }

    fun saveActivity(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validate type
        if (state.type.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please select an activity type")
            return
        }

        // Get final activity name
        val activityName = if (state.type == "Other") {
            if (state.customActivityName.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Please enter a custom activity name")
                return
            }
            state.customActivityName
        } else {
            state.type
        }

        // Validate duration
        val duration = state.duration.toIntOrNull()
        if (duration == null || !ValidationUtils.isValidDuration(duration)) {
            _uiState.value = state.copy(
                errorMessage = ValidationUtils.getErrorMessage("duration", duration ?: 0)
            )
            return
        }

        // Validate calories
        val calories = state.calories.toIntOrNull()
        if (calories == null || !ValidationUtils.isValidCalories(calories)) {
            _uiState.value = state.copy(
                errorMessage = ValidationUtils.getErrorMessage("calories", calories ?: 0)
            )
            return
        }

        // Validate notes length
        if (!ValidationUtils.isValidTextLength(state.notes)) {
            _uiState.value = state.copy(
                errorMessage = ValidationUtils.getErrorMessage("text", state.notes)
            )
            return
        }

        viewModelScope.launch {
            try {
                val activity = Activity(
                    id = state.activityId,
                    type = activityName,
                    duration = duration,
                    calories = calories,
                    steps = 0,
                    date = System.currentTimeMillis(),
                    notes = state.notes
                )

                if (state.activityId == 0) {
                    Log.d("AddEditActivityViewModel", "Inserting new activity")
                    activityRepository.insertActivity(activity)
                } else {
                    Log.d("AddEditActivityViewModel", "Updating activity: ${state.activityId}")
                    activityRepository.updateActivity(activity)
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("AddEditActivityViewModel", "Error saving activity", e)
                _uiState.value = state.copy(errorMessage = "Failed to save activity: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AddEditActivityUiState(
    val type: String = "",
    val customActivityName: String = "",
    val duration: String = "",
    val calories: String = "",
    val notes: String = "",
    val activityId: Int = 0,
    val errorMessage: String? = null
)