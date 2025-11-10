// FILE: app/src/main/java/com/smartfit/ui/screens/activitylog/ActivityLogScreen.kt

package com.smartfit.ui.screens.activitylog

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartfit.domain.model.Activity
import com.smartfit.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ActivityLogScreen(
    viewModel: ActivityLogViewModel,
    onNavigateToAddActivity: () -> Unit,
    onNavigateToEditActivity: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
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
                    .navigationBarsPadding()
                    .padding(bottom = 90.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add activity")
            }
        }
    ) { paddingValues ->
        if (isTablet) {
            TabletActivityLogLayout(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onEditActivity = onNavigateToEditActivity,
                onDeleteActivity = viewModel::deleteActivity,
                onPeriodChange = viewModel::setTimePeriod,
                onToggleDateExpansion = viewModel::toggleDateExpansion,
                onAddActivity = onNavigateToAddActivity,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            PhoneActivityLogLayout(
                uiState = uiState,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onEditActivity = onNavigateToEditActivity,
                onDeleteActivity = viewModel::deleteActivity,
                onPeriodChange = viewModel::setTimePeriod,
                onToggleDateExpansion = viewModel::toggleDateExpansion,
                onAddActivity = onNavigateToAddActivity,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun PhoneActivityLogLayout(
    uiState: ActivityLogUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditActivity: (Int) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onToggleDateExpansion: (Long) -> Unit,
    onAddActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 175.dp)
    ) {
        // Period Selector
        item {
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodChange = onPeriodChange
            )
        }

        // Summary Card or Skeleton
        item {
            SummaryCard(
                steps = uiState.periodSteps,
                calories = uiState.periodCalories,
                period = uiState.selectedPeriod
            )
        }

        // Search Bar
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { /* Real-time search already active */ },
                placeholder = "Search activities...",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Activities List
        if (uiState.activities.isEmpty()) {
            item {
                if (searchQuery.isNotEmpty()) {
                    EmptySearchResultsState(searchQuery = searchQuery)
                } else {
                    EmptyActivitiesState(onAddClick = onAddActivity)
                }
            }
        } else {
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

            if (uiState.selectedPeriod == TimePeriod.THIS_WEEK) {
                activitiesByDate.forEach { (date, activitiesForDate) ->
                    item {
                        CollapsibleDateSection(
                            date = date,
                            activities = activitiesForDate,
                            isExpanded = uiState.expandedDates.contains(date),
                            onToggleExpand = { onToggleDateExpansion(date) },
                            onEdit = onEditActivity,
                            onDelete = onDeleteActivity
                        )
                    }
                }
            } else {
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
private fun TabletActivityLogLayout(
    uiState: ActivityLogUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditActivity: (Int) -> Unit,
    onDeleteActivity: (Activity) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onToggleDateExpansion: (Long) -> Unit,
    onAddActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
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

            SummaryCard(
                steps = uiState.periodSteps,
                calories = uiState.periodCalories,
                period = uiState.selectedPeriod
            )
        }

        Column(
            modifier = Modifier.weight(0.7f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { /* Real-time search already active */ },
                placeholder = "Search activities...",
                modifier = Modifier.fillMaxWidth()
            )

            // Activities List
            if (uiState.activities.isEmpty()) {
                if (searchQuery.isNotEmpty()) {
                    EmptySearchResultsState(searchQuery = searchQuery)
                } else {
                    EmptyActivitiesState(onAddClick = onAddActivity)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 160.dp)
                ) {
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

                    if (uiState.selectedPeriod == TimePeriod.THIS_WEEK) {
                        activitiesByDate.forEach { (date, activitiesForDate) ->
                            item {
                                CollapsibleDateSection(
                                    date = date,
                                    activities = activitiesForDate,
                                    isExpanded = uiState.expandedDates.contains(date),
                                    onToggleExpand = { onToggleDateExpansion(date) },
                                    onEdit = onEditActivity,
                                    onDelete = onDeleteActivity
                                )
                            }
                        }
                    } else {
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
    activities: List<Activity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Activity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    val cardShape = CardDefaults.shape

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .clickable(
                        onClick = onToggleExpand,
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp),
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
                        text = "${activities.size} ${if (activities.size == 1) "activity" else "activities"}",
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
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activities.forEach { activity ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 10.dp,
                        ) {
                            ActivityCardContent(
                                activity = activity,
                                onEdit = { onEdit(activity.id) },
                                onDelete = { onDelete(activity) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCardContent(
    activity: Activity,
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
            .semantics { contentDescription = "Activity: ${activity.type}" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeFormat.format(Date(activity.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activity.type,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

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

@Composable
private fun SummaryCard(
    steps: Int,
    calories: Int,
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${period.name} activity summary card" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (period) {
                    TimePeriod.TODAY -> "Today's Activities"
                    TimePeriod.THIS_WEEK -> "This Week"
                },
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
                    thickness = DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outlineVariant
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
    val cardShape = CardDefaults.shape

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
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
                Text(
                    text = timeFormat.format(Date(activity.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

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