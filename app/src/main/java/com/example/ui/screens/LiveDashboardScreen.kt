package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VotolTuningViewModel
import com.example.ui.theme.*

@Composable
fun LiveDashboardScreen(
    viewModel: VotolTuningViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val params by viewModel.params.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cockpit Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "เรือนไมล์ & มอนิเตอร์สด (Live Cockpit)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${params.model.displayName} • เฟิร์มแวร์ ${params.firmwareVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("LIVE 10Hz", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Digital Speedometer Display Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gear Selection Bar (P, 1, 2, 3, R)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF070D18))
                            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("P", "1", "2", "3", "R").forEach { gear ->
                            val isSelected = telemetry.currentGear == gear
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            when (gear) {
                                                "3" -> Color(0xFFFF2A4B)
                                                "R" -> NkGold
                                                else -> NkCyanAccent
                                            }
                                        } else Color.Transparent
                                    )
                                    .clickable { viewModel.setSimGear(gear) }
                                    .testTag("cockpit_gear_$gear"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gear,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gauge Canvas
                    SpeedometerCanvas(
                        speedKmh = telemetry.speedKmh,
                        maxSpeed = 160f,
                        rpm = telemetry.motorRpm
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IndicatorPill("FLUX BOOST", telemetry.fluxActive, Color(0xFFFF2A4B))
                        Spacer(modifier = Modifier.width(8.dp))
                        IndicatorPill("EBS REGEN", telemetry.ebsActive, Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        IndicatorPill("VOLT ${String.format("%.1f", telemetry.batteryVoltage)}V", true, NkCyanAccent)
                    }
                }
            }
        }

        // Virtual Throttle Slider for testing
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NkCardBg),
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
                        Text("คันเร่งจำลอง (Virtual Throttle)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("${telemetry.throttlePercent} % (${String.format("%.2f", telemetry.throttleVoltage)} V)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NkCyanAccent)
                    }

                    Slider(
                        value = telemetry.throttlePercent.toFloat(),
                        onValueChange = { viewModel.setSimThrottle(it.toInt()) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = NkCyanAccent, activeTrackColor = NkCyanAccent),
                        modifier = Modifier.fillMaxWidth().testTag("slider_live_throttle")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 25, 50, 75, 100).forEach { p ->
                            OutlinedButton(
                                onClick = { viewModel.setSimThrottle(p) },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (telemetry.throttlePercent == p) NkCyanAccent else Color(0xFF94A3B8)
                                )
                            ) {
                                Text(if (p == 0) "ปล่อย" else "${p}%", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Telemetry Grid Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CockpitMetricCard(
                    title = "กระแสเฟสมอเตอร์ (Phase A)",
                    value = "${telemetry.phaseCurrentA.toInt()} A",
                    subtitle = "Bus Current: ${telemetry.busCurrentA.toInt()} A",
                    icon = Icons.Default.Bolt,
                    color = Color(0xFFFF2A4B),
                    modifier = Modifier.weight(1f)
                )
                CockpitMetricCard(
                    title = "แรงดันระบบ (Battery V)",
                    value = "${String.format("%.1f", telemetry.batteryVoltage)} V",
                    subtitle = "Thro: ${String.format("%.2f", telemetry.throttleVoltage)} V",
                    icon = Icons.Default.BatteryChargingFull,
                    color = NkCyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CockpitMetricCard(
                    title = "อุณหภูมิกล่อง Votol",
                    value = "${String.format("%.1f", telemetry.controllerTempC)} °C",
                    subtitle = "มอเตอร์: ${String.format("%.1f", telemetry.motorTempC)} °C",
                    icon = Icons.Default.Thermostat,
                    color = if (telemetry.controllerTempC > 65f) Color(0xFFFF2A4B) else NkGold,
                    modifier = Modifier.weight(1f)
                )
                CockpitMetricCard(
                    title = "สถานะระบบ (Diagnosis)",
                    value = telemetry.errorCode,
                    subtitle = telemetry.errorDescription,
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SpeedometerCanvas(speedKmh: Float, maxSpeed: Float, rpm: Int) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedKmh,
        animationSpec = tween(250),
        label = "anim_speed"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val strokeWidth = 16.dp.toPx()
            val diameter = minOf(size.width, size.height) - strokeWidth * 2
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f + 8.dp.toPx())

            val startAngle = 140f
            val sweepAngle = 260f

            // Track
            drawArc(
                color = Color(0xFF131D31),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val progress = (animatedSpeed / maxSpeed).coerceIn(0f, 1f)
            if (progress > 0.01f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            NkCyanAccent,
                            if (progress > 0.5f) NkGold else NkCyanAccent,
                            if (progress > 0.75f) Color(0xFFFF2A4B) else NkGold
                        )
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 14.dp)
        ) {
            Text(
                text = "${animatedSpeed.toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                ),
                color = Color.White
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = NkCyanAccent,
                modifier = Modifier.offset(y = (-6).dp)
            )
            Text(
                text = "$rpm RPM",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun IndicatorPill(text: String, isActive: Boolean, activeColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else Color(0xFF0F182A))
            .border(1.dp, if (isActive) activeColor else Color(0xFF1E2D4A), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) activeColor else Color(0xFF64748B)
        )
    }
}

@Composable
private fun CockpitMetricCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        modifier = modifier.border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), maxLines = 1)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF64748B))
        }
    }
}
