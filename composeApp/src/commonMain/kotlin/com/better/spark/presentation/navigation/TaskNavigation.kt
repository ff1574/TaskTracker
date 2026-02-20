package com.better.spark.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.better.spark.presentation.ui.FloatingBottomBar
import com.better.spark.presentation.ui.HomeScreen
import com.better.spark.presentation.ui.MotivationScreen
import com.better.spark.presentation.ui.TaskListScreen
import com.better.spark.presentation.viewmodel.TaskViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Navigation destinations for the app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object TaskList : Screen("task_list")
    data object Motivation : Screen("motivation")
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

    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
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
                HomeScreen()
            }
            
            composable(Screen.TaskList.route) {
                val viewModel = koinViewModel<TaskViewModel>()
                TaskListScreen(
                    viewModel = viewModel
                )
            }
            
            composable(Screen.Motivation.route) {
                MotivationScreen()
            }
        }
    }
}
