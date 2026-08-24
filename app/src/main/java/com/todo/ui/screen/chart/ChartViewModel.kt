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

    data class UiState(
        val stats: List<DailyStats> = emptyList(),
        val hasData: Boolean = false,
        val averageRate: Int = 0,
        val maxRate: Int = 0,
        val totalCompleted: Int = 0,
        val consecutiveDays: Int = 0,
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
            getStatsUseCase(30).collect { rawStats ->
                val statByDate = rawStats.associateBy { it.date }
                val filledStats = (29 downTo 0).map { daysAgo ->
                    val date = DateUtils.daysAgo(daysAgo)
                    statByDate[date] ?: DailyStats(date = date, totalCount = 0, completedCount = 0)
                }

                val validStats = filledStats.filter { it.totalCount > 0 }
                val averageRate = if (validStats.isEmpty()) {
                    0
                } else {
                    (validStats.map { it.completionRate }.average() * 100).toInt()
                }
                val maxRate = ((validStats.maxOfOrNull { it.completionRate } ?: 0f) * 100).toInt()
                val totalCompleted = filledStats.sumOf { it.completedCount }
                val consecutiveDays = calculateConsecutiveFullCompletionDays(filledStats)

                _uiState.update {
                    it.copy(
                        stats = filledStats,
                        hasData = rawStats.isNotEmpty(),
                        averageRate = averageRate.coerceIn(0, 100),
                        maxRate = maxRate.coerceIn(0, 100),
                        totalCompleted = totalCompleted,
                        consecutiveDays = consecutiveDays,
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
            if (day.totalCount == 0 || day.completedCount != day.totalCount) break
            count++
            offset++
        }
        return count
    }
}
