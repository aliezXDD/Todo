package com.todo.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.todo.ui.component.neumorph
import com.todo.ui.theme.MotionTokens
import com.todo.ui.theme.Neumorph
import com.todo.ui.theme.NeumorphElevation
import com.todo.ui.theme.NeumorphShapes

/**
 * 新拟态底部导航：整条栏是一块凸起的同色材质，**选中项凹进去**（凹陷 = 选中态）。
 * 未选中项为与背景齐平的平面，不额外加任何颜色块——层级全部交给光影。
 */
@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination
    val isDark = MaterialTheme.colorScheme.onSurface.luminance() > 0.7f

    val barShape = RoundedCornerShape(NeumorphShapes.Large)
    val itemShape = RoundedCornerShape(NeumorphShapes.Medium)
    val accent = MaterialTheme.colorScheme.primary
    val idleColor = MaterialTheme.colorScheme.secondary

    val items: List<Triple<Screen, String, ImageVector>> = listOf(
        Triple(Screen.Preset, "预设", Icons.AutoMirrored.Filled.List),
        Triple(Screen.Todo, "待办", Icons.Filled.CheckCircle),
        Triple(Screen.Settings, "设置", Icons.Filled.Settings)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .neumorph(
                    shape = barShape,
                    isDark = isDark,
                    depth = 1f,
                    elevation = NeumorphElevation.XLarge
                ),
            shape = barShape,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEach { (screen, label, icon) ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    val noRipple = remember { MutableInteractionSource() }

                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.06f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = MotionTokens.SpringMedium
                        ),
                        label = "bottomIconScale"
                    )
                    val tint by animateColorAsState(
                        targetValue = if (selected) accent else idleColor,
                        label = "bottomTint"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .neumorph(
                                shape = itemShape,
                                isDark = isDark,
                                depth = 1f,
                                // 选中项是"抬起来"（更亮的受光底 + 小阴影），而不是凹进去
                                surface = if (selected) Neumorph.raisedSurface(isDark) else Neumorph.surface(isDark),
                                elevation = if (selected) NeumorphElevation.Small else NeumorphElevation.None
                            )
                            .clickable(
                                interactionSource = noRipple,
                                indication = null
                            ) {
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Screen.Todo.route) {
                                        saveState = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.scale(iconScale),
                                tint = tint
                            )
                            Text(
                                text = label,
                                color = tint,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
