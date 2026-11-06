package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DkysRepository
import com.example.data.db.AppDatabase
import com.example.data.db.ControllerProfileEntity
import com.example.data.repository.ControllerProfileRepository
import com.example.model.*
import com.example.ui.VotolTuningViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class ControllerProfileViewModel(
    application: Application,
    private val repository: ControllerProfileRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        repository = ControllerProfileRepository(
            AppDatabase.getDatabase(application).controllerProfileDao()
        )
    )

    private val _selectedCategory = MutableStateFlow("ALL") // "ALL", "DKYS", "VOTOL"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private val _selectedProfile = MutableStateFlow<ControllerProfileEntity?>(null)
    val selectedProfile: StateFlow<ControllerProfileEntity?> = _selectedProfile.asStateFlow()

    // Combining raw allProfiles flow with category filter and search query
    val profiles: StateFlow<List<ControllerProfileEntity>> = combine(
        repository.allProfiles,
        _selectedCategory,
        _searchQuery
    ) { all, category, query ->
        all.filter { profile ->
            val matchesCategory = (category == "ALL" || profile.controllerCategory.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    profile.profileName.contains(query, ignoreCase = true) ||
                    profile.modelName.contains(query, ignoreCase = true) ||
                    profile.notes.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectProfile(profile: ControllerProfileEntity?) {
        _selectedProfile.value = profile
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    /**
     * Saves current DKYS / SP GUI Tool parameters into Room database.
     */
    fun saveDkysProfile(
        name: String,
        notes: String,
        model: DkysControllerModel,
        basic: DkysBasicSettings,
        func: DkysFunctionSettings,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("hardwareVersion", basic.hardwareVersion)
                put("softwareVersion", basic.softwareVersion)
                put("serialNumber", basic.serialNumber)
                put("systemVoltage", basic.systemVoltage)
                put("polePairs", basic.polePairs)
                put("sensorType", basic.sensorType.name)
                put("phaseAngle", basic.phaseAngle)
                put("motorDirectionForward", basic.motorDirectionForward)
                put("ratedMotorRpm", basic.ratedMotorRpm)
                put("fluxWeakeningRpmUnlock", basic.fluxWeakeningRpmUnlock)
                put("lowVoltageCutoff", basic.lowVoltageCutoff.toDouble())
                put("lowVoltageRecovery", basic.lowVoltageRecovery.toDouble())
                put("overVoltageProtection", basic.overVoltageProtection.toDouble())
                put("maxBusCurrentA", basic.maxBusCurrentA)
                put("batteryOverheatCutoffC", basic.batteryOverheatCutoffC)
                put("maxPhaseCurrentA", basic.maxPhaseCurrentA)
                put("continuousPhaseCurrentA", basic.continuousPhaseCurrentA)
                put("fluxWeakeningCurrentA", basic.fluxWeakeningCurrentA)
                put("boostCurrentDurationSec", basic.boostCurrentDurationSec)
                put("throttleMinVoltage", basic.throttleMinVoltage.toDouble())
                put("throttleMaxVoltage", basic.throttleMaxVoltage.toDouble())
                put("throttleDeadband", basic.throttleDeadband.toDouble())
                put("throttleCurve", basic.throttleCurve.name)
                put("throttleFaultProtection", basic.throttleFaultProtection)

                // Functions
                put("speedSwitchType", func.speedSwitchType.name)
                put("gear1SpeedPercent", func.gear1SpeedPercent)
                put("gear1PhaseCurrentPercent", func.gear1PhaseCurrentPercent)
                put("gear2SpeedPercent", func.gear2SpeedPercent)
                put("gear2PhaseCurrentPercent", func.gear2PhaseCurrentPercent)
                put("gear3SpeedPercent", func.gear3SpeedPercent)
                put("gear3PhaseCurrentPercent", func.gear3PhaseCurrentPercent)
                put("maxSpeedLimitKmh", func.maxSpeedLimitKmh)
                put("softStartLevel", func.softStartLevel)
                put("ebsEnabled", func.ebsEnabled)
                put("ebsRegenLevelPercent", func.ebsRegenLevelPercent)
                put("ebsLinearProportional", func.ebsLinearProportional)
                put("hdcEnabled", func.hdcEnabled)
                put("hdcHoldSpeedKmh", func.hdcHoldSpeedKmh)
                put("tcsEnabled", func.tcsEnabled)
                put("tcsSensitivity", func.tcsSensitivity)
                put("antiTheftMotorLock", func.antiTheftMotorLock)
                put("cruiseControlEnabled", func.cruiseControlEnabled)
                put("cruiseAutoHoldSec", func.cruiseAutoHoldSec)
                put("reverseEnabled", func.reverseEnabled)
                put("reverseSpeedPercent", func.reverseSpeedPercent)
                put("reverseWarningBeeper", func.reverseWarningBeeper)
                put("parkingModeEnabled", func.parkingModeEnabled)
                put("sideStandInterlock", func.sideStandInterlock.name)
                put("chargeProtectionCutoff", func.chargeProtectionCutoff)
            }

            val entity = ControllerProfileEntity(
                profileName = name.ifBlank { "${model.name} Preset ${System.currentTimeMillis() % 1000}" },
                controllerCategory = "DKYS",
                modelName = model.name,
                systemVoltage = basic.systemVoltage,
                maxBusCurrentA = basic.maxBusCurrentA,
                maxPhaseCurrentA = basic.maxPhaseCurrentA,
                topSpeedLimitKmh = func.maxSpeedLimitKmh,
                lowVoltageCutoff = basic.lowVoltageCutoff,
                fluxWeakeningEnabled = basic.fluxWeakeningRpmUnlock,
                ebsRegenEnabled = func.ebsEnabled,
                notes = notes,
                configPayloadJson = json.toString(),
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val id = repository.saveProfile(entity)
            _userFeedbackMessage.value = "บันทึกโปรไฟล์ ${entity.profileName} เรียบร้อย (ID: $id)"
            onSuccess(id)
        }
    }

    /**
     * Saves current VOTOL parameters into Room database.
     */
    fun saveVotolProfile(
        name: String,
        notes: String,
        params: VotolParameters,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("model", params.model.name)
                put("ratedVoltage", params.ratedVoltage)
                put("underVoltageProtection", params.underVoltageProtection.toDouble())
                put("overVoltageProtection", params.overVoltageProtection.toDouble())
                put("softStartLevel", params.softStartLevel)
                put("busCurrentLimitA", params.busCurrentLimitA)
                put("phaseCurrentLimitA", params.phaseCurrentLimitA)
                put("fluxWeakeningEnabled", params.fluxWeakeningEnabled)
                put("fluxWeakeningCurrentA", params.fluxWeakeningCurrentA)
                put("ebsEnabled", params.ebsEnabled)
                put("ebsMaxCurrentA", params.ebsMaxCurrentA)
                put("gear3SpeedPercent", params.gear3SpeedPercent)
            }

            val entity = ControllerProfileEntity(
                profileName = name.ifBlank { "${params.model.displayName} Formula" },
                controllerCategory = "VOTOL",
                modelName = params.model.name,
                systemVoltage = params.ratedVoltage,
                maxBusCurrentA = params.busCurrentLimitA,
                maxPhaseCurrentA = params.phaseCurrentLimitA,
                topSpeedLimitKmh = 120,
                lowVoltageCutoff = params.underVoltageProtection,
                fluxWeakeningEnabled = params.fluxWeakeningEnabled,
                ebsRegenEnabled = params.ebsEnabled,
                notes = notes,
                configPayloadJson = json.toString(),
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val id = repository.saveProfile(entity)
            _userFeedbackMessage.value = "บันทึกสูตร VOTOL: ${entity.profileName} แล้ว"
            onSuccess(id)
        }
    }

    /**
     * Loads a saved profile into the DKYS controller repository.
     */
    fun loadProfileIntoDkys(profile: ControllerProfileEntity, dkysRepo: DkysRepository) {
        viewModelScope.launch {
            try {
                // Find matching controller model
                val matchedModel = try {
                    DkysControllerModel.valueOf(profile.modelName)
                } catch (e: Exception) {
                    DkysControllerModel.DK72420S
                }
                dkysRepo.selectModel(matchedModel)

                val payload = if (profile.configPayloadJson.isNotBlank()) {
                    JSONObject(profile.configPayloadJson)
                } else null

                val currentBasic = dkysRepo.basicSettings.value
                val updatedBasic = currentBasic.copy(
                    deviceModel = matchedModel,
                    systemVoltage = profile.systemVoltage,
                    maxBusCurrentA = profile.maxBusCurrentA,
                    maxPhaseCurrentA = profile.maxPhaseCurrentA,
                    lowVoltageCutoff = profile.lowVoltageCutoff,
                    fluxWeakeningRpmUnlock = profile.fluxWeakeningEnabled,
                    polePairs = payload?.optInt("polePairs", currentBasic.polePairs) ?: currentBasic.polePairs,
                    sensorType = payload?.optString("sensorType")?.let {
                        try { DkysSensorType.valueOf(it) } catch (e: Exception) { currentBasic.sensorType }
                    } ?: currentBasic.sensorType,
                    throttleCurve = payload?.optString("throttleCurve")?.let {
                        try { DkysThrottleCurve.valueOf(it) } catch (e: Exception) { currentBasic.throttleCurve }
                    } ?: currentBasic.throttleCurve,
                    fluxWeakeningCurrentA = payload?.optInt("fluxWeakeningCurrentA", currentBasic.fluxWeakeningCurrentA) ?: currentBasic.fluxWeakeningCurrentA
                )

                val currentFunc = dkysRepo.functionSettings.value
                val updatedFunc = currentFunc.copy(
                    maxSpeedLimitKmh = profile.topSpeedLimitKmh,
                    ebsEnabled = profile.ebsRegenEnabled,
                    ebsRegenLevelPercent = payload?.optInt("ebsRegenLevelPercent", currentFunc.ebsRegenLevelPercent) ?: currentFunc.ebsRegenLevelPercent,
                    softStartLevel = payload?.optInt("softStartLevel", currentFunc.softStartLevel) ?: currentFunc.softStartLevel
                )

                dkysRepo.updateBasicSettings { updatedBasic }
                dkysRepo.updateFunctionSettings { updatedFunc }
                _userFeedbackMessage.value = "โหลดสูตร ${profile.profileName} ไปยังคอนโทรลเลอร์ ${matchedModel.modelName} แล้ว"
            } catch (e: Exception) {
                _userFeedbackMessage.value = "เกิดข้อผิดพลาดในการโหลดสูตร: ${e.message}"
            }
        }
    }

    /**
     * Loads a saved profile into the VOTOL tuning ViewModel.
     */
    fun loadProfileIntoVotol(profile: ControllerProfileEntity, votolViewModel: VotolTuningViewModel) {
        viewModelScope.launch {
            try {
                val matchedModel = try {
                    ControllerModel.valueOf(profile.modelName)
                } catch (e: Exception) {
                    ControllerModel.EM_150_2
                }

                val currentParams = votolViewModel.params.value
                val updated = currentParams.copy(
                    model = matchedModel,
                    ratedVoltage = profile.systemVoltage,
                    busCurrentLimitA = profile.maxBusCurrentA,
                    phaseCurrentLimitA = profile.maxPhaseCurrentA,
                    underVoltageProtection = profile.lowVoltageCutoff,
                    fluxWeakeningEnabled = profile.fluxWeakeningEnabled,
                    ebsEnabled = profile.ebsRegenEnabled
                )
                votolViewModel.updateParameters(updated)
                _userFeedbackMessage.value = "โหลดสูตร ${profile.profileName} ไปยังกล่อง VOTOL ${matchedModel.displayName} แล้ว"
            } catch (e: Exception) {
                _userFeedbackMessage.value = "โหลดสูตรไม่สำเร็จ: ${e.message}"
            }
        }
    }

    fun duplicateProfile(profile: ControllerProfileEntity) {
        viewModelScope.launch {
            val duplicate = profile.copy(
                id = 0,
                profileName = "${profile.profileName} (สำเนา)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveProfile(duplicate)
            _userFeedbackMessage.value = "คัดลอกโปรไฟล์ '${duplicate.profileName}' สำเร็จ"
        }
    }

    fun toggleFavorite(profile: ControllerProfileEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(profile.id, !profile.isFavorite)
        }
    }

    fun deleteProfile(profile: ControllerProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            _userFeedbackMessage.value = "ลบโปรไฟล์ '${profile.profileName}' แล้ว"
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ControllerProfileViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repo = ControllerProfileRepository(db.controllerProfileDao())
                return ControllerProfileViewModel(application, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
