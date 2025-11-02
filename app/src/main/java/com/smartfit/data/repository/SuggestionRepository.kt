// FILE: app/src/main/java/com/smartfit/data/repository/SuggestionRepository.kt

package com.smartfit.data.repository

import android.util.Log
import com.smartfit.data.remote.ApiService
import com.smartfit.data.remote.dto.toDomainModel
import com.smartfit.domain.model.Suggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching workout suggestions from REST API or mock data.
 * Uses coroutines for non-blocking network calls.
 */
class SuggestionRepository(
    private val apiService: ApiService,
    private val useMockData: Boolean = false
) {

    suspend fun getSuggestions(limit: Int = 10): Result<List<Suggestion>> = withContext(Dispatchers.IO) {
        try {
            if (useMockData) {
                Log.d("SuggestionRepository", "Using mock data for suggestions")
                return@withContext Result.success(getMockSuggestions(limit))
            }

            Log.d("SuggestionRepository", "Fetching suggestions from API, limit: $limit")
            val response = apiService.getExercises(limit = limit)
            Log.d("SuggestionRepository", "Successfully fetched ${response.size} suggestions")
            Result.success(response.map { it.toDomainModel() })
        } catch (e: Exception) {
            Log.e("SuggestionRepository", "Error fetching suggestions: ${e.message}", e)
            // Fallback to mock data on error
            Log.d("SuggestionRepository", "Falling back to mock data")
            Result.success(getMockSuggestions(limit))
        }
    }

    private fun getMockSuggestions(limit: Int): List<Suggestion> {
        return listOf(
            Suggestion(
                id = "0001",
                title = "3/4 Sit-Up",
                shortDescription = "Abs • Body Weight • Beginner",
                fullDescription = "The 3/4 sit-up is an abdominal exercise performed with body weight. It involves curling the torso up to a 45-degree angle, engaging the abs, hip flexors, and lower back. This movement is commonly used to build core strength and stability.",
                bodyPart = "waist",
                equipment = "body weight",
                target = "abs",
                secondaryMuscles = listOf("hip flexors", "lower back"),
                instructions = listOf(
                    "Lie flat on your back with your knees bent and feet flat on the ground.",
                    "Place your hands behind your head with your elbows pointing outwards.",
                    "Engaging your abs, slowly lift your upper body off the ground, curling forward until your torso is at a 45-degree angle.",
                    "Pause for a moment at the top, then slowly lower your upper body back down to the starting position.",
                    "Repeat for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0002",
                title = "45° Side Bend",
                shortDescription = "Abs • Body Weight • Beginner",
                fullDescription = "The 45° side bend is a bodyweight exercise targeting the abdominal muscles, particularly the obliques. It involves bending the torso to the side while standing, engaging the core for stability and control.",
                bodyPart = "waist",
                equipment = "body weight",
                target = "abs",
                secondaryMuscles = listOf("obliques"),
                instructions = listOf(
                    "Stand with your feet shoulder-width apart and your arms extended straight down by your sides.",
                    "Keeping your back straight and your core engaged, slowly bend your torso to one side, lowering your hand towards your knee.",
                    "Pause for a moment at the bottom, then slowly return to the starting position.",
                    "Repeat on the other side.",
                    "Continue alternating sides for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0003",
                title = "Air Bike",
                shortDescription = "Abs • Body Weight • Beginner",
                fullDescription = "The air bike is a bodyweight exercise targeting the abdominal muscles and hip flexors. It involves a pedaling motion while lying on your back, alternating elbow-to-knee contact to engage the core.",
                bodyPart = "waist",
                equipment = "body weight",
                target = "abs",
                secondaryMuscles = listOf("hip flexors"),
                instructions = listOf(
                    "Lie flat on your back with your hands placed behind your head.",
                    "Lift your legs off the ground and bend your knees at a 90-degree angle.",
                    "Bring your right elbow towards your left knee while simultaneously straightening your right leg.",
                    "Return to the starting position and repeat the movement on the opposite side, bringing your left elbow towards your right knee while straightening your left leg.",
                    "Continue alternating sides in a pedaling motion for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0006",
                title = "Alternate Heel Touchers",
                shortDescription = "Abs • Body Weight • Beginner",
                fullDescription = "Alternate heel touchers is a bodyweight exercise targeting the abdominal muscles, particularly the obliques. It involves lying on your back, lifting your shoulders, and reaching side to side to touch your heels, engaging your core throughout.",
                bodyPart = "waist",
                equipment = "body weight",
                target = "abs",
                secondaryMuscles = listOf("obliques"),
                instructions = listOf(
                    "Lie flat on your back with your knees bent and feet flat on the ground.",
                    "Extend your arms straight out to the sides, parallel to the ground.",
                    "Engaging your abs, lift your shoulders off the ground and reach your right hand towards your right heel.",
                    "Return to the starting position and repeat on the left side, reaching your left hand towards your left heel.",
                    "Continue alternating sides for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0007",
                title = "Alternate Lateral Pulldown",
                shortDescription = "Lats • Cable • Beginner",
                fullDescription = "The alternate lateral pulldown is a cable machine exercise targeting the latissimus dorsi, with secondary emphasis on the biceps and rhomboids. It involves pulling handles towards the chest in an alternating fashion, focusing on back strength and muscle engagement.",
                bodyPart = "back",
                equipment = "cable",
                target = "lats",
                secondaryMuscles = listOf("biceps", "rhomboids"),
                instructions = listOf(
                    "Sit on the cable machine with your back straight and feet flat on the ground.",
                    "Grasp the handles with an overhand grip, slightly wider than shoulder-width apart.",
                    "Lean back slightly and pull the handles towards your chest, squeezing your shoulder blades together.",
                    "Pause for a moment at the peak of the movement, then slowly release the handles back to the starting position.",
                    "Repeat for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0009",
                title = "Assisted Chest Dip (Kneeling)",
                shortDescription = "Pectorals • Leverage Machine • Beginner",
                fullDescription = "The assisted chest dip (kneeling) is a chest-focused exercise performed on a leverage machine, where the user kneels on a pad for support. This machine-assisted variation helps reduce the load, making it accessible for those building strength or learning proper dip technique.",
                bodyPart = "chest",
                equipment = "leverage machine",
                target = "pectorals",
                secondaryMuscles = listOf("triceps", "shoulders"),
                instructions = listOf(
                    "Adjust the machine to your desired height and secure your knees on the pad.",
                    "Grasp the handles with your palms facing down and your arms fully extended.",
                    "Lower your body by bending your elbows until your upper arms are parallel to the floor.",
                    "Pause for a moment, then push yourself back up to the starting position.",
                    "Repeat for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0010",
                title = "Assisted Hanging Knee Raise with Throw Down",
                shortDescription = "Abs • Assisted • Advanced",
                fullDescription = "The assisted hanging knee raise with throw down is an advanced core exercise that targets the abdominal muscles, with additional engagement of the hip flexors and lower back. The movement involves hanging from a pull-up bar, raising the knees to the chest, and then explosively throwing the legs downward, requiring significant core strength, coordination, and control.",
                bodyPart = "waist",
                equipment = "assisted",
                target = "abs",
                secondaryMuscles = listOf("hip flexors", "lower back"),
                instructions = listOf(
                    "Hang from a pull-up bar with your arms fully extended and your palms facing away from you.",
                    "Engage your core and lift your knees towards your chest, keeping your legs together.",
                    "Once your knees are at chest level, explosively throw your legs down towards the ground, extending them fully.",
                    "Allow your legs to swing back up and repeat the movement for the desired number of repetitions."
                ),
                difficulty = "advanced",
                category = "strength"
            ),
            Suggestion(
                id = "0011",
                title = "Assisted Hanging Knee Raise",
                shortDescription = "Abs • Assisted • Beginner",
                fullDescription = "The assisted hanging knee raise is an abdominal exercise performed while hanging from a pull-up bar, using assistance to help lift the knees toward the chest. It primarily targets the abs and also works the hip flexors.",
                bodyPart = "waist",
                equipment = "assisted",
                target = "abs",
                secondaryMuscles = listOf("hip flexors"),
                instructions = listOf(
                    "Hang from a pull-up bar with your arms fully extended and your palms facing away from you.",
                    "Engage your core muscles and lift your knees towards your chest, bending at the hips and knees.",
                    "Pause for a moment at the top of the movement, squeezing your abs.",
                    "Slowly lower your legs back down to the starting position.",
                    "Repeat for the desired number of repetitions."
                ),
                difficulty = "beginner",
                category = "strength"
            ),
            Suggestion(
                id = "0012",
                title = "Assisted Lying Leg Raise with Lateral Throw Down",
                shortDescription = "Abs • Assisted • Intermediate",
                fullDescription = "The assisted lying leg raise with lateral throw down is an abdominal exercise that targets the abs while also engaging the hip flexors and obliques. The movement involves lifting the legs while lying on your back, then lowering them to each side in a controlled manner, which challenges core stability and strength.",
                bodyPart = "waist",
                equipment = "assisted",
                target = "abs",
                secondaryMuscles = listOf("hip flexors", "obliques"),
                instructions = listOf(
                    "Lie flat on your back with your legs extended and your arms by your sides.",
                    "Place your hands under your glutes for support.",
                    "Engage your abs and lift your legs off the ground, keeping them straight.",
                    "While keeping your legs together, lower them to one side until they are a few inches above the ground.",
                    "Pause for a moment, then lift your legs back to the starting position.",
                    "Repeat the movement to the other side.",
                    "Continue alternating sides for the desired number of repetitions."
                ),
                difficulty = "intermediate",
                category = "strength"
            ),
            Suggestion(
                id = "0013",
                title = "Assisted Lying Leg Raise with Throw Down",
                shortDescription = "Abs • Assisted • Intermediate",
                fullDescription = "The assisted lying leg raise with throw down is an abdominal exercise that targets the abs while also engaging the hip flexors and quadriceps. The movement involves raising the legs while lying on your back, then forcefully lowering them (throwing them down) and raising them again, often with a partner providing resistance or assistance.",
                bodyPart = "waist",
                equipment = "assisted",
                target = "abs",
                secondaryMuscles = listOf("hip flexors", "quadriceps"),
                instructions = listOf(
                    "Lie flat on your back with your legs extended and your arms by your sides.",
                    "Place your hands under your glutes for support.",
                    "Engage your core and lift your legs off the ground, keeping them straight.",
                    "Raise your legs until they are perpendicular to the ground.",
                    "Lower your legs back down to the starting position.",
                    "Simultaneously, throw your legs down towards the ground, keeping them straight.",
                    "Raise your legs back up to the starting position.",
                    "Repeat for the desired number of repetitions."
                ),
                difficulty = "intermediate",
                category = "strength"
            )
        ).take(limit)
    }
}