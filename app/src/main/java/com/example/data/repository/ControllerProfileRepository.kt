package com.example.data.repository

import com.example.data.db.ControllerProfileDao
import com.example.data.db.ControllerProfileEntity
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class ControllerProfileRepository(
    private val dao: ControllerProfileDao
) {

    val allProfiles: Flow<List<ControllerProfileEntity>> = dao.getAllProfiles()

    val favoriteProfiles: Flow<List<ControllerProfileEntity>> = dao.getFavoriteProfiles()

    fun getProfilesByCategory(category: String): Flow<List<ControllerProfileEntity>> {
        return dao.getProfilesByCategory(category)
    }

    fun getProfileById(id: Long): Flow<ControllerProfileEntity?> {
        return dao.getProfileById(id)
    }

    suspend fun getProfileByIdSync(id: Long): ControllerProfileEntity? {
        return dao.getProfileByIdSync(id)
    }

    suspend fun saveProfile(profile: ControllerProfileEntity): Long {
        return dao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: ControllerProfileEntity) {
        dao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: ControllerProfileEntity) {
        dao.deleteProfile(profile)
    }

    suspend fun deleteProfileById(id: Long): Boolean {
        return dao.deleteProfileById(id) > 0
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavoriteStatus(id, isFavorite)
    }

    /**
     * Seeds initial factory tuning profiles if database is empty.
     * Features DK72420S, DK72240, DK72180, and VOTOL EM-150/2.
     */
    suspend fun seedDefaultsIfEmpty() {
        if (dao.getProfileCount() > 0) return

        val defaultProfiles = listOf(
            ControllerProfileEntity(
                profileName = "DK72420S Drag Racing 145km/h",
                controllerCategory = "DKYS",
                modelName = DkysControllerModel.DK72420S.name,
                systemVoltage = 72,
                maxBusCurrentA = 150,
                maxPhaseCurrentA = 420,
                topSpeedLimitKmh = 145,
                lowVoltageCutoff = 62.0f,
                fluxWeakeningEnabled = true,
                ebsRegenEnabled = true,
                notes = "สูตรแข่งขันทางตรง ปลดรอบ Flux Weakening 65A ออกตัวดุดัน ตอบสนองคันเร่งแบบ Sport",
                configPayloadJson = buildDkysJson(
                    polePairs = 5,
                    maxBus = 150,
                    maxPhase = 420,
                    fluxA = 65,
                    throttle = "SPORT",
                    speedLimit = 145,
                    ebsLevel = 50,
                    softStart = 5
                ),
                isFavorite = true
            ),
            ControllerProfileEntity(
                profileName = "DK72240 / DK728240 สูตรฟรี aekboy2010",
                controllerCategory = "DKYS",
                modelName = DkysControllerModel.DK72240.name,
                systemVoltage = 72,
                maxBusCurrentA = 100,
                maxPhaseCurrentA = 240,
                topSpeedLimitKmh = 110,
                lowVoltageCutoff = 61.5f,
                fluxWeakeningEnabled = true,
                ebsRegenEnabled = true,
                notes = "แจกฟรี! สเต็ปขี่เที่ยว ทอร์คแน่น ไม่กระชาก ประหยัดแบตเตอรี่ คันเร่ง Linear ลื่นไหล",
                configPayloadJson = buildDkysJson(
                    polePairs = 5,
                    maxBus = 100,
                    maxPhase = 240,
                    fluxA = 35,
                    throttle = "LINEAR",
                    speedLimit = 110,
                    ebsLevel = 40,
                    softStart = 3
                ),
                isFavorite = true
            ),
            ControllerProfileEntity(
                profileName = "DK72180 City Eco Commuter",
                controllerCategory = "DKYS",
                modelName = DkysControllerModel.DK72180.name,
                systemVoltage = 72,
                maxBusCurrentA = 75,
                maxPhaseCurrentA = 180,
                topSpeedLimitKmh = 85,
                lowVoltageCutoff = 63.0f,
                fluxWeakeningEnabled = false,
                ebsRegenEnabled = true,
                notes = "สำหรับใช้งานในเมืองทุกวัน ออกตัวนุ่มนวล ถนอมแบตเตอรี่และโซ่ Regen สูง ดึงไฟกลับ 60%",
                configPayloadJson = buildDkysJson(
                    polePairs = 5,
                    maxBus = 75,
                    maxPhase = 180,
                    fluxA = 0,
                    throttle = "ECO",
                    speedLimit = 85,
                    ebsLevel = 60,
                    softStart = 2
                ),
                isFavorite = false
            ),
            ControllerProfileEntity(
                profileName = "DK72550T Super FOC Pro-Track",
                controllerCategory = "DKYS",
                modelName = DkysControllerModel.DK72550T.name,
                systemVoltage = 72,
                maxBusCurrentA = 200,
                maxPhaseCurrentA = 550,
                topSpeedLimitKmh = 165,
                lowVoltageCutoff = 60.0f,
                fluxWeakeningEnabled = true,
                ebsRegenEnabled = true,
                notes = "สูตรลงสนามระดับโปร ทอร์คจัดเต็ม 550A ปลดรอบลึก พร้อมระบบ Traction Control แบบไว",
                configPayloadJson = buildDkysJson(
                    polePairs = 5,
                    maxBus = 200,
                    maxPhase = 550,
                    fluxA = 90,
                    throttle = "SPORT",
                    speedLimit = 165,
                    ebsLevel = 55,
                    softStart = 5
                ),
                isFavorite = false
            ),
            ControllerProfileEntity(
                profileName = "VOTOL EM-150/2 Stage 2 Racing",
                controllerCategory = "VOTOL",
                modelName = ControllerModel.EM_150_2.name,
                systemVoltage = 72,
                maxBusCurrentA = 150,
                maxPhaseCurrentA = 450,
                topSpeedLimitKmh = 138,
                lowVoltageCutoff = 61.5f,
                fluxWeakeningEnabled = true,
                ebsRegenEnabled = true,
                notes = "สูตร VOTOL ยอดนิยม ปลดรอบ 60A Soft Start ระดับ 5 เกียร์ 3 เต็มสปีด 100%",
                configPayloadJson = buildVotolJson(
                    busCurrent = 150,
                    phaseCurrent = 450,
                    fluxA = 60,
                    softStart = 5
                ),
                isFavorite = true
            )
        )

        dao.insertProfiles(defaultProfiles)
    }

    companion object {
        fun buildDkysJson(
            polePairs: Int,
            maxBus: Int,
            maxPhase: Int,
            fluxA: Int,
            throttle: String,
            speedLimit: Int,
            ebsLevel: Int,
            softStart: Int
        ): String {
            val json = JSONObject()
            json.put("polePairs", polePairs)
            json.put("maxBusCurrentA", maxBus)
            json.put("maxPhaseCurrentA", maxPhase)
            json.put("fluxWeakeningCurrentA", fluxA)
            json.put("throttleCurve", throttle)
            json.put("maxSpeedLimitKmh", speedLimit)
            json.put("ebsRegenLevelPercent", ebsLevel)
            json.put("softStartLevel", softStart)
            return json.toString()
        }

        fun buildVotolJson(
            busCurrent: Int,
            phaseCurrent: Int,
            fluxA: Int,
            softStart: Int
        ): String {
            val json = JSONObject()
            json.put("busCurrentLimitA", busCurrent)
            json.put("phaseCurrentLimitA", phaseCurrent)
            json.put("fluxWeakeningCurrentA", fluxA)
            json.put("softStartLevel", softStart)
            return json.toString()
        }
    }
}
