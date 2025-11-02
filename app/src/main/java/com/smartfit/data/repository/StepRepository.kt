// FILE: app/src/main/java/com/smartfit/data/repository/StepRepository.kt

package com.smartfit.data.repository

import android.util.Log
import com.smartfit.data.local.database.StepCountDao
import com.smartfit.data.local.database.toEntity
import com.smartfit.data.local.database.toDomainModel
import com.smartfit.domain.model.StepCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class StepRepository(private val stepCountDao: StepCountDao) {

    fun getStepsByDateRange(startDate: Long, endDate: Long): Flow<List<StepCount>> {
        Log.d("StepRepository", "Fetching steps from $startDate to $endDate")
        return stepCountDao.getStepsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getDailySteps(date: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        Log.d("StepRepository", "Calculating daily steps for date: $date")
        return stepCountDao.getTotalSteps(startOfDay, endOfDay) ?: 0
    }

    suspend fun insertStepCount(stepCount: StepCount): Long {
        Log.d("StepRepository", "Inserting step count: ${stepCount.steps}")
        return stepCountDao.insertStepCount(stepCount.toEntity())
    }

    suspend fun getTodaysSensorSteps(): StepCount? {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        return stepCountDao.getTodaysSensorSteps(startOfDay, endOfDay)?.toDomainModel()
    }

    suspend fun cleanOldSteps() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        Log.d("StepRepository", "Cleaning steps older than 30 days")
        stepCountDao.deleteOldSteps(thirtyDaysAgo)
    }
}