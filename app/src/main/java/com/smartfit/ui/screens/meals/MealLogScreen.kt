// FILE: app/src/main/java/com/smartfit/ui/screens/meals/MealLogScreen.kt

package com.smartfit.ui.screens.meals

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
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
import com.smartfit.domain.model.Meal
import com.smartfit.ui.screens.activitylog.TimePeriod
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MealLogScreen(
    viewModel: MealLogViewModel,
    onNavigateToAddMeal: () -> Unit,
    onNavigateToEditMeal: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Log") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMeal,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 90.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add meal")
            }
        }
    ) { paddingValues ->
        if (isTablet) {
            TabletMealLogLayout(
                uiState = uiState,
                onEditMeal = onNavigateToEditMeal,
                onDeleteMeal = viewModel::deleteMeal,
                onPeriodChange = viewModel::setTimePeriod,
                onToggleDateExpansion = viewModel::toggleDateExpansion,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            PhoneMealLogLayout(
                uiState = uiState,
                onEditMeal = onNavigateToEditMeal,
                onDeleteMeal = viewModel::deleteMeal,
                onPeriodChange = viewModel::setTimePeriod,
                onToggleDateExpansion = viewModel::toggleDateExpansion,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PhoneMealLogLayout(
    uiState: MealLogUiState,
    onEditMeal: (Int) -> Unit,
    onDeleteMeal: (Meal) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onToggleDateExpansion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 175.dp)
    ) {
        item {
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodChange = onPeriodChange
            )
        }

        item {
            CalorieBalanceCard(
                consumed = uiState.dailyCaloriesConsumed,
                burned = uiState.dailyCaloriesBurned,
                goal = uiState.calorieGoal,
                period = uiState.selectedPeriod
            )
        }

        if (uiState.meals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No meals logged yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Start tracking your nutrition!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val mealsByDate = uiState.meals.groupBy { meal ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = meal.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                calendar.timeInMillis
            }

            if (uiState.selectedPeriod == TimePeriod.THIS_WEEK) {
                mealsByDate.forEach { (date, mealsForDate) ->
                    item {
                        CollapsibleDateSection(
                            date = date,
                            meals = mealsForDate,
                            isExpanded = uiState.expandedDates.contains(date),
                            onToggleExpand = { onToggleDateExpansion(date) },
                            onEdit = onEditMeal,
                            onDelete = onDeleteMeal
                        )
                    }
                }
            } else {
                mealsByDate.forEach { (date, mealsForDate) ->
                    item {
                        DateHeader(date = date)
                    }
                    items(
                        items = mealsForDate,
                        key = { it.id }
                    ) { meal ->
                        MealCard(
                            meal = meal,
                            onEdit = { onEditMeal(meal.id) },
                            onDelete = { onDeleteMeal(meal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletMealLogLayout(
    uiState: MealLogUiState,
    onEditMeal: (Int) -> Unit,
    onDeleteMeal: (Meal) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onToggleDateExpansion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(0.3f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodChange = onPeriodChange
            )

            CalorieBalanceCard(
                consumed = uiState.dailyCaloriesConsumed,
                burned = uiState.dailyCaloriesBurned,
                goal = uiState.calorieGoal,
                period = uiState.selectedPeriod
            )
        }

        LazyColumn(
            modifier = Modifier.weight(0.7f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            if (uiState.meals.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(22.dp)
                        ) {
                            Text(
                                text = "No meals logged yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Start tracking your nutrition!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val mealsByDate = uiState.meals.groupBy { meal ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = meal.date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    calendar.timeInMillis
                }

                if (uiState.selectedPeriod == TimePeriod.THIS_WEEK) {
                    mealsByDate.forEach { (date, mealsForDate) ->
                        item {
                            CollapsibleDateSection(
                                date = date,
                                meals = mealsForDate,
                                isExpanded = uiState.expandedDates.contains(date),
                                onToggleExpand = { onToggleDateExpansion(date) },
                                onEdit = onEditMeal,
                                onDelete = onDeleteMeal
                            )
                        }
                    }
                } else {
                    mealsByDate.forEach { (date, mealsForDate) ->
                        item {
                            DateHeader(date = date)
                        }
                        items(
                            items = mealsForDate,
                            key = { it.id }
                        ) { meal ->
                            MealCard(
                                meal = meal,
                                onEdit = { onEditMeal(meal.id) },
                                onDelete = { onDeleteMeal(meal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodChange: (TimePeriod) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedPeriod == TimePeriod.TODAY,
            onClick = { onPeriodChange(TimePeriod.TODAY) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) {
            Text("Today")
        }
        SegmentedButton(
            selected = selectedPeriod == TimePeriod.THIS_WEEK,
            onClick = { onPeriodChange(TimePeriod.THIS_WEEK) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text("This Week")
        }
    }
}

@Composable
private fun CalorieBalanceCard(
    consumed: Int,
    burned: Int,
    goal: Int,
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    val netCalories = consumed - burned
    val remaining = goal - consumed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${period.name} calorie balance card" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (period) {
                    TimePeriod.TODAY -> "Today's Nutrition"
                    TimePeriod.THIS_WEEK -> "This Week"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Consumed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = consumed.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " cal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Burned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = burned.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = " cal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Net Calories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = netCalories.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (netCalories > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = " cal",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (netCalories > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            if (period == TimePeriod.TODAY) {
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = remaining.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = " cal",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                if (remaining < 0) {
                    Text(
                        text = "You've exceeded your daily calorie goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: Long) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    Text(
        text = dateFormat.format(Date(date)),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CollapsibleDateSection(
    date: Long,
    meals: List<Meal>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Meal) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onToggleExpand,
                        indication = ripple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateFormat.format(Date(date)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${meals.size} ${if (meals.size == 1) "meal" else "meals"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(if (isExpanded) 0f else 180f)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    meals.forEach { meal ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
                            },
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 3.dp,
                            shadowElevation = 2.dp
                        ) {
                            MealCardContent(
                                meal = meal,
                                onEdit = { onEdit(meal.id) },
                                onDelete = { onDelete(meal) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealCardContent(
    meal: Meal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onEdit,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(16.dp)
            .semantics { contentDescription = "Meal: ${meal.name}" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (meal.mealType.lowercase()) {
                        "breakfast" -> MaterialTheme.colorScheme.primaryContainer
                        "lunch" -> MaterialTheme.colorScheme.secondaryContainer
                        "dinner" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = meal.mealType.capitalize(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = timeFormat.format(Date(meal.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${meal.calories} cal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (meal.portion != 1.0) {
                    Text(
                        text = "Portion: ${meal.portion}x",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (meal.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meal.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }

        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete meal",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meal") },
            text = { Text("Are you sure you want to delete this meal?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}

@Composable
private fun MealCard(
    meal: Meal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onEdit,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .semantics { contentDescription = "Meal: ${meal.name}" }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = when (meal.mealType.lowercase()) {
                            "breakfast" -> MaterialTheme.colorScheme.primaryContainer
                            "lunch" -> MaterialTheme.colorScheme.secondaryContainer
                            "dinner" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = meal.mealType.capitalize(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = timeFormat.format(Date(meal.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "${meal.calories} cal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (meal.portion != 1.0) {
                        Text(
                            text = "Portion: ${meal.portion}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (meal.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete meal",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meal") },
            text = { Text("Are you sure you want to delete this meal?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}