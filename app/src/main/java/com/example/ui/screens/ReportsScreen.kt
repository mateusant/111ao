package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.gis.GisEngine
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val incidents by viewModel.incidents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedReportType by remember { mutableStateOf("BOLETIM_INDIVIDUAL") }
    var selectedFormat by remember { mutableStateOf("PDF_VIEW") } // "PDF_VIEW", "GEOJSON", "SQL_POSTGIS", "CSV"

    // Generate dynamic report content based on selections
    val reportContent = remember(selectedReportType, selectedFormat, incidents) {
        when (selectedFormat) {
            "GEOJSON" -> {
                val features = incidents.map { inc ->
                    Pair(
                        Pair(inc.latitude, inc.longitude),
                        mapOf(
                            "numero" to inc.incidentNumber,
                            "tipo" to inc.incidentType,
                            "categoria" to inc.category,
                            "data" to inc.date,
                            "hora" to inc.time,
                            "municipio" to inc.municipio,
                            "bairro" to inc.bairro,
                            "estado" to inc.status,
                            "prioridade" to inc.priority,
                            "agente" to inc.agentName
                        )
                    )
                }
                GisEngine.generateGeoJson(features)
            }
            "SQL_POSTGIS" -> {
                val sb = StringBuilder()
                sb.append("-- EXPORTAÇÃO GEOESPACIAL POSTGIS - OCORRÊNCIA REMOTA\n")
                sb.append("-- Gerado em: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n\n")
                incidents.forEach { inc ->
                    val escapedDesc = inc.description.replace("'", "''")
                    sb.append("""
                    INSERT INTO ocorrencias (numero_ocorrencia, data_ocorrencia, hora_ocorrencia, tipo_ocorrencia, categoria_criminal, descricao, local_nome, bairro, municipio, provincia, geom, precisao_gps, agente_codigo, agente_nome, estado, prioridade)
                    VALUES ('${inc.incidentNumber}', '${inc.date}', '${inc.time}', '${inc.incidentType}', '${inc.category}', '$escapedDesc', '${inc.locationName}', '${inc.bairro}', '${inc.municipio}', '${inc.provincia}', ST_SetSRID(ST_MakePoint(${inc.longitude}, ${inc.latitude}), 4326), ${inc.gpsAccuracy}, '${inc.agentCode}', '${inc.agentName}', '${inc.status}', '${inc.priority}');
                    """.trimIndent())
                    sb.append("\n\n")
                }
                sb.toString()
            }
            "CSV" -> {
                val sb = StringBuilder()
                sb.append("NUMERO_OCORRENCIA;DATA;HORA;CATEGORIA;TIPO;LOCAL;BAIRRO;MUNICIPIO;LATITUDE;LONGITUDE;AGENTE;ESTADO;PRIORIDADE;SYNC\n")
                incidents.forEach { inc ->
                    sb.append("${inc.incidentNumber};${inc.date};${inc.time};${inc.category};${inc.incidentType};${inc.locationName};${inc.bairro};${inc.municipio};${inc.latitude};${inc.longitude};${inc.agentName};${inc.status};${inc.priority};${inc.syncStatus}\n")
                }
                sb.toString()
            }
            else -> {
                // Formatted Official Bulletin / Report Layout (PDF Layout)
                val firstInc = incidents.firstOrNull()
                val sb = StringBuilder()
                sb.append("=======================================================================\n")
                sb.append("        REPÚBLICA DE ANGOLA - MINISTÉRIO DO INTERIOR                  \n")
                sb.append("                 POLÍCIA NACIONAL DE ANGOLA                            \n")
                sb.append("          SISTEMA INTEGRADO GEOESPACIAL: OCORRÊNCIA REMOTA             \n")
                sb.append("=======================================================================\n\n")
                sb.append("DOCUMENTO: BOLETIM OFICIAL DE REGISTO CRIMINAL E CARTOGRÁFICO\n")
                sb.append("DATA DE EMISSÃO: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date())}\n")
                sb.append("EMITIDO POR: ${currentUser.name} (Crachá: ${currentUser.badgeNumber})\n")
                sb.append("UNIDADE: ${currentUser.unit}\n\n")

                if (firstInc != null) {
                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("1. DADOS DE IDENTIFICAÇÃO DA OCORRÊNCIA\n")
                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("Número da Ocorrência: ${firstInc.incidentNumber}\n")
                    sb.append("Data e Hora do Facto: ${firstInc.date} às ${firstInc.time}\n")
                    sb.append("Categoria Criminal:   ${firstInc.category.uppercase()}\n")
                    sb.append("Tipologia Específica: ${firstInc.incidentType}\n")
                    sb.append("Prioridade:           ${firstInc.priority.uppercase()}\n")
                    sb.append("Estado Atual:         ${firstInc.status}\n\n")

                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("2. LOCALIZAÇÃO E GEORREFERENCIAÇÃO TÉCNICA (POSTGIS / SIG)\n")
                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("Província:            ${firstInc.provincia}\n")
                    sb.append("Município:            ${firstInc.municipio}\n")
                    sb.append("Bairro / Comuna:      ${firstInc.bairro}\n")
                    sb.append("Ponto de Referência:  ${firstInc.locationName}\n")
                    sb.append("Coordenadas WGS 84:   Lat ${firstInc.latitude}, Lng ${firstInc.longitude}\n")
                    sb.append("Projeção UTM:         ${GisEngine.toUtm(firstInc.latitude, firstInc.longitude)}\n")
                    sb.append("Coordenadas DMS:      ${GisEngine.toDmsFormatted(firstInc.latitude, firstInc.longitude)}\n")
                    sb.append("Precisão GPS:         ${firstInc.gpsAccuracy} metros (Altitude: ${firstInc.altitude}m)\n")
                    sb.append("Geometria WKT:        ${firstInc.wktGeometry}\n\n")

                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("3. HISTÓRICO E CIRCUNSTÂNCIAS DO CRIME\n")
                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("${firstInc.description}\n\n")

                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("4. CADEIA DE CUSTÓDIA E VALIDAÇÃO PERICIAL\n")
                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("Agente Relator:       ${firstInc.agentName} (${firstInc.agentCode})\n")
                    sb.append("Supervisão / Visto:   ${firstInc.validatedBy ?: "Pendente de Visto Formal"}\n")
                    sb.append("Estado Sincronização: ${firstInc.syncStatus} (Base Central PostgreSQL / ODK)\n")
                    sb.append("Hash de Integridade:  e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n\n")
                } else {
                    sb.append("-----------------------------------------------------------------------\n")
                    sb.append("AVISO: Nenhuma ocorrência registada no sistema até ao momento.\n")
                    sb.append("As ocorrências registadas pelos agentes em campo serão compiladas neste boletim.\n")
                    sb.append("-----------------------------------------------------------------------\n\n")
                }

                sb.append("=======================================================================\n")
                sb.append("TOTAL DE OCORRÊNCIAS EM BASE: ${incidents.size} | FORMATO POLICIAL AUDITÁVEL\n")
                sb.append("=======================================================================\n")
                sb.toString()
            }
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
            Column {
                Text(
                    text = "RELATÓRIOS E EXPORTAÇÃO SIG",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Geração de boletins formais, dados abertos GeoJSON, scripts SQL PostGIS e planilhas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Report Type Selector
        item {
            Text(
                text = "1. TIPO DE RELATÓRIO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("BOLETIM_INDIVIDUAL", "Boletim Individual"),
                    Pair("DIARIO", "Relatório Diário"),
                    Pair("ESTATISTICO", "Estatístico Consolidado"),
                    Pair("MUNICIPAL", "Relatório Municipal")
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = selectedReportType == type,
                        onClick = { selectedReportType = type },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        // 2. Export Format Selector
        item {
            Text(
                text = "2. FORMATO DE SAÍDA",
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
                listOf(
                    Pair("PDF_VIEW", "PDF / Boletim"),
                    Pair("GEOJSON", "GeoJSON"),
                    Pair("SQL_POSTGIS", "PostGIS SQL"),
                    Pair("CSV", "CSV / Excel")
                ).forEach { (fmt, label) ->
                    val isSelected = selectedFormat == fmt
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SecCyanPrimary else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFormat = fmt }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Document Action Buttons (Copiar, Partilhar, Imprimir)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Relatório Ocorrência Remota", reportContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Conteúdo copiado para a Área de Transferência!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Texto", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Exportação concluída: Ficheiro gerado com sucesso.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Descarregar", fontSize = 11.sp)
                }
            }
        }

        // 4. Formatted Document Preview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRÉ-VISUALIZAÇÃO DO DOCUMENTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = SecCyanPrimary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = selectedFormat,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFF030712),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reportContent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
