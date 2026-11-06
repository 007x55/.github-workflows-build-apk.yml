package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VotolTuningRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("aekboy2010 shop", appName)
    }

    @Test
    fun `verify votol tuning repository initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = VotolTuningRepository(context)
        assertNotNull(repo.params.value)
        assertNotNull(repo.telemetry.value)
        assertNotNull(repo.selectedInterface.value)
    }

    @Test
    fun `verify soyosource repository initialization and presets`() {
        val soyoRepo = com.example.data.SoyosourceRepository()
        assertNotNull(soyoRepo.telemetry.value)
        assertNotNull(soyoRepo.settings.value)
        assertEquals(4, soyoRepo.presets.size)
        assertEquals(1200, soyoRepo.presets[0].targetWatts)
    }

    @Test
    fun `verify dkys repository initialization and free preset`() {
        val dkysRepo = com.example.data.DkysRepository()
        assertNotNull(dkysRepo.basicSettings.value)
        assertNotNull(dkysRepo.functionSettings.value)
        assertNotNull(dkysRepo.telemetry.value)

        // Verify free presets include DK72240 / DK728240
        val freePreset = dkysRepo.presets.firstOrNull { it.id == "DK72240_FREE_STREET" }
        assertNotNull(freePreset)
        assertEquals(true, freePreset?.isFree)
        assertEquals(240, freePreset?.phaseCurrent)
        assertEquals(100, freePreset?.busCurrent)
        assertEquals(125, freePreset?.topSpeedKmh)

        // Verify applying preset
        freePreset?.let { dkysRepo.applyPreset(it) }
        assertEquals(240, dkysRepo.basicSettings.value.maxPhaseCurrentA)
        assertEquals(100, dkysRepo.basicSettings.value.maxBusCurrentA)
        assertEquals(125, dkysRepo.functionSettings.value.maxSpeedLimitKmh)
    }

    @Test
    fun `verify dkys controller models and features`() {
        val dkysRepo = com.example.data.DkysRepository()
        dkysRepo.selectModel(com.example.model.DkysControllerModel.DK72420S)
        assertEquals(420, dkysRepo.basicSettings.value.maxPhaseCurrentA)
        assertEquals(150, dkysRepo.basicSettings.value.maxBusCurrentA)

        // Verify Safety & Convenience features
        val func = dkysRepo.functionSettings.value
        assertEquals(true, func.ebsEnabled)
        assertEquals(true, func.hdcEnabled)
        assertEquals(true, func.tcsEnabled)
        assertEquals(true, func.antiTheftMotorLock)
        assertEquals(true, func.cruiseControlEnabled)
        assertEquals(true, func.parkingModeEnabled)
        assertEquals(true, func.chargeProtectionCutoff)
    }

    @Test
    fun `verify room database entity and dao for DK72420S`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.db.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = db.controllerProfileDao()
        val initialCount = dao.getProfileCount()
        assertEquals(0, initialCount)

        val dk72420sProfile = com.example.data.db.ControllerProfileEntity(
            profileName = "DK72420S Drag Racing 145km/h",
            controllerCategory = "DKYS",
            modelName = "DK72420S",
            systemVoltage = 72,
            maxBusCurrentA = 150,
            maxPhaseCurrentA = 420,
            topSpeedLimitKmh = 145,
            lowVoltageCutoff = 62.0f,
            fluxWeakeningEnabled = true,
            ebsRegenEnabled = true,
            notes = "สูตรจูนแข่งทางตรง aekboy2010 shop",
            configPayloadJson = "{}"
        )

        val newId = dao.insertProfile(dk72420sProfile)
        assertEquals(1, dao.getProfileCount())

        val retrieved = dao.getProfileByIdSync(newId)
        assertNotNull(retrieved)
        assertEquals("DK72420S Drag Racing 145km/h", retrieved?.profileName)
        assertEquals(420, retrieved?.maxPhaseCurrentA)
        assertEquals(150, retrieved?.maxBusCurrentA)

        // Test updating favorite status
        dao.updateFavoriteStatus(newId, true)
        val updated = dao.getProfileByIdSync(newId)
        assertEquals(true, updated?.isFavorite)

        // Test delete
        val rowsDeleted = dao.deleteProfileById(newId)
        assertEquals(1, rowsDeleted)
        assertEquals(0, dao.getProfileCount())

        db.close()
    }

    @Test
    fun `verify repository seed and ViewModel load to dkys`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.db.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = com.example.data.repository.ControllerProfileRepository(db.controllerProfileDao())
        repository.seedDefaultsIfEmpty()

        val count = db.controllerProfileDao().getProfileCount()
        assertEquals(true, count >= 4) // 4 factory profiles seeded

        val vm = com.example.viewmodel.ControllerProfileViewModel(context, repository)
        val dkysRepo = com.example.data.DkysRepository()

        // Load DK72420S profile into dkysRepo
        val profile = db.controllerProfileDao().getProfileByIdSync(1)
        assertNotNull(profile)
        if (profile != null) {
            vm.loadProfileIntoDkys(profile, dkysRepo)
            assertEquals(72, dkysRepo.basicSettings.value.systemVoltage)
        }

        db.close()
    }

    @Test
    fun `verify clone manager viewmodel initializes and lists installed apps`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.CloneManagerViewModel(app)

        val apps = vm.installedApps.value
        assertEquals(true, apps.isNotEmpty())

        // Verify initial clone exists
        assertEquals(true, vm.totalClonesCount.value >= 1)
        val dkysApp = apps.find { it.packageName == "com.dkys.spguitool" }
        assertNotNull(dkysApp)
        assertEquals(true, dkysApp?.hasClones)
    }

    @Test
    fun `verify clone manager creates and deletes clones`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.CloneManagerViewModel(app)

        val targetPackage = "com.siaecosys.votol"
        val initialCount = vm.totalClonesCount.value

        // Create new clone
        vm.createClone(targetPackage, "VOTOL Pro Track Test")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        val votolClones = vm.getClonesForPackage(targetPackage)
        val createdClone = votolClones.find { it.name == "VOTOL Pro Track Test" }
        assertNotNull(createdClone)

        val votolApp = vm.installedApps.value.find { it.packageName == targetPackage }
        assertNotNull(votolApp)
        assertNotNull(votolApp?.clones?.find { it.name == "VOTOL Pro Track Test" })

        // Delete the created clone
        if (createdClone != null) {
            vm.removeClone(targetPackage, createdClone.id)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            assertEquals(true, vm.getClonesForPackage(targetPackage).none { it.id == createdClone.id })
        }
    }

    @Test
    fun `verify clone manager search and filtering`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.CloneManagerViewModel(app)

        // Search for DKYS
        vm.setSearchQuery("DKYS")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val searchResults = vm.installedApps.value
        assertEquals(true, searchResults.isNotEmpty())
        assertEquals(true, searchResults.all { it.appName.contains("DKYS", ignoreCase = true) || it.packageName.contains("dkys", ignoreCase = true) })

        // Reset search
        vm.setSearchQuery("")
        vm.setFilter(com.example.model.CloneFilter.CLONED_ONLY)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val clonedOnly = vm.installedApps.value
        assertEquals(true, clonedOnly.isNotEmpty())
        assertEquals(true, clonedOnly.all { it.hasClones })
    }

    @Test
    fun `verify clone status indicator reflects active clone existence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.viewmodel.CloneManagerViewModel(app)

        val unClonedPackage = "com.fardriver.controller"
        val appBefore = vm.installedApps.value.find { it.packageName == unClonedPackage }
        assertNotNull(appBefore)
        assertEquals(false, appBefore?.hasClones)
        assertEquals(0, appBefore?.cloneCount)

        // Create a clone
        vm.createClone(unClonedPackage, "FarDriver Profile 2")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        val appAfter = vm.installedApps.value.find { it.packageName == unClonedPackage }
        assertNotNull(appAfter)
        assertEquals(true, appAfter?.hasClones)
        assertEquals(1, appAfter?.cloneCount)
    }
}

