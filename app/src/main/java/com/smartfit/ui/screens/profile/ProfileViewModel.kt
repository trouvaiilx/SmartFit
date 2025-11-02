// FILE: app/src/main/java/com/smartfit/ui/screens/profile/ProfileViewModel.kt

package com.smartfit.ui.screens.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.local.datastore.PreferencesManager
import com.smartfit.util.PermissionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile screen managing user preferences.
 */
class ProfileViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Loading user preferences")
            preferencesManager.themeModeFlow.collect { themeMode ->
                _uiState.value = _uiState.value.copy(themeMode = themeMode)
                Log.d("ProfileViewModel", "Theme mode loaded: $themeMode")
            }
        }

        viewModelScope.launch {
            preferencesManager.dailyStepGoalFlow.collect { goal ->
                _uiState.value = _uiState.value.copy(
                    dailyStepGoal = goal,
                    stepGoalText = goal.toString()
                )
            }
        }

        viewModelScope.launch {
            preferencesManager.dailyCalorieGoalFlow.collect { goal ->
                _uiState.value = _uiState.value.copy(
                    dailyCalorieGoal = goal,
                    calorieGoalText = goal.toString()
                )
            }
        }

        viewModelScope.launch {
            preferencesManager.stepTrackingEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isStepTrackingEnabled = enabled)
            }
        }
    }

    fun checkPermission(context: Context) {
        val hasPermission = PermissionHandler.hasActivityRecognitionPermission(context)
        _uiState.value = _uiState.value.copy(hasPermission = hasPermission)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Setting theme mode to: $mode")
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setStepTracking(enabled: Boolean) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Setting step tracking to: $enabled")
            preferencesManager.setStepTrackingEnabled(enabled)
        }
    }

    fun updateStepGoalText(text: String) {
        _uiState.value = _uiState.value.copy(stepGoalText = text)
    }

    fun saveStepGoal() {
        val goal = _uiState.value.stepGoalText.toIntOrNull()
        if (goal != null && goal > 0) {
            viewModelScope.launch {
                Log.d("ProfileViewModel", "Saving step goal: $goal")
                preferencesManager.setDailyStepGoal(goal)
                _uiState.value = _uiState.value.copy(dailyStepGoal = goal)
            }
        }
    }

    fun updateCalorieGoalText(text: String) {
        _uiState.value = _uiState.value.copy(calorieGoalText = text)
    }

    fun saveCalorieGoal() {
        val goal = _uiState.value.calorieGoalText.toIntOrNull()
        if (goal != null && goal > 0) {
            viewModelScope.launch {
                Log.d("ProfileViewModel", "Saving calorie goal: $goal")
                preferencesManager.setDailyCalorieGoal(goal)
                _uiState.value = _uiState.value.copy(dailyCalorieGoal = goal)
            }
        }
    }
}

data class ProfileUiState(
    val themeMode: String = PreferencesManager.THEME_SYSTEM,
    val dailyStepGoal: Int = 10000,
    val dailyCalorieGoal: Int = 2000,
    val stepGoalText: String = "10000",
    val calorieGoalText: String = "2000",
    val isStepTrackingEnabled: Boolean = false,
    val hasPermission: Boolean = true
)