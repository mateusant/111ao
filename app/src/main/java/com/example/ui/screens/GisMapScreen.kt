package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentEntity
import com.example.odk.OdkEngine
import com.example.ui.components.WebGisMapCanvas
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanLight
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecNavyDark
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GisMapScreen(
    viewModel: MainViewModel,
    onIncidentSelected: (IncidentEntity) -> Unit
) {
    val incidents by viewModel.mapIncidents.collectAsState()
    val surveys by viewModel.surveys.collectAsState()
    val pois by viewModel.pois.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedMunicipioFilter by remember { mutableStateOf<String?>(null) }

    val municipiosLuanda = remember {
        listOf("Ingombota", "Maianga", "Talatona", "Kilamba", "Viana", "Cazenga", "Cacuaco", "Belas")
    }

    val filteredIncidents = remember(incidents, selectedCategoryFilter, selectedMunicipioFilter) {
        incidents.filter { inc ->
            (selectedCategoryFilter == null || inc.category == selectedCategoryFilter) &&
            (selectedMunicipioFilter == null || inc.municipio.equals(selectedMunicipioFilter, ignoreCase = true) ||
                    inc.bairro?.contains(selectedMunicipioFilter!!, ignoreCase = true) == true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dedicated, Interactive GIS Tactical Map Canvas
        WebGisMapCanvas(
            incidents = filteredIncidents,
            surveys = surveys,
            pois = pois,
            onIncidentSelected = onIncidentSelected
        )

        // Top GIS Controls: Header Bar with Status, Municipality & Category Filters
        Surface(
            color = SecNavyDark.copy(alpha = 0.94f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Top Row: Title, Synchronization & Unified Layer Telemetry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SecCyanPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = SecCyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SISTEMA GIS TÁCTICO • VETORIAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) SecEmerald else SecAmber,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isOnline) "Cloud Firestore & Room Sincronizados" else "Modo Offline (Base Local Room)",
                                    fontSize = 10.sp,
                                    color = if (isOnline) SecEmerald else SecAmber
                                )
                            }
                        }
                    }

                    if (selectedCategoryFilter != null || selectedMunicipioFilter != null) {
                        Text(
                            text = "Limpar Filtros",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecCyanPrimary,
                            modifier = Modifier
                                .clickable {
                                    selectedCategoryFilter = null
                                    selectedMunicipioFilter = null
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Layer Metrics Strip: Pontos, Linhas, Polígonos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Points Layer Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecCyanPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("📍", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${filteredIncidents.size} Pontos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecCyanLight
                            )
                        }
                    }

                    // Lines Layer Badge
                    val lineCount = surveys.count { it.surveyType == "LINHA" }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecAmber.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("〰️", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$lineCount Linhas",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecAmber
                            )
                        }
                    }

                    // Polygons Layer Badge
                    val polyCount = surveys.count { it.surveyType == "POLIGONO" }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("⬡", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$polyCount Zonas",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecEmerald
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Municipality Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = SecCyanLight,
                        modifier = Modifier.size(15.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedMunicipioFilter == null) SecCyanPrimary else Color(0xFF1E293B),
                        modifier = Modifier.clickable { selectedMunicipioFilter = null }
                    ) {
                        Text(
                            text = "Todos Municípios",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMunicipioFilter == null) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    municipiosLuanda.forEach { mun ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMunicipioFilter == mun) SecCyanPrimary else Color(0xFF1E293B),
                            modifier = Modifier.clickable {
                                selectedMunicipioFilter = if (selectedMunicipioFilter == mun) null else mun
                            }
                        ) {
                            Text(
                                text = mun,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMunicipioFilter == mun) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Crime Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = SecCyanLight,
                        modifier = Modifier.size(15.dp)
                    )

                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Todas Categorias (${filteredIncidents.size})", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecCyanPrimary.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White
                        )
                    )

                    OdkEngine.CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecCyanPrimary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
