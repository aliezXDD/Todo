package com.todo.ui.screen.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todo.domain.model.DailyStats
import com.todo.domain.usecase.GetStatsUseCase
import com.todo.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val getStatsUseCase: GetStatsUseCase
) : ViewModel() {

    enum class ChartType {
        BAR,
        LINE
    }

    /**
     * [stats] 是**最近 30 天**的窗口（图表画它）；
     * [totalCompleted] 与 [fullCompletionDays] 则是**全部历史**的累计值，
     * 两者口径不同，改动时别把它们混到同一个列表上算。
     */
    data class UiState(
        val stats: List<DailyStats> = emptyList(),
        val hasData: Boolean = false,
        val averageRate: Int = 0,
        val totalCompleted: Int = 0,
        val consecutiveDays: Int = 0,
        val fullCompletionDays: Int = 0,
        val chartType: ChartType = ChartType.BAR,
        val isLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            getStatsUseCase().collect { allStats ->
                val statByDate = allStats.associateBy { it.date }

                // 图表与平均完成率仍然只看最近 30 天（补空白天，柱子/折线才有连续横轴）
                val windowDates = (CHART_WINDOW_DAYS - 1 downTo 0).map { DateUtils.daysAgo(it) }
                val windowStats = windowDates.map { date ->
                    statByDate[date] ?: DailyStats(date = date, totalCount = 0, completedCount = 0)
                }

                val validStats = windowStats.filter { it.totalCount > 0 }
                val averageRate = if (validStats.isEmpty()) {
                    0
                } else {
                    (validStats.map { it.completionRate }.average() * 100).toInt()
                }

                // 两个「累计」是历史总量，不受 30 天窗口限制
                val totalCompleted = allStats.sumOf { it.completedCount }
                val fullCompletionDays = allStats.count { it.isFullyCompleted }
                val consecutiveDays = calculateConsecutiveFullCompletionDays(allStats)

                _uiState.update {
                    it.copy(
                        stats = windowStats,
                        hasData = windowDates.any { date -> date in statByDate },
                        averageRate = averageRate.coerceIn(0, 100),
                        totalCompleted = totalCompleted,
                        consecutiveDays = consecutiveDays,
                        fullCompletionDays = fullCompletionDays,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleChartType() {
        _uiState.update {
            it.copy(
                chartType = if (it.chartType == ChartType.BAR) ChartType.LINE else ChartType.BAR
            )
        }
    }

    private fun calculateConsecutiveFullCompletionDays(stats: List<DailyStats>): Int {
        if (stats.isEmpty()) return 0
        val byDate = stats.associateBy { it.date }
        var count = 0
        var offset = 0
        while (true) {
            val date = DateUtils.daysAgo(offset)
            val day = byDate[date] ?: break
            if (!day.isFullyCompleted) break
            count++
            offset++
        }
        return count
    }

    private companion object {
        /** 图表只画最近 30 天；两个「累计」不走这个窗口。 */
        const val CHART_WINDOW_DAYS = 30
    }
}
