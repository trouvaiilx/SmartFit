// FILE: app/src/main/java/com/smartfit/domain/model/Meal.kt

package com.smartfit.domain.model

data class Meal(
    val id: Int = 0,
    val name: String,
    val calories: Int,
    val mealType: String,
    val date: Long,
    val portion: Double = 1.0,
    val notes: String = ""
)