package com.todo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.luminance
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.todo.ui.component.NeumorphBackground
import com.todo.ui.navigation.BottomNavBar
import com.todo.ui.navigation.NavGraph
import com.todo.ui.navigation.Screen
import com.todo.ui.theme.MotionTokens
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

// 底部导航栏底色：与整体深色底保持一致
private val NavigationBarScrim: Int = AndroidColor.parseColor("#0F0F11")

@Composable
private fun ConfigureSystemBars() {
    val context = LocalContext.current
    val activity = context.findActivity() as? ComponentActivity ?: return
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    SideEffect {
        // 统一走 enableEdgeToEdge：它同时处理系统栏底色与图标明暗，且随主题切换可重复调用。
        // 不直接写 window.statusBarColor / navigationBarColor —— 自 API 35 起系统会忽略这两个值。
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ) { isDark },
            navigationBarStyle = SystemBarStyle.dark(NavigationBarScrim)
        )
    }
}

@Composable
private fun TodoRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = listOf(Screen.Preset.route, Screen.Todo.route, Screen.Settings.route)
        .any { route -> currentDestination?.hierarchy?.any { it.route == route } == true }

    NeumorphBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                AppNavContent(innerPadding = innerPadding, navController = navController)
            }

            // 底部导航以“覆盖层”形式叠加在内容之上并做淡入/滑入、淡出/滑出动画。
            // 关键：不占用 Scaffold 的 bottomBar 布局空间，因此它出现/消失不会改变内容区尺寸，
            // 避免进出详情页时 NavHost 内容因内边距突变而上下跳变/错乱。
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(tween(MotionTokens.ScreenSlide)) +
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(MotionTokens.ScreenSlide)
                    ),
                exit = fadeOut(tween(MotionTokens.ScreenSlide)) +
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(MotionTokens.ScreenSlide)
                    ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomNavBar(navController = navController)
            }
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
