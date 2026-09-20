package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.FirebaseAuthManager
import com.example.data.AppDatabase
import com.example.data.firestore.FirestoreIncidentManager
import com.example.data.model.CartographicSurveyEntity
import com.example.data.model.EvidenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.LocationPoiEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OdkFormEntity
import com.example.data.model.UserEntity
import com.example.data.repository.AppRepository
import com.example.gis.GisEngine
import com.example.odk.OdkEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = AppRepository(db)

    // Current Authenticated User
    private val _currentUser = MutableStateFlow(OdkEngine.CLEAN_DEFAULT_USER) // Default clean account
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    // Authentication session state
    private val _isUserLoggedIn = MutableStateFlow(true)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        // Automatically restore session if already authenticated with Firebase
        viewModelScope.launch {
            try {
                val fbUser = FirebaseAuthManager.getCurrentFirebaseUser(application)
                if (fbUser != null) {
                    val email = fbUser.email ?: ""
                    val localUser = repository.getUserByFirebaseUid(fbUser.uid)
                        ?: if (email.isNotBlank()) repository.getUserByEmail(email) else null
                    if (localUser != null) {
                        _currentUser.value = localUser
                        _isUserLoggedIn.value = true
                    }
                }
            } catch (e: Exception) {
                // Ignore initialization error in offline cold boot
            }
        }

        // Observação em tempo real das ocorrências do Cloud Firestore
        viewModelScope.launch {
            try {
                FirestoreIncidentManager.observeIncidentsFlow(application).collect { list ->
                    val productionList = list.filterNot { it.incidentNumber in com.example.gis.CartographicData.LEGACY_DEMO_INCIDENT_NUMBERS }
                    _firestoreIncidents.value = productionList
                    _firestoreSyncStatus.value = if (productionList.isEmpty()) {
                        "Cloud Firestore Pronto (0 ocorrências)"
                    } else {
                        "${productionList.size} ocorrências ativas no Firestore"
                    }
                    // Armazena no cache local Room para consulta e visualização 100% offline
                    repository.cacheRemoteIncidents(productionList)
                }
            } catch (e: Exception) {
                _firestoreSyncStatus.value = "Modo Offline (Base Local)"
            }
        }

        // Assegura ambiente de produção limpo e utilizável diariamente sem dados de demonstração
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.cleanAllDemoData()
                FirestoreIncidentManager.cleanDemoData(application)
                repository.ensureInitialDataIfEmpty()
                if (_currentUser.value.badgeNumber in com.example.gis.CartographicData.DEMO_USER_BADGES) {
                    val cleanUser = repository.getUserByBadge("PARTICULAR") ?: OdkEngine.CLEAN_DEFAULT_USER
                    _currentUser.value = cleanUser
                }
            } catch (e: Exception) {
                // Ignore initialization error
            }
        }

        // Monitorização ativa de conectividade de rede para auto-sincronização
        setupNetworkMonitoring()
    }

    // Network Status (Field Connectivity / Offline Mode)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private fun setupNetworkMonitoring() {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        try {
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isOnline.value = isConnected

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val wasOffline = !_isOnline.value
                    _isOnline.value = true
                    if (wasOffline) {
                        autoSyncPendingData()
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            }
            networkCallback = callback
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // Fallback se não for permitido registrar callback
        }
    }

    override fun onCleared() {
        super.onCleared()
        networkCallback?.let { callback ->
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Sync in Progress Flag
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // Cloud Firestore Incidents Flow & Status
    private val _firestoreIncidents = MutableStateFlow<List<IncidentEntity>>(emptyList())
    val firestoreIncidents: StateFlow<List<IncidentEntity>> = _firestoreIncidents.asStateFlow()

    private val _firestoreSyncStatus = MutableStateFlow("Firestore Cloud Conectado")
    val firestoreSyncStatus: StateFlow<String> = _firestoreSyncStatus.asStateFlow()

    // Filter toggle for "Minhas Ocorrências (Conta Particular)" vs "Todas as Ocorrências"
    private val _isMyIncidentsFilter = MutableStateFlow(true)
    val isMyIncidentsFilter: StateFlow<Boolean> = _isMyIncidentsFilter.asStateFlow()

    // Dialog state for Account Manager Modal
    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> = _showAccountDialog.asStateFlow()

    // Data Flows from Repository
    val incidents: StateFlow<List<IncidentEntity>> = repository.allIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Marcadores Unificados para o Google Maps baseados no Firestore e Base Local
    val mapIncidents: StateFlow<List<IncidentEntity>> = combine(incidents, _firestoreIncidents) { localList, firestoreList ->
        if (firestoreList.isEmpty()) {
            localList
        } else {
            val map = linkedMapOf<String, IncidentEntity>()
            // Carrega primeiro as locais
            for (item in localList) {
                map[item.incidentNumber] = item
            }
            // Sobrepõe e adiciona as ocorrências do Cloud Firestore
            for (item in firestoreList) {
                map[item.incidentNumber] = item
            }
            map.values.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ocorrências cadastradas exclusivamente na conta particular do agente autenticado
    val myIncidents: StateFlow<List<IncidentEntity>> = combine(_currentUser, incidents) { user, list ->
        list.filter {
            it.agentCode.equals(user.badgeNumber, ignoreCase = true) ||
            (user.badgeNumber == "PARTICULAR" && (it.agentCode.isBlank() || it.agentCode == "PARTICULAR"))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val surveys: StateFlow<List<CartographicSurveyEntity>> = repository.allSurveys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mySurveys: StateFlow<List<CartographicSurveyEntity>> = combine(_currentUser, surveys) { user, list ->
        list.filter {
            it.agentCode.equals(user.badgeNumber, ignoreCase = true) ||
            (user.badgeNumber == "PARTICULAR" && (it.agentCode.isBlank() || it.agentCode == "PARTICULAR"))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forms: StateFlow<List<OdkFormEntity>> = repository.allForms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pois: StateFlow<List<LocationPoiEntity>> = repository.allPois
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadAlertsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val auditLogs = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users = repository.allUsers
        .map { list ->
            list.filter { it.badgeNumber !in com.example.gis.CartographicData.DEMO_USER_BADGES }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(OdkEngine.CLEAN_DEFAULT_USER))

    val fieldAgents = repository.fieldAgents
        .map { list ->
            list.filter { it.badgeNumber !in com.example.gis.CartographicData.DEMO_USER_BADGES }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openAccountDialog() {
        _showAccountDialog.value = true
    }

    fun closeAccountDialog() {
        _showAccountDialog.value = false
    }

    fun toggleMyIncidentsFilter() {
        _isMyIncidentsFilter.value = !_isMyIncidentsFilter.value
    }

    fun setMyIncidentsFilter(onlyMine: Boolean) {
        _isMyIncidentsFilter.value = onlyMine
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
        viewModelScope.launch {
            repository.logAudit(
                user = user,
                action = "LOGIN",
                entityType = "SESSAO",
                entityId = user.badgeNumber,
                details = "Sessão iniciada na conta particular de ${user.patent} ${user.name} (${user.badgeNumber})"
            )
        }
    }

    fun loginWithPin(
        badge: String,
        pin: String,
        onSuccess: (UserEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val user = repository.authenticateAgent(badge.trim(), pin.trim())
            if (user != null) {
                _currentUser.value = user
                repository.logAudit(
                    user = user,
                    action = "LOGIN",
                    entityType = "SESSAO",
                    entityId = user.badgeNumber,
                    details = "Login autenticado com PIN na conta de ${user.name}"
                )
                onSuccess(user)
            } else {
                val exists = repository.getUserByBadge(badge.trim())
                if (exists == null) {
                    onError("Nº de Agente/Mecanográfico não encontrado no sistema.")
                } else {
                    onError("PIN/Senha incorreta para o agente $badge.")
                }
            }
        }
    }

    fun registerNewAgentAccount(
        badgeNumber: String,
        name: String,
        pin: String,
        unit: String,
        role: String = "AGENTE_CAMPO",
        phone: String,
        patent: String,
        photoUri: String?,
        onSuccess: (UserEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val cleanBadge = badgeNumber.trim().uppercase()
            if (cleanBadge.isBlank()) {
                onError("O Nº Mecanográfico/Badge é obrigatório.")
                return@launch
            }
            if (name.trim().isBlank()) {
                onError("O nome completo do agente é obrigatório.")
                return@launch
            }
            if (photoUri.isNullOrBlank()) {
                onError("A fotografia de identificação do perfil é obrigatória.")
                return@launch
            }
            if (pin.trim().length < 4) {
                onError("O PIN de acesso pessoal deve ter pelo menos 4 dígitos.")
                return@launch
            }
            val existing = repository.getUserByBadge(cleanBadge)
            if (existing != null) {
                onError("Já existe um agente cadastrado com o número $cleanBadge.")
                return@launch
            }

            val newAgent = UserEntity(
                badgeNumber = cleanBadge,
                name = name.trim(),
                role = role,
                unit = unit.trim().ifBlank { "Comando Municipal / Terreno" },
                email = "${name.trim().lowercase().replace(" ", ".")}@policia.gov.ao",
                active = true,
                pin = pin.trim(),
                phone = phone.trim().ifBlank { "+244 923 000 000" },
                patent = patent.trim().ifBlank { "Agente de 2ª Classe" },
                photoUri = photoUri.trim(),
                createdAt = System.currentTimeMillis()
            )
            repository.registerAgent(newAgent)
            _currentUser.value = newAgent
            onSuccess(newAgent)
        }
    }

    fun loginWithFirebase(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onError("Email institucional e palavra-passe são obrigatórios.")
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = FirebaseAuthManager.signInWithEmail(getApplication(), email, pass)
            _authLoading.value = false
            result.fold(
                onSuccess = { fbUser ->
                    var user = repository.getUserByFirebaseUid(fbUser.uid)
                    if (user == null && fbUser.email != null) {
                        user = repository.getUserByEmail(fbUser.email!!)
                    }
                    if (user == null) {
                        // Create profile for this authenticated agent
                        val generatedBadge = "AGT-" + fbUser.uid.take(4).uppercase()
                        user = UserEntity(
                            badgeNumber = generatedBadge,
                            name = fbUser.displayName ?: fbUser.email?.substringBefore("@")?.replace(".", " ")?.replaceFirstChar { it.uppercase() } ?: "Agente Operacional",
                            role = "AGENTE_CAMPO",
                            unit = "Comando Provincial / Terreno",
                            email = fbUser.email ?: email,
                            active = true,
                            pin = "1234",
                            phone = "+244 923 000 000",
                            patent = "Agente de 1ª Classe",
                            firebaseUid = fbUser.uid,
                            photoUri = com.example.ui.components.ProfilePhotoUtils.PRESET_OFFICER_ONE
                        )
                        repository.registerAgent(user)
                    }
                    _currentUser.value = user
                    _isUserLoggedIn.value = true
                    onSuccess()
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Falha ao autenticar com Firebase."
                    _authError.value = msg
                    onError(msg)
                }
            )
        }
    }

    fun registerWithFirebase(
        email: String,
        pass: String,
        fullName: String,
        badgeNumber: String,
        patent: String,
        unit: String,
        pin: String,
        phone: String,
        photoUri: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || pass.isBlank() || fullName.isBlank() || badgeNumber.isBlank()) {
            onError("Preencha todos os campos obrigatórios (*).")
            return
        }
        val finalPhoto = photoUri?.trim()?.ifBlank { null } ?: com.example.ui.components.ProfilePhotoUtils.PRESET_OFFICER_ONE
        if (pass.length < 6) {
            onError("A palavra-passe deve conter pelo menos 6 caracteres.")
            return
        }
        val cleanBadge = badgeNumber.trim().uppercase()
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val existingBadge = repository.getUserByBadge(cleanBadge)
            if (existingBadge != null) {
                _authLoading.value = false
                onError("Já existe um agente cadastrado com o número mecanográfico $cleanBadge.")
                return@launch
            }

            val result = FirebaseAuthManager.signUpWithEmail(getApplication(), email, pass)
            _authLoading.value = false
            result.fold(
                onSuccess = { fbUser ->
                    val newUser = UserEntity(
                        badgeNumber = cleanBadge,
                        name = fullName.trim(),
                        role = "AGENTE_CAMPO",
                        unit = unit.trim().ifBlank { "Comando Municipal / Terreno" },
                        email = email.trim(),
                        active = true,
                        pin = if (pin.trim().length >= 4) pin.trim() else "1234",
                        phone = phone.trim().ifBlank { "+244 923 000 000" },
                        patent = patent.trim().ifBlank { "Agente de 1ª Classe" },
                        firebaseUid = fbUser.uid,
                        photoUri = finalPhoto
                    )
                    repository.registerAgent(newUser)
                    _currentUser.value = newUser
                    _isUserLoggedIn.value = true
                    onSuccess()
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Erro ao criar conta no Firebase."
                    _authError.value = msg
                    onError(msg)
                }
            )
        }
    }

    fun loginWithFieldPin(badgeNumber: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val user = repository.authenticateAgent(badgeNumber.trim().uppercase(), pin.trim())
            _authLoading.value = false
            if (user != null) {
                _currentUser.value = user
                _isUserLoggedIn.value = true
                onSuccess()
            } else {
                val msg = "Nº Mecanográfico ou PIN de segurança incorreto."
                _authError.value = msg
                onError(msg)
            }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        FirebaseAuthManager.signOut(getApplication())
        _isUserLoggedIn.value = false
        onComplete()
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun updateAgentProfile(
        name: String,
        unit: String,
        phone: String,
        patent: String,
        newPin: String?,
        badgeNumber: String = "",
        email: String = "",
        photoUri: String? = null,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val current = _currentUser.value
            val finalPhoto = photoUri?.trim()?.ifBlank { null } ?: current.photoUri
            if (finalPhoto.isNullOrBlank()) {
                onError?.invoke("A fotografia de perfil é obrigatória para identificação.")
                return@launch
            }
            val targetBadge = badgeNumber.trim().ifBlank { current.badgeNumber.ifBlank { "PARTICULAR" } }
            val updated = current.copy(
                badgeNumber = targetBadge,
                name = name.trim(),
                unit = unit.trim(),
                phone = phone.trim(),
                patent = patent.trim(),
                email = email.trim().ifBlank { current.email },
                pin = if (!newPin.isNullOrBlank() && newPin.trim().length >= 4) newPin.trim() else current.pin,
                photoUri = finalPhoto
            )
            repository.insertUser(updated)
            _currentUser.value = updated
            onSuccess()
        }
    }

    fun updateProfilePhoto(
        photoUri: String,
        onSuccess: () -> Unit = {},
        onError: ((String) -> Unit)? = null
    ) {
        if (photoUri.isBlank()) {
            onError?.invoke("A fotografia de perfil é obrigatória.")
            return
        }
        viewModelScope.launch {
            val current = _currentUser.value
            val updated = current.copy(photoUri = photoUri.trim())
            repository.insertUser(updated)
            _currentUser.value = updated
            onSuccess()
        }
    }

    fun clearAccountInformation(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAccountInformation()
            _currentUser.value = OdkEngine.CLEAN_DEFAULT_USER
            onSuccess()
        }
    }

    fun enterCleanPrivateAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByBadge("PARTICULAR") ?: OdkEngine.CLEAN_DEFAULT_USER
            _currentUser.value = user
            _isUserLoggedIn.value = true
            onSuccess()
        }
    }

    fun toggleOnline() {
        val newState = !_isOnline.value
        _isOnline.value = newState
        if (newState) {
            autoSyncPendingData()
        }
    }

    fun autoSyncPendingData() {
        viewModelScope.launch {
            try {
                if (_isSyncing.value) return@launch
                _isSyncing.value = true
                _syncMessage.value = "Rede restabelecida. Sincronizando dados pendentes..."
                val (successOdk, _) = repository.syncAllPending(_currentUser.value)
                val currentLocal = repository.allIncidents.firstOrNull() ?: emptyList()
                val (successFirestore, _) = FirestoreIncidentManager.syncLocalListToFirestore(getApplication(), currentLocal)
                val remoteList = FirestoreIncidentManager.fetchIncidentsOnce(getApplication()).getOrNull() ?: emptyList()
                if (remoteList.isNotEmpty()) {
                    _firestoreIncidents.value = remoteList
                    repository.cacheRemoteIncidents(remoteList)
                }
                _isSyncing.value = false
                _syncMessage.value = "Sincronização automática concluída ($successOdk ODK, $successFirestore Firestore)"
            } catch (e: Exception) {
                _isSyncing.value = false
            }
        }
    }

    fun createIncident(
        incidentNumber: String,
        date: String,
        time: String,
        category: String,
        type: String,
        description: String,
        locationName: String,
        municipio: String,
        bairro: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        altitude: Double,
        priority: String,
        hasPhoto: Boolean = false,
        hasCroqui: Boolean = false,
        notes: String? = null,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val syncState = if (_isOnline.value) "SYNCHRONIZED" else "PENDING"
            val incident = IncidentEntity(
                incidentNumber = incidentNumber,
                date = date,
                time = time,
                incidentType = type,
                category = category,
                description = description,
                locationName = locationName,
                municipio = municipio,
                bairro = bairro,
                latitude = latitude,
                longitude = longitude,
                gpsAccuracy = accuracy,
                altitude = altitude,
                agentCode = user.badgeNumber,
                agentName = user.name,
                unitDepartment = user.unit,
                status = "Registada",
                priority = priority,
                syncStatus = syncState,
                wktGeometry = GisEngine.toWktPoint(latitude, longitude),
                hasPhoto = hasPhoto,
                hasCroqui = hasCroqui,
                notes = notes
            )
            val id = repository.insertIncident(incident, user)
            // Se estiver online, tenta enviar em tempo real para o Cloud Firestore
            if (_isOnline.value) {
                try {
                    val result = FirestoreIncidentManager.saveIncident(getApplication(), incident)
                    if (result.isFailure) {
                        repository.updateIncident(incident.copy(syncStatus = "PENDING"), user)
                    }
                } catch (e: Exception) {
                    repository.updateIncident(incident.copy(syncStatus = "PENDING"), user)
                }
            }
            onSuccess(id)
        }
    }

    fun syncWithFirestore(onSuccess: (Int) -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                val currentLocal = repository.allIncidents.firstOrNull() ?: emptyList()
                val (successes, _) = FirestoreIncidentManager.syncLocalListToFirestore(getApplication(), currentLocal)
                val remoteList = FirestoreIncidentManager.fetchIncidentsOnce(getApplication()).getOrNull() ?: emptyList()
                if (remoteList.isNotEmpty()) {
                    _firestoreIncidents.value = remoteList
                    repository.cacheRemoteIncidents(remoteList)
                }
                _isSyncing.value = false
                _firestoreSyncStatus.value = "Sincronizado ($successes sincronizados no Firestore)"
                onSuccess(successes)
            } catch (e: Exception) {
                _isSyncing.value = false
                onError(e.localizedMessage ?: "Falha na sincronização")
            }
        }
    }

    fun createSurvey(
        surveyCode: String,
        title: String,
        surveyType: String,
        coordinateSystem: String,
        coordinatesJson: String,
        lengthMeters: Double,
        areaM2: Double,
        municipio: String,
        bairro: String,
        notes: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val syncState = if (_isOnline.value) "SYNCHRONIZED" else "PENDING"
            val survey = CartographicSurveyEntity(
                surveyCode = surveyCode,
                title = title,
                surveyType = surveyType,
                coordinateSystem = coordinateSystem,
                coordinatesJson = coordinatesJson,
                lengthMeters = lengthMeters,
                areaM2 = areaM2,
                agentCode = user.badgeNumber,
                municipio = municipio,
                bairro = bairro,
                notes = notes,
                syncStatus = syncState
            )
            val id = repository.insertSurvey(survey, user)
            onSuccess(id)
        }
    }

    fun updateStatus(incident: IncidentEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateIncident(incident.copy(status = newStatus), _currentUser.value)
        }
    }

    fun validateIncident(incidentNumber: String) {
        viewModelScope.launch {
            repository.validateIncident(incidentNumber, _currentUser.value)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Conectando ao ODK Central e PostGIS..."
            kotlinx.coroutines.delay(1200) // Network transmission
            val (success, error) = repository.syncAllPending(_currentUser.value)
            _isSyncing.value = false
            _syncMessage.value = if (error == 0) {
                "Sincronização concluída com sucesso! ($success registros)"
            } else {
                "Sincronização parcial: $success ok, $error pendentes."
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun clearOperationalData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearOperationalData(_currentUser.value)
            _firestoreIncidents.value = emptyList()
            try {
                FirestoreIncidentManager.cleanDemoData(getApplication<Application>())
            } catch (e: Exception) {
                // Silencioso
            }
            onComplete()
        }
    }
}
