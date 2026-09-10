package com.todo.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
            enterTransition = { tabEnter(isTabForward(initialState, targetState)) },
            exitTransition = {
                if (targetState.destination.route == Screen.Chart.route ||
                    targetState.destination.route == Screen.History.route
                ) {
                    detailExit()
                } else {
                    tabExit(isTabForward(initialState, targetState))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.Chart.route ||
                    initialState.destination.route == Screen.History.route
                ) {
                    detailPopEnter()
                } else {
                    tabEnter(isTabForward(initialState, targetState))
                }
            },
            popExitTransition = {
                if (targetState.destination.route == Screen.Chart.route) {
                    detailPopExit()
                } else {
                    tabExit(isTabForward(initialState, targetState))
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
            enterTransition = { detailEnter() },
            exitTransition = { detailExit() },
            popEnterTransition = { detailPopEnter() },
            popExitTransition = { detailPopExit() }
        ) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Preset.route,
            enterTransition = { tabEnter(isTabForward(initialState, targetState)) },
            exitTransition = { tabExit(isTabForward(initialState, targetState)) },
            popEnterTransition = { tabEnter(isTabForward(initialState, targetState)) },
            popExitTransition = { tabExit(isTabForward(initialState, targetState)) }
        ) {
            PresetScreen()
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = { tabEnter(isTabForward(initialState, targetState)) },
            exitTransition = {
                if (targetState.destination.route == Screen.RecycleBin.route) {
                    detailExit()
                } else {
                    tabExit(isTabForward(initialState, targetState))
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.RecycleBin.route) {
                    detailPopEnter()
                } else {
                    tabEnter(isTabForward(initialState, targetState))
                }
            },
            popExitTransition = { tabExit(isTabForward(initialState, targetState)) }
        ) {
            SettingsScreen(
                onNavigateToRecycleBin = { navController.navigate(Screen.RecycleBin.route) }
            )
        }

        composable(
            route = Screen.RecycleBin.route,
            enterTransition = { detailEnter() },
            exitTransition = { detailExit() },
            popEnterTransition = { detailPopEnter() },
            popExitTransition = { detailPopExit() }
        ) {
            RecycleBinScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Chart.route,
            enterTransition = { detailEnter() },
            exitTransition = { detailExit() },
            popEnterTransition = { detailPopEnter() },
            popExitTransition = { detailPopExit() }
        ) {
            ChartScreen(onBack = { navController.popBackStack() })
        }
    }
}

// --- Tab 切换：含蓄的淡入 + 微滑 + 微缩 —— 更"高级"、不生硬 ---
private fun tabEnter(forward: Boolean): EnterTransition =
    fadeIn(tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing)) +
        slideInHorizontally(
            initialOffsetX = { if (forward) it / 8 else -it / 8 },
            animationSpec = tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing)
        ) +
        scaleIn(
            initialScale = 0.99f,
            animationSpec = tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing)
        )

private fun tabExit(forward: Boolean): ExitTransition =
    fadeOut(tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing)) +
        slideOutHorizontally(
            targetOffsetX = { if (forward) -it / 10 else it / 10 },
            animationSpec = tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing)
        ) +
        scaleOut(
            targetScale = 0.99f,
            animationSpec = tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing)
        )

// --- Push 详情页：纯交叉淡入淡出（无位移、无缩放、无纵深）---
// 新页面永远在最上层，与旧页面同位置、同贴屏幕边缘完全重叠。
// 因无 slide / scale，旧页面边缘永远不会从新页面之下露出——彻底杜绝露底硬边。
private fun detailEnter(): EnterTransition =
    fadeIn(tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing))

private fun detailExit(): ExitTransition =
    fadeOut(tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing))

private fun detailPopEnter(): EnterTransition =
    fadeIn(tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing))

private fun detailPopExit(): ExitTransition =
    fadeOut(tween(MotionTokens.ScreenSlide, easing = MotionTokens.StandardEasing))

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
