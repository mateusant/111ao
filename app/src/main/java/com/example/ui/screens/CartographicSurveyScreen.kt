package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartographicSurveyScreen(
    viewModel: MainViewModel,
    onSurveySaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    val surveyCode = remember { OdkEngine.generateSurveyCode() }
    var title by remember { mutableStateOf("") }
    var selectedGeomType by remember { mutableStateOf("POLIGONO") } // "PONTO", "LINHA", "POLIGONO", "CROQUI"
    var selectedCoordSystem by remember { mutableStateOf("WGS 84") } // "WGS 84", "UTM", "GEOGRAFICAS_DMS"

    var selectedMunicipio by remember { mutableStateOf(OdkEngine.MUNICIPIOS[0]) }
    var bairro by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Live Collected GPS Vertices from Fieldwork
    val vertices = remember {
        mutableStateListOf<Pair<Double, Double>>()
    }

    // Current Live GPS Position
    var curLat by remember { mutableDoubleStateOf(-8.8383) }
    var curLng by remember { mutableDoubleStateOf(13.2344) }
    var curAccuracy by remember { mutableFloatStateOf(2.5f) }
    var isCapturingGps by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val surveyLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                isCapturingGps = true
                val result = IncidentLocationManager.getCurrentPinnedLocation(context)
                result.onSuccess { loc ->
                    curLat = loc.latitude
                    curLng = loc.longitude
                    curAccuracy = loc.accuracy
                    vertices.add(Pair(curLat, curLng))
                    if (bairro.isBlank() && !loc.bairro.isNullOrBlank()) {
                        bairro = loc.bairro
                    }
                    loc.municipio?.let { m ->
                        val matched = OdkEngine.MUNICIPIOS.find { it.equals(m, ignoreCase = true) }
                        if (matched != null) selectedMunicipio = matched
                    }
                    Toast.makeText(context, "Vértice #${vertices.size} capturado via GPS (±${loc.accuracy.toInt()}m)", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    vertices.add(Pair(curLat, curLng))
                    Toast.makeText(context, "Vértice #${vertices.size} adicionado", Toast.LENGTH_SHORT).show()
                }
                isCapturingGps = false
            }
        } else {
            Toast.makeText(context, "Permissão de GPS necessária para capturar pontos em campo.", Toast.LENGTH_LONG).show()
        }
    }

    fun captureVertexWithGps() {
        if (IncidentLocationManager.hasLocationPermission(context)) {
            coroutineScope.launch {
                isCapturingGps = true
                val result = IncidentLocationManager.getCurrentPinnedLocation(context)
                result.onSuccess { loc ->
                    curLat = loc.latitude
                    curLng = loc.longitude
                    curAccuracy = loc.accuracy
                    vertices.add(Pair(curLat, curLng))
                    if (bairro.isBlank() && !loc.bairro.isNullOrBlank()) {
                        bairro = loc.bairro
                    }
                    loc.municipio?.let { m ->
                        val matched = OdkEngine.MUNICIPIOS.find { it.equals(m, ignoreCase = true) }
                        if (matched != null) selectedMunicipio = matched
                    }
                    Toast.makeText(context, "Vértice #${vertices.size} capturado via GPS (±${loc.accuracy.toInt()}m)", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    vertices.add(Pair(curLat, curLng))
                    Toast.makeText(context, "Vértice #${vertices.size} fixado", Toast.LENGTH_SHORT).show()
                }
                isCapturingGps = false
            }
        } else {
            surveyLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Dynamic Calculations
    val calculatedLengthMeters = remember(vertices.toList()) {
        var total = 0.0
        for (i in 0 until vertices.size - 1) {
            total += GisEngine.haversineDistanceMeters(
                vertices[i].first, vertices[i].second,
                vertices[i + 1].first, vertices[i + 1].second
            )
        }
        total
    }

    val calculatedAreaM2 = remember(vertices.toList(), selectedGeomType) {
        if (selectedGeomType == "POLIGONO" && vertices.size >= 3) {
            GisEngine.calculatePolygonAreaM2(vertices.toList())
        } else {
            0.0
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecEmerald.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SecEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ODK-GEO-02 (Módulo Cartográfico)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = SecEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = surveyCode,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SecEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Levantamento Geoespacial de Campo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Coleta de pontos, perímetros de risco, rotas de fuga e infraestruturas com suporte a PostGIS.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. Tipo de Geometria
        item {
            Text(
                text = "1. TIPO DE GEOMETRIA ESPACIAL *",
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
                listOf(
                    Pair("PONTO", "Ponto"),
                    Pair("LINHA", "Linha / Rota"),
                    Pair("POLIGONO", "Polígono"),
                    Pair("CROQUI", "Croqui")
                ).forEach { (type, label) ->
                    val isSelected = selectedGeomType == type
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SecEmerald else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedGeomType = type }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. Título do Levantamento
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Designação do Local / Infraestrutura *") },
                placeholder = { Text("Ex: Perímetro de Assaltos Noturnos ou Rota de Fuga") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Sistema de Coordenadas
        item {
            Text(
                text = "2. SISTEMA DE COORDENADAS CARTOGRÁFICO",
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
                listOf("WGS 84", "UTM", "GEOGRAFICAS_DMS").forEach { cs ->
                    val isSelected = selectedCoordSystem == cs
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SecCyanPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCoordSystem = cs }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cs,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Coleta de Vértices GPS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vértices Coletados: ${vertices.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Precisão atual: ${curAccuracy}m",
                                fontSize = 11.sp,
                                color = SecEmerald
                            )
                        }

                        Button(
                            onClick = { captureVertexWithGps() },
                            colors = ButtonDefaults.buttonColors(containerColor = SecEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isCapturingGps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("A capturar...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Capturar Ponto", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Display (Length & Area)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (calculatedLengthMeters > 0) {
                            Text(
                                text = "Comprimento: ${calculatedLengthMeters.toInt()}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecAmber
                            )
                        }
                        if (calculatedAreaM2 > 0) {
                            val ha = calculatedAreaM2 / 10000.0
                            Text(
                                text = String.format(Locale.US, "Área: %.1f m² (%.2f ha)", calculatedAreaM2, ha),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecCyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vertices table list
                    if (vertices.isEmpty()) {
                        Text(
                            text = "Nenhum ponto capturado ainda. Prima 'Capturar Ponto' para registar a coordenada GPS atual.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        vertices.forEachIndexed { index, vertex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "P${index + 1}: ${String.format(Locale.US, "%.5f, %.5f", vertex.first, vertex.second)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover",
                                    tint = SecRed,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            vertices.removeAt(index)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Localização Administrativa
        item {
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
                label = { Text("Bairro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações Técnicas / Diagnóstico") },
                placeholder = { Text("Descreva anomalias, iluminação, pontos cegos ou recomendações...") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Save Button
        item {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Por favor, indique a designação do local.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (selectedGeomType == "POLIGONO" && vertices.size < 3) {
                        Toast.makeText(context, "Um polígono requer pelo menos 3 vértices capturados.", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else if (selectedGeomType == "LINHA" && vertices.size < 2) {
                        Toast.makeText(context, "Uma linha/rota requer pelo menos 2 pontos capturados.", Toast.LENGTH_SHORT).show()
                        return@Button
                    } else if (vertices.isEmpty()) {
                        // If no vertices captured, capture current location as point
                        vertices.add(Pair(curLat, curLng))
                    }

                    // Build JSON string of vertices
                    val jsonArray = JSONArray()
                    vertices.forEach { v ->
                        val obj = JSONObject()
                        obj.put("lat", v.first)
                        obj.put("lng", v.second)
                        jsonArray.put(obj)
                    }

                    viewModel.createSurvey(
                        surveyCode = surveyCode,
                        title = title,
                        surveyType = selectedGeomType,
                        coordinateSystem = selectedCoordSystem,
                        coordinatesJson = jsonArray.toString(),
                        lengthMeters = calculatedLengthMeters,
                        areaM2 = calculatedAreaM2,
                        municipio = selectedMunicipio,
                        bairro = bairro,
                        notes = notes,
                        onSuccess = { id ->
                            Toast.makeText(context, "Levantamento $surveyCode registado com sucesso!", Toast.LENGTH_LONG).show()
                            onSurveySaved(id)
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecEmerald),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Registar Levantamento Cartográfico",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
