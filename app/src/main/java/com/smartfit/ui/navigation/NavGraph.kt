// FILE: app/src/main/java/com/smartfit/ui/navigation/NavGraph.kt

package com.smartfit.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartfit.di.AppContainer
import com.smartfit.ui.screens.activitylog.ActivityLogScreen
import com.smartfit.ui.screens.activitylog.ActivityLogViewModel
import com.smartfit.ui.screens.activitylog.AddEditActivityScreen
import com.smartfit.ui.screens.activitylog.AddEditActivityViewModel
import com.smartfit.ui.screens.exercisedetail.ExerciseDetailScreen
import com.smartfit.ui.screens.exercisedetail.ExerciseDetailViewModel
import com.smartfit.ui.screens.home.HomeScreen
import com.smartfit.ui.screens.meals.AddEditMealScreen
import com.smartfit.ui.screens.meals.AddEditMealViewModel
import com.smartfit.ui.screens.meals.MealLogScreen
import com.smartfit.ui.screens.meals.MealLogViewModel
import com.smartfit.ui.screens.profile.ProfileScreen
import com.smartfit.ui.screens.profile.ProfileViewModel
import com.smartfit.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

/**
 * Main navigation scaffold with floating bottom navigation bar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavGraph(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    
    val currentPagerRoute = bottomNavItems[pagerState.currentPage].route
    val showBottomBar = currentDestination?.route == "home_pager"

    // Cache ViewModels to prevent recreation on navigation
    val activityLogViewModel = remember {
        ActivityLogViewModel(
            appContainer.activityRepository,
            appContainer.stepRepository
        )
    }

    // Get calorie goal for MealLogViewModel
    val calorieGoal by appContainer.preferencesManager.dailyCalorieGoalFlow.collectAsState(initial = 2000)

    val mealLogViewModel = remember(calorieGoal) {
        MealLogViewModel(
            appContainer.mealRepository,
            appContainer.activityRepository,
            appContainer.stepRepository,
            calorieGoal
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home_pager",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home_pager") {
                    HorizontalPager(state = pagerState) { page ->
                        when (bottomNavItems[page].route) {
                            "home" -> {
                                val viewModel = appContainer.homeViewModel ?: appContainer.apply {
                                    homeViewModel = com.smartfit.ui.screens.home.HomeViewModel(
                                        activityRepository = activityRepository,
                                        suggestionRepository = suggestionRepository,
                                        mealRepository = mealRepository,
                                        stepRepository = stepRepository,
                                        appContainer = this
                                    )
                                }.homeViewModel!!

                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToExerciseDetail = { exerciseId ->
                                        navController.navigate("exercise_detail/$exerciseId")
                                    },
                                    onNavigateToSettings = {
                                        navController.navigate("settings")
                                    }
                                )
                            }
                            "activity_log" -> ActivityLogScreen(
                                viewModel = activityLogViewModel,
                                onNavigateToAddActivity = { navController.navigate("add_activity") },
                                onNavigateToEditActivity = { activityId ->
                                    navController.navigate("edit_activity/$activityId")
                                }
                            )
                            "meals" -> MealLogScreen(
                                viewModel = mealLogViewModel,
                                onNavigateToAddMeal = { navController.navigate("add_meal") },
                                onNavigateToEditMeal = { mealId ->
                                    navController.navigate("edit_meal/$mealId")
                                }
                            )
                            "profile" -> {
                                val viewModel = remember {
                                    ProfileViewModel(appContainer.preferencesManager)
                                }
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onStartStepTracking = appContainer::startStepCounterService,
                                    onStopStepTracking = appContainer::stopStepCounterService
                                )
                            }
                        }
                    }
                }

                composable("settings") {
                    val viewModel = remember {
                        ProfileViewModel(appContainer.preferencesManager)
                    }
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("add_activity") {
                    val viewModel = remember {
                        AddEditActivityViewModel(appContainer.activityRepository)
                    }
                    AddEditActivityScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        isEditMode = false
                    )
                }

                composable(
                    route = "edit_activity/{activityId}",
                    arguments = listOf(navArgument("activityId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                    val viewModel = remember(activityId) {
                        AddEditActivityViewModel(appContainer.activityRepository)
                    }
                    AddEditActivityScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        isEditMode = true,
                        activityId = activityId
                    )
                }

                composable("add_meal") {
                    val viewModel = remember {
                        AddEditMealViewModel(appContainer.mealRepository)
                    }
                    AddEditMealScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        isEditMode = false
                    )
                }

                composable(
                    route = "edit_meal/{mealId}",
                    arguments = listOf(navArgument("mealId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getInt("mealId") ?: 0
                    val viewModel = remember(mealId) {
                        AddEditMealViewModel(appContainer.mealRepository)
                    }
                    AddEditMealScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        isEditMode = true,
                        mealId = mealId
                    )
                }

                composable(
                    route = "exercise_detail/{exerciseId}",
                    arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                    val viewModel = remember(exerciseId) {
                        ExerciseDetailViewModel(
                            appContainer = appContainer,
                            exerciseId = exerciseId
                        )
                    }
                    ExerciseDetailScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Animated bottom navigation bar
        AnimatedVisibility(
            visible = showBottomBar,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { it }
            ),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomNavigationBar(
                items = bottomNavItems,
                currentRoute = currentPagerRoute,
                onItemClick = { item ->
                    val newIndex = bottomNavItems.indexOfFirst { it.route == item.route }
                    if (pagerState.currentPage != newIndex) {
                        coroutineScope.launch {
                            pagerState.scrollToPage(newIndex)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FloatingBottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            .copy(alpha = 1.0f),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    selected = selected,
                    onClick = { onItemClick(item) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
