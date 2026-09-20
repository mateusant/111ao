package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gis.GisEngine
import com.example.gis.IncidentLocationManager
import com.example.odk.OdkEngine
import com.example.ui.components.IncidentMapPickerDialog
import com.example.ui.components.SketchDrawingPad
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecNavyDark
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewIncidentScreen(
    viewModel: MainViewModel,
    onIncidentSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    // Form fields
    val autoIncidentNumber = remember { OdkEngine.generateIncidentNumber() }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.US).format(Date()) }

    var selectedCategory by remember { mutableStateOf("Roubo") }
    var incidentType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedMunicipio by remember { mutableStateOf(OdkEngine.MUNICIPIOS[0]) }
    var bairro by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }

    // GPS Geopoint state (Field GPS accuracy)
    var latitude by remember { mutableDoubleStateOf(-8.8383) }
    var longitude by remember { mutableDoubleStateOf(13.2344) }
    var gpsAccuracy by remember { mutableFloatStateOf(2.8f) }
    var altitude by remember { mutableDoubleStateOf(38.0) }
    var locationPinSource by remember { mutableStateOf("GPS Operacional") }
    var detectedAddressSummary by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var isLocatingGps by remember { mutableStateOf(false) }
    var showMapPickerDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                isLocatingGps = true
                val result = IncidentLocationManager.getCurrentPinnedLocation(context)
                result.onSuccess { loc ->
                    latitude = loc.latitude
                    longitude = loc.longitude
                    altitude = loc.altitude
                    gpsAccuracy = loc.accuracy
                    locationPinSource = "GPS em Tempo Real"
                    if (bairro.isBlank() && !loc.bairro.isNullOrBlank()) {
                        bairro = loc.bairro
                    }
                    if (locationName.isBlank() && !loc.streetAddress.isNullOrBlank()) {
                        locationName = loc.streetAddress
                    }
                    loc.municipio?.let { m ->
                        val match = OdkEngine.MUNICIPIOS.find { it.equals(m, ignoreCase = true) }
                        if (match != null) selectedMunicipio = match
                    }
                    detectedAddressSummary = listOfNotNull(loc.streetAddress, loc.bairro, loc.municipio).joinToString(" • ")
                    Toast.makeText(context, "Localização fixada com sucesso (±${loc.accuracy.toInt()}m)", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Erro ao obter GPS: ${it.message}", Toast.LENGTH_SHORT).show()
                }
                isLocatingGps = false
            }
        } else {
            Toast.makeText(context, "Permissão de localização necessária para fixar coordenadas via GPS.", Toast.LENGTH_LONG).show()
        }
    }

    fun requestAndPinCurrentLocation() {
        if (IncidentLocationManager.hasLocationPermission(context)) {
            coroutineScope.launch {
                isLocatingGps = true
                val result = IncidentLocationManager.getCurrentPinnedLocation(context)
                result.onSuccess { loc ->
                    latitude = loc.latitude
                    longitude = loc.longitude
                    altitude = loc.altitude
                    gpsAccuracy = loc.accuracy
                    locationPinSource = "GPS em Tempo Real"
                    if (bairro.isBlank() && !loc.bairro.isNullOrBlank()) {
                        bairro = loc.bairro
                    }
                    if (locationName.isBlank() && !loc.streetAddress.isNullOrBlank()) {
                        locationName = loc.streetAddress
                    }
                    loc.municipio?.let { m ->
                        val match = OdkEngine.MUNICIPIOS.find { it.equals(m, ignoreCase = true) }
                        if (match != null) selectedMunicipio = match
                    }
                    detectedAddressSummary = listOfNotNull(loc.streetAddress, loc.bairro, loc.municipio).joinToString(" • ")
                    Toast.makeText(context, "Localização fixada: ±${loc.accuracy.toInt()}m", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Erro de localização: ${it.message}", Toast.LENGTH_SHORT).show()
                }
                isLocatingGps = false
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (showMapPickerDialog) {
        IncidentMapPickerDialog(
            initialLat = latitude,
            initialLng = longitude,
            initialAccuracy = gpsAccuracy,
            onDismissRequest = { showMapPickerDialog = false },
            onLocationPinned = { lat, lng, acc, b, m, street ->
                latitude = lat
                longitude = lng
                gpsAccuracy = acc
                locationPinSource = "Marcador no Mapa"
                if (!b.isNullOrBlank()) {
                    bairro = b
                }
                if (!street.isNullOrBlank()) {
                    locationName = street
                }
                m?.let { mun ->
                    val match = OdkEngine.MUNICIPIOS.find { it.equals(mun, ignoreCase = true) }
                    if (match != null) selectedMunicipio = match
                }
                detectedAddressSummary = listOfNotNull(street, b, m).joinToString(" • ")
                Toast.makeText(context, "Ponto da ocorrência fixado no mapa!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Coordinate Display Mode (0: WGS84, 1: UTM, 2: DMS)
    var coordMode by remember { mutableIntStateOf(0) }

    // Priority
    var selectedPriority by remember { mutableStateOf("Média") }

    // Attachments
    var hasPhotoAttached by remember { mutableStateOf(false) }
    var hasCroquiAttached by remember { mutableStateOf(false) }
    var showSketchPad by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    // Validation State
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card: Form identification and ODK Engine header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SecCyanPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ODK-CRIM-01 (v2026.1)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = SecCyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = if (isOnline) SecEmerald.copy(alpha = 0.15f) else SecAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isOnline) "🟢 ONLINE" else "🟡 MODO OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) SecEmerald else SecAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Registo de Ocorrência Criminal Padrão",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nº: $autoIncidentNumber",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SecCyanPrimary
                        )
                        Text(
                            text = "$currentDate às $currentTime",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 1. Categoria Criminal
        item {
            Text(
                text = "1. CATEGORIA DO CRIME *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal chips wrap
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val chunked = OdkEngine.CATEGORIES.chunked(4)
                chunked.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowCategories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SecCyanPrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SecCyanLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = cat }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Tipologia e Descrição
        item {
            Text(
                text = "2. TIPOLOGIA E FACTOS *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = incidentType,
                onValueChange = { incidentType = it },
                label = { Text("Tipologia Específica") },
                placeholder = { Text("Ex: Roubo com arma de fogo na via pública") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição Detalhada dos Factos *") },
                placeholder = { Text("Descreva circunstâncias, suspeitos, vítimas, veículos e armas envolvidas...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Localização Administrativa
        item {
            Text(
                text = "3. LOCALIZAÇÃO ADMINISTRATIVA *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Município Dropdown Selector
            var muniExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = muniExpanded,
                onExpandedChange = { muniExpanded = !muniExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMunicipio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Município") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muniExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = muniExpanded,
                    onDismissRequest = { muniExpanded = false }
                ) {
                    OdkEngine.MUNICIPIOS.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = {
                                selectedMunicipio = m
                                muniExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bairro,
                onValueChange = { bairro = it },
                label = { Text("Bairro / Comuna *") },
                placeholder = { Text("Ex: Maianga Central") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Ponto de Referência / Endereço *") },
                placeholder = { Text("Ex: Rua Comandante Gika, junto ao Banco BFA") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Captura de Coordenadas GPS (ODK Geopoint)
        item {
            Text(
                text = "4. COORDENADAS GPS (GEOPOINT) *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Status & Accuracy Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (gpsAccuracy <= 10f) SecEmerald.copy(alpha = 0.15f) else SecAmber.copy(alpha = 0.15f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = if (gpsAccuracy <= 10f) SecEmerald else SecAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = locationPinSource,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Precisão GPS: ±${gpsAccuracy.toInt()}m",
                                    fontSize = 10.sp,
                                    color = if (gpsAccuracy <= 10f) SecEmerald else SecAmber
                                )
                            }
                        }

                        // Refresh / Location Services Status Icon
                        IconButton(
                            onClick = { requestAndPinCurrentLocation() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isLocatingGps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = SecCyanPrimary
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Atualizar Coordenadas", tint = SecCyanPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Action Buttons: Live GPS & Interactive Map Pinning
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Pin Current GPS
                        Button(
                            onClick = { requestAndPinCurrentLocation() },
                            colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isLocatingGps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("A obter sinal...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fixar Meu GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Button 2: Interactive Map Pinning
                        OutlinedButton(
                            onClick = { showMapPickerDialog = true },
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajustar no Mapa", fontSize = 11.sp, color = SecCyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Detected Address / Geocoding Notification
                    detectedAddressSummary?.let { summary ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecCyanPrimary.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Endereço: $summary",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coordinate Projection Selector (WGS 84, UTM, DMS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("WGS 84", "UTM (Zona 33S)", "DMS").forEachIndexed { idx, name ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (coordMode == idx) SecCyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { coordMode = idx }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (coordMode == idx) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val coordString = when (coordMode) {
                        0 -> String.format(Locale.US, "Lat: %.6f°, Lng: %.6f°", latitude, longitude)
                        1 -> GisEngine.toUtm(latitude, longitude)
                        else -> GisEngine.toDmsFormatted(latitude, longitude)
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = coordString,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Altitude: ${altitude}m • PostGIS: ${GisEngine.toWktPoint(latitude, longitude)}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Prioridade
        item {
            Text(
                text = "5. NÍVEL DE PRIORIDADE OPERACIONAL *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Baixa", "Média", "Alta", "Crítica").forEach { p ->
                    val isSelected = selectedPriority == p
                    val accentColor = when (p) {
                        "Crítica" -> SecRed
                        "Alta" -> SecAmber
                        "Média" -> SecCyanPrimary
                        else -> Color.Gray
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedPriority = p }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 6. Evidências Fotográficas e Croquis
        item {
            Text(
                text = "6. EVIDÊNCIAS E CROQUIS TÉCNICO",
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
                // Photo Attachment Action Button
                OutlinedButton(
                    onClick = {
                        hasPhotoAttached = !hasPhotoAttached
                        Toast.makeText(
                            context,
                            if (hasPhotoAttached) "Fotografia pericial anexada com sucesso" else "Foto removida",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (hasPhotoAttached) Icons.Default.Check else Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = if (hasPhotoAttached) SecEmerald else SecCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasPhotoAttached) "Foto Anexada" else "Anexar Foto",
                        fontSize = 11.sp,
                        color = if (hasPhotoAttached) SecEmerald else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Croqui Sketch Canvas Toggle
                OutlinedButton(
                    onClick = { showSketchPad = !showSketchPad },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = null,
                        tint = if (hasCroquiAttached) SecEmerald else SecCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasCroquiAttached) "Croqui Pronto" else "Desenhar Croqui",
                        fontSize = 11.sp,
                        color = if (hasCroquiAttached) SecEmerald else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Expandable Sketch Pad
            AnimatedVisibility(visible = showSketchPad) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    SketchDrawingPad(
                        onSketchSaved = { lineCount ->
                            hasCroquiAttached = lineCount > 0
                            showSketchPad = false
                            Toast.makeText(context, "Croqui salvo com $lineCount traços", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Error message if validation fails
        errorMessage?.let { msg ->
            item {
                Surface(
                    color = SecRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SecRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, fontSize = 12.sp, color = SecRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 7. Agente e Submissão
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecEmerald.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SecEmerald)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CONTA PARTICULAR DO AGENTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = SecEmerald
                            )
                        }
                        Text(
                            text = "${currentUser.patent} ${currentUser.name} (${currentUser.badgeNumber})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentUser.unit} • Ocorrência associada à conta particular de terreno",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Save Button (Registar Ocorrência)
            Button(
                onClick = {
                    if (description.isBlank()) {
                        errorMessage = "Por favor, preencha a descrição detalhada dos factos."
                        return@Button
                    }
                    if (locationName.isBlank()) {
                        errorMessage = "Por favor, especifique o ponto de referência / endereço."
                        return@Button
                    }
                    errorMessage = null

                    viewModel.createIncident(
                        incidentNumber = autoIncidentNumber,
                        date = currentDate,
                        time = currentTime,
                        category = selectedCategory,
                        type = if (incidentType.isNotBlank()) incidentType else selectedCategory,
                        description = description,
                        locationName = locationName,
                        municipio = selectedMunicipio,
                        bairro = bairro,
                        latitude = latitude,
                        longitude = longitude,
                        accuracy = gpsAccuracy,
                        altitude = altitude,
                        priority = selectedPriority,
                        hasPhoto = hasPhotoAttached,
                        hasCroqui = hasCroquiAttached,
                        notes = notes.ifBlank { null },
                        onSuccess = { newId ->
                            Toast.makeText(
                                context,
                                "Ocorrência $autoIncidentNumber registada com sucesso na conta particular do agente ${currentUser.name}!",
                                Toast.LENGTH_LONG
                            ).show()
                            onIncidentSaved(newId)
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Registar Ocorrência (Offline-First)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
