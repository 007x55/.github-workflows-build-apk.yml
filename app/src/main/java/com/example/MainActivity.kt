package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SoyosourceRepository
import com.example.ui.VotolTuningViewModel
import com.example.ui.screens.CloneManagerScreen
import com.example.ui.screens.ConfigManagerScreen
import com.example.ui.screens.ConnectionScreen
import com.example.ui.screens.LiveDashboardScreen
import com.example.ui.screens.OverviewDashboardScreen
import com.example.ui.screens.SoyosourceScreen
import com.example.ui.screens.SpGuiToolScreen
import com.example.ui.screens.TuningScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NkCardBg
import com.example.ui.theme.NkDarkBg
import com.example.viewmodel.CloneManagerViewModel
import com.example.viewmodel.ControllerProfileViewModel

enum class AppWorkspace {
    EV_TUNER,
    DKYS_SP_GUI,
    SOYOSOURCE_1200W,
    CLONE_MANAGER
}

enum class AekboyScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("แดชบอร์ด", Icons.Default.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    TUNING("จูนกล่อง", Icons.Default.Tune, Icons.Outlined.Tune, "tab_tuning"),
    COCKPIT("มอนิเตอร์", Icons.Default.Speed, Icons.Outlined.Speed, "tab_cockpit"),
    CONNECTION("เชื่อมต่อ", Icons.Default.Cable, Icons.Outlined.Cable, "tab_connection"),
    CONFIG("ไฟล์ & ร้าน", Icons.Default.Settings, Icons.Outlined.Settings, "tab_config")
}

class MainActivity : ComponentActivity() {

    private val viewModel: VotolTuningViewModel by viewModels()
    private val profileViewModel: ControllerProfileViewModel by viewModels()
    private val cloneViewModel: CloneManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                AekboyAppRoot(
                    viewModel = viewModel,
                    profileViewModel = profileViewModel,
                    cloneViewModel = cloneViewModel
                )
            }
        }
    }
}

@Composable
fun AekboyAppRoot(
    viewModel: VotolTuningViewModel,
    profileViewModel: ControllerProfileViewModel,
    cloneViewModel: CloneManagerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var currentWorkspace by remember { mutableStateOf(AppWorkspace.EV_TUNER) }
    var currentEvScreen by remember { mutableStateOf(AekboyScreen.DASHBOARD) }
    val soyoRepository = remember { SoyosourceRepository() }
    val dkysRepository = remember { com.example.data.DkysRepository() }

    // BackHandler navigates back cleanly
    BackHandler(enabled = currentWorkspace != AppWorkspace.EV_TUNER || currentEvScreen != AekboyScreen.DASHBOARD) {
        if (currentWorkspace != AppWorkspace.EV_TUNER) {
            currentWorkspace = AppWorkspace.EV_TUNER
        } else {
            currentEvScreen = AekboyScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NkDarkBg),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            // Top Mode Switcher Bar: VOTOL EM, SP GUI dk 2.0 (DKYS), Soyosource 1200W
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                color = Color(0xFF0F172A),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // VOTOL EM Controller Workspace Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (currentWorkspace == AppWorkspace.EV_TUNER) Color(0xFFFF5722) else Color(0xFF1E293B)
                            )
                            .clickable { currentWorkspace = AppWorkspace.EV_TUNER }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = if (currentWorkspace == AppWorkspace.EV_TUNER) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "🛵 VOTOL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentWorkspace == AppWorkspace.EV_TUNER) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // SP GUI Tool dk 2.0 (DKYS FOC) Workspace Button
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (currentWorkspace == AppWorkspace.DKYS_SP_GUI) Color(0xFF0284C7) else Color(0xFF1E293B)
                            )
                            .clickable { currentWorkspace = AppWorkspace.DKYS_SP_GUI }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = if (currentWorkspace == AppWorkspace.DKYS_SP_GUI) Color.White else Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "⚡ SP GUI dk 2.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentWorkspace == AppWorkspace.DKYS_SP_GUI) Color.White else Color(0xFF38BDF8)
                            )
                        }
                    }

                    // Soyosource 1200W Workspace Button (kept separate)
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (currentWorkspace == AppWorkspace.SOYOSOURCE_1200W) Color(0xFFFFC107) else Color(0xFF1E293B)
                            )
                            .clickable { currentWorkspace = AppWorkspace.SOYOSOURCE_1200W }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (currentWorkspace == AppWorkspace.SOYOSOURCE_1200W) Color.Black else Color(0xFFFFC107),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "☀️ Inverter",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentWorkspace == AppWorkspace.SOYOSOURCE_1200W) Color.Black else Color(0xFFFFC107)
                            )
                        }
                    }

                    // Clone Manager Workspace Button
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (currentWorkspace == AppWorkspace.CLONE_MANAGER) Color(0xFF10B981) else Color(0xFF1E293B)
                            )
                            .clickable { currentWorkspace = AppWorkspace.CLONE_MANAGER }
                            .padding(vertical = 8.dp)
                            .testTag("tab_clone_manager"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (currentWorkspace == AppWorkspace.CLONE_MANAGER) Color.White else Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "📱 โคลน",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentWorkspace == AppWorkspace.CLONE_MANAGER) Color.White else Color(0xFF34D399)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (currentWorkspace == AppWorkspace.EV_TUNER) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = NkCardBg,
                    tonalElevation = 6.dp
                ) {
                    AekboyScreen.values().forEach { screen ->
                        val isSelected = currentEvScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentEvScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFFF5722),
                                selectedTextColor = Color(0xFFFF5722),
                                indicatorColor = Color(0xFFFF5722).copy(alpha = 0.2f),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentWorkspace) {
                AppWorkspace.EV_TUNER -> {
                    when (currentEvScreen) {
                        AekboyScreen.DASHBOARD -> OverviewDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTuning = { currentEvScreen = AekboyScreen.TUNING },
                            onNavigateToCockpit = { currentEvScreen = AekboyScreen.COCKPIT },
                            onNavigateToConnection = { currentEvScreen = AekboyScreen.CONNECTION },
                            onNavigateToConfig = { currentEvScreen = AekboyScreen.CONFIG },
                            onNavigateToSoyosource = { currentWorkspace = AppWorkspace.SOYOSOURCE_1200W },
                            onNavigateToDkys = { currentWorkspace = AppWorkspace.DKYS_SP_GUI },
                            onNavigateToCloneManager = { currentWorkspace = AppWorkspace.CLONE_MANAGER }
                        )
                        AekboyScreen.TUNING -> TuningScreen(viewModel = viewModel)
                        AekboyScreen.COCKPIT -> LiveDashboardScreen(viewModel = viewModel)
                        AekboyScreen.CONNECTION -> ConnectionScreen(viewModel = viewModel)
                        AekboyScreen.CONFIG -> ConfigManagerScreen(
                            viewModel = viewModel,
                            profileViewModel = profileViewModel
                        )
                    }
                }
                AppWorkspace.DKYS_SP_GUI -> {
                    SpGuiToolScreen(
                        repository = dkysRepository,
                        profileViewModel = profileViewModel,
                        onBackToHome = { currentWorkspace = AppWorkspace.EV_TUNER }
                    )
                }
                AppWorkspace.SOYOSOURCE_1200W -> {
                    SoyosourceScreen(
                        repository = soyoRepository,
                        onBackToEvTuner = { currentWorkspace = AppWorkspace.EV_TUNER }
                    )
                }
                AppWorkspace.CLONE_MANAGER -> {
                    CloneManagerScreen(
                        viewModel = cloneViewModel,
                        onBackToHome = { currentWorkspace = AppWorkspace.EV_TUNER }
                    )
                }
            }
        }
    }
}
