package com.todo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.todo.ui.component.GradientBackground
import com.todo.ui.navigation.BottomNavBar
import com.todo.ui.navigation.NavGraph
import com.todo.ui.navigation.Screen
import com.todo.ui.theme.TodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                ConfigureSystemBars()
                TodoRoot()
            }
        }
    }
}

@Composable
private fun ConfigureSystemBars() {
    val view = LocalView.current
    val context = LocalContext.current
    val activity = context.findActivity()
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    SideEffect {
        val window = activity?.window ?: return@SideEffect
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.parseColor("#0F0F11")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = false
        }
    }
}

@Composable
private fun TodoRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = listOf(Screen.Preset.route, Screen.Todo.route, Screen.Settings.route)
        .any { route -> currentDestination?.hierarchy?.any { it.route == route } == true }

    GradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(navController = navController)
                }
            }
        ) { innerPadding ->
            AppNavContent(innerPadding = innerPadding, navController = navController)
        }
    }
}

@Composable
private fun AppNavContent(innerPadding: PaddingValues, navController: androidx.navigation.NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .statusBarsPadding()
    ) {
        NavGraph(navController = navController)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
