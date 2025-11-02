// FILE: app/src/main/java/com/smartfit/ui/screens/activitylog/ActivityLogScreen.kt

package com.smartfit.ui.screens.activitylog

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartfit.domain.model.Activity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity Log screen for viewing, editing, and deleting activities.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ActivityLogScreen(
    viewModel: ActivityLogViewModel,
    onNavigateToAddActivity: () -> Unit,
    onNavigateToEditActivity: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current as android.app.Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Log") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddActivity,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .navigationBarsPadding() // Respect system nav bar
                    .padding(bottom = 90.dp) // Lift above navbar
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add activity")
            }
        }
    ) { paddingValues ->
        if (isTablet) {
            TabletActivityLogLayout(
                uiState = uiState,
                onEditActivity = onNavigateToEditActivity,
                onDeleteActivity = viewModel::deleteActivity,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            PhoneActivityLogLayout(
                uiState = uiState,
                onEditActivity = onNavigateToEditActivity,
                onDeleteActivity = viewModel::deleteActivity,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PhoneActivityLogLayout(
    uiState: ActivityLogUiState,
    onEditActivity: (Int) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
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
            WeeklySummaryCard(
                steps = uiState.weeklySteps,
                calories = uiState.weeklyCalories
            )
        }

        if (uiState.activities.isEmpty()) {
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
                            text = "No activities yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add your first activity!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Group activities by date
            val activitiesByDate = uiState.activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = activity.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                calendar.timeInMillis
            }

            activitiesByDate.forEach { (date, activitiesForDate) ->
                item {
                    DateHeader(date = date)
                }
                items(
                    items = activitiesForDate,
                    key = { it.id }
                ) { activity ->
                    ActivityCard(
                        activity = activity,
                        onEdit = { onEditActivity(activity.id) },
                        onDelete = { onDeleteActivity(activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletActivityLogLayout(
    uiState: ActivityLogUiState,
    onEditActivity: (Int) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeeklySummaryCard(
            steps = uiState.weeklySteps,
            calories = uiState.weeklyCalories,
            modifier = Modifier
                .weight(0.3f)
        )

        LazyColumn(
            modifier = Modifier.weight(0.7f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            if (uiState.activities.isEmpty()) {
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
                                text = "No activities yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add your first activity!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Group activities by date
                val activitiesByDate = uiState.activities.groupBy { activity ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = activity.date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    calendar.timeInMillis
                }

                activitiesByDate.forEach { (date, activitiesForDate) ->
                    item {
                        DateHeader(date = date)
                    }
                    items(
                        items = activitiesForDate,
                        key = { it.id }
                    ) { activity ->
                        ActivityCard(
                            activity = activity,
                            onEdit = { onEditActivity(activity.id) },
                            onDelete = { onDeleteActivity(activity) }
                        )
                    }
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
private fun WeeklySummaryCard(
    steps: Int,
    calories: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Weekly activity summary card" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = steps.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = calories.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
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
            .semantics { contentDescription = "Activity: ${activity.type}" }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Time above title
                Text(
                    text = timeFormat.format(Date(activity.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = activity.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration and colored calories
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "${activity.duration} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${activity.calories} cal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (activity.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete activity",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Activity") },
            text = { Text("Are you sure you want to delete this activity?") },
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