// FILE: app/src/main/java/com/smartfit/ui/screens/activitylog/AddEditActivityScreen.kt

package com.smartfit.ui.screens.activitylog

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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

/**
 * Screen for adding or editing activity entries with adaptive layout.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AddEditActivityScreen(
    viewModel: AddEditActivityViewModel,
    onNavigateBack: () -> Unit,
    isEditMode: Boolean,
    activityId: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    // Load activity data when in edit mode - no loading screen
    LaunchedEffect(activityId) {
        if (isEditMode && activityId > 0) {
            viewModel.loadActivity(activityId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Activity" else "Log Activity") },
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
            ActivityForm(
                uiState = uiState,
                viewModel = viewModel
            )

            Button(
                onClick = { viewModel.saveActivity(onNavigateBack) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Update Activity" else "Save Activity")
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
private fun ActivityForm(
    uiState: AddEditActivityUiState,
    viewModel: AddEditActivityViewModel
) {
    val activityTypes = listOf("Walking", "Running", "Cycling", "Gym", "Swimming", "Yoga", "Other")
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = "Activity Type",
        style = MaterialTheme.typography.labelLarge
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = uiState.type,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                    enabled = true
                )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            activityTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        viewModel.updateType(type)
                        expanded = false
                    }
                )
            }
        }
    }

    // Show custom activity name input if "Other" is selected
    AnimatedVisibility(
        visible = uiState.type == "Other",
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
    ) {
        OutlinedTextField(
            value = uiState.customActivityName,
            onValueChange = { viewModel.updateCustomActivityName(it) },
            label = { Text("Activity Name") },
            placeholder = { Text("e.g., Boxing, Dancing, etc.") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    OutlinedTextField(
        value = uiState.duration,
        onValueChange = { viewModel.updateDuration(it) },
        label = { Text("Duration (minutes)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = uiState.calories,
        onValueChange = { viewModel.updateCalories(it) },
        label = {
            Text(if (uiState.type == "Other") "Calories" else "Calories (auto-calculated)")
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        supportingText = if (uiState.type == "Other") {
            { Text("Enter calories manually") }
        } else null
    )

    OutlinedTextField(
        value = uiState.notes,
        onValueChange = { viewModel.updateNotes(it) },
        label = { Text("Notes (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5
    )
}