package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.auth.FirebaseAuthManager
import com.example.data.model.IncidentEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Gestor de persistência e sincronização de Ocorrências no Cloud Firestore
 * Permite leitura e escrita em tempo real das ocorrências na coleção "ocorrencias",
 * alimentando os marcadores visuais do Google Maps com suporte a cache offline.
 */
object FirestoreIncidentManager {
    private const val TAG = "FirestoreIncidentMgr"
    const val COLLECTION_INCIDENTS = "ocorrencias"

    fun getFirestore(context: Context): FirebaseFirestore? {
        return try {
            FirebaseAuthManager.getAuth(context)
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Aviso ao obter Firestore: ${e.message}")
            null
        }
    }

    /**
     * Salva ou atualiza uma ocorrência no Cloud Firestore
     */
    suspend fun saveIncident(context: Context, incident: IncidentEntity): Result<Unit> {
        val firestore = getFirestore(context) ?: return Result.failure(Exception("Firestore não inicializado"))
        val docId = incident.incidentNumber.ifBlank { "OC-${System.currentTimeMillis()}" }

        return suspendCancellableCoroutine { cont ->
            try {
                val data = incidentToMap(incident)
                firestore.collection(COLLECTION_INCIDENTS)
                    .document(docId)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Ocorrência $docId guardada com sucesso no Firestore")
                        if (cont.isActive) cont.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { err ->
                        Log.w(TAG, "Falha ao enviar $docId para o Firestore: ${err.message}")
                        if (cont.isActive) cont.resume(Result.failure(err))
                    }
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(Result.failure(e))
            }
        }
    }

    /**
     * Busca todas as ocorrências gravadas no Firestore uma única vez
     */
    suspend fun fetchIncidentsOnce(context: Context): Result<List<IncidentEntity>> {
        val firestore = getFirestore(context) ?: return Result.failure(Exception("Firestore indisponível"))
        return suspendCancellableCoroutine { cont ->
            try {
                firestore.collection(COLLECTION_INCIDENTS)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.data?.let { mapToIncident(it, doc.id) }
                        }
                        if (cont.isActive) cont.resume(Result.success(list))
                    }
                    .addOnFailureListener { err ->
                        if (cont.isActive) cont.resume(Result.failure(err))
                    }
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(Result.failure(e))
            }
        }
    }

    /**
     * Flow que escuta em tempo real adições e atualizações de ocorrências no Firestore
     */
    fun observeIncidentsFlow(context: Context): Flow<List<IncidentEntity>> = callbackFlow {
        val firestore = getFirestore(context)
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = firestore.collection(COLLECTION_INCIDENTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Erro no SnapshotListener do Firestore: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            doc.data?.let { mapToIncident(it, doc.id) }
                        }
                        trySend(items)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao registrar listener do Firestore: ${e.message}")
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Sincroniza lista local de ocorrências com o Firestore
     */
    suspend fun syncLocalListToFirestore(context: Context, localList: List<IncidentEntity>): Pair<Int, Int> {
        var successes = 0
        var errors = 0
        for (inc in localList) {
            val res = saveIncident(context, inc)
            if (res.isSuccess) {
                successes++
            } else {
                errors++
            }
        }
        return Pair(successes, errors)
    }

    fun incidentToMap(incident: IncidentEntity): Map<String, Any?> = mapOf(
        "incidentNumber" to incident.incidentNumber,
        "date" to incident.date,
        "time" to incident.time,
        "timestamp" to incident.timestamp,
        "incidentType" to incident.incidentType,
        "category" to incident.category,
        "description" to incident.description,
        "locationName" to incident.locationName,
        "bairro" to incident.bairro,
        "municipio" to incident.municipio,
        "provincia" to incident.provincia,
        "latitude" to incident.latitude,
        "longitude" to incident.longitude,
        "gpsAccuracy" to incident.gpsAccuracy.toDouble(),
        "altitude" to incident.altitude,
        "agentCode" to incident.agentCode,
        "agentName" to incident.agentName,
        "unitDepartment" to incident.unitDepartment,
        "status" to incident.status,
        "priority" to incident.priority,
        "syncStatus" to "SYNCHRONIZED",
        "odkFormId" to incident.odkFormId,
        "wktGeometry" to incident.wktGeometry,
        "suspectInfo" to (incident.suspectInfo ?: ""),
        "victimInfo" to (incident.victimInfo ?: ""),
        "notes" to (incident.notes ?: ""),
        "hasPhoto" to incident.hasPhoto,
        "hasCroqui" to incident.hasCroqui,
        "validatedBy" to (incident.validatedBy ?: ""),
        "validatedAt" to (incident.validatedAt ?: 0L),
        "createdAt" to incident.createdAt,
        "updatedAt" to incident.updatedAt
    )

    fun mapToIncident(data: Map<String, Any?>, documentId: String): IncidentEntity? {
        return try {
            val lat = when (val v = data["latitude"]) {
                is Double -> v
                is Number -> v.toDouble()
                is String -> v.toDoubleOrNull() ?: -8.8383
                else -> -8.8383
            }
            val lng = when (val v = data["longitude"]) {
                is Double -> v
                is Number -> v.toDouble()
                is String -> v.toDoubleOrNull() ?: 13.2344
                else -> 13.2344
            }
            val num = (data["incidentNumber"] as? String)?.ifBlank { documentId } ?: documentId

            IncidentEntity(
                id = 0,
                incidentNumber = num,
                date = (data["date"] as? String) ?: "2026-09-17",
                time = (data["time"] as? String) ?: "10:00",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                incidentType = (data["incidentType"] as? String) ?: "Ocorrência",
                category = (data["category"] as? String) ?: "Outros",
                description = (data["description"] as? String) ?: "",
                locationName = (data["locationName"] as? String) ?: "Luanda",
                bairro = (data["bairro"] as? String) ?: "Centro",
                municipio = (data["municipio"] as? String) ?: "Luanda",
                provincia = (data["provincia"] as? String) ?: "Luanda",
                latitude = lat,
                longitude = lng,
                gpsAccuracy = (data["gpsAccuracy"] as? Number)?.toFloat() ?: 4.0f,
                altitude = (data["altitude"] as? Number)?.toDouble() ?: 35.0,
                agentCode = (data["agentCode"] as? String) ?: "POL-442",
                agentName = (data["agentName"] as? String) ?: "Agente de Campo",
                unitDepartment = (data["unitDepartment"] as? String) ?: "Comando Provincial",
                status = (data["status"] as? String) ?: "Registada",
                priority = (data["priority"] as? String) ?: "Média",
                syncStatus = "SYNCHRONIZED",
                syncErrorMessage = null,
                odkFormId = (data["odkFormId"] as? String) ?: "ODK-CRIM-01",
                wktGeometry = (data["wktGeometry"] as? String) ?: "POINT($lng $lat)",
                suspectInfo = (data["suspectInfo"] as? String)?.ifBlank { null },
                victimInfo = (data["victimInfo"] as? String)?.ifBlank { null },
                notes = (data["notes"] as? String)?.ifBlank { null },
                hasPhoto = (data["hasPhoto"] as? Boolean) ?: false,
                hasCroqui = (data["hasCroqui"] as? Boolean) ?: false,
                validatedBy = (data["validatedBy"] as? String)?.ifBlank { null },
                validatedAt = (data["validatedAt"] as? Number)?.toLong()?.takeIf { it > 0L },
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro convertendo mapa Firestore em ocorrência: ${e.message}")
            null
        }
    }

    /**
     * Remove uma ocorrência do Cloud Firestore
     */
    suspend fun deleteIncident(context: Context, incidentNumber: String): Result<Unit> {
        val firestore = getFirestore(context) ?: return Result.failure(Exception("Firestore não inicializado"))
        return suspendCancellableCoroutine { cont ->
            try {
                firestore.collection(COLLECTION_INCIDENTS)
                    .document(incidentNumber)
                    .delete()
                    .addOnSuccessListener {
                        if (cont.isActive) cont.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { err ->
                        if (cont.isActive) cont.resume(Result.failure(err))
                    }
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(Result.failure(e))
            }
        }
    }

    /**
     * Limpa dados de demonstração da coleção Firestore para manter o ambiente de produção 100% operacional
     */
    suspend fun cleanDemoData(context: Context) {
        val firestore = getFirestore(context) ?: return
        com.example.gis.CartographicData.LEGACY_DEMO_INCIDENT_NUMBERS.forEach { id ->
            try {
                firestore.collection(COLLECTION_INCIDENTS).document(id).delete()
            } catch (e: Exception) {
                // Silencioso se não existir ou sem permissão
            }
        }
    }
}
