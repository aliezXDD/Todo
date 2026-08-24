package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todo.domain.model.DailyRecord
import com.todo.util.DateUtils

@Composable
fun HistoryDayCard(
    record: DailyRecord,
    modifier: Modifier = Modifier
) {
    val total = record.todos.size
    val completed = record.todos.count { it.isCompleted }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${DateUtils.formatShortDate(record.date)}  ${completed}/${total} 完成",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            record.todos.forEach { todo ->
                Text(
                    text = if (todo.isCompleted) "✓ ${todo.content}" else "○ ${todo.content}",
                    modifier = Modifier.padding(start = 2.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (todo.isCompleted) 0.68f else 0.94f),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
