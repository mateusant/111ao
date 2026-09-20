package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.HourlyIncidenceChart
import com.example.ui.components.TacticalBarChart
import com.example.ui.components.TacticalKpiCard
import com.example.ui.theme.SecAmber
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel
) {
    val incidents by viewModel.incidents.collectAsState()

    val currentDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val currentDateFormatted = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("pt-AO")).format(Date()) }

    val totalCount = incidents.size
    val todayCount = incidents.count { it.date == currentDateStr }
    val resolvedCount = incidents.count { it.status == "Concluída" || it.status == "Validada" }
    val pendingCount = incidents.count { it.status == "Registada" || it.status == "Em Investigação" }

    // Categories Distribution
    val categoryDistribution = remember(incidents) {
        incidents.groupBy { it.category }
            .map { Pair(it.key, it.value.size) }
            .sortedByDescending { it.second }
    }

    // Municipal Distribution
    val municipalDistribution = remember(incidents) {
        incidents.groupBy { it.municipio }
            .map { Pair(it.key, it.value.size) }
            .sortedByDescending { it.second }
    }

    // Hourly Incidence (00h - 23h)
    val hourlyData = remember(incidents) {
        val map = (0..23).associateWith { 0 }.toMutableMap()
        incidents.forEach { inc ->
            try {
                val hour = inc.time.split(":")[0].toInt()
                map[hour] = (map[hour] ?: 0) + 1
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        map
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
            Column {
                Text(
                    text = "PAINEL DE INTELIGÊNCIA GEOGRÁFICA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Análise criminal, padrões espaço-temporais e gestão operacional SIG",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Primary Operational KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalKpiCard(
                    title = "Total Ocorrências",
                    value = "$totalCount",
                    subtitle = "Registadas no SIG",
                    icon = Icons.Default.Assessment,
                    accentColor = SecCyanPrimary,
                    modifier = Modifier.weight(1f)
                )

                TacticalKpiCard(
                    title = "Ocorrências Hoje",
                    value = "$todayCount",
                    subtitle = currentDateFormatted,
                    icon = Icons.Default.Today,
                    accentColor = SecEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalKpiCard(
                    title = "Validadas / Fechadas",
                    value = "$resolvedCount",
                    subtitle = "${if (totalCount > 0) (resolvedCount * 100 / totalCount) else 0}% do total",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SecEmerald,
                    modifier = Modifier.weight(1f)
                )

                TacticalKpiCard(
                    title = "Em Investigação",
                    value = "$pendingCount",
                    subtitle = "Diligências ativas",
                    icon = Icons.Default.HourglassTop,
                    accentColor = SecAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (incidents.isEmpty()) {
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
                            shape = CircleShape,
                            color = SecCyanPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = SecCyanPrimary, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Sem Dados de Ocorrências para Análise",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "À medida que os agentes em campo registarem ocorrências e levantamentos cartográficos via ODK, os gráficos de distribuição, incidência horária e setorização geográfica serão atualizados em tempo real.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // 2. Category Donut Chart
            item {
                CategoryDonutChart(categories = categoryDistribution)
            }

            // 3. Hourly Incidence Bar Chart
            item {
                HourlyIncidenceChart(hourlyData = hourlyData)
            }

            // 4. Municipal Distribution Bar Chart
            item {
                TacticalBarChart(
                    title = "Ocorrências por Município",
                    data = municipalDistribution,
                    accentColor = SecCyanPrimary
                )
            }
        }
    }
}
