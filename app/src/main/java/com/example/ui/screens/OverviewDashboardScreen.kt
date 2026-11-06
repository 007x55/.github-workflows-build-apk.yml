package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.model.ConnectionType
import com.example.ui.VotolTuningViewModel
import com.example.ui.theme.*

@Composable
fun OverviewDashboardScreen(
    viewModel: VotolTuningViewModel,
    onNavigateToTuning: () -> Unit,
    onNavigateToCockpit: () -> Unit,
    onNavigateToConnection: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onNavigateToSoyosource: () -> Unit = {},
    onNavigateToDkys: () -> Unit = {},
    onNavigateToCloneManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedInterface by viewModel.selectedInterface.collectAsState()
    val params by viewModel.params.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    var showModelSelectDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Free DK72240 / DK728240 Banner - Opens SP GUI Tool dk 2.0
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF5722).copy(alpha = 0.25f),
                                Color(0xFF00E5FF).copy(alpha = 0.2f)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToDkys() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text("🎁", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "แจกฟรี! จูนกล่อง DK72240 / DK728240",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                            Text(
                                text = "ปลดล็อกจูน 72V 240A เต็มระบบ + สูตรจูนพร้อมขี่ (คลิกเปิด SP GUI Tool)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF5722))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("เปิด SP GUI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // SP GUI Tool dk 2.0 Dedicated Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToDkys() }
                    .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ SP GUI Tool dk 2.0",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFF5722))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("DKYS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Text(
                                text = "จูนกล่อง DK72420S, DK72180, DK72240 (Basic & Function Setting, EBS, HDC, TCS)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF38BDF8))
                }
            }
        }

        // Soyosource GTI 1200W Shortcut Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToSoyosource() }
                    .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFC107)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "☀️ เครื่องมือ Soyosource GTI 1200W",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC107)
                            )
                            Text(
                                text = "RS485 Limiter กันย้อน • มอนิเตอร์วัตต์ 0-1200W • แบต/PV",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFC107))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("เปิดใช้งาน", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // Clone Manager Dual Workspace Shortcut Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToCloneManager() }
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📱 Clone Manager (Multi-App)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("SANDBOX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                }
                            }
                            Text(
                                text = "ตรวจจับแอปที่ติดตั้ง & สร้างโคลนแยกบัญชี โปรไฟล์จูนกล่องอิสระ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("จัดการโคลน", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        // Hero Header & Brand Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF5722).copy(alpha = 0.6f),
                                Color(0xFF00E5FF).copy(alpha = 0.3f),
                                Color(0xFF1E2D4A)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFF5722)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "aekboy2010 ",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = NkCyanAccent
                                    )
                                    Text(
                                        text = "SHOP",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = Color(0xFFFF5722)
                                    )
                                }
                                Text(
                                    text = "VOTOL EM TUNER COMMAND CENTER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Connection State Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (connectionState == ConnectionState.CONNECTED) Color(0xFF10B981).copy(alpha = 0.18f)
                                    else Color(0xFFEF4444).copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (connectionState == ConnectionState.CONNECTED) Color(0xFF10B981) else Color(0xFFEF4444),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onNavigateToConnection() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (connectionState == ConnectionState.CONNECTED) Color(0xFF10B981)
                                            else Color(0xFFEF4444)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (connectionState == ConnectionState.CONNECTED) "ONLINE" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (connectionState == ConnectionState.CONNECTED) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFF1E2D4A))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Connected Device & Interface Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showModelSelectDialog = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("กล่องควบคุมปัจจุบัน:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.width(6.dp))
                                if (params.model.isFree) {
                                    Text(
                                        text = "FREE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        color = Color(0xFFFF5722),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFF5722).copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = params.model.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("ช่องทางเชื่อมต่อ:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                            Text(
                                text = when (selectedInterface) {
                                    ConnectionType.USB_CABLE -> "สาย USB Data (OTG)"
                                    ConnectionType.CAN_BUS -> "CAN Bus 250k"
                                    ConnectionType.BLUETOOTH_BLE -> "บลูทูธ BLE"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = NkCyanAccent
                            )
                        }
                    }
                }
            }
        }

        // Live Telemetry Snapshot (Quick Glance)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "สถานะการทำงานสด (Live Glance)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ดูหน้าปัดเต็ม ➔",
                        style = MaterialTheme.typography.labelSmall,
                        color = NkCyanAccent,
                        modifier = Modifier.clickable { onNavigateToCockpit() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMetricCard(
                        title = "ความเร็วปัจจุบัน",
                        value = "${telemetry.speedKmh.toInt()} km/h",
                        subtitle = "${telemetry.motorRpm} RPM | เกียร์ ${telemetry.currentGear}",
                        icon = Icons.Default.Speed,
                        accentColor = NkCyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "แรงดันระบบแบตเตอรี่",
                        value = "${String.format("%.1f", telemetry.batteryVoltage)} V",
                        subtitle = "ระบบ ${params.ratedVoltage}V LiFePO4",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMetricCard(
                        title = "กระแสเฟสมอเตอร์",
                        value = "${telemetry.phaseCurrentA.toInt()} A",
                        subtitle = "Bus: ${telemetry.busCurrentA.toInt()} A (จำกัด ${params.busCurrentLimitA}A)",
                        icon = Icons.Default.ElectricBolt,
                        accentColor = Color(0xFFFF2A4B),
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricCard(
                        title = "อุณหภูมิกล่อง / มอเตอร์",
                        value = "${String.format("%.1f", telemetry.controllerTempC)} °C",
                        subtitle = "มอเตอร์: ${String.format("%.1f", telemetry.motorTempC)} °C (ปกติ)",
                        icon = Icons.Default.Thermostat,
                        accentColor = if (telemetry.controllerTempC > 60f) Color(0xFFFF2A4B) else NkGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions Grid (Links to Most Used Features)
        item {
            Text(
                text = "เมนูลัดใช้งานบ่อย (Quick Shortcuts)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard(
                    title = "จูนพารามิเตอร์",
                    subtitle = "กระแส / สปีด / โวลต์",
                    icon = Icons.Default.Tune,
                    iconBg = Color(0xFFFF5722),
                    onClick = onNavigateToTuning,
                    modifier = Modifier.weight(1f),
                    testTag = "shortcut_tuning"
                )
                QuickActionCard(
                    title = "เรือนไมล์สด",
                    subtitle = "จอความเร็ว & คันเร่ง",
                    icon = Icons.Default.Speed,
                    iconBg = NkCyanAccent,
                    onClick = onNavigateToCockpit,
                    modifier = Modifier.weight(1f),
                    testTag = "shortcut_cockpit"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard(
                    title = "สลับการเชื่อมต่อ",
                    subtitle = "USB / CAN / BT",
                    icon = Icons.Default.Cable,
                    iconBg = Color(0xFF007DFE),
                    onClick = onNavigateToConnection,
                    modifier = Modifier.weight(1f),
                    testTag = "shortcut_connection"
                )
                QuickActionCard(
                    title = "สูตรจูน Room DB",
                    subtitle = "DK72420S & VOTOL",
                    icon = Icons.Default.FolderSpecial,
                    iconBg = Color(0xFF8B5CF6),
                    onClick = onNavigateToConfig,
                    modifier = Modifier.weight(1f),
                    testTag = "shortcut_config"
                )
            }
        }

        // Quick Tuning Preset Switcher (1-Touch Preset Bar)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "สลับสูตรจูนด่วน (One-Touch Presets)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = NkGold)
                    }
                    Text(
                        text = "เลือกสูตรจูนพร้อมขี่และเขียนค่าลงกล่องทันที",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val isDk = params.model.series.contains("DK")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            title = if (isDk) "🏁 DK-RACE" else "🏁 RACING",
                            desc = if (isDk) "240A เต็มสูตร" else "Phase 480A",
                            accentColor = Color(0xFFFF2A4B),
                            onClick = {
                                if (isDk) {
                                    viewModel.applyPreset("DK72240_RACING")
                                    Toast.makeText(context, "โหลดสูตรแข่ง DK72240 240A (ฟรี) สำเร็จ!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.applyPreset("RACING")
                                    Toast.makeText(context, "โหลดสูตรแข่ง Racing Track สำเร็จ!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            title = if (isDk) "🚀 DK-STREET" else "🚀 STREET",
                            desc = if (isDk) "190A วิ่งถนน" else "Phase 350A",
                            accentColor = NkCyanAccent,
                            onClick = {
                                if (isDk) {
                                    viewModel.applyPreset("DK72240_STREET")
                                    Toast.makeText(context, "โหลดสูตรสตรีท DK72240 190A (ฟรี) สำเร็จ!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.applyPreset("STREET")
                                    Toast.makeText(context, "โหลดสูตรวิ่งถนน Street Sport สำเร็จ!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PresetButton(
                            title = if (isDk) "🔋 DK-ECO" else "🔋 ECO",
                            desc = if (isDk) "140A ประหยัด" else "ประหยัดแบต",
                            accentColor = Color(0xFF10B981),
                            onClick = {
                                if (isDk) {
                                    viewModel.applyPreset("DK72240_ECO")
                                    Toast.makeText(context, "โหลดสูตรประหยัด DK72240 (ฟรี) สำเร็จ!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.applyPreset("ECO")
                                    Toast.makeText(context, "โหลดสูตรประหยัด Eco Delivery สำเร็จ!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Parameters Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "สรุปพารามิเตอร์การจูนปัจจุบัน",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SummaryRow(label = "กระแสแบตสูงสุด (Bus Current):", value = "${params.busCurrentLimitA} A")
                    SummaryRow(label = "กระแสเฟสมอเตอร์ (Phase Current):", value = "${params.phaseCurrentLimitA} A")
                    SummaryRow(label = "ระบบเพิ่มรอบปลาย (Flux Weakening):", value = if (params.fluxWeakeningEnabled) "${params.fluxWeakeningCurrentA}A @ ${params.fluxStartRpm} RPM" else "ปิดใช้งาน")
                    SummaryRow(label = "เบรกหน่วงชาร์จไฟกลับ (EBS):", value = if (params.ebsEnabled) "ระดับ ${params.ebsHighSpeedLevel} (${params.ebsMaxCurrentA}A Max)" else "ปิดใช้งาน")
                    SummaryRow(label = "แรงดันตัดไฟต่ำ (Cutoff Voltage):", value = "${String.format("%.1f", params.underVoltageProtection)} V")
                    SummaryRow(label = "ออกตัวนุ่มนวล (Soft Start):", value = "ระดับ ${params.softStartLevel}/15")
                }
            }
        }

        // Fast Sync Actions (Read & Write to Controller)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.readParameters {
                            Toast.makeText(context, "อ่านค่าจากกล่อง Votol สำเร็จ!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D4A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = NkCyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("อ่านค่าจากกล่อง", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.writeParameters {
                            Toast.makeText(context, "บันทึกและเขียนค่าลงกล่อง Votol สำเร็จ!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เขียนค่าลงกล่อง", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showModelSelectDialog) {
        AlertDialog(
            onDismissRequest = { showModelSelectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFF5722))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("เลือกรุ่นกล่องควบคุม (ฟรี DK72240)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        Text("ซีรีส์ DK / DKYS (ปลดล็อกฟรี!):", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    items(com.example.model.ControllerModel.values().filter { it.series.contains("DK") }) { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (params.model == model) Color(0xFF1E2D4A) else Color.Transparent)
                                .clickable {
                                    viewModel.selectModel(model)
                                    showModelSelectDialog = false
                                    Toast.makeText(context, "เลือกกล่อง ${model.displayName} สำเร็จ!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = params.model == model,
                                onClick = {
                                    viewModel.selectModel(model)
                                    showModelSelectDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5722))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(model.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Max Bus ${model.maxBusCurrentA}A | Phase ${model.maxPhaseCurrentA}A", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ซีรีส์ VOTOL EM:", color = NkCyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    items(com.example.model.ControllerModel.values().filter { !it.series.contains("DK") }) { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (params.model == model) Color(0xFF1E2D4A) else Color.Transparent)
                                .clickable {
                                    viewModel.selectModel(model)
                                    showModelSelectDialog = false
                                    Toast.makeText(context, "เลือกกล่อง ${model.displayName} สำเร็จ!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = params.model == model,
                                onClick = {
                                    viewModel.selectModel(model)
                                    showModelSelectDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = NkCyanAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(model.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Max Bus ${model.maxBusCurrentA}A | Phase ${model.maxPhaseCurrentA}A", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showModelSelectDialog = false }) {
                    Text("ปิด", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0D1527)
        )
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = modifier.border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = modifier
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconBg,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun PresetButton(
    title: String,
    desc: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF090F1C)),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.5f))
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(desc, fontSize = 9.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
