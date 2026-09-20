package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.IncidentEntity
import com.example.odk.OdkEngine
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsListScreen(
    viewModel: MainViewModel,
    onNavigateToNewIncident: () -> Unit
) {
    val context = LocalContext.current
    val incidents by viewModel.incidents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var filterByMyAccountOnly by remember { mutableStateOf(false) }

    var selectedIncidentForDetails by remember { mutableStateOf<IncidentEntity?>(null) }
    var showXmlDialog by remember { mutableStateOf(false) }

    val filteredIncidents = remember(incidents, searchQuery, selectedCategoryFilter, selectedStatusFilter, filterByMyAccountOnly, currentUser) {
        incidents.filter { inc ->
            val matchesAccount = !filterByMyAccountOnly ||
                    inc.agentCode.equals(currentUser.badgeNumber, ignoreCase = true) ||
                    inc.agentName.contains(currentUser.name, ignoreCase = true)

            val matchesQuery = searchQuery.isBlank() ||
                    inc.incidentNumber.contains(searchQuery, ignoreCase = true) ||
                    inc.incidentType.contains(searchQuery, ignoreCase = true) ||
                    inc.locationName.contains(searchQuery, ignoreCase = true) ||
                    inc.bairro.contains(searchQuery, ignoreCase = true) ||
                    inc.municipio.contains(searchQuery, ignoreCase = true) ||
                    inc.agentName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == null || inc.category == selectedCategoryFilter
            val matchesStatus = selectedStatusFilter == null || inc.status == selectedStatusFilter

            matchesAccount && matchesQuery && matchesCategory && matchesStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REGISTO DE OCORRÊNCIAS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredIncidents.size} de ${incidents.size} registadas no banco PostGIS",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onNavigateToNewIncident,
                    colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Nova", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Account Scope Selector: Minha Conta Particular vs Todas
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (filterByMyAccountOnly) SecCyanPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { filterByMyAccountOnly = true }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (filterByMyAccountOnly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Minha Conta (${currentUser.badgeNumber})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (filterByMyAccountOnly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (!filterByMyAccountOnly) SecCyanPrimary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { filterByMyAccountOnly = false }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = if (!filterByMyAccountOnly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Todas as Contas (Comando)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!filterByMyAccountOnly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar por número, tipo, rua, bairro, agente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SecCyanPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("Todas Categorias", fontSize = 11.sp) }
                )
                OdkEngine.CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Todos Estados", "Registada", "Em Investigação", "Validada", "Concluída").forEach { st ->
                    val isAll = st == "Todos Estados"
                    val isSelected = if (isAll) selectedStatusFilter == null else selectedStatusFilter == st
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = if (isAll || selectedStatusFilter == st) null else st },
                        label = { Text(st, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Incidents List
        if (filteredIncidents.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = SecCyanPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (incidents.isEmpty()) "Nenhuma ocorrência registada" else "Nenhum resultado encontrado",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (incidents.isEmpty())
                                "O banco de dados operacional está limpo. Toque no botão '+ Nova' para iniciar o levantamento de ocorrências criminais em campo."
                            else
                                "Tente ajustar os termos de pesquisa ou remover os filtros de categoria e estado.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredIncidents) { inc ->
                IncidentListCard(
                    incident = inc,
                    currentBadge = currentUser.badgeNumber,
                    onClick = { selectedIncidentForDetails = inc }
                )
            }
        }
    }

    // Detail Dialog with full fields, validation, and PostGIS/ODK inspect
    selectedIncidentForDetails?.let { inc ->
        Dialog(onDismissRequest = { selectedIncidentForDetails = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(18.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = inc.incidentNumber,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PriorityBadge(inc.priority)
                            }
                            IconButton(onClick = { selectedIncidentForDetails = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadge(inc.category)
                            SyncStatusBadge(inc.syncStatus)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = inc.incidentType,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = inc.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "📍 Localização:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${inc.locationName}\nBairro: ${inc.bairro} • Município: ${inc.municipio} (${inc.provincia})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🌐 Geometria PostGIS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${inc.wktGeometry}\nLat: ${inc.latitude}, Lng: ${inc.longitude} (Precisão: ${inc.gpsAccuracy}m)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SecCyanPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "👮 Agente Responsável:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${inc.agentName} (${inc.agentCode}) • ${inc.unitDepartment}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        inc.validatedBy?.let { valBy ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = SecEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SecEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Validado formalmente por: $valBy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecEmerald
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Workflow Actions (Validation for Supervisors/Admin)
                        if (currentUser.role == "SUPERVISOR" || currentUser.role == "ADMINISTRADOR") {
                            if (inc.status != "Validada") {
                                Button(
                                    onClick = {
                                        viewModel.validateIncident(inc.incidentNumber)
                                        Toast.makeText(context, "Ocorrência validada com sucesso!", Toast.LENGTH_SHORT).show()
                                        selectedIncidentForDetails = inc.copy(status = "Validada", validatedBy = currentUser.name)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecEmerald),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Validar Ocorrência (Supervisor)")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // OpenRosa XML inspector button
                        OutlinedButton(
                            onClick = { showXmlDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Payload XML OpenRosa (ODK)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // OpenRosa XML Dialog Viewer
    if (showXmlDialog && selectedIncidentForDetails != null) {
        val inc = selectedIncidentForDetails!!
        val xml = OdkEngine.buildOpenRosaXml(
            formId = inc.odkFormId,
            version = "2026.1",
            data = mapOf(
                "numero_ocorrencia" to inc.incidentNumber,
                "data_ocorrencia" to inc.date,
                "hora_ocorrencia" to inc.time,
                "categoria_criminal" to inc.category,
                "tipo_ocorrencia" to inc.incidentType,
                "descricao_factos" to inc.description,
                "municipio" to inc.municipio,
                "bairro" to inc.bairro,
                "local_ocorrencia" to inc.locationName,
                "posicao_geopoint" to "${inc.latitude} ${inc.longitude} ${inc.altitude} ${inc.gpsAccuracy}",
                "agente_responsavel" to inc.agentName,
                "unidade_departamento" to inc.unitDepartment,
                "prioridade" to inc.priority,
                "estado" to inc.status
            )
        )

        Dialog(onDismissRequest = { showXmlDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ODK CENTRAL XML SUBMISSION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SecCyanPrimary
                        )
                        IconButton(onClick = { showXmlDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF070B12),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = xml,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncidentListCard(
    incident: IncidentEntity,
    currentBadge: String = "",
    onClick: () -> Unit
) {
    val isMyIncident = currentBadge.isNotBlank() && incident.agentCode.equals(currentBadge, ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMyIncident) SecEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = incident.incidentNumber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryBadge(incident.category)
                }
                PriorityBadge(incident.priority)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = incident.incidentType,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📍 ${incident.locationName} (${incident.bairro}, ${incident.municipio})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Agent Identity Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isMyIncident) SecEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Agente: ${incident.agentName} (${incident.agentCode})",
                        fontSize = 11.sp,
                        fontWeight = if (isMyIncident) FontWeight.Bold else FontWeight.Normal,
                        color = if (isMyIncident) SecEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isMyIncident) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SecEmerald.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "MINHA CONTA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = SecEmerald,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️ ${incident.date} às ${incident.time} • Estado: ${incident.status}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SyncStatusBadge(status = incident.syncStatus)
            }
        }
    }
}
