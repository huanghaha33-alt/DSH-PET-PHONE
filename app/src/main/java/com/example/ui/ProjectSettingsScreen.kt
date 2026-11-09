package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PetPreferences
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSettingsScreen(
    onNavigateBackToControlCenter: () -> Unit
) {
    // Hardware & gesture back handler to return to control center
    BackHandler {
        onNavigateBackToControlCenter()
    }

    val context = LocalContext.current
    val petPreferences = remember(context) { PetPreferences.getInstance(context) }
    val settings by petPreferences.settings.collectAsState()

    val backgroundColor = Color(0xFFF8F9FA)
    val cardColor = Color(0xFFFFFFFF)
    val textColor = Color(0xFF1F2937)
    val subtextColor = Color(0xFF6B7280)
    val primaryAccent = Color(0xFF2563EB)
    val dividerColor = Color(0xFFF3F4F6)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "项目设置",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBackToControlCenter,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回控制中心",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Appearance & Size
            SettingsGroupCard(title = "外观与比例", cardColor = cardColor) {
                // Pet Scale Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "桌宠显示大小",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${(settings.petScale * 100).roundToInt()}%",
                            color = primaryAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("scale_percentage_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = settings.petScale,
                        onValueChange = { petPreferences.updatePetScale(it) },
                        valueRange = 0.70f..1.50f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryAccent,
                            activeTrackColor = primaryAccent,
                            inactiveTrackColor = Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pet_scale_slider")
                    )

                    // Quick size preset chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            0.8f to "紧凑 (80%)",
                            1.0f to "标准 (100%)",
                            1.25f to "放大 (125%)",
                            1.5f to "超大 (150%)"
                        ).forEach { (scale, label) ->
                            val isSelected = (settings.petScale * 100).roundToInt() == (scale * 100).roundToInt()
                            FilterChip(
                                selected = isSelected,
                                onClick = { petPreferences.updatePetScale(scale) },
                                label = { Text(text = label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryAccent.copy(alpha = 0.12f),
                                    selectedLabelColor = primaryAccent
                                )
                            )
                        }
                    }
                }
            }

            // Section 2: Interaction & Feedback
            SettingsGroupCard(title = "交互与物理反馈", cardColor = cardColor) {
                // Haptic feedback toggle
                SettingsSwitchRow(
                    icon = Icons.Default.Vibration,
                    title = "触感反馈 (Haptics)",
                    subtitle = "点击与长按时触发清脆的触觉震动",
                    checked = settings.hapticEnabled,
                    onCheckedChange = { petPreferences.updateHapticEnabled(it) },
                    testTag = "switch_haptic"
                )

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Squash & Stretch physics toggle
                SettingsSwitchRow(
                    icon = Icons.Default.Animation,
                    title = "物理 Q 弹形变",
                    subtitle = "模拟被手指戳按时的果冻弹性回弹动画",
                    checked = settings.squashEnabled,
                    onCheckedChange = { petPreferences.updateSquashEnabled(it) },
                    testTag = "switch_squash"
                )

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Combo Sensitivity Window
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "连续点击判定窗口",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "多快间隔内的连续轻敲算作连击动作",
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1200L to "敏感 (1.2s)",
                            1800L to "标准 (1.8s)",
                            2500L to "宽松 (2.5s)"
                        ).forEach { (ms, label) ->
                            val isSelected = settings.comboWindowMs == ms
                            FilterChip(
                                selected = isSelected,
                                onClick = { petPreferences.updateComboWindow(ms) },
                                label = { Text(text = label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryAccent.copy(alpha = 0.12f),
                                    selectedLabelColor = primaryAccent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 3: Playback & State
            SettingsGroupCard(title = "动画与系统控制", cardColor = cardColor) {
                // Auto Idle Recovery Time
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "互动后恢复待机时长",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "表情动作表演完毕后自动恢复呼吸待机的间隔",
                                color = subtextColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            5 to "快捷 5 秒",
                            10 to "标准 10 秒",
                            15 to "充裕 15 秒"
                        ).forEach { (seconds, label) ->
                            val isSelected = settings.autoIdleDurationSec == seconds
                            FilterChip(
                                selected = isSelected,
                                onClick = { petPreferences.updateAutoIdleDuration(seconds) },
                                label = { Text(text = label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryAccent.copy(alpha = 0.12f),
                                    selectedLabelColor = primaryAccent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // Keep Screen On
                SettingsSwitchRow(
                    icon = Icons.Default.StayCurrentPortrait,
                    title = "桌面陪伴屏幕常亮",
                    subtitle = "在桌宠画面中阻止系统自动休眠锁屏",
                    checked = settings.keepScreenOn,
                    onCheckedChange = { petPreferences.updateKeepScreenOn(it) },
                    testTag = "switch_keep_screen_on"
                )
            }

            // Section 4: System Actions & Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedButton(
                        onClick = { petPreferences.resetToDefaults() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "恢复全部默认设置", color = Color(0xFFDC2626))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "BigFish Pet · 原生 Android 桌宠独立版 v1.0",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    cardColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color(0xFF6B7280),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color(0xFF1F2937),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD1D5DB)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
