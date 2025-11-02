// FILE: app/src/main/java/com/smartfit/data/local/database/ActivityEntity.kt

package com.smartfit.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartfit.domain.model.Activity

/**
 * Room entity for storing activity logs locally.
 */
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val duration: Int,
    val calories: Int,
    val steps: Int,
    val date: Long,
    val notes: String
)

fun ActivityEntity.toDomainModel() = Activity(
    id = id,
    type = type,
    duration = duration,
    calories = calories,
    steps = steps,
    date = date,
    notes = notes
)

fun Activity.toEntity() = ActivityEntity(
    id = id,
    type = type,
    duration = duration,
    calories = calories,
    steps = steps,
    date = date,
    notes = notes
)