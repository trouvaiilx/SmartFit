// FILE: app/src/main/java/com/smartfit/util/Constants.kt

package com.smartfit.util

object Constants {
    // Time Constants
    const val UI_INIT_DELAY_MS = 100L
    const val UI_SHORT_DELAY_MS = 50L
    const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    const val WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS
    const val THIRTY_DAYS_IN_MILLIS = 30 * DAY_IN_MILLIS

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 300
    const val ANIMATION_DURATION_MEDIUM = 400
    const val ANIMATION_DURATION_LONG = 800
    const val TYPEWRITER_DELAY_MS = 30L
    const val ERROR_AUTO_DISMISS_DELAY_MS = 5000L

    // Calorie Calculations
    const val CALORIES_PER_STEP = 0.04
    const val DEFAULT_USER_WEIGHT_KG = 70.0

    // MET Values for Activities
    object MetValues {
        const val WALKING = 3.5
        const val RUNNING = 8.0
        const val CYCLING = 6.0
        const val GYM = 5.0
        const val SWIMMING = 7.0
        const val YOGA = 2.5
        const val DEFAULT = 4.0
    }

    // Default Goals
    const val DEFAULT_STEP_GOAL = 10000
    const val DEFAULT_CALORIE_GOAL = 2000

    // Validation Limits
    const val MAX_ACTIVITY_DURATION_MINUTES = 1440 // 24 hours
    const val MAX_CALORIES = 10000
    const val MIN_CALORIES = 0
    const val MAX_PORTION_SIZE = 10.0
    const val MIN_PORTION_SIZE = 0.1
    const val MAX_TEXT_LENGTH = 500

    // Cache
    const val SUGGESTION_CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours

    // Step Counter
    const val STEP_UPDATE_THRESHOLD = 1

    // Notification
    const val STEP_COUNTER_NOTIFICATION_ID = 1
    const val STEP_COUNTER_CHANNEL_ID = "step_counter_channel"
}

object CalorieCalculator {
    /**
     * Calculate calories burned from steps
     */
    fun calculateStepCalories(steps: Int): Int {
        return (steps * Constants.CALORIES_PER_STEP).toInt()
    }

    /**
     * Calculate calories burned from activity
     * Formula: duration * MET value * 3.5 * weight(kg) / 200
     */
    fun calculateActivityCalories(
        durationMinutes: Int,
        activityType: String,
        weightKg: Double = Constants.DEFAULT_USER_WEIGHT_KG
    ): Int {
        val metValue = when (activityType.lowercase()) {
            "walking" -> Constants.MetValues.WALKING
            "running" -> Constants.MetValues.RUNNING
            "cycling" -> Constants.MetValues.CYCLING
            "gym" -> Constants.MetValues.GYM
            "swimming" -> Constants.MetValues.SWIMMING
            "yoga" -> Constants.MetValues.YOGA
            else -> Constants.MetValues.DEFAULT
        }

        return (durationMinutes * metValue * 3.5 * weightKg / 200).toInt()
    }
}

object DateUtils {
    /**
     * Get start of day timestamp
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Get end of day timestamp
     */
    fun getEndOfDay(timestamp: Long): Long {
        return getStartOfDay(timestamp) + Constants.DAY_IN_MILLIS
    }

    /**
     * Get start of week timestamp
     */
    fun getStartOfWeek(timestamp: Long): Long {
        return getEndOfDay(timestamp) - Constants.WEEK_IN_MILLIS
    }
}

object ValidationUtils {
    fun isValidDuration(duration: Int): Boolean {
        return duration > 0 && duration <= Constants.MAX_ACTIVITY_DURATION_MINUTES
    }

    fun isValidCalories(calories: Int): Boolean {
        return calories in Constants.MIN_CALORIES..Constants.MAX_CALORIES
    }

    fun isValidPortion(portion: Double): Boolean {
        return portion in Constants.MIN_PORTION_SIZE..Constants.MAX_PORTION_SIZE
    }

    fun isValidTextLength(text: String): Boolean {
        return text.length <= Constants.MAX_TEXT_LENGTH
    }

    fun getErrorMessage(field: String, value: Any): String {
        return when (field) {
            "duration" -> "Duration must be between 1 and ${Constants.MAX_ACTIVITY_DURATION_MINUTES} minutes"
            "calories" -> "Calories must be between ${Constants.MIN_CALORIES} and ${Constants.MAX_CALORIES}"
            "portion" -> "Portion size must be between ${Constants.MIN_PORTION_SIZE} and ${Constants.MAX_PORTION_SIZE}"
            "text" -> "Text must be less than ${Constants.MAX_TEXT_LENGTH} characters"
            else -> "Invalid value"
        }
    }
}