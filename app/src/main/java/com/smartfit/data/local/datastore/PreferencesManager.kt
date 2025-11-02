// FILE: app/src/main/java/com/smartfit/data/local/datastore/PreferencesManager.kt

package com.smartfit.data.local.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages user preferences using DataStore.
 * Stores theme mode, daily step goals, and step tracking state.
 */
class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val DAILY_STEP_GOAL_KEY = intPreferencesKey("daily_step_goal")
        val DAILY_CALORIE_GOAL_KEY = intPreferencesKey("daily_calorie_goal")
        val STEP_TRACKING_ENABLED_KEY = booleanPreferencesKey("step_tracking_enabled")

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }

    val themeModeFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("PreferencesManager", "Error reading theme mode preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THEME_MODE_KEY] ?: THEME_SYSTEM
        }

    val dailyStepGoalFlow: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("PreferencesManager", "Error reading step goal preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DAILY_STEP_GOAL_KEY] ?: 10000
        }

    val dailyCalorieGoalFlow: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("PreferencesManager", "Error reading calorie goal preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DAILY_CALORIE_GOAL_KEY] ?: 2000
        }

    val stepTrackingEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("PreferencesManager", "Error reading step tracking preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[STEP_TRACKING_ENABLED_KEY] ?: false
        }

    suspend fun setThemeMode(mode: String) {
        Log.d("PreferencesManager", "Setting theme mode: $mode")
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setDailyStepGoal(goal: Int) {
        Log.d("PreferencesManager", "Setting daily step goal: $goal")
        dataStore.edit { preferences ->
            preferences[DAILY_STEP_GOAL_KEY] = goal
        }
    }

    suspend fun setDailyCalorieGoal(goal: Int) {
        Log.d("PreferencesManager", "Setting daily calorie goal: $goal")
        dataStore.edit { preferences ->
            preferences[DAILY_CALORIE_GOAL_KEY] = goal
        }
    }

    suspend fun setStepTrackingEnabled(enabled: Boolean) {
        Log.d("PreferencesManager", "Setting step tracking enabled: $enabled")
        dataStore.edit { preferences ->
            preferences[STEP_TRACKING_ENABLED_KEY] = enabled
        }
    }
}