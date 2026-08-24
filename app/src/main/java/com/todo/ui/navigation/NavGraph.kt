package com.todo.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.todo.ui.screen.chart.ChartScreen
import com.todo.ui.screen.preset.PresetScreen
import com.todo.ui.screen.recyclebin.RecycleBinScreen
import com.todo.ui.screen.settings.SettingsScreen
import com.todo.ui.screen.todo.HistoryScreen
import com.todo.ui.screen.todo.TodoScreen
import com.todo.ui.theme.MotionTokens

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Todo.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(
            route = Screen.Todo.route,
            enterTransition = {
                val forward = isTabForward(initialState, targetState)
                slideInHorizontally(
                    initialOffsetX = { width -> if (forward) width else -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            exitTransition = {
                val forward = isTabForward(initialState, targetState)
                slideOutHorizontally(
                    targetOffsetX = { width -> if (forward) -width else width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.Chart.route) {
                    slideInHorizontally(
                        initialOffsetX = { width -> -width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                } else if (initialState.destination.route == Screen.History.route) {
                    slideInHorizontally(
                        initialOffsetX = { width -> -width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                } else {
                    val forward = isTabForward(initialState, targetState)
                    slideInHorizontally(
                        initialOffsetX = { width -> if (forward) width else -width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                }
            },
            popExitTransition = {
                if (targetState.destination.route == Screen.Chart.route) {
                    slideOutHorizontally(
                        targetOffsetX = { width -> width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                } else {
                    val forward = isTabForward(initialState, targetState)
                    slideOutHorizontally(
                        targetOffsetX = { width -> if (forward) -width else width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                }
            }
        ) {
            TodoScreen(
                onNavigateToChart = { navController.navigate(Screen.Chart.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(
            route = Screen.History.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { width -> -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { width -> -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            }
        ) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Preset.route,
            enterTransition = {
                val forward = isTabForward(initialState, targetState)
                slideInHorizontally(
                    initialOffsetX = { width -> if (forward) width else -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            exitTransition = {
                val forward = isTabForward(initialState, targetState)
                slideOutHorizontally(
                    targetOffsetX = { width -> if (forward) -width else width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popEnterTransition = {
                val forward = isTabForward(initialState, targetState)
                slideInHorizontally(
                    initialOffsetX = { width -> if (forward) width else -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popExitTransition = {
                val forward = isTabForward(initialState, targetState)
                slideOutHorizontally(
                    targetOffsetX = { width -> if (forward) -width else width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            }
        ) {
            PresetScreen()
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                val forward = isTabForward(initialState, targetState)
                slideInHorizontally(
                    initialOffsetX = { width -> if (forward) width else -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            exitTransition = {
                if (targetState.destination.route == Screen.RecycleBin.route) {
                    slideOutHorizontally(
                        targetOffsetX = { width -> -width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                } else {
                    val forward = isTabForward(initialState, targetState)
                    slideOutHorizontally(
                        targetOffsetX = { width -> if (forward) -width else width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.RecycleBin.route) {
                    slideInHorizontally(
                        initialOffsetX = { width -> -width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                } else {
                    val forward = isTabForward(initialState, targetState)
                    slideInHorizontally(
                        initialOffsetX = { width -> if (forward) width else -width },
                        animationSpec = tween(
                            durationMillis = MotionTokens.ScreenSlide,
                            easing = MotionTokens.StandardEasing
                        )
                    )
                }
            },
            popExitTransition = {
                val forward = isTabForward(initialState, targetState)
                slideOutHorizontally(
                    targetOffsetX = { width -> if (forward) -width else width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            }
        ) {
            SettingsScreen(
                onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) }
            )
        }

        composable(
            route = Screen.RecycleBin.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { width -> -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            }
        ) {
            RecycleBinScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Chart.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { width -> -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { width -> -width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { width -> width },
                    animationSpec = tween(
                        durationMillis = MotionTokens.ScreenSlide,
                        easing = MotionTokens.StandardEasing
                    )
                )
            }
        ) {
            ChartScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun routeTabIndex(route: String?): Int = when (route) {
    Screen.Preset.route -> 0
    Screen.Todo.route -> 1
    Screen.Settings.route -> 2
    else -> -1
}

private fun isTabForward(initial: NavBackStackEntry, target: NavBackStackEntry): Boolean {
    val from = routeTabIndex(initial.destination.route)
    val to = routeTabIndex(target.destination.route)
    return if (from == -1 || to == -1) true else to >= from
}
