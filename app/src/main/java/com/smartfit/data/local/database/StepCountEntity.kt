// FILE: app/src/main/java/com/smartfit/data/local/database/StepCountEntity.kt

package com.smartfit.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartfit.domain.model.StepCount

@Entity(tableName = "step_counts")
data class StepCountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val steps: Int,
    val date: Long,
    val source: String = "sensor" // sensor or manual
)

fun StepCountEntity.toDomainModel() = StepCount(
    id = id,
    steps = steps,
    date = date,
    source = source
)

fun StepCount.toEntity() = StepCountEntity(
    id = id,
    steps = steps,
    date = date,
    source = source
)