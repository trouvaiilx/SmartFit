// FILE: app/src/main/java/com/smartfit/data/remote/dto/SuggestionDto.kt

package com.smartfit.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.smartfit.domain.model.Suggestion

/**
 * Data Transfer Object for ExerciseDB API responses.
 * Maps the actual API response structure to domain models.
 */
data class SuggestionDto(
    val id: String,
    val name: String,
    @SerializedName("bodyPart")
    val bodyPart: String,
    val equipment: String,
    val target: String,
    @SerializedName("secondaryMuscles")
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val description: String? = null,
    val difficulty: String? = null,
    val category: String? = null
)

fun SuggestionDto.toDomainModel() = Suggestion(
    id = id,
    title = name.capitalize(),
    shortDescription = buildShortDescription(),
    fullDescription = description ?: "",
    imageUrl = "",
    bodyPart = bodyPart,
    equipment = equipment,
    target = target,
    secondaryMuscles = secondaryMuscles,
    instructions = instructions,
    difficulty = difficulty ?: "",
    category = category ?: ""
)

private fun SuggestionDto.buildShortDescription(): String {
    val parts = mutableListOf<String>()

    parts.add(target.capitalize())
    parts.add(equipment.capitalize())
    difficulty?.let { parts.add(it.capitalize()) }

    return parts.joinToString(" • ")
}

private fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}