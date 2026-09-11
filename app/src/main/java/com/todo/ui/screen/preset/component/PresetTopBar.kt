package com.todo.ui.screen.preset.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todo.ui.component.GlassCard
import com.todo.ui.theme.NeumorphShapes

@Composable
fun PresetTopBar(
    isMultiSelectMode: Boolean,
    selectedCount: Int,
    onCreateClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // IconButton 默认圆形水波纹与本页的圆角矩形语言不一致，显式传形状使其与卡片同形
    val actionShape = RoundedCornerShape(NeumorphShapes.Corner)

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isMultiSelectMode) "已选 ${selectedCount} 项" else "我的预设",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isMultiSelectMode) {
                Row {
                    IconButton(onClick = onCancelClick, modifier = Modifier.size(40.dp), shape = actionShape) {
                        Text(text = "取消", color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onSelectAllClick, modifier = Modifier.size(40.dp), shape = actionShape) {
                        Text(text = "全选", color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp), shape = actionShape) {
                        Text(text = "删除", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                IconButton(onClick = onCreateClick, modifier = Modifier.size(40.dp), shape = actionShape) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "新建",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
