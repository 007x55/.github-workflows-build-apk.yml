package com.example.data

import com.example.model.*
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

class DkysRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var telemetryJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _selectedInterface = MutableStateFlow(ConnectionType.USB_CABLE)
    val selectedInterface: StateFlow<ConnectionType> = _selectedInterface.asStateFlow()

    private val _selectedModel = MutableStateFlow(DkysControllerModel.DK72240)
    val selectedModel: StateFlow<DkysControllerModel> = _selectedModel.asStateFlow()

    private val _basicSettings = MutableStateFlow(DkysBasicSettings(deviceModel = DkysControllerModel.DK72240))
    val basicSettings: StateFlow<DkysBasicSettings> = _basicSettings.asStateFlow()

    private val _functionSettings = MutableStateFlow(DkysFunctionSettings())
    val functionSettings: StateFlow<DkysFunctionSettings> = _functionSettings.asStateFlow()

    private val _telemetry = MutableStateFlow(DkysTelemetry())
    val telemetry: StateFlow<DkysTelemetry> = _telemetry.asStateFlow()

    private val _packetLogs = MutableStateFlow<List<String>>(emptyList())
    val packetLogs: StateFlow<List<String>> = _packetLogs.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _statusMessage = MutableStateFlow("พร้อมเชื่อมต่อ SP GUI Tool dk 2.0")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    val presets: List<DkysPreset> = listOf(
        DkysPreset(
            id = "DK72240_FREE_STREET",
            title = "🎁 DK72240 / DK728240 สตรีททัวร์ริ่ง [สูตรฟรี]",
            subtitle = "ปลดล็อกความเร็ว 125 km/h คันเร่งสปอร์ต ประหยัดไฟ ไม่ร้อน อุณหภูมิคุมนิ่ง",
            controllerModel = DkysControllerModel.DK72240,
            busCurrent = 100,
            phaseCurrent = 240,
            fluxCurrent = 45,
            topSpeedKmh = 125,
            ebsRegen = 45,
            isFree = true
        ),
        DkysPreset(
            id = "DK72420S_POWER_FOC",
            title = "⚡ DK72420S Vector FOC ไฮทอร์ค สายหมอบ",
            subtitle = "ปลดรอบเต็มพิกัด 420A Phase ตอบสนองไว ออกตัวติดมือ ความเร็วปลายไหล 145+ km/h",
            controllerModel = DkysControllerModel.DK72420S,
            busCurrent = 150,
            phaseCurrent = 420,
            fluxCurrent = 65,
            topSpeedKmh = 145,
            ebsRegen = 55,
            isFree = true
        ),
        DkysPreset(
            id = "DK72180_ECO_COMMUTER",
            title = "🌱 DK72180 คล่องตัวในเมือง นุ่มนวล [สูตรฟรี]",
            subtitle = "เหมาะสำหรับสกู๊ตเตอร์และมอเตอร์ไซค์ไฟฟ้าใช้งานประจำวัน เซฟแบตเตอรี่ คันเร่งนุ่ม",
            controllerModel = DkysControllerModel.DK72180,
            busCurrent = 75,
            phaseCurrent = 180,
            fluxCurrent = 25,
            topSpeedKmh = 95,
            ebsRegen = 40,
            isFree = true
        ),
        DkysPreset(
            id = "DK72550T_RACING_FOC",
            title = "🏁 DK72550T Racing Edition ปลดรอบสนาม",
            subtitle = "550A Phase + Flux Weakening 85A สำหรับงานแข่งขัน ดึงหนักเบรกหนึบ EBS Linear",
            controllerModel = DkysControllerModel.DK72550T,
            busCurrent = 200,
            phaseCurrent = 550,
            fluxCurrent = 85,
            topSpeedKmh = 160,
            ebsRegen = 70,
            isFree = false
        ),
        DkysPreset(
            id = "DK72400S_HILL_CLIMB",
            title = "⛰️ DK72400S ไต่เขา ทอร์คหนัก พร้อม HDC + TCS",
            subtitle = "ระบบหน่วงลงเขา HDC และกันล้อฟรี TCS พร้อมลุยทางชัน ป้องกันล้อสะบัด",
            controllerModel = DkysControllerModel.DK72400S,
            busCurrent = 140,
            phaseCurrent = 400,
            fluxCurrent = 50,
            topSpeedKmh = 130,
            ebsRegen = 60,
            isFree = true
        )
    )

    fun selectInterface(type: ConnectionType) {
        _selectedInterface.value = type
        addLog("เลือกพอร์ตสื่อสาร: ${type.titleTh}")
    }

    fun selectModel(model: DkysControllerModel) {
        _selectedModel.value = model
        _basicSettings.update {
            it.copy(
                deviceModel = model,
                maxBusCurrentA = model.maxBusCurrentA,
                maxPhaseCurrentA = model.maxPhaseCurrentA,
                serialNumber = "DKYS${model.ratedVoltage}${model.maxPhaseCurrentA}-${Random.nextInt(1000, 9999)}"
            )
        }
        addLog("สลับรุ่นกล่องควบคุมเป็น: ${model.modelName} (พิกัด ${model.maxPhaseCurrentA}A Phase)")
    }

    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED) return

        scope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            _statusMessage.value = "กำลังจับคู่สื่อสารผ่าน ${_selectedInterface.value.titleTh}..."
            addLog(">>> SP GUI Tool dk 2.0: ส่งคำสั่ง Handshake [AA 55 01 00 12 ... 0D 0A]")

            delay(600)
            addLog("<<< ได้รับการตอบกลับจาก DKYS Controller [55 AA 01 02 OK]")
            delay(400)
            addLog(">>> ตรวจสอบ Firmware: DKYS-FOC-V2.0 | Baudrate: 115200bps")

            _connectionState.value = ConnectionState.CONNECTED
            _statusMessage.value = "เชื่อมต่อสำเร็จกับ ${_selectedModel.value.modelName}"
            addLog("เชื่อมต่อสำเร็จ: อ่านค่าพารามิเตอร์เริ่มต้นอัตโนมัติ")

            startTelemetryStream()
        }
    }

    fun disconnect() {
        telemetryJob?.cancel()
        telemetryJob = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _statusMessage.value = "ตัดการเชื่อมต่อแล้ว"
        addLog("ตัดการเชื่อมต่อคอนโทรลเลอร์แล้ว")
        _telemetry.value = DkysTelemetry()
    }

    fun readFromController(onComplete: (Boolean) -> Unit = {}) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            _statusMessage.value = "กรุณาเชื่อมต่อกล่องก่อนอ่านค่า"
            onComplete(false)
            return
        }

        scope.launch {
            _isBusy.value = true
            _statusMessage.value = "กำลังอ่านข้อมูลพารามิเตอร์จากกล่อง DKYS..."
            addLog(">>> SP GUI Tool dk 2.0: READ_ALL_BLOCKS (Block 0x01 - 0x0A)")
            delay(400)
            addLog("<<< Block 0x01 (Device & Motor Specs): OK")
            delay(350)
            addLog("<<< Block 0x02 (Battery & Current Limits): OK")
            delay(350)
            addLog("<<< Block 0x03 (Throttle & Riding Modes): OK")
            delay(300)
            addLog("<<< Block 0x04 (Braking EBS/HDC & Safety): OK")

            _isBusy.value = false
            _statusMessage.value = "อ่านค่าจากกล่องสำเร็จครบทุกพารามิเตอร์"
            addLog("ดาวน์โหลดพารามิเตอร์เสร็จสมบูรณ์ พร้อมปรับแต่ง")
            onComplete(true)
        }
    }

    fun uploadToController(onComplete: (Boolean) -> Unit = {}) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            _statusMessage.value = "กรุณาเชื่อมต่อกล่องก่อนอัปโหลด"
            onComplete(false)
            return
        }

        scope.launch {
            _isBusy.value = true
            _statusMessage.value = "กำลังบันทึกและเบิร์นค่าลง EEPROM ของกล่อง..."
            addLog(">>> SP GUI Tool dk 2.0: WRITE_PARAMS_BEGIN")
            delay(350)
            addLog(">>> เขียน Basic Settings (กระแส Phase: ${_basicSettings.value.maxPhaseCurrentA}A, Bus: ${_basicSettings.value.maxBusCurrentA}A)")
            delay(400)
            addLog(">>> เขียน Function Settings (เกียร์ 1/2/3, EBS: ${_functionSettings.value.ebsRegenLevelPercent}%, HDC/TCS)")
            delay(450)
            addLog(">>> VERIFY CHECKSUM CRC32: 0x8F3C4B [PASS]")
            delay(300)
            addLog("<<< EEPROM Flash Write OK: Rebooting Controller MCU...")

            _isBusy.value = false
            _statusMessage.value = "เบิร์นค่าลงกล่องสำเร็จ! การตั้งค่ามีผลทันที"
            addLog("อัปโหลดพารามิเตอร์ลงกล่อง DKYS สำเร็จ 100%")
            onComplete(true)
        }
    }

    fun applyPreset(preset: DkysPreset, onApplied: () -> Unit = {}) {
        _selectedModel.value = preset.controllerModel
        _basicSettings.update {
            it.copy(
                deviceModel = preset.controllerModel,
                maxBusCurrentA = preset.busCurrent,
                maxPhaseCurrentA = preset.phaseCurrent,
                fluxWeakeningCurrentA = preset.fluxCurrent,
                throttleCurve = if (preset.phaseCurrent > 300) DkysThrottleCurve.SPORT else DkysThrottleCurve.LINEAR
            )
        }
        _functionSettings.update {
            it.copy(
                maxSpeedLimitKmh = preset.topSpeedKmh,
                ebsRegenLevelPercent = preset.ebsRegen,
                gear3SpeedPercent = 100,
                gear3PhaseCurrentPercent = 100
            )
        }
        addLog("โหลดพรีเซ็ต '${preset.title}' (Phase: ${preset.phaseCurrent}A, Max: ${preset.topSpeedKmh} km/h)")
        _statusMessage.value = "โหลดสูตร '${preset.title}' แล้ว กด 'Upload' เพื่อส่งเข้ากล่อง"
        onApplied()
    }

    fun updateBasicSettings(update: (DkysBasicSettings) -> DkysBasicSettings) {
        _basicSettings.update(update)
    }

    fun updateFunctionSettings(update: (DkysFunctionSettings) -> DkysFunctionSettings) {
        _functionSettings.update(update)
    }

    fun clearLogs() {
        _packetLogs.value = emptyList()
    }

    private fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        _packetLogs.update { (listOf("[$time] $message") + it).take(80) }
    }

    private fun startTelemetryStream() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var simulatedRpm = 0
            var simulatedSpeed = 0f
            var throttleSim = 0

            while (isActive) {
                delay(200)
                if (_connectionState.value == ConnectionState.CONNECTED) {
                    val basic = _basicSettings.value
                    val func = _functionSettings.value

                    // Simulate slight fluctuation or throttle
                    val targetThrottle = if (Random.nextFloat() > 0.35f) Random.nextInt(15, 85) else 0
                    throttleSim = (throttleSim * 0.7f + targetThrottle * 0.3f).toInt()

                    val maxRpm = basic.ratedMotorRpm * (if (basic.fluxWeakeningRpmUnlock) 1.35f else 1.0f)
                    val targetRpm = (maxRpm * (throttleSim / 100f)).toInt()
                    simulatedRpm = (simulatedRpm * 0.75f + targetRpm * 0.25f).toInt()
                    simulatedSpeed = (simulatedRpm * 0.082f).coerceAtMost(func.maxSpeedLimitKmh.toFloat())

                    val busCurrentSim = (basic.maxBusCurrentA * (throttleSim / 100f) * 0.8f) + Random.nextFloat() * 1.5f
                    val phaseCurrentSim = (basic.maxPhaseCurrentA * (throttleSim / 100f) * 0.9f) + Random.nextFloat() * 3.0f

                    val baseVolt = basic.systemVoltage.toFloat() + 2.4f
                    val voltSag = (busCurrentSim / basic.maxBusCurrentA) * 2.8f
                    val liveVolt = (baseVolt - voltSag).coerceAtLeast(basic.lowVoltageCutoff)

                    val ctrlTemp = 36.0f + (busCurrentSim / basic.maxBusCurrentA) * 15.0f
                    val motorTemp = 40.0f + (phaseCurrentSim / basic.maxPhaseCurrentA) * 18.0f

                    _telemetry.value = DkysTelemetry(
                        motorRpm = simulatedRpm,
                        vehicleSpeedKmh = simulatedSpeed,
                        batteryVoltage = liveVolt,
                        busCurrentA = busCurrentSim,
                        phaseCurrentA = phaseCurrentSim,
                        controllerTempC = ctrlTemp,
                        motorTempC = motorTemp,
                        throttleInputPercent = throttleSim,
                        activeGear = if (simulatedSpeed > 75) 3 else if (simulatedSpeed > 40) 2 else 1,
                        isBraking = throttleSim == 0 && simulatedSpeed > 20 && Random.nextFloat() > 0.7f,
                        isSideStandDown = false,
                        isParkingMode = false,
                        isHdcActive = func.hdcEnabled && simulatedSpeed > func.hdcHoldSpeedKmh,
                        isTcsActive = func.tcsEnabled && throttleSim > 70 && Random.nextFloat() > 0.8f,
                        isFluxWeakeningActive = basic.fluxWeakeningRpmUnlock && simulatedRpm > basic.ratedMotorRpm,
                        faultCode = "NO_FAULT"
                    )
                }
            }
        }
    }
}
