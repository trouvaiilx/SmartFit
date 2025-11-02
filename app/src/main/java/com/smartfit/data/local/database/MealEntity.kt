// FILE: app/src/main/java/com/smartfit/data/local/database/MealEntity.kt

package com.smartfit.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartfit.domain.model.Meal

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val calories: Int,
    val mealType: String, // breakfast, lunch, dinner, snack
    val date: Long,
    val portion: Double = 1.0,
    val notes: String = ""
)

fun MealEntity.toDomainModel() = Meal(
    id = id,
    name = name,
    calories = calories,
    mealType = mealType,
    date = date,
    portion = portion,
    notes = notes
)

fun Meal.toEntity() = MealEntity(
    id = id,
    name = name,
    calories = calories,
    mealType = mealType,
    date = date,
    portion = portion,
    notes = notes
)