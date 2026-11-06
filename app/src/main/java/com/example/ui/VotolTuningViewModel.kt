package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VotolTuningRepository
import com.example.model.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VotolTuningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VotolTuningRepository(application.applicationContext)

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
    val selectedInterface: StateFlow<ConnectionType> = repository.selectedInterface
    val params: StateFlow<VotolParameters> = repository.params
    val telemetry: StateFlow<LiveVotolTelemetry> = repository.telemetry
    val packetLogs: StateFlow<List<CommPacketLog>> = repository.packetLogs

    fun setInterface(type: ConnectionType) {
        repository.setInterface(type)
    }

    fun connect() {
        repository.connect()
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun readParameters(onComplete: (Boolean) -> Unit) {
        repository.readParametersFromController(onComplete)
    }

    fun writeParameters(onComplete: (Boolean) -> Unit) {
        repository.writeParametersToController(onComplete)
    }

    fun updateParameters(newParams: VotolParameters) {
        viewModelScope.launch {
            repository.saveParameters(newParams)
        }
    }

    fun selectModel(model: ControllerModel) {
        viewModelScope.launch {
            repository.selectModel(model)
        }
    }

    fun applyPreset(presetType: String) {
        viewModelScope.launch {
            repository.applyPreset(presetType)
        }
    }

    fun setSimThrottle(percent: Int) {
        repository.simThrottlePercent = percent
    }

    fun setSimGear(gear: String) {
        repository.simGear = gear
    }
}
