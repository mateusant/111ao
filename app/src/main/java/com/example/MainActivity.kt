package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CartographicSurveyScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GisMapScreen
import com.example.ui.screens.HeatmapScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncidentsListScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.components.AgentAccountDialog
import com.example.ui.components.AgentTopBarChip
import com.example.ui.screens.NewIncidentScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsAdminScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: ImageVector) {
    HOME("Início", Icons.Default.Home),
    NEW_INCIDENT("Nova Ocorrência", Icons.Default.AddCircle),
    GIS_MAP("Mapa SIG", Icons.Default.Map),
    HEATMAP("Mapa de Calor", Icons.Default.LocalFireDepartment),
    INCIDENTS_LIST("Ocorrências", Icons.AutoMirrored.Filled.List),
    SURVEY("Levantamento", Icons.Default.AddLocationAlt),
    DASHBOARD("Inteligência SIG", Icons.Default.Assessment),
    SYNC("Sincronização", Icons.Default.Sync),
    REPORTS("Relatórios", Icons.Default.Description),
    NOTIFICATIONS("Alertas", Icons.Default.Notifications),
    SETTINGS("Configurações", Icons.Default.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.openAccountDialog()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val unreadAlerts by viewModel.unreadNotificationsCount.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(310.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drawer Tactical Header
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp).padding(top = 24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SecCyanPrimary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "OCORRÊNCIA REMOTA",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Plataforma ODK + SIG / WebGIS",
                                        fontSize = 10.sp,
                                        color = SecCyanPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.example.ui.components.ProfilePhotoAvatar(
                                    photoUri = currentUser.photoUri,
                                    name = currentUser.name,
                                    size = 42.dp,
                                    showWarningBadge = true,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        viewModel.openAccountDialog()
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val displayName = if (currentUser.name.isNotBlank()) currentUser.name else "Conta Particular"
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${currentUser.patent.ifBlank { "Agente de Terreno" }} • ${if (currentUser.badgeNumber != "PARTICULAR") currentUser.badgeNumber else "Operacional"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SecCyanPrimary
                                    )
                                    Text(
                                        text = currentUser.unit.ifBlank { "Unidade não definida" },
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        viewModel.openAccountDialog()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Conta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Drawer Items
                    AppDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        val badgeCount = when (destination) {
                            AppDestination.SYNC -> if (pendingCount > 0) pendingCount else null
                            AppDestination.NOTIFICATIONS -> if (unreadAlerts > 0) unreadAlerts else null
                            else -> null
                        }

                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = destination.label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    if (badgeCount != null && badgeCount > 0) {
                                        Surface(
                                            color = if (destination == AppDestination.SYNC) SecAmber else SecRed,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "$badgeCount",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                    tint = if (isSelected) SecCyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                currentDestination = destination
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = SecCyanPrimary.copy(alpha = 0.15f),
                                selectedTextColor = SecCyanPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Terminar Sessão / Sair
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = "Terminar Sessão",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecRed
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Terminar Sessão",
                                tint = SecRed
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = SecRed.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )

                    // Version Footer
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Versão 2026.1 • PostgreSQL + PostGIS\nODK Central Mobile Client",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "OCORRÊNCIA REMOTA",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Lateral")
                        }
                    },
                    actions = {
                        // Network status toggle button in top bar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isOnline) SecEmerald.copy(alpha = 0.15f) else SecAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isOnline) SecEmerald.copy(alpha = 0.4f) else SecAmber.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable { viewModel.toggleOnline() }
                                .padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) SecEmerald else SecAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isOnline) "ON" else "OFF",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOnline) SecEmerald else SecAmber
                                )
                            }
                        }

                        // Agent TopBar Chip - Conta Particular
                        AgentTopBarChip(
                            user = currentUser,
                            onClick = { viewModel.openAccountDialog() },
                            modifier = Modifier.padding(end = 6.dp)
                        )

                        // Notifications Icon with badge
                        IconButton(onClick = { currentDestination = AppDestination.NOTIFICATIONS }) {
                            if (unreadAlerts > 0) {
                                BadgedBox(badge = { Badge { Text("$unreadAlerts") } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Alertas")
                                }
                            } else {
                                Icon(Icons.Default.Notifications, contentDescription = "Alertas")
                            }
                        }

                        // Profile Avatar com Foto Obrigatória
                        com.example.ui.components.ProfilePhotoAvatar(
                            photoUri = currentUser.photoUri,
                            name = currentUser.name,
                            size = 34.dp,
                            showWarningBadge = true,
                            onClick = { viewModel.openAccountDialog() },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val primaryDestinations = listOf(
                        AppDestination.HOME,
                        AppDestination.NEW_INCIDENT,
                        AppDestination.GIS_MAP,
                        AppDestination.HEATMAP,
                        AppDestination.INCIDENTS_LIST
                    )

                    primaryDestinations.forEach { dest ->
                        val isSelected = currentDestination == dest
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = { currentDestination = dest },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SecCyanPrimary,
                                selectedTextColor = SecCyanPrimary,
                                indicatorColor = SecCyanPrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestination.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onNavigateToNewIncident = { currentDestination = AppDestination.NEW_INCIDENT },
                        onNavigateToSurvey = { currentDestination = AppDestination.SURVEY },
                        onNavigateToMap = { currentDestination = AppDestination.GIS_MAP },
                        onNavigateToHeatmap = { currentDestination = AppDestination.HEATMAP },
                        onNavigateToDashboard = { currentDestination = AppDestination.DASHBOARD },
                        onNavigateToSync = { currentDestination = AppDestination.SYNC },
                        onNavigateToList = { currentDestination = AppDestination.INCIDENTS_LIST },
                        onNavigateToReports = { currentDestination = AppDestination.REPORTS },
                        onNavigateToNotifications = { currentDestination = AppDestination.NOTIFICATIONS },
                        onNavigateToSettings = { currentDestination = AppDestination.SETTINGS },
                        onIncidentClick = { currentDestination = AppDestination.INCIDENTS_LIST }
                    )
                    AppDestination.NEW_INCIDENT -> NewIncidentScreen(
                        viewModel = viewModel,
                        onIncidentSaved = { currentDestination = AppDestination.INCIDENTS_LIST }
                    )
                    AppDestination.GIS_MAP -> GisMapScreen(
                        viewModel = viewModel,
                        onIncidentSelected = { currentDestination = AppDestination.INCIDENTS_LIST }
                    )
                    AppDestination.HEATMAP -> HeatmapScreen(
                        viewModel = viewModel
                    )
                    AppDestination.INCIDENTS_LIST -> IncidentsListScreen(
                        viewModel = viewModel,
                        onNavigateToNewIncident = { currentDestination = AppDestination.NEW_INCIDENT }
                    )
                    AppDestination.SURVEY -> CartographicSurveyScreen(
                        viewModel = viewModel,
                        onSurveySaved = { currentDestination = AppDestination.GIS_MAP }
                    )
                    AppDestination.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel
                    )
                    AppDestination.SYNC -> SyncScreen(
                        viewModel = viewModel
                    )
                    AppDestination.REPORTS -> ReportsScreen(
                        viewModel = viewModel
                    )
                    AppDestination.NOTIFICATIONS -> NotificationsScreen(
                        viewModel = viewModel
                    )
                    AppDestination.SETTINGS -> SettingsAdminScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Modal de Gestão da Conta Particular do Agente de Terreno
    val showAccountDialog by viewModel.showAccountDialog.collectAsState()
    if (showAccountDialog) {
        AgentAccountDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeAccountDialog() }
        )
    }
}
