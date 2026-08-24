package com.todo.ui.screen.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.todo.domain.model.Todo
import com.todo.ui.component.GlassBottomSheet
import com.todo.ui.component.GlassButton
import com.todo.ui.component.GlassCard
import com.todo.ui.component.glassTextFieldColors
import com.todo.ui.component.glassTextFieldShape
import com.todo.ui.theme.DarkGlassBorder
import com.todo.ui.theme.DarkGlassSurface
import com.todo.ui.theme.LightGlassBorder
import com.todo.ui.theme.LightGlassSurface

@Composable
fun EditTodoSheet(
    visible: Boolean,
    todo: Todo?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: (Todo) -> Unit
) {
    if (!visible || todo == null) return

    val focusRequester = remember { FocusRequester() }
    var input by remember(todo.id) { mutableStateOf(todo.content) }
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    LaunchedEffect(todo.id) {
        focusRequester.requestFocus()
    }

    GlassBottomSheet(
        onDismissRequest = onDismiss,
        opaqueBackground = true
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "编辑待办",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = glassTextFieldShape(),
                    colors = glassTextFieldColors()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassButton(
                        text = "保存",
                        onClick = { if (input.isNotBlank()) onSave(input) },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "删除",
                        onClick = { onDelete(todo) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
