package com.todo.ui.screen.todo.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todo.domain.model.DailyRecord
import com.todo.ui.component.GlassCard
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import com.todo.util.DateUtils

/**
 * 往日记录中的一天。
 *
 * 原实现与全局规则冲突，这里统一：卡片改用 [GlassCard]（原来是 6% 透明的填充 + 14dp 越界圆角），
 * 行内完成标记改用与「今日」勾选框**同一种平面样式**（原来是 1.5dp 描边圆圈，
 * 而全局已经确定"不用描边"）；同时去掉覆盖应用字体的 fontFamily，
 * 并把完成度从日期后面拆到右侧，形成"标题—数值"的对齐关系。
 */
@Composable
fun HistoryDayCard(
    record: DailyRecord,
    modifier: Modifier = Modifier
) {
    val total = record.todos.size
    val completed = record.todos.count { it.isCompleted }

    // 用"列表项级"阴影（与其他列表项一致），而不是卡片级——一屏里多张卡片用大阴影会互相压盖
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        elevation = NeumorphElevation.Medium
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatShortDate(record.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$completed/$total 完成",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            record.todos.forEach { todo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 与待办页勾选框同款：勾选 = 主题色实底 + 白勾；未勾选 = 很浅的平面底
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = if (todo.isCompleted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                },
                                shape = RoundedCornerShape(NeumorphShapes.Corner)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (todo.isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = todo.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (todo.isCompleted) 0.62f else 0.94f
                        )
                    )
                }
            }
        }
    }
}
