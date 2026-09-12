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

    fun markTodayTodosLoaded() {
        _todayTodosLoaded.value = true
    }
}
