package com.example.model

/**
 * Represents a cloned instance of an installed application in the dual-app sandbox.
 */
data class CloneInstance(
    val id: String,
    val name: String,
    val badgeColor: Long = 0xFF00D2FF,
    val createdAt: Long = System.currentTimeMillis(),
    val storageUsageMb: Double = 14.8,
    val isRunning: Boolean = false
)

/**
 * Represents an installed Android application available for cloning.
 */
data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val versionName: String = "1.0.0",
    val isSystemApp: Boolean = false,
    val category: String = "App",
    val clones: List<CloneInstance> = emptyList()
) {
    val cloneCount: Int
        get() = clones.size

    val hasClones: Boolean
        get() = clones.isNotEmpty()
}

/**
 * Filter categories for the installed app list in CloneManagerScreen.
 */
enum class CloneFilter(val label: String) {
    ALL("ทั้งหมด (All)"),
    CLONED_ONLY("โคลนแล้ว (Cloned)"),
    EV_TOOLS("เครื่องมือ EV & ช่าง"),
    USER_APPS("แอปผู้ใช้ (User Apps)")
}
