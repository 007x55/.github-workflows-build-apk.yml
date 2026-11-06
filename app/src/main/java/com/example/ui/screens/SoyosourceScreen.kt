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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SoyosourceRepository
import com.example.model.ConnectionState
import com.example.model.SoyoOperatingMode
import com.example.ui.theme.*

@Composable
fun SoyosourceScreen(
    repository: SoyosourceRepository,
    onBackToEvTuner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectionState by repository.connectionState.collectAsState()
    val telemetry by repository.telemetry.collectAsState()
    val settings by repository.settings.collectAsState()
    val logs by repository.packetLogs.collectAsState()

    var showPresetDialog by remember { mutableStateOf(false) }
    var showProtectionDialog by remember { mutableStateOf(false) }

    val isConnected = connectionState == ConnectionState.CONNECTED

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Switcher Bar to return to EV Tuner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBackToEvTuner() }
                    .testTag("btn_switch_to_ev_tuner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5722)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("สลับไปโหมด: จูนกล่องมอเตอร์ EV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("VOTOL EM & DK72240 [ฟรี] (USB / CAN / BT)", color = Color(0xFFFF9800), fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }
        }

        // Header Title Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("☀️ SOYOSOURCE GTI 1200W", color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Text("Grid-Tie Inverter + RS485 Limiter Suite • aekboy2010 shop", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        // Connection State Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (connectionState) {
                                ConnectionState.CONNECTED -> NkLineGreen.copy(alpha = 0.2f)
                                ConnectionState.CONNECTING -> Color(0xFFFFC107).copy(alpha = 0.2f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (connectionState) {
                                                ConnectionState.CONNECTED -> NkLineGreen
                                                ConnectionState.CONNECTING -> Color(0xFFFFC107)
                                                else -> Color(0xFFEF4444)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (connectionState) {
                                        ConnectionState.CONNECTED -> "ออนไลน์ (RS485)"
                                        ConnectionState.CONNECTING -> "กำลังเชื่อมต่อ..."
                                        else -> "ออฟไลน์"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (connectionState) {
                                        ConnectionState.CONNECTED -> NkLineGreen
                                        ConnectionState.CONNECTING -> Color(0xFFFFC107)
                                        else -> Color(0xFFEF4444)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connect/Disconnect + Port selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isConnected) {
                                    repository.disconnect()
                                    Toast.makeText(context, "ตัดการเชื่อมต่อ Soyosource 1200W แล้ว", Toast.LENGTH_SHORT).show()
                                } else {
                                    repository.connect()
                                    Toast.makeText(context, "กำลังส่ง Handshake RS485 ไปยัง Soyosource...", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("btn_soyo_connect"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) Color(0xFFDC2626) else Color(0xFFFF9800)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                if (isConnected) Icons.Default.Close else Icons.Default.Cable,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isConnected) "ตัดการเชื่อมต่อ" else "เชื่อมต่อ RS485",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { showPresetDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107))
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("สูตรสำเร็จ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Output Power Gauge (0 - 1200W)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("กำลังไฟฟ้าที่จ่ายเข้าบ้าน (AC Output)", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${telemetry.acPower.toInt()}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFC107),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Watts / 1200W", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(bottom = 10.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    val progress = (telemetry.acPower / 1200f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF22C55E), Color(0xFFFFC107), Color(0xFFFF5722))
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SoyoTelemetryCell(title = "แรงดันแบต/PV", value = "${telemetry.dcVoltage} V", icon = Icons.Default.BatteryChargingFull, color = NkLineGreen, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SoyoTelemetryCell(title = "กระแส DC Input", value = "${telemetry.dcCurrent} A", icon = Icons.Default.Bolt, color = Color(0xFFFFC107), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SoyoTelemetryCell(title = "ไฟบ้าน Grid AC", value = "${telemetry.acVoltage} V", icon = Icons.Default.Power, color = NkCyanAccent, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SoyoTelemetryCell(title = "ความถี่ไฟบ้าน", value = "${telemetry.acFrequency} Hz", icon = Icons.Default.Waves, color = Color(0xFF818CF8), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SoyoTelemetryCell(title = "อุณหภูมิฮีตซิงก์", value = "${telemetry.temperature} °C", icon = Icons.Default.Thermostat, color = if (telemetry.temperature > 60f) Color(0xFFEF4444) else Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        SoyoTelemetryCell(title = "พลังงานสะสมวันนี้", value = "${telemetry.dailyEnergyKwh} kWh", icon = Icons.Default.Today, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Operating Mode Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ เลือกโหมดการทำงานอินเวอร์เตอร์", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    SoyoOperatingMode.values().forEach { mode ->
                        val isSelected = settings.operatingMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1E293B) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFFFC107) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    repository.updateOperatingMode(mode)
                                    Toast.makeText(context, "เปลี่ยนเป็น ${mode.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { repository.updateOperatingMode(mode) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFC107))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(mode.displayName, color = if (isSelected) Color(0xFFFFC107) else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(mode.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Power Limiter Slider & Instant Quick Watts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡ ตั้งจำกัดวัตต์ (Power Limit)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "${settings.targetPowerWatts} W",
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = settings.targetPowerWatts.toFloat(),
                        onValueChange = { repository.updateTargetPower(it.toInt()) },
                        valueRange = 50f..1200f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFC107),
                            activeTrackColor = Color(0xFFFFC107),
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("slider_soyo_watts")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Watt buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(250, 500, 800, 1000, 1200).forEach { watts ->
                            OutlinedButton(
                                onClick = {
                                    repository.updateTargetPower(watts)
                                    Toast.makeText(context, "ตั้งค่ากำลังไฟ $watts W ผ่าน RS485 แล้ว", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (settings.targetPowerWatts == watts) Color.Black else Color.White,
                                    containerColor = if (settings.targetPowerWatts == watts) Color(0xFFFFC107) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107))
                            ) {
                                Text(if (watts == 1200) "MAX" else "${watts}W", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Battery Protection Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🛡️ การป้องกันแบตเตอรี่ (Battery Cutoff)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("ป้องกันแบต 48V/72V โอเวอร์ดิสชาร์จ", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        IconButton(onClick = { showProtectionDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "แก้ไข", tint = Color(0xFFFFC107))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("แรงดันตัดการทำงาน (Cutoff):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${settings.batteryCutoffVoltage} V", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("แรงดันเริ่มทำงานใหม่ (Restart):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${settings.batteryRestartVoltage} V", color = NkLineGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("เวลาหน่วงเริ่ม (Delay):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${settings.startupDelaySeconds} วิ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // RS485 Live Frame Monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = NkCyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("มอนิเตอร์แพ็กเก็ต RS485 (9600/4800 baud)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        TextButton(onClick = { repository.clearLogs() }) {
                            Text("ล้างข้อมูล", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF0B132B), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = true) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    color = when {
                                        log.contains("TX") -> Color(0xFFFFC107)
                                        log.contains("RX") -> NkCyanAccent
                                        log.contains("Online") -> NkLineGreen
                                        else -> Color(0xFF94A3B8)
                                    },
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                repository.sendZeroExportLimiterFrame(settings.targetPowerWatts)
                                Toast.makeText(context, "ส่งเฟรม Limiter 4800 baud สำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Text("ยิงเฟรม Limiter 4800bps", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                repository.connect()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Text("ขอ Status 9600bps", fontSize = 11.sp, color = Color(0xFFFFC107))
                        }
                    }
                }
            }
        }
    }

    // Dialog for presets
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFFFC107))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("สูตรการตั้งค่า Soyosource 1200W", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(repository.presets) { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.applyPreset(preset)
                                    showPresetDialog = false
                                    Toast.makeText(context, "ใช้งานสูตร: ${preset.title}", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(preset.title, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(preset.subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("กำลังไฟ: ${preset.targetWatts}W", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("ตัดไฟ: ${preset.cutoffVoltage}V", color = Color(0xFFEF4444), fontSize = 11.sp)
                                    Text("ต่อไฟ: ${preset.restartVoltage}V", color = NkLineGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("ปิด", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Protection dialog
    if (showProtectionDialog) {
        var tempCutoff by remember { mutableStateOf(settings.batteryCutoffVoltage) }
        var tempRestart by remember { mutableStateOf(settings.batteryRestartVoltage) }

        AlertDialog(
            onDismissRequest = { showProtectionDialog = false },
            title = {
                Text("ปรับตั้งค่าแรงดันตัดต่อแบตเตอรี่", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("แรงดันตัดต่ำสุด (Cutoff): " + "%.1f".format(tempCutoff) + " V", color = Color(0xFFEF4444), fontSize = 13.sp)
                    Slider(
                        value = tempCutoff,
                        onValueChange = { tempCutoff = it },
                        valueRange = 40.0f..65.0f,
                        steps = 50,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
                    )

                    Text("แรงดันเริ่มทำงานใหม่ (Restart): " + "%.1f".format(tempRestart) + " V", color = NkLineGreen, fontSize = 13.sp)
                    Slider(
                        value = tempRestart,
                        onValueChange = { tempRestart = it },
                        valueRange = 44.0f..70.0f,
                        steps = 52,
                        colors = SliderDefaults.colors(thumbColor = NkLineGreen, activeTrackColor = NkLineGreen)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateBatteryCutoff(tempCutoff, tempRestart)
                        showProtectionDialog = false
                        Toast.makeText(context, "บันทึกค่าลงอินเวอร์เตอร์แล้ว", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                ) {
                    Text("บันทึกค่า (Save)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProtectionDialog = false }) {
                    Text("ยกเลิก", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun SoyoTelemetryCell(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Text(title, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 1)
        }
    }
}
