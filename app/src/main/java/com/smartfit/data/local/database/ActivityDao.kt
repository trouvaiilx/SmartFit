// FILE: app/src/main/java/com/smartfit/data/local/database/ActivityDao.kt

package com.smartfit.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for activity CRUD operations.
 */
@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): ActivityEntity?

    @Query("SELECT * FROM activities WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT SUM(steps) FROM activities WHERE date >= :startDate AND date < :endDate")
    suspend fun getTotalSteps(startDate: Long, endDate: Long): Int?

    @Query("SELECT SUM(calories) FROM activities WHERE date >= :startDate AND date < :endDate")
    suspend fun getTotalCalories(startDate: Long, endDate: Long): Int?
}