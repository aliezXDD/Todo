package com.todo.util

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 全 App 唯一的"当前逻辑日"（凌晨 4 点分界，见 [DateUtils.DAY_START_HOUR]）。
 *
 * 为什么必须是一个共享来源：日期若由各 ViewModel 各持一份，就会出现"待办页已经换到新的一天、
 * 统计页还画着昨天"的错位——待办页自己有定时器，统计页没有，而且它只在统计表恰好写入时才
 * 顺带重算一次。现在所有界面观察同一个 StateFlow，跨天时一起换绑。
 *
 * 定时器在进程内只跑这一条（本类是 [Singleton]）。scope 与进程同生命周期是刻意的：
 * 这个循环的语义就是"进程活着就一直对时"，没有更早的取消时机可用。
 */
@Singleton
class CurrentDay @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _day = MutableStateFlow(DateUtils.today())
    val day: StateFlow<String> = _day.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                // 多留 1 秒余量：刚过边界时墙钟可能还没跨过去，宁可晚一秒，也不要早一次
                delay(DateUtils.millisUntilNextDayStart() + 1_000L)
                refresh()
            }
        }
    }

    /**
     * 重新对时。界面回到前台时也要调它：进程在后台被冻结时，定时器不会推进
     * （`delay` 用的是 uptime 计时），所以只靠定时器会漏掉"锁屏过夜"这类情况。
     */
    fun refresh() {
        _day.value = DateUtils.today()
    }
}
