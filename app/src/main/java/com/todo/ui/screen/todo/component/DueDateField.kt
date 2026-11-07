package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassButton
import com.todo.ui.theme.NeumorphElevation
import com.todo.util.DateUtils

/**
 * 截止日期那颗按钮的高度。
 *
 * 比常规按钮矮一档：常规按钮的内容是 labelLarge 行高 20dp + 上下各 12dp = 44dp，随后又被 M3 的最小触摸目标
 * 垫到 48dp。40dp 正好是 M3 自己给小号按钮的高度（`ButtonDefaults.MinHeight`），也是它被当作"次要行动"时
 * 该有的分量：和「删除」一样站次要一侧，不抢主操作的位置。
 *
 * 用 [LocalMinimumInteractiveComponentSize] 把它同时声明成触摸目标，内容高度就与触摸目标相等：
 * 波纹/高光依旧和新拟态形状严丝合缝（见 [GlassButton] 的说明），不会像默认那样差出那 2dp。
 */
private val DueDateButtonHeight = 40.dp

/**
 * 截止日期那颗按钮的光影档位：比常规按钮淡一半。
 *
 * 新拟态里按钮的"颜色"其实就是这层暗影 —— 表面色与面板底色完全相同（浅色下还刻意不留左上高光），
 * 所以暗影一收，按钮立刻显得轻。常规档是按列表项那类大元素标定的（偏移 7dp / 模糊 11dp、满强度），
 * 压在一颗 40dp 高的小按钮上，它就显得比按钮承载的内容还重；这里几何不动，只把暗影强度减半。
 */
private val DueDateButtonElevation = NeumorphElevation.Medium.copy(darkAlpha = 0.5f)

/**
 * 「标签 + 日期按钮」这一行：左边是文字标签，右边那颗按钮显示当前截止日期（未设置时写「选择日期」）。
 *
 * 编辑待办与添加待办共用**同一个组件**，两处的宽度比例、按钮高度、光影强度、文案因此不可能走偏 ——
 * 这也是"添加时设日期要和编辑时逻辑一致"最省事的保证方式。
 *
 * 两者各占一半宽度：按钮的宽度因此与并排的主操作按钮完全对齐，两行成为同一套两列网格。
 * 左右内边距不在这里给，由调用方按自己面板的留白规则传 [modifier]。
 *
 * [onClick] 只负责"把日历叫出来"，选中的值由调用方持有（两块面板都把它当草稿，不立即落盘）。
 */
@Composable
fun DueDateField(
    label: String,
    dueDate: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        // 这颗按钮矮一档：把触摸目标也声明成 [DueDateButtonHeight]，
        // 于是内容高度 = 触摸目标 = 新拟态形状，三者重合（见 [GlassButton] 的说明）。
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides DueDateButtonHeight
        ) {
            GlassButton(
                text = dueDate?.let { DateUtils.formatDotDate(it) } ?: "选择日期",
                onClick = onClick,
                // 玻璃面 = 次要行动：不抢主操作的位置；暗影再淡一半，比「删除」更轻
                glassSurface = true,
                elevation = DueDateButtonElevation,
                modifier = Modifier.weight(1f),
                // 纵向内边距 = (目标高度 − labelLarge 行高 20dp) / 2：
                // 内容高度正好等于目标高度，改 [DueDateButtonHeight] 也不会与内边距脱节。
                // 横向压到 12dp：宽度已经被"与主操作按钮对齐"钉死，这点内边距只影响文字能摊多宽 ——
                // 留窄些，免得窄屏或大字体下「2026.10.2」这类日期被挤成两行（文字居中，平时看不出差别）
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = (DueDateButtonHeight - 20.dp) / 2
                )
            )
        }
    }
}
