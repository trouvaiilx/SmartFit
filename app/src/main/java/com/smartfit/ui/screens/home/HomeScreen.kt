// FILE: app/src/main/java/com/smartfit/ui/screens/home/HomeScreen.kt

package com.smartfit.ui.screens.home

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartfit.domain.model.Suggestion
import com.smartfit.ui.components.AnimatedSkeletonSuggestion
import com.smartfit.ui.components.TypewriterText
import kotlinx.coroutines.delay

/**
 * Home screen displaying daily summary and workout suggestions.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToExerciseDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartFit") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isTablet) {
            TabletHomeLayout(
                uiState = uiState,
                onLoadSuggestions = viewModel::loadSuggestions,
                onToggleExpanded = viewModel::toggleSuggestionsExpanded,
                onSuggestionClick = onNavigateToExerciseDetail,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            PhoneHomeLayout(
                uiState = uiState,
                onLoadSuggestions = viewModel::loadSuggestions,
                onToggleExpanded = viewModel::toggleSuggestionsExpanded,
                onSuggestionClick = onNavigateToExerciseDetail,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PhoneHomeLayout(
    uiState: HomeUiState,
    onLoadSuggestions: () -> Unit,
    onToggleExpanded: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 105.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                DailySummaryCard(
                    steps = uiState.dailySteps,
                    caloriesBurned = uiState.dailyCaloriesBurned,
                    caloriesConsumed = uiState.dailyCaloriesConsumed,
                    netCalories = uiState.netCalories
                )
            }
        }

        item {
            SuggestionsSection(
                suggestions = uiState.suggestions,
                isLoading = uiState.isLoadingSuggestions,
                error = uiState.suggestionsError,
                suggestionsLoaded = uiState.suggestionsLoaded,
                isSuggestionsExpanded = uiState.isSuggestionsExpanded,
                onLoadSuggestions = onLoadSuggestions,
                onToggleExpanded = onToggleExpanded,
                isContainedInScrollingParent = true, // The parent scrolls
                onSuggestionClick = onSuggestionClick
            )
        }
    }
}

@Composable
private fun TabletHomeLayout(
    uiState: HomeUiState,
    onLoadSuggestions: () -> Unit,
    onToggleExpanded: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DailySummaryCard(
                steps = uiState.dailySteps,
                caloriesBurned = uiState.dailyCaloriesBurned,
                caloriesConsumed = uiState.dailyCaloriesConsumed,
                netCalories = uiState.netCalories
            )
        }

        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            SuggestionsSection(
                suggestions = uiState.suggestions,
                isLoading = uiState.isLoadingSuggestions,
                error = uiState.suggestionsError,
                suggestionsLoaded = uiState.suggestionsLoaded,
                isSuggestionsExpanded = uiState.isSuggestionsExpanded,
                onLoadSuggestions = onLoadSuggestions,
                onToggleExpanded = onToggleExpanded,
                isContainedInScrollingParent = false, // The parent does not scroll
                onSuggestionClick = onSuggestionClick
            )
        }
    }
}

@Composable
private fun DailySummaryCard(
    steps: Int,
    caloriesBurned: Int,
    caloriesConsumed: Int,
    netCalories: Int,
    modifier: Modifier = Modifier
) {
    // Track if this is the first load
    var isFirstLoad by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(100)
        isFirstLoad = false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Daily activity summary card" }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Steps
            SummaryRow(
                label = "Steps",
                value = steps.toString(),
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Calories Consumed
            SummaryRow(
                label = "Calories Consumed",
                value = "$caloriesConsumed cal",
                color = MaterialTheme.colorScheme.tertiary
            )

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Calories Burned
            SummaryRow(
                label = "Calories Burned",
                value = "$caloriesBurned cal",
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Net Calories
            SummaryRow(
                label = "Net Calories",
                value = "$netCalories cal",
                color = if (netCalories > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            )

            // Animated message - only shows after first load
            AnimatedVisibility(
                visible = !isFirstLoad && netCalories != 0,
                enter = fadeIn(tween(300)) + expandVertically(tween(300))
            ) {
                if (netCalories > 0) {
                    TypewriterText(
                        text = "You're in a calorie surplus. 🍔",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (netCalories < 0) {
                    TypewriterText(
                        text = "You're in a calorie deficit. 🔥",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SuggestionsSection(
    suggestions: List<Suggestion>,
    isLoading: Boolean,
    error: String?,
    suggestionsLoaded: Boolean,
    isSuggestionsExpanded: Boolean,
    onLoadSuggestions: () -> Unit,
    onToggleExpanded: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    isContainedInScrollingParent: Boolean,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isSuggestionsExpanded) 0f else 180f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "caret rotation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = suggestionsLoaded,
                    onClick = { onToggleExpanded() },
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Workout Suggestions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (suggestionsLoaded) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isSuggestionsExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }

        when {
            !suggestionsLoaded && !isLoading -> {
                // Show prompt to load suggestions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Want personalized workout suggestions?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Button(onClick = onLoadSuggestions) {
                            Text("Load Suggestions")
                        }
                    }
                }
            }
            isLoading -> {
                AnimatedSkeletonSuggestion()
            }
            error != null -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(800)) + expandVertically(tween(800)),
                    exit = fadeOut(tween(500)) + shrinkVertically(tween(500))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = onLoadSuggestions) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            suggestions.isNotEmpty() -> {
                // Use single animation - only on expand/collapse, not individual cards
                AnimatedVisibility(
                    visible = isSuggestionsExpanded,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) + expandVertically(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ) + shrinkVertically(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    if (isContainedInScrollingParent) {
                        // FOR PHONES: Use a simple Column because the parent scrolls
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestions.forEach { suggestion ->
                                SuggestionCard(
                                    suggestion = suggestion,
                                    onClick = { onSuggestionClick(suggestion.id) }
                                )
                            }
                        }
                    } else {
                        // FOR TABLETS: Use a LazyColumn because the parent does not scroll
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(
                                items = suggestions,
                                key = { it.id }
                            ) { suggestion ->
                                SuggestionCard(
                                    suggestion = suggestion,
                                    onClick = { onSuggestionClick(suggestion.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: Suggestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .semantics { contentDescription = "Exercise suggestion: ${suggestion.title}" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = suggestion.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}