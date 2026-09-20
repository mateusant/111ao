package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AuditLogEntity
import com.example.data.model.CartographicSurveyEntity
import com.example.data.model.EvidenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationPoiEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OdkFormEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM ocorrencias ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM ocorrencias WHERE id = :id")
    fun getIncidentById(id: Long): Flow<IncidentEntity?>

    @Query("SELECT * FROM ocorrencias WHERE incidentNumber = :number LIMIT 1")
    fun getIncidentByNumber(number: String): Flow<IncidentEntity?>

    @Query("SELECT * FROM ocorrencias WHERE syncStatus = :status")
    fun getIncidentsBySyncStatus(status: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM ocorrencias WHERE syncStatus != 'SYNCHRONIZED'")
    suspend fun getPendingSyncIncidents(): List<IncidentEntity>

    @Query("SELECT COUNT(*) FROM ocorrencias")
    fun countTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocorrencias WHERE status = :status")
    fun countByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocorrencias WHERE syncStatus = 'PENDING'")
    fun countPendingSync(): Flow<Int>

    @Query("""
        SELECT * FROM ocorrencias 
        WHERE incidentNumber LIKE '%' || :query || '%' 
           OR incidentType LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR locationName LIKE '%' || :query || '%'
           OR municipio LIKE '%' || :query || '%'
           OR bairro LIKE '%' || :query || '%'
           OR agentName LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchIncidents(query: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM ocorrencias WHERE agentCode = :agentCode ORDER BY timestamp DESC")
    fun getIncidentsByAgent(agentCode: String): Flow<List<IncidentEntity>>

    @Query("SELECT COUNT(*) FROM ocorrencias WHERE agentCode = :agentCode")
    fun countByAgent(agentCode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocorrencias WHERE agentCode = :agentCode AND syncStatus = 'PENDING'")
    fun countPendingByAgent(agentCode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM ocorrencias WHERE agentCode = :agentCode AND date = :date")
    fun countTodayByAgent(agentCode: String, date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<IncidentEntity>)

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Delete
    suspend fun deleteIncident(incident: IncidentEntity)

    @Query("DELETE FROM ocorrencias WHERE incidentNumber IN (:numbers)")
    suspend fun deleteByNumbers(numbers: List<String>)

    @Query("DELETE FROM ocorrencias")
    suspend fun deleteAllIncidents()
}

@Dao
interface CartographicSurveyDao {
    @Query("SELECT * FROM levantamentos_cartograficos ORDER BY timestamp DESC")
    fun getAllSurveys(): Flow<List<CartographicSurveyEntity>>

    @Query("SELECT * FROM levantamentos_cartograficos WHERE syncStatus != 'SYNCHRONIZED'")
    suspend fun getPendingSurveys(): List<CartographicSurveyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: CartographicSurveyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveys(surveys: List<CartographicSurveyEntity>)

    @Update
    suspend fun updateSurvey(survey: CartographicSurveyEntity)

    @Query("DELETE FROM levantamentos_cartograficos WHERE surveyCode IN (:codes)")
    suspend fun deleteByCodes(codes: List<String>)

    @Query("DELETE FROM levantamentos_cartograficos")
    suspend fun deleteAllSurveys()
}

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidencias WHERE incidentNumber = :incidentNumber ORDER BY timestamp ASC")
    fun getEvidencesForIncident(incidentNumber: String): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidencias ORDER BY timestamp DESC")
    fun getAllEvidences(): Flow<List<EvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidences(evidences: List<EvidenceEntity>)
}

@Dao
interface OdkFormDao {
    @Query("SELECT * FROM formularios_odk ORDER BY formId ASC")
    fun getAllForms(): Flow<List<OdkFormEntity>>

    @Query("SELECT * FROM formularios_odk WHERE formId = :formId")
    fun getFormById(formId: String): Flow<OdkFormEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForms(forms: List<OdkFormEntity>)

    @Update
    suspend fun updateForm(form: OdkFormEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM auditoria ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface UserDao {
    @Query("SELECT * FROM utilizadores ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM utilizadores WHERE role = 'AGENTE_CAMPO' ORDER BY name ASC")
    fun getFieldAgents(): Flow<List<UserEntity>>

    @Query("SELECT * FROM utilizadores WHERE badgeNumber = :badge LIMIT 1")
    suspend fun getUserByBadge(badge: String): UserEntity?

    @Query("SELECT * FROM utilizadores WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM utilizadores WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getUserByFirebaseUid(uid: String): UserEntity?

    @Query("SELECT * FROM utilizadores WHERE badgeNumber = :badge AND pin = :pin LIMIT 1")
    suspend fun authenticate(badge: String, pin: String): UserEntity?

    @Query("DELETE FROM utilizadores WHERE badgeNumber IN (:badges)")
    suspend fun deleteDemoUsers(badges: List<String>)

    @Query("DELETE FROM utilizadores")
    suspend fun deleteAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notificacoes ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notificacoes WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notificacoes SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notificacoes SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notificacoes")
    suspend fun deleteAllNotifications()
}

@Dao
interface LocationPoiDao {
    @Query("SELECT * FROM locais ORDER BY name ASC")
    fun getAllPois(): Flow<List<LocationPoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPois(pois: List<LocationPoiEntity>)
}
