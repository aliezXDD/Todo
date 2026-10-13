package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.todo.ui.component.DialogActionTextButton
import com.todo.ui.component.DialogButtonRole
import com.todo.ui.component.NeumorphDialog

/**
 * 点头像后弹出的两个选择：换一张，或恢复默认。
 *
 * 「恢复默认」只在已经自定义过时出现 —— 默认状态下给一个"恢复默认"按钮只会让人以为点坏了。
 * 动作用文字按钮而不是图形：这里没有能一眼读懂"换图 / 恢复"的图标，文字更直接。
 */
@Composable
fun AvatarActionDialog(
    visible: Boolean,
    hasCustomAvatar: Boolean,
    onPickImage: () -> Unit,
    onResetToDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    NeumorphDialog(visible = visible, onDismiss = onDismiss) {
        Text(text = "头像", style = MaterialTheme.typography.titleMedium)
        Text(
            text = if (hasCustomAvatar) "当前是自定义头像" else "可选择自定义头像",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasCustomAvatar) {
                DialogActionTextButton(
                    text = "恢复默认",
                    role = DialogButtonRole.SECONDARY,
                    onClick = onResetToDefault
                )
                // 把「恢复默认」推到最左，与右侧的取消/更换隔开，免得三点一线时误触
                Spacer(modifier = Modifier.weight(1f))
            }
            DialogActionTextButton(
                text = "取消",
                role = DialogButtonRole.SECONDARY,
                onClick = onDismiss
            )
            DialogActionTextButton(
                text = "更换图片",
                role = DialogButtonRole.PRIMARY,
                onClick = onPickImage
            )
        }
    }
}
