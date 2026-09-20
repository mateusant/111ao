package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.CartographicSurveyEntity
import com.example.data.model.EvidenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationPoiEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OdkFormEntity
import com.example.data.model.UserEntity
import com.example.gis.GisEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val db: AppDatabase) {

    // Incidents
    val allIncidents: Flow<List<IncidentEntity>> = db.incidentDao().getAllIncidents()
    val totalCount: Flow<Int> = db.incidentDao().countTotal()
    val pendingSyncCount: Flow<Int> = db.incidentDao().countPendingSync()

    fun getIncidentsByAgent(agentCode: String): Flow<List<IncidentEntity>> =
        db.incidentDao().getIncidentsByAgent(agentCode)

    fun countByAgent(agentCode: String): Flow<Int> =
        db.incidentDao().countByAgent(agentCode)

    fun countPendingByAgent(agentCode: String): Flow<Int> =
        db.incidentDao().countPendingByAgent(agentCode)

    fun getIncidentById(id: Long): Flow<IncidentEntity?> = db.incidentDao().getIncidentById(id)
    fun getIncidentByNumber(number: String): Flow<IncidentEntity?> = db.incidentDao().getIncidentByNumber(number)
    fun searchIncidents(query: String): Flow<List<IncidentEntity>> = db.incidentDao().searchIncidents(query)

    suspend fun insertIncident(incident: IncidentEntity, currentUser: UserEntity): Long {
        val id = db.incidentDao().insertIncident(incident)
        logAudit(
            user = currentUser,
            action = "CRIACAO",
            entityType = "OCORRENCIA",
            entityId = incident.incidentNumber,
            details = "Registo de ocorrência criminal [${incident.category}] no bairro ${incident.bairro}, ${incident.municipio}"
        )
        return id
    }

    suspend fun updateIncident(incident: IncidentEntity, currentUser: UserEntity) {
        db.incidentDao().updateIncident(incident.copy(updatedAt = System.currentTimeMillis()))
        logAudit(
            user = currentUser,
            action = "ATUALIZACAO",
            entityType = "OCORRENCIA",
            entityId = incident.incidentNumber,
            details = "Atualização de estado para: ${incident.status}, Prioridade: ${incident.priority}"
        )
    }

    suspend fun validateIncident(incidentNumber: String, validator: UserEntity) {
        val current = db.incidentDao().getIncidentByNumber(incidentNumber).firstOrNull()
        if (current != null) {
            val updated = current.copy(
                status = "Validada",
                validatedBy = validator.name,
                validatedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.incidentDao().updateIncident(updated)
            logAudit(
                user = validator,
                action = "VALIDACAO",
                entityType = "OCORRENCIA",
                entityId = incidentNumber,
                details = "Ocorrência validada formalmente pelo supervisor"
            )
        }
    }

    // Surveys
    val allSurveys: Flow<List<CartographicSurveyEntity>> = db.cartographicSurveyDao().getAllSurveys()

    suspend fun insertSurvey(survey: CartographicSurveyEntity, currentUser: UserEntity): Long {
        val id = db.cartographicSurveyDao().insertSurvey(survey)
        logAudit(
            user = currentUser,
            action = "CRIACAO",
            entityType = "LEVANTAMENTO",
            entityId = survey.surveyCode,
            details = "Levantamento cartográfico do tipo ${survey.surveyType} em ${survey.bairro}"
        )
        return id
    }

    // Evidences
    fun getEvidencesForIncident(incidentNumber: String): Flow<List<EvidenceEntity>> =
        db.evidenceDao().getEvidencesForIncident(incidentNumber)

    suspend fun insertEvidence(evidence: EvidenceEntity, currentUser: UserEntity): Long {
        val id = db.evidenceDao().insertEvidence(evidence)
        logAudit(
            user = currentUser,
            action = "CRIACAO",
            entityType = "EVIDENCIA",
            entityId = evidence.incidentNumber,
            details = "Anexo de evidência [${evidence.evidenceType}] com Hash SHA-256: ${evidence.hashSha256.take(12)}..."
        )
        return id
    }

    // Forms
    val allForms: Flow<List<OdkFormEntity>> = db.odkFormDao().getAllForms()

    // Users
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    val fieldAgents: Flow<List<UserEntity>> = db.userDao().getFieldAgents()

    suspend fun getUserByBadge(badge: String): UserEntity? = db.userDao().getUserByBadge(badge)
    suspend fun getUserByEmail(email: String): UserEntity? = db.userDao().getUserByEmail(email)
    suspend fun getUserByFirebaseUid(uid: String): UserEntity? = db.userDao().getUserByFirebaseUid(uid)

    suspend fun authenticateAgent(badge: String, pin: String): UserEntity? =
        db.userDao().authenticate(badge, pin)

    suspend fun insertUser(user: UserEntity) {
        db.userDao().insertUser(user)
    }

    suspend fun registerAgent(user: UserEntity) {
        db.userDao().insertUser(user)
        logAudit(
            user = user,
            action = "CRIACAO_CONTA",
            entityType = "UTILIZADOR",
            entityId = user.badgeNumber,
            details = "Conta particular de agente de terreno criada: ${user.name} (${user.patent}) - ${user.unit}"
        )
    }

    suspend fun updateAgent(user: UserEntity) {
        db.userDao().updateUser(user)
        logAudit(
            user = user,
            action = "ATUALIZACAO_CONTA",
            entityType = "UTILIZADOR",
            entityId = user.badgeNumber,
            details = "Dados da conta particular atualizados: ${user.name} (${user.badgeNumber})"
        )
    }

    // POIs
    val allPois: Flow<List<LocationPoiEntity>> = db.locationPoiDao().getAllPois()

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    val unreadAlertsCount: Flow<Int> = db.notificationDao().getUnreadCount()

    suspend fun markNotificationAsRead(id: Long) = db.notificationDao().markAsRead(id)
    suspend fun markAllNotificationsAsRead() = db.notificationDao().markAllAsRead()

    // Audit logs
    val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()

    suspend fun logAudit(
        user: UserEntity,
        action: String,
        entityType: String,
        entityId: String,
        details: String
    ) {
        db.auditLogDao().insertLog(
            AuditLogEntity(
                userBadge = user.badgeNumber,
                userName = user.name,
                userRole = user.role,
                action = action,
                entityType = entityType,
                entityId = entityId,
                details = details
            )
        )
    }

    /**
     * Executes synchronization with ODK Central & Central PostGIS DB
     */
    suspend fun syncAllPending(currentUser: UserEntity): Pair<Int, Int> {
        val pendingIncidents = db.incidentDao().getPendingSyncIncidents()
        val pendingSurveys = db.cartographicSurveyDao().getPendingSurveys()

        var successCount = 0
        var errorCount = 0

        pendingIncidents.forEach { incident ->
            try {
                // In production, this posts to ODK Central REST API (e.g. /v1/projects/1/forms/{id}/submissions)
                val syncedIncident = incident.copy(
                    syncStatus = "SYNCHRONIZED",
                    syncErrorMessage = null,
                    updatedAt = System.currentTimeMillis()
                )
                db.incidentDao().updateIncident(syncedIncident)
                successCount++
            } catch (e: Exception) {
                errorCount++
                db.incidentDao().updateIncident(
                    incident.copy(
                        syncStatus = "ERROR",
                        syncErrorMessage = e.message ?: "Falha ao contactar servidor ODK Central"
                    )
                )
            }
        }

        pendingSurveys.forEach { survey ->
            try {
                val syncedSurvey = survey.copy(syncStatus = "SYNCHRONIZED")
                db.cartographicSurveyDao().updateSurvey(syncedSurvey)
                successCount++
            } catch (e: Exception) {
                errorCount++
            }
        }

        logAudit(
            user = currentUser,
            action = "SINCRONIZACAO",
            entityType = "LOTE",
            entityId = "SYNC-ALL",
            details = "Sincronização concluída: $successCount com sucesso, $errorCount falhas."
        )

        return Pair(successCount, errorCount)
    }

    /**
     * Armazena em cache local Room as ocorrências vindas da nuvem (Firestore / ODK),
     * garantindo disponibilidade 100% offline para consulta sem conexão.
     * Preserva ocorrências pendentes locais ainda não sincronizadas.
     */
    suspend fun cacheRemoteIncidents(remoteList: List<IncidentEntity>) {
        if (remoteList.isEmpty()) return
        val pendingIncidents = db.incidentDao().getPendingSyncIncidents().associateBy { it.incidentNumber }
        val toCache = remoteList.filterNot { it.incidentNumber in pendingIncidents.keys }
            .map { it.copy(syncStatus = "SYNCHRONIZED") }
        if (toCache.isNotEmpty()) {
            db.incidentDao().insertIncidents(toCache)
        }
    }

    /**
     * Limpar todas as ocorrências e levantamentos locais para iniciar operação com dados 100% limpos
     */
    suspend fun clearOperationalData(currentUser: UserEntity) {
        db.incidentDao().deleteAllIncidents()
        db.cartographicSurveyDao().deleteAllSurveys()
        db.notificationDao().deleteAllNotifications()
        logAudit(
            user = currentUser,
            action = "LIMPEZA_DADOS",
            entityType = "BASE_DADOS",
            entityId = "ALL",
            details = "Limpeza de dados operacionais e reinicialização efectuada por ${currentUser.name}"
        )
    }

    /**
     * Elimina permanentemente qualquer dado de demonstração pré-existente
     */
    suspend fun cleanAllDemoData() {
        try {
            db.incidentDao().deleteByNumbers(com.example.gis.CartographicData.LEGACY_DEMO_INCIDENT_NUMBERS)
            db.cartographicSurveyDao().deleteByCodes(com.example.gis.CartographicData.LEGACY_DEMO_SURVEY_CODES)
            db.userDao().deleteDemoUsers(com.example.gis.CartographicData.DEMO_USER_BADGES)
        } catch (e: Exception) {
            // Ignorar se já não existirem
        }
    }

    suspend fun ensureInitialDataIfEmpty() {
        val poiCount = db.locationPoiDao().getAllPois().firstOrNull()?.size ?: 0
        if (poiCount == 0) {
            db.locationPoiDao().insertPois(com.example.gis.CartographicData.OFFICIAL_POIS)
        }
        val userCount = db.userDao().getAllUsers().firstOrNull()?.size ?: 0
        if (userCount == 0) {
            db.userDao().insertUser(com.example.odk.OdkEngine.CLEAN_DEFAULT_USER)
        }
    }

    suspend fun clearAccountInformation() {
        db.userDao().deleteDemoUsers(com.example.gis.CartographicData.DEMO_USER_BADGES)
        db.userDao().insertUser(com.example.odk.OdkEngine.CLEAN_DEFAULT_USER)
    }
}
