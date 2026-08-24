package com.todo.ui.screen.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todo.R
import com.todo.ui.component.GlassCard
import com.todo.ui.component.GlassTopBar
import com.todo.util.Constants
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable
fun SettingsScreen(
    onNavigateToRecycleBin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GlassTopBar(
            title = "设置",
            glassEffect = true,
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
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onNavigateToRecycleBin)
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

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                drawShadow = false
            ) {
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.secondary
            )
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
