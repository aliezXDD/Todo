package com.todo.ui.screen.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.R
import com.todo.ui.component.GlassCard
import com.todo.ui.component.GlassTopBar
import com.todo.ui.component.neumorph
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes
import com.todo.util.Constants

@Composable
fun SettingsScreen(
    onNavigateToRecycleBin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GlassTopBar(
            title = "设置",
            overlayBelowContent = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "外观主题", style = MaterialTheme.typography.titleMedium)
                    ThemeOptionRow(
                        label = "跟随系统",
                        selected = uiState.themeMode == Constants.THEME_MODE_SYSTEM,
                        onClick = { viewModel.setThemeMode(Constants.THEME_MODE_SYSTEM) }
                    )
                    ThemeOptionRow(
                        label = "浅色模式",
                        selected = uiState.themeMode == Constants.THEME_MODE_LIGHT,
                        onClick = { viewModel.setThemeMode(Constants.THEME_MODE_LIGHT) }
                    )
                    ThemeOptionRow(
                        label = "深色模式",
                        selected = uiState.themeMode == Constants.THEME_MODE_DARK,
                        onClick = { viewModel.setThemeMode(Constants.THEME_MODE_DARK) }
                    )
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToRecycleBin
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "回收站", style = MaterialTheme.typography.bodyLarge)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "回收站"
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "关于", style = MaterialTheme.typography.bodyLarge)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Todo v1.3",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "小肥霙",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.logo_rounded),
                            contentDescription = "应用图标",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f
    val noRipple = remember { MutableInteractionSource() }
    val rowSurface = if (selected) Neumorph.recessedSurface(isDark) else Neumorph.surface(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 选中行整体凹进去、未选中保持平面：层级完全靠光影，不用任何描边或色块
            .neumorph(
                shape = RoundedCornerShape(NeumorphShapes.Small),
                isDark = isDark,
                depth = if (selected) 0f else 1f,
                surface = rowSurface,
                elevation = if (selected) NeumorphElevation.Small else NeumorphElevation.None
            )
            .clickable(
                interactionSource = noRipple,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 新拟态单选：未选 = 凸起的空圈；选中 = 凹陷 + 凸起的主题色圆点
        Box(
            modifier = Modifier
                .size(20.dp)
                .neumorph(
                    shape = CircleShape,
                    isDark = isDark,
                    depth = if (selected) 0f else 1f,
                    surface = rowSurface,
                    elevation = NeumorphElevation.Small
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
