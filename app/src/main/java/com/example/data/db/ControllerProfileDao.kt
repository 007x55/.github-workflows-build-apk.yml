package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ControllerProfileDao {

    @Query("SELECT * FROM controller_profiles ORDER BY is_favorite DESC, updated_at DESC")
    fun getAllProfiles(): Flow<List<ControllerProfileEntity>>

    @Query("SELECT * FROM controller_profiles WHERE controller_category = :category ORDER BY is_favorite DESC, updated_at DESC")
    fun getProfilesByCategory(category: String): Flow<List<ControllerProfileEntity>>

    @Query("SELECT * FROM controller_profiles WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun getFavoriteProfiles(): Flow<List<ControllerProfileEntity>>

    @Query("SELECT * FROM controller_profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<ControllerProfileEntity?>

    @Query("SELECT * FROM controller_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdSync(id: Long): ControllerProfileEntity?

    @Query("SELECT COUNT(*) FROM controller_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ControllerProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ControllerProfileEntity>): List<Long>

    @Update
    suspend fun updateProfile(profile: ControllerProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ControllerProfileEntity)

    @Query("DELETE FROM controller_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long): Int

    @Query("UPDATE controller_profiles SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
}
