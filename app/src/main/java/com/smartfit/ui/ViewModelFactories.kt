// FILE: app/src/main/java/com/smartfit/ui/ViewModelFactories.kt

package com.smartfit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartfit.data.local.datastore.PreferencesManager
import com.smartfit.data.repository.ActivityRepository
import com.smartfit.data.repository.MealRepository
import com.smartfit.data.repository.StepRepository
import com.smartfit.data.repository.SuggestionRepository
import com.smartfit.di.AppContainer
import com.smartfit.ui.screens.activitylog.ActivityLogViewModel
import com.smartfit.ui.screens.activitylog.AddEditActivityViewModel
import com.smartfit.ui.screens.exercisedetail.ExerciseDetailViewModel
import com.smartfit.ui.screens.home.HomeViewModel
import com.smartfit.ui.screens.meals.AddEditMealViewModel
import com.smartfit.ui.screens.meals.MealLogViewModel
import com.smartfit.ui.screens.profile.ProfileViewModel

class HomeViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val suggestionRepository: SuggestionRepository,
    private val mealRepository: MealRepository,
    private val stepRepository: StepRepository,
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                activityRepository,
                suggestionRepository,
                mealRepository,
                stepRepository,
                appContainer
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ActivityLogViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val stepRepository: StepRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityLogViewModel::class.java)) {
            return ActivityLogViewModel(activityRepository, stepRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AddEditActivityViewModelFactory(
    private val activityRepository: ActivityRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditActivityViewModel::class.java)) {
            return AddEditActivityViewModel(activityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MealLogViewModelFactory(
    private val mealRepository: MealRepository,
    private val activityRepository: ActivityRepository,
    private val stepRepository: StepRepository,
    private val calorieGoal: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealLogViewModel::class.java)) {
            return MealLogViewModel(
                mealRepository,
                activityRepository,
                stepRepository,
                calorieGoal
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AddEditMealViewModelFactory(
    private val mealRepository: MealRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditMealViewModel::class.java)) {
            return AddEditMealViewModel(mealRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ExerciseDetailViewModelFactory(
    private val appContainer: AppContainer,
    private val exerciseId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java)) {
            return ExerciseDetailViewModel(appContainer, exerciseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}