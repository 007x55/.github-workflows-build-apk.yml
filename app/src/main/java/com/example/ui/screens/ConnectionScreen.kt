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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionState
import com.example.model.ConnectionType
import com.example.model.ControllerModel
import com.example.ui.VotolTuningViewModel
import com.example.ui.theme.*

@Composable
fun ConnectionScreen(
    viewModel: VotolTuningViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedInterface by viewModel.selectedInterface.collectAsState()
    val params by viewModel.params.collectAsState()
    val packetLogs by viewModel.packetLogs.collectAsState()

    var showModelDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header: aekboy2010 shop
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "aekboy2010 ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = NkCyanAccent
                            )
                            Text(
                                text = "SHOP",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = Color(0xFFFF5722)
                            )
                        }
                        Text(
                            text = "VOTOL EM TUNING & DIAGNOSTIC TOOL",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (connectionState) {
                                    ConnectionState.CONNECTED -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    ConnectionState.CONNECTING, ConnectionState.READING_PARAMS, ConnectionState.WRITING_PARAMS -> NkGold.copy(alpha = 0.2f)
                                    ConnectionState.DISCONNECTED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                }
                            )
                            .border(
                                1.dp,
                                when (connectionState) {
                                    ConnectionState.CONNECTED -> Color(0xFF10B981)
                                    ConnectionState.CONNECTING, ConnectionState.READING_PARAMS, ConnectionState.WRITING_PARAMS -> NkGold
                                    ConnectionState.DISCONNECTED -> Color(0xFFEF4444)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (connectionState) {
                                ConnectionState.CONNECTED -> "เชื่อมต่อแล้ว"
                                ConnectionState.CONNECTING -> "กำลังเชื่อมต่อ..."
                                ConnectionState.READING_PARAMS -> "กำลังอ่านค่า..."
                                ConnectionState.WRITING_PARAMS -> "กำลังบันทึก..."
                                ConnectionState.DISCONNECTED -> "ออฟไลน์"
                            },
                            color = when (connectionState) {
                                ConnectionState.CONNECTED -> Color(0xFF10B981)
                                ConnectionState.CONNECTING, ConnectionState.READING_PARAMS, ConnectionState.WRITING_PARAMS -> NkGold
                                ConnectionState.DISCONNECTED -> Color(0xFFEF4444)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Connection Type Selector (USB / CAN / Bluetooth)
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
                        text = "เลือกช่องทางการเชื่อมต่อกล่อง Votol",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "รองรับทั้งสาย USB-OTG, พอร์ต CAN Bus และบลูทูธไร้สาย",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ConnectionType.values().forEach { iface ->
                        val isSelected = selectedInterface == iface
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF12223D) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) NkCyanAccent else Color(0xFF1E2D4A),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setInterface(iface) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (iface) {
                                    ConnectionType.USB_CABLE -> Icons.Default.Usb
                                    ConnectionType.CAN_BUS -> Icons.Default.SettingsEthernet
                                    ConnectionType.BLUETOOTH_BLE -> Icons.Default.Bluetooth
                                },
                                contentDescription = null,
                                tint = if (isSelected) NkCyanAccent else Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = iface.titleTh,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = "พอร์ต: ${iface.defaultPort}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setInterface(iface) },
                                colors = RadioButtonDefaults.colors(selectedColor = NkCyanAccent)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Controller Model Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showModelDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "รุ่นกล่องควบคุม VOTOL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = params.model.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Max Bus: ${params.model.maxBusCurrentA}A | Peak Phase: ${params.model.maxPhaseCurrentA}A",
                            style = MaterialTheme.typography.labelSmall,
                            color = NkCyanAccent
                        )
                    }

                    Button(
                        onClick = { showModelDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D4A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("เปลี่ยนรุ่น", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Action Buttons: Connect, Read, Write
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Connect / Disconnect button
                Button(
                    onClick = {
                        if (connectionState == ConnectionState.CONNECTED) {
                            viewModel.disconnect()
                        } else {
                            viewModel.connect()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (connectionState == ConnectionState.CONNECTED) Color(0xFFEF4444) else NkBlueBrand
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_connect_toggle")
                ) {
                    Icon(
                        imageVector = if (connectionState == ConnectionState.CONNECTED) Icons.Default.LinkOff else Icons.Default.Link,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) "ตัดการเชื่อมต่อ" else "เชื่อมต่อกล่อง",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Read Parameters
                OutlinedButton(
                    onClick = {
                        viewModel.readParameters {
                            Toast.makeText(context, "อ่านข้อมูลจากกล่อง Votol สำเร็จ!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NkCyanAccent),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NkCyanAccent)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_read_params")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("อ่านค่า (Read)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Write Parameters
                Button(
                    onClick = {
                        viewModel.writeParameters {
                            Toast.makeText(context, "เขียนค่าลงกล่อง Votol สำเร็จเรียบร้อย!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_write_params")
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เขียนค่า (Write)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Hex Packet Log Terminal
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070D18)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VOTOL UART / CAN FRAME STREAM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NkCyanAccent
                            )
                        }
                        Text(
                            text = "Baud: 115200 8N1",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030710))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        packetLogs.take(8).forEach { packet ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = packet.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.width(72.dp)
                                )
                                Text(
                                    text = if (packet.isTx) "TX >> " else "RX << ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (packet.isTx) Color(0xFFFF5722) else Color(0xFF10B981)
                                )
                                Text(
                                    text = packet.hexData,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Model Selector Dialog
    if (showModelDialog) {
        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFF5722))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("เลือกรุ่นกล่อง (รองรับ VOTOL & ฟรี DK72240)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        Text("ซีรีส์ DK / DKYS (ปลดล็อกฟรี!):", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    items(ControllerModel.values().filter { it.series.contains("DK") }) { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (params.model == model) Color(0xFF1E2D4A) else Color.Transparent)
                                .clickable {
                                    viewModel.selectModel(model)
                                    showModelDialog = false
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = params.model == model,
                                onClick = {
                                    viewModel.selectModel(model)
                                    showModelDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5722))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(model.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Max Bus ${model.maxBusCurrentA}A | Phase ${model.maxPhaseCurrentA}A", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ซีรีส์ VOTOL EM:", color = NkCyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    items(ControllerModel.values().filter { !it.series.contains("DK") }) { model ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (params.model == model) Color(0xFF1E2D4A) else Color.Transparent)
                                .clickable {
                                    viewModel.selectModel(model)
                                    showModelDialog = false
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = params.model == model,
                                onClick = {
                                    viewModel.selectModel(model)
                                    showModelDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = NkCyanAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
                TextButton(onClick = { showModelDialog = false }) {
                    Text("ปิด", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0D1527)
        )
    }
}
