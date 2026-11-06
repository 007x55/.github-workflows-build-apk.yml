package com.example.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a saved EV Motor Controller tuning profile.
 * Supports DKYS models (DK72420S, DK72240, DK72180, DK72400S, DK72550T)
 * and VOTOL EM series controllers.
 */
@Entity(tableName = "controller_profiles")
data class ControllerProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "profile_name")
    val profileName: String,

    @ColumnInfo(name = "controller_category")
    val controllerCategory: String, // "DKYS" or "VOTOL"

    @ColumnInfo(name = "model_name")
    val modelName: String, // e.g. "DK72420S", "DK72240", "EM-150/2"

    @ColumnInfo(name = "system_voltage")
    val systemVoltage: Int, // e.g. 72

    @ColumnInfo(name = "max_bus_current_a")
    val maxBusCurrentA: Int, // Battery limit (A)

    @ColumnInfo(name = "max_phase_current_a")
    val maxPhaseCurrentA: Int, // Phase wire limit (A)

    @ColumnInfo(name = "top_speed_limit_kmh")
    val topSpeedLimitKmh: Int,

    @ColumnInfo(name = "low_voltage_cutoff")
    val lowVoltageCutoff: Float,

    @ColumnInfo(name = "flux_weakening_enabled")
    val fluxWeakeningEnabled: Boolean,

    @ColumnInfo(name = "ebs_regen_enabled")
    val ebsRegenEnabled: Boolean,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "config_payload_json")
    val configPayloadJson: String, // Full JSON containing detailed settings

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
