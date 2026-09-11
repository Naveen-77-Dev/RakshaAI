package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AnalysisEventEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.ScamPatternEntity
import com.example.data.local.entity.TrustedContactEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity): Long

    @Query("UPDATE users SET language = :language WHERE id = :userId")
    suspend fun updateLanguage(userId: Long, language: String)

    @Query("UPDATE users SET protectionMode = :mode WHERE id = :userId")
    suspend fun updateProtectionMode(userId: Long, mode: String)
}

@Dao
interface TrustedContactDao {
    @Query("SELECT * FROM trusted_contacts WHERE consentStatus = 1 ORDER BY id DESC")
    fun getActiveContacts(): Flow<List<TrustedContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContactEntity): Long

    @Query("DELETE FROM trusted_contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)

    @Query("UPDATE trusted_contacts SET consentStatus = :consent WHERE id = :id")
    suspend fun updateConsent(id: Long, consent: Boolean)
}

@Dao
interface AnalysisEventDao {
    @Query("SELECT * FROM analysis_events WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<AnalysisEventEntity>>

    @Query("SELECT * FROM analysis_events WHERE deletedAt IS NULL AND riskScore >= 45 ORDER BY createdAt DESC")
    fun getHighRiskEvents(): Flow<List<AnalysisEventEntity>>

    @Query("SELECT * FROM analysis_events WHERE deletedAt IS NULL AND type = :type ORDER BY createdAt DESC")
    fun getEventsByType(type: String): Flow<List<AnalysisEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AnalysisEventEntity): Long

    @Query("UPDATE analysis_events SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM analysis_events")
    suspend fun clearAll()
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncident(id: Long)

    @Query("DELETE FROM incidents")
    suspend fun clearAll()
}

@Dao
interface ScamPatternDao {
    @Query("SELECT * FROM scam_patterns WHERE enabled = 1")
    suspend fun getEnabledPatterns(): List<ScamPatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patterns: List<ScamPatternEntity>)
}
