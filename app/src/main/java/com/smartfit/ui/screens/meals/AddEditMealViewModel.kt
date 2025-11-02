// FILE: app/src/main/java/com/smartfit/ui/screens/meals/AddEditMealViewModel.kt

package com.smartfit.ui.screens.meals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartfit.data.repository.MealRepository
import com.smartfit.domain.model.CommonFoods
import com.smartfit.domain.model.FoodItem
import com.smartfit.domain.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddEditMealViewModel(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditMealUiState())
    val uiState: StateFlow<AddEditMealUiState> = _uiState.asStateFlow()

    fun loadMeal(mealId: Int) {
        viewModelScope.launch {
            Log.d("AddEditMealViewModel", "Loading meal with id: $mealId")
            val meal = mealRepository.getMealById(mealId)
            if (meal != null) {
                _uiState.value = _uiState.value.copy(
                    mealType = meal.mealType.replaceFirstChar { it.uppercase() },
                    customFoodName = meal.name,
                    portion = meal.portion.toString(),
                    calories = meal.calories.toString(),
                    notes = meal.notes,
                    mealId = meal.id,
                    availableFoods = CommonFoods.getFoodsByCategory(
                        meal.mealType.replaceFirstChar { it.uppercase() }
                    )
                )
                Log.d("AddEditMealViewModel", "Meal loaded: ${meal.name}, id: ${meal.id}")
            } else {
                Log.e("AddEditMealViewModel", "Meal not found with id: $mealId")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Meal not found"
                )
            }
        }
    }

    fun updateMealType(type: String) {
        _uiState.value = _uiState.value.copy(
            mealType = type,
            availableFoods = CommonFoods.getFoodsByCategory(type)
        )
    }

    fun selectFood(food: FoodItem) {
        _uiState.value = _uiState.value.copy(
            selectedFood = food,
            customFoodName = ""
        )
        calculateCalories()
    }

    fun updateCustomFoodName(name: String) {
        _uiState.value = _uiState.value.copy(
            customFoodName = name,
            selectedFood = null
        )
    }

    fun updatePortion(portion: String) {
        _uiState.value = _uiState.value.copy(portion = portion)
        calculateCalories()
    }

    fun updateCalories(calories: String) {
        _uiState.value = _uiState.value.copy(calories = calories)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    private fun calculateCalories() {
        val food = _uiState.value.selectedFood ?: return
        val portion = _uiState.value.portion.toDoubleOrNull() ?: 1.0

        val calculatedCalories = (food.caloriesPer100g * portion).toInt()
        _uiState.value = _uiState.value.copy(calories = calculatedCalories.toString())
        Log.d("AddEditMealViewModel", "Calculated calories: $calculatedCalories for ${food.name}, portion: $portion")
    }

    fun saveMeal(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.mealType.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please select a meal type")
            return
        }

        val foodName = if (state.selectedFood != null) {
            state.selectedFood.name
        } else state.customFoodName.ifBlank {
            _uiState.value = state.copy(errorMessage = "Please select or enter a food name")
            return
        }

        val calories = state.calories.toIntOrNull()
        if (calories == null || calories <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter valid calories")
            return
        }

        val portion = state.portion.toDoubleOrNull()
        if (portion == null || portion <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid portion size")
            return
        }

        viewModelScope.launch {
            try {
                val meal = Meal(
                    id = state.mealId,
                    name = foodName,
                    calories = calories,
                    mealType = state.mealType.lowercase(),
                    date = System.currentTimeMillis(),
                    portion = portion,
                    notes = state.notes
                )

                if (state.mealId == 0) {
                    Log.d("AddEditMealViewModel", "Inserting new meal")
                    mealRepository.insertMeal(meal)
                } else {
                    Log.d("AddEditMealViewModel", "Updating meal: ${state.mealId}")
                    mealRepository.updateMeal(meal)
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("AddEditMealViewModel", "Error saving meal", e)
                _uiState.value = state.copy(errorMessage = "Failed to save meal")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AddEditMealUiState(
    val mealType: String = "",
    val selectedFood: FoodItem? = null,
    val customFoodName: String = "",
    val portion: String = "1.0",
    val calories: String = "",
    val notes: String = "",
    val availableFoods: List<FoodItem> = emptyList(),
    val mealId: Int = 0,
    val errorMessage: String? = null
)