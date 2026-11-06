package com.example.model

enum class ConnectionType(val titleTh: String, val titleEn: String, val defaultPort: String, val iconName: String) {
    USB_CABLE("สาย USB Data Cable (OTG / Serial)", "USB-OTG Serial", "/dev/ttyUSB0 (CH340/FTDI)", "usb"),
    CAN_BUS("พอร์ต CAN Bus (CAN-H / CAN-L)", "CAN Bus 2.0B", "can0 (250 kbps)", "cable"),
    BLUETOOTH_BLE("บลูทูธไร้สาย (BT / BLE)", "Bluetooth Low Energy", "aekboy-votol-ble", "bluetooth")
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    READING_PARAMS,
    WRITING_PARAMS
}

enum class ControllerModel(
    val displayName: String,
    val maxBusCurrentA: Int,
    val maxPhaseCurrentA: Int,
    val defaultVolt: Int,
    val series: String = "VOTOL",
    val isFree: Boolean = false
) {
    // VOTOL EM Series
    EM_30("VOTOL EM-30", 35, 110, 48, "VOTOL EM"),
    EM_50("VOTOL EM-50", 55, 170, 72, "VOTOL EM"),
    EM_100("VOTOL EM-100", 100, 300, 72, "VOTOL EM"),
    EM_150_2("VOTOL EM-150/2 Super", 150, 450, 72, "VOTOL EM"),
    EM_200_2("VOTOL EM-200/2 Racing", 200, 600, 72, "VOTOL EM"),
    EM_260("VOTOL EM-260 Pro", 260, 750, 84, "VOTOL EM"),

    // DK / DKYS Series (FREE / ฟรี)
    DK_72240("DK72240 / DK728240 (72V 240A) [ฟรี]", 100, 240, 72, "DK / DKYS", true),
    DK_72180("DK72180 (72V 180A) [ฟรี]", 75, 180, 72, "DK / DKYS", true),
    DK_72400S("DK72400S (72V 400A) [ฟรี]", 150, 400, 72, "DK / DKYS", true),
    DK_72550T("DK72550T (72V 550A) [ฟรี]", 200, 550, 72, "DK / DKYS", true)
}

enum class SpeedSensorType(val label: String) {
    HALL("Hall Sensor (เซนเซอร์ฮอลล์)"),
    ONE_LINE("One-Line Meter (สายเดี่ยว)"),
    PHASE_WIRE("Phase Wire (สายเฟสมอเตอร์)")
}

enum class BrakeSignalType(val label: String) {
    LOW_LEVEL("Low Level (0V กราวด์สวิตช์เบรก)"),
    HIGH_LEVEL("High Level (12V ไฟเบรก)")
}

data class VotolParameters(
    // Controller Profile
    val model: ControllerModel = ControllerModel.EM_150_2,
    val ratedVoltage: Int = 72, // 48, 60, 72, 84, 96
    val firmwareVersion: String = "V03.02.18-AEKBOY",

    // Page 1: Basic Settings
    val underVoltageProtection: Float = 61.5f,
    val overVoltageProtection: Float = 89.0f,
    val softStartLevel: Int = 4, // 0 - 15
    val throttleStartVoltage: Float = 1.15f,
    val throttleEndVoltage: Float = 4.10f,
    val reverseSpeedPercent: Int = 30, // %
    val speedSensorType: SpeedSensorType = SpeedSensorType.HALL,
    val motorPolePairs: Int = 5, // e.g. 10 magnet poles

    // Page 2: Current & 3-Speed Gears
    val busCurrentLimitA: Int = 150,
    val phaseCurrentLimitA: Int = 420,
    val gear1SpeedPercent: Int = 55,
    val gear1CurrentPercent: Int = 60,
    val gear2SpeedPercent: Int = 85,
    val gear2CurrentPercent: Int = 85,
    val gear3SpeedPercent: Int = 100,
    val gear3CurrentPercent: Int = 100,
    val sportModeCurrentBoostA: Int = 175,
    val sportBoostDurationSec: Int = 15,

    // Page 3: Flux Weakening (เพิ่มรอบมอเตอร์)
    val fluxWeakeningEnabled: Boolean = true,
    val fluxWeakeningCurrentA: Int = 45, // 0 - 80A
    val fluxStartRpm: Int = 3200,
    val fluxAdvanceAngleDeg: Int = 24,

    // Page 4: EBS / Regen Braking
    val ebsEnabled: Boolean = true,
    val ebsLowSpeedLevel: Int = 2, // 1 - 5
    val ebsHighSpeedLevel: Int = 4, // 1 - 5
    val ebsMaxCurrentA: Int = 35,
    val brakeSignalType: BrakeSignalType = BrakeSignalType.LOW_LEVEL
)

data class LiveVotolTelemetry(
    val speedKmh: Float = 0f,
    val motorRpm: Int = 0,
    val batteryVoltage: Float = 75.6f,
    val throttleVoltage: Float = 1.15f,
    val throttlePercent: Int = 0,
    val busCurrentA: Float = 0f,
    val phaseCurrentA: Float = 0f,
    val controllerTempC: Float = 36.5f,
    val motorTempC: Float = 42.0f,
    val currentGear: String = "P",
    val fluxActive: Boolean = false,
    val ebsActive: Boolean = false,
    val errorCode: String = "00 (ปกติ)",
    val errorDescription: String = "ระบบทำงานปกติ ไม่พบโค้ดข้อผิดพลาด"
)

data class CommPacketLog(
    val id: Long = System.currentTimeMillis(),
    val isTx: Boolean,
    val hexData: String,
    val timestamp: String
)
