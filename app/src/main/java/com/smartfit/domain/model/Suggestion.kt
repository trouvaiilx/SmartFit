// FILE: app/src/main/java/com/smartfit/domain/model/Suggestion.kt

package com.smartfit.domain.model

/**
 * Domain model for workout or nutrition suggestions from API.
 * Contains comprehensive exercise information.
 */
data class Suggestion(
    val id: String,
    val title: String,
    val shortDescription: String,
    val fullDescription: String = "",
    val imageUrl: String = "",
    val bodyPart: String = "",
    val equipment: String = "",
    val target: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val difficulty: String = "",
    val category: String = ""
)