// FILE: app/src/main/java/com/smartfit/data/repository/ActivityRepository.kt

package com.smartfit.data.repository

import android.util.Log
import com.smartfit.data.local.database.ActivityDao
import com.smartfit.data.local.database.toEntity
import com.smartfit.data.local.database.toDomainModel
import com.smartfit.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

/**
 * Repository implementing data layer architecture with Room database.
 * Handles CRUD operations and data transformations.
 */
class ActivityRepository(private val activityDao: ActivityDao) {

    fun getAllActivities(): Flow<List<Activity>> {
        Log.d("ActivityRepository", "Fetching all activities from database")
        return activityDao.getAllActivities().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getActivityById(id: Int): Activity? {
        Log.d("ActivityRepository", "Fetching activity with id: $id")
        return activityDao.getActivityById(id)?.toDomainModel()
    }

    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<Activity>> {
        Log.d("ActivityRepository", "Fetching activities from $startDate to $endDate")
        return activityDao.getActivitiesByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun insertActivity(activity: Activity): Long {
        Log.d("ActivityRepository", "Inserting activity: ${activity.type}")
        return activityDao.insertActivity(activity.toEntity())
    }

    suspend fun updateActivity(activity: Activity) {
        Log.d("ActivityRepository", "Updating activity with id: ${activity.id}")
        activityDao.updateActivity(activity.toEntity())
    }

    suspend fun deleteActivity(activity: Activity) {
        Log.d("ActivityRepository", "Deleting activity with id: ${activity.id}")
        activityDao.deleteActivity(activity.toEntity())
    }

    suspend fun getDailySummary(date: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        Log.d("ActivityRepository", "Calculating daily summary for date: $date")
        val totalSteps = activityDao.getTotalSteps(startOfDay, endOfDay) ?: 0
        val totalCalories = activityDao.getTotalCalories(startOfDay, endOfDay) ?: 0
        return Pair(totalSteps, totalCalories)
    }

    suspend fun getWeeklySummary(endDate: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfDay = calendar.timeInMillis + 24 * 60 * 60 * 1000
        val startOfWeek = endOfDay - 7 * 24 * 60 * 60 * 1000

        Log.d("ActivityRepository", "Calculating weekly summary ending: $endDate")
        val totalSteps = activityDao.getTotalSteps(startOfWeek, endOfDay) ?: 0
        val totalCalories = activityDao.getTotalCalories(startOfWeek, endOfDay) ?: 0
        return Pair(totalSteps, totalCalories)
    }
}