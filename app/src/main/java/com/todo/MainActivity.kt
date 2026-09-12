package com.todo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.SystemClock
import android.view.ViewTreeObserver
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
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.todo.ui.component.NeumorphBackground
import com.todo.ui.navigation.BottomNavBar
import com.todo.ui.navigation.NavGraph
import com.todo.ui.navigation.Screen
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.TodoTheme
import com.todo.util.StartupGate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var startupGate: StartupGate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        holdSplashUntilFirstData()

        enableEdgeToEdge()
        setContent {
            TodoTheme {
                ConfigureSystemBars()
                TodoRoot()
            }
        }
    }

    /**
     * 让系统启动画面保持到首屏数据真正到位。
     *
     * 不这样做的话，首帧一定早于数据库首次发射，界面会先渲染一帧"0/0 已完成 + 添加第一条待办"
     * 的假空状态再跳回真实数据——即冷启动时那一下闪。
     *
     * 实现说明：平台 API（`android.window.SplashScreen`）**没有** keep-on-screen 能力，
     * 它只提供退出动画监听与主题设置；那套能力是 androidx.core:core-splashscreen 在 API 31+
     * 用「拦截第一帧」模拟出来的。这里采用同一机制：条件不满足就让 pre-draw 返回 false，
     * App 便不会提交第一帧，系统的那张启动窗口（启动画面）自然继续留在屏幕上，
     * 因此无需引入额外依赖。
     *
     * 必须带超时兜底：该条件在每帧绘制前被轮询，一旦数据源出问题（例如开库失败）就永远为真，
     * 启动画面会把界面彻底挡死。最多多留 [SPLASH_MAX_HOLD_MS] 就无条件放行，
     * 之后交给界面自己的加载态显示"加载中…"。
     *
     * 超时**从第一次 pre-draw 起算**，不是在 onCreate 里算好：冷启动时首次 pre-draw 可能比
     * onCreate 晚好几秒，若按 onCreate 计时，第一次求值就已过期，等于完全没有保持。
     */
    private fun holdSplashUntilFirstData() {
        val decorView = window.decorView
        decorView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                private var deadline = 0L

                override fun onPreDraw(): Boolean {
                    if (deadline == 0L) {
                        deadline = SystemClock.uptimeMillis() + SPLASH_MAX_HOLD_MS
                    }
                    val release = startupGate.todayTodosLoaded.value ||
                        SystemClock.uptimeMillis() >= deadline
                    if (release) {
                        decorView.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return release
                }
            }
        )
    }

    private companion object {
        /**
         * 启动画面在数据未就绪时最多多停留的时间（从它本可以收起的那一刻起算）。
         *
         * 取值权衡：设小了，慢设备/慢构建上数据还没到就被放行，用户仍会看到一次"加载中…"切换；
         * 设大了，万一数据源真的卡住，用户就要多盯着启动图标。Room 首次读一张小表正常在几十毫秒级，
         * 2.5s 足够覆盖冷启动最慢的情况，同时把最坏等待限制在一个可接受的长度内。
         */
        const val SPLASH_MAX_HOLD_MS = 2500L
    }
}

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
                Neumorph.LightBackground.toArgb(),
                Neumorph.DarkBackground.toArgb()
            ) { isDark },
            navigationBarStyle = SystemBarStyle.auto(
                Neumorph.LightBackground.toArgb(),
                Neumorph.DarkBackground.toArgb()
            ) { isDark }
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
                // 用色板里的 background（= 最底层页面底色）而不是 Color.Transparent：
                // Scaffold 根部是一层 Surface，它会按 containerColor 反查 contentColor 并注入
                // LocalContentColor。透明色不在配色表里 → 反查得到 Unspecified → 任何"没写颜色"的
                // Text 会退回 LocalContentColor 的默认值 Color.Black（深色下就成了黑字压深底）。
                containerColor = MaterialTheme.colorScheme.background,
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
