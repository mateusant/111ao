package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentEntity
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.components.TacticalHeader
import com.example.ui.components.TacticalKpiCard
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecNavyDark
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenMenu: () -> Unit = {},
    onNavigateToNewIncident: () -> Unit,
    onNavigateToSurvey: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToHeatmap: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onIncidentClick: (IncidentEntity) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val myIncidents by viewModel.myIncidents.collectAsState()
    val mySurveys by viewModel.mySurveys.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val unreadAlerts by viewModel.unreadNotificationsCount.collectAsState()

    var showOnlyMyIncidents by remember { mutableStateOf(true) }

    val currentDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val currentDateFormatted = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("pt-AO")).format(Date()) }

    val totalCount = incidents.size
    val todayCount = incidents.count { it.date == currentDateStr }
    val criticalCount = incidents.count { it.priority == "Crítica" || it.priority == "Alta" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tactical Status & Agent Private Account Card
        item {
            com.example.ui.components.MandatoryPhotoNoticeBanner(
                hasPhoto = !currentUser.photoUri.isNullOrBlank(),
                onConfigurePhotoClick = { viewModel.openAccountDialog() },
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            com.example.ui.components.ProfilePhotoAvatar(
                                photoUri = currentUser.photoUri,
                                name = currentUser.name,
                                size = 48.dp,
                                showWarningBadge = true,
                                onClick = { viewModel.openAccountDialog() }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SecEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "CONTA PARTICULAR ATIVA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SecEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                val displayName = if (currentUser.name.isNotBlank()) {
                                    "${if (currentUser.patent.isNotBlank()) "${currentUser.patent} " else ""}${currentUser.name}"
                                } else {
                                    "Conta Particular (Não Configurada)"
                                }
                                Text(
                                    text = displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val displaySubtitle = buildString {
                                    if (currentUser.badgeNumber.isNotBlank() && currentUser.badgeNumber != "PARTICULAR") {
                                        append("Mecanográfico: ${currentUser.badgeNumber}")
                                    } else {
                                        append("Modo Operacional Individual")
                                    }
                                    if (currentUser.phone.isNotBlank()) {
                                        append(" • Tel: ${currentUser.phone}")
                                    }
                                }
                                Text(
                                    text = displaySubtitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SecCyanPrimary
                                )
                            }
                        }

                        // Network Connectivity Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOnline) SecEmerald.copy(alpha = 0.15f) else SecAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isOnline) SecEmerald.copy(alpha = 0.4f) else SecAmber.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.clickable { viewModel.toggleOnline() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) SecEmerald else SecAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "ON" else "OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOnline) SecEmerald else SecAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (currentUser.unit.isNotBlank()) "📍 Unidade: ${currentUser.unit}" else "📍 Unidade / Posto: Não definida (Toque para configurar)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Agent Private Quick Metrics
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${myIncidents.size}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SecCyanPrimary
                                )
                                Text(
                                    text = "Ocorrências Minhas",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val myToday = myIncidents.count { it.date == currentDateStr }
                                Text(
                                    text = "$myToday",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SecEmerald
                                )
                                Text(
                                    text = "Hoje no Terreno",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val myPending = myIncidents.count { it.syncStatus == "PENDING" }
                                Text(
                                    text = "$myPending",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (myPending > 0) SecAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Por Sincronizar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${mySurveys.size}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "Levantamentos SIG",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PIN: ●●●● (Sessão Protegida)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { viewModel.openAccountDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Minha Conta / Trocar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. MENU DO APLICATIVO (Menu Principal e Navegação Rápida)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SecCyanPrimary.copy(alpha = 0.18f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = null,
                                        tint = SecCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MENU DO APLICATIVO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Módulos operacionais do sistema",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenMenu,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Menu Lateral", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Linha 1: Nova Ocorrência + Registo de Ocorrências
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Nova Ocorrência",
                            subtitle = "Formulário ODK de campo",
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            accentColor = SecCyanPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToNewIncident
                        )
                        QuickActionButton(
                            title = "Ocorrências",
                            subtitle = "Consultar e filtrar lista",
                            icon = Icons.Default.Description,
                            accentColor = Color(0xFF818CF8),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToList
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linha 2: Mapa SIG + Mapa de Calor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Mapa SIG",
                            subtitle = "WebGIS e camadas",
                            icon = Icons.Default.Map,
                            accentColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMap
                        )
                        QuickActionButton(
                            title = "Mapa de Calor",
                            subtitle = "Hotspots de criminalidade",
                            icon = Icons.Default.LocalFireDepartment,
                            accentColor = SecRed,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToHeatmap
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linha 3: Levantamento Cartográfico + Dashboard
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Levantamento",
                            subtitle = "Ponto, Linha e Área",
                            icon = Icons.Default.AddLocationAlt,
                            accentColor = SecEmerald,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSurvey
                        )
                        QuickActionButton(
                            title = "Dashboard",
                            subtitle = "Estatísticas e gráficos",
                            icon = Icons.Default.Assessment,
                            accentColor = Color(0xFFA78BFA),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDashboard
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linha 4: Relatórios + Sincronização
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Relatórios",
                            subtitle = "Boletins e resumos táticos",
                            icon = Icons.Default.Summarize,
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )
                        QuickActionButton(
                            title = "Sincronização",
                            subtitle = if (pendingCount > 0) "$pendingCount pendentes" else "Base atualizada",
                            icon = Icons.Default.Sync,
                            accentColor = if (pendingCount > 0) SecAmber else SecEmerald,
                            badgeCount = if (pendingCount > 0) pendingCount else null,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSync
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linha 5: Alertas + Configurações
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            title = "Alertas",
                            subtitle = if (unreadAlerts > 0) "$unreadAlerts não lidos" else "Central de mensagens",
                            icon = Icons.Default.Notifications,
                            accentColor = Color(0xFFEC4899),
                            badgeCount = if (unreadAlerts > 0) unreadAlerts else null,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToNotifications
                        )
                        QuickActionButton(
                            title = "Definições",
                            subtitle = "PIN e opções do sistema",
                            icon = Icons.Default.Settings,
                            accentColor = Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSettings
                        )
                    }
                }
            }
        }

        // 3. Tactical Operational KPIs
        item {
            Text(
                text = "INDICADORES DO SISTEMA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalKpiCard(
                    title = "Total Geral",
                    value = "$totalCount",
                    subtitle = "Base PostgreSQL",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    accentColor = SecCyanPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToList
                )
                TacticalKpiCard(
                    title = "Hoje",
                    value = "$todayCount",
                    subtitle = currentDateFormatted,
                    icon = Icons.Default.Assessment,
                    accentColor = SecEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToDashboard
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalKpiCard(
                    title = "Fila Offline",
                    value = "$pendingCount",
                    subtitle = if (pendingCount > 0) "Aguardando ODK" else "Sincronizado",
                    icon = Icons.Default.Sync,
                    accentColor = if (pendingCount > 0) SecAmber else SecEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSync
                )
                TacticalKpiCard(
                    title = "Prioridade Alta",
                    value = "$criticalCount",
                    subtitle = "Requer Intervenção",
                    icon = Icons.Default.Warning,
                    accentColor = SecRed,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToList
                )
            }
        }

        // 4. Synchronization Status Banner if pending items exist
        if (pendingCount > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SecAmber.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = SecAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$pendingCount registos pendentes de sincronização",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Armazenados localmente no SQLite / Room",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToSync,
                            colors = ButtonDefaults.buttonColors(containerColor = SecAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sincronizar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // 5. Recent Incidents List (Últimas Ocorrências Registadas)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OCORRÊNCIAS OPERACIONAIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Ver Todas (${if (showOnlyMyIncidents) myIncidents.size else incidents.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecCyanPrimary,
                        modifier = Modifier.clickable { onNavigateToList() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle between Minhas vs Todas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showOnlyMyIncidents) SecCyanPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showOnlyMyIncidents = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (showOnlyMyIncidents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Minha Conta (${myIncidents.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showOnlyMyIncidents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!showOnlyMyIncidents) SecCyanPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showOnlyMyIncidents = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = if (!showOnlyMyIncidents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Todas (${incidents.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!showOnlyMyIncidents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        val displayList = if (showOnlyMyIncidents) myIncidents else incidents

        if (displayList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SecCyanPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showOnlyMyIncidents) "Nenhuma ocorrência nesta conta particular" else "Nenhuma ocorrência registada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val agentGreeting = if (currentUser.name.isNotBlank()) currentUser.name else "da sua conta"
                        Text(
                            text = if (showOnlyMyIncidents)
                                "O agente $agentGreeting ainda não cadastrou ocorrências no terreno. Toque no botão 'Nova Ocorrência' para cadastrar."
                            else
                                "Utilize 'Nova Ocorrência' para efetuar o primeiro levantamento em campo com georreferenciação ODK.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(displayList.take(5)) { incident ->
                IncidentItemCard(incident = incident, onClick = { onIncidentClick(incident) })
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeCount: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (badgeCount != null && badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = accentColor
                ) {
                    Text(
                        text = "$badgeCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IncidentItemCard(
    incident: IncidentEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
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

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "📍 ${incident.locationName} (${incident.bairro}, ${incident.municipio})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️ ${incident.date} às ${incident.time} • 👮 ${incident.agentName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SyncStatusBadge(status = incident.syncStatus)
            }
        }
    }
}
