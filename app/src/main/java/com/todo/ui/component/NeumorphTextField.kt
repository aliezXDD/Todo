package com.todo.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.lerp
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态输入框 = 一块**凹陷**的材质（这是新拟态里"可输入"的标准语义）。
 * 不用描边表示聚焦，而是聚焦时再"按深一点"（depth 由 0.12 → 0）。
 */
@Composable
fun NeumorphTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = false,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Small),
    elevation: NeumorphElevation = NeumorphElevation.Medium,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    // 聚焦时"再抬高一点"，而不是凹进去
    val lift by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(MotionTokens.ItemReveal, easing = MotionTokens.StandardEasing),
        label = "fieldLift"
    )
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val fieldElevation = NeumorphElevation(
        offset = lerp(elevation.offset, elevation.offset * 1.5f, lift),
        blur = lerp(elevation.blur, elevation.blur * 1.5f, lift)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.neumorph(
            shape = shape,
            isDark = isDark,
            depth = 1f,
            surface = Neumorph.raisedSurface(isDark),
            elevation = fieldElevation
        ),
        textStyle = textStyle,
        shape = shape,
        singleLine = singleLine,
        interactionSource = interactionSource,
        placeholder = placeholder?.let { { Text(text = it) } },
        colors = neumorphFieldColors()
    )
}

/** [TextFieldValue] 版本：调用方需要自行控制光标位置时使用。 */
@Composable
fun NeumorphTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = false,
    shape: Shape = RoundedCornerShape(NeumorphShapes.Small),
    elevation: NeumorphElevation = NeumorphElevation.Medium,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    // 聚焦时"再抬高一点"，而不是凹进去
    val lift by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(MotionTokens.ItemReveal, easing = MotionTokens.StandardEasing),
        label = "fieldLift"
    )
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val fieldElevation = NeumorphElevation(
        offset = lerp(elevation.offset, elevation.offset * 1.5f, lift),
        blur = lerp(elevation.blur, elevation.blur * 1.5f, lift)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.neumorph(
            shape = shape,
            isDark = isDark,
            depth = 1f,
            surface = Neumorph.raisedSurface(isDark),
            elevation = fieldElevation
        ),
        textStyle = textStyle,
        shape = shape,
        singleLine = singleLine,
        interactionSource = interactionSource,
        placeholder = placeholder?.let { { Text(text = it) } },
        colors = neumorphFieldColors()
    )
}

/** 容器与描边全透明：凹陷感完全由 [neumorph] 的内阴影提供。 */
@Composable
internal fun neumorphFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
)
