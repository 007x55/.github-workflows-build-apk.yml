package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CloneFilter
import com.example.model.CloneInstance
import com.example.model.InstalledAppItem
import com.example.ui.theme.*
import com.example.viewmodel.CloneManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneManagerScreen(
    viewModel: CloneManagerViewModel = viewModel(),
    onBackToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsState()
    val totalClones by viewModel.totalClonesCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val userFeedback by viewModel.userFeedback.collectAsState()

    var showCreateDialogForApp by remember { mutableStateOf<InstalledAppItem?>(null) }
    var customCloneNameInput by remember { mutableStateOf("") }

    // Handle system back navigation
    BackHandler {
        onBackToHome()
    }

    // Feedback toast
    LaunchedEffect(userFeedback) {
        userFeedback?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
            .testTag("clone_manager_screen"),
        containerColor = NkDarkBg,
        topBar = {
            CloneManagerTopBar(
                totalClones = totalClones,
                onBackClicked = onBackToHome,
                onRefresh = { viewModel.loadInstalledApps() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("clone_manager_list"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner & Sandbox Stats Card
            item(key = "stats_banner") {
                CloneStatsHeaderCard(
                    totalApps = installedApps.size,
                    activeClones = totalClones
                )
            }

            // Search Bar
            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_installed_apps"),
                    placeholder = {
                        Text(
                            text = "ค้นหาแอปที่ติดตั้ง หรือ แพ็กเกจ...",
                            color = NkTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Apps",
                            tint = NkCyanAccent
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = NkTextMuted
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NkCardBg,
                        unfocusedContainerColor = NkCardBg,
                        focusedBorderColor = NkCyanAccent,
                        unfocusedBorderColor = NkBorderDark,
                        focusedTextColor = NkTextPrimary,
                        unfocusedTextColor = NkTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filter Chips Row
            item(key = "filter_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CloneFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = NkCardBg,
                                labelColor = NkTextSecondary,
                                selectedContainerColor = Color(0xFF007DFE).copy(alpha = 0.25f),
                                selectedLabelColor = NkCyanAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NkCyanAccent else NkBorderDark
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.name}")
                        )
                    }
                }
            }

            // Section Label
            item(key = "section_label") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "รายการแอปที่ติดตั้ง (${installedApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NkTextPrimary
                    )
                    Text(
                        text = "กดปุ่มเพื่อสร้างโคลนแอป",
                        style = MaterialTheme.typography.labelSmall,
                        color = NkCyanAccent
                    )
                }
            }

            // Empty state if search yielded nothing
            if (installedApps.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = NkTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ไม่พบแอปที่ตรงกับคำค้นหา",
                                color = NkTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Installed Apps List
            items(
                items = installedApps,
                key = { it.packageName }
            ) { app ->
                InstalledAppCard(
                    app = app,
                    onCreateCloneClicked = {
                        // Open clone configuration dialog
                        showCreateDialogForApp = app
                        customCloneNameInput = "${app.appName} โคลน #${app.cloneCount + 1}"
                    },
                    onQuickCloneClicked = {
                        viewModel.createClone(app.packageName)
                    },
                    onLaunchClone = { clone ->
                        viewModel.launchClone(app.packageName, clone.id, clone.name)
                    },
                    onDeleteClone = { clone ->
                        viewModel.removeClone(app.packageName, clone.id)
                    }
                )
            }
        }
    }

    // Modal Dialog to configure and create custom clone
    showCreateDialogForApp?.let { targetApp ->
        AlertDialog(
            onDismissRequest = { showCreateDialogForApp = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = NkCyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "สร้างโคลนแอปใหม่ (Dual Sandbox)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NkTextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "สร้างพื้นที่แซนด์บ็อกซ์แยกอิสระสำหรับ:\n${targetApp.appName} (${targetApp.packageName})",
                        color = NkTextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = customCloneNameInput,
                        onValueChange = { customCloneNameInput = it },
                        label = { Text("ชื่อโคลน / โปรไฟล์", color = NkCyanAccent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_clone_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NkCardBg,
                            unfocusedContainerColor = NkCardBg,
                            focusedBorderColor = NkCyanAccent,
                            unfocusedBorderColor = NkBorderDark,
                            focusedTextColor = NkTextPrimary,
                            unfocusedTextColor = NkTextPrimary
                        ),
                        singleLine = true
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ข้อมูลการตั้งค่า, บัญชี และแคชจะถูกแยกอิสระ 100% จากแอปหลัก",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createClone(targetApp.packageName, customCloneNameInput)
                        showCreateDialogForApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NkBlueBrand),
                    modifier = Modifier.testTag("confirm_create_clone_button")
                ) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("สร้างโคลนทันที", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialogForApp = null }
                ) {
                    Text("ยกเลิก", color = NkTextSecondary)
                }
            },
            containerColor = NkCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Top App Bar for Clone Manager
 */
@Composable
private fun CloneManagerTopBar(
    totalClones: Int,
    onBackClicked: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F172A),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier.testTag("btn_clone_manager_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "กลับหน้าหลัก",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📱 Clone Manager",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00D2FF).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Dual Sandbox",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00D2FF)
                            )
                        }
                    }
                    Text(
                        text = "ระบบสร้างแอปโคลน & จัดการหลายโปรไฟล์พร้อมกัน",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("btn_refresh_installed_apps")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "รีเฟรชรายการแอป",
                    tint = NkCyanAccent
                )
            }
        }
    }
}

/**
 * Summary Statistics Header Card
 */
@Composable
private fun CloneStatsHeaderCard(
    totalApps: Int,
    activeClones: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(
            listOf(Color(0xFF007DFE), Color(0xFF00D2FF))
        ))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007DFE).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = NkCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Multi-App Engine สถานะพร้อมใช้งาน",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NkTextPrimary
                        )
                        Text(
                            text = "โคลนแอปจูนกล่อง DKYS, VOTOL และแอปทั่วไปแยกโปรไฟล์",
                            fontSize = 11.sp,
                            color = NkTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● ระบบทำงาน",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            HorizontalDivider(color = NkBorderDark, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumnItem(
                    label = "แอปในระบบ",
                    value = "$totalApps",
                    icon = Icons.Default.Apps,
                    iconColor = Color(0xFF38BDF8)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(NkBorderDark)
                )
                StatColumnItem(
                    label = "โคลนที่กำลังรัน",
                    value = "$activeClones",
                    icon = Icons.Default.ContentCopy,
                    iconColor = Color(0xFF00D2FF)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(NkBorderDark)
                )
                StatColumnItem(
                    label = "ความจุเซฟตี้",
                    value = "แยกแคช 100%",
                    icon = Icons.Default.Storage,
                    iconColor = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun StatColumnItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NkTextPrimary
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = NkTextMuted
        )
    }
}

/**
 * Individual Installed App Item Card with "Create Clone" Action Button
 */
@Composable
private fun InstalledAppCard(
    app: InstalledAppItem,
    onCreateCloneClicked: () -> Unit,
    onQuickCloneClicked: () -> Unit,
    onLaunchClone: (CloneInstance) -> Unit,
    onDeleteClone: (CloneInstance) -> Unit
) {
    var isExpanded by remember { mutableStateOf(app.hasClones) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_item_${app.packageName}"),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (app.hasClones) Brush.horizontalGradient(
                listOf(Color(0xFF007DFE).copy(alpha = 0.6f), Color(0xFF00D2FF).copy(alpha = 0.6f))
            ) else Brush.horizontalGradient(listOf(NkBorderDark, NkBorderDark))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // App info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Monogram / Icon Placeholder
                val monogramLetter = app.appName.firstOrNull()?.toString()?.uppercase() ?: "A"
                val appColor = when {
                    app.packageName.contains("dkys") -> Color(0xFF0284C7)
                    app.packageName.contains("votol") -> Color(0xFFFF5722)
                    app.packageName.contains("soyo") -> Color(0xFFFFC107)
                    app.packageName.contains("line") -> Color(0xFF06C755)
                    app.packageName.contains("whatsapp") -> Color(0xFF25D366)
                    app.isSystemApp -> Color(0xFF64748B)
                    else -> Color(0xFF007DFE)
                }

                // App Monogram with Status Dot
                Box(modifier = Modifier.size(48.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(10.dp))
                            .background(appColor.copy(alpha = 0.2f))
                            .border(1.dp, appColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monogramLetter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColor
                        )
                    }

                    // Colored Status Dot on icon corner
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(if (app.hasClones) Color(0xFF10B981) else Color(0xFF64748B))
                            .border(1.5.dp, NkCardBg, CircleShape)
                            .testTag("clone_icon_dot_${app.packageName}")
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Package Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NkTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NkTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = app.category,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Version Chip
                        Text(
                            text = "v${app.versionName}",
                            fontSize = 9.sp,
                            color = NkTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Visual Status Indicator Badge with colored dot
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (app.hasClones) Color(0xFF10B981).copy(alpha = 0.16f)
                            else Color(0xFF334155).copy(alpha = 0.35f)
                        )
                        .border(
                            1.dp,
                            if (app.hasClones) Color(0xFF10B981).copy(alpha = 0.6f)
                            else Color(0xFF475569).copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("clone_status_badge_${app.packageName}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Colored Status Dot: Green if clone exists, Grey if not
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (app.hasClones) Color(0xFF10B981) else Color(0xFF94A3B8))
                                .testTag("clone_status_dot_${app.packageName}")
                        )
                        Text(
                            text = if (app.hasClones) "โคลนแล้ว (${app.cloneCount})" else "ยังไม่โคลน",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (app.hasClones) Color(0xFF34D399) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Action Buttons Row: Primary "Create Clone" button + Quick Clone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Button: Create Clone
                Button(
                    onClick = onCreateCloneClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("clone_button_${app.packageName}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (app.hasClones) Color(0xFF0284C7) else NkBlueBrand
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Create Clone for ${app.appName}",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (app.hasClones) "โคลนเพิ่ม (+1)" else "สร้างโคลน (Clone)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // If app already has clones, show toggle to expand/collapse clone instances
                if (app.hasClones) {
                    OutlinedButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("toggle_clones_${app.packageName}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NkCyanAccent
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.horizontalGradient(listOf(NkBorderDark, NkCyanAccent.copy(alpha = 0.5f)))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "ซ่อนโคลน" else "ดูโคลน (${app.cloneCount})",
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded Clones List
            AnimatedVisibility(
                visible = isExpanded && app.hasClones,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = NkBorderDark, thickness = 1.dp)

                    Text(
                        text = "อินสแตนซ์โคลนที่ทำงานอยู่:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NkTextSecondary
                    )

                    app.clones.forEachIndexed { index, clone ->
                        CloneInstanceRow(
                            clone = clone,
                            index = index + 1,
                            onLaunch = { onLaunchClone(clone) },
                            onDelete = { onDeleteClone(clone) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Clone Row inside an installed app card
 */
@Composable
private fun CloneInstanceRow(
    clone: CloneInstance,
    index: Int,
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(0.5.dp, Color(clone.badgeColor).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("clone_instance_${clone.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(clone.badgeColor))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = clone.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (clone.isRunning) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ทำงานอยู่",
                                fontSize = 8.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                Text(
                    text = "ID: ${clone.id} • แคช: ${String.format("%.1f", clone.storageUsageMb)} MB",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Action Buttons: Open/Launch and Delete
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Launch Clone Button
            IconButton(
                onClick = onLaunch,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("launch_clone_${clone.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "เปิดใช้งาน ${clone.name}",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Delete Clone Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_clone_${clone.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "ลบโคลน ${clone.name}",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
