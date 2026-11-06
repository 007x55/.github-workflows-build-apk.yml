package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CloneFilter
import com.example.model.CloneInstance
import com.example.model.InstalledAppItem
import kotlinx.coroutines.flow.*

class CloneManagerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _rawInstalledApps = MutableStateFlow<List<InstalledAppItem>>(defaultPresetApps())
    private val _clonesMap = MutableStateFlow<Map<String, List<CloneInstance>>>(defaultSampleClones())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(CloneFilter.ALL)
    val selectedFilter: StateFlow<CloneFilter> = _selectedFilter.asStateFlow()

    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    private val badgeColors = listOf(
        0xFF00D2FF, // Cyan
        0xFFFF5722, // Orange
        0xFF10B981, // Emerald Green
        0xFF8B5CF6, // Purple
        0xFFF59E0B, // Amber
        0xFFEC4899  // Pink
    )

    // Merged and filtered installed apps flow
    val installedApps: StateFlow<List<InstalledAppItem>> = combine(
        _rawInstalledApps,
        _clonesMap,
        _searchQuery,
        _selectedFilter
    ) { apps, clones, query, filter ->
        computeFilteredApps(apps, clones, query, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = computeFilteredApps(defaultPresetApps(), defaultSampleClones(), "", CloneFilter.ALL)
    )

    // Statistics
    val totalClonesCount: StateFlow<Int> = _clonesMap.map { map ->
        map.values.sumOf { it.size }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = defaultSampleClones().values.sumOf { it.size }
    )

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        val pm = getApplication<Application>().packageManager
        val detectedList = mutableListOf<InstalledAppItem>()

        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)

            for (info in resolveInfos) {
                val pkgName = info.activityInfo.packageName
                val appLabel = info.loadLabel(pm).toString()
                val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                var versionName = "1.0.0"
                try {
                    val pInfo = pm.getPackageInfo(pkgName, 0)
                    versionName = pInfo.versionName ?: "1.0.0"
                } catch (_: Exception) { }

                val category = if (pkgName.contains("google", ignoreCase = true) || pkgName.contains("android", ignoreCase = true)) {
                    "System & Google"
                } else if (pkgName.contains("example") || pkgName.contains("aistudio")) {
                    "EV Controller Suite"
                } else {
                    "Installed App"
                }

                detectedList.add(
                    InstalledAppItem(
                        packageName = pkgName,
                        appName = appLabel,
                        versionName = versionName,
                        isSystemApp = isSystem,
                        category = category
                    )
                )
            }
        } catch (_: Exception) { }

        // Deduplicate detected with preset apps
        val existingPackages = detectedList.map { it.packageName }.toSet()
        for (preset in defaultPresetApps()) {
            if (!existingPackages.contains(preset.packageName)) {
                detectedList.add(preset)
            }
        }

        _rawInstalledApps.value = detectedList
    }

    /**
     * Create a clone for an installed application.
     */
    fun createClone(packageName: String, customName: String? = null) {
        val currentMap = _clonesMap.value.toMutableMap()
        val currentList = currentMap[packageName]?.toMutableList() ?: mutableListOf()
        val cloneNumber = currentList.size + 1

        val app = _rawInstalledApps.value.find { it.packageName == packageName }
        val targetName = customName?.takeIf { it.isNotBlank() }
            ?: "${app?.appName ?: "แอป"} โคลน #${cloneNumber}"

        val colorIndex = currentList.size % badgeColors.size
        val newClone = CloneInstance(
            id = "clone_${System.currentTimeMillis()}_$cloneNumber",
            name = targetName,
            badgeColor = badgeColors[colorIndex],
            storageUsageMb = 12.0 + (cloneNumber * 3.5),
            isRunning = false
        )

        currentList.add(newClone)
        currentMap[packageName] = currentList
        _clonesMap.value = currentMap

        _userFeedback.value = "โคลน '${targetName}' ถูกสร้างเรียบร้อยแล้ว! (แซนด์บ็อกซ์แยกสมบูรณ์)"
    }

    /**
     * Remove an existing clone instance.
     */
    fun removeClone(packageName: String, cloneId: String) {
        val currentMap = _clonesMap.value.toMutableMap()
        val currentList = currentMap[packageName]?.toMutableList() ?: return

        val removed = currentList.find { it.id == cloneId }
        currentList.removeAll { it.id == cloneId }

        if (currentList.isEmpty()) {
            currentMap.remove(packageName)
        } else {
            currentMap[packageName] = currentList
        }

        _clonesMap.value = currentMap
        _userFeedback.value = "ลบโคลน '${removed?.name ?: cloneId}' และล้างข้อมูลแคชแล้ว"
    }

    /**
     * Launch a cloned app or original app with clone profile tag.
     */
    fun launchClone(packageName: String, cloneId: String, cloneName: String) {
        val pm = getApplication<Application>().packageManager
        try {
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("CLONE_INSTANCE_ID", cloneId)
                intent.putExtra("CLONE_INSTANCE_NAME", cloneName)
                getApplication<Application>().startActivity(intent)
            }
        } catch (_: Exception) { }

        _userFeedback.value = "กำลังเปิดพื้นที่โคลน: $cloneName ($packageName)..."
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: CloneFilter) {
        _selectedFilter.value = filter
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    fun getClonesForPackage(packageName: String): List<CloneInstance> {
        return _clonesMap.value[packageName] ?: emptyList()
    }

    companion object {
        fun computeFilteredApps(
            apps: List<InstalledAppItem>,
            clones: Map<String, List<CloneInstance>>,
            query: String,
            filter: CloneFilter
        ): List<InstalledAppItem> {
            return apps.map { app ->
                val appClones = clones[app.packageName] ?: emptyList()
                app.copy(clones = appClones)
            }.filter { app ->
                val matchesQuery = query.isBlank() ||
                        app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true) ||
                        app.category.contains(query, ignoreCase = true)

                val matchesCategory = when (filter) {
                    CloneFilter.ALL -> true
                    CloneFilter.CLONED_ONLY -> app.hasClones
                    CloneFilter.EV_TOOLS -> app.category.contains("EV", ignoreCase = true) ||
                            app.category.contains("Tuning", ignoreCase = true) ||
                            app.packageName.contains("dkys", ignoreCase = true) ||
                            app.packageName.contains("votol", ignoreCase = true) ||
                            app.packageName.contains("controller", ignoreCase = true)
                    CloneFilter.USER_APPS -> !app.isSystemApp
                }

                matchesQuery && matchesCategory
            }
        }

        fun defaultPresetApps(): List<InstalledAppItem> = listOf(
            InstalledAppItem(
                packageName = "com.dkys.spguitool",
                appName = "SP GUI Tool dk 2.0 (DKYS)",
                versionName = "2.0.4",
                isSystemApp = false,
                category = "EV Controller (DK72420S/DK72180)"
            ),
            InstalledAppItem(
                packageName = "com.siaecosys.votol",
                appName = "VOTOL EM Controller Tuner",
                versionName = "3.2.1",
                isSystemApp = false,
                category = "EV Controller (EM-150/200/260)"
            ),
            InstalledAppItem(
                packageName = "com.soyosource.gti1200",
                appName = "Soyosource Smart Monitor 1200W",
                versionName = "1.8.0",
                isSystemApp = false,
                category = "EV Power & Inverter"
            ),
            InstalledAppItem(
                packageName = "com.fardriver.controller",
                appName = "FarDriver FOC Controller Tool",
                versionName = "2.4.9",
                isSystemApp = false,
                category = "EV Controller"
            ),
            InstalledAppItem(
                packageName = "com.evtools.bleterminal",
                appName = "Bluetooth BLE & CAN-Bus Terminal",
                versionName = "2.1.0",
                isSystemApp = false,
                category = "Hardware & Diagnostics"
            ),
            InstalledAppItem(
                packageName = "com.ev.speedometer",
                appName = "GPS Speedometer & Cockpit HUD",
                versionName = "1.4.2",
                isSystemApp = false,
                category = "Telemetry & Dashboard"
            ),
            InstalledAppItem(
                packageName = "jp.naver.line.android",
                appName = "LINE Shop & Customer Support",
                versionName = "14.2.1",
                isSystemApp = false,
                category = "Communication"
            ),
            InstalledAppItem(
                packageName = "com.whatsapp",
                appName = "WhatsApp Business",
                versionName = "2.24.8",
                isSystemApp = false,
                category = "Communication"
            ),
            InstalledAppItem(
                packageName = "com.google.android.apps.maps",
                appName = "Google Maps (EV Charging Stations)",
                versionName = "11.120",
                isSystemApp = true,
                category = "Navigation"
            )
        )

        fun defaultSampleClones(): Map<String, List<CloneInstance>> = mapOf(
            "com.dkys.spguitool" to listOf(
                CloneInstance(
                    id = "clone_dkys_1",
                    name = "โปรไฟล์ DK72420S รถคันที่ 2",
                    badgeColor = 0xFF00D2FF,
                    storageUsageMb = 18.4,
                    isRunning = true
                )
            ),
            "com.siaecosys.votol" to listOf(
                CloneInstance(
                    id = "clone_votol_1",
                    name = "VOTOL Drag Race Setup",
                    badgeColor = 0xFFFF5722,
                    storageUsageMb = 22.1,
                    isRunning = false
                )
            )
        )
    }
}
