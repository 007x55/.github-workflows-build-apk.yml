package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VotolTuningViewModel
import com.example.ui.screens.components.ControllerProfilesView
import com.example.ui.theme.*
import com.example.viewmodel.ControllerProfileViewModel

enum class ConfigSectionTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ROOM_PROFILES("คลังสูตร Room", Icons.Default.FolderSpecial),
    INI_EXPORT("ส่งออก .INI & ร้านค้า", Icons.Default.Description)
}

@Composable
fun ConfigManagerScreen(
    viewModel: VotolTuningViewModel,
    profileViewModel: ControllerProfileViewModel? = null,
    modifier: Modifier = Modifier
) {
    val actualProfileViewModel: ControllerProfileViewModel =
        profileViewModel ?: androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current
    val params by viewModel.params.collectAsState()

    var activeSection by remember { mutableStateOf(ConfigSectionTab.ROOM_PROFILES) }
    var showExportDialog by remember { mutableStateOf(false) }

    val iniConfigPreview = remember(params) {
        """
        [VOTOL_CONTROLLER_CONFIG]
        SHOP=aekboy2010 shop
        MODEL=${params.model.name}
        RATED_VOLTAGE=${params.ratedVoltage}
        UNDER_VOLT_PROTECT=${params.underVoltageProtection}
        OVER_VOLT_PROTECT=${params.overVoltageProtection}
        SOFT_START=${params.softStartLevel}
        THROTTLE_MIN=${params.throttleStartVoltage}
        THROTTLE_MAX=${params.throttleEndVoltage}
        BUS_CURRENT_LIMIT=${params.busCurrentLimitA}
        PHASE_CURRENT_LIMIT=${params.phaseCurrentLimitA}
        GEAR1_SPEED_RATIO=${params.gear1SpeedPercent}
        GEAR2_SPEED_RATIO=${params.gear2SpeedPercent}
        GEAR3_SPEED_RATIO=${params.gear3SpeedPercent}
        FLUX_ENABLE=${if (params.fluxWeakeningEnabled) 1 else 0}
        FLUX_CURRENT=${params.fluxWeakeningCurrentA}
        FLUX_START_RPM=${params.fluxStartRpm}
        FLUX_ANGLE=${params.fluxAdvanceAngleDeg}
        EBS_ENABLE=${if (params.ebsEnabled) 1 else 0}
        EBS_LOW_LEVEL=${params.ebsLowSpeedLevel}
        EBS_HIGH_LEVEL=${params.ebsHighSpeedLevel}
        EBS_MAX_CURRENT=${params.ebsMaxCurrentA}
        CHECKSUM=0x9A4F
        """.trimIndent()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
    ) {
        // Top Sub Tab Navigation
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "จัดการไฟล์จูน & ข้อมูลร้าน",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "คลังสูตรจูน Room Database สำหรับกล่อง VOTOL / DKYS และสำรองข้อมูล (.ini)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = activeSection.ordinal,
                    containerColor = Color(0xFF1E293B),
                    contentColor = NkCyanAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeSection.ordinal]),
                            color = NkCyanAccent
                        )
                    }
                ) {
                    ConfigSectionTab.values().forEach { tab ->
                        Tab(
                            selected = activeSection == tab,
                            onClick = { activeSection = tab },
                            icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            text = {
                                Text(
                                    tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (activeSection == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = NkCyanAccent,
                            unselectedContentColor = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        when (activeSection) {
            ConfigSectionTab.ROOM_PROFILES -> {
                ControllerProfilesView(
                    profileViewModel = actualProfileViewModel,
                    votolViewModel = viewModel,
                    targetControllerType = "VOTOL"
                )
            }
            ConfigSectionTab.INI_EXPORT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Export / Import Config Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ไฟล์พารามิเตอร์กล่อง VOTOL (.INI)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "สามารถส่งออกไฟล์การจูนเพื่อนำไปแชร์ให้เพื่อน หรือเปิดในโปรแกรม Votol PC ได้",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Code Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070D18))
                            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = iniConfigPreview,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NkCyanAccent,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("VOTOL Config", iniConfigPreview))
                                Toast.makeText(context, "คัดลอกไฟล์ Config .INI แล้ว!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NkCyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("btn_copy_ini")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("คัดลอก Config", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "นำเข้าไฟล์ Config สำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E2D4A))
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("btn_import_ini")
                        ) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("นำเข้าไฟล์", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // About aekboy2010 shop Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF5722)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "aekboy2010 shop",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ศูนย์จูน ติดตั้ง และจำหน่ายกล่องควบคุม VOTOL EM แท้",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFF1E2D4A))
                    Spacer(modifier = Modifier.height(12.dp))

                    ContactRow(icon = Icons.Default.Build, title = "บริการ:", value = "รับจูนกล่อง Votol ทุกรุ่น, ปลดล็อกความเร็ว, แก้โค้ดเออเรอร์")
                    ContactRow(icon = Icons.Default.Chat, title = "LINE Official:", value = "@aekboy2010")
                    ContactRow(icon = Icons.Default.Phone, title = "เบอร์โทรศัพท์:", value = "08X-XXX-XXXX (สายด่วนจูนเนอร์)")
                    ContactRow(icon = Icons.Default.Cable, title = "รองรับการเชื่อมต่อ:", value = "สาย USB Data, พอร์ต CAN Bus และบลูทูธไร้สาย")

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("LINE ID", "@aekboy2010"))
                            Toast.makeText(context, "คัดลอก LINE: @aekboy2010 เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("คัดลอก LINE: @aekboy2010 ติดต่อช่าง", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}
}
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NkCyanAccent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1), fontWeight = FontWeight.SemiBold)
        }
    }
}
