package com.example.model

/**
 * Data models for SP GUI Tool dk 2.0 (DKYS Vector FOC / Sine Wave Motor Controllers)
 * Covers DK72420S, DK72180, DK72240, DK72400S, DK72550T.
 */

enum class DkysControllerModel(
    val modelName: String,
    val ratedVoltage: Int,
    val maxBusCurrentA: Int,
    val maxPhaseCurrentA: Int,
    val description: String,
    val isFreePromotion: Boolean = false
) {
    DK72180("DK72180 FOC", 72, 75, 180, "72V 180A Sine-Wave Controller สำหรับสกู๊ตเตอร์ขนาดกลาง", true),
    DK72240("DK72240 / DK728240 [ฟรี]", 72, 100, 240, "72V 240A ปลดล็อกฟังก์ชันเต็มระบบ แจกฟรีจาก aekboy2010 shop", true),
    DK72420S("DK72420S Vector FOC", 72, 150, 420, "72V 420A High-Power FOC Controller รุ่นยอดนิยม สายหมอบ/สายทอร์ค"),
    DK72400S("DK72400S High-Current", 72, 140, 400, "72V 400A Sine Wave ควบคุมแม่นยำ พร้อมระบบระบายความร้อนพิเศษ"),
    DK72550T("DK72550T Racing Edition", 72, 200, 550, "72V 550A ตัวท็อปสายแข่งสนาม รองรับ Flux Weakening หนัก")
}

enum class DkysSensorType(val displayName: String) {
    HALL("Hall Sensor (เซนเซอร์ฮอลล์ 120°/60°)"),
    SENSORLESS("Sensorless (ไร้เซนเซอร์ - วัดแรงดันย้อน BEMF)"),
    ENCODER("Magnetic Encoder (เอ็นโค้เดอร์ความละเอียดสูง)")
}

enum class DkysThrottleCurve(val displayName: String, val description: String) {
    LINEAR("เชิงเส้น (Linear)", "อัตราเร่งขึ้นตรงตามบิดมือคันเร่ง 1:1"),
    SPORT("สปอร์ต (Sport / Aggressive)", "ต้นจัด ติดมือ ตอบสนองไว เหมาะกับสายซิ่ง"),
    ECO("ประหยัด (Smooth / Eco)", "คันเร่งนุ่มนวล ออกตัวไม่กระชาก ถนอมสายพาน/โซ่"),
    EXPONENTIAL("เอ็กซ์โปเนนเชียล (Exponential)", "ต้นนิ่ม ปลายจัด บิดลึกถึงจะพุ่ง")
}

enum class DkysSpeedSwitchType(val displayName: String) {
    BUTTON_CYCLE("ปุ่มกดวน (Cycle Button 1-2-3-1)"),
    THREE_POSITION_SWITCH("สวิตช์ 3 แก๊ก (High / Med / Low)"),
    SOFT_SWITCH("สวิตช์สั่งงานดิจิทัล (Digital Soft Switch)")
}

enum class DkysSideStandInterlock(val displayName: String) {
    NORMALLY_OPEN("Normally Open (ต่อลงกราวด์เมื่อเอาขาตั้งลง)"),
    NORMALLY_CLOSED("Normally Closed (ตัดวงจรเมื่อเอาขาตั้งลง)"),
    DISABLED("ปิดใช้งาน (Disabled)")
}

data class DkysBasicSettings(
    // Device Info
    val deviceModel: DkysControllerModel = DkysControllerModel.DK72420S,
    val hardwareVersion: String = "DKYS-FOC-V2.0",
    val softwareVersion: String = "SP GUI Tool dk 2.0.4-PRO",
    val serialNumber: String = "DKYS72420-202409-8812",
    val systemVoltage: Int = 72, // 48, 60, 72, 84, 96 V

    // Motor Parameters
    val polePairs: Int = 5, // Pole pairs (e.g. 5 = 10 poles)
    val sensorType: DkysSensorType = DkysSensorType.HALL,
    val phaseAngle: Int = 120, // 60 or 120 degrees
    val motorDirectionForward: Boolean = true,
    val ratedMotorRpm: Int = 1200,
    val fluxWeakeningRpmUnlock: Boolean = true, // ปลดรอบมอเตอร์

    // Battery Limits
    val lowVoltageCutoff: Float = 62.0f, // LVC (V)
    val lowVoltageRecovery: Float = 64.0f, // (V)
    val overVoltageProtection: Float = 88.5f, // (V)
    val maxBusCurrentA: Int = 150, // กระแสแบตเตอรี่สูงสุด (A)
    val batteryOverheatCutoffC: Int = 65,

    // Current Limits
    val maxPhaseCurrentA: Int = 420, // กระแสเฟสสูงสุด (A)
    val continuousPhaseCurrentA: Int = 220, // กระแสเฟสต่อเนื่อง (A)
    val fluxWeakeningCurrentA: Int = 60, // กระแสปลดรอบ Id (A)
    val boostCurrentDurationSec: Int = 10,

    // Throttle Response
    val throttleMinVoltage: Float = 1.15f, // จุดเริ่มตอบสนอง (V)
    val throttleMaxVoltage: Float = 4.10f, // จุดคันเร่งสุด (V)
    val throttleDeadband: Float = 0.15f, // ค่าฟรีคันเร่ง (V)
    val throttleCurve: DkysThrottleCurve = DkysThrottleCurve.SPORT,
    val throttleFaultProtection: Boolean = true // ป้องกันคันเร่งค้าง/สายขาด
)

data class DkysFunctionSettings(
    // Riding & Speeds
    val speedSwitchType: DkysSpeedSwitchType = DkysSpeedSwitchType.THREE_POSITION_SWITCH,
    val gear1SpeedPercent: Int = 50,
    val gear1PhaseCurrentPercent: Int = 60,
    val gear2SpeedPercent: Int = 80,
    val gear2PhaseCurrentPercent: Int = 85,
    val gear3SpeedPercent: Int = 100,
    val gear3PhaseCurrentPercent: Int = 100,
    val maxSpeedLimitKmh: Int = 135,
    val softStartLevel: Int = 3, // 1 (นุ่ม) - 5 (กระชาก)

    // Braking & Safety
    val ebsEnabled: Boolean = true, // Electronic Braking System (Regen)
    val ebsRegenLevelPercent: Int = 45, // 0 - 100%
    val ebsLinearProportional: Boolean = true, // เบรกไฟฟ้าน้ำหนักแปรผันตามก้านเบรก
    val hdcEnabled: Boolean = true, // Hill Descent Control หน่วงลงเขา
    val hdcHoldSpeedKmh: Int = 25,
    val tcsEnabled: Boolean = true, // Traction Control System ป้องกันล้อฟรี
    val tcsSensitivity: Int = 2, // 1=Low, 2=Med, 3=High
    val antiTheftMotorLock: Boolean = true, // ล็อกล้อมอเตอร์กันขโมย

    // Convenience Features
    val cruiseControlEnabled: Boolean = true,
    val cruiseAutoHoldSec: Int = 8, // บิดค้างกี่วิถึงล็อกความเร็วอัตโนมัติ
    val reverseEnabled: Boolean = true,
    val reverseSpeedPercent: Int = 30, // %
    val reverseWarningBeeper: Boolean = true,
    val parkingModeEnabled: Boolean = true, // P-Gear สวิตช์เกียร์จอด
    val sideStandInterlock: DkysSideStandInterlock = DkysSideStandInterlock.NORMALLY_OPEN,
    val chargeProtectionCutoff: Boolean = true // ห้ามบิดตอนเสียบสายชาร์จ
)

data class DkysTelemetry(
    val motorRpm: Int = 0,
    val vehicleSpeedKmh: Float = 0f,
    val batteryVoltage: Float = 72.4f,
    val busCurrentA: Float = 0f,
    val phaseCurrentA: Float = 0f,
    val controllerTempC: Float = 38.5f,
    val motorTempC: Float = 42.0f,
    val throttleInputPercent: Int = 0,
    val activeGear: Int = 1,
    val isBraking: Boolean = false,
    val isSideStandDown: Boolean = false,
    val isParkingMode: Boolean = false,
    val isHdcActive: Boolean = false,
    val isTcsActive: Boolean = false,
    val isFluxWeakeningActive: Boolean = false,
    val faultCode: String = "NO_FAULT"
)

data class DkysPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val controllerModel: DkysControllerModel,
    val busCurrent: Int,
    val phaseCurrent: Int,
    val fluxCurrent: Int,
    val topSpeedKmh: Int,
    val ebsRegen: Int,
    val isFree: Boolean = true
)
