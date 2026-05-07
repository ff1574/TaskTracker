package com.better.spark.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.better.spark.presentation.ui.FloatingBottomBar
import com.better.spark.presentation.ui.HomeScreen
import com.better.spark.presentation.ui.LifeCalendarScreen
import com.better.spark.presentation.ui.MotivationScreen
import com.better.spark.presentation.ui.ProgressScreen
import com.better.spark.presentation.ui.RelapseJournalScreen
import com.better.spark.presentation.ui.TemplatesScreen
import com.better.spark.presentation.ui.TaskListScreen
import com.better.spark.presentation.ui.BadHabitsScreen
import com.better.spark.presentation.viewmodel.TaskViewModel
import com.better.spark.presentation.viewmodel.BadHabitViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Navigation destinations for the app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TaskList : Screen("task_list")
    data object BadHabits : Screen("bad_habits")
    data object Progress : Screen("progress")
    data object Motivation : Screen("motivation")
    data object LifeCalendar : Screen("life_calendar")
    data object RelapseJournal : Screen("relapse_journal/{badHabitId}") {
        fun createRoute(badHabitId: String) = "relapse_journal/$badHabitId"
    }
    data object Templates : Screen("templates")
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
}

@Composable
fun TaskNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var homeResetKey by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == Screen.Home.route && currentRoute == Screen.Home.route) {
                        homeResetKey++
                        return@FloatingBottomBar
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    resetKey = homeResetKey,
                    onOpenLifeCalendar = { navController.navigate(Screen.LifeCalendar.route) }
                )
            }
            
            composable(Screen.TaskList.route) {
                val viewModel = koinViewModel<TaskViewModel>()
                TaskListScreen(
                    viewModel = viewModel,
                    onOpenTemplates = { navController.navigate(Screen.Templates.route) }
                )
            }
            
            composable(Screen.BadHabits.route) {
                val viewModel = koinViewModel<BadHabitViewModel>()
                BadHabitsScreen(
                    viewModel = viewModel,
                    onOpenJournal = { habitId ->
                        navController.navigate(Screen.RelapseJournal.createRoute(habitId))
                    }
                )
            }
            
            composable(Screen.Motivation.route) {
                MotivationScreen()
            }

            composable(Screen.LifeCalendar.route) {
                LifeCalendarScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.RelapseJournal.route) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("badHabitId") ?: return@composable
                RelapseJournalScreen(
                    badHabitId = habitId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen()
            }

            composable(Screen.Templates.route) {
                TemplatesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
