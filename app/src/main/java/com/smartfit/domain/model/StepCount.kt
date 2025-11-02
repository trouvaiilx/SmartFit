// FILE: app/src/main/java/com/smartfit/domain/model/StepCount.kt

package com.smartfit.domain.model

data class StepCount(
    val id: Int = 0,
    val steps: Int,
    val date: Long,
    val source: String = "sensor"
)