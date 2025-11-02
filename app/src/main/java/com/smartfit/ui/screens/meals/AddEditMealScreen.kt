// FILE: app/src/main/java/com/smartfit/ui/screens/meals/AddEditMealScreen.kt
package com.smartfit.ui.screens.meals

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartfit.ui.components.AnimatedErrorMessage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AddEditMealScreen(
    viewModel: AddEditMealViewModel,
    onNavigateBack: () -> Unit,
    isEditMode: Boolean = false,
    mealId: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    // Load meal data when in edit mode
    LaunchedEffect(mealId) {
        if (isEditMode && mealId > 0) {
            viewModel.loadMeal(mealId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Meal" else "Log Meal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .then(if (isTablet) Modifier.width(600.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MealForm(
                uiState = uiState,
                viewModel = viewModel,
                isEditMode = isEditMode
            )

            Button(
                onClick = { viewModel.saveMeal(onNavigateBack) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Update Meal" else "Save Meal")
            }
        }
    }

    // Animated error message overlay
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedErrorMessage(
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.clearError() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealForm(
    uiState: AddEditMealUiState,
    viewModel: AddEditMealViewModel,
    isEditMode: Boolean
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    var mealTypeExpanded by remember { mutableStateOf(false) }
    var foodSelectorExpanded by remember { mutableStateOf(false) }

    // Meal Type
    Text(
        text = "Meal Type",
        style = MaterialTheme.typography.labelLarge
    )
    ExposedDropdownMenuBox(
        expanded = mealTypeExpanded,
        onExpandedChange = { mealTypeExpanded = !mealTypeExpanded }
    ) {
        OutlinedTextField(
            value = uiState.mealType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Meal Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealTypeExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = mealTypeExpanded,
            onDismissRequest = { mealTypeExpanded = false }
        ) {
            mealTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        viewModel.updateMealType(type)
                        mealTypeExpanded = false
                    }
                )
            }
        }
    }

    // Food Selection - only show if not in edit mode
    if (!isEditMode) {
        Text(
            text = "Select Food",
            style = MaterialTheme.typography.labelLarge
        )
        ExposedDropdownMenuBox(
            expanded = foodSelectorExpanded,
            onExpandedChange = { foodSelectorExpanded = !foodSelectorExpanded }
        ) {
            OutlinedTextField(
                value = uiState.selectedFood?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Choose from common foods") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = foodSelectorExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = foodSelectorExpanded,
                onDismissRequest = { foodSelectorExpanded = false }
            ) {
                uiState.availableFoods.forEach { food ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(food.name)
                                Text(
                                    text = "${food.caloriesPer100g} cal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            viewModel.selectFood(food)
                            foodSelectorExpanded = false
                        }
                    )
                }
            }
        }

        // Or custom name
        Text(
            text = "Or Enter Custom Food",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        // In edit mode, show food name as label
        Text(
            text = "Food Name",
            style = MaterialTheme.typography.labelLarge
        )
    }

    OutlinedTextField(
        value = uiState.customFoodName,
        onValueChange = { viewModel.updateCustomFoodName(it) },
        label = { Text("Food Name") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isEditMode
    )

    // Portion size
    OutlinedTextField(
        value = uiState.portion,
        onValueChange = { viewModel.updatePortion(it) },
        label = { Text("Portion Size (servings)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = { Text("1.0 = standard serving") },
        modifier = Modifier.fillMaxWidth()
    )

    // Calories (auto-calculated or manual)
    OutlinedTextField(
        value = uiState.calories,
        onValueChange = { viewModel.updateCalories(it) },
        label = { Text("Calories") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            if (uiState.selectedFood != null) {
                Text("Auto-calculated based on portion")
            } else {
                Text("Enter manually for custom foods")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    // Notes
    OutlinedTextField(
        value = uiState.notes,
        onValueChange = { viewModel.updateNotes(it) },
        label = { Text("Notes (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5
    )

    // Preview
    if (uiState.selectedFood != null || uiState.customFoodName.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Meal Preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = uiState.selectedFood?.name ?: uiState.customFoodName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${uiState.calories} calories",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}