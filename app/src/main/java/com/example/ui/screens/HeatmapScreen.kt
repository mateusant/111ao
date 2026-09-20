package com.example.ui.screens

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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odk.OdkEngine
import com.example.ui.components.HeatmapCanvas
import com.example.ui.theme.HeatmapCritical
import com.example.ui.theme.HeatmapExtreme
import com.example.ui.theme.HeatmapHigh
import com.example.ui.theme.HeatmapLow
import com.example.ui.theme.HeatmapMed
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HeatmapScreen(
    viewModel: MainViewModel
) {
    val incidents by viewModel.incidents.collectAsState()

    var selectedCrimeType by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf("Todos") } // "Últimas 24h", "Últimos 7 dias", "Todos"
    var intensity by remember { mutableFloatStateOf(1.2f) }

    val filteredIncidents = remember(incidents, selectedCrimeType, selectedPeriod) {
        incidents.filter { inc ->
            (selectedCrimeType == null || inc.category == selectedCrimeType)
        }
    }

    // Top Critical Hotspot Zones Ranking
    val hotspotsRank = remember(filteredIncidents) {
        filteredIncidents.groupBy { "${it.bairro} (${it.municipio})" }
            .map { Pair(it.key, it.value.size) }
            .sortedByDescending { it.second }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Kernel Density Estimation Heatmap Canvas
        HeatmapCanvas(
            incidents = filteredIncidents,
            radiusIntensity = intensity
        )

        // Top Heatmap Controls & Filters
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.92f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = SecRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAPA DE CALOR & HOTSPOTS CRIMINAIS",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Text(
                        text = "${filteredIncidents.size} Ocorrências",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SecCyanPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Crime Type Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCrimeType == null,
                        onClick = { selectedCrimeType = null },
                        label = { Text("Todos os Crimes", fontSize = 10.sp) }
                    )

                    OdkEngine.CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = selectedCrimeType == cat,
                            onClick = { selectedCrimeType = if (selectedCrimeType == cat) null else cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Heat Radius Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Raio de Difusão:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = SecRed,
                            activeTrackColor = SecRed
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom Right: Heatmap Legend
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.90f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "DENSIDADE DE RISCO",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    Pair(HeatmapExtreme, "Extremo (Homicídios/Armas)"),
                    Pair(HeatmapCritical, "Muito Alto"),
                    Pair(HeatmapHigh, "Alto"),
                    Pair(HeatmapMed, "Médio"),
                    Pair(HeatmapLow, "Baixo")
                ).forEach { (color, label) ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = label, fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }

        // Bottom Left: Hotspots Ranking Card
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.90f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp)
                .width(180.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SecRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TOP ÁREAS CRÍTICAS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = SecRed,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (hotspotsRank.isEmpty()) {
                    Text(
                        text = "Sem dados criminais",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                } else {
                    hotspotsRank.take(3).forEachIndexed { index, (zone, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. $zone",
                                fontSize = 10.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "$count oc.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecRed
                            )
                        }
                    }
                }
            }
        }
    }
}
