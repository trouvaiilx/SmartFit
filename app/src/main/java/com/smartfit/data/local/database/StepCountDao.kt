// FILE: app/src/main/java/com/smartfit/data/local/database/StepCountDao.kt

package com.smartfit.data.local.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepCountDao {
    @Query("SELECT * FROM step_counts WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    fun getStepsByDateRange(startDate: Long, endDate: Long): Flow<List<StepCountEntity>>

    @Query("SELECT SUM(steps) FROM step_counts WHERE date >= :startDate AND date < :endDate")
    suspend fun getTotalSteps(startDate: Long, endDate: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepCount(stepCount: StepCountEntity): Long

    @Query("DELETE FROM step_counts WHERE date < :beforeDate")
    suspend fun deleteOldSteps(beforeDate: Long)

    @Query("SELECT * FROM step_counts WHERE date >= :startDate AND date < :endDate AND source = 'sensor' LIMIT 1")
    suspend fun getTodaysSensorSteps(startDate: Long, endDate: Long): StepCountEntity?
}