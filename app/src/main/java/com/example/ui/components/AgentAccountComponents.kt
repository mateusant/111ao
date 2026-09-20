package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserEntity
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecNavyDark
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val POLICE_PATENTS = listOf(
    "Agente de 3ª Classe",
    "Agente de 2ª Classe",
    "Agente de 1ª Classe",
    "Sub-Inspector de Polícia",
    "Inspector de Polícia",
    "Intendente de Polícia",
    "Superintendente de Polícia",
    "Superintendente-Chefe"
)

val POLICE_UNITS = listOf(
    "1ª Divisão Policial - Maianga / Patrulha Terreno",
    "2ª Divisão Policial - Kilamba Kiaxi / Terreno",
    "3ª Divisão Policial - Rangel / Terreno",
    "4ª Divisão Policial - Cazenga / Patrulha Móvel",
    "5ª Divisão Policial - Viana / Operações de Terreno",
    "6ª Divisão Policial - Belas / Brigada de Campo",
    "Comando Municipal de Luanda / Terreno",
    "Comando Municipal de Cacuaco / Terreno",
    "Direção de Informações Policiais / Campo"
)

/**
 * Cartão Digital de Identificação do Agente no Terreno
 */
@Composable
fun AgentCardCredential(
    user: UserEntity,
    myIncidentsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SecCyanPrimary.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SecNavyDark.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Header Bar with Emblema e Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SecCyanPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SecCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "POLÍCIA NACIONAL DE ANGOLA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = SecCyanLight
                            )
                            Text(
                                text = "CARTEIRA OPERACIONAL DE TERRENO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SecEmerald.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SecEmerald, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "CONTA ATIVA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = SecEmerald
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Agent Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar com Suporte à Foto Obrigatória
                    ProfilePhotoAvatar(
                        photoUri = user.photoUri,
                        name = user.name,
                        size = 54.dp,
                        showWarningBadge = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name.ifBlank { "Conta Particular (Sem Dados)" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val patentBadgeText = if (user.name.isNotBlank() || (user.badgeNumber.isNotBlank() && user.badgeNumber != "PARTICULAR")) {
                            "${user.patent.ifBlank { "Agente de Terreno" }} • ${if (user.badgeNumber.isNotBlank() && user.badgeNumber != "PARTICULAR") user.badgeNumber else "Conta Operacional"}"
                        } else {
                            "Perfil Operacional em Branco • Pronto para o Dia-a-Dia"
                        }
                        Text(
                            text = patentBadgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecCyanPrimary
                        )
                        Text(
                            text = user.unit.ifBlank { "Unidade / Posto: Não configurada" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom strip with occurrences count and digital code
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = SecCyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ocorrências Registadas no Terreno:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$myIncidentsCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = SecCyanPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo Completo de Gestão de Contas Particulares de Agentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentAccountDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val myIncidents by viewModel.myIncidents.collectAsState()
    val mySurveys by viewModel.mySurveys.collectAsState()

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayIncidentsCount = remember(myIncidents, todayDate) {
        myIncidents.count { it.date == todayDate }
    }
    val pendingIncidentsCount = remember(myIncidents) {
        myIncidents.count { it.syncStatus == "PENDING" }
    }
    val syncedIncidentsCount = remember(myIncidents) {
        myIncidents.count { it.syncStatus == "SYNCHRONIZED" }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // State for PIN dialog when switching user
    var userToAuthenticate by remember { mutableStateOf<UserEntity?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }

    // State for Editing Profile
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser.name) }
    var editUnit by remember { mutableStateOf(currentUser.unit) }
    var editPhone by remember { mutableStateOf(currentUser.phone) }
    var editPatent by remember { mutableStateOf(currentUser.patent) }
    var editBadge by remember { mutableStateOf(if (currentUser.badgeNumber != "PARTICULAR") currentUser.badgeNumber else "") }
    var editEmail by remember { mutableStateOf(currentUser.email) }
    var editNewPin by remember { mutableStateOf("") }
    var editPhotoUri by remember { mutableStateOf(currentUser.photoUri ?: "") }
    var editPhotoError by remember { mutableStateOf<String?>(null) }
    var showConfirmClearAccountDialog by remember { mutableStateOf(false) }

    // State for Registering New Agent
    var regBadge by remember { mutableStateOf("") }
    var regName by remember { mutableStateOf("") }
    var regPatent by remember { mutableStateOf(POLICE_PATENTS[1]) } // Agente de 2ª Classe
    var regUnit by remember { mutableStateOf(POLICE_UNITS[0]) }
    var regPhone by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var regPinConfirm by remember { mutableStateOf("") }
    var regPhotoUri by remember { mutableStateOf("") }
    var regError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(currentUser) {
        editName = currentUser.name
        editUnit = currentUser.unit
        editPhone = currentUser.phone
        editPatent = currentUser.patent
        editBadge = if (currentUser.badgeNumber != "PARTICULAR") currentUser.badgeNumber else ""
        editEmail = currentUser.email
        editPhotoUri = currentUser.photoUri ?: ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONTA PARTICULAR DO AGENTE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Registo individual de ocorrências e atividades de campo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = SecCyanPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Minha Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Trocar Agente", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("+ Novo Agente", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                when (selectedTabIndex) {
                    // TAB 0: MINHA CONTA DO AGENTE
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            item {
                                AgentCardCredential(
                                    user = currentUser,
                                    myIncidentsCount = myIncidents.size
                                )
                            }

                            // Personal Field Stats Grid
                            item {
                                Text(
                                    text = "DESEMPENHO INDIVIDUAL NO TERRENO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Ocorrências
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Total Cadastrado", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${myIncidents.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SecCyanPrimary)
                                            Text("Ocorrências de campo", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // Hoje no terreno
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Registadas Hoje", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$todayIncidentsCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SecEmerald)
                                            Text("Neste turno", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Sincronizadas
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Sincronizadas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$syncedIncidentsCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SecCyanLight)
                                            Text("Comando Central", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // Pendentes Offline
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Pendentes Sync", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$pendingIncidentsCount", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (pendingIncidentsCount > 0) SecAmber else SecEmerald)
                                            Text("Salvas no telemóvel", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            // Account Details List
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Nº Mecanográfico:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val badgeLabel = if (currentUser.badgeNumber.isNotBlank() && currentUser.badgeNumber != "PARTICULAR") currentUser.badgeNumber else "Não definido (Modo Particular)"
                                            Text(badgeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Telefone Funcional:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(currentUser.phone.ifBlank { "Não configurado" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Email Oficial:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(currentUser.email.ifBlank { "Não configurado" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Levantamentos SIG:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${mySurveys.size} realizados", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecEmerald)
                                        }
                                    }
                                }
                            }

                            // Action Buttons
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            editName = currentUser.name
                                            editUnit = currentUser.unit
                                            editPhone = currentUser.phone
                                            editPatent = currentUser.patent
                                            editBadge = if (currentUser.badgeNumber != "PARTICULAR") currentUser.badgeNumber else ""
                                            editEmail = currentUser.email
                                            editNewPin = ""
                                            showEditProfileDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Configurar Conta", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { selectedTabIndex = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Trocar Agente", fontSize = 12.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showConfirmClearAccountDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecRed),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SecRed.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecRed)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Limpar Dados da Conta (Deixar Vazio)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecRed)
                                }

                                Button(
                                    onClick = {
                                        viewModel.closeAccountDialog()
                                        viewModel.logout()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Terminar Sessão (Sair)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // TAB 1: TROCAR DE AGENTE / LISTA DE CONTAS
                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            item {
                                Text(
                                    text = "SELECIONE UMA CONTA DE AGENTE PARA ENTRAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (users.size <= 1) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Conta Operacional Individual",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = SecCyanPrimary
                                            )
                                            Text(
                                                text = "Esta é a sua conta principal e única no dispositivo, configurada para uso diário sem dados de demonstração. Caso deseje cadastrar outro perfil compartilhado, utilize a aba '+ Novo Agente'.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            items(users) { user ->
                                val isSelected = user.badgeNumber == currentUser.badgeNumber
                                val badgeColor = when (user.role) {
                                    "ADMINISTRADOR" -> SecRed
                                    "SUPERVISOR" -> SecAmber
                                    else -> SecCyanPrimary
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) SecCyanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) {
                                                Toast.makeText(context, "Esta já é a sua conta ativa.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                userToAuthenticate = user
                                                pinInput = ""
                                                pinError = null
                                                showPinDialog = true
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            ProfilePhotoAvatar(
                                                photoUri = user.photoUri,
                                                name = user.name,
                                                size = 38.dp,
                                                showWarningBadge = true
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = user.name.ifBlank { "Conta Particular" },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                val badgeSubtitle = if (user.badgeNumber.isNotBlank() && user.badgeNumber != "PARTICULAR") {
                                                    "${user.patent.ifBlank { "Agente" }} • ${user.badgeNumber}"
                                                } else {
                                                    if (user.patent.isNotBlank()) user.patent else "Conta Operacional em Branco"
                                                }
                                                Text(
                                                    text = badgeSubtitle,
                                                    fontSize = 11.sp,
                                                    color = badgeColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = user.unit.ifBlank { "Sem unidade configurada" },
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = SecEmerald.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "ATIVO",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = SecEmerald,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    userToAuthenticate = user
                                                    pinInput = ""
                                                    pinError = null
                                                    showPinDialog = true
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Entrar", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: CADASTRAR NOVA CONTA DE AGENTE
                    2 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            item {
                                Text(
                                    text = "CADASTRO DE CONTA PARTICULAR DE AGENTE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Cada agente tem direito à sua conta individual para registar e auditar todas as ocorrências de terreno.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Error message if any
                            if (regError != null) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SecRed.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SecRed),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = regError ?: "",
                                            fontSize = 11.sp,
                                            color = SecRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }

                            // Fotografia Obrigatória de Perfil do Agente
                            item {
                                ProfilePhotoPickerField(
                                    currentPhotoUri = regPhotoUri.ifBlank { null },
                                    onPhotoSelected = {
                                        regPhotoUri = it
                                        regError = null
                                    },
                                    isMandatoryError = regError != null && regPhotoUri.isBlank()
                                )
                            }

                            // Nº Mecanográfico / Badge
                            item {
                                OutlinedTextField(
                                    value = regBadge,
                                    onValueChange = { regBadge = it },
                                    label = { Text("Nº de Agente / Mecanográfico *") },
                                    placeholder = { Text("ex: POL-778 ou AG-1234") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Nome Completo
                            item {
                                OutlinedTextField(
                                    value = regName,
                                    onValueChange = { regName = it },
                                    label = { Text("Nome Completo do Agente *") },
                                    placeholder = { Text("ex: Mateus Pedro Domingos") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Patente Policial Dropdown
                            item {
                                var expandedPatent by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedPatent,
                                    onExpandedChange = { expandedPatent = !expandedPatent }
                                ) {
                                    OutlinedTextField(
                                        value = regPatent,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Patente / Graduação *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPatent) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedPatent,
                                        onDismissRequest = { expandedPatent = false }
                                    ) {
                                        POLICE_PATENTS.forEach { patent ->
                                            DropdownMenuItem(
                                                text = { Text(patent) },
                                                onClick = {
                                                    regPatent = patent
                                                    expandedPatent = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Unidade / Esquadra Dropdown
                            item {
                                var expandedUnit by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedUnit,
                                    onExpandedChange = { expandedUnit = !expandedUnit }
                                ) {
                                    OutlinedTextField(
                                        value = regUnit,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Esquadra / Unidade Operacional *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedUnit,
                                        onDismissRequest = { expandedUnit = false }
                                    ) {
                                        POLICE_UNITS.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit) },
                                                onClick = {
                                                    regUnit = unit
                                                    expandedUnit = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Telefone Operacional
                            item {
                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("Telefone Funcional / Contacto") },
                                    placeholder = { Text("+244 923 000 000") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // PIN de Segurança (4 dígitos)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = regPin,
                                        onValueChange = { if (it.length <= 6) regPin = it },
                                        label = { Text("PIN Pessoal *") },
                                        placeholder = { Text("ex: 1234") },
                                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = regPinConfirm,
                                        onValueChange = { if (it.length <= 6) regPinConfirm = it },
                                        label = { Text("Confirmar PIN *") },
                                        placeholder = { Text("ex: 1234") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            // Submit Button
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        if (regPhotoUri.isBlank()) {
                                            regError = "A fotografia de perfil é obrigatória para cadastrar um novo perfil."
                                            return@Button
                                        }
                                        if (regBadge.isBlank() || regName.isBlank()) {
                                            regError = "Por favor, preencha o número mecanográfico e o nome completo."
                                            return@Button
                                        }
                                        if (regPin.length < 4) {
                                            regError = "O PIN pessoal deve ter pelo menos 4 dígitos."
                                            return@Button
                                        }
                                        if (regPin != regPinConfirm) {
                                            regError = "Os PINs digitados não coincidem."
                                            return@Button
                                        }
                                        regError = null

                                        viewModel.registerNewAgentAccount(
                                            badgeNumber = regBadge,
                                            name = regName,
                                            pin = regPin,
                                            unit = regUnit,
                                            role = "AGENTE_CAMPO",
                                            phone = regPhone,
                                            patent = regPatent,
                                            photoUri = regPhotoUri,
                                            onSuccess = { createdUser ->
                                                Toast.makeText(
                                                    context,
                                                    "Conta particular do agente ${createdUser.name} criada com fotografia obrigatória!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                regPhotoUri = ""
                                                regBadge = ""
                                                regName = ""
                                                regPin = ""
                                                regPinConfirm = ""
                                                selectedTabIndex = 0
                                            },
                                            onError = { err ->
                                                regError = err
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cadastrar Conta e Iniciar Sessão", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para Autenticação por PIN ao Trocar de Agente
    if (showPinDialog && userToAuthenticate != null) {
        val target = userToAuthenticate!!
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SecCyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Autenticação da Conta", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Acesso à conta do ${target.patent} ${target.name} (${target.badgeNumber})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("PIN de Segurança") },
                        placeholder = { Text("Introduza o PIN de 4 dígitos") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = pinError ?: "", fontSize = 11.sp, color = SecRed, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Introduza o código de acesso pessoal para autenticar a sessão do agente.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loginWithPin(
                            badge = target.badgeNumber,
                            pin = pinInput,
                            onSuccess = {
                                Toast.makeText(context, "Sessão iniciada como ${target.name}!", Toast.LENGTH_SHORT).show()
                                showPinDialog = false
                                selectedTabIndex = 0
                            },
                            onError = { err ->
                                pinError = err
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary)
                ) {
                    Text("Validar e Entrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para Editar Perfil e Alterar PIN
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Configurar Dados da Conta Particular", fontSize = 16.sp, fontWeight = FontWeight.Black)
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Cada perfil deve ter uma fotografia obrigatória para identificação no terreno. Configure a sua foto e dados operacionais abaixo.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Fotografia Obrigatória de Perfil
                    ProfilePhotoPickerField(
                        currentPhotoUri = editPhotoUri.ifBlank { null },
                        onPhotoSelected = {
                            editPhotoUri = it
                            editPhotoError = null
                        },
                        isMandatoryError = editPhotoError != null && editPhotoUri.isBlank()
                    )

                    if (editPhotoError != null) {
                        Text(
                            text = editPhotoError ?: "",
                            fontSize = 11.sp,
                            color = SecRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome Completo") },
                        placeholder = { Text("ex: António Manuel ou em branco") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editBadge,
                        onValueChange = { editBadge = it },
                        label = { Text("Nº Mecanográfico") },
                        placeholder = { Text("ex: POL-0001 ou em branco") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUnit,
                        onValueChange = { editUnit = it },
                        label = { Text("Unidade / Esquadra / Divisão") },
                        placeholder = { Text("ex: 1ª Divisão Policial") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Telefone Operacional") },
                        placeholder = { Text("ex: +244 923 000 000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Operacional / Pessoal") },
                        placeholder = { Text("ex: agente@policia.gov.ao") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNewPin,
                        onValueChange = { editNewPin = it },
                        label = { Text("Novo PIN (opcional)") },
                        placeholder = { Text("Mínimo 4 dígitos para proteger acesso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = {
                            editName = ""
                            editUnit = ""
                            editPhone = ""
                            editPatent = ""
                            editBadge = ""
                            editEmail = ""
                            editNewPin = ""
                            editPhotoUri = ""
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Esvaziar Todos os Campos", fontSize = 11.sp, color = SecRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editPhotoUri.isBlank()) {
                            editPhotoError = "A fotografia de perfil é obrigatória para identificação."
                            Toast.makeText(context, "A fotografia é obrigatória para cada perfil!", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        viewModel.updateAgentProfile(
                            name = editName,
                            unit = editUnit,
                            phone = editPhone,
                            patent = editPatent,
                            newPin = editNewPin.ifBlank { null },
                            badgeNumber = editBadge.ifBlank { "PARTICULAR" },
                            email = editEmail,
                            photoUri = editPhotoUri,
                            onSuccess = {
                                Toast.makeText(context, "Dados e foto do perfil salvos com sucesso!", Toast.LENGTH_SHORT).show()
                                showEditProfileDialog = false
                            },
                            onError = { err ->
                                editPhotoError = err
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary)
                ) {
                    Text("Salvar Alterações")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Confirmação para Limpar Informações da Conta
    if (showConfirmClearAccountDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearAccountDialog = false },
            title = {
                Text("Limpar Informações da Conta?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Esta operação remove todas as informações pessoais ou de demonstração da sua conta particular, deixando-a totalmente sem dados cadastrados e pronta para utilização diária no terreno.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAccountInformation {
                            Toast.makeText(context, "Informações da conta limpas! Conta agora sem dados.", Toast.LENGTH_SHORT).show()
                            showConfirmClearAccountDialog = false
                            editName = ""
                            editUnit = ""
                            editPhone = ""
                            editPatent = ""
                            editBadge = ""
                            editEmail = ""
                            editPhotoUri = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecRed)
                ) {
                    Text("Sim, Limpar Conta")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmClearAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Chip compacta de topo para a barra do aplicativo exibindo o Agente ativo
 */
@Composable
fun AgentTopBarChip(
    user: UserEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.5f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePhotoAvatar(
                photoUri = user.photoUri,
                name = user.name,
                size = 24.dp,
                showWarningBadge = true
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                val displayName = if (user.name.isNotBlank()) {
                    user.name.split(" ").filter { it.isNotBlank() }.let {
                        if (it.size >= 2) "${it.first()} ${it.last()}" else user.name
                    }
                } else {
                    "Conta Particular"
                }
                Text(
                    text = displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val badgeSubtitle = if (user.badgeNumber.isNotBlank() && user.badgeNumber != "PARTICULAR") {
                    user.badgeNumber
                } else {
                    "Modo Operacional"
                }
                Text(
                    text = badgeSubtitle,
                    fontSize = 9.sp,
                    color = SecCyanPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
