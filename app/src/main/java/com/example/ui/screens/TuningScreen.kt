package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrakeSignalType
import com.example.model.SpeedSensorType
import com.example.model.VotolParameters
import com.example.ui.VotolTuningViewModel
import com.example.ui.theme.*

@Composable
fun TuningScreen(
    viewModel: VotolTuningViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val params by viewModel.params.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("พื้นฐาน", "กระแส & 3 สปีด", "เพิ่มรอบปลาย (Flux)", "เบรกหน่วง (EBS)", "สูตรจูนพรีเซ็ต")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = NkCyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "จูนเนอร์กล่อง VOTOL EM",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "ปรับตั้งค่าพารามิเตอร์ละเอียด โดย aekboy2010 shop",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Sub Tabs Scrollable
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = NkCardBg,
                contentColor = NkCyanAccent,
                edgePadding = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NkCyanAccent else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> item { BasicParamsTab(params = params, onUpdate = { viewModel.updateParameters(it) }) }
            1 -> item { CurrentAndGearsTab(params = params, onUpdate = { viewModel.updateParameters(it) }) }
            2 -> item { FluxWeakeningTab(params = params, onUpdate = { viewModel.updateParameters(it) }) }
            3 -> item { EbsRegenTab(params = params, onUpdate = { viewModel.updateParameters(it) }) }
            4 -> item {
                PresetsTab(
                    onApply = { preset ->
                        viewModel.applyPreset(preset)
                        Toast.makeText(context, "โหลดสูตรจูน $preset สำเร็จ!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Quick Save Button
        item {
            Button(
                onClick = {
                    viewModel.writeParameters {
                        Toast.makeText(context, "เขียนค่าลงกล่อง Votol เรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_tune")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("บันทึกค่าลงกล่อง VOTOL ทันที", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun BasicParamsTab(params: VotolParameters, onUpdate: (VotolParameters) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ตั้งค่าระบบไฟฟ้าและคันเร่งพื้นฐาน", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // Rated Voltage Select
            Text("แรงดันระบบแบตเตอรี่ (Rated Voltage): ${params.ratedVoltage}V", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(48, 60, 72, 84, 96).forEach { volt ->
                    val isSel = params.ratedVoltage == volt
                    FilterChip(
                        selected = isSel,
                        onClick = { onUpdate(params.copy(ratedVoltage = volt)) },
                        label = { Text("${volt}V") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NkCyanAccent,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Low Voltage Cutoff
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("แรงดันตัดไฟต่ำ (Low Voltage Cutoff)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                Text("${String.format("%.1f", params.underVoltageProtection)} V", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }
            Slider(
                value = params.underVoltageProtection,
                onValueChange = { onUpdate(params.copy(underVoltageProtection = it)) },
                valueRange = 40.0f..85.0f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Soft Start
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ออกตัวนุ่มนวล (Soft Start Level 0-15)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                Text("${params.softStartLevel}", fontWeight = FontWeight.Bold, color = NkCyanAccent)
            }
            Slider(
                value = params.softStartLevel.toFloat(),
                onValueChange = { onUpdate(params.copy(softStartLevel = it.toInt())) },
                valueRange = 0f..15f,
                steps = 14,
                colors = SliderDefaults.colors(thumbColor = NkCyanAccent, activeTrackColor = NkCyanAccent)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reverse Speed
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ความเร็วเกียร์ถอยหลัง (Reverse Speed %)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                Text("${params.reverseSpeedPercent} %", fontWeight = FontWeight.Bold, color = NkGold)
            }
            Slider(
                value = params.reverseSpeedPercent.toFloat(),
                onValueChange = { onUpdate(params.copy(reverseSpeedPercent = it.toInt())) },
                valueRange = 10f..60f,
                colors = SliderDefaults.colors(thumbColor = NkGold, activeTrackColor = NkGold)
            )
        }
    }
}

@Composable
private fun CurrentAndGearsTab(params: VotolParameters, onUpdate: (VotolParameters) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ตั้งค่าขีดจำกัดกระแส & สปีดเกียร์ 1-2-3", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // Bus Current Limit
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("กระแสแบตเตอรี่สูงสุด (Bus Current Limit)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                Text("${params.busCurrentLimitA} A", fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
            }
            Slider(
                value = params.busCurrentLimitA.toFloat(),
                onValueChange = { onUpdate(params.copy(busCurrentLimitA = it.toInt())) },
                valueRange = 30f..params.model.maxBusCurrentA.toFloat(),
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5722), activeTrackColor = Color(0xFFFF5722))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Phase Current Limit
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("กระแสเฟสมอเตอร์สูงสุด (Phase Current Limit)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                Text("${params.phaseCurrentLimitA} A", fontWeight = FontWeight.Bold, color = Color(0xFFFF2A4B))
            }
            Slider(
                value = params.phaseCurrentLimitA.toFloat(),
                onValueChange = { onUpdate(params.copy(phaseCurrentLimitA = it.toInt())) },
                valueRange = 80f..params.model.maxPhaseCurrentA.toFloat(),
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF2A4B), activeTrackColor = Color(0xFFFF2A4B))
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0xFF1E2D4A))
            Spacer(modifier = Modifier.height(12.dp))

            Text("สปีดเกียร์ (Gear Speed & Current Ratios):", style = MaterialTheme.typography.titleSmall, color = Color.White)

            // Gear 1
            GearRow(gearName = "เกียร์ 1 (Low)", speedPercent = params.gear1SpeedPercent, onSpeedChange = { onUpdate(params.copy(gear1SpeedPercent = it)) })
            // Gear 2
            GearRow(gearName = "เกียร์ 2 (Mid)", speedPercent = params.gear2SpeedPercent, onSpeedChange = { onUpdate(params.copy(gear2SpeedPercent = it)) })
            // Gear 3
            GearRow(gearName = "เกียร์ 3 (High)", speedPercent = params.gear3SpeedPercent, onSpeedChange = { onUpdate(params.copy(gear3SpeedPercent = it)) })
        }
    }
}

@Composable
private fun GearRow(gearName: String, speedPercent: Int, onSpeedChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(gearName, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
            Text("$speedPercent %", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NkCyanAccent)
        }
        Slider(
            value = speedPercent.toFloat(),
            onValueChange = { onSpeedChange(it.toInt()) },
            valueRange = 20f..100f,
            colors = SliderDefaults.colors(thumbColor = NkCyanAccent, activeTrackColor = NkCyanAccent)
        )
    }
}

@Composable
private fun FluxWeakeningTab(params: VotolParameters, onUpdate: (VotolParameters) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("ระบบเพิ่มรอบปลาย (Flux Weakening)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("ขับสนามแม่เหล็กอ่อน เพิ่มความเร็วปลายได้ 15-30 km/h", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
                Switch(
                    checked = params.fluxWeakeningEnabled,
                    onCheckedChange = { onUpdate(params.copy(fluxWeakeningEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF5722), checkedTrackColor = Color(0xFFFF5722).copy(alpha = 0.3f))
                )
            }

            if (params.fluxWeakeningEnabled) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("กระแสเพิ่มรอบปลาย (Flux Current)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("${params.fluxWeakeningCurrentA} A", fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))
                }
                Slider(
                    value = params.fluxWeakeningCurrentA.toFloat(),
                    onValueChange = { onUpdate(params.copy(fluxWeakeningCurrentA = it.toInt())) },
                    valueRange = 0f..80f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5722), activeTrackColor = Color(0xFFFF5722))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("รอบที่เริ่มทำงาน (Flux Start RPM)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("${params.fluxStartRpm} RPM", fontWeight = FontWeight.Bold, color = NkCyanAccent)
                }
                Slider(
                    value = params.fluxStartRpm.toFloat(),
                    onValueChange = { onUpdate(params.copy(fluxStartRpm = it.toInt())) },
                    valueRange = 1500f..5000f,
                    colors = SliderDefaults.colors(thumbColor = NkCyanAccent, activeTrackColor = NkCyanAccent)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("องศาปรับเฟส (Advance Angle)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("${params.fluxAdvanceAngleDeg}°", fontWeight = FontWeight.Bold, color = NkGold)
                }
                Slider(
                    value = params.fluxAdvanceAngleDeg.toFloat(),
                    onValueChange = { onUpdate(params.copy(fluxAdvanceAngleDeg = it.toInt())) },
                    valueRange = 0f..35f,
                    colors = SliderDefaults.colors(thumbColor = NkGold, activeTrackColor = NkGold)
                )
            }
        }
    }
}

@Composable
private fun EbsRegenTab(params: VotolParameters, onUpdate: (VotolParameters) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("เบรกหน่วงชาร์จไฟกลับ (EBS Regenerative)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("หน่วงความเร็วเมื่อเบรก ชาร์จไฟกลับเข้าแบตเตอรี่", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
                Switch(
                    checked = params.ebsEnabled,
                    onCheckedChange = { onUpdate(params.copy(ebsEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981), checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f))
                )
            }

            if (params.ebsEnabled) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ระดับหน่วงความเร็วต่ำ (Low Speed EBS Level 1-5)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("${params.ebsLowSpeedLevel}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Slider(
                    value = params.ebsLowSpeedLevel.toFloat(),
                    onValueChange = { onUpdate(params.copy(ebsLowSpeedLevel = it.toInt())) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ระดับหน่วงความเร็วสูง (High Speed EBS Level 1-5)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("${params.ebsHighSpeedLevel}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Slider(
                    value = params.ebsHighSpeedLevel.toFloat(),
                    onValueChange = { onUpdate(params.copy(ebsHighSpeedLevel = it.toInt())) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("กระแสชาร์จกลับสูงสุด (Max Regen Current)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text("${params.ebsMaxCurrentA} A", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Slider(
                    value = params.ebsMaxCurrentA.toFloat(),
                    onValueChange = { onUpdate(params.copy(ebsMaxCurrentA = it.toInt())) },
                    valueRange = 5f..60f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                )
            }
        }
    }
}

@Composable
private fun PresetsTab(onApply: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // DK72240 Free Presets
        Text("สูตรจูนกล่อง DK72240 / DK728240 (ปลดล็อกฟรี):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFF5722))

        PresetCard(
            title = "⚡ aekboy2010 DK72240 Full Power 240A (ฟรี)",
            subtitle = "ปลดปล่อยกำลังเฟสเต็ม 240A ปลายไหลด้วย Flux 45A คันเร่งตอบสนองไว",
            badge = "DK72240 ฟรี",
            badgeColor = Color(0xFFFF5722),
            onSelect = { onApply("DK72240_RACING") }
        )
        PresetCard(
            title = "🛵 aekboy2010 DK72240 Street 190A (ฟรี)",
            subtitle = "สูตรวิ่งถนน ทอร์กแน่น ออกตัวนุ่มนวล ประหยัดพลังงาน Phase 190A, Flux 25A",
            badge = "DK72240 ฟรี",
            badgeColor = NkCyanAccent,
            onSelect = { onApply("DK72240_STREET") }
        )
        PresetCard(
            title = "🔋 aekboy2010 DK72240 Eco Standard (ฟรี)",
            subtitle = "สูตรประหยัดแบตเตอรี่ วิ่งได้ไกล ถนอมมอเตอร์และ BMS แบตเตอรี่",
            badge = "DK72240 ฟรี",
            badgeColor = Color(0xFF10B981),
            onSelect = { onApply("DK72240_ECO") }
        )

        Spacer(modifier = Modifier.height(6.dp))
        Divider(color = Color(0xFF1E2D4A))
        Spacer(modifier = Modifier.height(4.dp))

        // VOTOL EM Presets
        Text("สูตรจูนกล่อง VOTOL EM Series:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NkCyanAccent)

        PresetCard(
            title = "🏁 aekboy2010 Racing Track (VOTOL สเต็ปแข่ง)",
            subtitle = "ปลดปล่อยพลังเต็มกำลัง Phase 480A, Flux 60A, คันเร่งติดมือไวสุด",
            badge = "VOTOL แข่งขัน",
            badgeColor = Color(0xFFFF2A4B),
            onSelect = { onApply("RACING") }
        )
        PresetCard(
            title = "🚀 Street Sport (VOTOL สปอร์ต วิ่งถนน)",
            subtitle = "ขี่สนุก ออกตัวนุ่มแต่ปลายไหล เร่งแซงทันใจ Phase 350A, Flux 35A",
            badge = "VOTOL แนะนำ",
            badgeColor = NkCyanAccent,
            onSelect = { onApply("STREET") }
        )
        PresetCard(
            title = "🔋 Eco Delivery (VOTOL เดลิเวอรี่ ประหยัดแบต)",
            subtitle = "จำกัดกระแสเน้นประหยัดแบตเตอรี่ วิ่งได้ระยะทางไกล ถนอมมอเตอร์",
            badge = "VOTOL ประหยัด",
            badgeColor = Color(0xFF10B981),
            onSelect = { onApply("ECO") }
        )
    }
}

@Composable
private fun PresetCard(title: String, subtitle: String, badge: String, badgeColor: Color, onSelect: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D4A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("โหลดสูตรจูนนี้ลงกล่อง", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
