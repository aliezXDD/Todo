package com.todo.util

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 首屏数据是否已就绪。
 *
 * 冷启动时，Compose 的首帧一定早于数据库的首次发射（Room 要开库、跑第一次查询）。
 * 界面如果不区分"还在加载"和"真的没有数据"，就会先渲染一帧假空状态
 * （`0/0 已完成` + "添加第一条待办"），等数据到了再跳成真实内容——也就是启动时那一下闪。
 *
 * 这里把"首个真实数据到位"这件事暴露出来，让 [com.todo.MainActivity] 能据此决定
 * 何时收起系统启动画面，从而在正常情况下**根本不会渲染那一帧**；
 * 界面自身的加载态（[com.todo.ui.screen.todo.TodoViewModel.UiState.isLoading]）作为兜底，
 * 覆盖"数据来得很慢"这种启动画面已经放行的情形。
 */
@Singleton
class StartupGate @Inject constructor() {

    private val _todayTodosLoaded = MutableStateFlow(false)

    /** 今日待办完成首次发射后变为 true，此后不再变回。 */
    val todayTodosLoaded: StateFlow<Boolean> = _todayTodosLoaded.asStateFlow()

    /**
     * 深浅色偏好是否已从 DataStore 读出。
     *
     * 主题偏好存在 DataStore 里、首帧前读不到，所以 App 的第一帧先按系统深浅色渲染，读到偏好后
     * 再切——用户自定义过主题（例如"系统=浅色、App=深色"）时，冷启动会先浅后深闪一下。
     * 把它也纳入启动画面的放行条件，首帧就已是正确主题。
     */
    private val _themeResolved = MutableStateFlow(false)

    val themeResolved: StateFlow<Boolean> = _themeResolved.asStateFlow()

    fun markTodayTodosLoaded() {
        _todayTodosLoaded.value = true
    }

    fun markThemeResolved() {
        _themeResolved.value = true
    }
}
