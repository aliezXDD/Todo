package com.todo.ui.screen.preset.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
                    IconButton(onClick = onCancelClick, modifier = Modifier.size(40.dp)) {
                        Text(text = "取消", color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onSelectAllClick, modifier = Modifier.size(40.dp)) {
                        Text(text = "全选", color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(40.dp)) {
                        Text(text = "删除", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            } else {
                IconButton(onClick = onCreateClick, modifier = Modifier.size(40.dp)) {
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
