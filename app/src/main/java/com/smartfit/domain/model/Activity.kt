// FILE: app/src/main/java/com/smartfit/MainActivity.kt

package com.smartfit.domain.model

/**
 * Domain model representing a user activity log entry.
 */
data class Activity(
    val id: Int = 0,
    val type: String, // e.g., "Walking", "Running", "Cycling", "Gym"
    val duration: Int, // minutes
    val calories: Int,
    val steps: Int = 0,
    val date: Long, // timestamp
    val notes: String = ""
)