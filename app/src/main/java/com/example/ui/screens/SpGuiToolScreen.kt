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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DkysRepository
import com.example.model.*
import com.example.ui.screens.components.ControllerProfilesView
import com.example.ui.theme.NkCardBg
import com.example.ui.theme.NkDarkBg
import com.example.viewmodel.ControllerProfileViewModel

enum class DkysTab(val title: String, val icon: ImageVector) {
    BASIC("Basic", Icons.Default.Tune),
    FUNCTION("Function", Icons.Default.SettingsSuggest),
    MONITOR("Live", Icons.Default.Speed),
    PROFILES("สูตร Room", Icons.Default.FolderSpecial),
    PRESETS("สูตรฟรี", Icons.Default.CardGiftcard)
}

@Composable
fun SpGuiToolScreen(
    repository: DkysRepository,
    profileViewModel: ControllerProfileViewModel? = null,
    onBackToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val actualProfileViewModel: ControllerProfileViewModel =
        profileViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current
    val connectionState by repository.connectionState.collectAsState()
    val selectedInterface by repository.selectedInterface.collectAsState()
    val selectedModel by repository.selectedModel.collectAsState()
    val basicSettings by repository.basicSettings.collectAsState()
    val functionSettings by repository.functionSettings.collectAsState()
    val telemetry by repository.telemetry.collectAsState()
    val packetLogs by repository.packetLogs.collectAsState()
    val isBusy by repository.isBusy.collectAsState()
    val statusMessage by repository.statusMessage.collectAsState()

    var activeTab by remember { mutableStateOf(DkysTab.BASIC) }
    var showModelDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
    ) {
        // App / Software Header
        Surface(
            color = Color(0xFF0F172A),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = "SP GUI dk 2.0",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SP GUI Tool dk 2.0",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFF5722))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DKYS FOC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "สำหรับกล่อง DK72420S, DK72180, DK72240 (DK728240 ฟรี)",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Connection Badge
                    val isConnected = connectionState == ConnectionState.CONNECTED
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isConnected) Color(0xFF059669).copy(alpha = 0.2f)
                                else Color(0xFFDC2626).copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isConnected) "ONLINE" else "OFFLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) Color(0xFF34D399) else Color(0xFFFCA5A5)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Connection Controls Bar: Interface Selector & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Controller Model Dropdown Selector
                    Box(modifier = Modifier.weight(1.1f)) {
                        OutlinedButton(
                            onClick = { showModelDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("dkys_model_selector"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Text(
                                text = selectedModel.modelName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showModelDropdown,
                            onDismissRequest = { showModelDropdown = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            DkysControllerModel.values().forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(model.modelName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (model.isFreePromotion) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("ฟรี", color = Color(0xFFFF5722), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(model.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        }
                                    },
                                    onClick = {
                                        repository.selectModel(model)
                                        showModelDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Connection Interface Chip (USB/CAN/BT)
                    Box(modifier = Modifier.weight(0.9f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ConnectionType.values().forEach { connType ->
                                val active = selectedInterface == connType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(if (active) Color(0xFF0284C7) else Color.Transparent)
                                        .clickable { repository.selectInterface(connType) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (connType) {
                                            ConnectionType.USB_CABLE -> "USB"
                                            ConnectionType.CAN_BUS -> "CAN"
                                            ConnectionType.BLUETOOTH_BLE -> "BT"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                        color = if (active) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    // Connect / Disconnect Button
                    Button(
                        onClick = {
                            if (connectionState == ConnectionState.CONNECTED) repository.disconnect()
                            else repository.connect()
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("dkys_connect_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (connectionState == ConnectionState.CONNECTED) Color(0xFFDC2626) else Color(0xFF0284C7)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (connectionState == ConnectionState.CONNECTED) "ตัดต่อ" else "Connect",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Action Bar: Read, Upload, and Room Profiles Buttons
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            repository.readFromController { success ->
                                Toast.makeText(
                                    context,
                                    if (success) "อ่านค่าสำเร็จ" else "เชื่อมต่อกล่องก่อนอ่านค่า",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("dkys_read_button"),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isBusy,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("1. Read", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            repository.uploadToController { success ->
                                Toast.makeText(
                                    context,
                                    if (success) "บันทึกและเบิร์นลงกล่องสำเร็จ!" else "เชื่อมต่อกล่องก่อนอัปโหลด",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(36.dp)
                            .testTag("dkys_upload_button"),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("2. Burn/Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { activeTab = DkysTab.PROFILES },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("dkys_room_profiles_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (activeTab == DkysTab.PROFILES) Color(0xFF0284C7).copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = Color(0xFF38BDF8)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("3. คลัง Room", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Status message indicator
                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ℹ️ $statusMessage",
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        maxLines = 1
                    )
                }
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                    color = Color(0xFF0284C7)
                )
            }
        ) {
            DkysTab.values().forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    icon = { Icon(tab.icon, contentDescription = tab.title, modifier = Modifier.size(18.dp)) },
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Color(0xFF38BDF8),
                    unselectedContentColor = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("dkys_tab_${tab.name.lowercase()}")
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (activeTab) {
                DkysTab.BASIC -> DkysBasicSettingsTab(
                    basic = basicSettings,
                    onUpdate = { repository.updateBasicSettings(it) }
                )
                DkysTab.FUNCTION -> DkysFunctionSettingsTab(
                    func = functionSettings,
                    onUpdate = { repository.updateFunctionSettings(it) }
                )
                DkysTab.MONITOR -> DkysLiveMonitorTab(
                    telemetry = telemetry,
                    packetLogs = packetLogs,
                    onClearLogs = { repository.clearLogs() }
                )
                DkysTab.PROFILES -> ControllerProfilesView(
                    profileViewModel = actualProfileViewModel,
                    dkysRepository = repository,
                    targetControllerType = "DKYS",
                    onProfileLoaded = {
                        activeTab = DkysTab.BASIC
                    }
                )
                DkysTab.PRESETS -> DkysPresetsTab(
                    presets = repository.presets,
                    onApply = { preset ->
                        repository.applyPreset(preset) {
                            Toast.makeText(context, "โหลดสูตร ${preset.title} สำเร็จ! กด Upload เพื่อเบิร์น", Toast.LENGTH_LONG).show()
                            activeTab = DkysTab.BASIC
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// TAB 1: BASIC SETTINGS
// ==========================================
@Composable
fun DkysBasicSettingsTab(
    basic: DkysBasicSettings,
    onUpdate: ((DkysBasicSettings) -> DkysBasicSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Device Info Card
        item {
            DkysSectionCard(title = "Device Info (ข้อมูลอุปกรณ์)", icon = Icons.Default.Info) {
                InfoRow("Controller Model", basic.deviceModel.modelName)
                InfoRow("Hardware Version", basic.hardwareVersion)
                InfoRow("Firmware / Software", basic.softwareVersion)
                InfoRow("Serial Number", basic.serialNumber)

                Spacer(modifier = Modifier.height(8.dp))
                Text("System Voltage (แรงดันระบบแบตเตอรี่)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(48, 60, 72, 84, 96).forEach { volt ->
                        val selected = basic.systemVoltage == volt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                .border(1.dp, if (selected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                .clickable { onUpdate { it.copy(systemVoltage = volt) } }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${volt}V",
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Motor Parameters Card
        item {
            DkysSectionCard(title = "Motor Parameters (พารามิเตอร์มอเตอร์)", icon = Icons.Default.SettingsInputComponent) {
                // Pole pairs
                DkysSliderRow(
                    title = "Pole Pairs (จำนวนคู่ขั้วแม่เหล็ก)",
                    valueText = "${basic.polePairs} คู่ (${basic.polePairs * 2} Poles)",
                    value = basic.polePairs.toFloat(),
                    valueRange = 2f..24f,
                    steps = 21,
                    onValueChange = { onUpdate { s -> s.copy(polePairs = it.toInt()) } }
                )

                // Sensor Type
                Spacer(modifier = Modifier.height(6.dp))
                Text("Sensor Type (ประเภทเซนเซอร์ตรวจจับตำแหน่งล้อ)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DkysSensorType.values().forEach { sensor ->
                        val sel = basic.sensorType == sensor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (sel) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(1.dp, if (sel) Color(0xFF0284C7) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                .clickable { onUpdate { it.copy(sensorType = sensor) } }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sel,
                                onClick = { onUpdate { it.copy(sensorType = sensor) } },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(sensor.displayName, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Phase Angle & Direction
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Phase Angle
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Phase Angle (มุมองศาเฟส)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(60, 120).forEach { angle ->
                                val sel = basic.phaseAngle == angle
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (sel) Color(0xFF0284C7) else Color(0xFF1E293B))
                                        .clickable { onUpdate { it.copy(phaseAngle = angle) } }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${angle}°", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Motor Direction
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ทิศทางการหมุนล้อ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (basic.motorDirectionForward) Color(0xFF059669) else Color(0xFF1E293B))
                                    .clickable { onUpdate { it.copy(motorDirectionForward = true) } }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ตามเข็ม", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (!basic.motorDirectionForward) Color(0xFF059669) else Color(0xFF1E293B))
                                    .clickable { onUpdate { it.copy(motorDirectionForward = false) } }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ทวนเข็ม", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Rated RPM & Flux Weakening Unlock
                Spacer(modifier = Modifier.height(10.dp))
                DkysSliderRow(
                    title = "Rated Motor RPM (รอบมอเตอร์พิกัด)",
                    valueText = "${basic.ratedMotorRpm} RPM",
                    value = basic.ratedMotorRpm.toFloat(),
                    valueRange = 500f..3500f,
                    steps = 29,
                    onValueChange = { onUpdate { s -> s.copy(ratedMotorRpm = it.toInt()) } }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Flux Weakening ปลดรอบมอเตอร์", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
                        Text("เพิ่มความเร็วปลายเกินขีดจำกัด Back-EMF สูงสุด 35%", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = basic.fluxWeakeningRpmUnlock,
                        onCheckedChange = { onUpdate { s -> s.copy(fluxWeakeningRpmUnlock = it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF5722)
                        )
                    )
                }
            }
        }

        // Battery Limits Card
        item {
            DkysSectionCard(title = "Battery Limits (ขีดจำกัดแบตเตอรี่)", icon = Icons.Default.BatteryChargingFull) {
                DkysSliderRow(
                    title = "Low-Voltage Cut-off (ตัดไฟต่ำ LVC)",
                    valueText = "%.1f V".format(basic.lowVoltageCutoff),
                    value = basic.lowVoltageCutoff,
                    valueRange = 40f..85f,
                    onValueChange = { onUpdate { s -> s.copy(lowVoltageCutoff = it) } }
                )

                DkysSliderRow(
                    title = "Low-Voltage Recovery (กลับมาต่อไฟ)",
                    valueText = "%.1f V".format(basic.lowVoltageRecovery),
                    value = basic.lowVoltageRecovery,
                    valueRange = 42f..90f,
                    onValueChange = { onUpdate { s -> s.copy(lowVoltageRecovery = it) } }
                )

                DkysSliderRow(
                    title = "Overvoltage Protection (ตัดไฟเกิน)",
                    valueText = "%.1f V".format(basic.overVoltageProtection),
                    value = basic.overVoltageProtection,
                    valueRange = 55f..105f,
                    onValueChange = { onUpdate { s -> s.copy(overVoltageProtection = it) } }
                )

                DkysSliderRow(
                    title = "Max Bus Current (กระแสแบตสูงสุด)",
                    valueText = "${basic.maxBusCurrentA} A",
                    value = basic.maxBusCurrentA.toFloat(),
                    valueRange = 30f..250f,
                    onValueChange = { onUpdate { s -> s.copy(maxBusCurrentA = it.toInt()) } }
                )

                DkysSliderRow(
                    title = "Battery Overheat Cutoff (ตัดความร้อนแบต)",
                    valueText = "${basic.batteryOverheatCutoffC} °C",
                    value = basic.batteryOverheatCutoffC.toFloat(),
                    valueRange = 45f..80f,
                    onValueChange = { onUpdate { s -> s.copy(batteryOverheatCutoffC = it.toInt()) } }
                )
            }
        }

        // Current Limits Card
        item {
            DkysSectionCard(title = "Current Limits (ขีดจำกัดกระแสเฟส)", icon = Icons.Default.ElectricBolt) {
                DkysSliderRow(
                    title = "Max Phase Current (กระแสเฟสสูงสุด)",
                    valueText = "${basic.maxPhaseCurrentA} A",
                    value = basic.maxPhaseCurrentA.toFloat(),
                    valueRange = 80f..600f,
                    onValueChange = { onUpdate { s -> s.copy(maxPhaseCurrentA = it.toInt()) } }
                )

                DkysSliderRow(
                    title = "Continuous Phase Current (กระแสเฟสต่อเนื่อง)",
                    valueText = "${basic.continuousPhaseCurrentA} A",
                    value = basic.continuousPhaseCurrentA.toFloat(),
                    valueRange = 50f..300f,
                    onValueChange = { onUpdate { s -> s.copy(continuousPhaseCurrentA = it.toInt()) } }
                )

                DkysSliderRow(
                    title = "Flux Weakening Id Current (กระแสชะลอสนามแม่เหล็ก)",
                    valueText = "${basic.fluxWeakeningCurrentA} A",
                    value = basic.fluxWeakeningCurrentA.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { onUpdate { s -> s.copy(fluxWeakeningCurrentA = it.toInt()) } }
                )

                DkysSliderRow(
                    title = "Boost Current Duration (ระยะเวลาบูสต์สูงสุด)",
                    valueText = "${basic.boostCurrentDurationSec} วินาที",
                    value = basic.boostCurrentDurationSec.toFloat(),
                    valueRange = 3f..30f,
                    onValueChange = { onUpdate { s -> s.copy(boostCurrentDurationSec = it.toInt()) } }
                )
            }
        }

        // Throttle Response Card
        item {
            DkysSectionCard(title = "Throttle Response (การตอบสนองคันเร่ง)", icon = Icons.Default.Speed) {
                DkysSliderRow(
                    title = "จุดเริ่มตอบสนอง (Min Voltage)",
                    valueText = "%.2f V".format(basic.throttleMinVoltage),
                    value = basic.throttleMinVoltage,
                    valueRange = 0.8f..2.0f,
                    onValueChange = { onUpdate { s -> s.copy(throttleMinVoltage = it) } }
                )

                DkysSliderRow(
                    title = "จุดคันเร่งสุด (Max Voltage)",
                    valueText = "%.2f V".format(basic.throttleMaxVoltage),
                    value = basic.throttleMaxVoltage,
                    valueRange = 3.0f..4.8f,
                    onValueChange = { onUpdate { s -> s.copy(throttleMaxVoltage = it) } }
                )

                DkysSliderRow(
                    title = "ค่าฟรีคันเร่ง (Deadband)",
                    valueText = "%.2f V".format(basic.throttleDeadband),
                    value = basic.throttleDeadband,
                    valueRange = 0.05f..0.50f,
                    onValueChange = { onUpdate { s -> s.copy(throttleDeadband = it) } }
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("Throttle Curve (กราฟการตอบสนองคันเร่ง)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DkysThrottleCurve.values().forEach { curve ->
                        val sel = basic.throttleCurve == curve
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (sel) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(1.dp, if (sel) Color(0xFF0284C7) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                .clickable { onUpdate { it.copy(throttleCurve = curve) } }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sel,
                                onClick = { onUpdate { it.copy(throttleCurve = curve) } },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(curve.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(curve.description, fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Throttle Fault Protection", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ตัดการขับขี่ทันทีเมื่อแรงดันคันเร่งหลุดย่าน (สายขาดหรือค้าง)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = basic.throttleFaultProtection,
                        onCheckedChange = { onUpdate { s -> s.copy(throttleFaultProtection = it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF059669)
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: FUNCTION SETTINGS
// ==========================================
@Composable
fun DkysFunctionSettingsTab(
    func: DkysFunctionSettings,
    onUpdate: ((DkysFunctionSettings) -> DkysFunctionSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Riding & Speeds Card
        item {
            DkysSectionCard(title = "Riding & Speeds (โหมดการขับขี่ & เกียร์)", icon = Icons.Default.DirectionsBike) {
                // Switch Type
                Text("Speed Switch Type (รูปแบบสวิตช์เลือกเกียร์)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DkysSpeedSwitchType.values().forEach { sw ->
                        val sel = func.speedSwitchType == sw
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (sel) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(1.dp, if (sel) Color(0xFF0284C7) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                .clickable { onUpdate { it.copy(speedSwitchType = sw) } }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sel,
                                onClick = { onUpdate { it.copy(speedSwitchType = sw) } },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(sw.displayName, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // 3 Speed Gears
                Spacer(modifier = Modifier.height(10.dp))
                Text("Three-Speed Gears (ปรับแต่งเกียร์ 1, 2, 3)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))

                // Gear 1
                DkysSliderRow(
                    title = "เกียร์ 1 (Low Speed %)",
                    valueText = "${func.gear1SpeedPercent}% (Phase ${func.gear1PhaseCurrentPercent}%)",
                    value = func.gear1SpeedPercent.toFloat(),
                    valueRange = 30f..100f,
                    onValueChange = { onUpdate { s -> s.copy(gear1SpeedPercent = it.toInt()) } }
                )

                // Gear 2
                DkysSliderRow(
                    title = "เกียร์ 2 (Med Speed %)",
                    valueText = "${func.gear2SpeedPercent}% (Phase ${func.gear2PhaseCurrentPercent}%)",
                    value = func.gear2SpeedPercent.toFloat(),
                    valueRange = 40f..100f,
                    onValueChange = { onUpdate { s -> s.copy(gear2SpeedPercent = it.toInt()) } }
                )

                // Gear 3
                DkysSliderRow(
                    title = "เกียร์ 3 (High Speed %)",
                    valueText = "${func.gear3SpeedPercent}% (Phase ${func.gear3PhaseCurrentPercent}%)",
                    value = func.gear3SpeedPercent.toFloat(),
                    valueRange = 50f..120f,
                    onValueChange = { onUpdate { s -> s.copy(gear3SpeedPercent = it.toInt()) } }
                )

                // Max Speed Limit
                DkysSliderRow(
                    title = "Max Speed Limit (ล็อกความเร็วสูงสุด)",
                    valueText = "${func.maxSpeedLimitKmh} km/h",
                    value = func.maxSpeedLimitKmh.toFloat(),
                    valueRange = 45f..180f,
                    onValueChange = { onUpdate { s -> s.copy(maxSpeedLimitKmh = it.toInt()) } }
                )

                // Soft Start Level
                DkysSliderRow(
                    title = "Soft Start Level (ความนุ่มนวลช่วงออกตัว 1-5)",
                    valueText = "ระดับ ${func.softStartLevel}",
                    value = func.softStartLevel.toFloat(),
                    valueRange = 1f..5f,
                    steps = 3,
                    onValueChange = { onUpdate { s -> s.copy(softStartLevel = it.toInt()) } }
                )
            }
        }

        // Braking & Safety Card
        item {
            DkysSectionCard(title = "Braking & Safety (ระบบเบรกและความปลอดภัย)", icon = Icons.Default.Security) {
                // EBS Regen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EBS Electronic Braking (เบรกไฟฟ้าชาร์จกลับ)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("หน่วงมอเตอร์อัตโนมัติเมื่อแตะเบรก ชาร์จไฟกลับแบตเตอรี่", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.ebsEnabled,
                        onCheckedChange = { onUpdate { s -> s.copy(ebsEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }

                if (func.ebsEnabled) {
                    DkysSliderRow(
                        title = "EBS Regen Strength (ระดับความหน่วง)",
                        valueText = "${func.ebsRegenLevelPercent}%",
                        value = func.ebsRegenLevelPercent.toFloat(),
                        valueRange = 5f..100f,
                        onValueChange = { onUpdate { s -> s.copy(ebsRegenLevelPercent = it.toInt()) } }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("EBS Linear Proportional", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("แรงเบรกไฟฟ้าแปรผันตามน้ำหนักการบีบก้านเบรก", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = func.ebsLinearProportional,
                            onCheckedChange = { onUpdate { s -> s.copy(ebsLinearProportional = it) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0284C7))
                        )
                    }
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // HDC Hill Descent Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("HDC (Hill Descent Control ควบคุมลงเขาชัน)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ล็อกความเร็วหน่วงคงที่อัตโนมัติเมื่อลงทางลาดชัน", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.hdcEnabled,
                        onCheckedChange = { onUpdate { s -> s.copy(hdcEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }

                if (func.hdcEnabled) {
                    DkysSliderRow(
                        title = "HDC Hold Speed (ความเร็วควบคุมลงเขา)",
                        valueText = "${func.hdcHoldSpeedKmh} km/h",
                        value = func.hdcHoldSpeedKmh.toFloat(),
                        valueRange = 10f..50f,
                        onValueChange = { onUpdate { s -> s.copy(hdcHoldSpeedKmh = it.toInt()) } }
                    )
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // TCS Traction Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TCS (Traction Control System กันล้อฟรี)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ลดกระแสทันทีเมื่อล้อหลังหมุนฟรีบนถนนลื่น", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.tcsEnabled,
                        onCheckedChange = { onUpdate { s -> s.copy(tcsEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Anti-Theft Motor Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Anti-Theft Motor Lock (ระบบล็อกมอเตอร์กันขโมย)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("เมื่อล็อกรีโมต มอเตอร์จะต้านแรงผลักและเปิดเสียงเตือน", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.antiTheftMotorLock,
                        onCheckedChange = { onUpdate { s -> s.copy(antiTheftMotorLock = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFDC2626))
                    )
                }
            }
        }

        // Convenience Features Card
        item {
            DkysSectionCard(title = "Convenience Features (ฟังก์ชันความสะดวก)", icon = Icons.Default.Tune) {
                // Cruise Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cruise Control (ล็อกความเร็วอัตโนมัติ)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("บิดคันเร่งคงที่ตามเวลาที่กำหนดเพื่อล็อกความเร็ว", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.cruiseControlEnabled,
                        onCheckedChange = { onUpdate { s -> s.copy(cruiseControlEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0284C7))
                    )
                }

                if (func.cruiseControlEnabled) {
                    DkysSliderRow(
                        title = "บิดคันเร่งค้างเพื่อล็อกความเร็ว",
                        valueText = "${func.cruiseAutoHoldSec} วินาที",
                        value = func.cruiseAutoHoldSec.toFloat(),
                        valueRange = 4f..15f,
                        onValueChange = { onUpdate { s -> s.copy(cruiseAutoHoldSec = it.toInt()) } }
                    )
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Reverse Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reverse Mode (เกียร์ถอยหลัง)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ความเร็วถอยหลัง: ${func.reverseSpeedPercent}% (พร้อมเสียงเตือนปี๊บ)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.reverseEnabled,
                        onCheckedChange = { onUpdate { s -> s.copy(reverseEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Parking P-Gear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Parking Mode (ปุ่มสวิตช์เกียร์จอด P-Gear)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ป้องกันบิดคันเร่งโดยไม่ตั้งใจ ต้องกดปดล็อค P ก่อนออกตัว", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.parkingModeEnabled,
                        onCheckedChange = { onUpdate { s -> s.copy(parkingModeEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Side-stand Interlock
                Text("Side-Stand Sensor (เซนเซอร์ตัดไฟขาตั้งข้าง)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DkysSideStandInterlock.values().forEach { interlock ->
                        val sel = func.sideStandInterlock == interlock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (sel) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF1E293B))
                                .border(1.dp, if (sel) Color(0xFF0284C7) else Color(0xFF334155), RoundedCornerShape(6.dp))
                                .clickable { onUpdate { it.copy(sideStandInterlock = interlock) } }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sel,
                                onClick = { onUpdate { it.copy(sideStandInterlock = interlock) } },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(interlock.displayName, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                // Charge Protection Cutoff
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Charge Protection Cut-off", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("ห้ามบิดออกตัวขณะเสียบสายชาร์จไฟ ป้องกันสายขาดและอุบัติเหตุ", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = func.chargeProtectionCutoff,
                        onCheckedChange = { onUpdate { s -> s.copy(chargeProtectionCutoff = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 3: LIVE MONITOR & DIAGNOSTICS
// ==========================================
@Composable
fun DkysLiveMonitorTab(
    telemetry: DkysTelemetry,
    packetLogs: List<String>,
    onClearLogs: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Speed & RPM Gauges
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speedometer
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.0f".format(telemetry.vehicleSpeedKmh),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace
                            )
                            Text("KM/H", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(Color(0xFF334155))
                        )

                        // RPM
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${telemetry.motorRpm}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722),
                                fontFamily = FontFamily.Monospace
                            )
                            Text("MOTOR RPM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        }

                        // Active Gear
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G${telemetry.activeGear}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("GEAR", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusIndicatorChip("EBS", telemetry.isBraking, Color(0xFFEF4444), Modifier.weight(1f))
                        StatusIndicatorChip("HDC", telemetry.isHdcActive, Color(0xFF3B82F6), Modifier.weight(1f))
                        StatusIndicatorChip("TCS", telemetry.isTcsActive, Color(0xFFF59E0B), Modifier.weight(1f))
                        StatusIndicatorChip("FLUX", telemetry.isFluxWeakeningActive, Color(0xFF8B5CF6), Modifier.weight(1f))
                    }
                }
            }
        }

        // Live Telemetry Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryMetricBox(
                        title = "Battery Voltage",
                        value = "%.1f V".format(telemetry.batteryVoltage),
                        icon = Icons.Default.BatteryFull,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricBox(
                        title = "Bus Current (แบต)",
                        value = "%.1f A".format(telemetry.busCurrentA),
                        icon = Icons.Default.FlashOn,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryMetricBox(
                        title = "Phase Current (เฟส)",
                        value = "%.1f A".format(telemetry.phaseCurrentA),
                        icon = Icons.Default.ElectricBolt,
                        color = Color(0xFFFF5722),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricBox(
                        title = "Throttle Input",
                        value = "${telemetry.throttleInputPercent}%",
                        icon = Icons.Default.Speed,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryMetricBox(
                        title = "Controller Temp",
                        value = "%.1f °C".format(telemetry.controllerTempC),
                        icon = Icons.Default.Thermostat,
                        color = if (telemetry.controllerTempC > 60) Color(0xFFEF4444) else Color(0xFF06B6D4),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricBox(
                        title = "Motor Temp",
                        value = "%.1f °C".format(telemetry.motorTempC),
                        icon = Icons.Default.DeviceThermostat,
                        color = if (telemetry.motorTempC > 70) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Diagnostics & Fault Code Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Controller Diagnostic Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("รหัสวิเคราะห์: ${telemetry.faultCode} (ระบบสมบูรณ์ 100%)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("NORMAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                }
            }
        }

        // Live Packet Console (USB / CAN / BT frames)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Serial / CAN Packet Monitor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        TextButton(
                            onClick = onClearLogs,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ล้าง Log", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF030712))
                            .padding(8.dp)
                    ) {
                        if (packetLogs.isEmpty()) {
                            Text("รอแพ็กเก็ตข้อมูลจากกล่อง DKYS...", fontSize = 10.sp, color = Color(0xFF64748B))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(packetLogs) { log ->
                                    Text(
                                        text = log,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (log.contains(">>>")) Color(0xFF38BDF8)
                                        else if (log.contains("<<<")) Color(0xFF34D399)
                                        else Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: PRESETS TAB (สูตรจูนฟรี aekboy2010)
// ==========================================
@Composable
fun DkysPresetsTab(
    presets: List<DkysPreset>,
    onApply: (DkysPreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner Promo Free
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF5722).copy(alpha = 0.3f), Color(0xFF0284C7).copy(alpha = 0.3f))
                        )
                    )
                    .border(1.dp, Color(0xFFFF5722), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "แจกฟรี! สูตรจูนกล่อง DK72240 / DK728240",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                        Text(
                            text = "โดย aekboy2010 shop - สำหรับชาวมอเตอร์ไซค์ไฟฟ้า ปลดรอบ 72V 240A นิ่งเหนียวไม่ร้อน",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        items(presets) { preset ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (preset.isFree) Color(0xFFFF5722).copy(alpha = 0.6f) else Color(0xFF334155)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = preset.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (preset.isFree) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF5722))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ฟรี", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(preset.subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.height(10.dp))

                    // Spec Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetBadge("Phase: ${preset.phaseCurrent}A", Modifier.weight(1f))
                        PresetBadge("Bus: ${preset.busCurrent}A", Modifier.weight(1f))
                        PresetBadge("Flux: ${preset.fluxCurrent}A", Modifier.weight(1f))
                        PresetBadge("Top: ${preset.topSpeedKmh} km/h", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onApply(preset) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("apply_preset_${preset.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (preset.isFree) Color(0xFFFF5722) else Color(0xFF0284C7)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "เลือกใช้สูตรนี้ (Load Profile)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE HELPER COMPOSABLES
// ==========================================
@Composable
fun DkysSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            content()
        }
    }
}

@Composable
fun DkysSliderRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 11.sp, color = Color(0xFFCBD5E1))
            Text(valueText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF38BDF8),
                activeTrackColor = Color(0xFF0284C7),
                inactiveTrackColor = Color(0xFF334155)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun StatusIndicatorChip(label: String, active: Boolean, activeColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) activeColor.copy(alpha = 0.25f) else Color(0xFF1E293B))
            .border(1.dp, if (active) activeColor else Color(0xFF334155), RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) activeColor else Color(0xFF64748B)
        )
    }
}

@Composable
fun TelemetryMetricBox(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun PresetBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0F172A))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 9.sp, color = Color(0xFFCBD5E1), maxLines = 1)
    }
}
