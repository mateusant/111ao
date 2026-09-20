package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel

/**
 * Tela de Autenticação Firebase do Sistema Ocorrência Remota
 * Permite que cada agente acesse ou registre sua própria conta particular
 * para registro e auditoria de ocorrências no terreno.
 */
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login Firebase, 1: Novo Agente Firebase, 2: PIN de Campo

    // Login Firebase fields
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Cadastro Firebase fields
    var regFullName by remember { mutableStateOf("") }
    var regBadgeNumber by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var regPatent by remember { mutableStateOf("Agente de 1ª Classe") }
    var regUnit by remember { mutableStateOf("Divisão de Polícia de Ordem Pública") }
    var regPhone by remember { mutableStateOf("+244 923 ") }
    var regPin by remember { mutableStateOf("") }

    // Offline / Field PIN fields
    var fieldBadge by remember { mutableStateOf("") }
    var fieldPin by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Insígnia / Logotipo Tático
            Surface(
                shape = CircleShape,
                color = SecCyanPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Emblema de Segurança",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "OCORRÊNCIA REMOTA",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Autenticação de Agentes de Terreno",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecCyanPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Firebase Connection Status Chip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isOnline) SecEmerald.copy(alpha = 0.15f) else SecAmber.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isOnline) SecEmerald else SecAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "Firebase Cloud Auth Ativo" else "Modo de Terreno (Sem Rede)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) SecEmerald else SecAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            viewModel.clearAuthError()
                        },
                        text = { Text("Entrar (Firebase)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.clearAuthError()
                        },
                        text = { Text("Novo Agente", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            viewModel.clearAuthError()
                        },
                        text = { Text("PIN Campo", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Column(modifier = Modifier.padding(18.dp)) {
                    // Error Banner
                    AnimatedVisibility(visible = authError != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecRed.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = authError ?: "",
                                    fontSize = 11.sp,
                                    color = SecRed,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    when (selectedTab) {
                        // 1. LOGIN COM FIREBASE
                        0 -> {
                            Text(
                                text = "Acesso à Conta Particular do Agente",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Introduza as credenciais da sua conta institucional vinculada ao Firebase.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                label = { Text("Email Institucional (*)") },
                                placeholder = { Text("agente@policia.gov.ao") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Palavra-passe (*)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                        Icon(
                                            if (showLoginPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    viewModel.loginWithFirebase(
                                        email = loginEmail,
                                        pass = loginPassword,
                                        onSuccess = {
                                            Toast.makeText(context, "Sessão iniciada com sucesso via Firebase!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        },
                                        onError = {
                                            // Handled in ViewModel error state
                                        }
                                    )
                                },
                                enabled = !isLoading && loginEmail.isNotBlank() && loginPassword.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Entrar na Minha Conta", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // 2. REGISTAR NOVO AGENTE NO FIREBASE
                        1 -> {
                            Text(
                                text = "Registo de Agente no Firebase",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Crie uma conta particular vinculada ao Firebase para envio e assinatura de ocorrências.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = { Text("Nome Completo do Agente (*)") },
                                placeholder = { Text("Ex: Pedro Manuel dos Santos") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = regBadgeNumber,
                                    onValueChange = { regBadgeNumber = it.uppercase() },
                                    label = { Text("Nº Mecanográfico (*)") },
                                    placeholder = { Text("POL-901") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = regPatent,
                                    onValueChange = { regPatent = it },
                                    label = { Text("Patente Policial (*)") },
                                    placeholder = { Text("Agente 1ª Classe") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regUnit,
                                onValueChange = { regUnit = it },
                                label = { Text("Unidade / Esquadra (*)") },
                                placeholder = { Text("Comando Municipal / Esquadra Central") },
                                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Institucional (*)") },
                                placeholder = { Text("pedro.santos@policia.gov.ao") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Palavra-passe Firebase (mín. 6 car.) (*)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                        Icon(
                                            if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = regPin,
                                    onValueChange = { if (it.length <= 6) regPin = it },
                                    label = { Text("PIN Terreno (4 dígitos)") },
                                    placeholder = { Text("1234") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("Telefone Operacional") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    viewModel.registerWithFirebase(
                                        email = regEmail,
                                        pass = regPassword,
                                        fullName = regFullName,
                                        badgeNumber = regBadgeNumber,
                                        patent = regPatent,
                                        unit = regUnit,
                                        pin = regPin,
                                        phone = regPhone,
                                        onSuccess = {
                                            Toast.makeText(context, "Conta criada no Firebase com sucesso!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        },
                                        onError = {
                                            // Handled in ViewModel error state
                                        }
                                    )
                                },
                                enabled = !isLoading && regEmail.isNotBlank() && regPassword.isNotBlank() && regFullName.isNotBlank() && regBadgeNumber.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = SecEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Criar Conta e Aceder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // 3. ACESSO RÁPIDO DE CAMPO (PIN / OFFLINE)
                        2 -> {
                            Text(
                                text = "Acesso Rápido de Terreno (Modo Offline)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Para acesso direto no terreno com banco de dados local Room / SQLite sem requisições de rede.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = fieldBadge,
                                onValueChange = { fieldBadge = it.uppercase() },
                                label = { Text("Nº Mecanográfico do Agente (*)") },
                                placeholder = { Text("Ex: POL-442") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = fieldPin,
                                onValueChange = { fieldPin = it },
                                label = { Text("PIN de Segurança Pessoal (*)") },
                                placeholder = { Text("Código de 4 dígitos") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    viewModel.loginWithFieldPin(
                                        badgeNumber = fieldBadge,
                                        pin = fieldPin,
                                        onSuccess = {
                                            Toast.makeText(context, "Sessão iniciada na conta local do agente!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        },
                                        onError = {
                                            // Handled in ViewModel error state
                                        }
                                    )
                                },
                                enabled = !isLoading && fieldBadge.isNotBlank() && fieldPin.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Entrar na Conta Particular", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Acesso Direto para uso prático diário sem senhas pré-definidas
            OutlinedButton(
                onClick = {
                    viewModel.enterCleanPrivateAccount {
                        Toast.makeText(context, "Acesso concedido à Conta Particular Limpa!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                },
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Entrar na Conta Particular Limpa (Uso Diário)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecCyanPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer info
            Text(
                text = "Ministério do Interior • Direcção Geral da Polícia Nacional",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Segurança com Encriptação AES-256 e Firebase Cloud Auth",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
