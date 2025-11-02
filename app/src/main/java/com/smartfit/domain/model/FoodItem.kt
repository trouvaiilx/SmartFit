// FILE: app/src/main/java/com/smartfit/domain/model/FoodItem.kt

package com.smartfit.domain.model

data class FoodItem(
    val name: String,
    val caloriesPer100g: Int,
    val category: String
)

object CommonFoods {
    val foods = listOf(
        // Breakfast
        FoodItem("Oatmeal", 68, "Breakfast"),
        FoodItem("Scrambled Eggs (2)", 140, "Breakfast"),
        FoodItem("Toast with Butter", 150, "Breakfast"),
        FoodItem("Banana", 89, "Breakfast"),
        FoodItem("Greek Yogurt", 59, "Breakfast"),
        FoodItem("Cereal with Milk", 200, "Breakfast"),
        FoodItem("Pancakes (3)", 350, "Breakfast"),
        FoodItem("Bagel with Cream Cheese", 290, "Breakfast"),

        // Lunch
        FoodItem("Chicken Breast (grilled)", 165, "Lunch"),
        FoodItem("Rice (1 cup)", 206, "Lunch"),
        FoodItem("Pasta (1 cup)", 220, "Lunch"),
        FoodItem("Salad with Dressing", 150, "Lunch"),
        FoodItem("Sandwich (turkey)", 320, "Lunch"),
        FoodItem("Burger", 540, "Lunch"),
        FoodItem("Pizza (2 slices)", 570, "Lunch"),
        FoodItem("Soup (1 bowl)", 180, "Lunch"),

        // Dinner
        FoodItem("Salmon (6 oz)", 350, "Dinner"),
        FoodItem("Steak (6 oz)", 460, "Dinner"),
        FoodItem("Vegetables (mixed)", 85, "Dinner"),
        FoodItem("Potatoes (mashed)", 237, "Dinner"),
        FoodItem("Pasta with Sauce", 400, "Dinner"),
        FoodItem("Stir Fry", 350, "Dinner"),
        FoodItem("Tacos (3)", 450, "Dinner"),

        // Snacks
        FoodItem("Apple", 95, "Snack"),
        FoodItem("Protein Bar", 200, "Snack"),
        FoodItem("Nuts (handful)", 170, "Snack"),
        FoodItem("Chips (small bag)", 150, "Snack"),
        FoodItem("Cookie", 140, "Snack"),
        FoodItem("Smoothie", 180, "Snack"),
        FoodItem("Dark Chocolate (1 oz)", 170, "Snack"),
        FoodItem("Popcorn (3 cups)", 90, "Snack")
    )

    fun getFoodsByCategory(category: String) = foods.filter { it.category == category }
}