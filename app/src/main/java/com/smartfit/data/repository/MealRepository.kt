// FILE: app/src/main/java/com/smartfit/data/repository/MealRepository.kt

package com.smartfit.data.repository

import android.util.Log
import com.smartfit.data.local.database.MealDao
import com.smartfit.data.local.database.toEntity
import com.smartfit.data.local.database.toDomainModel
import com.smartfit.domain.model.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class MealRepository(private val mealDao: MealDao) {

    fun getAllMeals(): Flow<List<Meal>> {
        Log.d("MealRepository", "Fetching all meals from database")
        return mealDao.getAllMeals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getMealById(id: Int): Meal? {
        Log.d("MealRepository", "Fetching meal with id: $id")
        return mealDao.getMealById(id)?.toDomainModel()
    }

    fun getMealsByDateRange(startDate: Long, endDate: Long): Flow<List<Meal>> {
        Log.d("MealRepository", "Fetching meals from $startDate to $endDate")
        return mealDao.getMealsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun insertMeal(meal: Meal): Long {
        Log.d("MealRepository", "Inserting meal: ${meal.name}")
        return mealDao.insertMeal(meal.toEntity())
    }

    suspend fun updateMeal(meal: Meal) {
        Log.d("MealRepository", "Updating meal with id: ${meal.id}")
        mealDao.updateMeal(meal.toEntity())
    }

    suspend fun deleteMeal(meal: Meal) {
        Log.d("MealRepository", "Deleting meal with id: ${meal.id}")
        mealDao.deleteMeal(meal.toEntity())
    }

    suspend fun getDailyCalorieIntake(date: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        Log.d("MealRepository", "Calculating daily calorie intake for date: $date")
        return mealDao.getTotalCalories(startOfDay, endOfDay) ?: 0
    }
}