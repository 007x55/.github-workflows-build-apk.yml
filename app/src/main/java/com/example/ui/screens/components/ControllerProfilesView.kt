package com.example.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DkysRepository
import com.example.data.db.ControllerProfileEntity
import com.example.ui.VotolTuningViewModel
import com.example.ui.theme.*
import com.example.viewmodel.ControllerProfileViewModel

@Composable
fun ControllerProfilesView(
    profileViewModel: ControllerProfileViewModel,
    dkysRepository: DkysRepository? = null,
    votolViewModel: VotolTuningViewModel? = null,
    targetControllerType: String? = null, // "DKYS" or "VOTOL" or null for all
    onProfileLoaded: (ControllerProfileEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profiles by profileViewModel.profiles.collectAsState()
    val selectedCategory by profileViewModel.selectedCategory.collectAsState()
    val searchQuery by profileViewModel.searchQuery.collectAsState()
    val feedbackMessage by profileViewModel.userFeedbackMessage.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<ControllerProfileEntity?>(null) }

    // Pre-filter if screen has a fixed target
    LaunchedEffect(targetControllerType) {
        if (targetControllerType != null) {
            profileViewModel.setCategoryFilter(targetControllerType)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NkDarkBg)
    ) {
        // Feedback snackbar banner
        if (feedbackMessage != null) {
            Surface(
                color = Color(0xFF065F46),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMessage.orEmpty(),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { profileViewModel.clearFeedback() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Action Header & Save Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "คลังสูตรจูน Room Database",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "บันทึกและโหลดสูตรกล่อง DK72420S & VOTOL เก็บถาวร",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Button(
                onClick = { showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NkCyanAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_save_new_profile")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "บันทึกสูตรนี้",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { profileViewModel.setSearchQuery(it) },
            placeholder = { Text("ค้นหาชื่อสูตร หรือรุ่นกล่อง (เช่น DK72420S, 72V)...", color = Color(0xFF64748B), fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { profileViewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NkCyanAccent,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("input_search_profiles")
        )

        // Category Filter Chips
        if (targetControllerType == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "ทั้งหมด",
                    "DKYS" to "DKYS FOC",
                    "VOTOL" to "VOTOL EM"
                ).forEach { (catKey, catLabel) ->
                    val isSelected = selectedCategory.equals(catKey, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { profileViewModel.setCategoryFilter(catKey) },
                        label = { Text(catLabel, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NkCyanAccent,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NkCyanAccent else Color(0xFF334155)
                        )
                    )
                }
            }
        }

        // Profiles Count & Content
        Text(
            text = "พบ ${profiles.size} รายการสูตรที่พร้อมใช้งาน",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ไม่พบสูตรที่ตรงกับเงื่อนไข",
                        color = Color(0xFF94A3B8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "กดปุ่ม 'บันทึกสูตรนี้' เพื่อสร้างสูตรจูนใหม่",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileCard(
                        profile = profile,
                        onLoad = {
                            if (profile.controllerCategory == "DKYS" && dkysRepository != null) {
                                profileViewModel.loadProfileIntoDkys(profile, dkysRepository)
                                onProfileLoaded(profile)
                            } else if (profile.controllerCategory == "VOTOL" && votolViewModel != null) {
                                profileViewModel.loadProfileIntoVotol(profile, votolViewModel)
                                onProfileLoaded(profile)
                            } else if (dkysRepository != null) {
                                profileViewModel.loadProfileIntoDkys(profile, dkysRepository)
                                onProfileLoaded(profile)
                            } else if (votolViewModel != null) {
                                profileViewModel.loadProfileIntoVotol(profile, votolViewModel)
                                onProfileLoaded(profile)
                            }
                        },
                        onDuplicate = { profileViewModel.duplicateProfile(profile) },
                        onToggleFavorite = { profileViewModel.toggleFavorite(profile) },
                        onDelete = { profileToDelete = profile }
                    )
                }
            }
        }
    }

    // Save Profile Dialog
    if (showSaveDialog) {
        SaveProfileDialog(
            targetCategory = targetControllerType ?: if (dkysRepository != null) "DKYS" else "VOTOL",
            dkysRepository = dkysRepository,
            votolViewModel = votolViewModel,
            onDismiss = { showSaveDialog = false },
            onSave = { name, notes ->
                if ((targetControllerType == "DKYS" || dkysRepository != null) && dkysRepository != null) {
                    profileViewModel.saveDkysProfile(
                        name = name,
                        notes = notes,
                        model = dkysRepository.selectedModel.value,
                        basic = dkysRepository.basicSettings.value,
                        func = dkysRepository.functionSettings.value
                    )
                } else if (votolViewModel != null) {
                    profileViewModel.saveVotolProfile(
                        name = name,
                        notes = notes,
                        params = votolViewModel.params.value
                    )
                }
                showSaveDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("ยืนยันการลบสูตรจูน", color = Color.White) },
            text = {
                Text(
                    "ต้องการลบสูตร '${profileToDelete?.profileName}' ออกจาก Room Database ใช่หรือไม่? การกระทำนี้ไม่สามารถย้อนกลับได้",
                    color = Color(0xFFCBD5E1)
                )
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        profileToDelete?.let { profileViewModel.deleteProfile(it) }
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("ลบออก", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("ยกเลิก", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: ControllerProfileEntity,
    onLoad: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_card_${profile.id}"),
        colors = CardDefaults.cardColors(containerColor = NkCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (profile.isFavorite) Color(0xFFEAB308) else Color(0xFF334155)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category Badge + Title + Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val categoryColor = if (profile.controllerCategory == "DKYS") Color(0xFF0284C7) else Color(0xFFFF5722)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .border(1.dp, categoryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profile.controllerCategory,
                            color = categoryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = profile.profileName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (profile.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (profile.isFavorite) Color(0xFFEAB308) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specs badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SpecBadge(label = "รุ่น", value = profile.modelName, color = NkCyanAccent)
                SpecBadge(label = "โวลต์", value = "${profile.systemVoltage}V", color = Color(0xFF38BDF8))
                SpecBadge(label = "กระแสเฟส", value = "${profile.maxPhaseCurrentA}A", color = Color(0xFFF97316))
                SpecBadge(label = "ความเร็ว", value = "${profile.topSpeedLimitKmh} km/h", color = Color(0xFF4ADE80))
            }

            if (profile.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.notes,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onDuplicate,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF475569))
                        )
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("คัดลอก", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7F1D1D))
                        )
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ลบ", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_load_profile_${profile.id}")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "โหลดใส่กล่อง",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpecBadge(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SaveProfileDialog(
    targetCategory: String,
    dkysRepository: DkysRepository?,
    votolViewModel: VotolTuningViewModel?,
    onDismiss: () -> Unit,
    onSave: (name: String, notes: String) -> Unit
) {
    var profileName by remember {
        mutableStateOf(
            if (targetCategory == "DKYS" && dkysRepository != null) {
                val model = dkysRepository.selectedModel.value
                "${model.name} สูตรแต่งทางเรียบ"
            } else if (votolViewModel != null) {
                val model = votolViewModel.params.value.model
                "${model.displayName} สูตรขี่เที่ยว"
            } else {
                "สูตรจูน EV ใหม่"
            }
        )
    }

    var notes by remember {
        mutableStateOf("ปรับโดย aekboy2010 shop - สเต็ปตอบสนองคันเร่งฉับไวและประหยัดแบตเตอรี่")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    tint = NkCyanAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("บันทึกสูตรจูนลง Room Database", color = Color.White, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "จัดเก็บการตั้งค่าปัจจุบันลงฐานข้อมูลภายในเครื่อง สามารถนำกลับมาโหลดหรือแก้ไขได้ทุกเวลา",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("ชื่อสูตรจูน / ชื่อโปรไฟล์") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NkCyanAccent,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_name")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("บันทึกรายละเอียด / หมายเหตุ (Notes)") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NkCyanAccent,
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_notes")
                )
            }
        },
        containerColor = Color(0xFF1E293B),
        confirmButton = {
            Button(
                onClick = { onSave(profileName, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = NkCyanAccent),
                modifier = Modifier.testTag("btn_confirm_save_profile")
            ) {
                Text("บันทึกถาวร", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = Color(0xFF94A3B8))
            }
        }
    )
}
