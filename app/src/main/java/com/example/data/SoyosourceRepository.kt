package com.example.data

import com.example.model.ConnectionState
import com.example.model.ConnectionType
import com.example.model.SoyoOperatingMode
import com.example.model.SoyoPreset
import com.example.model.SoyoSettings
import com.example.model.SoyoTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SoyosourceRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var telemetryJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _selectedInterface = MutableStateFlow(ConnectionType.USB_CABLE)
    val selectedInterface: StateFlow<ConnectionType> = _selectedInterface.asStateFlow()

    private val _telemetry = MutableStateFlow(SoyoTelemetry())
    val telemetry: StateFlow<SoyoTelemetry> = _telemetry.asStateFlow()

    private val _settings = MutableStateFlow(SoyoSettings())
    val settings: StateFlow<SoyoSettings> = _settings.asStateFlow()

    private val _packetLogs = MutableStateFlow<List<String>>(emptyList())
    val packetLogs: StateFlow<List<String>> = _packetLogs.asStateFlow()

    val presets = listOf(
        SoyoPreset(
            id = "SOLAR_MAX_1200W",
            title = "☀️ โซลาร์เซลล์แดดจัด (PV MPPT 1200W)",
            subtitle = "ต่อแผงโซลาร์เซลล์โดยตรง MPPT ดึงกำลังไฟสูงสุดตามแสงแดด",
            targetWatts = 1200,
            cutoffVoltage = 42.0f,
            restartVoltage = 46.0f,
            mode = SoyoOperatingMode.PV_MPPT
        ),
        SoyoPreset(
            id = "BATTERY_BASE_350W",
            title = "🌙 โหมดประหยัดกลางคืน (Base Load 350W)",
            subtitle = "ดึงแบต 48V จ่ายไฟเลี้ยงตู้เย็น พัดลม หลอดไฟ คงที่ตลอดคืน",
            targetWatts = 350,
            cutoffVoltage = 45.0f,
            restartVoltage = 49.5f,
            mode = SoyoOperatingMode.BATTERY_CONSTANT
        ),
        SoyoPreset(
            id = "LIMITER_ZERO_EXPORT",
            title = "🛡️ โหมดกันย้อนอัจฉริยะ (Zero Export 900W)",
            subtitle = "จ่ายไฟเท่าที่บ้านเปิดใช้ ผ่าน RS485 CT Clamp ป้องกันมิเตอร์หมุนย้อน",
            targetWatts = 900,
            cutoffVoltage = 44.0f,
            restartVoltage = 49.0f,
            mode = SoyoOperatingMode.BATTERY_LIMITER
        ),
        SoyoPreset(
            id = "PEAK_SHAVING_1100W",
            title = "⚡ โหมดจ่ายหนักตัดพีค (Peak Shaving 1100W)",
            subtitle = "ตัดค่าไฟช่วงพีคกลางวัน แอร์ 2-3 ตัว พร้อมระบบคุมอุณหภูมิ",
            targetWatts = 1100,
            cutoffVoltage = 45.5f,
            restartVoltage = 50.0f,
            mode = SoyoOperatingMode.BATTERY_LIMITER
        )
    )

    init {
        logPacket("SYS: โมดูลสื่อสาร Soyosource GTI 1200W RS485 พร้อมใช้งาน (9600/4800 baud)")
    }

    fun selectInterface(type: ConnectionType) {
        _selectedInterface.value = type
        logPacket("SYS: สลับพอร์ตเชื่อมต่อเป็น ${type.titleTh}")
    }

    fun connect() {
        scope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            logPacket("TX [RS485 9600]: 55 01 00 00 00 56 (Request Status Handshake)")
            delay(500)
            logPacket("RX [RS485 9600]: A6 00 35 12 E4 03 52 01 FA 00 00 2F 00 1E C2 (Soyo GTI 1200W Online)")
            _connectionState.value = ConnectionState.CONNECTED
            startTelemetrySimulation()
        }
    }

    fun disconnect() {
        telemetryJob?.cancel()
        _connectionState.value = ConnectionState.DISCONNECTED
        logPacket("SYS: ตัดการเชื่อมต่อ Soyosource GTI 1200W")
    }

    private fun startTelemetrySimulation() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var simEnergyDaily = 4.85f
            while (isActive) {
                delay(1000)
                val currentSettings = _settings.value
                val basePower = when (currentSettings.operatingMode) {
                    SoyoOperatingMode.PV_MPPT -> (1050 + Random.nextInt(-60, 90)).coerceIn(100, 1200)
                    SoyoOperatingMode.BATTERY_CONSTANT -> currentSettings.targetPowerWatts
                    SoyoOperatingMode.BATTERY_LIMITER -> (currentSettings.targetPowerWatts + Random.nextInt(-40, 50)).coerceIn(50, 1200)
                }

                val simulatedDcV = 52.8f + (Random.nextFloat() * 0.6f - 0.3f)
                val simulatedDcA = basePower / simulatedDcV.coerceAtLeast(10f)
                val simulatedGridV = 227f + (Random.nextFloat() * 2.5f)
                val simulatedTemp = 45f + (basePower / 1200f) * 8f + Random.nextFloat()
                simEnergyDaily += (basePower / 1000f) / 3600f

                _telemetry.update {
                    it.copy(
                        dcVoltage = String.format(Locale.US, "%.1f", simulatedDcV).toFloat(),
                        dcCurrent = String.format(Locale.US, "%.1f", simulatedDcA).toFloat(),
                        acPower = basePower.toFloat(),
                        acVoltage = String.format(Locale.US, "%.1f", simulatedGridV).toFloat(),
                        acFrequency = 50.0f,
                        temperature = String.format(Locale.US, "%.1f", simulatedTemp).toFloat(),
                        dailyEnergyKwh = String.format(Locale.US, "%.2f", simEnergyDaily).toFloat(),
                        limiterDemandWatts = basePower + currentSettings.zeroExportMarginWatts,
                        isGridConnected = true,
                        isOverheat = simulatedTemp > 65.0f,
                        isLimiterActive = currentSettings.operatingMode == SoyoOperatingMode.BATTERY_LIMITER
                    )
                }

                // Periodic RS485 frame logs
                if (Random.nextInt(5) == 0) {
                    val pwrHex = String.format("%04X", basePower)
                    if (currentSettings.operatingMode == SoyoOperatingMode.BATTERY_LIMITER) {
                        logPacket("TX [RS485 4800 Limiter]: 24 56 00 21 ${pwrHex.take(2)} ${pwrHex.takeLast(2)} 80 CC (Set Demand ${basePower}W)")
                    } else {
                        logPacket("RX [RS485 9600 Status]: A6 DC:${String.format(Locale.US, "%.1f", simulatedDcV)}V AC:${basePower}W TEMP:${String.format(Locale.US, "%.0f", simulatedTemp)}°C")
                    }
                }
            }
        }
    }

    fun updateTargetPower(watts: Int) {
        val clamped = watts.coerceIn(50, 1200)
        _settings.update { it.copy(targetPowerWatts = clamped) }
        val pwrHex = String.format("%04X", clamped)
        logPacket("TX [RS485 Command]: 55 13 00 00 ${pwrHex.take(2)} ${pwrHex.takeLast(2)} (Set Inverter Power $clamped W)")
    }

    fun updateOperatingMode(mode: SoyoOperatingMode) {
        _settings.update { it.copy(operatingMode = mode) }
        val modeByte = when (mode) {
            SoyoOperatingMode.PV_MPPT -> "01"
            SoyoOperatingMode.BATTERY_CONSTANT -> "02"
            SoyoOperatingMode.BATTERY_LIMITER -> "03"
        }
        logPacket("TX [RS485]: 55 03 $modeByte 00 00 58 (Change Mode to ${mode.displayName})")
    }

    fun updateBatteryCutoff(cutoff: Float, restart: Float) {
        _settings.update { it.copy(batteryCutoffVoltage = cutoff, batteryRestartVoltage = restart) }
        logPacket("TX [RS485]: 55 0B ${String.format(Locale.US, "%.1f", cutoff)}V ${String.format(Locale.US, "%.1f", restart)}V (Save Battery Cutoff Protection)")
    }

    fun updateBaudRate(baud: Int) {
        _settings.update { it.copy(rs485BaudRate = baud) }
        logPacket("SYS: สลับความเร็ว RS485 Baud Rate เป็น $baud bps")
    }

    fun applyPreset(preset: SoyoPreset) {
        _settings.update {
            it.copy(
                targetPowerWatts = preset.targetWatts,
                batteryCutoffVoltage = preset.cutoffVoltage,
                batteryRestartVoltage = preset.restartVoltage,
                operatingMode = preset.mode
            )
        }
        logPacket("CFG: ใช้งานโปรไฟล์สำเร็จ '${preset.title}' (กำลังไฟ ${preset.targetWatts}W)")
    }

    fun sendZeroExportLimiterFrame(watts: Int) {
        val clamped = watts.coerceIn(0, 1200)
        val pwrHex = String.format("%04X", clamped)
        logPacket("TX [RS485 4800 Limiter]: 24 56 00 21 ${pwrHex.take(2)} ${pwrHex.takeLast(2)} 80 A2 (Direct Limiter Inject ${clamped}W)")
    }

    fun clearLogs() {
        _packetLogs.value = emptyList()
    }

    private fun logPacket(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _packetLogs.update { (listOf("[$time] $msg") + it).take(50) }
    }
}
