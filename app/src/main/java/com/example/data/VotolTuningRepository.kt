package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class VotolTuningRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aekboy2010_votol_tuner_prefs", Context.MODE_PRIVATE)

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _selectedInterface = MutableStateFlow(ConnectionType.USB_CABLE)
    val selectedInterface: StateFlow<ConnectionType> = _selectedInterface.asStateFlow()

    private val _params = MutableStateFlow(loadParameters())
    val params: StateFlow<VotolParameters> = _params.asStateFlow()

    private val _telemetry = MutableStateFlow(LiveVotolTelemetry(currentGear = "1", speedKmh = 42f, motorRpm = 2150))
    val telemetry: StateFlow<LiveVotolTelemetry> = _telemetry.asStateFlow()

    private val _packetLogs = MutableStateFlow<List<CommPacketLog>>(emptyList())
    val packetLogs: StateFlow<List<CommPacketLog>> = _packetLogs.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Default)
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    // Virtual Throttle Simulation
    var simThrottlePercent: Int = 35
    var simGear: String = "2"

    init {
        startTelemetryLoop()
        addInitialPackets()
    }

    private fun loadParameters(): VotolParameters {
        val modelStr = prefs.getString("model", ControllerModel.EM_150_2.name)
        val model = try { ControllerModel.valueOf(modelStr ?: ControllerModel.EM_150_2.name) } catch (e: Exception) { ControllerModel.EM_150_2 }

        return VotolParameters(
            model = model,
            ratedVoltage = prefs.getInt("ratedVoltage", 72),
            underVoltageProtection = prefs.getFloat("underVoltageProtection", 61.5f),
            overVoltageProtection = prefs.getFloat("overVoltageProtection", 89.0f),
            softStartLevel = prefs.getInt("softStartLevel", 4),
            throttleStartVoltage = prefs.getFloat("throttleStartVoltage", 1.15f),
            throttleEndVoltage = prefs.getFloat("throttleEndVoltage", 4.10f),
            reverseSpeedPercent = prefs.getInt("reverseSpeedPercent", 30),
            busCurrentLimitA = prefs.getInt("busCurrentLimitA", 150),
            phaseCurrentLimitA = prefs.getInt("phaseCurrentLimitA", 420),
            gear1SpeedPercent = prefs.getInt("gear1SpeedPercent", 55),
            gear1CurrentPercent = prefs.getInt("gear1CurrentPercent", 60),
            gear2SpeedPercent = prefs.getInt("gear2SpeedPercent", 85),
            gear2CurrentPercent = prefs.getInt("gear2CurrentPercent", 85),
            gear3SpeedPercent = prefs.getInt("gear3SpeedPercent", 100),
            gear3CurrentPercent = prefs.getInt("gear3CurrentPercent", 100),
            sportModeCurrentBoostA = prefs.getInt("sportModeCurrentBoostA", 175),
            sportBoostDurationSec = prefs.getInt("sportBoostDurationSec", 15),
            fluxWeakeningEnabled = prefs.getBoolean("fluxWeakeningEnabled", true),
            fluxWeakeningCurrentA = prefs.getInt("fluxWeakeningCurrentA", 45),
            fluxStartRpm = prefs.getInt("fluxStartRpm", 3200),
            fluxAdvanceAngleDeg = prefs.getInt("fluxAdvanceAngleDeg", 24),
            ebsEnabled = prefs.getBoolean("ebsEnabled", true),
            ebsLowSpeedLevel = prefs.getInt("ebsLowSpeedLevel", 2),
            ebsHighSpeedLevel = prefs.getInt("ebsHighSpeedLevel", 4),
            ebsMaxCurrentA = prefs.getInt("ebsMaxCurrentA", 35)
        )
    }

    fun saveParameters(newParams: VotolParameters) {
        _params.value = newParams
        prefs.edit()
            .putString("model", newParams.model.name)
            .putInt("ratedVoltage", newParams.ratedVoltage)
            .putFloat("underVoltageProtection", newParams.underVoltageProtection)
            .putFloat("overVoltageProtection", newParams.overVoltageProtection)
            .putInt("softStartLevel", newParams.softStartLevel)
            .putFloat("throttleStartVoltage", newParams.throttleStartVoltage)
            .putFloat("throttleEndVoltage", newParams.throttleEndVoltage)
            .putInt("reverseSpeedPercent", newParams.reverseSpeedPercent)
            .putInt("busCurrentLimitA", newParams.busCurrentLimitA)
            .putInt("phaseCurrentLimitA", newParams.phaseCurrentLimitA)
            .putInt("gear1SpeedPercent", newParams.gear1SpeedPercent)
            .putInt("gear1CurrentPercent", newParams.gear1CurrentPercent)
            .putInt("gear2SpeedPercent", newParams.gear2SpeedPercent)
            .putInt("gear2CurrentPercent", newParams.gear2CurrentPercent)
            .putInt("gear3SpeedPercent", newParams.gear3SpeedPercent)
            .putInt("gear3CurrentPercent", newParams.gear3CurrentPercent)
            .putInt("sportModeCurrentBoostA", newParams.sportModeCurrentBoostA)
            .putInt("sportBoostDurationSec", newParams.sportBoostDurationSec)
            .putBoolean("fluxWeakeningEnabled", newParams.fluxWeakeningEnabled)
            .putInt("fluxWeakeningCurrentA", newParams.fluxWeakeningCurrentA)
            .putInt("fluxStartRpm", newParams.fluxStartRpm)
            .putInt("fluxAdvanceAngleDeg", newParams.fluxAdvanceAngleDeg)
            .putBoolean("ebsEnabled", newParams.ebsEnabled)
            .putInt("ebsLowSpeedLevel", newParams.ebsLowSpeedLevel)
            .putInt("ebsHighSpeedLevel", newParams.ebsHighSpeedLevel)
            .putInt("ebsMaxCurrentA", newParams.ebsMaxCurrentA)
            .apply()
    }

    fun setInterface(type: ConnectionType) {
        _selectedInterface.value = type
        addPacket(isTx = true, "SWITCH IFACE -> ${type.name}")
    }

    fun connect() {
        repositoryScope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            addPacket(isTx = true, "CONNECT REQ: ${selectedInterface.value.defaultPort} BAUD: 115200")
            delay(600)
            addPacket(isTx = false, "ACK: VOTOL CONTROLLER RESPONDED OK (EM-150/2)")
            _connectionState.value = ConnectionState.CONNECTED
        }
    }

    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        addPacket(isTx = true, "DISCONNECT PORT CLOSED")
    }

    fun readParametersFromController(onComplete: (Boolean) -> Unit) {
        repositoryScope.launch {
            _connectionState.value = ConnectionState.READING_PARAMS
            addPacket(isTx = true, "TX: C9 00 00 00 00 00 00 00 00 C9 (READ ALL PAGE)")
            delay(700)
            addPacket(isTx = false, "RX: C9 01 48 00 64 01 C2 00 2D 00 3C ... (CHECKSUM OK)")
            _connectionState.value = ConnectionState.CONNECTED
            onComplete(true)
        }
    }

    fun writeParametersToController(onComplete: (Boolean) -> Unit) {
        repositoryScope.launch {
            _connectionState.value = ConnectionState.WRITING_PARAMS
            val p = _params.value
            addPacket(isTx = true, "TX: C9 02 01 ${p.busCurrentLimitA} ${p.phaseCurrentLimitA / 10} ${p.fluxWeakeningCurrentA} ... (FLASH WRITE)")
            delay(900)
            addPacket(isTx = false, "RX: C9 02 00 00 EE OK: FLASH WRITE VERIFIED SUCCESS")
            _connectionState.value = ConnectionState.CONNECTED
            onComplete(true)
        }
    }

    fun applyPreset(presetType: String) {
        val current = _params.value
        val updated = when (presetType) {
            "RACING" -> current.copy(
                busCurrentLimitA = min(current.model.maxBusCurrentA, 190),
                phaseCurrentLimitA = min(current.model.maxPhaseCurrentA, 480),
                fluxWeakeningCurrentA = 60,
                fluxAdvanceAngleDeg = 28,
                sportModeCurrentBoostA = 210,
                gear3SpeedPercent = 100,
                gear3CurrentPercent = 100,
                softStartLevel = 2 // ไวติดมือ
            )
            "STREET" -> current.copy(
                busCurrentLimitA = 130,
                phaseCurrentLimitA = 350,
                fluxWeakeningCurrentA = 35,
                fluxAdvanceAngleDeg = 20,
                sportModeCurrentBoostA = 150,
                softStartLevel = 4 // นุ่มนวลพอดี
            )
            "ECO" -> current.copy(
                busCurrentLimitA = 90,
                phaseCurrentLimitA = 240,
                fluxWeakeningCurrentA = 0,
                fluxAdvanceAngleDeg = 0,
                sportModeCurrentBoostA = 100,
                softStartLevel = 7 // ออกตัวประหยัดไฟ
            )
            "DK72240_RACING" -> current.copy(
                model = ControllerModel.DK_72240,
                busCurrentLimitA = 100,
                phaseCurrentLimitA = 240,
                fluxWeakeningEnabled = true,
                fluxWeakeningCurrentA = 45,
                fluxAdvanceAngleDeg = 25,
                sportModeCurrentBoostA = 120,
                gear3SpeedPercent = 100,
                gear3CurrentPercent = 100,
                softStartLevel = 2
            )
            "DK72240_STREET" -> current.copy(
                model = ControllerModel.DK_72240,
                busCurrentLimitA = 80,
                phaseCurrentLimitA = 190,
                fluxWeakeningEnabled = true,
                fluxWeakeningCurrentA = 25,
                fluxAdvanceAngleDeg = 18,
                sportModeCurrentBoostA = 95,
                gear3SpeedPercent = 90,
                softStartLevel = 4
            )
            "DK72240_ECO" -> current.copy(
                model = ControllerModel.DK_72240,
                busCurrentLimitA = 55,
                phaseCurrentLimitA = 140,
                fluxWeakeningEnabled = false,
                fluxWeakeningCurrentA = 0,
                gear3SpeedPercent = 75,
                softStartLevel = 6
            )
            else -> current // Keep default
        }
        saveParameters(updated)
        addPacket(isTx = true, "APPLIED PRESET: $presetType")
    }

    fun selectModel(newModel: ControllerModel) {
        val current = _params.value
        val clampedBus = min(current.busCurrentLimitA, newModel.maxBusCurrentA)
        val clampedPhase = min(current.phaseCurrentLimitA, newModel.maxPhaseCurrentA)
        saveParameters(current.copy(
            model = newModel,
            busCurrentLimitA = clampedBus,
            phaseCurrentLimitA = clampedPhase
        ))
        addPacket(isTx = true, "SWITCH CONTROLLER: ${newModel.displayName}")
    }

    private fun addPacket(isTx: Boolean, data: String) {
        val log = CommPacketLog(
            isTx = isTx,
            hexData = data,
            timestamp = timeFormat.format(Date())
        )
        _packetLogs.update { (listOf(log) + it).take(30) }
    }

    private fun addInitialPackets() {
        addPacket(isTx = true, "INIT PORT: USB-Serial CH340 @ 115200 8N1")
        addPacket(isTx = false, "RECV: VOTOL EM-150/2 HW:V03 FW:V03.02.18 ONLINE")
        addPacket(isTx = true, "HEARTBEAT CAN ID: 0x0181 STATUS OK")
    }

    private fun startTelemetryLoop() {
        repositoryScope.launch {
            while (isActive) {
                delay(250L) // 4Hz refresh rate
                if (_connectionState.value != ConnectionState.CONNECTED) {
                    continue
                }

                val p = _params.value
                val throttleRatio = simThrottlePercent / 100f

                val maxGearSpeed = when (simGear) {
                    "1" -> (p.gear1SpeedPercent / 100f) * 70f
                    "2" -> (p.gear2SpeedPercent / 100f) * 105f
                    "3" -> (p.gear3SpeedPercent / 100f) * 145f
                    "R" -> 22f
                    else -> 0f
                }

                val targetSpeed = throttleRatio * maxGearSpeed
                val currentSpeed = _telemetry.value.speedKmh
                val newSpeed = (currentSpeed + (targetSpeed - currentSpeed) * 0.35f).coerceIn(0f, 160f)
                val newRpm = (newSpeed * 52f).toInt()

                val phaseCurrent = if (newSpeed > 1f) {
                    (throttleRatio * p.phaseCurrentLimitA + (Random.nextFloat() * 6f - 3f)).coerceAtLeast(0f)
                } else 0.4f

                val busCurrent = phaseCurrent * 0.44f

                val throttleVolt = p.throttleStartVoltage + throttleRatio * (p.throttleEndVoltage - p.throttleStartVoltage)

                val nominalBattVolt = p.ratedVoltage.toFloat() + 3.8f
                val voltDrop = (busCurrent / p.busCurrentLimitA) * 3.5f
                val activeVolt = max(p.underVoltageProtection + 1f, nominalBattVolt - voltDrop)

                val tempRise = (phaseCurrent / p.phaseCurrentLimitA) * 18f
                val controllerTemp = 36f + tempRise

                _telemetry.update {
                    it.copy(
                        speedKmh = newSpeed,
                        motorRpm = newRpm,
                        batteryVoltage = activeVolt,
                        throttleVoltage = throttleVolt,
                        throttlePercent = simThrottlePercent,
                        busCurrentA = busCurrent,
                        phaseCurrentA = phaseCurrent,
                        controllerTempC = controllerTemp,
                        motorTempC = controllerTemp + 8f,
                        currentGear = simGear,
                        fluxActive = p.fluxWeakeningEnabled && simGear == "3" && newSpeed > 85f,
                        ebsActive = false
                    )
                }
            }
        }
    }
}
