package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade de Ocorrência Criminal compatível com PostgreSQL / PostGIS
 * Tabela: ocorrencias
 */
@Entity(tableName = "ocorrencias")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentNumber: String, // ex: "OC-2026-00101"
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val timestamp: Long = System.currentTimeMillis(),
    val incidentType: String, // e.g. "Roubo Qualificado", "Furto", "Homicídio"
    val category: String, // "Furto", "Roubo", "Agressão", "Homicídio", "Violência", "Acidente", "Vandalismo", "Tráfico", "Desaparecimento", "Incêndio", "Outros"
    val description: String,
    val locationName: String,
    val bairro: String,
    val municipio: String,
    val provincia: String = "Luanda",
    val latitude: Double,
    val longitude: Double,
    val gpsAccuracy: Float = 4.2f, // metros
    val altitude: Double = 35.0, // metros
    val agentCode: String, // ex: "AG-4421"
    val agentName: String,
    val unitDepartment: String, // ex: "1ª Divisão Policial - Maianga"
    val status: String = "Registada", // "Rascunho", "Registada", "Em Investigação", "Validada", "Encaminhada", "Concluída", "Arquivada"
    val priority: String = "Média", // "Baixa", "Média", "Alta", "Crítica"
    val syncStatus: String = "SYNCHRONIZED", // "SYNCHRONIZED", "PENDING", "ERROR"
    val syncErrorMessage: String? = null,
    val odkFormId: String = "ODK-CRIM-01",
    val wktGeometry: String = "", // e.g. "POINT(13.2345 -8.8383)"
    val suspectInfo: String? = null,
    val victimInfo: String? = null,
    val notes: String? = null,
    val hasPhoto: Boolean = false,
    val hasCroqui: Boolean = false,
    val validatedBy: String? = null,
    val validatedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Levantamento Cartográfico e Infraestruturas
 * Tabela: levantamentos_cartograficos
 */
@Entity(tableName = "levantamentos_cartograficos")
data class CartographicSurveyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surveyCode: String, // ex: "LEV-2026-0042"
    val title: String,
    val surveyType: String, // "PONTO", "LINHA", "POLIGONO", "CROQUI"
    val coordinateSystem: String = "WGS 84", // "WGS 84", "UTM", "GEOGRAFICAS_DMS"
    val coordinatesJson: String, // JSON de pontos [{lat, lng, alt}]
    val lengthMeters: Double = 0.0,
    val areaM2: Double = 0.0,
    val accuracy: Float = 3.5f,
    val altitude: Double = 42.0,
    val timestamp: Long = System.currentTimeMillis(),
    val agentCode: String,
    val municipio: String,
    val bairro: String,
    val notes: String = "",
    val photoUri: String? = null,
    val syncStatus: String = "SYNCHRONIZED" // "SYNCHRONIZED", "PENDING", "ERROR"
)

/**
 * Evidências com Metadados e Cadeia de Custódia (Tamper-evident)
 * Tabela: evidencias
 */
@Entity(tableName = "evidencias")
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentNumber: String,
    val evidenceType: String, // "FOTOGRAFIA_LOCAL", "FOTOGRAFIA_DANOS", "OBJETO_RELEVANTE", "CROQUI", "DOCUMENTO"
    val title: String,
    val description: String,
    val fileUri: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val hashSha256: String, // Hash para garantia de integridade da cadeia de custódia
    val recordedBy: String,
    val isSealed: Boolean = true
)

/**
 * Formulários ODK Cadastrados
 * Tabela: formularios_odk
 */
@Entity(tableName = "formularios_odk")
data class OdkFormEntity(
    @PrimaryKey
    val formId: String, // ex: "ODK-CRIM-01"
    val title: String,
    val version: String,
    val category: String,
    val description: String,
    val fieldsJson: String,
    val xmlTemplate: String,
    val isActive: Boolean = true,
    val submissionCount: Int = 0
)

/**
 * Registo de Auditoria
 * Tabela: auditoria
 */
@Entity(tableName = "auditoria")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userBadge: String,
    val userName: String,
    val userRole: String,
    val action: String, // "CRIACAO", "EDICAO", "EXCLUSAO", "VALIDACAO", "SINCRONIZACAO", "EXPORTACAO", "LOGIN"
    val entityType: String, // "OCORRENCIA", "LEVANTAMENTO", "EVIDENCIA", "FORMULARIO"
    val entityId: String,
    val details: String
)

/**
 * Utilizadores e Perfis (RBAC)
 * Tabela: utilizadores
 */
@Entity(tableName = "utilizadores")
data class UserEntity(
    @PrimaryKey
    val badgeNumber: String, // ex: "POL-001"
    val name: String,
    val role: String, // "ADMINISTRADOR", "SUPERVISOR", "AGENTE_CAMPO", "ANALISTA_SIG"
    val unit: String,
    val email: String = "",
    val active: Boolean = true,
    val pin: String = "1234", // PIN de 4 dígitos para autenticação da conta particular
    val phone: String = "+244 923 000 000",
    val patent: String = "Agente de 1ª Classe", // Patente/Graduação Policial
    val firebaseUid: String? = null, // UID gerado pelo Firebase Authentication
    val photoUri: String? = null, // Caminho local ou URI da fotografia obrigatória do perfil
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Notificações e Alertas Operacionais
 * Tabela: notificacoes
 */
@Entity(tableName = "notificacoes")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "CRITICAL_AREA", "PRIORITY_CRIME", "SYNC_ERROR", "PENDING_VALIDATION", "NEW_DISPATCH"
    val incidentNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val priority: String = "Normal" // "Baixa", "Normal", "Alta", "Urgente"
)

/**
 * Locais e Pontos de Interesse (POIs)
 * Tabela: locais
 */
@Entity(tableName = "locais")
data class LocationPoiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val type: String, // "Esquadra Policial", "Hospital", "Banco", "Escola", "Ponto Crítico", "Posto Fiscal"
    val latitude: Double,
    val longitude: Double,
    val municipio: String,
    val bairro: String,
    val riskLevel: String = "Normal"
)
