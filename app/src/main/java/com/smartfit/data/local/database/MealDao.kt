// FILE: app/src/main/java/com/smartfit/data/local/database/MealDao.kt

package com.smartfit.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY date DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: Int): MealEntity?

    @Query("SELECT * FROM meals WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    fun getMealsByDateRange(startDate: Long, endDate: Long): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("SELECT SUM(calories) FROM meals WHERE date >= :startDate AND date < :endDate")
    suspend fun getTotalCalories(startDate: Long, endDate: Long): Int?
}