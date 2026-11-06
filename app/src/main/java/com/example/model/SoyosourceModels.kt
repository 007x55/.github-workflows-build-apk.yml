package com.example.model

enum class SoyoOperatingMode(val displayName: String, val description: String) {
    PV_MPPT("โหมดแผงโซลาร์เซลล์ (PV MPPT)", "ระบบติดตามจุดกำลังสูงสุดของแผงโซลาร์เซลล์ ผลิตไฟเต็มตามแดดสูงสุด 1200W"),
    BATTERY_CONSTANT("โหมดแบตเตอรี่จ่ายไฟคงที่ (Constant Power)", "ดึงไฟจากแบตเตอรี่ปล่อยเข้าบ้านตามวัตต์ที่ตั้งไว้คงที่ 50W-1200W"),
    BATTERY_LIMITER("โหมดกันย้อน (Limiter / Zero Export)", "จ่ายไฟตามการใช้ไฟของบ้านจริง ไม่ย้อนมิเตอร์ไฟฟ้าผ่าน CT Clamp RS485")
}

data class SoyoTelemetry(
    val dcVoltage: Float = 53.2f,           // Battery or PV voltage (V)
    val dcCurrent: Float = 16.8f,           // DC Current (A)
    val acPower: Float = 850f,              // AC Output Power (Watts: 0 - 1200W)
    val acVoltage: Float = 228.5f,          // AC Grid Voltage (V)
    val acFrequency: Float = 50.0f,         // AC Grid Frequency (Hz)
    val temperature: Float = 47.2f,         // Inverter Heatsink Temp (°C)
    val dailyEnergyKwh: Float = 4.85f,      // Today generated kWh
    val totalEnergyKwh: Float = 1420.6f,    // Lifetime generated kWh
    val limiterDemandWatts: Int = 850,      // Grid meter reading / demand
    val isGridConnected: Boolean = true,    // Grid status
    val isOverheat: Boolean = false,        // Over temp protection (>65C)
    val isLimiterActive: Boolean = true     // Limiter RS485 heartbeat active
)

data class SoyoSettings(
    val targetPowerWatts: Int = 850,        // 50W - 1200W
    val batteryCutoffVoltage: Float = 44.5f, // Cutoff under-voltage
    val batteryRestartVoltage: Float = 49.5f,// Recovery voltage to start again
    val batteryMaxWatts: Int = 900,         // Max power limit in battery mode
    val startupDelaySeconds: Int = 10,      // 0 - 60s
    val operatingMode: SoyoOperatingMode = SoyoOperatingMode.BATTERY_LIMITER,
    val rs485BaudRate: Int = 9600,          // 9600 for status, 4800 for limiter port
    val zeroExportMarginWatts: Int = 20     // Safety anti-reverse buffer
)

data class SoyoPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetWatts: Int,
    val cutoffVoltage: Float,
    val restartVoltage: Float,
    val mode: SoyoOperatingMode
)
